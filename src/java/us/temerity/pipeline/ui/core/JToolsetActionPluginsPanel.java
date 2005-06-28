// $Id: JToolsetActionPluginsPanel.java,v 1.1 2005/06/28 18:05:22 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.toolset.*; 
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   A C T I O N   P L U G I N S   P A N E L                                */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JToolsetActionPluginsPanel
  extends JBaseToolsetPluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JToolsetActionPluginsPanel
  (
   JManageToolsetsDialog dialog,
   JManageToolsetPluginsDialog parent
  )
  {
    super("Action", dialog, parent); 
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
    TreeMap<String,TreeSet<VersionID>> plugins = new TreeMap<String,TreeSet<VersionID>>();
    {
      int wk;
      for(wk=0; wk<toolset.getNumPackages(); wk++) {
	String pname = toolset.getPackageName(wk);
	VersionID pvid = toolset.getPackageVersionID(wk);
	
	TreeMap<String,TreeSet<VersionID>> table = 
	  pDialog.getPackageActions(pname, os, pvid);

	for(String name : table.keySet()) {
	  TreeSet<VersionID> vids = plugins.get(name);
	  if(vids == null) {
	    vids = new TreeSet<VersionID>();
	    plugins.put(name, vids);
	  }
	  
	  vids.addAll(table.get(name));
	}
      }
    }

    String tname = toolset.getName();      
    updateHelper(tname, os, getLayout(tname, os), plugins, isPrivileged);
  }

  /**
   * Replace with the default menu layout.
   */ 
  public void 
  setDefault() 
    throws PipelineException
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    PluginMenuLayout layout = client.getActionMenuLayout(pToolsetOsType);
    setLayout(pToolsetName, pToolsetOsType, layout);
    updateDefault(layout);
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
    return pDialog.getToolsetActions(tname, os);
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
  protected void
  setLayout
  (
   String tname, 
   OsType os, 
   PluginMenuLayout layout
  )
    throws PipelineException
  {
    pDialog.setToolsetActions(tname, os, layout);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6224774382039341101L;


}
