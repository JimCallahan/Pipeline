// $Id: JManageComparatorMenusDialog.java,v 1.2 2005/01/15 02:56:32 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   C O M P A R A T O R   M E N U S   D I A L O G                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the layout of the {@link BaseComparator BaseComparator} plugin selection menu. 
 */ 
public 
class JManageComparatorMenusDialog
  extends JBaseManagePluginsDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageComparatorMenusDialog() 
  {
    super("Manage Comparator Menus"); 
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

      PluginMgrClient pclient = PluginMgrClient.getInstance();
      pclient.update();
      TreeMap<String,TreeSet<VersionID>> plugins = pclient.getComparators();

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
