// $Id: JToolsetComparatorPluginsPanel.java,v 1.7 2009/03/19 21:55:59 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import us.temerity.pipeline.*;

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
  @Override
  @SuppressWarnings("unused")
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
  @Override
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
  @Override
  public void 
  defaultLayout() 
    throws PipelineException
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.acquireMasterMgrClient();
    try {
      PluginMenuLayout layout = client.getComparatorMenuLayout();
      setLayout(pToolsetName, layout);
      updateDefault(layout);
    }
    finally {
      master.releaseMasterMgrClient(client);
    }
  }

  /**
   * Save the current menu layout as the default layout.
   */ 
  @Override
  public void 
  saveDefaultLayout() 
    throws PipelineException
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.acquireMasterMgrClient();
    try {
      client.setComparatorMenuLayout(getLayout(pToolsetName));
    }
    finally {
      master.releaseMasterMgrClient(client);
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   */ 
  @Override
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
  @Override
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
