// $Id: JErrorDialog.java,v 1.11 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.GlueException;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   E R R O R     D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays Pipeline error messages.
 */ 
public 
class JErrorDialog
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
   */ 
  public 
  JErrorDialog
  (
   Frame owner
  )  
  {
    super(owner, "Error");
    initUI();
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JErrorDialog
  (
   Dialog owner
  )  
  {
    super(owner, "Error");
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  public void 
  initUI() 
  {
    /* create dialog body components */ 
    {
      JPanel body = new JPanel(new BorderLayout());
      body.setName("MainDialogPanel");

      body.setMinimumSize(new Dimension(300, 180));

      {
	JTextArea area = new JTextArea(8, 35); 
	pMessageArea = area;

	area.setName("TextArea");

	area.setLineWrap(true);
	area.setWrapStyleWord(true);
	area.setEditable(false);

	area.setFocusable(true);
      }
      
      {
	JScrollPane scroll = new JScrollPane(pMessageArea);
	scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	
	body.add(scroll);
      }

      super.initUI("Error:", body, null, null, null, "Close");
      pack();
    }  
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
   String title, 
   String msg
  ) 
  {
    pHeaderLabel.setText(title);
    pMessageArea.setText(msg);
  }

  /**
   * Set the title and text of the error message based on an exception.
   */ 
  public void 
  setMessage
  (
   Exception ex
  ) 
  {
    if(ex instanceof PipelineException) {
      pHeaderLabel.setText("Error:");
      pMessageArea.setText(ex.getMessage());
    }
    else if(ex instanceof GlueException) {
      pHeaderLabel.setText("Glue Error:");
      pMessageArea.setText(getStackTrace(ex));
    }
    else if(ex instanceof IOException) {
      pHeaderLabel.setText("I/O Error:");
      pMessageArea.setText(ex.getMessage());
    }
    else {
      pHeaderLabel.setText("Internal Error:");
      pMessageArea.setText(getStackTrace(ex));
    }
  }

  private String
  getStackTrace
  (
   Exception ex
  ) 
  {
    StringBuilder buf = new StringBuilder();

    if(ex.getMessage() != null) 
      buf.append(ex.getMessage() + "\n\n"); 	
    else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
    
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = ex.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");

    return buf.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6715827604298406774L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The error message text.
   */ 
  private JTextArea pMessageArea;
  
}
