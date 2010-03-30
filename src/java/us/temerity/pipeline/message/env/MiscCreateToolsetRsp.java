// $Id: MiscCreateToolsetRsp.java,v 1.5 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.env;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   C R E A T E   T O O L S E T   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscCreateToolsetReq MiscCreateToolsetReq} request.
 */
public
class MiscCreateToolsetRsp
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
  MiscCreateToolsetRsp
  (
   TaskTimer timer, 
   Toolset tset
  )
  { 
    super(timer);

    if(tset == null) 
      throw new IllegalArgumentException("The toolset cannot be (null)!");
    pToolset = tset;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.createToolset(): " + pToolset.getName() + "\n" + 
       "  " + getTimer());
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

  private static final long serialVersionUID = -6858822285293037957L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolset.
   */ 
  private Toolset  pToolset;

}
  
