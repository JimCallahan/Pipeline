// $Id: MiscGetToolsetRsp.java,v 1.3 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   R S P                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetToolsetReq MiscGetToolsetReq} request.
 */
public
class MiscGetToolsetRsp
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
   * @param tset
   *   The toolset.
   */ 
  public
  MiscGetToolsetRsp
  (
   TaskTimer timer, 
   Toolset tset
  )
  { 
    super(timer);

    if(tset == null) 
      throw new IllegalArgumentException("The toolset cannot be (null)!");
    pToolset = tset;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"MasterMgr.getToolset(): " + pToolset.getName() + "\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the toolset.
   */
  public Toolset
  getToolset() 
  {
    return pToolset;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 75554148782358048L;


  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolset.
   */ 
  private Toolset  pToolset;

}
  
