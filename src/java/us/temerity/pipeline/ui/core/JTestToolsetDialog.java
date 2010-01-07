// $Id: JTestToolsetDialog.java,v 1.4 2010/01/07 22:14:34 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.toolset2.*;

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


  /**
   * Get the toolset currently displayed.
   */ 
  public ToolsetCommon
  getToolset2() 
  {
    return pToolset2;
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

      TreeMap<String,String> env = 
	pToolset.getEnvironment(PackageInfo.sUser, "default", PackageInfo.sOsType);

      updateEnvironment(header, env);
    }
  }
  
  /**
   * Update the underlying toolset and UI components.
   * 
   * @param tset
   *   The toolset
   */ 
  public void 
  updateToolset2
  (
    ToolsetCommon tset
  )
  { 
    pToolset2 = tset;

    if(pToolset2 != null) {
      String header = null;
      if(pToolset2.isFrozen()) 
        header = ("Test " + PackageInfo.sMachineType + " Toolset:  " + tset.getName() + 
                  " (v" + tset.getVersion() + ")");
      else 
        header = ("Test " + PackageInfo.sMachineType + " Toolset:  " + tset.getName() + 
                  " (w" + tset.getVersion() + ")");

      TreeMap<String,String> env = 
        pToolset2.getEnvironment(PackageInfo.sUser, "default", PackageInfo.sMachineType);

      updateEnvironment(header, env);
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
  
  private ToolsetCommon pToolset2;

}
