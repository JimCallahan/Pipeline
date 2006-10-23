// $Id: MiscGetPluginMenuLayoutsRsp.java,v 1.1 2006/10/23 11:30:20 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P L U G I N   M E N U   L A Y O U T S   R S P                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the layout plugin menus for all plugin types associated with a toolset.
 */
public
class MiscGetPluginMenuLayoutsRsp
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
   * @param layout
   *   The heirarchical set of editor plugin menus indexed by plugin type.
   */ 
  public
  MiscGetPluginMenuLayoutsRsp
  (
   TaskTimer timer, 
   TreeMap<PluginType,PluginMenuLayout> layouts
  )
  { 
    super(timer);

    if(layouts == null) 
      throw new IllegalArgumentException("The plugin menu layouts cannot be (null)!");
    pLayouts = layouts;
    
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
   * Gets the heirarchical set of editor plugin menus indexed by plugin type.
   */
  public TreeMap<PluginType,PluginMenuLayout>
  getLayouts() 
  {
    return pLayouts;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -373332227743249616L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The heirarchical set of editor plugin menus indexed by plugin type.
   */ 
  private TreeMap<PluginType,PluginMenuLayout>  pLayouts;

}
  
