// $Id: JManageComparatorsDialog.java,v 1.1 2005/01/07 07:11:05 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   C O M P A R A T O R S   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the layout of the comparator plugin selection menu. 
 */ 
public 
class JManageComparatorsDialog
  extends JBaseManagePluginsDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageComparatorsDialog() 
  {
    super("Manage Comparators"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the plugin menu layout.
   */ 
  public void 
  updateMenuLayout() 
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      boolean isPrivileged = client.isPrivileged(false);

      PluginMenuLayout layout = client.getComparatorMenuLayout(); 
      TreeMap<String,TreeSet<VersionID>> plugins = PluginMgr.getInstance().getComparators();

      updateMenuLayout(layout, plugins, isPrivileged);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      setVisible(false);
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and continue. 
   */ 
  public void 
  doApply()
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      PluginMenuLayout layout = getMenuLayout();
      client.setComparatorMenuLayout(layout); 
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7619854267798491593L;
  
}
