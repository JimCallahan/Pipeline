// $Id: JTopLevelPanel.java,v 1.3 2004/05/07 21:05:47 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

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

    setAuthorView(PackageInfo.sUser, "default");
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

    if(panel != null) {
      setAuthorView(panel.getAuthor(), panel.getView());
      setGroupID(panel.getGroupID());
    }
    else {
      setAuthorView(PackageInfo.sUser, "default");
      setGroupID(0);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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
   * Is the contents of the panel read-only.
   */ 
  public boolean
  isLocked() 
  {
    return pIsLocked;
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

    pIsLocked = !pAuthor.equals(PackageInfo.sUser);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update the parent manager title panel to reflect the current state of this panel.
   */ 
  public void
  updateManagerTitlePanel() 
  {
    if(pManagerPanel != null) 
      pManagerPanel.updateTitlePanel();
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
   * Whether the contents of the panel is read-only.
   */   
  protected boolean  pIsLocked;


}
