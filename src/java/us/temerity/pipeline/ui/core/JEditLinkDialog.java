// $Id: JEditLinkDialog.java,v 1.2 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*; 

import javax.swing.*;
import java.awt.Frame;

/*------------------------------------------------------------------------------------------*/
/*   E D I T   L I N K   D I A L O G                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Quieries the user for information needed to edit an existing link between nodes.
 */ 
public 
class JEditLinkDialog
  extends JBaseLinkDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public
  JEditLinkDialog
  (
   Frame owner
  ) 
  {
    super(owner, "Edit Link", "Edit Node Link:", "Confirm");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update the UI components.
   */ 
  public void 
  updateLink
  (
   LinkMod link 
  ) 
  {
    super.updateLink(link.getPolicy(), link.getRelationship(), link.getFrameOffset());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1201919194354685391L;

}
