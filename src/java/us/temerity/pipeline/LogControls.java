// $Id: LogControls.java,v 1.1 2006/11/10 21:57:23 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G   C O N T R O L S                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A specification of logging levels for a subset of the kinds of loggers.
 */
public
class LogControls  
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new set of log controls.
   */ 
  public 
  LogControls() 
  {
    pLevels = new TreeMap<LogMgr.Kind,LogMgr.Level>(); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the logging level for the given kind of log message.
   * 
   * @param kind
   *   The kind of message being logged.
   */ 
  public LogMgr.Level
  getLevel
  (
   LogMgr.Kind kind
  ) 
  {
    return pLevels.get(kind);
  }

  /**
   * Set the logging level for the given kind of log message.
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   */ 
  public void 
  setLevel
  (
   LogMgr.Kind kind, 
   LogMgr.Level level
  ) 
  {
    pLevels.put(kind, level);
  }

  /**
   * Set the logging level for all kinds of log messages.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   */ 
  public synchronized void 
  setLevels
  (
   LogMgr.Level level
  ) 
  {
    LogMgr.Kind kinds[] = LogMgr.Kind.values();
    int wk;
    for(wk=0; wk<kinds.length; wk++)
      pLevels.put(kinds[wk], level);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2829703787682428786L; 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The level of logging verbosity for each kind of message.
   */
  private TreeMap<LogMgr.Kind,LogMgr.Level>  pLevels; 

}


