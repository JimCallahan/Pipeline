// $Id: JToolDialog.java,v 1.1 2005/01/05 09:44:31 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

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
   BaseTool tool
  ) 
  {
    super("Tool", true);
    initUI(tool.getHeaderText(), true, tool.getDialogBody(), "Confirm", null, null, "Cancel");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2888190912678205880L;

}
