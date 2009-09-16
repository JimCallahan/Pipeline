// $Id: TemplateOrderAnnotation.java,v 1.3 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateOrderAnnotation.v2_4_3;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   O R D E R   A N N O T A T I O N                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Specify an order for the root nodes in the template.
 * <p>
 * The nodes with a lower order will be queued and checked-in before nodes with higher orders.
 * This is important in the case of networks using Task v2.4.1 setups, where the Submit node
 * needs to be checked in before the Approval network to avoid having the TaskGuard Extension
 * deny the check-in.
 * <p>
 * If this annotation is placed on a non-root node, it will be ignored. Root nodes without any
 * order annotation will go last.
 */
public 
class TemplateOrderAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  TemplateOrderAnnotation()
  {
    super("TemplateOrder", new VersionID("2.4.3"), "Temerity", 
          "Specify an order for the root nodes in the template.");
    
    {
      AnnotationParam param = 
        new IntegerAnnotationParam
        (aOrder,
         "The order in which a root node will be queued and checked-in.",
         100);
      addParam(param);
    }
    
    addContext(AnnotationContext.PerVersion);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 4468389112513980207L;

  public static final String aOrder = "Order";
}
