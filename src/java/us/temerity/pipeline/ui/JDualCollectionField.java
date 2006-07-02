// $Id: JDualCollectionField.java,v 1.1 2006/07/02 00:27:49 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   D U A L   C O L L E C T I O N   F I E L D                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents two {@link Collection Collection} instances where the values
 * which can be selected is a subset of the values which can be displayed. <P> 
 */
public 
class JDualCollectionField
  extends JCollectionField
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   * 
   * @param selectValues
   *   The values which can be selected using a pull-down menu.
   * 
   * @param displayValues
   *   The values which can be displayed in the field.
   */ 
  public 
  JDualCollectionField
  (
   Collection<String> selectValues, 
   Collection<String> displayValues
  )
  {
    this(selectValues, displayValues, null);
  }

  /**
   * Construct a new field.
   * 
   * @param selectValues
   *   The values which can be selected using a pull-down menu.
   * 
   * @param displayValues
   *   The values which can be displayed in the field.
   */ 
  public 
  JDualCollectionField
  (
   Collection<String> selectValues, 
   Collection<String> displayValues, 
   JDialog parent
  )
  {
    super(displayValues, parent);  
    setValues(selectValues, displayValues); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Set the collection values. 
   */ 
  public void 
  setValues
  (
   Collection<String> values
  ) 
  {
    setValues(values, values);
  }

  /**
   * Set the collection values. 
   * 
   * @param selectValues
   *   The values which can be selected using a pull-down menu.
   * 
   * @param displayValues
   *   The values which can be displayed in the field.
   */ 
  public void 
  setValues
  (
   Collection<String> selectValues, 
   Collection<String> displayValues
  ) 
  {
    if(!displayValues.containsAll(selectValues)) 
      throw new IllegalArgumentException
	("All of the select values must also be display values!");
    
    pValues = new ArrayList<String>(displayValues);
    pSelectValues = new ArrayList<String>(selectValues);

    pPopup.removeAll();
    for(String v : pSelectValues) {
      JMenuItem item = new JMenuItem(v);
      
      item.setActionCommand(v);
      item.addActionListener(this);

      pPopup.add(item);  
    }

    setSelectedIndex(0);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  ) 
  {
    mousePressedHelper(e, pValues, pSelectValues);
  }


  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

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

    int idx = pItemList.getSelectedIndex();
    if(idx != -1) {
      setSelected(pSelectValues.get(idx));
      pDialog.setVisible(false);
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4438937894390715487L; 


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying selectable Collection.
   */ 
  private ArrayList<String>  pSelectValues;

}
