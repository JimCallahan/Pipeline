// $Id: MiscArchiveRsp.java,v 1.2 2005/03/10 08:07:27 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A R C H I V E   R S P                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscArchiveReq MiscArchiveReq} 
 * request.
 */
public
class MiscArchiveRsp
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
   * @param name
   *   The unique name given to the newly created archive. 
   */ 
  public
  MiscArchiveRsp
  (
   TaskTimer timer, 
   String name
  )
  { 
    super(timer);

    if(name == null) 
      throw new IllegalArgumentException("The archive name cannot be (null)!");
    pName = name; 

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.archive(): " + name + "\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique name given to the newly created archive. 
   */
  public String
  getName()
  {
    return pName; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8688166869973310014L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique name given to the newly created archive. 
   */ 
  private String  pName; 

}
  
