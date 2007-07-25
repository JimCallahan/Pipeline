package us.temerity.pipeline.plugin.FocusNodeAnnotation.v2_3_2;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   F O C U S   N O D E   A N N O T A T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A node that should be inspected as part of the review process for a task.<p>
 * 
 * This type of node is located downstream from the EditNode and upstream from the 
 * SubmitNode for a task and are associated with the data that will be reviewed by 
 * the supervisor of the task in order to determine if the task has been completed
 * satisfactorily. <P> 
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
public class 
FocusNodeAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  FocusNodeAnnotation() 
  {
    super("FocusNode", new VersionID("2.3.2"), "Temerity", 
	  "A node that should be inspected as part of the review process for a task.");
    
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
  
  private static final long serialVersionUID = -3706261505162834053L;

  public static final String aTaskName = "TaskName";
  public static final String aTaskType = "TaskType";

}
