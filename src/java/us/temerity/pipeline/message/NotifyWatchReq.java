// $Id: NotifyWatchReq.java,v 1.1 2004/04/11 19:31:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   W A T C H   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to report any directories which have been modified.
 * 
 * @see DNotify
 */
public
class NotifyWatchReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   */
  public
  NotifyWatchReq() 
  {}
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3058409318969150381L;

}
  
