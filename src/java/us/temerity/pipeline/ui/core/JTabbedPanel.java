// $Id: JTabbedPanel.java,v 1.2 2005/06/14 13:38:33 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.ui.JNewNameDialog;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;


/*------------------------------------------------------------------------------------------*/
/*   T A B B E D   P A N N E L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A <CODE>JTabbedPane</CODE> which implements {@link Glueable Glueable}.
 */ 
public 
class JTabbedPanel
  extends JTabbedPane
  implements Glueable, MouseListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  JTabbedPanel()
  {
    super();

    pPopup = new JPopupMenu();  
    {
      JMenuItem item; 

      item = new JMenuItem("Rename...");
      item.setActionCommand("rename");
      item.addActionListener(this);
      pPopup.add(item);

      item = new JMenuItem("Reset");
      item.setActionCommand("reset");
      item.addActionListener(this);
      pPopup.add(item);
    }
      
    addMouseListener(this); 
  }


  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given manager panel as a tab.
   */ 
  public void 
  addTab
  (
   JManagerPanel mgr
  ) 
  {
    addTab(mgr.getTitle(), null, mgr);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    requestFocusInWindow();
  }
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  ) 
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
  }

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    pRenameMgrPanel = null;

    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON3:
      {
        int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
        int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
                    MouseEvent.BUTTON2_DOWN_MASK | 
                    MouseEvent.SHIFT_DOWN_MASK |
                    MouseEvent.ALT_DOWN_MASK |
                    MouseEvent.CTRL_DOWN_MASK);

        if((mods & (on1 | off1)) == on1) {
          int idx = indexAtLocation(e.getX(), e.getY());
          if(idx != -1) {
            Component comp = getComponentAt(idx); 
            if(comp instanceof JManagerPanel) {
              pRenameMgrPanel = (JManagerPanel) comp;
              pPopup.show(e.getComponent(), e.getX(), e.getY());
            }
          }
        }
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}



  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("rename")) {
      if(pRenameMgrPanel != null) {
        JNewNameDialog diag = new JNewNameDialog(pRenameMgrPanel.getPanelFrame(), 
                                                 "Rename Tab", "Tab Title", null, "Rename"); 
        diag.setVisible(true);
        if(diag.wasConfirmed()) 
          pRenameMgrPanel.setTitle(diag.getName());
      }
    }
    else if(cmd.equals("reset")) {
      if(pRenameMgrPanel != null) 
        pRenameMgrPanel.resetTitle(); 
    }
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
    LinkedList<JManagerPanel> tabs = new LinkedList<JManagerPanel>();
    int wk;
    for(wk=0; wk<getTabCount(); wk++) 
      tabs.add((JManagerPanel) getComponentAt(wk));
    encoder.encode("Tabs", tabs);

    encoder.encode("SelectedIndex", getSelectedIndex());
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    LinkedList<JManagerPanel> tabs = (LinkedList<JManagerPanel>) decoder.decode("Tabs");
    if(tabs == null) 
      throw new GlueException("The \"Tabs\" was missing or (null)!");
    for(JManagerPanel mgr : tabs) 
      addTab(mgr);

    Integer idx = (Integer) decoder.decode("SelectedIndex");
    if(idx == null) 
      throw new GlueException("The \"SelectedIndex\" was missing or (null)!");
    setSelectedIndex(idx);    
  }
  
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1073333511291732408L;


  private static final Icon sTabIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The tab naming popup menu.
   */ 
  private JPopupMenu  pPopup; 

  /**
   * The manager panel to rename.
   */ 
  private JManagerPanel pRenameMgrPanel; 

}
