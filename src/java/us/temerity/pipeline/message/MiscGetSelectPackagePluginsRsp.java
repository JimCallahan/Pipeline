// $Id: MiscGetSelectPackagePluginsRsp.java,v 1.1 2006/10/23 11:30:20 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   S E L E C T   P A C K A G E   P L U G I N S   R S P                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Get all types of plugins associated with the given packages.
 */
public
class MiscGetSelectPackagePluginsRsp
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
   *   The vendors, names and revision numbers of the associated plugins indexed by
   *   the package names and revision numbers.
   */ 
  public
  MiscGetSelectPackagePluginsRsp
  (
   TaskTimer timer, 
   TripleMap<String,VersionID,PluginType,PluginSet> plugins
  )
  { 
    super(timer);

    if(plugins == null) 
      throw new IllegalArgumentException("The associated plugins cannot be (null)!");
    pPlugins = plugins;
    
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the vendors, names and revision numbers of the associated plugins indexed by
   * the package names and revision numbers.
   */
  public TripleMap<String,VersionID,PluginType,PluginSet>
  getPlugins() 
  {
    return pPlugins;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4239247427443660544L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The vendors, names and revision numbers of the associated plugins indexed by
   * the package names and revision numbers.
   */ 
  private TripleMap<String,VersionID,PluginType,PluginSet>  pPlugins;

}
  
