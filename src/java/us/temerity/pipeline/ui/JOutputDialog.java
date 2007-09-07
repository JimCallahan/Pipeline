// $Id: JOutputDialog.java,v 1.3 2007/09/07 18:52:38 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.GlueException;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   O U T P U T   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays a large text message. 
 */ 
public 
class JOutputDialog
  extends JTopLevelDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog owned by the main application frame. 
   */ 
  public 
  JOutputDialog() 
  {
    super("Output");

    JPanel body = new JPanel();
    {    
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      body.setName("MainDialogPanel");
      body.setMinimumSize(new Dimension(300, 180));
      
      {
	Box lbox = new Box(BoxLayout.X_AXIS);
      
	lbox.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JLabel label = new JLabel("X");
	  pTitleLabel = label;
	  label.setName("PanelLabel");
	
	  lbox.add(label);
	}
	
	lbox.add(Box.createHorizontalGlue());
	
	body.add(lbox);
      }
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JTextArea area = new JTextArea(20, 80); 
	pMessageArea = area;
	
	area.setName("CodeTextArea");

	area.setLineWrap(false);
	area.setEditable(false);
	
	area.setFocusable(true);
      }
      
      {
	JScrollPane scroll = 
          UIFactory.createScrollPane
          (pMessageArea, 
           JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,
           JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
           null, null, null);
	
	body.add(scroll);
      }
    }
      
    super.initUI(":", body, null, null, null, "Close");
    pack();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the title and text of the error message.
   */ 
  public void 
  setMessage
  (
   String header, 
   String title, 
   String msg
  ) 
  {
    pHeaderLabel.setText(header);
    pTitleLabel.setText(title);
    pMessageArea.setText(msg);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2835849889423148288L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The message area title.
   */ 
  private JLabel  pTitleLabel; 
  
  /**
   * The error message text.
   */ 
  private JTextArea pMessageArea;
  
}
