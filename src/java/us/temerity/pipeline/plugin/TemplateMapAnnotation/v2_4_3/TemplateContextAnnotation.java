// $Id: TemplateContextAnnotation.java,v 1.1 2008/10/02 00:26:56 jesse Exp $

package us.temerity.pipeline.plugin.TemplateContextAnnotation.v2_4_3;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C O N T E X T   A N N O T A T I O N                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation specifying the name of a context that is going to be applied to this node while
 * it is being built.
 * <p>
 * The node that this is on will be constructed from the template once for each list in the
 * context.
 */
public 
class TemplateContextAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateContextAnnotation()
  {
    super("TemplateContext", new VersionID("2.4.3"), "Temerity", 
          "Annotation specifying a name of the context that is going to be applied to this " +
          "node while it is being built.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aContextName,
         "The name of the context.",
         null
        );
      addParam(param);
    }
    
    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2189896375410619526L;

  public static final String aContextName = "ContextName";
}
