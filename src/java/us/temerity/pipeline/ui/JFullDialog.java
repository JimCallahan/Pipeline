// $Id: JFullDialog.java,v 1.1 2006/09/25 12:11:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   F U L L   D I A L O G                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of all model dialogs with error handling capabilities.
 */ 
public 
class JFullDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog owned by a top-level frame.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  protected
  JFullDialog
  (
   Frame owner,        
   String title
  ) 
  {
    super(owner, title);
  }

  /**
   * Construct a new dialog owned by another dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  protected
  JFullDialog
  (
   Dialog owner,        
   String title
  ) 
  {
    super(owner, title);
  }

     

  /*----------------------------------------------------------------------------------------*/
  /*   D I A L O G S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Show an error message dialog for the given exception.
   */ 
  public void 
  showErrorDialog
  (
   Exception ex
  ) 
  {
    if(pErrorDialog == null) 
      pErrorDialog = new JErrorDialog(this);

    pErrorDialog.setMessage(ex);
    SwingUtilities.invokeLater(new ShowErrorDialogTask());
  }

  /**
   * Show an error message dialog with the given title and message.
   */ 
  public void 
  showErrorDialog
  (
   String title, 
   String msg
  ) 
  {
    if(pErrorDialog == null) 
      pErrorDialog = new JErrorDialog(this);

    pErrorDialog.setMessage(title, msg);
    SwingUtilities.invokeLater(new ShowErrorDialogTask());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the error dialog. <P> 
   * 
   * The reason for the thread wrapper is to allow the rest of the UI to repaint before
   * showing the dialog.
   */ 
  private
  class ShowErrorDialogTask
    extends Thread
  { 
    public 
    ShowErrorDialogTask() 
    {
      super("JFullDialog:ShowErrorDialogTask");
    }

    public void 
    run() 
    {
      pErrorDialog.setVisible(true);
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1972856299852271626L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The error message dialog.
   */ 
  private JErrorDialog  pErrorDialog;

}
