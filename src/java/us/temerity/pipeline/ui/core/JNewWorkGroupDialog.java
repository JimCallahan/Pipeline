// $Id: JNewWorkGroupDialog.java,v 1.2 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   N E W   W O R K   G R O U P   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name of a new work group.
 */ 
public 
class JNewWorkGroupDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewWorkGroupDialog
  (
   Frame owner        
  )
  {
    super(owner, "New Work Group", "New Group Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4563819308105110698L;

}
