// $Id: MiscGetToolsetEnvironmentRsp.java,v 1.3 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   E N V I R O N M E N T   R S P                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a 
 * {@link MiscGetToolsetEnvironmentReq MiscGetToolsetEnvironmentReq} request.
 */
public
class MiscGetToolsetEnvironmentRsp
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
   * @param env
   *   The cooked toolset environment.
   */ 
  public
  MiscGetToolsetEnvironmentRsp
  (
   TaskTimer timer, 
   String name, 
   TreeMap<String,String> env
  )
  { 
    super(timer);

    if(env == null) 
      throw new IllegalArgumentException("The environment cannot be (null)!");
    pEnvironment = env;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"MasterMgr.getToolsetEnvironment(): " + name + "\n" + 
		    "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the cooked toolset environment.
   */
  public TreeMap<String,String> 
  getEnvironment() 
  {
    return pEnvironment;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3240033242663935494L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The cooked toolset environment.
   */ 
  private TreeMap<String,String>  pEnvironment;

}
  
