// $Id: TemplateMapAnnotation.java,v 1.1 2008/09/19 03:30:10 jesse Exp $

package us.temerity.pipeline.plugin.TemplateMapAnnotation.v2_4_3;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   M A P   B U I L D   A N N O T A T I O N                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation specifying the name of a map that is going to be applied to this node while
 * it is being built.
 * <p>
 * The node that this is on will be constructed from the template once for each list in the 
 * map.
 */
public 
class TemplateMapAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateMapAnnotation()
  {
    super("TemplateMap", new VersionID("2.4.3"), "Temerity", 
          "Annotation specifying a name of the map that is going to be applied to this node " +
          "while it is being built.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aMapName,
         "The name of the map",
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

  public static final String aMapName = "MapName";
}
