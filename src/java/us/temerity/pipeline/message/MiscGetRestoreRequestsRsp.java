// $Id: MiscGetRestoreRequestsRsp.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   R E S T O R E   R E Q U E S T S   R S P                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a requet to get the names and revision numbers of the checked-in 
 * versions which users have requested to be restored from an previously created archive. 
 */
public
class MiscGetRestoreRequestsRsp
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
   * @param requests
   *   The names of the archives containing the requested checked-in versions indexed by 
   *   fully resolved node name and revision number.
   */ 
  public
  MiscGetRestoreRequestsRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeMap<VersionID,TreeSet<String>>> requests
  )
  { 
    super(timer);

    if(requests == null) 
      throw new IllegalArgumentException("The restore requests cannot be (null)!");
    pRequests = requests;

    Logs.net.finest("MasterMgr.getRestoreRequests()\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets restore requests.
   */
  public TreeMap<String,TreeMap<VersionID,TreeSet<String>>>
  getRequests()
  {
    return pRequests;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8602222632897469336L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The restore requests.
   */ 
  private TreeMap<String,TreeMap<VersionID,TreeSet<String>>>  pRequests;

}
  
