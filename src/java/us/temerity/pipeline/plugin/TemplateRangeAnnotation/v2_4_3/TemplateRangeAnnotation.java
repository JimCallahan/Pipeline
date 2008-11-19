// $Id: TemplateRangeAnnotation.java,v 1.1 2008/11/19 04:34:48 jesse Exp $

package us.temerity.pipeline.plugin.TemplateRangeAnnotation.v2_4_3;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   R A N G E   A N N O T A T I O N                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation specifying the name of the frame range that the template will assign to the
 * node.
 * <p>
 * Frame ranges will be assigned to nodes that either have a file sequence as their primary
 * file sequence or that have StartFrame and EndFrame (and possibly ByFrame) parameters.
 */
public 
class TemplateRangeAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateRangeAnnotation()
  {
    super("TemplateRange", new VersionID("2.4.3"), "Temerity", 
          "Annotation specifying the name of the frame range that the template will assign " +
          "to the node while it is being built.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aRangeName,
         "The name of the range.",
         null
        );
      addParam(param);
    }
    
    underDevelopment();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3457597426233323974L;

  public static final String aRangeName = "RangeName";
}
