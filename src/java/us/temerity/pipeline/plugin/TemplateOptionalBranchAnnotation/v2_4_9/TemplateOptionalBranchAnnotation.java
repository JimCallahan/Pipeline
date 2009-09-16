// $Id: TemplateOptionalBranchAnnotation.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateOptionalBranchAnnotation.v2_4_9;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   O P T I O N A L   B R A N C H   A N N O T A T I O N                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Designate an optional branch in a template.
 * <p>
 * If the Template Builder is told not to build an optional branch, then the node with this
 * annotation on it will definitely not be built and any nodes upstream which are not shared
 * by any other parts of the template will also not be built. Upstream nodes which are used as
 * sources by other parts of the template will still be built.
 * <p>
 * The Option Name parameter is the name that the Template will use to identify the optional
 * branch.
 * <p>
 * The Option Type parameter determines the exact behavior of the template.  If it is set to 
 * BuildOnly, then when the option is disabled, the network is not built and the rest of the 
 * template will ignore the node. If Option Type is set to As Product, then the node 
 * will be ignored in terms of construction by the template but will be used as a product node
 * if a version exists in the repository.  If no version exists in the repository, then the
 * behavior will be identical to how BuildOnly works.
 */
public 
class TemplateOptionalBranchAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateOptionalBranchAnnotation()
  {
    super("TemplateOptionalBranch", new VersionID("2.4.9"), "Temerity", 
      "Designate an optional branch in a template.");
    
    {
      AnnotationParam param =
        new ParamNameAnnotationParam
        (aOptionName,
         "The name of the optional branch.",
         null);
      addParam(param);
    }
    
    {
      ArrayList<String> values = new ArrayList<String>(OptionalBranchType.titles());
      
      AnnotationParam param =
        new EnumAnnotationParam
        (aOptionType, 
         "Which sort of optional branch this is", 
         OptionalBranchType.BuildOnly.toTitle(), 
         values);
      addParam(param);
    }
    
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }

  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1871528190506446502L;
  
  public static final String aOptionName = "OptionName";
  public static final String aOptionType = "OptionType";
}
