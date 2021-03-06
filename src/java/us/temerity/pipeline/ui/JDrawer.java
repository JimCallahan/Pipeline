// $Id: JDrawer.java,v 1.5 2009/07/13 17:26:02 jlee Exp $

package us.temerity.pipeline.ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   D R A W E R                                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel with a header which is used to hide/show the contents of the panel.
 */ 
public 
class JDrawer
  extends JPanel
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an closed and empty drawer.
   * 
   * @param title
   *   The header text.
   */ 
  public 
  JDrawer
  (
   String title
  ) 
  {
    super();
    initUI(title, null, 0, false);
  }

  /**
   * Construct a drawer with the given contents.
   * 
   * @param title
   *   The header text.
   * 
   * @param contents
   *   The contents of the drawer.
   * 
   * @param isOpen
   *   Whether the drawer is initially open.
   */ 
  public 
  JDrawer
  (
   String title,
   JComponent contents, 
   boolean isOpen
  ) 
  {
    super();
    initUI(title, contents, 0, isOpen);
  }

  /**
   * Construct a drawer with the given contents.
   * 
   * @param title
   *   The header text.
   * 
   * @param contents
   *   The contents of the drawer.
   *
   * @param width
   *   The preferred width of the drawer.
   * 
   * @param isOpen
   *   Whether the drawer is initially open.
   */ 
  public 
  JDrawer
  (
   String title,
   JComponent contents, 
   int width, 
   boolean isOpen
  ) 
  {
    super();
    initUI(title, contents, width, isOpen);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   * 
   * @param title
   *   The header text.
   * 
   * @param contents
   *   The contents of the drawer.
   *
   * @param width
   *   The preferred width of the drawer.
   * 
   * @param isOpen
   *   Whether the drawer is initially open.
   */ 
  private void 
  initUI
  (
   String title,
   JComponent contents, 
   int width, 
   boolean isOpen
  ) 
  {  
    setName("Drawer");
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    /* title bar */ 
    {
      JCheckBox cbox = new JCheckBox(title);
      pHeader = cbox; 

      cbox.setName("DrawerHeader");

      int height = cbox.getPreferredSize().height;
      cbox.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

      /* Set the preferred width of the JCheckBox to the value passed to the constrcutor, 
         this the checkbox can display long titles with trailing "..." */
      if(width > 0) {
	cbox.setPreferredSize(new Dimension(width, height));

	int len = title.length();
	if(title.charAt(len - 1) == ':') {
	  String tooltip = title.substring(0, len - 1);
	  cbox.setToolTipText(UIFactory.formatToolTip(tooltip));
	}
      }

      cbox.setAlignmentX(0.5f);

      cbox.setActionCommand("collapse");
      cbox.addActionListener(this);

      add(cbox);
    }

    setContents(contents);
    setIsOpen(isOpen);
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the title text.
   */ 
  public String
  getTitle()
  {
    return pHeader.getText();
  }

  /**
   * Set the title text.
   */ 
  public void 
  setTitle
  (
   String title  
  ) 
  {
    pHeader.setText(title);
  }
 
  
  /**
   * Get the contents of the drawer.
   */ 
  public JComponent
  getContents()
  {
    return pContents;
  }

  /**
   * Set the contents of the drawer.
   */ 
  public void
  setContents
  (
   JComponent contents
  ) 
  {
    if(pContents != null) 
      remove(pContents);

    if(contents == null) {
      JPanel panel = new JPanel();
      pContents = panel;
      
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      panel.add(Box.createRigidArea(new Dimension(0, 8)));
      panel.add(Box.createHorizontalGlue());	
    }
    else {
      pContents = contents;
    }

    pContents.setVisible(isOpen());
    
    add(pContents);
  }


  /**
   * Is the drawer currently open? 
   */ 
  public boolean
  isOpen()
  {
    return pHeader.getModel().isSelected();
  }

  /**
   * Set whether the drawer should be open.
   */ 
  public void
  setIsOpen
  (
   boolean isOpen   
  ) 
  {
    pHeader.getModel().setSelected(isOpen);
    pContents.setVisible(isOpen);
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   E V E N T S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Adds an ActionListener to the drawer. <P> 
   * 
   * The action is triggered when the drawer is opened or closed.
   */ 
  public void 
  addActionListener
  (
   ActionListener l
  ) 
  {
    pHeader.addActionListener(l);
  }

  /**
   * Removes an ActionListener from the drawer. <P> 
   */ 
  public void 
  removeActionListener
  (
   ActionListener l
  )
  {
    pHeader.removeActionListener(l);
  }
  
  /**
   * Sets the action command for this drawer. <P> 
   */
  public void 
  setActionCommand
  (
   String cmd
  )
  {
    pHeader.setActionCommand(cmd);
  }

   
  /*----------------------------------------------------------------------------------------*/
  /*   J C O M P O N E N T   O V E R R I D E S                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Registers the text to display in a tool tip.
   */
  public void 	
  setToolTipText
  (
   String text
  )
  {
    pHeader.setToolTipText(text);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    pContents.setVisible(isOpen());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6827904546455435160L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The always visible header used to open/close the drawer.
   */ 
  private JCheckBox   pHeader;  

  /**
   * The contents which is shown/hidded when the drawer is opened/closed.
   */ 
  private JComponent  pContents; 

}
