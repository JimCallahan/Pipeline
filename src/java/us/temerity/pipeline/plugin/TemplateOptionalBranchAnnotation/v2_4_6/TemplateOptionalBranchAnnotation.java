// $Id: TemplateOptionalBranchAnnotation.java,v 1.1 2009/05/26 07:09:32 jesse Exp $

package us.temerity.pipeline.plugin.TemplateOptionalBranchAnnotation.v2_4_6;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   O P T I O N A L   B R A N C H   A N N O T A T I O N                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Designate an optional branch if a template.
 * <p>
 * If the Template Builder is told not to build an optional branch, then the node with this
 * annotation on it will definitely not be built and any nodes upstream which are not shared
 * by any other parts of the template will also not be built. Upstream nodes which are used as
 * sources by other parts of the template will still be built.
 * <p>
 * The Option Name parameter is the name that the Template will use to identify the optional
 * branch
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
    super("TemplateOptionalBranch", new VersionID("2.4.6"), "Temerity", 
      "Designate an optional branch in a template.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aOptionName,
         "The name of the optional branch.",
         null);
      addParam(param);
    }

    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
    
    underDevelopment();
  }
 
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7956020650301407282L;
  
  public static final String aOptionName = "OptionName";
}
