// $Id: MiscCreateInitialPanelLayoutRsp.java,v 1.1 2006/07/02 07:48:55 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*    M I S C   C R E A T E   I N I T I A L   P A N E L   L A Y O U T   R S P               */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscCreateInitialPanelLayoutReq 
 * MiscCreateInitialPanelLayoutReq  } request.
 */
public
class MiscCreateInitialPanelLayoutRsp
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
   * @param contents
   *   The contents of the panel layout file.
   */ 
  public
  MiscCreateInitialPanelLayoutRsp
  (
   TaskTimer timer,
   String contents
  )
  { 
    super(timer);

    if(contents == null) 
      throw new IllegalArgumentException("The layout contents cannot be (null)!");
    pContents = contents;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.createInitialPanelLayout():\n" +
       "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the contents of the panel layout file.
   */ 
  public String
  getContents() 
  {
    return pContents; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -639927425247244578L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The contents of the panel layout file.
   */
  private String  pContents; 

}
  
