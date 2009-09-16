// $Id: TemplateCheckpointAnnotation.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateCheckpointAnnotation.v2_4_6;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C H E C K P O I N T   A N N O T A T I O N                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Designate a checkpoint in an template.
 * <p>
 * A checkpoint is a node which needs to be finalized separately before other nodes in the
 * instantiated template are finalized.  Checkpoints need to be used where ever the 
 * instantiated network will end up a dubious node that is upstream from another dubious node.
 * Since the builder cannot resolve this situation automatically, a checkpoint should be used
 * on each dubious node, which will result in it being handled separately and correctly.
 * <p>
 * This annotation does not have any parameters.
 */
public 
class TemplateCheckpointAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateCheckpointAnnotation()
  {
    super("TemplateCheckpoint", new VersionID("2.4.6"), "Temerity", 
      "Designate a checkpoint in an template.");
    
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }
  
  private static final long serialVersionUID = -5068050741566100342L;
}
