// $Id: QueueGetNamesRsp.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message.queue;

import java.util.TreeSet;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.MappedSet;
import us.temerity.pipeline.TaskTimer;
import us.temerity.pipeline.message.*;

/*---------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T S   W I T H   N O T E S   R S P                               */
/*---------------------------------------------------------------------------------------------*/

/**
 * Get the names of the hosts for which there are current notes.
 */
public
class QueueGetHostsWithNotesRsp
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
   *   The set of timestamps for each note indexed by fully resolved host names.
   */ 
  public
  QueueGetHostsWithNotesRsp
  (
   TaskTimer timer, 
   MappedSet<String,Long> index
  )
  { 
    super(timer);

    if(index == null) 
      throw new IllegalArgumentException("The host names cannot be (null)!");
    pNoteIndex = index;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHostsWithNotes():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the set of timestamps for each note indexed by fully resolved host names.
   */
  public MappedSet<String,Long>
  getNoteIndex() 
  {
    return pNoteIndex;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8036967967082448023L;


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The set of timestamps for each note indexed by fully resolved host names.
   */ 
  private MappedSet<String,Long>  pNoteIndex;

}
