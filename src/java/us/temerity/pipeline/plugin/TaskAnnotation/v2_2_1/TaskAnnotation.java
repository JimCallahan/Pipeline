// $Id: TaskAnnotation.java,v 1.1 2007/06/17 15:34:46 jim Exp $

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
 *     this node.
 *   </DIV> <BR>
 * 
 *   Supervised By <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the WorkGroup or or project supervisor who is responsible for approving 
 *     changes made by artists AssignedTo complete the task. 
 *   </DIV> <BR>
 * 
 *   Is Edit Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether this node which should be manually edited by the assigned artist in order to
 *     accomplish the goals of the task.
 *   </DIV> <BR>
 * 
 *   Is Focus Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether this node should be inspected as part of the review process for a task.
 *   </DIV> <BR>
 * 
 *   Is Submit Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether a Checked-In of this node signals that a given task is ready for review.
 *   </DIV> <BR>
 * 
 *   Is Approve Node <BR>
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
	(aIsEditNode, 
	 "Whether this node which should be manually edited by the assigned artist in " + 
         "order to accomplish the goals of the task.", 
	 false); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new BooleanAnnotationParam
	(aIsFocusNode, 
	 "Whether this node should be inspected as part of the review process for a task.", 
	 false); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new BooleanAnnotationParam
	(aIsSubmitNode, 
	 "Whether a Checked-In of this node signals that a given task is ready for review.", 
	 false); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new BooleanAnnotationParam
	(aIsApproveNode, 
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
      layout.add(aIsEditNode);
      layout.add(aIsFocusNode);
      layout.add(aIsSubmitNode);
      layout.add(aIsApproveNode);

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
   * The default implementation only grants users with Annotator privileges the right to 
   * modify a parameter, but subclasses may override this method to implement their own 
   * modification policy.  Note that users with Annotator privileges will always be able
   * to modify annoation parameters even if a subclass overrides this method to always
   * return <CODE>false</CODE>.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  public boolean
  isParamModifiable
  (
   String name,
   PrivilegeDetails privileges
  )
  {
    
    // ... 

    return super.isParamModifiable(name, privileges);
  } 



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 

  public static final String aTaskName      = "TaskName";
  public static final String aAssignedTo    = "AssignedTo";
  public static final String aSupervisedBy  = "SupervisedBy";
  public static final String aIsEditNode    = "IsEditNode";
  public static final String aIsFocusNode   = "IsFocusNode";
  public static final String aIsSubmitNode  = "IsSubmitNode";
  public static final String aIsApproveNode = "IsApproveNode";

}


