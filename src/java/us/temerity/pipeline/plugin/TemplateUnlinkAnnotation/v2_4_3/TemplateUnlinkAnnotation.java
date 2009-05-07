// $Id: TemplateUnlinkAnnotation.java,v 1.2 2009/05/07 03:12:50 jesse Exp $

package us.temerity.pipeline.plugin.TemplateUnlinkAnnotation.v2_4_3;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   U N L I N K   A N N O T A T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Template Annotation for specifying a source node that should be unlinked after the
 * template has constructed the node.
 */
public 
class TemplateUnlinkAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  TemplateUnlinkAnnotation()
  {
    super("TemplateUnlink", new VersionID("2.4.3"), "Temerity", 
          "Specify a node to unlink after the network has been built.");
    
    {
      AnnotationParam param = 
        new StringAnnotationParam
        (aLinkName,
         "The name of the node to unlink.",
         null);
      addParam(param);
    }

    addContext(AnnotationContext.PerVersion);
    
    underDevelopment();
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2676597177488844309L;
  
  public static final String aLinkName = "LinkName";
}
