// $Id: MiscGetPackagePluginsRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P A C K A G E   P L U G I N S   R S P                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the plugins associated with a toolset package.
 */
public
class MiscGetPackagePluginsRsp
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
   * @param plugins
   *   The vendors, names and revision numbers of the associated plugins.
   */ 
  public
  MiscGetPackagePluginsRsp
  (
   TaskTimer timer, 
   PluginSet plugins
  )
  { 
    super(timer);

    if(plugins == null) 
      throw new IllegalArgumentException("The associated plugins cannot be (null)!");
    pPlugins = plugins;
    
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets vendors, names and revision numbers of the associated plugins 
   * or <CODE>null</CODE> if none exist.
   */
  public PluginSet
  getPlugins() 
  {
    return pPlugins;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7867696279645229500L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The vendors, names and revision numbers of the associated plugins.
   */ 
  private PluginSet  pPlugins;

}
  
