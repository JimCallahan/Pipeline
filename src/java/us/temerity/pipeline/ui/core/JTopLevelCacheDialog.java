// $Id: JTopLevelCacheDialog.java,v 1.3 2010/01/07 22:14:34 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/**
 * An extension of {@link JTopLevelCacheDialog} that integrates caching  
 */
public 
class JTopLevelCacheDialog
  extends JTopLevelDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new top-level frame. 
   * 
   * @param title
   *   The title of the dialog window.
   */ 
  public 
  JTopLevelCacheDialog
  (
    String title
  )
  {
    super(title);
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
    UIMaster.getInstance().getUICache(pChannel).invalidateCaches();
  }
  
  /**
   * Shortcut method for updating the toolset part of the UI Cache for this dialog.
   */
  protected void
  updateToolsetInfo()
    throws PipelineException
  {
    UIMaster.getInstance().getUICache(pChannel).updateToolsetInformation();
  }
  
  /**
   * Get the UI Cache associated with this dialog.
   */
  protected UICache
  getUICache()
  {
    return UIMaster.getInstance().getUICache(pChannel);
  }
  
  /**
   * Get the UI Cache channel being used by this dialog.
   * @return
   */
  protected int
  getChannel()
  {
    return pChannel;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8705507476966192285L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private int pChannel;
}
