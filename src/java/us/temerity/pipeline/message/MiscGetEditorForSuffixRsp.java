// $Id: MiscGetEditorForSuffixRsp.java,v 1.3 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   S U F F I X   E D I T O R S   R S P                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetEditorForSuffixReq MiscGetEditorForSuffixReq} 
 * request.
 */
public
class MiscGetEditorForSuffixRsp
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
   * @param editor
   *   The editor name.
   */ 
  public
  MiscGetEditorForSuffixRsp
  (
   TaskTimer timer, 
   String editor
  )
  { 
    super(timer);

    pEditor = editor;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"MasterMgr.getEditorForSuffix():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the editor name.
   */
  public String
  getEditor() 
  {
    return pEditor;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1549480439983487721L;


  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The editor name.
   */ 
  private String  pEditor;

}
  
