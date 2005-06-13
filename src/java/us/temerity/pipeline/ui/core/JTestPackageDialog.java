// $Id: JTestPackageDialog.java,v 1.2 2005/06/13 16:05:01 jim Exp $

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
/*   T E S T   P A C K A G E   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JTestPackageDialog
  extends JTestEnvironmentDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JTestPackageDialog() 
  {
    super("Test Package");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the underlying package and UI components.
   * 
   * @param com
   *   The package.
   */ 
  public void 
  updatePackage
  (
   PackageCommon com
  )
  { 
    if(com != null) {
      String header = null;
      if(com instanceof PackageMod) 
	header = ("Test " + PackageInfo.sOsType + " Package:  " + com.getName());
      else if(com instanceof PackageVersion) 
	header = ("Test " + PackageInfo.sOsType + " Package:  " + com.getName() + 
		  " (v" + ((PackageVersion) com).getVersionID() + ")");

      updateEnvironment(header, com.getEnvironment(PackageInfo.sUser, "default"));
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2007445037961869833L;

}
