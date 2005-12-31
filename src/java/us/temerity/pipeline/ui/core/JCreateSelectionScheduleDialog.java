// $Id: JCreateSelectionScheduleDialog.java,v 1.1 2005/12/31 20:40:43 jim Exp $

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
/*   C R E A T E   S E L E C T I O N   S C H E D U L E   D I A L O G                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new selection schedule.
 */ 
public 
class JCreateSelectionScheduleDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCreateSelectionScheduleDialog
  (
   Dialog owner        
  )
  {
    super(owner, "New Selection Schedule", "New Schedule Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6310118431973005403L;

}
