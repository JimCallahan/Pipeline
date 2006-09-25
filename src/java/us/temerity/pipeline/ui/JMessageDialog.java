// $Id: JMessageDialog.java,v 1.2 2006/09/25 12:11:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   M E S S A G E   D I A L O G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays a simple message.
 */ 
public 
class JMessageDialog
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
   * @param msg
   *   The message to display. 
   */ 
  public 
  JMessageDialog
  (
   Frame owner,      
   String msg
  )
  {
    super(owner, "Message");
    initUI(msg);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param msg
   *   The message to display. 
   */ 
  public 
  JMessageDialog
  (      
   Dialog owner, 
   String msg
  )
  {
    super(owner, "Message");
    initUI(msg);
  }


  /*----------------------------------------------------------------------------------------*/

  private void
  initUI
  (
   String msg
  ) 
  {
    super.initUI(msg, null, null, null, null, "Ok");
    setResizable(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3361799049605399785L;

}
