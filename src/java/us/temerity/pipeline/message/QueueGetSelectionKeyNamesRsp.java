// $Id: QueueGetSelectionKeyNamesRsp.java,v 1.1 2004/07/25 03:13:17 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   S E L E C T I O N   K E Y   N A M E S   R S P                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the names of the currently defined selection keys. 
 */
public
class QueueGetSelectionKeyNamesRsp
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
   *   The selection key names
   */ 
  public
  QueueGetSelectionKeyNamesRsp
  (
   TaskTimer timer, 
   TreeSet<String> names
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The selection key names cannot be (null)!");
    pKeyNames = names;

    Logs.net.finest("QueueMgr.getSelectionKeyNames():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the selection key names.
   */
  public TreeSet<String>
  getKeyNames() 
  {
    return pKeyNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -854318957164973902L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection key names.
   */ 
  private TreeSet<String>  pKeyNames; 

}
  
