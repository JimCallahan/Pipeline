// $Id: JSaveLayoutDialog.java,v 1.3 2004/05/12 04:00:36 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   S A V E   L A Y O U T   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Saves the current panel layout to disk.
 */ 
public 
class JSaveLayoutDialog
  extends JSelectLayoutDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JSaveLayoutDialog()
  {
    super("Save Layout");
    initUI();
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JSaveLayoutDialog
  (
   Dialog owner
  )  
  {
    super(owner, "Save Layout");
    initUI();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   */ 
  protected void
  initUI()
  {
    super.initUI("Save Layout:", "Save As:", 54, "Save");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1836402945207087332L;

}
