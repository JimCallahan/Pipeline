// $Id: JCreateToolsetDialog.java,v 1.3 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C R E A T E   N O T E    D I A L O G                                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the text of a new server note. 
 */ 
public 
class JCreateNoteDialog
  extends JBaseDialog
  implements DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCreateNoteDialog
  (
   Frame owner        
  )
  {
    super(owner, "New Queue Server Note");
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  private void 
  initUI() 
  {
    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];

	{
	  JTextArea area = 
	    UIFactory.createTitledEditableTextArea(tpanel, "Message:", sTSize, 
                                                   vpanel, "", sVSize, 12, false);
	  pMessageArea = area;
	  area.getDocument().addDocumentListener(this);
	}

	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("New Queue Server Note:", body, "Add", null, null, "Close");
      pack();
    }    
    
    pConfirmButton.setEnabled(false);  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the message text.
   */ 
  public String 
  getMessage() 
  {
    return pMessageArea.getText();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   */ 
  public void  
  update() 
  {
    pMessageArea.setText(null);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Gives notification that an attribute or set of attributes changed.
   */ 
  public void 
  changedUpdate(DocumentEvent e) {}

  /**
   * Gives notification that there was an insert into the document.
   */
  public void
  insertUpdate
  (
   DocumentEvent e
  )
  {
    String desc = pMessageArea.getText();
    pConfirmButton.setEnabled((desc != null) && (desc.length() > 0));
  }
  
  /**
   * Gives notification that a portion of the document has been removed.
   */
  public void 
  removeUpdate
  (
   DocumentEvent e
  )
  {
    String desc = pMessageArea.getText();
    pConfirmButton.setEnabled((desc != null) && (desc.length() > 0));    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6502894548607381010L;

  private static final int sTSize = 70;
  private static final int sVSize = 350;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The description of the entity to create.
   */ 
  private JTextArea  pMessageArea;

}
