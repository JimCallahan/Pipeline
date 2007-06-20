// $Id: TaskPolicyExt.java,v 1.2 2007/06/20 18:07:46 jim Exp $

package us.temerity.pipeline.plugin.TaskPolicyExt.v2_2_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*  T A S K   P O L I C Y   E X T                                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * Restricts access to node operations based on the Task Annotation.
 */
public class 
TaskPolicyExt
  extends BaseMasterExt
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  TaskPolicyExt()
  {
    super("TaskPolicy", new VersionID("2.2.1"), "Temerity",
	  "Restricts access to node operations based on the Task Annotation.");

    {
      ExtensionParam param = 
        new StringExtensionParam
        (aTaskAnnotationName, 
         "The name of the Task Annotation to use in perform access tests.", 
         "Task"); 
      addParam(param);
    }

    /* check-in restrictions */ 
    {
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aAssignedCheckIns,
	   "Whether to restrict check-ins of Focus, Submit and Edit nodes to those users " + 
           "which are Assigned To the node.  Users which match the Supervised By " + 
           "annotation parameter will always be allowed to check-in these nodes.", 
	   true);
	addParam(param);
      }
      
      {
	ExtensionParam param = 
	  new BooleanExtensionParam
	  (aSupervisedCheckIns,
	   "Whether to restrict check-ins of Approve nodes to only those users which " + 
           "match the Supervised By annotation parameter.", 
	   true);
	addParam(param);
      }
    }

    {  
      LayoutGroup layout = new LayoutGroup(true); 

      layout.addEntry(aTaskAnnotationName);

      {
	LayoutGroup sub = new LayoutGroup
	  ("CheckInRestrictions", 
           "Limits to the availability of the Check-In operation.", 
           true); 
        sub.addEntry(aAssignedCheckIns);
        sub.addEntry(aSupervisedCheckIns);

	layout.addSubGroup(sub);
      }

      setLayout(layout);  
    }

    underDevelopment(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before checking-in an individual node.
   */  
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
    boolean restrictAssigned   = isParamTrue(aAssignedCheckIns);
    boolean restrictSupervised = isParamTrue(aAssignedCheckIns);
    if(!restrictAssigned && !restrictSupervised) 
      return;

    BaseAnnotation task = getTaskAnnotation(nodeID.getName());
    if(task == null)
      return; 

    String author = nodeID.getAuthor();
    
    String assigned   = (String) task.getParamValue(aAssignedTo);
    String supervised = (String) task.getParamValue(aSupervisedBy);

    boolean isAssigned = 
      (assigned != null) && (assigned.length() > 0) && (assigned.equals(author));      
    
    boolean isSupervised = 
      (supervised != null) && (supervised.length() > 0) && (supervised.equals(author));

    boolean isNonApproveNode = false;
    {
      {
        Boolean tf = (Boolean) task.getParamValue(aIsEdit);
        if((tf != null) && tf)
          isNonApproveNode = true;
      }

      {
        Boolean tf = (Boolean) task.getParamValue(aIsFocus);
        if((tf != null) && tf)
          isNonApproveNode = true;
      }

      {
        Boolean tf = (Boolean) task.getParamValue(aIsSubmit);
        if((tf != null) && tf)
          isNonApproveNode = true;
      }
    }

    boolean isApproveNode = false;
    {
      Boolean tf = (Boolean) task.getParamValue(aIsApprove);
      if((tf != null) && tf)
        isApproveNode = true;
    }

    if(isApproveNode && !isSupervised) 
      throw new PipelineException
	("Checking-in node (" + nodeID.getName() + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is only available to " + 
         "the supervisor (" + supervised + ") + of the node!");
    
    if(isNonApproveNode && !(isAssigned || isSupervised))
      throw new PipelineException
	("Checking-in node (" + nodeID.getName() + ") in working area " + 
	 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ") is only available to " + 
         "the the artist assigned to work on the node (" + assigned + ") or to the " + 
         "its supervisor (" + supervised + ")!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Task Annotation to use in tests.<P> 
   * 
   * To quailify, it must be an instance of the Task Annotation and have a name which
   * matches the TaskAnnotationName parameter of this extension.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return 
   *   The Task Annotation or <CODE>null</CODE> if none exists.
   */ 
  private BaseAnnotation
  getTaskAnnotation
  (
   String name
  ) 
  {
    try {
      String tname = (String) getParamValue(aTaskAnnotationName);
      BaseAnnotation task = getAnnotation(name, tname);
      if((task == null) || (!task.getName().equals(aTask)))
        return null;
      
      return task;
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 ex.getMessage()); 
      
      return null;
    }
  }

  /**
   * Whether the given boolean extension parameter is currently true.
   */ 
  private boolean 
  isParamTrue
  (
   String pname
  ) 
  {
    try {
      Boolean tf = (Boolean) getParamValue(pname); 
      return ((tf != null) && tf);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	 ex.getMessage()); 
      
      return false;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5895775289549908748L;

  private static final String  aTaskAnnotationName = "TaskAnnotationName"; 	   
  private static final String  aAssignedCheckIns   = "AssignedCheckIns";  
  private static final String  aSupervisedCheckIns = "SupervisedCheckIns";  

  private static final String  aTask         = "Task";  
  private static final String  aTaskName     = "TaskName";	   
  private static final String  aAssignedTo   = "AssignedTo";
  private static final String  aSupervisedBy = "SupervisedBy";
  private static final String  aIsEdit       = "IsEdit";
  private static final String  aIsFocus      = "IsFocus";
  private static final String  aIsSubmit     = "IsSubmit";
  private static final String  aIsApprove    = "IsApprove";

}
