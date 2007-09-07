// $Id: JConfirmListDialog.java,v 1.5 2007/09/07 18:52:38 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C O N F I R M   L I S T   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Asks a simple question about a list of items.
 */ 
public 
class JConfirmListDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param question
   *   The question to ask the user.
   * 
   * @param title
   *   The text to display as the title of the list.
   * 
   * @param items
   *   The items to display in the list.
   */ 
  public 
  JConfirmListDialog
  (
   Frame owner,  
   String question, 
   String title, 
   Collection<String> items
  )
  {
    super(owner, "Confirm");
    initUI(question, title, items);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param question
   *   The question to ask the user.
   * 
   * @param title
   *   The text to display as the title of the list.
   * 
   * @param items
   *   The items to display in the list.
   */ 
  public 
  JConfirmListDialog
  (
   Dialog owner,        
   String question, 
   String title, 
   Collection<String> items
  )
  {
    super(owner, "Confirm");
    initUI(question, title, items);
  }


  /*----------------------------------------------------------------------------------------*/
  
  private void 
  initUI
  (
   String question, 
   String title, 
   Collection<String> items
  )
  {
    Box body = new Box(BoxLayout.X_AXIS);
    {
      body.add(Box.createRigidArea(new Dimension(20, 0)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	
	vbox.add(UIFactory.createPanelLabel(title));

	vbox.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  String text = null;
	  {
	    StringBuilder buf = new StringBuilder();
	    for(String item : items) 
	      buf.append(item + "\n");
	    text = buf.toString();
	  }

	  JTextArea area = new JTextArea(text);
	  area.setName("TextArea");
	  area.setLineWrap(false);
	  area.setEditable(false);
	  
	  {
	    JScrollPane scroll =
              UIFactory.createScrollPane
              (area, 
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, 
               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
               null, null, null);
	  
	    vbox.add(scroll);
	  }
	}

	vbox.add(Box.createRigidArea(new Dimension(0, 20)));

	body.add(vbox);
      }
	
      body.add(Box.createRigidArea(new Dimension(20, 0)));
    }

    super.initUI(question, body, "Yes", null, null, "No");
    pack();

    setSize(350, 500);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4292754650405933966L;


}
