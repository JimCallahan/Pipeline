// $Id: JCreateLinkDialog.java,v 1.1 2004/06/28 23:39:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   C R E A T E   L I N K   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Quieries the user for information needed to create links between nodes.
 */ 
public 
class JCreateLinkDialog
  extends JBaseLinkDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public
  JCreateLinkDialog() 
  {
    super("Create Link", "Create Node Link:", "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update the UI components.
   */ 
  public void 
  updateLink() 
  {
    super.updateLink(new LinkCatagory("Dependency", LinkPolicy.Both), 
		     LinkRelationship.All, null); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7406236061637673110L;

}
