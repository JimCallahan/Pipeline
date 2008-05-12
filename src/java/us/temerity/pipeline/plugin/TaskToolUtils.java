// $Id: TaskToolUtils.java,v 1.1 2008/05/12 16:41:48 jesse Exp $

package us.temerity.pipeline.plugin;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_1.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   T O O L   U T I L S                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of tools which are designed to interact with Temerity Task plugins version 2.4.1 
 *
 */
public abstract
class TaskToolUtils
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the tool.  
   * 
   * @param vid
   *   The tool plugin revision number.
   * 
   * @param vendor
   *   The name of the tool vendor.
   * 
   * @param desc 
   *   A short description of the tool.
   */ 
  protected
  TaskToolUtils
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
    
    pAnnotationCache = new DoubleMap<String, String, BaseAnnotation>();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the Task Annotations on the given node.  
   * <p>
   * If the annotation has been cached, this will read from the cache.
   * @param name
   *   The name of the node.
   * @param mclient
   *   The instance of MasterMgrClient to search with.
   * @return
   *   A TreeMap of Task Annotations indexed by annotation name or 
   *   <code>null</code> if none exists.
   */
  protected TreeMap<String, BaseAnnotation>
  getTaskAnnotation
  (
    String name,
    MasterMgrClient mclient
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> toReturn = pAnnotationCache.get(name); 
    if (toReturn != null) {
      if (toReturn.containsKey(aNONE))
        return null;
      else 
        return toReturn;
    }
    TreeMap<String, BaseAnnotation> annotations = mclient.getAnnotations(name);
    for(String aname : annotations.keySet()) {
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        if (toReturn == null)
          toReturn = new TreeMap<String, BaseAnnotation>();
        BaseAnnotation tannot = annotations.get(aname);
        toReturn.put(aname, tannot);
      }
    }
    if (toReturn != null) {
      pAnnotationCache.put(name, toReturn);
      return toReturn;
    }
    
    pAnnotationCache.put(name, aNONE, null);
    return null;
  }
  
  /**
   * Lookup the value of the (Custom)TaskType annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskType
  (
   String name, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskType = (String) annot.getParamValue(aTaskType);
    if(taskType == null) 
      throw new PipelineException
        ("No " + aTaskType + " parameter was specified for the task " + 
         "annotation on the node (" + name + ")!"); 

    if(taskType.equals(aCUSTOM)) {
      taskType = (String) annot.getParamValue(aCustomTaskType);
      if(taskType == null) 
        throw new PipelineException
          ("No " + aCustomTaskType + " parameter was specified for the task " + 
           "annotation on the node (" + name + ") even though the " + aTaskType + " " + 
           "parameter was set to (" + aCUSTOM + ")!"); 
    }

    return taskType;
  }

  /**
   * Lookup the value of the ProjectName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupProjectName
  (
   String name, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String projectName = (String) annot.getParamValue(aProjectName);
    if(projectName == null) 
      throw new PipelineException
        ("No " + aProjectName + " parameter was specified for the task " + 
         "annotation on the node (" + name + ")!"); 
    
    return projectName;
  }

  /**
   * Lookup the value of the TaskName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskName
  (
   String name, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskName = (String) annot.getParamValue(aTaskName);
    if(taskName == null) 
      throw new PipelineException
        ("No " + aTaskName + " parameter was specified for the task " + 
         "annotation on the node (" + name + ")!"); 

    return taskName;
  }
  
  /**
   * Lookup the value of the Purpose annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskPurpose
  (
   String name, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String purpose = (String) annot.getParamValue(aPurpose);
    if(purpose == null) 
      throw new PipelineException
        ("No " + aPurpose + " parameter was specified for the task " + 
         "annotation on the node (" + name + ")!"); 

    return purpose;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E A R C H E R                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Search upstream from the specified node until an Edit node is found.
   * <p>
   * @throws PipelineException If an Edit node associated with the task cannot be found.
   */
  protected String
  getEditNode
  (
    String startNode,
    MasterMgrClient mclient
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(startNode, mclient);
    if (annots == null || annots.isEmpty())
      throw new PipelineException
        ("There were no Task Annotations on the node (" + startNode + ")");
    BaseAnnotation annot = annots.get(annots.firstKey());
    pProjectName = lookupProjectName(startNode, annot);
    pTaskName = lookupTaskName(startNode, annot);
    pTaskType = lookupTaskType(startNode, annot);
    
    pEditNode = null;
    
    NodeStatus status = pSelected.get(startNode);
    if (status == null)
      status = mclient.status(new NodeID(getAuthor(), getView(), startNode), true);
    
    findDownstreamNodes(status, mclient);
    
    if (pEditNode == null)
      throw new PipelineException
        ("Unable to find the edit node for the task annotation on the node " +
        "(" + startNode + ")!"); 
    
    return pEditNode;
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
  private void
  findDownstreamNodes
  (
    NodeStatus status,
    MasterMgrClient mclient
  )
    throws PipelineException
  {
    if (pEditNode != null)
      return;
    String nodeName = status.getName();
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(nodeName, mclient);
    if (annots != null && !annots.isEmpty()) {
      boolean taskMatch = false;
      for (BaseAnnotation annot : annots.values()) {
        String projectName = lookupProjectName(nodeName, annot);
        String taskName = lookupTaskName(nodeName, annot);
        String taskType = lookupTaskType(nodeName, annot);
        String purpose = lookupTaskPurpose(nodeName, annot);
        if (pTaskName.equals(taskName) && 
            pProjectName.equals(projectName) &&
            pTaskType.equals(taskType)) {
          taskMatch = true;
          if (purpose.equals(NodePurpose.Edit.toString())) {
            pEditNode = nodeName;
            return;
          }
        }
      }
      if (taskMatch) {
        Collection<NodeStatus> stati = status.getSources();
        for (NodeStatus childStatus : stati) {
          findDownstreamNodes(childStatus, mclient);
          if (pEditNode != null)
            return;
        }
      }
    }
  }
  
  /**
   * Search downstream from an edit node until the Submit and Approve nodes are found.
   *
   * @param editNode
   *   The edit node of the Task.  This can be found with the 
   *   {@link #getEditNode(String, MasterMgrClient)} method.
   * @param mclient
   *   The instance of the Master Manager to look up the annotations on.
   * @return A String Array containing two values. The first entry is the Submit node and the
   *   second is the Approve Node.
   * @throws PipelineException If either the Submit or the Approve node associated with 
   *    the task cannot be found.
   */
  protected String[]
  getSubmitAndApproveNodes
  (
    String editNode,
    MasterMgrClient mclient
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(editNode, mclient);
    if (annots == null || annots.isEmpty())
      throw new PipelineException
        ("There were no Task Annotations on the node (" + editNode + ")");
    
    boolean isEditNode = false;
    for (BaseAnnotation annot : annots.values()) {
      String purpose = lookupTaskPurpose(editNode, annot);
      if (purpose.equals(NodePurpose.Edit.toTitle())) {
        isEditNode = true;
        pProjectName = lookupProjectName(editNode, annot);
        pTaskName = lookupTaskName(editNode, annot);
        pTaskType = lookupTaskType(editNode, annot);
        break;
      }
    }
    if (!isEditNode)
      throw new PipelineException
      ("(" + editNode + ") is not an Edit node.  " +
       "This method can only be called on an Edit Node.");
    
    pSubmitNode = null;
    pApprovalNode = null;
    
    NodeStatus status = pSelected.get(editNode);
    if (status == null)
      status = mclient.status(new NodeID(getAuthor(), getView(), editNode), true);

    findUpstreamNodes(status, mclient);
    
    if (pSubmitNode == null)
      throw new PipelineException
        ("Unable to find the submit node for the task annotation on the node " +
        "(" + editNode + ")!"); 
    
    if (pApprovalNode == null)
      throw new PipelineException
        ("Unable to find the approve node for the task annotation on the node " +
        "(" + editNode + ")!"); 
    
    String toReturn[] = new String[2];
    toReturn[0] = pSubmitNode;
    toReturn[1] = pApprovalNode;
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
    if (pSubmitNode != null && pApprovalNode != null)
      return;
    String nodeName = status.getName();
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(nodeName, mclient);
    if (annots != null && !annots.isEmpty()) {
      boolean taskMatch = false;
      for (BaseAnnotation annot : annots.values()) {
        String projectName = lookupProjectName(nodeName, annot);
        String taskName = lookupTaskName(nodeName, annot);
        String taskType = lookupTaskType(nodeName, annot);
        String purpose = lookupTaskPurpose(nodeName, annot);
        if (pTaskName.equals(taskName) && 
            pProjectName.equals(projectName) &&
            pTaskType.equals(taskType)) {
          taskMatch = true;
          if (purpose.equals(NodePurpose.Submit.toString())) {
            pSubmitNode= nodeName;
            return;
          }
          if (purpose.equals(NodePurpose.Approve.toString())) {
            pApprovalNode = nodeName;
            return;
          }
        }
      }
      if (taskMatch) {
        Collection<NodeStatus> stati = 
          mclient.status(new NodeID(getAuthor(), getView(), nodeName), true).getTargets();
        for (NodeStatus parentStatus : stati) {
          findUpstreamNodes(parentStatus, mclient);
          if (pSubmitNode != null && pApprovalNode != null)
            return;
        }
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A local cache of Task Annotation plugins, to limit the number of server roundtrips.
   */
  private DoubleMap<String, String, BaseAnnotation> pAnnotationCache;
  
  /** 
   * The task identifiers.
   */ 
  private String pProjectName;
  private String pTaskName;
  private String pTaskType;
  
  private String pEditNode;
  private String pSubmitNode;
  private String pApprovalNode;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public static final String aProjectName     = "ProjectName";
  public static final String aTaskName        = "TaskName";
  public static final String aTaskType        = "TaskType";
  public static final String aCustomTaskType  = "CustomTaskType";
  public static final String aCUSTOM          = "[[CUSTOM]]";
  public static final String aNONE            = "[[NONE]]";  
  
  public static final String aPurpose         = "Purpose";
}
