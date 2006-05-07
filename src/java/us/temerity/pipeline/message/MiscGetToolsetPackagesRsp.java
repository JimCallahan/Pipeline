// $Id: MiscGetToolsetPackagesRsp.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   P A C K A G E S   R S P                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetToolsetPackagesReq MiscGetToolsetPackagesReq} 
 * request.
 */
public
class MiscGetToolsetPackagesRsp
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
   * @param packages
   *   The toolset packages indexed by name, revision number and operating system type.
   */ 
  public
  MiscGetToolsetPackagesRsp
  (
   TaskTimer timer, 
   TripleMap<String,VersionID,OsType,PackageVersion> packages
  )
  { 
    super(timer);

    if(packages == null) 
      throw new IllegalArgumentException("The toolset package cannot be (null)!");
    pPackages = packages;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getToolsetPackages():\n" + 
       "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the toolset packages indexed by name, revision number and operating system type.
   */
  public TripleMap<String,VersionID,OsType,PackageVersion>
  getPackages() 
  {
    return pPackages;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1760215559821288352L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolset packages indexed by name, revision number and operating system type.
   */ 
  private TripleMap<String,VersionID,OsType,PackageVersion>  pPackages;

}
  
