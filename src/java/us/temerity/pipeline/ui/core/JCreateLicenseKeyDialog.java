// $Id: JCreateLicenseKeyDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

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
/*   C R E A T E   L I C E N S E   K E Y   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new license key.
 */ 
public 
class JCreateLicenseKeyDialog
  extends JBaseCreateKeyDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCreateLicenseKeyDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New License Key");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2332080552601949420L;


}
