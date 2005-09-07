// $Id: JToolsetEditorPluginsPanel.java,v 1.3 2005/09/07 21:11:17 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.toolset.*; 
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   E D I T O R   P L U G I N S   P A N E L                                */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JToolsetEditorPluginsPanel
  extends JBaseToolsetPluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JToolsetEditorPluginsPanel
  (
   JManageToolsetsDialog dialog,
   JManageToolsetPluginsDialog parent
  )
  {
    super("Editor", dialog, parent); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the the UI components to display the current plugins associated with a 
   * toolset package.
   * 
   * @param toolset
   *   The toolset. 
   * 
   * @param os
   *   The toolset operating system.
   * 
   * @param isPrivileged
   *   Whether the current user is privileged.
   */ 
  public void 
  update
  (
   Toolset toolset, 
   OsType os,
   boolean isPrivileged
  )
    throws PipelineException
  {
    DoubleMap<String,String,TreeSet<VersionID>> plugins = 
      new DoubleMap<String,String,TreeSet<VersionID>>();
    {
      int wk;
      for(wk=0; wk<toolset.getNumPackages(); wk++) {
	String pname = toolset.getPackageName(wk);
	VersionID pvid = toolset.getPackageVersionID(wk);
	
	DoubleMap<String,String,TreeSet<VersionID>> table = 
	  pDialog.getPackageEditors(pname, os, pvid);

	for(String vendor : table.keySet()) {
	  for(String name : table.get(vendor).keySet()) {
	    TreeSet<VersionID> vids = plugins.get(vendor, name);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      plugins.put(vendor, name, vids);
	    }
	  
	    vids.addAll(table.get(vendor, name));
	  }
	}
      }
    }

    String tname = toolset.getName();      
    updateHelper(tname, os, getLayout(tname, os), plugins, isPrivileged);
  }

  /**
   * Reset the layout to the default menu lauyout.
   */ 
  public void 
  defaultLayout() 
    throws PipelineException
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    PluginMenuLayout layout = client.getEditorMenuLayout(pToolsetOsType);
    setLayout(pToolsetName, pToolsetOsType, layout);
    updateDefault(layout);
  }

  /**
   * Save the current menu layout as the default layout.
   */ 
  public void 
  saveDefaultLayout() 
    throws PipelineException
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    client.setEditorMenuLayout(pToolsetOsType, getLayout(pToolsetName, pToolsetOsType));
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param os
   *   The package operating system.
   */ 
  protected  PluginMenuLayout
  getLayout
  (
   String tname, 
   OsType os
  )
    throws PipelineException
  {
    return pDialog.getToolsetEditors(tname, os);
  }

  /**
   * Set the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param os
   *   The package operating system.
   * 
   * @param layout
   *   The plugin menu layout.
   */ 
  protected  void
  setLayout
  (
   String tname, 
   OsType os, 
   PluginMenuLayout layout
  )
    throws PipelineException
  {
    pDialog.setToolsetEditors(tname, os, layout);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4156522356579210043L;


}
