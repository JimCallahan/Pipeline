// $Id: JNodeBrowserPanel.java,v 1.2 2004/04/28 00:43:23 jim Exp $

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
      JPanel bar = new JPanel();
      bar.setName("PanelBar");
      bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS)); 
      bar.setMinimumSize(new Dimension(200, 29));
      bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
      bar.setPreferredSize(new Dimension(200, 29));

      bar.add(Box.createHorizontalGlue());

      {
	JComboBox combo = new JComboBox();
	combo.setRenderer(new JComboBoxCellRenderer());

// 	Dimension size = new Dimension(109, 17);
// 	combo.setMinimumSize(size);
// 	combo.setMaximumSize(size);
// 	combo.setPreferredSize(size);

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
	
	bar.add(combo);
      }

      bar.add(Box.createRigidArea(new Dimension(25,0)));

      {
	JButton close = new JButton();
	close.setName("CloseButton");

	Dimension size = new Dimension(15, 25);
	close.setMinimumSize(size);
	close.setMaximumSize(size);
	close.setPreferredSize(size);
	
	bar.add(close);
      }

      bar.add(Box.createRigidArea(new Dimension(7,0)));

      add(bar);
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
