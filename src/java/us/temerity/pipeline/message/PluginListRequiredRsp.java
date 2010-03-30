// $Id: PluginListRequiredRsp.java,v 1.1 2009/02/11 16:32:39 jlee Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   L I S T   R E Q U I R E D   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a PluginRequest.ListRequired.
 */
public
class PluginListRequiredRsp
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
   * @param requiredPlugins
   *   The table of required plugins that need to be installed in order for plplugmgr to 
   *   accept connections from plmaster and plqueuemgr.
   *
   * @param unknownPlugins
   *   The table of unregistered plugins detected by plpluginmgr at startup.
   */
  public
  PluginListRequiredRsp
  (
   TaskTimer timer, 
   MappedSet<PluginType,PluginID> requiredPlugins, 
   MappedSet<PluginType,PluginID> unknownPlugins
  )
  { 
    super(timer);

    pRequiredPlugins = requiredPlugins;
    pUnknownPlugins  = unknownPlugins;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the table of required plugins.
   */
  public MappedSet<PluginType,PluginID>
  getRequiredPlugins()
  {
    return pRequiredPlugins;
  }

  /**
   * Gets the table of unregistered plugins.
   */
  public MappedSet<PluginType,PluginID>
  getUnknownPlugins()
  {
    return pUnknownPlugins;
  }



  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3226865294894943922L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of required plugins.
   */
  private MappedSet<PluginType,PluginID>  pRequiredPlugins;

  /**
   * The table of unregistered plugins.
   */
  private MappedSet<PluginType,PluginID>  pUnknownPlugins;
}

