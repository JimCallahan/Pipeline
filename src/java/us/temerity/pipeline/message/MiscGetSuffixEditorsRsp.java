// $Id: MiscGetSuffixEditorsRsp.java,v 1.4 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   S U F F I X   E D I T O R S   R S P                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetSuffixEditorsReq MiscGetSuffixEditorsReq} request.
 */
public
class MiscGetSuffixEditorsRsp
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
   * @param editors
   *   The suffix editors.
   */ 
  public
  MiscGetSuffixEditorsRsp
  (
   TaskTimer timer, 
   TreeSet<SuffixEditor> editors
  )
  { 
    super(timer);

    if(editors == null) 
      throw new IllegalArgumentException("The suffix editors cannot be (null)!");
    pEditors = editors;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getSuffixEditors():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the suffix editors.
   */
  public TreeSet<SuffixEditor>
  getEditors() 
  {
    return pEditors;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5958072137494597511L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The suffix editors.
   */ 
  private TreeSet<SuffixEditor>  pEditors;

}
  
