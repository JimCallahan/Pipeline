// $Id: JConfirmDialog.java,v 1.1 2004/05/23 20:01:27 jim Exp $

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
   * @param question
   *   The question to ask the user.
   */ 
  public 
  JConfirmDialog
  (
   String question
  )
  {
    super("Confirm", true);

    super.initUI(question, true, null, "Yes", null, null, "No");
    setResizable(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8157500834598462446L;

}
