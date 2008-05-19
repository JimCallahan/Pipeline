// $Id: ReleaseTaskTool.java,v 1.1 2008/05/19 04:15:18 jesse Exp $

package us.temerity.pipeline.plugin.ReleaseTaskTool.v2_4_1;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   R E L E A S E   T A S K   T O O L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A tool which finds all the nodes in the same Task as the one which was selected and 
 * releases them.
 */
public 
class ReleaseTaskTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ReleaseTaskTool()
  {
    super("ReleaseTask", new VersionID("2.4.1"), "Temerity", 
          "A tool which finds all the nodes in the same Task as the one which was selected " +
          "and releases them."); 
    
    addPhase(new FirstPhase());
    addPhase(new SecondPhase());

    underDevelopment();
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Validate the selected node and find all other nodes in the task.
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
          ("Please select a single node in a Task."); 

      return " : Searching for nodes in the same Task.";
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
      pTaskNodes = new TreeSet<String>();
      pLeafNodes = new TreeSet<NodeStatus>();
      
      /* determine whether this is a node in a task*/ 
      {
        TreeMap<String, BaseAnnotation> annotations = mclient.getAnnotations(pPrimary);
        for(String aname : annotations.keySet()) {
          if(aname.equals("Task") || aname.startsWith("AltTask")) {
            BaseAnnotation annot = annotations.get(aname);
            pSelectedNode = pPrimary;

            /* save the task information */ 
            pProjectName = lookupProjectName(pSelectedNode, annot);
            pTaskName    = lookupTaskName(pSelectedNode, annot);
            pTaskType    = lookupTaskType(pSelectedNode, annot);
            break;
          }
        }
      }
      
      if(pSelectedNode == null) 
        throw new PipelineException
        ("The selected node (" + pPrimary + ") does not have the appropriate " + 
        "annotations to be a task node!");
      
      pTaskNodes.add(pSelectedNode);
      findDownstreamNodes(pSelected.get(pSelectedNode), mclient);
      for (NodeStatus leaf : pLeafNodes)
        findUpstreamNodes(leaf, mclient);
      
      return NextPhase.Continue;
    }
    
    /**
     * Recursively finds all the nodes underneath the current one which are a member
     * of the same task as the original node.
     * 
     * @param status
     *   The status of the current node.
     * @param mclient
     *   The instance of the Master Manager to look up the annotations on.
     */
    private boolean
    findDownstreamNodes
    (
      NodeStatus status,
      MasterMgrClient mclient
    )
      throws PipelineException
    {
      boolean toReturn = false;
      String nodeName = status.getName();
      TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(nodeName, mclient);
      if (annots != null) {
        BaseAnnotation annot = annots.get(annots.firstKey());
        String projectName = lookupProjectName(nodeName, annot);
        String taskName = lookupTaskName(nodeName, annot);
        String taskType = lookupTaskType(nodeName, annot);
        if (pTaskName.equals(taskName) && 
            pProjectName.equals(projectName) &&
            pTaskType.equals(taskType)) {
          toReturn = true;
          Collection<NodeStatus> stati = status.getSources();
          boolean child = true;
          for (NodeStatus childStatus : stati) {
            if (findDownstreamNodes(childStatus, mclient))
              child = false;
          }
          if (child)
            pLeafNodes.add(status);
          pTaskNodes.add(nodeName);
        }
      }
      return toReturn;
    }
    
    /**
     * Recursively finds all the nodes upstream of the current one which are a member
     * of the same task as the original node.
     * 
     * @param status
     *   The status of the current node.
     * @param mclient
     *   The instance of the Master Manager to look up the annotations on.
     */
    private void
    findUpstreamNodes
    (
      NodeStatus status,
      MasterMgrClient mclient
    )
      throws PipelineException
    {
      String nodeName = status.getName();
      TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(nodeName, mclient);
      if (annots != null) {
        BaseAnnotation annot = annots.get(annots.firstKey());
        String projectName = lookupProjectName(nodeName, annot);
        String taskName = lookupTaskName(nodeName, annot);
        String taskType = lookupTaskType(nodeName, annot);
        if (pTaskName.equals(taskName) && 
            pProjectName.equals(projectName) &&
            pTaskType.equals(taskType)) {
          Collection<NodeStatus> stati = mclient.status(new NodeID(getAuthor(), getView(), nodeName), true).getTargets();
          for (NodeStatus parentStatus : stati) {
            findUpstreamNodes(parentStatus, mclient);
          }
          pTaskNodes.add(nodeName);
        }
      }
    }
  }
  
  /**
   * Asks the user for confirmation on releasing all the nodes..
   */ 
  private 
  class SecondPhase
    extends ToolPhase
  {
    @SuppressWarnings("unused")
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      String message = "The following nodes are going to be released:\n\n";
      for (String node : pTaskNodes) {
        message += node + "\n";
      }
      message += "\nPress (Continue) to release the nodes.";
      
      Box vbox = Box.createVerticalBox();
      JTextArea area = new JTextArea(8, 4);
      area.setText(message);
      area.setEditable(false);
      vbox.add(area);
      JScrollPane scroll = 
        new JScrollPane(vbox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      scroll.setPreferredSize(new Dimension(sTSize + sVSize, 300));
      
      
      JToolDialog diag = 
        new JToolDialog("Release Nodes?", scroll, "Continue");
      diag.setVisible(true);
      
      if(diag.wasConfirmed())
        return " : Releasing nodes.";
      
      return null;
    }
    
    @SuppressWarnings("unused")
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      mclient.release(getAuthor(), getView(), pTaskNodes, true);
      return NextPhase.Finish;
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8722422132713624928L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 300;

  public static final String aSubmit  = "Submit";
  public static final String aApprove = "Approve";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The fully resolved name of the selected version.
   */ 
  private String pSelectedNode; 
  
  /** 
   * The task identifiers.
   */ 
  protected String pProjectName;
  protected String pTaskName;
  protected String pTaskType;
  
  private TreeSet<String> pTaskNodes;
  private TreeSet<NodeStatus> pLeafNodes;
}
