// $Id: JCreateSelectionGroupDialog.java,v 1.1 2005/12/31 20:40:43 jim Exp $

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
/*   C R E A T E   S E L E C T I O N   G R O U P   D I A L O G                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new selection group.
 */ 
public 
class JCreateSelectionGroupDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCreateSelectionGroupDialog
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
