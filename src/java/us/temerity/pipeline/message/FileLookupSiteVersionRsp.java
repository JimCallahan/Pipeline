// $Id: FileLookupSiteVersionRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*  F I L E   L O O K U P   S I T E  V E R S I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Lookup the NodeVersion contained within the extracted site version JAR archive.
 */
public
class FileLookupSiteVersionRsp
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
   * @param vsn
   *   The node version. 
   */
  public
  FileLookupSiteVersionRsp
  (
   TaskTimer timer, 
   NodeVersion vsn
  )
  { 
    super(timer);

    if(vsn == null) 
      throw new IllegalArgumentException("The node version cannot be (null)!"); 
    pNodeVersion = vsn;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "FileMgr.lookupSiteVersion(): " + vsn.getName() + " (v" + vsn.getVersionID() + ")\n" + 
       "  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The node version.
   */
  public NodeVersion
  getNodeVersion() 
  {
    return pNodeVersion; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1222692887643372306L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The node version; 
   */
  private NodeVersion  pNodeVersion; 

}
  
