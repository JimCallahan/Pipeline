// $Id: JNewViewDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

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
/*   N E W   V I E W   D I A L O G                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for a view name.
 */ 
public 
class JNewViewDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewViewDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New View", "New Working Area View Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1280727590963185951L;

}
