// $Id: QueueGetLicenseKeyNamesRsp.java,v 1.2 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   L I C E N S E   K E Y   N A M E S   R S P                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the names of the currently defined license keys. 
 */
public
class QueueGetLicenseKeyNamesRsp
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
  QueueGetLicenseKeyNamesRsp
  (
   TaskTimer timer, 
   TreeSet<String> names
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The license key names cannot be (null)!");
    pKeyNames = names;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"QueueMgr.getLicenseKeyNames():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the license key names.
   */
  public TreeSet<String>
  getKeyNames() 
  {
    return pKeyNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7054406944510148971L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The license key names.
   */ 
  private TreeSet<String>  pKeyNames; 

}
  
