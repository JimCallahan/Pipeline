// $Id: JToolDialog.java,v 1.3 2007/04/01 22:05:18 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L   D I A L O G                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The dialog used for collection user input for tool plugins.
 */ 
public 
class JToolDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog for the given tool.
   * 
   * @param header
   *   The text displayed in the dialog header. 
   * 
   * @param body
   *   The component containing the body of the dialog.
   * 
   * @param confirm
   *   The title of the confirm button.
   */ 
  public
  JToolDialog
  (
   String header, 
   JComponent body, 
   String confirm
  ) 
  {
    this(header, body, confirm, null, null); 
  }

  /**
   * Construct a new dialog for the given tool.
   * 
   * @param header
   *   The text displayed in the dialog header. 
   * 
   * @param body
   *   The component containing the body of the dialog.
   * 
   * @param confirm
   *   The title of the confirm button.
   *
   * @param apply
   *   The title of the apply button.
   */
  public
  JToolDialog
  (
   String header, 
   JComponent body, 
   String confirm,
   String apply
  ) 
  {
    this(header, body, confirm, apply, null); 
  }

  /**
   * Construct a new dialog for the given tool. 
   * 
   * @param header
   *   The text displayed in the dialog header. 
   * 
   * @param body
   *   The component containing the body of the dialog.
   * 
   * @param confirm
   *   The title of the confirm button.
   * 
   * @param apply
   *   The title of the apply button.
   * 
   * @param extra
   *   An array of title/action-command strings pairs used to create extra buttons.
   */ 
  public
  JToolDialog
  (
   String header, 
   JComponent body, 
   String confirm, 
   String apply, 
   String extra[][]
  ) 
  {
    super(sRootFrame, "Tool");
    initUI(header, body, confirm, apply, extra, "Cancel");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N I T I A L I Z A T I O N                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the root Pipeline frame.
   */ 
  public static void 
  initRootFrame
  (
   JFrame root
  ) 
  {
    sRootFrame = root;
  }

  /**
   * Get the root Pipeline frame.
   */ 
  public static JFrame
  getRootFrame() 
  {
    return sRootFrame; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2888190912678205880L;

  private static JFrame  sRootFrame = null;

}
