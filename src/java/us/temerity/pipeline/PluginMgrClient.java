// $Id: PluginMgrClient.java,v 1.6 2008/05/10 03:17:35 jesse Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.net.*; 
import java.io.*; 
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M G R   C L I E N T                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A connection to the Pipeline plugin manager daemon. <P> 
 * 
 * This class handles network communication with the Pipeline plugin manager daemon 
 * <A HREF="../../../../man/plpluginmgr.html"><B>plpluginmgr</B><A>(1). 
 * <p>
 * As opposed to the other Pipeline client classes, the PluginMgrClient is not initialized with
 * a constructor.  Instead, an instance is retrieved using the {@link #getInstance()} method,
 * which returns a connection to the server.  Attempting to make a connecting using 
 * {@link #PluginMgrClient()} will not succeed.
 * <p>
 * In stand alone Pipeline applications which need to deal with plugins, it is necessary to 
 * call the {@link #init()} method before any method calls that manipulate any data structures
 * that use plugins.  This could be something as apparently unconnected as getting a Working
 * or Checked-In Version of a node or retrieving information about a job.  Attempting to make
 * a call without initializing the plugin manager first will result in an exception being
 * thrown. 
 */
public
class PluginMgrClient
  extends BasePluginMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   **/
  protected 
  PluginMgrClient() 
  {
    super();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the plugin manager instance. 
   */
  public static void 
  init() 
    throws PipelineException
  {
    init(false);
  }

  /**
   * Initialize the plugin manager instance. 
   * 
   * @param failFast
   *   Whether to abort immediately if unable to connect to the plugin manager.
   */
  public static void 
  init
  (
   boolean failFast
  ) 
    throws PipelineException
  {
    if(sPluginMgrClient != null)
      throw new PipelineException("PluginMgrClient has already been initialized!");

    sPluginMgrClient = new PluginMgrClient();
    
    if(failFast) 
      sPluginMgrClient.verifyConnection();
    else 
      sPluginMgrClient.waitForConnection(100, 5000);

    sPluginMgrClient.update();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the PluginMgrClient instance.
   */ 
  public static PluginMgrClient
  getInstance() 
  {
    return sPluginMgrClient;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance.
   */ 
  private static PluginMgrClient  sPluginMgrClient = null;

}


