// $Id: TemplateVouchableAnnotation.java,v 1.3 2009/10/09 04:34:29 jesse Exp $

package us.temerity.pipeline.plugin.TemplateVouchableAnnotation.v2_4_6;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   V O U C H A B L E   A N N O T A T I O N                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Designate a node which may end up in a Dubious state during a template run, which the 
 * template builder can Vouch for.
 * <p>
 * This annotation does not have any parameters.
 */
public 
class TemplateVouchableAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateVouchableAnnotation()
  {
    super("TemplateVouchable", new VersionID("2.4.6"), "Temerity", 
      "Designate which may end up in a Dubious state during a template run, with the " +
      "template builder can Vouch for.");
    
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }

  private static final long serialVersionUID = 237237833135476071L;
}
