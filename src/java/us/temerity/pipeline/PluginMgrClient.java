// $Id: PluginMgrClient.java,v 1.1 2005/01/15 02:56:32 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.net.*; 
import java.io.*; 
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M G R   C L I E N T                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A connection to the Pipeline plugin manager daemon. <P> 
 * 
 * This class handles network communication with the Pipeline plugin manager daemon 
 * <A HREF="../../../../man/plpluginmgr.html"><B>plpluginmgr</B><A>(1).  
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
    assert(sPluginMgrClient == null);
    sPluginMgrClient = new PluginMgrClient();
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


