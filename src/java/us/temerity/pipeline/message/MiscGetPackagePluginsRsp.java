// $Id: MiscGetPackagePluginsRsp.java,v 1.2 2005/09/07 21:11:16 jim Exp $

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
   DoubleMap<String,String,TreeSet<VersionID>> plugins
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
   * Gets vendors, names and revision numbers of the associated plugins 
   * or <CODE>null</CODE> if none exist.
   */
  public DoubleMap<String,String,TreeSet<VersionID>>
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
  private DoubleMap<String,String,TreeSet<VersionID>>  pPlugins;

}
  
