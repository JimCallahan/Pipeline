// $Id: JManageLayoutsDialog.java,v 1.2 2004/05/11 19:16:33 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   L A Y O U T S   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Manage the set of saved panel layouts.
 */ 
public 
class JManageLayoutsDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageLayoutsDialog()
  {
    super("Manage Saved Layouts", true);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel(new BorderLayout());
      body.setName("ManageLayoutsPanel");

      








      String extra[][] = {
	{ "Rename", "rename-layout", },
	{ "Delete", "delete-layout" }
      };
      
      super.initUI("Manage Saved Layouts:", true, body, null, null, extra, "Close");
    }  
  }





  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 262356898038054868L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
}
