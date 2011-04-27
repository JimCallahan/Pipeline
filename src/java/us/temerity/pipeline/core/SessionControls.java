// $Id: VersionID.java,v 1.18 2008/07/22 21:37:30 jesse Exp $

package us.temerity.pipeline.core;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S E S S I O N   C O N T R O L S                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Communicates network connection session related status between Manager and ManagerServer
 * instances.
 */
public
class SessionControls
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new session table.
   */ 
  public 
  SessionControls()
  {
    pCancelled = new TreeSet<Long>();
  }

  
 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Add the given session to those which should have their operations cancelled.
   * 
   * @param sessionID
   *   The unique connection session ID.
   */ 
  public synchronized void
  cancel
  (
   long sessionID
  )
  {
    pCancelled.add(sessionID);
  }

  /** 
   * Whether operations from the given session should be cancelled. <P> 
   * 
   * Any existing cancel flag for the session is cleared by this operation.
   * 
   * @param sessionID
   *   The unique connection session ID.
   */ 
  public synchronized boolean 
  isCancelled
  (
   long sessionID
  ) 
  {
    return pCancelled.remove(sessionID);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * The session IDs of connections which have issued a cancel request.
   */
  private TreeSet<Long>  pCancelled;
  
}



