// $Id: JNewEnvVarDialog.java,v 1.1 2004/05/29 06:38:43 jim Exp $

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
/*   N E W   E N V   V A R   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for a new environmental variable name.
 */ 
public 
class JNewEnvVarDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewEnvVarDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Variable", "New Variable Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9129675262927308380L;


}
