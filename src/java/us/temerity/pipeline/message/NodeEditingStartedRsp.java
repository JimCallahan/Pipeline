// $Id: NodeEditingStartedRsp.java,v 1.1 2006/12/31 20:44:53 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E D I T I N G   S T A R T E D   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeEditingStartedReq} request.
 */
public
class NodeEditingStartedRsp
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
   * @param editID 
   *   The unique ID for the editing session.
   */
  public
  NodeEditingStartedRsp
  (
   TaskTimer timer, 
   Long editID
  )
  { 
    super(timer);

    if(editID == null) 
      throw new IllegalArgumentException("The editing session ID cannot be (null)!");
    pEditID = editID;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.editingStarted():\n" + 
       "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the unique ID for the editing session.
   */
  public Long
  getEditID() 
  {
    return pEditID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2920402339050409128L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique ID for the editing session.
   */ 
  private Long  pEditID;

}
  
