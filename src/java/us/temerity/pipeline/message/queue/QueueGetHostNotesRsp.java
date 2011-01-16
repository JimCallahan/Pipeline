// $Id: QueueGetNamesRsp.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message.queue;

import java.util.TreeMap;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.TaskTimer;
import us.temerity.pipeline.SimpleLogMessage;
import us.temerity.pipeline.message.*;

/*-------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   N O T E S   R S P                                         */
/*-------------------------------------------------------------------------------------------*/

/**
 * Get all of the notes (if any) associated with the given host.   
 */
public
class QueueGetHostNotesRsp
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
   * @param notes
   *   The host notes. 
   */ 
  public
  QueueGetHostNotesRsp
  (
   TaskTimer timer, 
   TreeMap<Long,SimpleLogMessage> notes
  )
  { 
    super(timer);

    pHostNotes = notes;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHostNotes():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the host notes. 
   */
  public TreeMap<Long,SimpleLogMessage>
  getHostNotes() 
  {
    return pHostNotes;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -481942261122160174L;


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The host notes indexed by timestamp.
   */ 
  private TreeMap<Long,SimpleLogMessage>  pHostNotes;

}
