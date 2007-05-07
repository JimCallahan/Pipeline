// $Id: JTopLevelPanel.java,v 1.8 2007/05/07 04:14:08 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;

import java.util.concurrent.atomic.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

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
      
    setAuthorView(PackageInfo.sUser, "default");
    setGroupID(0);

    pUpdateInProgress = new AtomicBoolean(false);
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

    if(panel != null) {
      setAuthorView(panel.getAuthor(), panel.getView());
      setGroupID(panel.getGroupID());
    }
    else {
      setAuthorView(PackageInfo.sUser, "default");
      setGroupID(0);
    }

    pUpdateInProgress = new AtomicBoolean(false);
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
    MasterMgrClient client = master.getMasterMgrClient(pGroupID);
    try {
      pPrivilegeDetails = client.getCachedPrivilegeDetails();
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
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
   * Whether a panel update is currently in progress.
   */ 
  public boolean
  isUpdateInProgress() 
  {
    return pUpdateInProgress.get();
  }

  /**
   * Perform any operations needed before an panel update starts. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  preUpdate() 
  {
    pUpdateInProgress.set(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  /**
   * Perform any operations needed after an panel update has completed. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  postUpdate() 
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));  
    pUpdateInProgress.set(false);
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
  private AtomicBoolean  pUpdateInProgress; 

}
