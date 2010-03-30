// $Id: NodeGetMissingSiteVersionRefsRsp.java,v 1.1 2009/03/25 22:02:24 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   M I S S I N G   S I T E  V E R S I O N   R E F S   R S P             */
/*------------------------------------------------------------------------------------------*/

/**
 * Checks each of the source nodes referenced by the extracted node contained in the 
 * given JAR archive and returns the names and versions of any of them that are not
 * already in the node database.<P> 
 */
public
class NodeGetMissingSiteVersionRefsRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param missing
   *   The names and versions of the missing nodes.
   */
  public
  NodeGetMissingSiteVersionRefsRsp
  (
   TaskTimer timer, 
   TreeMap<String,VersionID> missing
  )
  { 
    super(timer);

    if(missing == null) 
      throw new IllegalArgumentException("The missing versions cannot be (null)!");
    pMissingVersions = missing;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the names and versions of the missing nodes.
   */
  public TreeMap<String,VersionID>
  getMissingVersions() 
  {
    return pMissingVersions;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6790819977837520311L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names and versions of the missing nodes.
   */
  private TreeMap<String,VersionID>  pMissingVersions;

}
  
