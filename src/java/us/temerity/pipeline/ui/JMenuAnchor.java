// $Id: JMenuAnchor.java,v 1.3 2004/04/30 11:24:07 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   M E N U   A N C H O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A iconic anchor which shows a popup menu when pressed.
 */ 
public 
class JMenuAnchor
  extends JLabel
  implements MouseListener, PopupMenuListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a popup menu anchor. 
   */
  JMenuAnchor
  (
   JManagerPanel mgr
  )
  {
    super();

    if(mgr == null) 
      throw new IllegalArgumentException("The manager panel cannot be (null)!");
    pMgrPanel = mgr;

    setIcon(sNormalIcon);

    Dimension size = new Dimension(14, 19);
    setMinimumSize(size);
    setMaximumSize(size);
    setPreferredSize(size);

    addMouseListener(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTNER METHODS ---------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked
  (
   MouseEvent e
  )
  {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered
  (
   MouseEvent e
  )
  {}
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  )
  {}
  
  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    setIcon(sPressedIcon);
    pMgrPanel.showPopup(e);
  }
  
  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased
  (
   MouseEvent e
  )
  {}


  /*-- POPUP MENU LISTENER METHODS ---------------------------------------------------------*/

  /**
   * This method is called when the popup menu is canceled.
   */ 
  public void 	
  popupMenuCanceled
  (
   PopupMenuEvent e
  )
  {}
         
  /**
   * This method is called before the popup menu becomes invisible. <P> 
   * 
   * Note that a JPopupMenu can become invisible any time.
   */ 
  public void 	
  popupMenuWillBecomeInvisible
  (
   PopupMenuEvent e
  )
  {
    setIcon(sNormalIcon);
  }

  /**
   * This method is called before the popup menu becomes visible.
   */ 
  public void
  popupMenuWillBecomeVisible
  (
   PopupMenuEvent e
  )
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8272128881241954656L;


  static private Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("MenuAnchorIcon.png"));

  static private Icon sPressedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("MenuAnchorPressedIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent manager panel.
   */ 
  private JManagerPanel  pMgrPanel;

}
