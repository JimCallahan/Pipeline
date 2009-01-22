// $Id: FileGetOfflinedNodeVersionsRsp.java,v 1.1 2009/01/22 23:38:01 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   G E T   O F F L I N E D   N O D E   V E R S I O N S   R S P                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileGetOfflinedNodeVersionsReq} request.
 */
public
class FileGetOfflinedNodeVersionsRsp
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
   *   The revision numbers of all offlined checked-in versions of the given node.
   */
  public
  FileGetOfflinedNodeVersionsRsp
  (
   TaskTimer timer,
   TreeSet<VersionID> versions
  )
  { 
    super(timer);

    if(versions == null) 
      throw new IllegalArgumentException
	("The offlined versions cannot be (null)!");
    pVersions = versions;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "FileMgr.getOfflinedNodeVersions():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the revision numbers of all offlined checked-in versions of the given node.
   */
  public TreeSet<VersionID>
  getVersions() 
  {
    return pVersions;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4425708630616373449L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision numbers of all offlined checked-in versions of the given node.
   */ 
  private  TreeSet<VersionID>  pVersions; 

}
  
