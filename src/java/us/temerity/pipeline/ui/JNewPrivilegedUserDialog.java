// $Id: JNewPrivilegedUserDialog.java,v 1.1 2004/05/23 20:01:27 jim Exp $

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
/*   N E W   P R I V I L E G E D   U S E R   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name of a new privileged user.
 */ 
public 
class JNewPrivilegedUserDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewPrivilegedUserDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Privileged User", "New Privileged User:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -904524623930359293L;

}
