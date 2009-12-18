// $Id: UIClient.java,v 1.1 2009/12/18 08:44:26 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.util.*;


/*------------------------------------------------------------------------------------------*/
/*   U I   C L I E N T                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * An interface for managing the top-level windows of plui(1) containing node, queue and job
 * related panels.
 */ 
public 
interface UIClient
{
  /*----------------------------------------------------------------------------------------*/
  /*   G L O B A L   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Find the top-level window (if any) which cotains the given type of panel on a particular 
   * update channel.
   * 
   * @param ptype
   *   The type of panel. 
   * 
   * @param channel
   *   The identifier of the update channel [1,9] which contains the panel.
   * 
   * @return
   *   The matching Panel Frame or <CODE>null</CODE> if none matches.
   */ 
  public FrameProxy
  getFrameContaining
  (
   PanelType ptype, 
   int channel
  )
    throws PipelineException;

  /**
   * Find the Manager Panel (if any) which contains the given type of panel on a particular 
   * update channel.
   * 
   * @param ptype
   *   The type of panel. 
   * 
   * @param channel
   *   The identifier of the update channel [1,9] used by the panel.
   * 
   * @return
   *   The matching Manager Panel or <CODE>null</CODE> if none matches.
   */ 
  public ManagerPanelProxy
  getManagerContaining
  (
   PanelType ptype, 
   int channel
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initiate an update from the server of the contents of all panels which share the 
   * given update channel. 
   * 
   * @param channel
   *   The identifier of the update channel [1,9] used by the panels.
   * 
   * @param lightweight 
   *   Whether perform lightweight node status (true) or heavyweight node status (false). 
   *   Ignored if a Node Viewer panel is not a member of the update channel.
   */ 
  public void
  update
  (
   int channel,
   boolean lightweight
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Direct plui(1) to close all network connections and exit.
   */ 
  public void 
  quit();        
  


  /*----------------------------------------------------------------------------------------*/
  /*   W I N D O W S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the client interface for the main top-level panel window. 
   */ 
  public FrameProxy
  getMainFrame(); 

  /**
   * Get the client interfaces for all top-level panel windows. 
   */ 
  public List<FrameProxy>
  getFrames(); 
 

  /**
   * Create a new top-level window containing a single panel.
   * 
   * @param ptype
   *   The type of panel which the new window should contain.
   * 
   * @param channel
   *   The identifier of the update channel [0,9] the panel should use. 
   *   A value of (0) can be used to disable updates.
   */
  public FrameProxy
  createFrame
  (
   PanelType ptype, 
   int channel
  )
    throws PipelineException;

  

  /*----------------------------------------------------------------------------------------*/
  /*    L A Y O U T S                                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the identifiers of all saved panel layout.
   * 
   * @return 
   *   The layout identifiers. 
   */ 
  public SortedSet<Path>
  getSavedLayoutPaths();


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the identifier of the current panel layout.
   * 
   * @return 
   *   The layout identifier or <CODE>null</CODE> if unset.
   */ 
  public Path
  getLayoutPath();

  /**
   * Set the identifier of the current panel layout.
   */ 
  public void 
  setLayoutPath
  (
   Path path
  );


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the identifier of the current default panel layout.
   * 
   * @return 
   *   The layout identifier or <CODE>null</CODE> if unset.
   */ 
  public Path
  getDefaultLayoutPath();
  
  /**
   * Select an existing panel layout to be the default.
   * 
   * @param path
   *   The identifier of the layout to restore.
   */ 
  public void 
  setDefaultLayoutPath
  (
   Path path
  );


  /*----------------------------------------------------------------------------------------*/

  /**
   * Save the current panel layout.<P> 
   * 
   * If the current layout is has been named, it will be saved directly.  Otherwise a dialog
   * will be displayed allowing the user to choose a name for the layout.
   */
  public void 
  saveLayout();   

  /**
   * Replace the current panels with those stored in the stored layout with the given name.
   * 
   * @param path
   *   The identifier of the layout to restore.
   * 
   * @param restoreSelections
   *   Whether to restore the selections within panels at the time they were saved.
   */
  public void 
  restoreSavedLayout   
  (
   Path path, 
   boolean restoreSelections
  );


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make the current layout the default layout.
   */
  public void 
  defaultLayout();     
  
  /**
   * Make the given layout the default layout.
   *
   * @param path
   *   The identifier of the layout.
   */
  public void 
  defaultLayout      
  (
   Path path
  );
   
  /**
   * Reset the current layout to a standardized panel layout.
   */
  public void 
  resetLayout();   

  


  /*----------------------------------------------------------------------------------------*/
  /*   M I S C                                                                              */
  /*----------------------------------------------------------------------------------------*/

}
