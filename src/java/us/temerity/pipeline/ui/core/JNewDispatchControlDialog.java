// $Id: JNewDispatchControlDialog.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;

import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   N E W   D I S P A T C H   C O N T R O L   D I A L O G                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name of a new dispatch control.
 */ 
public 
class JNewDispatchControlDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewDispatchControlDialog
  (
    Frame owner        
  )
  {
    super(owner, "New Dispatch Control", "New Control Name:", null, "Add");
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5333406886894888820L;
}
