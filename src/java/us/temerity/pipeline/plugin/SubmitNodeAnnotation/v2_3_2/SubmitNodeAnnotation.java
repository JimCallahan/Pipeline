package us.temerity.pipeline.plugin.SubmitNodeAnnotation.v2_3_2;

import java.util.ArrayList;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S U B M I T   N O D E   A N N O T A T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A node that should be checked-in to signal that the task is ready for review.<P> 
 * 
 * All nodes with the IntermediateNode, EditNode and FocusNode annotations for the task
 * should be upstream of this node so that they are assured of being included in all check-in
 * of this node. 
 * 
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Task Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the common production goal this node is used to achieve.
 *   </DIV> <BR> 
 * 
 *   Task Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The type of production goal this node is used to achieve.
 *   </DIV> <BR> 
 *   
 *   Assigned To <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the WorkGroup or specific artist assigned to complete the task involving
 *     this node.  Only a user assigned to a node (or part of the group assigned to the node)
 *     is allowed to check-in this node. 
 *   </DIV> <BR> 
 *   
 *   Approve Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     The approve node that is associated with this Submit node.  Provided solely for 
 *     information purposes.
 *   </DIV> <BR> 
 * </DIV> <P> 
 */
public 
class SubmitNodeAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  SubmitNodeAnnotation() 
  {
    super("SubmitNode", new VersionID("2.3.2"), "Temerity", 
	  "A node that should be checked-in to signal that the task is ready for review.");
    
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
	(aTaskType, 
	 "The type of production goal this node is used to achieve.", 
	 null); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new WorkGroupAnnotationParam
	(aAssignedTo, 
	 "The name of the WorkGroup or specific artist assigned to complete the task " + 
         "involving this node.", 
	 true, true, null); 
      addParam(param);
    }
    
    { // IS THIS NEEDED??
      AnnotationParam param = 
	new StringAnnotationParam
	(aApproveNode, 
	 "The approve node that is associated with this Submit node.  " +
	 "Provided solely for information purposes.", 
	 null); 
      addParam(param);
    }
    
    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aTaskName);
      layout.add(aTaskType);
      layout.add(null);
      layout.add(aAssignedTo);
      layout.add(aApproveNode);
      setLayout(layout);
    }
    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 3689671873063787602L;

  public static final String aTaskName    = "TaskName";
  public static final String aTaskType    = "TaskType";
  public static final String aAssignedTo  = "AssignedTo";
  public static final String aApproveNode = "ApproveNode";
  
}
