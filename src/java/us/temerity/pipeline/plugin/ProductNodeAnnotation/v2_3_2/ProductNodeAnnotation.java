package us.temerity.pipeline.plugin.ProductNodeAnnotation.v2_3_2;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O D U C T   N O D E   A N N O T A T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A node which is being versioned along with the approval node. <p>
 * 
 * This annotation exists to prevent this node from being checked in when the Approval node
 * is not being checked-in.  This prevents illict versioning of things that could 
 * contaminate the production process.
 * 
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Task Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the common production goal this node is used to achieve.
 *   </DIV> <BR> 
 * 
 *  Task Type <BR>
 *  <DIV style="margin-left: 40px;">
 *    The type of production goal this node is used to achieve.
 *  </DIV> <BR> 
 * </DIV> <P>
 */
public 
class ProductNodeAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ProductNodeAnnotation() 
  {
    super("ProductNode", new VersionID("2.3.2"), "Temerity", 
	  "A node which is being versioned along with the Approval node.");
    
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
  
  private static final long serialVersionUID = 7436765361486860344L;

  public static final String aTaskName = "TaskName";
  public static final String aTaskType = "TaskType";
  
}
