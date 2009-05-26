// $Id: JFullCacheDialog.java,v 1.2 2009/05/26 09:45:12 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.ui.*;

/**
 *  A version of {@link JFullCacheDialog} that has a unique channel that corresponds to a 
 *  cache saved in {@link UIMaster}.
 *  <p>
 *  This allows the dialog access to all the same caching mechanism that the normal UI
 *  channels have. 
 *  <p>
 *  Classes which extend this should take note of the invalid
 */
public 
class JFullCacheDialog
  extends JFullDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog owned by a top-level frame.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  public 
  JFullCacheDialog
  (
    Dialog owner,
    String title
  )
  {
    super(owner, title);
    pChannel = UIMaster.getInstance().registerUICache();
  }

  /**
   * Construct a new dialog owned by another dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  public 
  JFullCacheDialog
  (
    Frame owner,
    String title
  )
  {
    super(owner, title);
    pChannel = UIMaster.getInstance().registerUICache();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U I   C A C H E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Shortcut method for invalidating the UI Cache for this dialog.
   */
  protected void
  invalidateCaches()
  {
//    LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
//      "Invalidating UI Cache with ID (" + pChannel + ")");
    UIMaster.getInstance().getUICache(pChannel).invalidateCaches();
  }
  
  /**
   * Get the UI Cache associated with this dialog.
   */
  protected UICache
  getUICache()
  {
    return UIMaster.getInstance().getUICache(pChannel);
  }
  
  protected int
  getChannel()
  {
    return pChannel;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6024581639295135729L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private int pChannel;
}
