// $Id: JTopLevelPanel.java,v 1.16 2009/03/25 19:31:58 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T O P   L E V E L   P A N E L                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class for all top level panels.
 */ 
public  
class JTopLevelPanel
  extends JPanel
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JTopLevelPanel()
  {
    super();

    pPrivilegeDetails = new PrivilegeDetails();
    pPanelOpInProgress = new AtomicBoolean(false);
    pUnsavedChanges   = new TreeSet<String>();

    setAuthorViewLocal(PackageInfo.sUser, "default");
    setGroupID(0);
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JTopLevelPanel
  (
    JTopLevelPanel panel
  )
  {
    super();

    pPrivilegeDetails = new PrivilegeDetails();
    pPanelOpInProgress = new AtomicBoolean(false);
    pUnsavedChanges   = new TreeSet<String>();

    if(panel != null) {
      setAuthorViewLocal(panel.getAuthor(), panel.getView());
      setGroupID(panel.getGroupID());
    }
    else {
      setAuthorViewLocal(PackageInfo.sUser, "default");
      setGroupID(0);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  public String 
  getTypeName() 
  {
    return "Unknown Type";
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the parent manager panel.
   */ 
  public void 
  setManager
  (
   JManagerPanel panel
  ) 
  {
    pManagerPanel = panel;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the top-level frame containing this panel.
   */ 
  public Frame
  getTopFrame() 
  {
    return pManagerPanel.getTopFrame(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the group ID. <P> 
   * 
   * @return 
   *   The group ID or (0) if not assigned to a group.
   */ 
  public int
  getGroupID()
  {
    return pGroupID;
  }

  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    setGroupIDLocal(groupID);
  }
  
  /**
   * Set the group ID. <P> 
   * 
   * A local method to be called in the constructor.
   * <p>
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  private void
  setGroupIDLocal
  (
   int groupID
  )
  {
    pGroupID = groupID;
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    return true;
  }
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   * 
   * Subclasses should override this method with a test against the pPrivilegeDetails.
   */ 
  public boolean
  isLocked() 
  {
    return (!PackageInfo.sUser.equals(pAuthor));
  }

  /**
   * Get the Author|View field text.
   */ 
  public String
  getTitle() 
  {
    return (pAuthor + " | " + pView);
  }

  /** 
   * Get the name of user which owns the working area view.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor; 
  }

  /** 
   * Get the name of the working area view.
   */
  public String
  getView()
  {
    return pView;
  }

  /**
   * Set the author and view.
   */ 
  public void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    setAuthorViewLocal(author, view);
  }

  /**
   * A local method to set the author and view which exists to be called in the constructor.
   */ 
  private void 
  setAuthorViewLocal
  (
   String author, 
   String view 
  ) 
  {
    if(warnUnsavedChangesBeforeAuthorView()) 
      return; 

    if(author == null)
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null)
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    updatePrivileges();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update the user privileges and title panel to reflect the changes.
   */ 
  public void 
  updatePrivileges() 
  {
    UIMaster master = UIMaster.getInstance();
    if (pGroupID > 0) {
      UICache cache = master.getUICache(pGroupID);
      try {
        pPrivilegeDetails = cache.getCachedPrivilegeDetails();
      }
      catch(PipelineException ex) {
        master.showErrorDialog(ex);
      }
    }
    else {
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        pPrivilegeDetails = client.getPrivilegeDetails();
      }
      catch(PipelineException ex) {
        master.showErrorDialog(ex);
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }

    updateManagerTitlePanel();
  }

  /**
   * Update the parent manager title panel to reflect the current state of this panel.
   */ 
  public void
  updateManagerTitlePanel() 
  {
    if(pManagerPanel != null) 
      pManagerPanel.updateTitlePanel();
  }

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {}

  /**
   * Update the tool tip for the given menu item.
   */   
  protected void 
  updateMenuToolTip
  (
   JMenuItem item, 
   HotKey key,
   String desc
  ) 
  {
    String text = null;
    if(UserPrefs.getInstance().getShowMenuToolTips()) {
      if(desc != null) {
	if(key != null) 
	  text = (desc + "<P>Hot Key = " + key);
	else 
	  text = desc;
      }
      else {
	text = ("Hot Key = " + key);
      }
    }
    
    if(text != null) 
      item.setToolTipText(UIFactory.formatToolTip(text));
    else 
      item.setToolTipText(null);
  }

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  public void 
  clearPluginCache()
  {}


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a panel operation is currently in progress.
   */ 
  public boolean
  isPanelOpInProgress() 
  {
    return pPanelOpInProgress.get();
  }

  /**
   * Perform any operations needed before an panel operation starts. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  prePanelOp() 
  {
    pPanelOpInProgress.set(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));  
  }

  /**
   * Perform any operations needed after an panel operation has completed. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  postPanelOp() 
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));  
    pPanelOpInProgress.set(false);
    pUnsavedChanges.clear();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether panel properties have been modified since the last update.
   */ 
  public boolean 
  hasUnsavedChanges() 
  {
    return (!pUnsavedChanges.isEmpty());
  }

  /**
   * The names of the panel properties which have been modified since the last panel update.
   */ 
  public SortedSet<String> 
  unsavedChanges()
  {
    return Collections.unmodifiableSortedSet(pUnsavedChanges);
  }

  /**
   * Register the name of a panel property which has just been modified.
   */ 
  public void
  unsavedChange
  (
   String name
  )
  {
    pUnsavedChanges.add(name);
  }

  /**
   * If unsaved panel modifications exist, display a dialog that warns the user about 
   * these unsaged changes and either apply or discard the changes based on the users 
   * input. 
   * 
   * @return 
   *   Whether previously unsaved changes where applied.
   */ 
  public boolean
  warnUnsavedChanges
  (
   String msg
  )
  {
    UserPrefs prefs = UserPrefs.getInstance();
    if(!hasUnsavedChanges() || !prefs.getWarnUnsavedChanges()) 
      return false;

    UnsavedChangesQueryTask task = new UnsavedChangesQueryTask(this, msg);
    if(SwingUtilities.isEventDispatchThread()) {
      task.run();
    }
    else {
      try {
        SwingUtilities.invokeAndWait(task);
      }
      catch(Exception ex) {
      }
    }
    
    if(!task.wasConfirmed()) 
      return false;
    
    doApply();
    return true;
  }

  /**
   * Warn about unsaved changes prior to a panel update.
   * 
   * @return 
   *   Whether previously unsaved changes where applied.
   */ 
  public boolean
  warnUnsavedChangesBeforeUpdate()   
  {
    String msg = 
      ("The " + getTypeName() + " panel on channel (" + getGroupID() + ") contains " + 
       "unsaved changes which will be lost during the requested panel update!"); 

    return warnUnsavedChanges(msg);
  }
    
  /**
   * Warn about unsaved changes prior to an panel operation.
   * 
   * @return 
   *   Whether previously unsaved changes where applied.
   */ 
  public boolean 
  warnUnsavedChangesBeforeOp
  (
   String opname
  ) 
  {
    String msg = 
      ("The " + getTypeName() + " panel on channel (" + getGroupID() + ") contains " + 
       "unsaved changes which will be lost during the requested " + opname + " operation!"); 

    return warnUnsavedChanges(msg);
  }

  /**
   * Warn about unsaved changes prior to closing a panel.
   * 
   * @return 
   *   Whether previously unsaved changes where applied.
   */ 
  public boolean
  warnUnsavedChangesBeforeClose()   
  {
    String msg = 
      ("The " + getTypeName() + " panel on channel (" + getGroupID() + ") contains " + 
       "unsaved changes which will be lost when the panel is closed!"); 

    return warnUnsavedChanges(msg);
  }
    
  /**
   * Warn about unsaved changes prior to replacing the current panel with a panel
   * of a different type.
   * 
   * @return 
   *   Whether previously unsaved changes where applied.
   */ 
  public boolean
  warnUnsavedChangesBeforeReplace()   
  {
    String msg = 
      ("The " + getTypeName() + " panel on channel (" + getGroupID() + ") contains " + 
       "unsaved changes which will be lost when the panel type is changed!"); 

    return warnUnsavedChanges(msg);
  }
    
  /**
   * Warn about unsaved changes prior to a change of working area view. 
   * 
   * @return 
   *   Whether previously unsaved changes where applied.
   */ 
  public boolean
  warnUnsavedChangesBeforeAuthorView()   
  {
    String msg = 
      ("The " + getTypeName() + " panel on channel (" + getGroupID() + ") contains " + 
       "unsaved changes which will be lost when switching to another working area view!"); 

    return warnUnsavedChanges(msg);
  }
    


  /*----------------------------------------------------------------------------------------*/

  /**
   * Refocus keyboard events on this panel if it contains the mouse.
   * 
   * @return
   *   Whether the panel has received the focus.
   */ 
  public boolean 
  refocusOnPanel() 
  {
    if(getMousePosition(true) != null) {
      requestFocusInWindow();
      return true;
    }

    return false;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Return the previously allocated OpenGL display lists to the pool of display lists to be 
   * reused. 
   */ 
  public void 
  freeDisplayLists() 
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply panel changes.
   */ 
  public void 
  doApply()
  {
    pUnsavedChanges.clear();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    encoder.encode("GroupID", pGroupID);
    encoder.encode("Author", pAuthor);
    encoder.encode("View", pView);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Integer groupID = (Integer) decoder.decode("GroupID");
    if(groupID == null) 
      throw new GlueException("The \"GroupID\" was missing or (null)!");
    setGroupID(groupID);

    String author = (String) decoder.decode("Author");
    if(author == null) 
      throw new GlueException("The \"GroupID\" was missing or (null)!");

    String view = (String) decoder.decode("View");
    if(view == null) 
      throw new GlueException("The \"View\" was missing or (null)!");

    setAuthorView(author, view);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Show the dialog querying the user about unsaved changes.
   */ 
  private
  class UnsavedChangesQueryTask
    extends Thread
  {
    public 
    UnsavedChangesQueryTask
    (
     JTopLevelPanel panel,
     String msg
    ) 
    {
      super("JTopLevelPanel:UnsavedChangesQueryTask"); 

      pParentPanel = panel; 
      pMessage     = msg; 
    }
    
    public boolean 
    wasConfirmed() 
    {
      return pWasConfirmed;
    }

    @Override
    public void 
    run() 
    {  
      JUnsavedChangesDialog diag = new JUnsavedChangesDialog(pParentPanel, pMessage);
      diag.setVisible(true);
      
      pUnsavedChanges.clear();
      
      pWasConfirmed = diag.wasConfirmed(); 
    }
 
    private JTopLevelPanel pParentPanel; 
    private String         pMessage; 
    private boolean        pWasConfirmed;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5078973600371515267L;



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent manager panel.
   */ 
  protected JManagerPanel  pManagerPanel; 


  /**
   * The group ID of this panel or (0) if unassigned.
   */ 
  protected int pGroupID;



  /** 
   * The name of user which owns the working area view associated with this panel.
   */
  protected String  pAuthor;

  /** 
   * The name of the working area view associated with this panel.
   */
  protected String  pView;


  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  protected PrivilegeDetails  pPrivilegeDetails; 


  /**
   * Whether a panel update is currently in progress.
   */ 
  private AtomicBoolean  pPanelOpInProgress; 

  /**
   * The names of panel properties which have been modified since the last panel update.
   */ 
  private TreeSet<String>  pUnsavedChanges; 

}
