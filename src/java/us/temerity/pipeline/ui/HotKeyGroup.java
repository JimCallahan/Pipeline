// $Id: HotKeyGroup.java,v 1.1 2005/01/09 23:12:34 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.event.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   H O T   K E Y   G R O U P                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Synchronizes the value displayed by a collection of {@link JHotKeyField JHotKeyField} 
 * instances.
 */
public 
class HotKeyGroup
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  HotKeyGroup() 
  {
    pFields = new LinkedList<JHotKeyField>();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a hot key field to the group.
   */ 
  public void 
  add
  (
   JHotKeyField field
  ) 
  {
    field.addActionListener(this);
    pFields.add(field);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    if(e.getActionCommand().equals("hot-key-changed")) {
      JHotKeyField source = (JHotKeyField) e.getSource();
      for(JHotKeyField field : pFields) {
	if(field != source) 
	  field.setHotKeyNoAction(source.getHotKey());
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fields who's values are to be synchronized.
   */ 
  private LinkedList<JHotKeyField>  pFields; 

}
