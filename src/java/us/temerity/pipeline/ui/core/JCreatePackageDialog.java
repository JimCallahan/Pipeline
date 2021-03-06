// $Id: JCreatePackageDialog.java,v 1.2 2006/09/25 12:11:44 jim Exp $

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
/*   C R E A T E   P A C K A G E   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the revision number and description of a new toolset package.
 */ 
public 
class JCreatePackageDialog
  extends JBaseCreateDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCreatePackageDialog
  (
   Frame owner        
  )
  {
    super(owner, "New Package", "Add", false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7081749170466309279L;

}
