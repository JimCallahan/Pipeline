// $Id: QueueGetNamesRsp.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message;

import java.util.TreeSet;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.TaskTimer;

/*---------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   Q U E U E   C O N T R O L   N A M E S   R S P                         */
/*---------------------------------------------------------------------------------------------*/

/**
 * Get the names of all existing queue controls. 
 */
public
class QueueGetNamesRsp
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
   * @param names
   *   The names. 
   *   
   * @param controlName
   *   The name of the control, used for error reporting.
   */ 
  public
  QueueGetNamesRsp
  (
   TaskTimer timer, 
   TreeSet<String> names,
   String controlName
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The " + controlName + " names cannot be (null)!");
    pNames = names;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.get" + controlName + "():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the queue control names.
   */
  public TreeSet<String>
  getNames() 
  {
    return pNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3867183433750364027L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hardware group names. 
   */ 
  private TreeSet<String>  pNames;
}