package us.temerity.pipeline.plugin.IntermediateNodeAnnotation.v2_3_2;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   I N T E R M E D I A T E   N O D E   A N N O T A T I O N                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A node that is between the edit and submit nodes used to accomplish some intermediate 
 * procedural step but has node special significance for the artist completing the task.<P> 
 * 
 * This annotation exists to prevent this node from being checked in when the SubmitNode
 * is not being checked-in.  This prevents there from being extraneous versions of nodes
 * that do not need to have them.
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
class IntermediateNodeAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  IntermediateNodeAnnotation() 
  {
    super("IntermediateNode", new VersionID("2.3.2"), "Temerity", 
	  "A node that is between the EditNode and SubmitNode used to accomplish some " + 
          "intermediate procedural step but has node special significance for the artist " +
          "completing the task.");
    
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

    underDevelopment();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 465428803901006872L;

  public static final String aTaskName = "TaskName";
  public static final String aTaskType = "TaskType";

}
