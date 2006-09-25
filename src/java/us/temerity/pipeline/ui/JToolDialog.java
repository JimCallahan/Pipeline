// $Id: JToolDialog.java,v 1.2 2006/09/25 12:11:45 jim Exp $

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
   */ 
  public
  JToolDialog
  (
   String header, 
   JComponent body, 
   String confirm
  ) 
  {
    super(sRootFrame, "Tool");
    initUI(header, body, confirm, null, null, "Cancel");
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

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2888190912678205880L;

  private static JFrame  sRootFrame = null;
}
