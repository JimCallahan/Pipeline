// $Id: JOwnerViewDialog.java,v 1.1 2004/05/02 12:13:34 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   O W N E R   V I E W   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of all application dialogs.
 */ 
public 
class JOwnerViewDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param author
   *   The current owner of the working area view.
   *
   * @param view
   *   The current name of the working area view.
   * 
   * @param table
   *   The table of valid working area view names indexed by author user name.
   */ 
  public 
  JOwnerViewDialog
  (
   String author, 
   String view, 
   TreeMap<String,TreeSet<String>> table
  )
  {
    super("Change Owner|View", true);    

    pAuthor = author; 
    pView   = view;

  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of user which owens the working version.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /** 
   * Get the name of the working area view.
   */
  public String
  getView()
  {
    return pView;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm()
  {
    

    super.doConfirm();
  }

  /**
   * Cancel changes and close.
   */ 
  public void 
  doCancel()
  {
    pAuthor = null;
    pView   = null;

    super.doCancel();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6049795946975300474L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The user name of the owner of the working area.
   */
  private String pAuthor; 
  
  /**
   * The name of the new working area view.
   */
  private String pView; 
  
}
