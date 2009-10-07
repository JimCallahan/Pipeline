// $Id: JChangeJobReqsDialog.java,v 1.4 2009/10/07 08:09:50 jim Exp $

package us.temerity.pipeline.ui.core;

import javax.swing.JFrame;

/*------------------------------------------------------------------------------------------*/
/*   C H A N G E   J O B S   R E Q S   D I A L O G                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The job reqs change parameters dialog. 
 *
 */
public 
class JChangeJobReqsDialog
  extends JBaseJobReqsDialog
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
  JChangeJobReqsDialog
  (
    JFrame parent  
  )
  {
    super(parent, "Change Job Reqs", false);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8768242796930167950L;
}
