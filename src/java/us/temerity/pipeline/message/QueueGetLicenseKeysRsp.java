// $Id: QueueGetLicenseKeysRsp.java,v 1.1 2004/07/24 18:28:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   L I C E N S E   K E Y S   R S P                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the currently defined license keys. 
 */
public
class QueueGetLicenseKeysRsp
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
   *   The license key names
   */ 
  public
  QueueGetLicenseKeysRsp
  (
   TaskTimer timer, 
   ArrayList<LicenseKey> keys
  )
  { 
    super(timer);

    if(keys == null) 
      throw new IllegalArgumentException("The license keys cannot be (null)!");
    pKeys = keys;

    Logs.net.finest("QueueMgr.getLicenseKeys():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the license keys.
   */
  public ArrayList<LicenseKey>
  getKeys() 
  {
    return pKeys;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7711452537599078957L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The license keys.
   */ 
  private ArrayList<LicenseKey>  pKeys;

}
  
