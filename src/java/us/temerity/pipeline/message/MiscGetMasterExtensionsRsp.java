// $Id: MiscGetMasterExtensionsRsp.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   M A S T E R   E X T E N S I O N S   R S P                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the current master extension configurations.
 */
public
class MiscGetMasterExtensionsRsp 
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
   * @param extensions
   *   The extension configurations indexed by configuration name.
   */ 
  public
  MiscGetMasterExtensionsRsp
  (
   TaskTimer timer, 
   TreeMap<String,MasterExtensionConfig> extensions
  )
  { 
    super(timer);

    if(extensions == null) 
      throw new IllegalArgumentException("The extensions cannot be (null)!");
    pExtensions = extensions;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getMasterExtensions():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the extension configurations indexed by configuration name.
   */
  public TreeMap<String,MasterExtensionConfig>
  getExtensions() 
  {
    return pExtensions;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4611750029416155521L;


  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The extension configurations indexed by configuration name.
   */ 
  private TreeMap<String,MasterExtensionConfig>  pExtensions;

}
  
