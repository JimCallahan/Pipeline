// $Id: QueueGetKeyNamesReq.java,v 1.1 2008/03/07 13:25:21 jim Exp $

package us.temerity.pipeline.message.node;

import java.io.Serializable;
import java.util.TreeMap; 

import us.temerity.pipeline.NodeID; 
import us.temerity.pipeline.CheckSumCache; 

/*------------------------------------------------------------------------------------------*/
/*   N O D E   U P D A T E   C H E C K S U M S   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Sent updates to the checksums for files associated with the given set of working versions. 
 */
public 
class NodeUpdateCheckSumsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param checksums
   *   The checksum caches for each working version.
   */
  public
  NodeUpdateCheckSumsReq
  (
   TreeMap<NodeID,CheckSumCache> checksums
  )
  {
    if(checksums == null) 
      throw new IllegalArgumentException("The checksums cannot be (null)!"); 
    pCheckSums = checksums;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the checksum caches for each working version.
   */ 
  public TreeMap<NodeID,CheckSumCache>
  getCheckSums()
  {
    return pCheckSums;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8566561408485419567L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checksum caches for each working version.
   */ 
  private TreeMap<NodeID,CheckSumCache> pCheckSums;

}
