// $Id: JNodeBrowserPanel.java,v 1.4 2004/04/28 23:23:51 jim Exp $

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
