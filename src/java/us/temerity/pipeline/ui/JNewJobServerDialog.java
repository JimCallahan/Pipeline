// $Id: JNewJobServerDialog.java,v 1.1 2004/07/28 19:22:50 jim Exp $

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
/*   N E W   J O B   S E R V E R   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the hostname of a new job server.
 */ 
public 
class JNewJobServerDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewJobServerDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Job Server", "New Job Server:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3210770426896576891L;


}
