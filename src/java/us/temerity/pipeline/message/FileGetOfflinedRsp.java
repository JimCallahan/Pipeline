// $Id: FileGetOfflinedRsp.java,v 1.3 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   G E T   O F F L I N E D   R S P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileGetOfflinedReq FileGetOfflinedReq} request.
 */
public
class FileGetOfflinedRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response. <P> 
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of all offlined checked-in versions.
   */
  public
  FileGetOfflinedRsp
  (
   TaskTimer timer,
   TreeMap<String,TreeSet<VersionID>> versions
  )
  { 
    super(timer);

    if(versions == null) 
      throw new IllegalArgumentException
	("The offlined versions cannot be (null)!");
    pVersions = versions;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "FileMgr.getOfflined():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved names and revision numbers of all offlined checked-in versions.
   */
  public TreeMap<String,TreeSet<VersionID>>
  getVersions() 
  {
    return pVersions;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1623003001052551372L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names and revision numbers of all offlined checked-in versions.
   */ 
  private  TreeMap<String,TreeSet<VersionID>>  pVersions; 

}
  
