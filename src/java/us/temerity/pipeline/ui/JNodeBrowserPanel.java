// $Id: JNodeBrowserPanel.java,v 1.3 2004/04/28 03:59:57 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   B R O W S E R   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The registered node browser.
 */ 
public  
class JNodeBrowserPanel
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel.
   */
  public 
  JNodeBrowserPanel()
  {
    super();
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));   

    {
      JPanel panel = new JPanel();
      panel.setName("PanelBar");
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 
      panel.setMinimumSize(new Dimension(200, 29));
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
      panel.setPreferredSize(new Dimension(200, 29));

      {
	JButton btn = new JButton();
	btn.setName("PanelMenuButton");

	Dimension size = new Dimension(14, 19);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);
	
	panel.add(btn);
      }

      panel.add(Box.createHorizontalGlue());

      {
	JComboBox combo = new JComboBox();
	combo.setRenderer(new JComboBoxCellRenderer());

	Dimension size = new Dimension(155, 19);
	combo.setMinimumSize(size);
	combo.setMaximumSize(size);
	combo.setPreferredSize(size);

	combo.addItem("Node Browser");
	combo.addItem("Node Viewer");
	combo.addItem("Node Properties");
	combo.addItem("Node Links");
	combo.addItem("Node Files");
	combo.addItem("Node History");
	combo.addItem("Queue Manager");
	combo.addItem("Job Details");
	combo.addItem("Task Timeline");
	combo.addItem("Task Details");
	
	panel.add(combo);
      }

      panel.add(Box.createHorizontalGlue());

      {
	JButton btn = new JButton();
	btn.setName("CloseButton");

	Dimension size = new Dimension(15, 19);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);
	
	panel.add(btn);
      }

      add(panel);
    }

    add(Box.createVerticalGlue());
    add(Box.createRigidArea(new Dimension(0,20)));

    JPanel hpanel = new JPanel(true);
    hpanel.setLayout(new BoxLayout(hpanel, BoxLayout.X_AXIS)); 
    add(hpanel);
      
    {	    
      hpanel.add(Box.createHorizontalGlue());
      hpanel.add(Box.createRigidArea(new Dimension(20,0)));

      hpanel.add(new Label("Node History"));

      hpanel.add(Box.createRigidArea(new Dimension(20,0)));
      hpanel.add(Box.createHorizontalGlue());
    }
      
    add(Box.createRigidArea(new Dimension(0,20)));
    add(Box.createVerticalGlue());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  //private static final long serialVersionUID = -3122417485809218152L;


}
