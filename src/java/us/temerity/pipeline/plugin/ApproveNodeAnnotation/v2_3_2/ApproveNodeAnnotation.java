package us.temerity.pipeline.plugin.ApproveNodeAnnotation.v2_3_2;

import java.util.ArrayList;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A P P R O V E   N O D E   A N N O T A T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * A node that needs to be rebuilt each time a task has been reviewed and approved.<P> 
 * 
 * Check-in of this node signals the completion of an approval step. In other words, that 
 * all nodes upstream of this node have been reviewed and officially approved.
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
 * </DIV> <P>
 * 
 * @deprecated
 *   This class has been made obsolete by the TaskAnnotation (v2.3.2).
 */
@Deprecated
public 
class ApproveNodeAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ApproveNodeAnnotation() 
  {
    super("ApproveNode", new VersionID("2.3.2"), "Temerity", 
	  "A node that needs to be rebuilt each time a task has been reviewed and approved.");
    
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
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1897099035648320035L;

  public static final String aTaskName = "TaskName";
  public static final String aTaskType = "TaskType";

}
