// $Id: BaseBuilder.java,v 1.33 2007/11/01 19:08:53 jesse Exp $

package us.temerity.pipeline.plugin.TemplateOffsetAnnotation.v2_4_25;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   O F F S E T   A N N O T A T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation allowing the specification of a frame offset value on a link between two nodes 
 * in a template.
 */
public 
class TemplateOffsetAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateOffsetAnnotation()
  {
    super("TemplateOffset", new VersionID("2.4.25"), "Temerity", 
          "Annotation allowing the specification of a frame offset value on a link between " +
          "two nodes in a template.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aOffsetName,
         "The name of the frame offset.",
         null);
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aLinkName,
         "The name of the linked mode to apply the frame offset to.",
         null);
      addParam(param);
    }
    
    underDevelopment();
    
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 490853465049066834L;

  public static final String aOffsetName = "OffsetName";
  public static final String aLinkName   = "LinkName";
}
