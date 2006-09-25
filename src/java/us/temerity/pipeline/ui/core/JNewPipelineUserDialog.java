// $Id: JNewPipelineUserDialog.java,v 1.2 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   N E W   U S E R   D I A L O G                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries for a user name.
 */ 
public 
class JNewPipelineUserDialog
  extends JNewIdentifierDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNewPipelineUserDialog
  (
   Frame owner        
  )
  {
    super(owner, "New Pipeline User", "New User Name:", null, "Add");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8542303007517261649L;

}
