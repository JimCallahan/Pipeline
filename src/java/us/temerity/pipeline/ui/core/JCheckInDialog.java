// $Id: JCheckInDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K - I N   D I A L O G                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the revision number and description of a new checked-in node version.
 */ 
public 
class JCheckInDialog
  extends JBaseCreateDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCheckInDialog() 
  {
    super("Check-In Node", "Check-In", true);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2236208527410390673L;

}
