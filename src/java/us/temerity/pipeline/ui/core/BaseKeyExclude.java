// $Id: BaseKeyExclude.java,v 1.2 2005/01/09 23:23:06 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.event.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   K E Y   E X C L U D E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Abstract base class of all hot key excluder classes.
 */
public abstract
class BaseKeyExclude
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  protected 
  BaseKeyExclude() 
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Check the other hot keys for conflicts.
   */ 
  protected abstract void 
  validate() 
    throws PipelineException;

  /**
   * Throw the conflict exception.
   */ 
  protected void 
  conflict
  (
   HotKey key,
   String pref, 
   String cpref
  ) 
    throws PipelineException
  {
    throw new PipelineException
      ("The hot key (" + key + ") cannot be used for the preference:\n" + 
       "  " + pref.replaceAll("\\|", " - ") + "\n\n" + 
       "The hot key is already being used by the preference:\n" +
       "  " + cpref.replaceAll("\\|", " - "));
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
      try {
	validate();
      }
      catch(PipelineException ex) {
	UIMaster.getInstance().showErrorDialog(ex);
	
	JHotKeyField field = (JHotKeyField) e.getSource();
	field.setHotKey(null);
      }
    }
  }
}
