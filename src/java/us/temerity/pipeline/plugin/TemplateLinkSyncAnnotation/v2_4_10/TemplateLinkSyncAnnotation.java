// $Id: TemplateLinkSyncAnnotation.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateLinkSyncAnnotation.v2_4_10;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.MayaFTNBuildAction.v2_4_5.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   L I N K   S Y N C   A N N O T A T I O N                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation specifying a node node whose links the target node will copy during the 
 * finalize stage of the template builder.
 * <p>
 * One potential application of this annotation would be in a submit/approve workflow for
 * adding textures during shading.  In the submit step, the textures are added as sources to
 * a node which constructs a Maya scene ({@link MayaFTNBuildAction}) that is used by the 
 * shader.  In the approve stage, a grouping node will be created that groups all the 
 * textures that were used in the submit stage.  Applying this annotation to the approve 
 * product node and pointing its link param at the submit Maya scene would implement this 
 * behavior.
 * <p>
 * The source node does not need to, and probably should not, be linked to the target node.
 */
public 
class TemplateLinkSyncAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateLinkSyncAnnotation()
  {
    super("TemplateLinkSync", new VersionID("2.4.10"), "Temerity", 
          "Annotation specifying a node node whose links the target node will copy during " +
          "the finalize stage of the template builder.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aLinkName,
         "The name of the source node whose links will be copied.",
         null);
      addParam(param);
    }

    removeContext(AnnotationContext.PerNode);
    addContext(AnnotationContext.PerVersion);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7719842717769455354L;
  
  public static final String aLinkName = "LinkName";
}
