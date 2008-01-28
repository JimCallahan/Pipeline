// $Id: JToolsetBuilderCollectionPluginsPanel.java,v 1.1 2008/01/28 11:58:50 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.TreeSet;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   B U I L D E R   C O L L E C T I O N   P L U G I N S   P A N E L        */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JToolsetBuilderCollectionPluginsPanel
  extends JBaseToolsetPluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JToolsetBuilderCollectionPluginsPanel
  (
   JManageToolsetsDialog dialog,
   JManageToolsetPluginsDialog parent
  )
  {
    super("Builder Collection", dialog, parent); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get all of the plugins. 
   */ 
  @Override
  protected TripleMap<String,String,VersionID,TreeSet<OsType>>
  getAllPlugins() 
  {
    PluginMgrClient pclient = PluginMgrClient.getInstance();
    return pclient.getBuilderCollections();
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
    return pDialog.getPackageBuilderCollections(pname, pvid);
  }

  /**
   * Reset the layout to the default menu layout.
   */ 
  @Override
  public void 
  defaultLayout() 
    throws PipelineException
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    PluginMenuLayout layout = client.getBuilderCollectionMenuLayout();
    setLayout(pToolsetName, layout);
    updateDefault(layout);
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
    MasterMgrClient client = master.getMasterMgrClient();
    client.setBuilderCollectionMenuLayout(getLayout(pToolsetName));
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   */ 
  @Override
  protected PluginMenuLayout
  getLayout
  (
   String tname
  )
    throws PipelineException
  {
    return pDialog.getToolsetBuilderCollections(tname);
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
  protected void
  setLayout
  (
   String tname, 
   PluginMenuLayout layout
  )
    throws PipelineException
  {
    pDialog.setToolsetBuilderCollections(tname, layout);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 342178177445209699L;


}
