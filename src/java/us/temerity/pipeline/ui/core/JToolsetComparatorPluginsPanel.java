// $Id: JToolsetComparatorPluginsPanel.java,v 1.5 2006/05/07 21:30:14 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.toolset.*; 
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   C O M P A R A T O R   P L U G I N S   P A N E L                        */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JToolsetComparatorPluginsPanel
  extends JBaseToolsetPluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JToolsetComparatorPluginsPanel
  (
   JManageToolsetsDialog dialog,
   JManageToolsetPluginsDialog parent
  )
  {
    super("Comparator", dialog, parent); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the all of the plugins. 
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param pvid
   *   The version number of the package.
   */ 
  protected TripleMap<String,String,VersionID,TreeSet<OsType>>
  getAllPlugins() 
    throws PipelineException
  {
    PluginMgrClient pclient = PluginMgrClient.getInstance();
    return pclient.getComparators();
  }

  /**
   * Get the plugins associated with the given toolset package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param pvid
   *   The version number of the package.
   */ 
  protected DoubleMap<String,String,TreeSet<VersionID>> 
  getPackagePlugins
  (
   String pname, 
   VersionID pvid
  ) 
    throws PipelineException
  {
    return pDialog.getPackageComparators(pname, pvid);
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
    PluginMenuLayout layout = client.getComparatorMenuLayout();
    setLayout(pToolsetName, layout);
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
    client.setComparatorMenuLayout(getLayout(pToolsetName));
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   */ 
  protected  PluginMenuLayout
  getLayout
  (
   String tname
  )
    throws PipelineException
  {
    return pDialog.getToolsetComparators(tname);
  }

  /**
   * Set the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param layout
   *   The plugin menu layout.
   */ 
  protected  void
  setLayout
  (
   String tname, 
   PluginMenuLayout layout
  )
    throws PipelineException
  {
    pDialog.setToolsetComparators(tname, layout);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1467038965338132380L;


}
