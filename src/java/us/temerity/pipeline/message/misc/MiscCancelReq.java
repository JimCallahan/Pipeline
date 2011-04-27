// $Id: MiscPluginIDReq.java,v 1.2 2010/01/07 19:19:09 jesse Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   C A N C E L   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 *  A server request that takes a PluginID as an argument. 
 */
public 
class MiscCancelReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new request.
   * 
   * @param sessionID
   *   Get the unique connection session ID.
   */
  public 
  MiscCancelReq
  (
    long sessionID  
  )
  {    
    pSessionID = sessionID;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the unique connection session ID.
   */
  public long
  getSessionID()
  {
    return pSessionID;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6585446062361504039L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private long pSessionID;
}
