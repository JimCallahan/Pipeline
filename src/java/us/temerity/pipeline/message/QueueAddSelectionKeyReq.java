// $Id: QueueAddSelectionKeyReq.java,v 1.1 2004/07/25 03:13:17 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   S E L E C T I O N   K E Y   R E Q                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add a selection key to the currently defined selection keys. <P> 
 */
public
class QueueAddSelectionKeyReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param key
   *   The selection key to add.
   */
  public
  QueueAddSelectionKeyReq
  (
   SelectionKey key
  )
  { 
    if(key == null) 
      throw new IllegalArgumentException
	("The selection key cannot be (null)!");
    pKey = key;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the selection key to add. 
   */
  public SelectionKey
  getSelectionKey() 
  {
    return pKey;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1873239053467007256L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection key to add.
   */ 
  private SelectionKey pKey;

}
  
