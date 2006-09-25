// $Id: JNewPackageDialog.java,v 1.2 2006/09/25 12:11:44 jim Exp $

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
/*   N E W   P A C K A G E   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for a new package name.
 */ 
public 
class JNewPackageDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new dialog.
   */ 
  public 
  JNewPackageDialog
  (
   Frame owner        
  )
  {
    super(owner, "New Package", "New Package Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3322369972207854665L;


}
