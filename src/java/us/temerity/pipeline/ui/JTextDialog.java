// $Id: JTextDialog.java,v 1.2 2004/06/19 00:35:29 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.toolset.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   D I A L O G                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A simple text viewer/editor dialog.
 */ 
public 
class JTextDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   *
   * @param isEditable
   *   Whether the displayed text can be edited by the dialog.
   */ 
  public 
  JTextDialog
  (
   boolean isEditable
  )    
  {
    super(isEditable ? "Edit Text" : "View Text", true);
    
    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));

      {
	JTextArea area = new JTextArea(null, 30, 80);
	pTextArea = area;

	area.setName("CodeTextArea");
	area.setLineWrap(true);
	area.setWrapStyleWord(true);
	area.setEditable(isEditable);
      }

      {
	JScrollPane scroll = new JScrollPane(pTextArea);

	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	
	body.add(scroll);
      }
      
      String confirm = null;
      String apply   = null;
      String cancel  = "Close";
      if(isEditable) {
	confirm = "Confirm";
	apply   = "Load";
	cancel  = "Cancel";
      }

      super.initUI("X", true, body, confirm, apply, null, cancel);
    }

    /* initialize the file loading dialog */ 
    if(isEditable) {
      pLoadDialog = 
	new JFileSelectDialog(this, "Select File", "Load File:", "File:", 42, "Load");
      pLoadDialog.updateTargetFile(null);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the edited text.
   */ 
  public String
  getText() 
  {
    return pTextArea.getText();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the text being displayed.
   * 
   * @param header
   *   The header label.
   * 
   * @param text
   *   The text to edit/view.
   */ 
  public void 
  updateText
  (
   String header, 
   String text
  ) 
  {
    pHeaderLabel.setText(header);
    pTextArea.setText(text);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Browse for a file who's contents will replace the current text.
   */ 
  public void 
  doApply()
  {
    pLoadDialog.setVisible(true);
      
    if(pLoadDialog.wasConfirmed()) {
      File file = pLoadDialog.getSelectedFile();
      if(file != null) {
	try {
	  FileReader in = new FileReader(file);
	  pTextArea.read(in, file);
	}
	catch(IOException ex) {
	  UIMaster.getInstance().showErrorDialog(ex);
	}
      }	
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7105423316560264964L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The text being edited.
   */ 
  private JTextArea  pTextArea;

  /**
   * The file loading dialog.
   */ 
  private JFileSelectDialog  pLoadDialog;

}
