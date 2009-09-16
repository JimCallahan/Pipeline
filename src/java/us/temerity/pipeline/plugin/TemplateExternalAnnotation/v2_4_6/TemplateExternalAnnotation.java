// $Id: TemplateExternalAnnotation.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateExternalAnnotation.v2_4_6;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   E X T E R N A L   A N N O T A T I O N                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation specifying a node whose files should come from outside Pipeline.
 */
public 
class TemplateExternalAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateExternalAnnotation()
  {
    super("TemplateExternal", new VersionID("2.4.6"), "Temerity", 
          "Annotation specifying a name of the context that is going to be applied to this " +
          "node while it is being built.");
    
    {
      AnnotationParam param =
        new ParamNameAnnotationParam
        (aExternalName,
         "The reference name for the external sequence.",
         null);
      addParam(param);
    }
    
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5568690866785446843L;
  
  public static final String aExternalName = "ExternalName";
}
