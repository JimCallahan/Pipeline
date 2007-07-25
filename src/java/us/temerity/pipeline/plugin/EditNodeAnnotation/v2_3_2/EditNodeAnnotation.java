package us.temerity.pipeline.plugin.EditNodeAnnotation.v2_3_2;

import java.util.ArrayList;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   E D I T   N O D E   A N N O T A T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Identifies the nodes that an artist should be editing in order to accomplish a task.
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
class EditNodeAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EditNodeAnnotation()
  {
    super("EditNode", new VersionID("2.3.2"), "Temerity", 
	  "Identifies the nodes that an artist should be editing in order to accomplish " +
          "a task.");
    
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
  
  public static final String aTaskName = "TaskName";
  public static final String aTaskType = "TaskType";
  
  private static final long serialVersionUID = 4845630839422760725L;
  
}
