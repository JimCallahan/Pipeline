package us.temerity.pipeline.plugin.EditNodeAnnotation.v2_3_2;

import java.util.ArrayList;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   E D I T   N O D E   A N N O T A T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Identifies a node as a node that an artist should be editing and provides a field to
 * identify the artist who is assigned to this node.
 * 
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Task Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the common production goal this node is used to achieve.
 *   </DIV> <BR>
 * 
 *   Assigned To <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the WorkGroup or specific artist assigned to complete the task involving
 *     this node.  In the case of the edit node, this field is purely informational.  There
 *     are no restrictions place upon the check-in of the edit node.
 *   </DIV> <BR>
 *   
 *   Submit Node Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the submit node that is associated with this edit node.  Provided solely to
 *     make it easy for a user to move from an edit node to a submit node.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
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
	  "Identifies a node as a node that an artist should be editing and provides a field to " +
	  "identify the artist who is assigned to this node.");
    
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
	new WorkGroupAnnotationParam
	(aAssignedTo, 
	 "The name of the WorkGroup or specific artist assigned to complete the task " + 
         "involving this node.", 
	 true, true, null); 
      addParam(param);
    }
    
    {
      AnnotationParam param = 
	new StringAnnotationParam
	(aSubmitNodeName, 
	 "The name of the common production goal this node is used to achieve.", 
	 null); 
      addParam(param);
    }
    
    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aTaskName);
      layout.add(null);
      layout.add(aAssignedTo);
      layout.add(aSubmitNodeName);
      setLayout(layout);
    }
    underDevelopment();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aTaskName       = "TaskName";
  public static final String aAssignedTo     = "AssignedTo";
  public static final String aSubmitNodeName = "SubmitNodeName";
  
  private static final long serialVersionUID = 4845630839422760725L;
  
}
