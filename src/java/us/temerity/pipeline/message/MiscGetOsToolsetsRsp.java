// $Id: MiscGetOsToolsetsRsp.java,v 1.1 2006/07/03 06:38:42 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   O S   T O O L S E T   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetOsToolsetsReq MiscGetOsToolsetsReq} request.
 */
public
class MiscGetOsToolsetsRsp
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
   * @param tsets
   *   The OS specific toolsets indexed by operating system type. 
   */ 
  public
  MiscGetOsToolsetsRsp
  (
   TaskTimer timer, 
   TreeMap<OsType,Toolset> tsets
  )
  { 
    super(timer);

    if(tsets == null) 
      throw new IllegalArgumentException("The toolsets cannot be (null)!");
    pToolsets = tsets;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getOsToolsets(): " + tsets.get(OsType.Unix).getName() + 
       "\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the toolsets.
   */
  public TreeMap<OsType,Toolset>
  getToolsets() 
  {
    return pToolsets;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 476336112385510310L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolsets.
   */ 
  private TreeMap<OsType,Toolset>  pToolsets;

}
  
