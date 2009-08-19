// $Id: MiscGetRestoreRequestsRsp.java,v 1.5 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   R E S T O R E   R E Q U E S T S   R S P                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the requests for restoration of checked-in versions.
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
   *   The restore requests for checked-in versions indexed by fully resolved node 
   *   name and revision number.
   */ 
  public
  MiscGetRestoreRequestsRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeMap<VersionID,RestoreRequest>> requests
  )
  { 
    super(timer);

    if(requests == null) 
      throw new IllegalArgumentException("The restore requests cannot be (null)!");
    pRequests = requests;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getRestoreRequests()\n  " + getTimer());
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets restore requests.
   */
  public TreeMap<String,TreeMap<VersionID,RestoreRequest>>
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
  private TreeMap<String,TreeMap<VersionID,RestoreRequest>> pRequests;

}
  
