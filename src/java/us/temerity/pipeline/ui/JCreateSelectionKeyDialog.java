// $Id: JCreateSelectionKeyDialog.java,v 1.1 2004/07/25 03:13:17 jim Exp $

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
/*   C R E A T E   S E L E C T I O N   K E Y   D I A L O G                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new selection key.
 */ 
public 
class JCreateSelectionKeyDialog
  extends JBaseCreateKeyDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCreateSelectionKeyDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Selection Key");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8212511846968019522L;


}
