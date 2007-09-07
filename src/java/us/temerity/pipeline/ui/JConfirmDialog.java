// $Id: JConfirmDialog.java,v 1.4 2007/09/07 18:52:38 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C O N F I R M   D I A L O G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Asks a simple question.
 */ 
public 
class JConfirmDialog
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
   */ 
  public 
  JConfirmDialog
  (
   Frame owner,      
   String question
  )
  {
    super(owner, "Confirm");
    initUI(question, null);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param question
   *   The question to ask the user.
   */ 
  public 
  JConfirmDialog
  (      
   Dialog owner, 
   String question
  )
  {
    super(owner, "Confirm");
    initUI(question, null);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param question
   *   The question to ask the user.
   * 
   * @param msg
   *   Longer explanitory message.
   */ 
  public 
  JConfirmDialog
  (
   Frame owner,      
   String question, 
   String msg
  )
  {
    super(owner, "Confirm");
    initUI(question, msg);
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
   * @param msg
   *   Longer explanitory message.
   */ 
  public 
  JConfirmDialog
  (      
   Dialog owner, 
   String question, 
   String msg
  )
  {
    super(owner, "Confirm");
    initUI(question, msg);
  }


  /*----------------------------------------------------------------------------------------*/

  private void
  initUI
  (
   String question, 
   String msg
  ) 
  {
    if(msg == null) {
      super.initUI(question, null, "Yes", null, null, "No");
      setResizable(false);
    }
    else {
      JPanel body = new JPanel(new BorderLayout());
      body.setName("MainDialogPanel");

      body.setMinimumSize(new Dimension(300, 180));

      {
	JTextArea area = new JTextArea(msg, 8, 35); 
	area.setName("TextArea");

	area.setLineWrap(true);
	area.setWrapStyleWord(true);
	area.setEditable(false);

	area.setFocusable(true);
      
	{
	  JScrollPane scroll = 
            UIFactory.createScrollPane
            (area, 
             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
             null, null, null);
	  
	  body.add(scroll);
	}
      }

      super.initUI(question, body, "Yes", null, null, "No");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8157500834598462446L;

}
