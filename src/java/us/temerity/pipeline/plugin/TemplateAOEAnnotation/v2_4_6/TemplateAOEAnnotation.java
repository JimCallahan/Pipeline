// $Id: TemplateAOEAnnotation.java,v 1.1 2009/05/07 22:12:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateAOEAnnotation.v2_4_6;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   A O E   A N N O T A T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Template annotation used to specify a particular AOE override for the tagged node in the 
 * given mode.
 */
public 
class TemplateAOEAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateAOEAnnotation()
  {
    super("TemplateAOE", new VersionID("2.4.6"), "Temerity", 
          "Template annotation used to specify a particular AOE override for the tagged node " +
          "in the given mode.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aModeName,
         "The name of the AOE mode.",
         null);
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new EnumAnnotationParam
        (aActionOnExistence,
         "The Action on Existence to apply to this node in the specified mode.",
         ActionOnExistence.Continue.toString(),
         ActionOnExistence.titles());
      addParam(param);
    }
    
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);

    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aModeName = "ModeName";
  public static final String aActionOnExistence = "ActionOnExistence";
  
  private static final long serialVersionUID = 4188990131183085601L;
}
