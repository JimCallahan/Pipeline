// $Id: JEditLinkDialog.java,v 1.1 2004/06/28 23:39:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import javax.swing.*;

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
   */ 
  public
  JEditLinkDialog() 
  {
    super("Edit Link", "Edit Node Link:", "Confirm");
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
    super.updateLink(link.getCatagory(), link.getRelationship(), link.getFrameOffset());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1201919194354685391L;

}
