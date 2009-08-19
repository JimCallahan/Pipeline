// $Id: QueueGetQueueExtensionsRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   M A S T E R   E X T E N S I O N S   R S P                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the current queue extension configurations.
 */
public
class QueueGetQueueExtensionsRsp 
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
  QueueGetQueueExtensionsRsp
  (
   TaskTimer timer, 
   TreeMap<String,QueueExtensionConfig> extensions
  )
  { 
    super(timer);

    if(extensions == null) 
      throw new IllegalArgumentException("The extensions cannot be (null)!");
    pExtensions = extensions;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getQueueExtensions():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the extension configurations indexed by configuration name.
   */
  public TreeMap<String,QueueExtensionConfig>
  getExtensions() 
  {
    return pExtensions;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6974061788802671523L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The extension configurations indexed by configuration name.
   */ 
  private TreeMap<String,QueueExtensionConfig>  pExtensions;

}
  
