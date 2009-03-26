// $Id: TemplateIgnoreProductAnnotation.java,v 1.1 2009/03/26 00:04:16 jesse Exp $

package us.temerity.pipeline.plugin.TemplateIgnoreProductAnnotation.v2_4_3;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.TemplateContextLinkAnnotation.v2_4_3.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   I G N O R E   P R O D U C T   A N N O T A T I O N                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation specifying a product node which can be ignored if it doesn't exist.
 * <p>
 * Normally a missing product node causes a builder to abort execution.  When this annotation
 * is used, the missing product will just be ignored and execution will continue normally.
 * <p>
 * This annotation needs to be placed on all the target nodes where the source is considered
 * optional.  This will interact with all contexts that are being assigned with 
 * {@link TemplateContextLinkAnnotation TemplateContextLinkAnnotations}, so it allow the
 * specification of a broad context which will only be applied in cases where the source nodes 
 * actually exist.
 */
public 
class TemplateIgnoreProductAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateIgnoreProductAnnotation()
  {
    super("TemplateIgnoreProduct", new VersionID("2.4.3"), "Temerity", 
          "Annotation specifying a product node which can be ignored if it doesn't exist.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aLinkName,
         "The name of the linked mode which can be ignored.",
         null);
      addParam(param);
    }

    addContext(AnnotationContext.PerVersion);
    
    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7072316316293833856L;

  public static final String aLinkName    = "LinkName";
}
