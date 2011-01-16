// $Id: QueueGetNamesRsp.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message.queue;

import java.util.TreeSet;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.TaskTimer;
import us.temerity.pipeline.SimpleLogMessage;
import us.temerity.pipeline.message.*;

/*-------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   N O T E   R S P                                           */
/*-------------------------------------------------------------------------------------------*/

/**
 * Get the note (if any) associated with the given host.   
 */
public
class QueueGetHostNoteRsp
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
   * @param note
   *   The host note or <CODE>null</CODE> if none exists.
   */ 
  public
  QueueGetHostNoteRsp
  (
   TaskTimer timer, 
   SimpleLogMessage note
  )
  { 
    super(timer);

    pHostNote = note;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHostNote():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the host note or <CODE>null</CODE> if none exists.  
   */
  public SimpleLogMessage
  getHostNote() 
  {
    return pHostNote;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7724095495878196885L;


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The host note or <CODE>null</CODE> if none exists.  
   */ 
  private SimpleLogMessage  pHostNote;

}
