// $Id: JManageToolsDialog.java,v 1.1 2005/01/05 09:44:31 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   E D I T O R S   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the layout of the editor plugin selection menu. 
 */ 
public 
class JManageToolsDialog
  extends JBaseManagePluginsDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageToolsDialog() 
  {
    super("Manage Tools");
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

      PluginMenuLayout layout = client.getToolMenuLayout(); 
      TreeMap<String,TreeSet<VersionID>> plugins = PluginMgr.getInstance().getTools();

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
      client.setToolMenuLayout(layout); 
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5308774884448103282L;
  
}
