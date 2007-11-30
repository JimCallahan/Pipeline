// $Id: JCreateHardwareKeyDialog.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Frame;

/*------------------------------------------------------------------------------------------*/
/*   C R E A T E   H A R D W A R E   K E Y   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the name and description of a new hardware key.
 */ 
public 
class JCreateHardwareKeyDialog
  extends JBaseCreateKeyDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCreateHardwareKeyDialog
  (
   Frame owner        
  )
  {
    super(owner, "New Hardware Key");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 698479128688252116L;

}
