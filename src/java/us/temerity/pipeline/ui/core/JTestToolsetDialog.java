// $Id: JTestToolsetDialog.java,v 1.2 2005/06/13 16:05:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.toolset.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   T O O L S E T   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JTestToolsetDialog
  extends JTestEnvironmentDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JTestToolsetDialog() 
  {
    super("Test Toolset");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the toolset currently displayed.
   */ 
  public Toolset
  getToolset() 
  {
    return pToolset;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the underlying toolset and UI components.
   * 
   * @param tset
   *   The toolset
   */ 
  public void 
  updateToolset
  (
   Toolset tset
  )
  { 
    pToolset = tset;

    if(pToolset != null) {
      String header = null;
      if(pToolset.isFrozen()) 
	header = ("Test " + PackageInfo.sOsType + " Toolset:  " + pToolset.getName());
      else 
	header = ("Test " + PackageInfo.sOsType + " Toolset:  " + pToolset.getName() + 
		  " (working)");

      updateEnvironment(header, pToolset.getEnvironment(PackageInfo.sUser, "default"));
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1463764204984328195L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The toolset.
   */ 
  private Toolset  pToolset;

}
