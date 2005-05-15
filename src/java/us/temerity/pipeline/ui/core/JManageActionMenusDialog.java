// $Id: JManageActionMenusDialog.java,v 1.1 2005/05/15 19:45:35 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   A C T I O N   M E N U S   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the layout of the {@link BaseAction BaseAction} plugin selection menu. 
 */ 
public 
class JManageActionMenusDialog
  extends JBaseManagePluginsDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageActionMenusDialog() 
  {
    super("Manage Action Menus"); 
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
      PluginMenuLayout layout = client.getActionMenuLayout(); 

      PluginMgrClient pclient = PluginMgrClient.getInstance();
      pclient.update();
      TreeMap<String,TreeSet<VersionID>> plugins = pclient.getActions();

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
      client.setActionMenuLayout(layout); 
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2963299182259808775L;
  
}
