// $Id: QueueBalanceTool.java,v 1.1 2008/07/04 15:27:56 jesse Exp $

package com.theorphanage.pipeline.plugin.QueueBalanceTool.v1_0_0;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/**
 * Create and balance selection groups on the farm
 * <p>
 * Depends upon having two sorts of selection keys, ones that start with SHOW and
 * ones that start with DEPT.  This will create selection groups that combine each
 * SHOW with all the departments and then asks you for percentages of machines to
 * assign to each group.
 *
 */
public 
class QueueBalanceTool
  extends BaseTool
{
  public 
  QueueBalanceTool()
  {
    super("QueueBalance", new VersionID("1.0.0"), "TheO",
          "Create and balance selection groups on the farm.");
    
    addPhase(new PassOne());
    addPhase(new PassTwo());
    addPhase(new PassThree());
    
    underDevelopment();
    
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
      return ": Analyzing the Selection Keys";
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
      
      TreeMap<String, SelectionGroup> groups = qclient.getSelectionGroups();
      for (String project : pProjectNames.keySet()) {
        for (String dept : pDepartmentNames.keySet()) {
          String groupName = project + "_" + dept;
          SelectionGroup group = null;
          if (groups.containsKey(groupName))
            group = groups.get(groupName);
          else {
            qclient.addSelectionGroup(groupName);
            group = new SelectionGroup(groupName);
          }
          group.removeAllBiases();
          for (String p : pProjectNames.keySet()) {
            String projectKey = pProjectNames.get(p);
            if (p.equals(project))
              group.addBias(projectKey, 100);
            else
              group.addBias(projectKey, 0);
          }
          for (String d : pDepartmentNames.keySet()) {
            String deptKey = pDepartmentNames.get(d);
            if (d.equals(dept))
              group.addBias(deptKey, 100);
            else
              group.addBias(deptKey, 0);
          }
          qclient.editSelectionGroup(group);
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
      pDepartmentValueFields = new DoubleMap<String, String, JDoubleField>();
      pProjectValueFields = new TreeMap<String, JDoubleField>();
      pDepartmentTotalField = new TreeMap<String, JDoubleField>();
      
      /* create the UI components */ 
      JScrollPane scroll = null;
      {
       Box vbox = new Box(BoxLayout.Y_AXIS);
       {
         Box hbox = new Box(BoxLayout.X_AXIS);
         Component comps[] = UIFactory.createTitledPanels();
         JPanel tpanel = (JPanel) comps[0];
         JPanel vpanel = (JPanel) comps[1];
         
         pProjectTotalField =
           UIFactory.createTitledDoubleField
           (tpanel, "Total:", sTSize, vpanel,
            0.0, sVSize, 
            "The total percentage of the queue currently assigned to projects.");
         pProjectTotalField.setEditable(false);
         
         UIFactory.addVerticalSpacer(tpanel, vpanel, 15);
         
         boolean first = true;
         for (String project : pProjectNames.keySet()) {
           if (!first)
             UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
           else
             first = false;
           JDoubleField field =
             UIFactory.createTitledDoubleField
             (tpanel, project + ":", sTSize, vpanel,
              0.0, sVSize, 
              "The percentage of the queue to give to the (" + project + ") project.");
           pProjectValueFields.put(project, field);
           field.addActionListener(this);
           field.setActionCommand(aProjectCommand + "-" + project);
           field.setInputVerifier(new QueueInputVerifier());
         }
         hbox.add(comps[2]);
         JDrawer drawer = new JDrawer("Projects", hbox, true);
         vbox.add(drawer);
       }
       for (String project : pProjectNames.keySet()) {
         String projectCommand = aDeptCommand + "-" + project;
         
         Box hbox = new Box(BoxLayout.X_AXIS);
         Component comps[] = UIFactory.createTitledPanels();
         JPanel tpanel = (JPanel) comps[0];
         JPanel vpanel = (JPanel) comps[1];
         
         JDoubleField totalField =
           UIFactory.createTitledDoubleField
           (tpanel, "Total:", sTSize, vpanel,
            0.0, sVSize, 
            "The total percentage of the project queue currently assigned to " +
            "each department.");
         totalField.setEditable(false);
         pDepartmentTotalField.put(project, totalField);
         
         UIFactory.addVerticalSpacer(tpanel, vpanel, 15);

         boolean first = true;
         for (String dept : pDepartmentNames.keySet()) {
           if (!first)
             UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
           else
             first = false;
           JDoubleField field =
             UIFactory.createTitledDoubleField
             (tpanel, dept + ":", sTSize, vpanel,
              0.0, sVSize, 
              "The percentage of the project queue to give to the (" + dept + ") department.");
           pDepartmentValueFields.put(project, dept, field);
           field.addActionListener(this);
           field.setActionCommand(projectCommand + "-" + dept);
           field.setInputVerifier(new QueueInputVerifier());
         }
         hbox.add(comps[2]);
         JDrawer drawer = new JDrawer("Project - " + project, hbox, false);
         vbox.add(drawer);
       }
       vbox.add(UIFactory.createFiller(sTSize + sVSize));
       {
         scroll = new JScrollPane(vbox);

         scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

         scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

         Dimension size = new Dimension(sTSize + sVSize, 500);
         scroll.setMinimumSize(size);
       }
      }
      
      JToolDialog diag = new JToolDialog("Queue Balance Tool", scroll, "Confirm");

      diag.setVisible(true);
      if(diag.wasConfirmed()) 
        return "Allocating the queue.";

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
      pHosts = qclient.getHosts();
      
      pHostsByOrder = new MappedSet<Integer, String>();
      pHostNames = new TreeSet<String>();
      pProjectHostNum = new TreeMap<String, Integer>();
      pGroupSizes = new TreeMap<String, Integer>();
      
      
      for (String hostname : pHosts.keySet()) {
        QueueHostInfo info = pHosts.get(hostname);
        if (info.getStatus() == QueueHostStatus.Enabled) {
          Integer order = info.getOrder();
          pHostsByOrder.put(order, hostname);
          pHostNames.add(hostname);
        }
      }
      
      int numHosts = pHostNames.size();
      int totalHosts = 0;
      for (String project : pProjectNames.keySet()) {
        double percent = pProjectValueFields.get(project).getValue();
        int hosts = (int) Math.round(percent * numHosts);
        if ((hosts + totalHosts) > numHosts)
          hosts = numHosts - totalHosts;
        totalHosts += hosts;
        pProjectHostNum.put(project, hosts);
        int deptHosts = 0;
        for (String dept : pDepartmentNames.keySet()) {
          String groupName = project + "_" + dept;
          double dPercent = pDepartmentValueFields.get(project, dept).getValue();
          int dHosts = (int) Math.round(dPercent * hosts);
          if ((dHosts + deptHosts) > hosts)
            dHosts = hosts - deptHosts;
          deptHosts += dHosts;
          pGroupSizes.put(groupName, dHosts);
          
        }
      }
      
      pGroupHosts = new MappedSet<String, String>();
      
      LinkedList<String> groupQueue = 
        new LinkedList<String>(pGroupSizes.keySet());
      
      for (String group : new LinkedList<String>(groupQueue)) {
        if (pGroupSizes.get(group) == 0)
          groupQueue.remove(group);
      }
      
      for (Integer order : pHostsByOrder.keySet()) {
        TreeSet<String> hosts = pHostsByOrder.get(order);
        for (String host : hosts) {
          QueueHostInfo info = pHosts.get(host);
          String currentGroup = info.getSelectionGroup();
          String actualGroup;
          if (currentGroup != null && groupQueue.contains(currentGroup)) 
            actualGroup = currentGroup;
          else 
            actualGroup = groupQueue.getFirst();
          groupQueue.remove(actualGroup);
          pGroupHosts.put(actualGroup, host);
          int size = pGroupHosts.get(actualGroup).size();
          if (size < pGroupSizes.get(actualGroup))
            groupQueue.addLast(actualGroup);
        }
      }
      
      System.out.println(pGroupHosts);
      
      return NextPhase.Continue;
    }
    
    @Override
    public void 
    actionPerformed
    (
      ActionEvent e
    )
    {
      System.out.println("action");
      String command = e.getActionCommand();
      handleCommand(command);
    }
    
    private void
    handleCommand
    (
      String command  
    )
    {
      if (command.startsWith(aProjectCommand)) {
        String project = command.split("-")[1];
        double sum = 0;
        double newValue = 0.0;
        for (String each : pProjectValueFields.keySet()) {
          if (each.equals(project)) {
            newValue = pProjectValueFields.get(each).getValue();
            sum += newValue;
          }
          else
            sum += pProjectValueFields.get(each).getValue();
        }
        if (sum > 100) {
          newValue = 100.0 - (sum - newValue);
          sum = 100;
        }
        JDoubleField field = pProjectValueFields.get(project);
        field.removeActionListener(this);
        field.setValue(newValue);
        field.addActionListener(this);
        pProjectTotalField.setValue(sum);
      }
      else if (command.startsWith(aDeptCommand)) {
        String buffer[] = command.split("-");
        String project = buffer[1];
        String department = buffer[2];
        double sum = 0;
        double newValue = 0;
        TreeMap<String, JDoubleField> fields = pDepartmentValueFields.get(project);
        for (String each : fields.keySet()) {
          if (each.equals(department)) {
            newValue = fields.get(each).getValue();
            sum += newValue;
          }
          else
            sum += fields.get(each).getValue();
        }
        if (sum > 100) {
          newValue = 100.0 - (sum - newValue);
          sum = 100;
        }
        JDoubleField field = fields.get(department);
        field.removeActionListener(this);
        field.setValue(newValue);
        field.addActionListener(this);
        pDepartmentTotalField.get(project).setValue(sum);
      }
    }
    
    private 
    class QueueInputVerifier
      extends InputVerifier
    {

      @Override
      public boolean 
      shouldYieldFocus
      (
        JComponent input
      )
      {
        JDoubleField field = (JDoubleField) input;
        for (String project : pProjectValueFields.keySet()) {
          JDoubleField pField = pProjectValueFields.get(project);
          if (field.equals(pField)) {
            handleCommand(aProjectCommand + "-" + project);
            return verify(input);
          }
          for (String dept : pDepartmentValueFields.get(project).keySet()) {
            JDoubleField dField = pDepartmentValueFields.get(project, dept);
            if (field.equals(dField)) {
              handleCommand(aDeptCommand + "-" + project + "-" + dept);
              return verify(input);
            }
          }
        }
        return verify(input);
      }
      
      @Override
      public boolean 
      verify
      (
        JComponent input
      )
      {
        return true;
      }
      
    }
    
    private static final String aProjectCommand = "project";
    private static final String aDeptCommand = "dept";
  }
  
  
  private 
  class PassThree
    extends ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      pHostSwitchFields = new TreeMap<String, JBooleanField>();
      
      JScrollPane scroll = null;
      {
       Box vbox = new Box(BoxLayout.Y_AXIS);
       for (String group : pGroupHosts.keySet()) {
         Box hbox = new Box(BoxLayout.X_AXIS);
         Component comps[] = UIFactory.createTitledPanels();
         JPanel tpanel = (JPanel) comps[0];
         JPanel vpanel = (JPanel) comps[1];
         
         boolean first = true;
         for (String host : pGroupHosts.get(group)) {
           if (!first)
             UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
           else
             first = false;
           JBooleanField field =
             UIFactory.createTitledBooleanField(tpanel, host, sTSize, vpanel, sVSize);
           field.setValue(true);
           pHostSwitchFields.put(host, field);
         }
         hbox.add(comps[2]);
         JDrawer draw = new JDrawer("Group: " + group, hbox, true);
         vbox.add(draw);
       }
       
       vbox.add(UIFactory.createFiller(sTSize + sVSize));
       {
         scroll = new JScrollPane(vbox);

         scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

         scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

         Dimension size = new Dimension(sTSize + sVSize, 500);
         scroll.setMinimumSize(size);
       }
      
      JToolDialog diag = new JToolDialog("Machine Allocation", scroll, "Confirm");
      
      diag.setVisible(true);
      if(diag.wasConfirmed()) 
        return ": Assigning machines";
      return null;
      }
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
      TreeMap<String, QueueHostMod> hostMods = new TreeMap<String, QueueHostMod>();
      for (String group : pGroupHosts.keySet()) {
        for (String host : pGroupHosts.get(group)) {
          boolean doThis = pHostSwitchFields.get(host).getValue();
          if (doThis) {
           QueueHostInfo info = pHosts.get(host);
           String oldGroup = info.getSelectionGroup(); 
           if (oldGroup == null || !oldGroup.equals(group) ) {
             QueueHostMod mod = 
               new QueueHostMod(null, null, false, null, null, group, true, 
                                null, false, null, false);
             hostMods.put(host, mod);
           }
          }
        }
      }
      qclient.editHosts(hostMods);
      
      return NextPhase.Finish;
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7059663122252474673L;

  
  private static final int sTSize = 150;
  private static final int sVSize = 300;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private TreeSet<String> pDepartmentKeys;
  private TreeMap<String, String> pDepartmentNames;
  private TreeSet<String> pProjectKeys;
  private TreeMap<String, String> pProjectNames;
  
  private JDoubleField pProjectTotalField;
  private TreeMap<String, JDoubleField> pDepartmentTotalField;
  private DoubleMap<String, String, JDoubleField> pDepartmentValueFields;
  private TreeMap<String, JDoubleField> pProjectValueFields;
  
  private MappedSet<Integer, String> pHostsByOrder;
  private TreeSet<String> pHostNames;
  private TreeMap<String, QueueHostInfo> pHosts;
  
  private TreeMap<String, Integer> pProjectHostNum;
  private TreeMap<String, Integer> pGroupSizes;
  
  private MappedSet<String, String> pGroupHosts;
  
  private TreeMap<String, JBooleanField> pHostSwitchFields;

}
