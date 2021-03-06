// $Id: TemplateContextLinkAnnotation.java,v 1.3 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateContextLinkAnnotation.v2_4_3;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.TemplateContextAnnotation.v2_4_3.*;


/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C O N T E X T   L I N K   A N N O T A T I O N                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation specifying the name of a context that is going to be applied to the named link
 * when the template is instantiated.
 * <p>
 * This only needs to be used when the node being linked is outside the current template. If
 * the linked node is in the current template, then the {@link TemplateContextAnnotation}
 * should be used.
 */
public 
class TemplateContextLinkAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateContextLinkAnnotation()
  {
    super("TemplateContextLink", new VersionID("2.4.3"), "Temerity", 
          "Annotation specifying a name of the context that is going to be applied to the " + 
          "named link when the template is instantiated.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aContextName,
         "The name of the context",
         null);
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aLinkName,
         "The name of the linked mode to apply the context to.",
         null);
      addParam(param);
    }

    addContext(AnnotationContext.PerVersion);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8753807750309229909L;

  public static final String aContextName = "ContextName";
  public static final String aLinkName    = "LinkName";
}

