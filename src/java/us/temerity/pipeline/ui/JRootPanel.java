// $Id: JRootPanel.java,v 1.1 2004/04/30 08:40:52 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   R O O T   P A N E L                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * 
*/ 
public 
class JRootPanel
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a manager panel. 
   */
  JRootPanel()
  {
    super();

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));   
      
    {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setName("RootPanel");

      JManagerPanel mgr = null;
      {
	mgr = new JManagerPanel();
	mgr.setContents(new JEmptyPanel());

	panel.add(mgr);
      }

      add(panel);
    }
    
    add(Box.createRigidArea(new Dimension(0, 2)));

    {
      JPanel panel = new JPanel(); 
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 

      panel.add(Box.createRigidArea(new Dimension(2, 0)));

      {
	JToggleButton btn = new JToggleButton();
	btn.setName("StopLight");

	Dimension size = new Dimension(15, 19);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);

	panel.add(btn);
      }

      {
	JTextField field = new JTextField("Check-In in Progress...");
	
	field.setMinimumSize(new Dimension(200, 19));
	field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	field.setPreferredSize(new Dimension(200, 19));
	
	panel.add(field);
      }

      panel.add(Box.createRigidArea(new Dimension(3, 0)));
      panel.add(Box.createHorizontalGlue());

      add(panel);
    }

    add(Box.createRigidArea(new Dimension(0, 4)));
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  //private static final long serialVersionUID = -3122417485809218152L;

//   static private Icon sTabIcon = 
//     new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));


  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

}
