// $Id: JConfirmListDialog.java,v 1.1 2005/02/22 18:20:03 jim Exp $

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
  implements ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param question
   *   The question to ask the user.
   * 
   * @param itemTitle
   *   The text to display as the title of the list.
   * 
   * @param items
   *   The items to display in the list.
   */ 
  public 
  JConfirmListDialog
  (
   String question, 
   String itemTitle, 
   Collection<String> items
  )
  {
    super("Confirm", true);

    Box body = new Box(BoxLayout.X_AXIS);
    {
      body.add(Box.createRigidArea(new Dimension(20, 0)));
      
      Dimension size = new Dimension(300, 200);
      pList = UIFactory.createListComponents(body, itemTitle, size);
      pList.addListSelectionListener(this);

      {
	DefaultListModel model = (DefaultListModel) pList.getModel();
	for(String item : items) 
	  model.addElement(item);
      }
      
      body.add(Box.createRigidArea(new Dimension(20, 0)));
    }

    super.initUI(question, true, body, "Yes", null, null, "No");
    pack();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- LIST LISTENER METHODS ---------------------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 
  valueChanged
  (
   ListSelectionEvent e
  )
  {
    if(e.getValueIsAdjusting()) 
      return;

    pList.clearSelection();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4292754650405933966L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The list containing the items.
   */ 
  private JList  pList; 

}
