package us.temerity.pipeline.plugin.FocusNodeAnnotation.v2_3_2;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   F O C U S   N O D E   A N N O T A T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/**
 *  A node that should be inspected as part of the review process for a task.<p>
 *  
 *  Downstream from the edit node and upstream from the submit node.  These nodes
 *  are what a supervisor will look at to get an idea of whether the task is 
 *  satisfactory. <p>
 *  
 *  This annotation defines the following parameters: <BR>
 * 
 *  <DIV style="margin-left: 40px;">
 *    Task Name <BR>
 *    <DIV style="margin-left: 40px;">
 *      The name of the common production goal this node is used to achieve.
 *    </DIV> <BR> 
 *  </DIV> <P>
 */
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
    underDevelopment();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aTaskName   = "TaskName";
  private static final long serialVersionUID = -3706261505162834053L;

}
