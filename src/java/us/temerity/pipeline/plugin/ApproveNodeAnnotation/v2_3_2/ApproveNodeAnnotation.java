package us.temerity.pipeline.plugin.ApproveNodeAnnotation.v2_3_2;

import java.util.ArrayList;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A P P R O V E   N O D E   A N N O T A T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Identifies a node as the node that needs to be rebuilt each time a task is approved. <p>
 * 
 * Check-in of this node signals the completion of an approval step: the nodes below it are
 * now offically approved.
 * 
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Task Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the common production goal this node is used to achieve.
 *   </DIV> <BR>
 *    
 *   IsApproved <BR>
 *   <DIV style="margin-left: 40px;">
 *     Is this node actually approved?  This parameter prevents the node from being checked-in
 *     unless it is set to true.  By default this value is never set to true, except right
 *     before an automated process checks in this node.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
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
	  "Identifies a node as the node that needs to be rebuilt each time a task is approved.");
    
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
	new BooleanAnnotationParam
	(aIsApproved, 
	 "Is this node actually approved?  This parameter prevents the node from being " +
	 "checked-in unless it is set to true.  By default this value is never set to true, " +
	 "except right before an automated process checks in this node.", 
	 false); 
      addParam(param);
    }
    
    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aTaskName);
      layout.add(null);
      layout.add(aIsApproved);
      setLayout(layout);
    }
    underDevelopment();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aTaskName   = "TaskName";
  public static final String aIsApproved = "IsApproved";
  
  private static final long serialVersionUID = -1897099035648320035L;

}
