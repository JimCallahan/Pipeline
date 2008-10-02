// $Id: MultipleTaskSetTool.java,v 1.2 2008/10/02 00:26:23 jesse Exp $

package us.temerity.pipeline.plugin.MultipleTaskSetTool.v2_4_1;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   M U L T I P L E   T A S K   S E T   T O O L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Sets the TaskType, TaskName, and Project field for many nodes at the same time.
 */
public 
class MultipleTaskSetTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  MultipleTaskSetTool()
  {
    super("MultipleTaskSet", new VersionID("2.4.1"), "Temerity",
          "ets the TaskType, TaskName, and Project field for many nodes at the same time.");

    pTaskTypeChoices = TaskType.titlesNonCustom();
    
    addPhase(new FirstPass());
    addPhase(new SecondPass());
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  P A S S E S                                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class FirstPass
    extends BaseTool.ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      {
        if(pSelected.size() == 0)
          throw new PipelineException
          ("The " + getName() + " tool requires at least one selected node!");
      }
      
      /* create the UI components */ 
      JScrollPane scroll = null;
      Box hbox = new Box(BoxLayout.X_AXIS);

      {
        scroll = new JScrollPane(hbox);

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        Dimension size = new Dimension(sTSize + sVSize, 500);
        scroll.setMinimumSize(size);
      }
      
      JToolDialog diag = new JToolDialog("Multiple Task Set", scroll, "Confirm");

      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        
        pProjectField = UIFactory.createTitledEditableTextField
          (tpanel, "Project: ", sTSize, vpanel, "project", sVSize, 
           "The name of the project the task is in.");
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        
        pTaskNameField = UIFactory.createTitledEditableTextField
          (tpanel, "TaskName: ", sTSize, vpanel, "taskName", sVSize, 
           "The name of the task.");
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        
        pTaskTypeField = UIFactory.createTitledCollectionField
          (tpanel, "TaskType:", sTSize, vpanel, TaskType.titles(), diag, sVSize, 
            "The type of this task or [[CUSTOM]] to input a custom value in the text area");
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        pCustomTaskTypeField = UIFactory.createTitledEditableTextField
          (tpanel, "CustomTaskType: ", sTSize, vpanel, "", sVSize, 
           "The type of the task if TaskType is set to [[CUSTOM]].");
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        
        pEntityField = UIFactory.createTitledCollectionField
          (tpanel, "EntityType:", sTSize, vpanel, EntityType.titles(), diag, sVSize, 
           "The Shotgun entity type of this task.");
        
        hbox.add(comps[2]);
      }

      diag.pack();
      diag.setVisible(true);
      if(diag.wasConfirmed()) 
        return ": Setting Task Annotation Values.";
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
      pNewAnnots = new TreeSet<String>();
      
      String project = pProjectField.getText();
      String taskName = pTaskNameField.getText();
      TaskType taskType = TaskType.valueOf(pTaskTypeField.getSelected());
      String customType = pCustomTaskTypeField.getText();
      String entityType = pEntityField.getSelected();
      
      for (String node : pSelected.keySet()) {
        TreeMap<String, BaseAnnotation> annots = 
          getTaskAnnotation(node, mclient);
        if (annots != null) {
          for (String aName : annots.keySet()) {
            BaseAnnotation annot = annots.get(aName);
            annot.setParamValue(aProjectName, project);
            annot.setParamValue(aTaskName, taskName);
            annot.setParamValue(aTaskType, taskType.toTitle());
            if (taskType == TaskType.CUSTOM)
              annot.setParamValue(aCustomTaskType, customType);
            annot.setParamValue(aEntityType, entityType);
            mclient.addAnnotation(node, aName, annot);
          }
        }
        else {
          pNewAnnots.add(node);
        }
      }
      
      if (pNewAnnots.isEmpty())
        return NextPhase.Finish;
      else
        return NextPhase.Continue;
    }
  }
  
  
  private 
  class SecondPass
    extends BaseTool.ToolPhase
  {
    @SuppressWarnings("unused")
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      JScrollPane scroll = null;
      Box vbox = new Box(BoxLayout.Y_AXIS);

      {
        scroll = new JScrollPane(vbox);

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        Dimension size = new Dimension(sTSize + sVSize2, 400);
        scroll.setMinimumSize(size);
      }
      
      JToolDialog diag = new JToolDialog("Node Purpose", scroll, "Confirm");
      JCollectionField purposeField;
      JTextField nodeField;
      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        
        nodeField = UIFactory.createTitledTextField
          (tpanel, "NodeName: ", sTSize, vpanel, "", sVSize2, 
           "The name of the node.");
      
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        
        purposeField = UIFactory.createTitledCollectionField
          (tpanel, "NodePurpose:", sTSize, vpanel, NodePurpose.titles(), diag, sVSize2, 
           "The purpose of this node.");
        
        vbox.add(comps[2]);
        
        vbox.add(UIFactory.createFiller(sTSize + sVSize2));
      }
      
      diag.pack();
      
      
      TreeSet<String> focusNodes = new TreeSet<String>();
      pNewAnnotsPurpose = new TreeMap<String, String>();
      for (String node : pNewAnnots) {
        nodeField.setText(node);
        if (node.contains("prepare"))
          purposeField.setSelected(NodePurpose.Prepare.toTitle());
        else if (node.contains("focus"))
          purposeField.setSelected(NodePurpose.Focus.toTitle());
        else if (node.contains("thumb"))
          purposeField.setSelected(NodePurpose.Thumbnail.toTitle());
        else if (node.contains("product"))
          purposeField.setSelected(NodePurpose.Product.toTitle());
        else if (node.contains("edit"))
          purposeField.setSelected(NodePurpose.Edit.toTitle());
        else if (node.contains("submit"))
          purposeField.setSelected(NodePurpose.Submit.toTitle());
        else if (node.contains("approve"))
          purposeField.setSelected(NodePurpose.Approve.toTitle());
        diag.setVisible(true);
        if (!diag.wasConfirmed())
          return null;
        String purpose = purposeField.getSelected();
        if (purpose.equals(NodePurpose.Focus.toTitle()))
          focusNodes.add(node);
        pNewAnnotsPurpose.put(node, purpose);
      }
      
      if (focusNodes.size() > 1) {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        
        JCollectionField masterField = UIFactory.createTitledCollectionField
          (tpanel, "MasterFocusNode:", sTSize, vpanel, focusNodes, diag, sVSize2, 
           "Which focus node should be the master focus node.");
        
        vbox.removeAll();
        vbox.add(comps[2]);
        vbox.add(UIFactory.createFiller(sTSize + sVSize));
        
        scroll.setViewportView(vbox);
        diag.pack();
        diag.setVisible(true);
        if (!diag.wasConfirmed())
          return null;
        pMasterFocus = masterField.getSelected();
      }
      
      return ": Creating new Task Annotations";
    }
    
    @Override
    public NextPhase execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      pPlug = PluginMgrClient.getInstance();
      
      String project = pProjectField.getText();
      String taskName = pTaskNameField.getText();
      TaskType taskType = TaskType.valueOf(pTaskTypeField.getSelected());
      String customType = pCustomTaskTypeField.getText();
      String entityType = pEntityField.getSelected();
      
      for (String node : pNewAnnotsPurpose.keySet()) {
        NodePurpose purpose = NodePurpose.valueOf(pNewAnnotsPurpose.get(node));
        boolean master = (pMasterFocus != null && node.equals(pMasterFocus));
        
        String type = taskType.toTitle();
        if (taskType == TaskType.CUSTOM)
          type = customType;
        BaseAnnotation annot = getNewTaskAnnotation
          (purpose, master, project, taskName, type, entityType );
        mclient.addAnnotation(node, "Task", annot);
      }
      
      return NextPhase.Finish;
    }
  }
  
  
  /** 
   * Return a new SubmitTask, ApproveTask, SynchTask, FocusTask or Task annotation instance 
   * appropriate to be added to the set of annotation plugins for a node. <P> 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   *   
   * @param master
   *   If the purpose is Focus, this will make it a Master Focus Node using the 
   *   FocusTask annotation.  Otherwise it has no effect.
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   *   
   * @param entityType
   *   The value to give the EntityType parameter of the annotation.
   */ 
  @SuppressWarnings("fallthrough")
  protected BaseAnnotation
  getNewTaskAnnotation
  (
   NodePurpose purpose, 
   boolean master,
   String projectName, 
   String taskName, 
   String taskType,
   String entityType
  )
    throws PipelineException
  {
    BaseAnnotation annot = null; 
    switch(purpose) {
    case Submit:
    case Approve:
    case Synch:
      annot = pPlug.newAnnotation(purpose + "Task", new VersionID("2.4.1"), "Temerity");
      break;
      
    case Focus:
      if (master) {
        annot = pPlug.newAnnotation(purpose + "Task", new VersionID("2.4.1"), "Temerity");
        break;
      }

    default:
      annot = pPlug.newAnnotation("Task", new VersionID("2.4.1"), "Temerity");
    }
 
    annot.setParamValue(aProjectName, projectName);
    annot.setParamValue(aTaskName, taskName);

    if(pTaskTypeChoices.contains(taskType)) {
      annot.setParamValue(aTaskType, taskType);
    }
    else {
      annot.setParamValue(aTaskType, TaskType.CUSTOM.toTitle()); 
      annot.setParamValue(aCustomTaskType, taskType);
    }

    switch(purpose) {
    case Submit:
    case Approve:
    case Synch:
      break;
      
    case Focus:
      if (master)
        break;

    default:
      annot.setParamValue(aPurpose, purpose.toString());
    }
    
    if (entityType != null)
      annot.setParamValue(aEntityType, entityType);

    return annot; 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final int sTSize = 150;
  private static final int sVSize = 300;
  private static final int sVSize2 = 600;
  
  private static final long serialVersionUID = 9199051032304433821L;
  
  public static final String aEntityType     = "EntityType";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private ArrayList<String> pTaskTypeChoices;
  
  PluginMgrClient pPlug;
  
  private JTextField pProjectField;
  private JTextField pTaskNameField;
  private JTextField pCustomTaskTypeField;
  
  private JCollectionField pEntityField;
  private JCollectionField pTaskTypeField;
  
  private TreeSet<String> pNewAnnots;
  private String pMasterFocus;
  private TreeMap<String, String> pNewAnnotsPurpose;
}
