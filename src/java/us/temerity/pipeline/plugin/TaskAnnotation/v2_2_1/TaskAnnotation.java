// $Id: TaskAnnotation.java,v 1.3 2007/06/19 22:05:04 jim Exp $

package us.temerity.pipeline.plugin.TaskAnnotation.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   A N N O T A T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Provides additional information about how nodes are used together to accomplish a
 * specific production task as well as which artists and supervisors are involved in 
 * the change approval process. <P> 
 * 
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Task Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the common production goal this node is used to achieve.
 *   </DIV> <BR>
 * 
 *   Assigned To<BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the WorkGroup or specific artist assigned to complete the task involving
 *     this node.  Users matching Supervised By are allowed to modify this parameter without
 *     Annotator privileges.
 *   </DIV> <BR>
 * 
 *   Supervised By <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the WorkGroup or or project supervisor who is responsible for approving 
 *     changes made by artists AssignedTo complete the task. 
 *   </DIV> <BR>
 * 
 *   Is Edit  <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether this node which should be manually edited by the assigned artist in order to
 *     accomplish the goals of the task.
 *   </DIV> <BR>
 * 
 *   Is Focus  <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether this node should be inspected as part of the review process for a task.
 *   </DIV> <BR>
 * 
 *   Is Submit  <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether a Checked-In of this node signals that a given task is ready for review.
 *   </DIV> <BR>
 * 
 *   Is Approve  <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether a Check-In of this node signals that the task as been approved.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class TaskAnnotation
  extends BaseAnnotation
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TaskAnnotation()
  {
    super("Task", new VersionID("2.2.1"), "Temerity", 
	  "Provides additional information about how nodes are used together to " + 
          "accomplish a specific production task as well as which artists and supervisors " + 
          "are involved in the change approval process."); 

    {
      AnnotationParam param = 
	new StringAnnotationParam
	(aTaskName, 
	 "The name of the common production goal this node is used to achieve.", 
	 null); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new StringAnnotationParam
	(aAssignedTo, 
	 "The name of the WorkGroup or specific artist assigned to complete the task " + 
         "involving this node.", 
	 null); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new StringAnnotationParam
	(aSupervisedBy, 
	 "The name of the WorkGroup or or project supervisor who is responsible for " + 
         "approving changes made by artists AssignedTo complete the task.", 
	 null); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new BooleanAnnotationParam
	(aIsEdit, 
	 "Whether this node which should be manually edited by the assigned artist in " + 
         "order to accomplish the goals of the task.", 
	 false); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new BooleanAnnotationParam
	(aIsFocus, 
	 "Whether this node should be inspected as part of the review process for a task.", 
	 false); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new BooleanAnnotationParam
	(aIsSubmit, 
	 "Whether a Checked-In of this node signals that a given task is ready for review.", 
	 false); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new BooleanAnnotationParam
	(aIsApprove, 
	 "Whether a Check-In of this node signals that the task as been approved.", 
	 false); 
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aTaskName);
      layout.add(null);
      layout.add(aAssignedTo);
      layout.add(aSupervisedBy);
      layout.add(null);
      layout.add(aIsEdit);
      layout.add(aIsFocus);
      layout.add(aIsSubmit);
      layout.add(aIsApprove);

      setLayout(layout);      
    }

    underDevelopment(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether a given user is allowed to modify a specific annotation parameter. <P> 
   * 
   * Allows users who's name matches the SupervisedBy parameter to edit the AssignedTo
   * parameter. Unless a user has Annotator privileges, all other parameters are read-only.
   * 
   * @param pname  
   *   The name of the parameter. 
   * 
   * @param user
   *   The name of the user requesting access to modify the parameter.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the user. 
   */ 
  public boolean
  isParamModifiable
  (
   String pname,
   String user, 
   PrivilegeDetails privileges
  )
  {
    try {
      String supervised = (String) getParamValue(aSupervisedBy);
      return (pname.equals(aAssignedTo) && user.equals(supervised));
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

  private static final long serialVersionUID = 2168290890306064601L;

  public static final String aTaskName      = "TaskName";
  public static final String aAssignedTo    = "AssignedTo";
  public static final String aSupervisedBy  = "SupervisedBy";
  public static final String aIsEdit        = "IsEdit";
  public static final String aIsFocus       = "IsFocus";
  public static final String aIsSubmit      = "IsSubmit";
  public static final String aIsApprove     = "IsApprove";

}


