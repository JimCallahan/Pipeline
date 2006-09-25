// $Id: JConfirmDialog.java,v 1.2 2006/09/25 12:11:45 jim Exp $

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
    initUI(question);
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
    initUI(question);
  }


  /*----------------------------------------------------------------------------------------*/

  private void
  initUI
  (
   String question
  ) 
  {
    super.initUI(question, null, "Yes", null, null, "No");
    setResizable(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8157500834598462446L;

}
