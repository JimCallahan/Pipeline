// $Id: JBackupDialog.java,v 1.1 2009/12/10 02:30:12 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   B A C K U P   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the path of the directory used to store database backups. 
 */ 
public 
class JBackupDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  public 
  JBackupDialog
  (
   Frame owner
  )
  {
    super(owner, "Backup Database");

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];

	{
	  JPathField field = 
	    UIFactory.createTitledPathField
            (tpanel, "Backup Directory:", sTSize, 
             vpanel, new Path("/usr/tmp"), sVSize, 
             "The directory local to each of the Master, Queue and Plugin Manager servers " + 
             "where the database backup files should be stored.");
	  pDirectoryField = field;
	}
      }

      super.initUI("Backup Database:", body, "Start Backup", null, null, "Cancel");
      pack();
    }  
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the backup directory path.
   */ 
  public Path
  getDirectory()
  {
    return pDirectoryField.getPath();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7519458735292435233L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The backup directory path.
   */ 
  private JPathField  pDirectoryField; 

}
