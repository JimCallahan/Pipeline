// $Id: JNewFolderDialog.java,v 1.2 2004/05/12 04:00:36 jim Exp $

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
/*   N E W   F O L D E R   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for a folder name.
 */ 
public 
class JNewFolderDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewFolderDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Folder", "New Folder Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4320568430731398212L;


}
