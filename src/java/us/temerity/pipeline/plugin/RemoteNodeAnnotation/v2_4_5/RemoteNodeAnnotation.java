// $Id: RemoteNodeAnnotation.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.RemoteNodeAnnotation.v2_4_5;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   R E M O T E   N O D E   A N N O T A T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * An annotation which marks the node as having been created at another site and inserted
 * into the local node database directly.  Nodes with this annotation should not be manually
 * edited or checked-in locally.
 */
public 
class RemoteNodeAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  RemoteNodeAnnotation()
  {
    super("RemoteNode", new VersionID("2.4.5"), "Temerity", 
          "An annotation which marks the node as having been created at another site and " + 
          "inserted into the local node database directly.  Nodes with this annotation " + 
          "should not be manually edited or checked-in locally.");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2938017187453807347L;

}
