// $Id: JInfoDialog.java,v 1.1 2007/10/23 02:29:59 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   I N F O   D I A L O G                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays some information in the form of a text message.
 */ 
public 
class JInfoDialog
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
   * @param title
   *   The dialog title.
   */ 
  public 
  JInfoDialog
  (
   Frame owner,      
   String title
  )
  {
    super(owner, "Info");
    initUI(title, null);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The dialog title.
   */ 
  public 
  JInfoDialog
  (      
   Dialog owner, 
   String title
  )
  {
    super(owner, "Info");
    initUI(title, null);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param title
   *   The dialog title.
   * 
   * @param msg
   *   Longer explanitory message.
   */ 
  public 
  JInfoDialog
  (
   Frame owner,      
   String title, 
   String msg
  )
  {
    super(owner, "Info");
    initUI(title, msg);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The dialog title.
   * 
   * @param msg
   *   Longer explanitory message.
   */ 
  public 
  JInfoDialog
  (      
   Dialog owner, 
   String title, 
   String msg
  )
  {
    super(owner, "Info");
    initUI(title, msg);
  }


  /*----------------------------------------------------------------------------------------*/

  private void
  initUI
  (
   String title, 
   String msg
  ) 
  {
    if(msg == null) {
      super.initUI(title, null, "Close", null, null, null);
      setResizable(false);
    }
    else {
      JPanel body = new JPanel(new BorderLayout());
      body.setName("MainDialogPanel");

      body.setMinimumSize(new Dimension(600, 300));

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

      super.initUI(title, body, "Close", null, null, null);
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8124420478348816050L;

}
