// $Id: JPanelFrame.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.BaseApp;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   P A N E L   F R A M E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A top level window which holds layouts of JManagerPanels. <P> 
 */ 
public 
class JPanelFrame
  extends JFrame
  implements WindowListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a manager panel. 
   */
  public 
  JPanelFrame()
  {
    super("plui");
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(this);

    {
      JPanel panel = new JPanel(new BorderLayout());
      pRootPanel = panel;
      panel.setName("RootPanel");
      
      {
	JManagerPanel mpanel = new JManagerPanel();
	mpanel.setContents(new JEmptyPanel());
	
	panel.add(mpanel);
      }
      
      setContentPane(panel);
    }

    setSize(520, 360);

    {
      Rectangle bounds = getGraphicsConfiguration().getBounds();
      setLocation(bounds.x + bounds.width/2 - getWidth()/2, 
		  bounds.y + bounds.height/2 - getHeight()/2);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the root manager panel.
   */ 
  public JManagerPanel
  getManagerPanel() 
  {
    return (JManagerPanel) pRootPanel.getComponent(0);
  }

  /**
   * Set the root manager panel.
   */ 
  public void 
  setManagerPanel
  (
   JManagerPanel panel
  ) 
  {
    pRootPanel.removeAll();        
    pRootPanel.add(panel);
    pRootPanel.validate();
    pRootPanel.repaint();
  }

  /**
   * Release any resources held by the existing panels.
   */ 
  public void 
  removePanels()
  {
    JManagerPanel mpanel = (JManagerPanel) pRootPanel.getComponent(0);

    pRootPanel.removeAll();

    if(mpanel != null) 
      mpanel.releasePanelGroups();

    mpanel = new JManagerPanel();
    mpanel.setContents(new JEmptyPanel());
  }


  /**
   * Get the name of the window.
   */ 
  public String
  getWindowName()
  {
    return pWindowName;
  }

  /** 
   * Set the name of the window.
   */ 
  public void 
  setWindowName
  (
   String name
  ) 
  {
    pWindowName = name;
    if(pWindowName != null) 
      setTitle("plui - " + pWindowName);
    else 
      setTitle("plui");
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- WINDOW LISTENER METHODS -------------------------------------------------------------*/

  /**
   * Invoked when the Window is set to be the active Window.
   */
  public void 
  windowActivated(WindowEvent e) {} 

  /**
   * Invoked when a window has been closed as the result of calling dispose on the window.
   */ 
  public void 	
  windowClosed(WindowEvent e) {} 

  /**
   * Invoked when the user attempts to close the window from the window's system menu.
   */ 
  public void 	
  windowClosing
  (
   WindowEvent e
  ) 
  {
    setVisible(false);
    removePanels();
    UIMaster.getInstance().destroyWindow(this);
  }

  /**
   * Invoked when a Window is no longer the active Window.
   */ 
  public void 	
  windowDeactivated(WindowEvent e) {}

  /**
   * Invoked when a window is changed from a minimized to a normal state.
   */ 
  public void 	
  windowDeiconified(WindowEvent e) {}

  /**
   * Invoked when a window is changed from a normal to a minimized state.
   */ 
  public void 	
  windowIconified(WindowEvent e) {}

  /**
   * Invoked the first time a window is made visible.	
   */ 
  public void     
  windowOpened(WindowEvent e) {}



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6101372426985723817L;


  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent of the root manager panel.
   */ 
  private JPanel  pRootPanel; 

  /**
   * The name of the window saved in layouts.
   */ 
  private String  pWindowName; 
  
}
