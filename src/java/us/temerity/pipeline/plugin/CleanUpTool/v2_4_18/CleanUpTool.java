// $Id: CleanUpTool.java,v 1.1 2009/12/14 03:20:56 jim Exp $

package us.temerity.pipeline.plugin.CleanUpTool.v2_4_18;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;


/*------------------------------------------------------------------------------------------*/
/*   C L E A N   U P   T O O L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Analyses the nodes and associated jobs in a working area and optionally performs 
 * preparatory steps required for removal of the working area. 
 */
public 
class CleanUpTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  CleanUpTool()
  {
    super("CleanUp", new VersionID("2.4.18"), "Temerity",
          "Analyses the nodes and associated jobs in a working area and optionally " + 
          "performs preparatory steps required for removal of the working area."); 
    
    addPhase(new PhaseOne());
    addPhase(new PhaseTwo());
    addPhase(new PhaseThree());
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   O N E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private 
  class PhaseOne
    extends BaseTool.ToolPhase
  {
    public 
    PhaseOne() 
    {
      super();
    }
    
    /**
     * Just shows message.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    @Override
    public String
    collectInput() 
      throws PipelineException 
    {
      return ": Searching for Unfinished Jobs.";
    }
    
    /**
     * Search for unfinished jobs associated with the nodes in the working area...
     */
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      pUnfinishedJobs = mclient.getUnfinishedJobs(getAuthor(), getView());

      return NextPhase.Continue;
    }
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   T W O                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class PhaseTwo
    extends BaseTool.ToolPhase
  {
    public 
    PhaseTwo() 
    {
      super();
    }
    
    /**
     * Display any unfinished jobs and give kill controls.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    @Override
    public String
    collectInput() 
      throws PipelineException 
    {
      if(pUnfinishedJobs.isEmpty())
        return (": Finding Root Nodes."); 

      /* show nodes with jobs to kill */
      JPhaseTwoDialog toolDialog = null; 
      UnfinishedJobsTableModel model = null;
      {
	JPanel panel = new JPanel();
	panel.setName("MainPanel");
	panel.setLayout(new BorderLayout());
	   
        model = new UnfinishedJobsTableModel();
        model.setData(pUnfinishedJobs);
        panel.add(new JTablePanel(model)); 
        
        String extra [][] = {
          { "Keep Jobs", "keep-jobs" }
        };

        toolDialog = new JPhaseTwoDialog(panel, extra); 
      }
      
      toolDialog.setVisible(true);
      if(toolDialog.wasConfirmed()) {
        if(pKeepJobs) 
          return (": Finding Root Nodes."); 
        else {
          pKillNodeNames = model.getKillNames(); 
          return (": Killing Jobs, Finding Root Nodes."); 
        }
      }
          
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
      if((pKillNodeNames != null) && !pKillNodeNames.isEmpty()) {
        TreeSet<Long> ids = new TreeSet<Long>();
        for(String name : pKillNodeNames) 
          ids.addAll(pUnfinishedJobs.get(name));
        qclient.killJobs(ids);
      }

      pWorkingAreaRoots = mclient.getWorkingRootNames(getAuthor(), getView());
      if(pWorkingAreaRoots.isEmpty())
        throw new PipelineException
          ("The working area must be empty already!"); 

      return NextPhase.Continue;
    }

    private class
    JPhaseTwoDialog
      extends JToolDialog
    {
      JPhaseTwoDialog
      (
       JComponent body, 
       String extra[][]
      ) 
      {
        super("Clean Up: Unfinished Jobs", body, "Kill Jobs", null, extra);
      }

      public void 
      actionPerformed
      (
       ActionEvent e
      ) 
      {
        if(e.getActionCommand().equals("keep-jobs")) {
          pKeepJobs = true;
          doConfirm();
        }
        else {
          super.actionPerformed(e);
        }
      }
    }

    private boolean pKeepJobs;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   T H R E E                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class PhaseThree
    extends BaseTool.ToolPhase
  {
    public 
    PhaseThree() 
    {
      super();
    }
    
    /**
     * Display the root node names and toggles for whether to display them.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    @Override
    public String
    collectInput() 
      throws PipelineException 
    {
      /* show nodes root nodes */
      JToolDialog toolDialog = null; 
      RootNodesTableModel model = null;
      {
	JPanel panel = new JPanel();
	panel.setName("MainPanel");
	panel.setLayout(new BorderLayout());
	   
        model = new RootNodesTableModel();
        model.setData(pWorkingAreaRoots);
        panel.add(new JTablePanel(model)); 
        
        toolDialog = new JToolDialog("Clean Up: Working Area Roots", panel, "Show Nodes");
      }
      
      toolDialog.setVisible(true);
      if(!toolDialog.wasConfirmed())
        return null;

      pRoots.clear(); 
      pRoots.addAll(model.getRootNames()); 

      return (": Killing Jobs, Finding Root Nodes."); 
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
      return NextPhase.Finish;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5357606989127687859L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unfinished jobIDs indexed node name.
   */ 
  private MappedSet<String,Long>  pUnfinishedJobs; 

  /**
   * The names of the nodes who's jobs should be killed. 
   */ 
  private TreeSet<String>  pKillNodeNames; 

  /**
   * The names of the mode downstream nodes in the working area.
   */ 
  private TreeSet<String>  pWorkingAreaRoots; 


}
