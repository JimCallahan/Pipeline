package us.temerity.pipeline.plugin.TaskPolicyExt.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;

/*------------------------------------------------------------------------------------------*/
/*  T A S K   P O L I C Y   E X T                                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * Restricts access to node operations based on all the Task Annotations.
 */
public 
class TaskPolicyExt 
  extends BaseMasterExt
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  TaskPolicyExt()
  {
    super("TaskPolicy", new VersionID("2.3.2"), "Temerity",
	  "Restricts access to node operations based on all the Task Annotations.");
    
    underDevelopment(); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before checking-in an individual node.
   */  
  @Override
  public boolean
  hasPreCheckInTest() 
  {
    return true;
  }	 

  /**
   * Test to perform before checking-in an individual node.
   * 
   * @param rname
   *   The fully resolved node name of the root node of the check-in operation.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version of the node.
   * 
   * @param level  
   *   The revision number component level to increment.
   * 
   * @param msg 
   *   The check-in message text.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */ 
  @Override
  public void
  preCheckInTest
  (
   String rname, 
   NodeID nodeID, 
   NodeMod mod,
   VersionID.Level level, 
   String msg
  ) 
    throws PipelineException
  {
    String nodeName = nodeID.getName();
    
    MasterMgrLightClient mclient = getMasterMgrClient();
    TreeMap<String,BaseAnnotation> rootAnnotations = mclient.getAnnotations(rname);
    TreeMap<String,BaseAnnotation> nodeAnnotations = mclient.getAnnotations(nodeName);
    
    WorkGroups wgroups = mclient.getWorkGroups();
    
    /* lookup the annotations on the root node of the check-in and validate whether the 
         check-in is allowed based on the annotation parameters */ 
    boolean rootApprove  = false;
    boolean rootSubmit   = false;
    boolean rootFocus    = false;
    String rootTaskName  = null;
    String rootTaskType  = null;
    String rootAnnotType = null;
    for(String aname : rootAnnotations.keySet()) {
      BaseAnnotation an = rootAnnotations.get(aname);
      String annotType = an.getName(); 
      rootAnnotType = annotType;

      if(annotType.equals("IntermediateNode") || annotType.equals("ProductNode")) {
        String goodRoot = annotType.equals("ProductNode") ? "ApproveNode" : "SubmitNode"; 
        throw new PipelineException
          ("Cannot check-in node (" + rname + "), because it is both the root node of the " + 
           "check-in operation and a " + annotType + "! You may only check-in a " + 
           annotType + " as part of the check-in of a " + goodRoot + "."); 
      }
      else {
        if(annotType.equals("FocusNode") || annotType.equals("ApproveNode") || 
           annotType.equals("SubmitNode") || annotType.equals("EditNode")) {
          rootTaskName = lookupTaskName(rname, an);
          rootTaskType = lookupTaskType(rname, an);
        }

        if(annotType.equals("FocusNode")) {
          rootFocus = true;
        }
        else if(annotType.equals("ApproveNode")) {
          rootApprove = true;
          if(!nodeID.getAuthor().equals(PackageInfo.sPipelineUser)) 
            throw new PipelineException
              ("Cannot check-in node (" + rname + "), because it is a " + annotType + " " + 
               "of the task (" + rootTaskName + ":" + rootTaskType + ") which can only be " + 
               "checked-in by the (" + PackageInfo.sPipelineUser + ") user as part of the " + 
               "automated post-approval process!"); 
        }
        else if(annotType.equals("SubmitNode")) {
          rootSubmit = true;
          String author   = nodeID.getAuthor();
          String assigned = (String) an.getParamValue(aAssignedTo);
          if((assigned == null) || (assigned.length() == 0)) 
            throw new PipelineException 
              ("Cannot check-in node (" + rname + ") because no one was Assigned To the " + 
               "complete the task (" + rootTaskName + ":" + rootTaskType + ")!"); 

          boolean isGroup = wgroups.isGroup(assigned); 
          if((!isGroup && !assigned.equals(author)) || 
             (isGroup && wgroups.isMemberOrManager(author, assigned) == null)) 
            throw new PipelineException
              ("The task (" + rootTaskName + ":" + rootTaskType + ") for node " + 
               "(" + rname + ") is assigned to the " + 
               (isGroup ? "Pipeline work group [" + assigned + "]" : 
                          "artist (" + assigned + ")") + 
               ".  The (" + author + ") user is not assigned to this task and therefore " + 
               "is not allowed to check-in this submit node.");
        }
      }
    }

    //
    // SHOULD ROOT FOCUS NODES BE ALLOWED EVEN IF THEY ARE THE SUBMIT NODE?
    //
    if(rootFocus && !rootSubmit)
      throw new PipelineException
        ("Cannot check-in node (" + rname + "), because it both a FocusNode and the root " + 
         "node of the check-in operation without also being a SubmitNode of the task " + 
         "(" + rootTaskName + ":" + rootTaskType + ")!"); 
    

    /* perform checks based on comparing annotation parameters of the root node of the 
         check-in with the current node */
    for(String aname : nodeAnnotations.keySet()) {
      BaseAnnotation an = nodeAnnotations.get(aname);
      String annotType = an.getName(); 

      if(annotType.equals("FocusNode") || annotType.equals("IntermediateNode")) {
	if(rootSubmit) 
	  verifyTask(nodeName, an, rname, rootTaskName, rootTaskType); 
	else
          throw new PipelineException
            ("Cannot check-in node (" + nodeName + "), because it is a " + annotType + " " + 
             "which can only be checked-in when the root node of the check-in operation " + 
             "is a SubmitNode!.  However in this case, the root node (" + rname + ") was " + 
             "a " + rootAnnotType + "."); 
      }
      else if(annotType.equals("ProductNode")) {
	if(rootApprove) 
	  verifyTask(nodeName, an, rname, rootTaskName, rootTaskType); 
	else
          throw new PipelineException
            ("Cannot check-in node (" + nodeName + "), because it is a " + annotType + " " + 
             "which can only be checked-in when the root node of the check-in operation " + 
             "is a ApproveNode!.  However in this case, the root node (" + rname + ") was " + 
             "a " + rootAnnotType + "."); 
      }
      else if(annotType.equals("EditNode")) {
        verifyTask(nodeName, an, rname, rootTaskName, rootTaskType); 
      }
      else if(annotType.equals("SubmitNode") || annotType.equals("ApproveNode")) {
	if(!nodeName.equals(rname)) 
	  throw new PipelineException
	    ("Cannot check-in node (" + nodeName + ") unless it is the root node of the " + 
             "check-in operation because it is a " + rootAnnotType + "!"); 
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to run a tesk after checking-in an individual node.
   */  
  @Override
  public boolean
  hasPostCheckInTask() 
  {
    return true;
  }
  
  /** 
   * The task to perform after checking-in an individual node. 
   * 
   * @param vsn 
   *  The newly created checked-in node version.
   */
  @Override
  public void 
  postCheckInTask
  (
   NodeVersion vsn
  )
  {
    String nodeName = vsn.getName();
    try {
      MasterMgrLightClient mclient = getMasterMgrClient();
      TreeMap<String,BaseAnnotation> nodeAnnotations = mclient.getAnnotations(nodeName);
      for (String aname : nodeAnnotations.keySet()) {
        BaseAnnotation an = nodeAnnotations.get(aname);
        String annotType = an.getName();

        String taskName = lookupTaskName(nodeName, an);
        String taskType = lookupTaskType(nodeName, an);

        
        // Useful things you can use in the sql COMMIT
        // String author = vsn.getAuthor();
        // VersionID id = vsn.getVersionID();
        // String message = vsn.getMessage();
        // java.sql.Date date = new Date(vsn.getTimeStamp());
        
        if (annotType.equals("SubmitNode")) {

          // Tell the sql database that a node has been submitted for approval.
          
          LogMgr.getInstance().log
            (Kind.Ops, Level.Info, 
             "The " + annotType + " (" + nodeName + " v" + vsn.getVersionID() + ") of " + 
             "task (" + taskName + ":" + taskType + ") has been checked-in.");

          /* find all focus and edit nodes upstream for the task */ 
          TreeMap<String,NodeVersion> focusNodes = new TreeMap<String,NodeVersion>();
          TreeMap<String,NodeVersion> editNodes  = new TreeMap<String,NodeVersion>();
          findFocusEditNodes(nodeName, taskName, taskType, vsn, 
                             focusNodes, editNodes, mclient);

          /* process focus nodes */ 
          for(NodeVersion fvsn : focusNodes.values()) {

            // Register the current version of the focus node for this submit check-in.

            LogMgr.getInstance().log
              (Kind.Ops, Level.Info, 
               "The FocusNode (" + fvsn.getName() + " v" + fvsn.getVersionID() + ") is " + 
               "associated with task (" + taskName + ":" + taskType + ") submitted for " + 
               "approval by checking-in the SubmitNode (" + nodeName + " v" + 
               vsn.getVersionID() + ").");
          }

          /* process edit nodes */ 
          for(NodeVersion evsn : editNodes.values()) {

            // Register the current version of the edit node for this submit check-in.

            LogMgr.getInstance().log
              (Kind.Ops, Level.Info, 
               "The FocusNode (" + evsn.getName() + " v" + evsn.getVersionID() + ") is " + 
               "associated with task (" + taskName + ":" + taskType + ") submitted for " + 
               "approval by checking-in the SubmitNode (" + nodeName + " v" + 
               vsn.getVersionID() + ").");
          }
        }
        if (annotType.equals("ApproveNode")) {

          /*  Tell the sql database that approval automation has finished. */

          LogMgr.getInstance().log
            (Kind.Ops, Level.Info, 
             "The " + annotType + " (" + nodeName + " v" + vsn.getVersionID() + ") of " + 
             "task (" + taskName + ":" + taskType + ") has been checked-in.");
        }
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
        (Kind.Ops, Level.Warning, 
         "PostCheckInTask (" + getName() + ") Failed on: " + nodeName + "\n" + 
         ex.getMessage());      
    }
  }

  private void 
  findFocusEditNodes
  (
   String submitNodeName, 
   String submitTaskName, 
   String submitTaskType, 
   NodeVersion vsn, 
   TreeMap<String,NodeVersion> focusNodes, 
   TreeMap<String,NodeVersion> editNodes, 
   MasterMgrLightClient mclient
  )
    throws PipelineException 
  {
    TreeMap<String,BaseAnnotation> annotations = mclient.getAnnotations(vsn.getName());
    for (String aname : annotations.keySet()) {
      BaseAnnotation an = annotations.get(aname);
      String annotType = an.getName();

      if(annotType.equals("FocusNode")) {
        verifyTask(vsn.getName(), an, submitNodeName, submitTaskName, submitTaskType); 
        focusNodes.put(vsn.getName(), vsn);
      }
      else if(annotType.equals("EditNode")) {
        verifyTask(vsn.getName(), an, submitNodeName, submitTaskName, submitTaskType); 
        editNodes.put(vsn.getName(), vsn); 
      }
    }
      
    for(LinkVersion link : vsn.getSources()) {
      NodeVersion source = mclient.getCheckedInVersion(link.getName(), link.getVersionID());
      findFocusEditNodes(submitNodeName, submitTaskName, submitTaskType, source, 
                         focusNodes, editNodes, mclient);
    }
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup the Task Name from the annotation.
   */ 
  private String 
  lookupTaskName
  (
    String nodeName, 
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String taskName = (String) an.getParamValue(aTaskName);
    if((taskName == null) || (taskName.length() == 0))
      throw new PipelineException
        ("A Task Name must be supplied for the " + an.getName() + " annotation on node " + 
         "(" + nodeName +")!"); 

    return taskName;
  }

  /**
   * Lookup the Task Type from the annotation.
   */ 
  private String 
  lookupTaskType
  (
    String nodeName, 
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String taskType = (String) an.getParamValue(aTaskType);
    if((taskType == null) || (taskType.length() == 0))
      throw new PipelineException
        ("A Task Type must be supplied for the " + an.getName() + " annotation on node " + 
         "(" + nodeName +")!"); 

    return taskType;
  }

  /**
   * Verify that the Task Name and Task Type match those of the root node.
   */ 
  private void 
  verifyTask
  (
   String nodeName, 
   BaseAnnotation an,
   String rootNodeName, 
   String rootTaskName, 
   String rootTaskType
  ) 
    throws PipelineException
  {
    String taskName = lookupTaskName(nodeName, an); 
    String taskType = lookupTaskType(nodeName, an); 

    if(rootTaskName != null) {
      if(!taskName.equals(rootTaskName) || !taskType.equals(rootTaskType)) {
        throw new PipelineException
          ("Cannot check-in node (" + nodeName + ") of the task (" + taskName + ":" + 
           taskType + ") because the root node of the check-in (" + rootNodeName + ") " + 
           "belongs to a different task (" + rootTaskName + ":" + rootTaskType + ")!  " + 
           "Please limit a check-in operation to nodes which share the same task.");
      }
    }
  }
      
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5625120891197882080L;
  
  
  public static final String aTaskName   = "TaskName";
  public static final String aTaskType   = "TaskType";
  public static final String aIsApproved = "IsApproved";
  public static final String aAssignedTo = "AssignedTo";
}
