// $Id: MiscGetArchivedOnRsp.java,v 1.3 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A R C H I V E D   O N   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a requet to get the names and creation timestamps of all 
 * existing archives. <P> 
 */
public
class MiscGetArchivedOnRsp
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
   * @param index
   *   The timestamps of when each archive was created indexed by unique archive name.
   */ 
  public
  MiscGetArchivedOnRsp
  (
   TaskTimer timer, 
   TreeMap<String,Long> index
  )
  { 
    super(timer);

    if(index == null) 
      throw new IllegalArgumentException("The restore index cannot be (null)!");
    pIndex = index;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getArchivedOn()\n  " + getTimer());
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the timestamps of when each archive was created indexed by unique archive name.
   */
  public TreeMap<String,Long>
  getIndex()
  {
    return pIndex;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1634842766569482147L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamps of when each archive was created indexed by unique archive name.
   */ 
  private TreeMap<String,Long>  pIndex;

}
  
