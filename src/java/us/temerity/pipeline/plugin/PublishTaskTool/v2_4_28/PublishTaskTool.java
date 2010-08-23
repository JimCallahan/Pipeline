package us.temerity.pipeline.plugin.PublishTaskTool.v2_4_28;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_28.*;
import us.temerity.pipeline.builder.v2_4_28.TaskBuilder;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   P U B L I S H   T A S K   T O O L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A tool which allows the supervisor to run the publish builder for a task using the Submit 
 * node for the task as the entry point.
 */
public 
class PublishTaskTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  PublishTaskTool() 
  {
    super("PublishTask", new VersionID("2.4.28"), "Temerity", 
          "A tool which allows the supervisor to run the publish builder for a task " + 
          "using the submit node for the task as the entry point."); 
    
    addPhase(new FirstPhase());
    addPhase(new SecondPhase());

    pTool = this;
    
    underDevelopment();
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   T O O L   O V E R R I D E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to perform a node status update upon successfully executing the tool. <P> 
   */ 
  @Override
  public boolean 
  updateOnExit()
  {
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Validate the selected node, find the possible checked-in versions to approve 
   * and the corresponding approve node for the task.
   */ 
  private 
  class FirstPhase
    extends ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      if(pPrimary == null || pSelected.size() > 1) 
        throw new PipelineException
          ("Please only select one node, from the task you wish to approve, before running " +
           "this tool.!"); 

      return " : Validating Task";
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
      TreeMap<NodePurpose, BaseAnnotation> tannots = 
        new TreeMap<NodePurpose, BaseAnnotation>();
      String taskInfo[] = lookupTaskAnnotations(mclient, pPrimary, tannots);
      
      if(taskInfo == null) 
        throw new PipelineException
          ("The selected node (" + pPrimary + ") does not have the appropriate " + 
           "annotations to be part of a task.");
      
      if (!tannots.containsKey(NodePurpose.Submit))
        throw new PipelineException
          ("The selected node (" + pPrimary + ") is not the submit node for the task.  This " +
           "tool can only be run on a submit node.");
      
      /* save the task information */ 
      pProjectName = taskInfo[0];
      pTaskIdent1  = taskInfo[1];
      pTaskIdent2  = taskInfo[2];
      pTaskType    = taskInfo[2];
      pTaskInfo    = taskInfo;
 
      String verifyNode = null;
      /* Is the Verify node the same as the Submit node? */
      if (tannots.containsKey(NodePurpose.Verify))
        verifyNode = pPrimary;
      else
        verifyNode = TaskBuilder.getDefaultVerifyNodeName(taskInfo);
      
      pAllVersions = mclient.getAllCheckedInVersions(verifyNode);
      pVerifyNode = verifyNode;
      
      TreeSet<VersionID> alreadyPublished = new TreeSet<VersionID>();
      String publishNode = TaskBuilder.getDefaultPublishBuilderNodeName(taskInfo);
      {
        TreeMap<VersionID, NodeVersion> versions = 
          mclient.getAllCheckedInVersions(publishNode);
        for (NodeVersion ver : versions.values()) {
          String message = ver.getMessage();
          String buffer[] = message.split("\\(");
          if (buffer.length > 1) {
            buffer = buffer[1].split("\\)");
            String vid = buffer[0].replaceAll("v", "");
            if (VersionID.isValidVersionID(vid)) {
              VersionID id = new VersionID(vid);
              alreadyPublished.add(id);
            }
          }
        }
      }
      
      pPublishedVersions = alreadyPublished;

      return NextPhase.Continue;
    }
  }
  
  /**
   * Query the supervisor for a suitable check-in message and version level and 
   * then run the approval builder.
   */ 
  private 
  class SecondPhase
    extends ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      Box vbox = new Box(BoxLayout.PAGE_AXIS);
      
      Box body = null;
      
      JList versionList ;
      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        body = (Box) comps[2];
        
        UIFactory.createTitledTextField
          (tpanel, "Project Name:", sTSize, 
           vpanel, pProjectName, sVSize, 
           "The name of the project this task part of achieving."); 
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        UIFactory.createTitledTextField
          (tpanel, "Task Ident1:", sTSize, 
           vpanel, pTaskIdent1, sVSize, 
           "The name of the overall production goal this node is used to achieve.  " + 
           "Typically, this is the name of a shot or the asset name.");

        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        UIFactory.createTitledTextField
          (tpanel, "Task Ident1:", sTSize, 
           vpanel, pTaskIdent2, sVSize, 
           "The name of the overall production goal this node is used to achieve.  " + 
           "Typically, this is the name of a shot or the asset name.");

        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        UIFactory.createTitledTextField
          (tpanel, "Task Type:", sTSize, 
           vpanel, pTaskType, sVSize, 
           "A unique type of production goal this node is used to achieve which is not one " +
           "of the standard type available in TaskType."); 

        UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
        
        vbox.add(body);
        
        {
          versionList = new JList(new DefaultListModel());

          versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
          versionList.setCellRenderer(new JVersionListCellRenderer(pTool));
          
          DefaultListModel model = (DefaultListModel) versionList.getModel();

          ArrayList<NodeVersion> versions = new ArrayList<NodeVersion>(pAllVersions.values());
          Collections.reverse(versions);
          for (NodeVersion ver : versions)
            model.addElement(ver);

          {
            Dimension size = new Dimension(sTSize + sVSize, 400);

            JScrollPane scroll = 
              UIFactory.createScrollPane
              (versionList, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                new Dimension(150, 150), size, null);

            vbox.add(scroll);
          }
        }
      }
      
      JToolDialog diag = 
        new JToolDialog("Publish Task: " + TaskBuilder.taskArrayToString(pTaskInfo), 
                         vbox, "Publish");
      
      diag.pack();

      diag.setVisible(true);
      if(diag.wasConfirmed()) {
        NodeVersion ver = (NodeVersion) versionList.getSelectedValue();
        if (ver == null)
          return null;
        
        pVerifyVersion = ver.getVersionID();
        
        return " : Publish Builder Started";
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
      RunBuilder task = 
        new RunBuilder(pVerifyNode, pVerifyVersion);
      task.start(); 

      return NextPhase.Finish;
    }
  }
  
  
  private class
  RunBuilder
    extends Thread
  {
    private
    RunBuilder
    (
      String nodeName,
      VersionID nodeVersion
    )
    {
      pNodeName    = nodeName;
      pNodeVersion = nodeVersion;
    }
    
    @Override
    public void 
    run()
    {
      PluginMgrClient plug = PluginMgrClient.getInstance();
      
      MasterMgrClient client = new MasterMgrClient();
      try {
        BaseBuilderCollection collection = 
          plug.newBuilderCollection("Task", new VersionID("2.4.28"), "Temerity");
        
        MultiMap<String, String> params = new MultiMap<String, String>();
        
        LinkedList<String> bkey = new LinkedList<String>();  
        bkey.add("RunPublish");

        {
          LinkedList<String> keys = new LinkedList<String>(bkey); 
          keys.add(aVerifyNode); 
            
          params.putValue(keys, pNodeName, true);
        }

        {
          LinkedList<String> keys = new LinkedList<String>(bkey); 
          keys.add(aVerifyVersion); 
            
          params.putValue(keys, pNodeVersion.toString(), true);
        }
        
        BuilderInformation info = 
          new BuilderInformation(null, false, false, true, false, params);
        
        BaseBuilder builder = 
          collection.instantiateBuilder
            ("RunPublish", new MasterMgrClient(), new QueueMgrClient(), info);
        
        builder.run();
      }
      catch (PipelineException ex) {
        LogMgr.getInstance().log
        (Kind.Ops, Level.Warning, 
         "RunBuilder Thread Failed in the PublishTaskTool on: " + 
         pVerifyNode + "\n" + ex.getMessage());
      }
      finally {
        client.disconnect();
      }
    }

    private String pNodeName;
    private VersionID pNodeVersion;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  public TreeSet<VersionID>
  getPublishedVersions()
  {
    return pPublishedVersions;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3991361421121648955L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;
  
  public static final String aVerifyNode = "VerifyNode";
  public static final String aVerifyVersion = "VerifyVersion";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * The task identifiers.
   */ 
  protected String pProjectName;
  protected String pTaskIdent1;
  protected String pTaskIdent2;
  protected String pTaskType;
  
  private String pTaskInfo[];
  
  private TreeSet<VersionID> pPublishedVersions;
  private TreeMap<VersionID, NodeVersion> pAllVersions;
  
  private VersionID pVerifyVersion;
  
  private String pVerifyNode;
  
  private PublishTaskTool pTool;
}
