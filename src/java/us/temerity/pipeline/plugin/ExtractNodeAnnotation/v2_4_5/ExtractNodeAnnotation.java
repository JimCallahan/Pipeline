// $Id: ExtractNodeAnnotation.java,v 1.2 2009/09/16 15:56:45 jesse Exp $

package us.temerity.pipeline.plugin.ExtractNodeAnnotation.v2_4_5;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   E X T R A C T   N O D E   A N N O T A T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An annotation which indicated the after check-in each version of this node should be 
 * extraced into a JAR archive suitable for insertion into the node database at a remote
 * site.  
 */
public 
class ExtractNodeAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ExtractNodeAnnotation()
  {
    super("ExtractNode", new VersionID("2.4.5"), "Temerity", 
          "An annotation which indicated the after check-in each version of this node " + 
          "should be extraced into a JAR archive suitable for insertion into the node " +
          "database at a remote site.");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4613022616473719895L;

}
