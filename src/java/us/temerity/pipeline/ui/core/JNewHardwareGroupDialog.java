// $Id: JNewHardwareGroupDialog.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Frame;

import us.temerity.pipeline.ui.JNewIdentifierDialog;

/*------------------------------------------------------------------------------------------*/
/*   N E W   H A R D W A R E   G R O U P   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new hardware group.
 */ 
public 
class JNewHardwareGroupDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewHardwareGroupDialog
  (
   Frame owner        
  )
  {
    super(owner, "New Hardware Group", "New Group Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6447516117002298311L;

}
