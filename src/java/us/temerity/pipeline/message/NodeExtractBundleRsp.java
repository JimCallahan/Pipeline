// $Id: NodeExtractBundleRsp.java,v 1.1 2007/10/23 02:29:58 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E X T R A C T   B U N D L E   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeExtractBundleReq} request.
 */
public
class NodeExtractBundleRsp
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
   * @param bundle
   *   The node metadata contained in the node bundle. 
   * 
   * @param bundlePath
   *   The abstract file system path to the node bundle.
   */
  public
  NodeExtractBundleRsp
  (
   TaskTimer timer, 
   NodeBundle bundle,
   Path bundlePath
  )
  { 
    super(timer);

    if(bundle == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pBundle = bundle;

    if(bundlePath == null) 
      throw new IllegalArgumentException
        ("The path to the node bundle cannot be (null)!");
    pPath = bundlePath;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.extractBundle(): " + bundle.getRootNodeID() + "(" + bundlePath + "):" + 
       "\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the node metadata contained in the node bundle. 
   */
  public NodeBundle
  getBundle() 
  {
    return pBundle; 
  }
  
  /**
   * Gets the abstract file system path to the node bundle.
   */
  public Path
  getPath() 
  {
    return pPath;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7357707546910828395L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The node metadata contained in the node bundle. 
   */ 
  private NodeBundle  pBundle; 

  /**
   * The abstract file system path to the node bundle.
   */
  private Path  pPath; 

}
  
