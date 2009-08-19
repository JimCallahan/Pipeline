// $Id: MiscGetPluginMenuLayoutRsp.java,v 1.5 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P L U G I N   M E N U   L A Y O U T   R S P                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Get layout of a plugin selection menu associated with a toolset.
 */
public
class MiscGetPluginMenuLayoutRsp
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
   *   The heirarchical set of menus for selection of a specific plugin version.
   */ 
  public
  MiscGetPluginMenuLayoutRsp
  (
   TaskTimer timer, 
   PluginMenuLayout layout
  )
  { 
    super(timer);

    if(layout == null) 
      throw new IllegalArgumentException("The plugin menu layuout cannot be (null)!");
    pLayout = layout;
    
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the heirarchical set of menus for selection of a specific plugin version.
   */
  public PluginMenuLayout
  getLayout() 
  {
    return pLayout;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5795847499681639558L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The heirarchical set of menus for selection of a specific plugin version.
   */ 
  private PluginMenuLayout  pLayout;

}
  
