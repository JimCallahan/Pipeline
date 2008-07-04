// $Id: JobRankingTool.java,v 1.1 2008/07/04 15:27:56 jesse Exp $

package com.theorphanage.pipeline.plugin.JobRankingTool.v1_0_0;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;


public 
class JobRankingTool
  extends CommonToolUtils
{
  
  public 
  JobRankingTool()
  {
    super("JobRanking", new VersionID("1.0.0"), "TheO",
          "Ranks the jobs contained in a particular project and department.");
    
    underDevelopment();
    
    addPhase(new PassOne());
    addPhase(new PassTwo());
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  private 
  class PassOne
    extends ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      return ": Analyzing the Queue";
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      pDepartmentKeys = new TreeSet<String>();
      pProjectKeys = new TreeSet<String>();
      
      pDepartmentNames = new TreeMap<String, String>();
      pProjectNames = new TreeMap<String, String>();
      
      pJobs = new TreeMap<Long, QueueJob>();
      pJobInfos = new TreeMap<Long, QueueJobInfo>();
      
      pProjectJobs = new MappedSet<String, Long>();
      pDepartmentJobs = new MappedSet<String, Long>();
      
      pJobSubmissionsTimes = new TreeMap<Long, Long>();
      pJobPriorities = new TreeMap<Long, Integer>();
      pJobNodeNames = new TreeMap<Long, String>();
      
      for (String key : qclient.getSelectionKeyNames(false)) {
        if (key.startsWith("DEPT_")) {
          pDepartmentKeys.add(key);
          String name = key.replaceFirst("DEPT_", "");
          pDepartmentNames.put(name, key);
        }
        else if (key.startsWith("SHOW_")) {
          pProjectKeys.add(key);
          String name = key.replaceFirst("SHOW_", "");
          pProjectNames.put(name, key);
        }
      }
      
      TreeSet<Long> ids = new TreeSet<Long>();
      for (QueueJobGroup group : qclient.getJobGroups().values()) {
        for (Long jobID : group.getJobIDs()) {
          ids.add(jobID);
        }
        
        pJobs = new TreeMap<Long, QueueJob>(qclient.getJobs(ids));
        pJobInfos = new TreeMap<Long, QueueJobInfo>(qclient.getJobInfos(ids));

        for (Long jobID : pJobs.keySet()) {
          QueueJob job = pJobs.get(jobID);
          QueueJobInfo info = pJobInfos.get(jobID);
          
          switch (info.getState()) {
          case Paused:
          case Preempted:
          case Queued:
            Set<String> selectionKeys = 
              job.getJobRequirements().getSelectionKeys();
            for (String key : selectionKeys) {
              if (pProjectKeys.contains(key))
                pProjectJobs.put(key, jobID);
              else if (pDepartmentKeys.contains(key))
                pDepartmentJobs.put(key, jobID);
            }  
            
            Integer priority = job.getJobRequirements().getPriority();
            long submission = info.getSubmittedStamp();
            pJobPriorities.put(jobID, priority);
            pJobSubmissionsTimes.put(jobID, submission);
            pJobNodeNames.put(jobID, job.getNodeID().getName());
            
            break;
          }
          pJobs.put(jobID, job);
          pJobInfos.put(jobID, info);
        }
      }
      
      return NextPhase.Continue;
    }
  }
  
  private 
  class PassTwo
    extends ToolPhase
    implements ActionListener
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      pTables = new TreeMap<String, JobRankingTableModel>();
      
      JPanel topPanel = new JPanel();
      BoxLayout layout = new BoxLayout(topPanel, BoxLayout.Y_AXIS);
      topPanel.setLayout(layout);
      JToolDialog dialog = new JToolDialog("Job Rank", topPanel, "Confirm");

      topPanel.add(Box.createVerticalStrut(10));
      
      {
        Box hbox = new Box(BoxLayout.X_AXIS);
        pProjectField = new JCollectionField(pProjectNames.keySet(), dialog);
        Dimension old = pProjectField.getPreferredSize();
        pProjectField.setMinimumSize(new Dimension(sVSize, old.height));
        pProjectField.setMaximumSize(new Dimension(sVSize, old.height));
        pDepartmentField = new JCollectionField(pDepartmentNames.keySet(), dialog);
        pDepartmentField.setMinimumSize(new Dimension(sVSize, old.height));
        pDepartmentField.setMaximumSize(new Dimension(sVSize, old.height));
        hbox.add(Box.createHorizontalStrut(5));
        hbox.add(new JLabel("Projects:"));
        hbox.add(Box.createHorizontalStrut(5));
        hbox.add(pProjectField);
        hbox.add(Box.createHorizontalStrut(15));
        hbox.add(new JLabel("Departments:"));
        hbox.add(Box.createHorizontalStrut(5));
        hbox.add(pDepartmentField);
        topPanel.add(hbox);
      }
      
      topPanel.add(Box.createVerticalStrut(10));
      
      {      
        Box hbox = new Box(BoxLayout.X_AXIS);

        pCardPanel = new JPanel();
        pCardPanelLayout = new CardLayout();
        pCardPanel.setLayout(pCardPanelLayout);
        
        hbox.add(pCardPanel);

        hbox.setMinimumSize(new Dimension(870, 500));
        hbox.setMaximumSize(new Dimension(870, Integer.MAX_VALUE));

        topPanel.add(hbox);
      }
      
      pProjectField.addActionListener(this);
      pDepartmentField.addActionListener(this);
      
      dialog.setMinimumSize(new Dimension(880, 600));
      dialog.setMaximumSize(new Dimension(880, Integer.MAX_VALUE));
      dialog.setVisible(true);
      
      if (dialog.wasConfirmed())
        return ": Updating Jobs";
      
      return null;
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      LinkedList<JobReqsDelta> deltas = new LinkedList<JobReqsDelta>();
      
      for (String key : pTables.keySet()) {
        JobRankingTableModel model = pTables.get(key);
        TreeMap<Long, Integer> priorities = model.getPriorities();
        for (Long jobID : priorities.keySet()) {
          Integer oldPriority = pJobPriorities.get(jobID);
          Integer newPriority = priorities.get(jobID);
          if (oldPriority != newPriority) {
            JobReqsDelta delta = 
              new JobReqsDelta(jobID, newPriority, null, null, null, null, null, null, null);
            deltas.add(delta);
          }
        }
      }
      
      qclient.changeJobReqs(deltas);
      
      return NextPhase.Finish;
    }
    
    
    
    private void
    updateTable
    (
      String project,
      String department
    )
    {
      String key = project + "_" + department;
      if (pTables.containsKey(key)) {
        pCardPanelLayout.show(pCardPanel, key);
      }
      else {
        TreeSet<Long> projectJobs = pProjectJobs.get(pProjectNames.get(project));
        TreeSet<Long> departJobs = pDepartmentJobs.get(pDepartmentNames.get(department));
        
        TreeSet<Long> allJobs = new TreeSet<Long>();
        if (projectJobs != null && departJobs != null ) {
          allJobs = new TreeSet<Long>(projectJobs);
          allJobs.retainAll(departJobs);
        }
        
        TreeMap<Long, Long> jobSubmissionsTimes = new TreeMap<Long, Long>();
        TreeMap<Long, Integer> jobPriorities = new TreeMap<Long, Integer>();
        TreeMap<Long, String> jobNodeNames = new TreeMap<Long, String>();
        for (Long jobID : allJobs) {
          jobSubmissionsTimes.put(jobID, pJobSubmissionsTimes.get(jobID));
          jobPriorities.put(jobID, pJobPriorities.get(jobID));
          jobNodeNames.put(jobID, pJobNodeNames.get(jobID));
        }
        
        JobRankingTableModel tableModel = 
          new JobRankingTableModel(jobNodeNames, jobSubmissionsTimes, jobPriorities);
        JTablePanel tpanel =
          new JTablePanel(tableModel, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        pCardPanel.add(tpanel, key);
        pCardPanelLayout.show(pCardPanel, key);
        pTables.put(key, tableModel);
      }
    }



    @Override
    public void actionPerformed(
      ActionEvent e)
    {
      String project = pProjectField.getSelected();
      String depart = pDepartmentField.getSelected();
      updateTable(project, depart);
    }
  }
  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2438893749228137165L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 300;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private TreeSet<String> pDepartmentKeys;
  private TreeMap<String, String> pDepartmentNames;
  private TreeSet<String> pProjectKeys;
  private TreeMap<String, String> pProjectNames;
  
  private TreeMap<Long, QueueJob> pJobs;
  private TreeMap<Long, QueueJobInfo> pJobInfos;
  
  private MappedSet<String, Long> pProjectJobs;
  private MappedSet<String, Long> pDepartmentJobs;
  
  private TreeMap<Long, Long> pJobSubmissionsTimes;
  private TreeMap<Long, Integer> pJobPriorities;
  private TreeMap<Long, String> pJobNodeNames;
  
  private JCollectionField pProjectField;
  private JCollectionField pDepartmentField;
  
  private JPanel pCardPanel;
  private CardLayout pCardPanelLayout;
  
  private TreeMap<String, JobRankingTableModel> pTables;
}
