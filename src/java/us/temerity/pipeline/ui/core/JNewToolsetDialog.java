// $Id: JNewToolsetDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

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
/*   N E W   T O O L S E T   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for a new toolset name.
 */ 
public 
class JNewToolsetDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewToolsetDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Toolset", "New Toolset Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4266429198416658721L;


}
