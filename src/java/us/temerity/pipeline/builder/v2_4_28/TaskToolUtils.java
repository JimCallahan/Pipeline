package us.temerity.pipeline.builder.v2_4_28;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   T O O L   U T I L S                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of tools which are designed to interact with version 2.4.28 of the Temerity 
 * Task system.
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
    
    pTaskTypeChoices = TaskType.titlesNonCustom();
    
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
    
    PluginID pid = new PluginID("Task", new VersionID("2.4.28"), "Temerity");
    
    TreeMap<String, BaseAnnotation> annots = mclient.getAnnotations(name);
    for(Entry<String, BaseAnnotation> entry : annots.entrySet()) {
      String aname = entry.getKey();
      BaseAnnotation tannot = annots.get(aname);
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        if (tannot.getPluginID().equals(pid)) {
          if (toReturn == null)
            toReturn = new TreeMap<String, BaseAnnotation>();
          toReturn.put(aname, tannot);
        }
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
   * Searches the set of annotations associated with the given node for Task related 
   * annotations. 
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param byPurpose
   *   A table of those that match indexed by Purpose parameter.
   * 
   * @return 
   *   The [ProjectName, TaskIdent1, TaskIdent1, TaskType] array.
   */ 
  protected String[] 
  lookupTaskAnnotations
  (
    MasterMgrClient mclient,
    String name, 
    TreeMap<NodePurpose, BaseAnnotation> byPurpose
  ) 
    throws PipelineException
  {
    PluginID pid = new PluginID("Task", new VersionID("2.4.28"), "Temerity");
    
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(name, mclient);
    String projectName = null; 
    String taskIdent1  = null; 
    String taskIdent2  = null;
    String taskType    = null; 
    for(Entry<String, BaseAnnotation> entry : annots.entrySet()) {
      String aname = entry.getKey();
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        BaseAnnotation an = entry.getValue();
        
        if (!an.getPluginID().equals(pid))
          continue;
            
        
        NodePurpose purpose = lookupTaskPurpose(name, aname, an); 
        if(purpose != null) {
          if(byPurpose.containsKey(purpose)) 
          throw new PipelineException
            ("More than one Task related annotation with a " + aPurpose + " of " + 
             purpose + " was found on node (" + name + ")!"); 
  
          {
            String pname = lookupProjectName(name, aname, an); 
            if(pname == null) 
              throw new PipelineException
                ("The " + aProjectName + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((projectName != null) && !projectName.equals(pname)) 
              throw new PipelineException 
                ("The " + aProjectName + " was set in multiple Task annotations on " +
                 "node (" + name + "), but the did not match!  Both (" + projectName + ") " +
                 "and (" + pname + ") where given as the " + aProjectName + ".");
  
            projectName = pname;
          }
  
          {
            String tname = lookupTaskIdent1(name, aname, an);  
            if(tname == null) 
              throw new PipelineException
                ("The " + aTaskIdent1 + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskIdent1 != null) && !taskIdent1.equals(tname)) 
              throw new PipelineException 
                ("The " + aTaskIdent1 + " was set in multiple Task annotations on " +
                 "node (" + name + "), but they did not match!  Both (" + taskIdent1 + ") " +
                 "and (" + tname + ") where given as the " + aTaskIdent1 + ".");
  
            taskIdent1 = tname; 
          }
          
          {
            String tname = lookupTaskIdent2(name, aname, an);  
            if(tname == null) 
              throw new PipelineException
                ("The " + aTaskIdent2 + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskIdent2 != null) && !taskIdent2.equals(tname)) 
              throw new PipelineException 
                ("The " + aTaskIdent2 + " was set in multiple Task annotations on " +
                 "node (" + name + "), but they did not match!  Both (" + taskIdent2 + ") " +
                 "and (" + tname + ") where given as the " + aTaskIdent2 + ".");
  
            taskIdent2 = tname; 
          }
  
          {
            String ttype = lookupTaskType(name, aname, an);  
            if(ttype == null) 
              throw new PipelineException
                ("The " + aTaskType + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskType != null) && !taskType.equals(ttype)) 
              throw new PipelineException 
                ("The " + aTaskType + " was set in multiple Task annotations on node " + 
                 "(" + name + "), but the did not match!  Both (" + taskType + ") and " + 
                 "(" + ttype + ") where given as the " + aTaskType + ".");
  
            taskType = ttype;
          }
  
          byPurpose.put(purpose, an); 
        }
      }
    }

    if(!byPurpose.isEmpty()) {
      String names[] = { projectName, taskIdent1, taskIdent2, taskType };
      return names;
    }

    return null;
  }
  
  /**
   * Lookup the value of the (Custom)TaskType annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   *   
   * @param aname
   *   The name of the annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskType
  (
   String name, 
   String aname,
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskType = (String) annot.getParamValue(aTaskType);
    if(taskType == null) 
      throw new PipelineException
        ("No " + aTaskType + " parameter was specified for the (" + aname + ") task " + 
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
   * @param aname
   *   The name of the annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupProjectName
  (
   String name, 
   String aname,
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String projectName = (String) annot.getParamValue(aProjectName);
    if(projectName == null) 
      throw new PipelineException
        ("No " + aProjectName + " parameter was specified for the (" + aname + ") task " + 
         "annotation on the node (" + name + ")!"); 
    
    return projectName;
  }

  /**
   * Lookup the value of the TaskIdent1 annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   *   
   * @param aname
   *   The name of the annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskIdent1
  (
   String name, 
   String aname,
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskName = (String) annot.getParamValue(aTaskIdent1);
    if(taskName == null) 
      throw new PipelineException
        ("No " + aTaskIdent1+ " parameter was specified for the (" + aname + ") task " + 
         "annotation on the node (" + name + ")!"); 

    return taskName;
  }
  
  /**
   * Lookup the value of the TaskIdent2 annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   *   
   * @param aname
   *   The name of the annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskIdent2
  (
   String name,
   String aname,
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskName = (String) annot.getParamValue(aTaskIdent2);
    if(taskName == null) 
      throw new PipelineException
        ("No " + aTaskIdent2 + " parameter was specified for the (" + aname + ") task " + 
         "annotation on the node (" + name + ")!"); 

    return taskName;
  }
  
  /**
   * Lookup the value of the Purpose annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected NodePurpose
  lookupTaskPurpose
  (
   String name, 
   String aname,
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    EnumAnnotationParam param = (EnumAnnotationParam) annot.getParam(aPurpose);
    
    if(param == null) 
      throw new PipelineException
        ("No " + aPurpose + " parameter was specified for the (" + aname + ") task " + 
         "annotation on the node (" + name + ")!");
    
    return NodePurpose.values()[param.getIndex()];
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
   * @param taskIdent1
   *   The value to give the TaskIdent1 parameter of the annotation.
   *   
   * @param taskIdent2
   *   The value to give the TaskIdent2 parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   *   
   * @param entityType
   *   The value to give the EntityType parameter of the annotation.
   */ 
  protected BaseAnnotation
  getNewTaskAnnotation
  (
    PluginMgrClient plug,
    NodePurpose purpose, 
    boolean master,
    String projectName, 
    String taskIdent1, 
    String taskIdent2,
    String taskType,
    String entityType
  )
    throws PipelineException
  {
    
    BaseAnnotation annot = plug.newAnnotation("Task", new VersionID("2.4.28"), "Temerity"); 
    
    annot.setParamValue(aProjectName, projectName);
    annot.setParamValue(aTaskIdent1, taskIdent1);
    annot.setParamValue(aTaskIdent2, taskIdent2);
    
    if(pTaskTypeChoices.contains(taskType)) {
      annot.setParamValue(aTaskType, taskType);
    }
    else {
      annot.setParamValue(aTaskType, TaskType.CUSTOM.toTitle()); 
      annot.setParamValue(aCustomTaskType, taskType);
    }
    
    annot.setParamValue(aPurpose, purpose.toString());
    
    if (purpose == NodePurpose.Focus && master)
      annot.setParamValue("Master", true);
    
    if (entityType != null)
      annot.setParamValue(aEntityType, entityType);

    return annot; 
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E A R C H E R                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Search upstream from the specified node until an Edit node is found.
   * <p>
   * @throws PipelineException If an Edit node associated with the task cannot be found.
   */
//  protected String
//  getEditNode
//  (
//    String startNode,
//    MasterMgrClient mclient
//  )
//    throws PipelineException
//  {
//    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(startNode, mclient);
//    if (annots == null || annots.isEmpty())
//      throw new PipelineException
//        ("There were no Task Annotations on the node (" + startNode + ")");
//    BaseAnnotation annot = annots.get(annots.firstKey());
//    pProjectName = lookupProjectName(startNode, annot);
//    pTaskName = lookupTaskName(startNode, annot);
//    pTaskType = lookupTaskType(startNode, annot);
//    
//    pEditNode = null;
//    
//    NodeStatus status = pSelected.get(startNode);
//    if (status == null)
//      status = mclient.status(new NodeID(getAuthor(), getView(), startNode), 
//                              true, DownstreamMode.None);
//    
//    findUpstreamNodes(status, mclient);
//    
//    if (pEditNode == null)
//      throw new PipelineException
//        ("Unable to find the edit node for the task annotation on the node " +
//        "(" + startNode + ")!"); 
//    
//    return pEditNode;
//  }
  
  /**
   * Recursively finds all the nodes underneath the current one which are a member
   * of the same task as the original node.
   * 
   * @param status
   *   The status of the current node.
   * @param mclient
   *   The instance of the Master Manager to look up the annotations on.
   */
//  private void
//  findUpstreamNodes
//  (
//    NodeStatus status,
//    MasterMgrClient mclient
//  )
//    throws PipelineException
//  {
//    if (pEditNode != null)
//      return;
//    String nodeName = status.getName();
//    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(nodeName, mclient);
//    if (annots != null && !annots.isEmpty()) {
//      boolean taskMatch = false;
//      for (BaseAnnotation annot : annots.values()) {
//        String projectName = lookupProjectName(nodeName, annot);
//        String taskName = lookupTaskName(nodeName, annot);
//        String taskType = lookupTaskType(nodeName, annot);
//        String purpose = lookupTaskPurpose(nodeName, annot);
//        if (pTaskName.equals(taskName) && 
//            pProjectName.equals(projectName) &&
//            pTaskType.equals(taskType)) {
//          taskMatch = true;
//          if (purpose.equals(NodePurpose.Edit.toString())) {
//            pEditNode = nodeName;
//            return;
//          }
//        }
//      }
//      if (taskMatch) {
//        Collection<NodeStatus> stati = status.getSources();
//        for (NodeStatus childStatus : stati) {
//          findUpstreamNodes(childStatus, mclient);
//          if (pEditNode != null)
//            return;
//        }
//      }
//    }
//  }
//  
//  /**
//   * Search downstream from an edit node until the Submit and Approve nodes are found.
//   *
//   * @param editNode
//   *   The edit node of the Task.  This can be found with the 
//   *   {@link #getEditNode(String, MasterMgrClient)} method.
//   * @param mclient
//   *   The instance of the Master Manager to look up the annotations on.
//   * @return A String Array containing two values. The first entry is the Submit node and the
//   *   second is the Approve Node.
//   * @throws PipelineException If either the Submit or the Approve node associated with 
//   *    the task cannot be found.
//   */
//  protected String[]
//  getSubmitAndApproveNodes
//  (
//    String editNode,
//    MasterMgrClient mclient
//  )
//    throws PipelineException
//  {
//    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(editNode, mclient);
//    if (annots == null || annots.isEmpty())
//      throw new PipelineException
//        ("There were no Task Annotations on the node (" + editNode + ")");
//    
//    boolean isEditNode = false;
//    for (BaseAnnotation annot : annots.values()) {
//      String purpose = lookupTaskPurpose(editNode, annot);
//      if (purpose.equals(NodePurpose.Edit.toTitle())) {
//        isEditNode = true;
//        pProjectName = lookupProjectName(editNode, annot);
//        pTaskName = lookupTaskName(editNode, annot);
//        pTaskType = lookupTaskType(editNode, annot);
//        break;
//      }
//    }
//    if (!isEditNode)
//      throw new PipelineException
//      ("(" + editNode + ") is not an Edit node.  " +
//       "This method can only be called on an Edit Node.");
//    
//    pSubmitNode = null;
//    pApprovalNode = null;
//    
//    NodeStatus status = mclient.status(new NodeID(getAuthor(), getView(), editNode), 
//                                       true, DownstreamMode.All);  // Correct DownstreamMode?
//
//    findDownstreamNodes(status, mclient);
//    
//    if (pSubmitNode == null)
//      throw new PipelineException
//        ("Unable to find the submit node for the task annotation on the node " +
//        "(" + editNode + ")!"); 
//    
//    if (pApprovalNode == null)
//      throw new PipelineException
//        ("Unable to find the approve node for the task annotation on the node " +
//        "(" + editNode + ")!"); 
//    
//    String toReturn[] = new String[2];
//    toReturn[0] = pSubmitNode;
//    toReturn[1] = pApprovalNode;
//    return toReturn;
//  }
//  
//  /**
//   * Recursively finds all the nodes upstream of the current one which are a member
//   * of the same task as the original node.
//   * 
//   * @param status
//   *   The status of the current node.
//   * @param mclient
//   *   The instance of the Master Manager to look up the annotations on.
//   */
//  private void
//  findDownstreamNodes
//  (
//    NodeStatus status,
//    MasterMgrClient mclient
//  )
//    throws PipelineException
//  {
//    if (pSubmitNode != null && pApprovalNode != null)
//      return;
//    String nodeName = status.getName();
//    TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(nodeName, mclient);
//    if (annots != null && !annots.isEmpty()) {
//      boolean taskMatch = false;
//      for (BaseAnnotation annot : annots.values()) {
//        String projectName = lookupProjectName(nodeName, annot);
//        String taskName = lookupTaskName(nodeName, annot);
//        String taskType = lookupTaskType(nodeName, annot);
//        String purpose = lookupTaskPurpose(nodeName, annot);
//        if (pTaskName.equals(taskName) && 
//            pProjectName.equals(projectName) &&
//            pTaskType.equals(taskType)) {
//          taskMatch = true;
//          if (purpose.equals(NodePurpose.Submit.toString())) {
//            pSubmitNode = nodeName;
//            return;
//          }
//          if (purpose.equals(NodePurpose.Approve.toString())) {
//            pApprovalNode = nodeName;
//            return;
//          }
//        }
//      }
//      if (taskMatch) {
//        Collection<NodeStatus> stati = status.getTargets();
//        for (NodeStatus parentStatus : stati) {
//          findDownstreamNodes(parentStatus, mclient);
//          if (pSubmitNode != null && pApprovalNode != null)
//            return;
//        }
//      }
//    }
//  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private ArrayList<String> pTaskTypeChoices;
  
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
  public static final String aTaskIdent1     = "TaskIdent1";
  public static final String aTaskIdent2     = "TaskIdent2";
  public static final String aTaskType        = "TaskType";
  public static final String aCustomTaskType  = "CustomTaskType";
  public static final String aEntityType     = "EntityType";
  public static final String aCUSTOM          = "[[CUSTOM]]";
  public static final String aNONE            = "[[NONE]]";  
  
  public static final String aPurpose         = "Purpose";
  
  private static final long serialVersionUID = -9150142209228867761L;
}
