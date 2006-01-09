// $Id: JNewSelectionGroupDialog.java,v 1.1 2006/01/09 12:05:34 jim Exp $

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
/*   N E W   S E L E C T I O N   G R O U P   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new selection group.
 */ 
public 
class JNewSelectionGroupDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewSelectionGroupDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Selection Group", "New Group Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3413948737235985793L;

}
