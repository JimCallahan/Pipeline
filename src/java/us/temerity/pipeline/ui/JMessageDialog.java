// $Id: JMessageDialog.java,v 1.1 2005/04/02 01:00:31 jim Exp $

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
   * @param msg
   *   The message to display. 
   */ 
  public 
  JMessageDialog
  (
   String msg
  )
  {
    super("Message", true);

    super.initUI(msg, true, null, null, null, null, "Ok");
    setResizable(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3361799049605399785L;

}
