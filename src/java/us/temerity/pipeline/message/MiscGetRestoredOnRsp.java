// $Id: MiscGetRestoredOnRsp.java,v 1.1 2005/04/03 01:54:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   R E S T O R E D   O N   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a requet to get the names and restoration timestamps of all 
 * existing archives. <P> 
 */
public
class MiscGetRestoredOnRsp
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
   *   The timestamps of when each archive was restored indexed by unique archive name.
   */ 
  public
  MiscGetRestoredOnRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeSet<Date>> index
  )
  { 
    super(timer);

    if(index == null) 
      throw new IllegalArgumentException("The restore index cannot be (null)!");
    pIndex = index;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getRestoredOn()\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the timestamps of when each archive was restored indexed by unique archive name.
   */
  public TreeMap<String,TreeSet<Date>>
  getIndex()
  {
    return pIndex;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5406258485706772647L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamps of when each archive was restored indexed by unique archive name.
   */ 
  private TreeMap<String,TreeSet<Date>>  pIndex;

}
  
