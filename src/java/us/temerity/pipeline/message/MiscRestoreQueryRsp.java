// $Id: MiscRestoreQueryRsp.java,v 1.1 2005/03/23 20:45:01 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   R E S T O R E   Q U E R Y   R S P                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscRestoreQueryReq MiscRestoreQueryReq} request.
 */
public
class MiscRestoreQueryRsp
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
   * @param versions
   *   The fully resolved node names and revision numbers of the matching versions.
   */ 
  public
  MiscRestoreQueryRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeSet<VersionID>> versions
  )
  { 
    super(timer);

    if(versions == null) 
      throw new IllegalArgumentException("The node versions cannot be (null)!");
    pVersions = versions;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.restoreQuery()\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node names and revision numbers of the matching versions.
   */
  public TreeMap<String,TreeSet<VersionID>> 
  getVersions()
  {
    return pVersions; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 518623356334220890L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names and revision numbers of the matching versions.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions;

}
  
