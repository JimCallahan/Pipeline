// $Id: TemplateConditionalBuildAnnotation.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateConditionalBuildAnnotation.v2_4_3;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.TemplateIgnoreProductAnnotation.v2_4_3.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C O N D I T I O N A L   B U I L D   A N N O T A T I O N              */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation which specifies whether to build a particular node based on the existence of
 * another node.
 * <p>
 * The condition name will have all the replacements applied to it that are being applied to
 * the node which this annotation is located on.  That means that this node should have 
 * matching replacements as the Condition Name.  If it doesn't, it will be easy to end up in
 * a case where this node is never built due to the condition never being matched.
 * <p>
 * This annotation is useful in cases where it is desirable to constrain context expansion to
 * cases where a product nodes actually exists.  In those cases, a 
 * {@link TemplateIgnoreProductAnnotation} can be used to ignore the product nodes which do
 * not exist and can be paired with a Conditional Build Annotation whose condition is set to
 * the name of the product node.  In that case, the node with this annotation will only be
 * built if it corresponding product node exists.
 * <p>
 * Nodes in a template which link to a Conditional Build node will allow for the non-existance
 * of the conditional nodes.
 */
public 
class TemplateConditionalBuildAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateConditionalBuildAnnotation()
  {
    super("TemplateConditionalBuild", new VersionID("2.4.3"), "Temerity", 
          "Annotation specifying a name of the context that is going to be applied to the " + 
          "named link when the template is instantiated.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aConditionName,
         "The name of the node whose existance controls whether this node gets built.",
         null);
      addParam(param);
    }
    
    addContext(AnnotationContext.PerVersion);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2189283796790818408L;

  public static final String aConditionName    = "ConditionName";
}
