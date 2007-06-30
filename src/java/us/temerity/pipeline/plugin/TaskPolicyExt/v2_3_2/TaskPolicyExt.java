package us.temerity.pipeline.plugin.TaskPolicyExt.v2_3_2;

import java.util.Set;

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
   * Get the operation requirements to test before checking-in an individual node. <P>
   */  
  @Override
  public ExtReqs
  getPreCheckInTestReqs() 
  {
    return new ExtReqs(true, true);
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
    
    Set<String> rootAnnotations = getAnnotationNames(rname);
    Set<String> nodeAnnotations = getAnnotationNames(nodeName);
    
    pUser = getWorkUser();
    pGroups = getWorkGroups();
    
    boolean rootApprove = false;
    boolean rootSubmit = false;
    boolean rootFocus = false;
    String rootTask = null;
    
    for (String rAnName : rootAnnotations) {
      BaseAnnotation an = getAnnotation(rname, rAnName);
      String aName = an.getName(); 
      if ( aName.equals("IntermediateNode") || aName.equals("ProductNode") )
	  throw new PipelineException
	    ("The Root node of the checkin (" + rname + ") is a (" + aName + ").  " +
	     "These nodes cannot be the root of a check-in.");
      if (aName.equals("FocusNode")) {
	rootFocus = true;
	rootTask = verifyRootTask(rname, an);
      }
      if (aName.equals("ApproveNode")) {
	rootApprove = true;
	rootTask = verifyRootTask(rname, an);
	boolean allowed = (Boolean) an.getParamValue(aIsApproved);
	if (!allowed)
	  throw new PipelineException
	    ("The Root node of the checkin (" + rname + ") is an (" + aName + ").  " +
	     "These nodes cannot be checked-in WHEN the Is Approved parameter is set to (no).");
      }
      else if (aName.equals("SubmitNode")) {
	rootSubmit = true;
	rootTask = verifyRootTask(rname, an);
	String assigned = (String) an.getParamValue(aAssignedTo);
	if (!hasPermissions(assigned))
	  throw new PipelineException
	    ("The task (" + rootTask + ") on node (" + rname +") is assigned " +
	     "to the user/group (" + assigned + ").  You are not allowed to check in " +
	     "this submit node.");
      }
      else if (aName.equals("EditNode")) {
      	rootTask = verifyRootTask(rname, an);
      }
    }
    if (rootFocus && !rootSubmit)
      throw new PipelineException
	    ("The Root node of the checkin (" + rname + ") is a (FocusNode).  " +
	     "These nodes cannot be the root of a check-in.");
    
    for (String nAnName : nodeAnnotations) {
      BaseAnnotation an = getAnnotation(nodeName, nAnName);
      String aName = an.getName(); 
      if ( aName.equals("FocusNode") || aName.equals("IntermediateNode") ) {
	if (rootSubmit) 
	  verifyTaskName(an, rootTask, nodeName, rname);
	else
          throw new PipelineException
            ("The node (" + nodeName  + ") is a " + aName + ".  " + aName + "s can only be " +
             "checked in when the root node of the check-in is a Submit Node.  The root " +
             "node of this check-in was (" + rname + ")");
      }
      if (aName.equals("ProductNode")) {
	if (rootApprove) 
	  verifyTaskName(an, rootTask, nodeName, rname);
	else
          throw new PipelineException
            ("The node (" + nodeName  + ") is a " + aName + ".  " + aName + "s can only be " +
             "checked in when the root node of the check-in is a Approval Node.  The root " +
             "node of this check-in was (" + rname + ")");
      }
      if (aName.equals("EditNode")) {
	verifyTaskName(an, rootTask, nodeName, rname);
      }
      if (aName.equals("SubmitNode")) {
	if (!nodeName.equals(rname)) 
	  throw new PipelineException
	    ("The node (" + nodeName + ") is a Submit Node.  " +
	     "A Submit Node must be the root of any check-in.");
      }
      if (aName.equals("ApproveNode")) {
	if (!nodeName.equals(rname)) 
	  throw new PipelineException
	    ("The node (" + nodeName + ") is an Approve Node.  " +
	     "An Approve Node must be the root of any check-in.");
      }
    }
  }

  
  /**
   * Get the operation requirements for a tast after checking-in an individual node. <P>
   */ 
  @Override
  public ExtReqs
  getPostCheckInTaskReqs() 
  {
    return new ExtReqs(true, true);
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
    Set<String> anNames = getAnnotationNames(nodeName);
    for (String nAnName : anNames) {
      BaseAnnotation an = getAnnotation(nodeName, nAnName);
      String aname = an.getName();
      
      /*
       * Useful things you can use in the sql COMMIT
       * String author = vsn.getAuthor();
       * VersionID id = vsn.getVersionID();
       * String message = vsn.getMessage();
       * java.sql.Date date = new Date(vsn.getTimeStamp());
       * 
       */
      if (aname.equals("SubmitNode")) {
	/*  Tell the sql database that a node has been submitted for approval. */
	LogMgr.getInstance().log(Kind.Ops, Level.Finer, "Submit node (" + nodeName + ") has been checked-in");
      }
      if (aname.equals("ApproveNode")) {
	/*  Tell the sql database that approval automation has finished. */
	LogMgr.getInstance().log(Kind.Ops, Level.Finer, "Approval node (" + nodeName + ") has been checked-in");
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/



  private String 
  verifyRootTask
  (
    String rname, 
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String rootTask;
    rootTask = (String) an.getParamValue(aTaskName);
    if (rootTask == null)
      throw new PipelineException
        ("(" + rname +") is a task related node with a null Task Name.  This state is not allowed.");
    return rootTask;
  }

  private void 
  verifyTaskName
  (
    BaseAnnotation an,
    String rootTask, 
    String nodeName, 
    String rname
  ) 
    throws PipelineException
  {
    String aTask = (String) an.getParamValue(aTaskName);
    if (aTask == null)
      throw new PipelineException
        ("(" + nodeName +") is a task related node with a null Task Name.  This state is not allowed.");
    if (rootTask != null)
      if (!aTask.equals(rootTask))
        throw new PipelineException
          ("This node (" + nodeName + ") and the root node (" + rname + ") belong to " +
           "two different tasks, (" + aTask + ") and (" + rootTask + ") respectively.  " +
           "Please limit a check-in to a single task.");
  }
  
  private boolean
  hasPermissions
  (
    String assigned
  )
  {
    if (assigned.equals(pUser))
      return true;
    if (pGroups.contains(assigned))
      return true;
    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private String pUser;
  private Set<String> pGroups;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5625120891197882080L;
  
  
  public static final String aTaskName   = "TaskName";
  public static final String aIsApproved = "IsApproved";
  public static final String aAssignedTo = "AssignedTo";
}
