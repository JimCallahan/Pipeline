// $Id: JToolsetArchiverPluginsPanel.java,v 1.4 2009/03/19 20:32:28 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   A R C H I V E R   P L U G I N S   P A N E L                            */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JToolsetArchiverPluginsPanel
  extends JBaseToolsetPluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JToolsetArchiverPluginsPanel
  (
   JManageToolsetsDialog dialog,
   JManageToolsetPluginsDialog parent
  )
  {
    super("Archiver", dialog, parent); 
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
    return pclient.getArchivers();
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
    return pDialog.getPackageArchivers(pname, pvid);
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
    MasterMgrClient client = master.leaseMasterMgrClient();
    try {
      PluginMenuLayout layout = client.getArchiverMenuLayout();
      setLayout(pToolsetName, layout);
      updateDefault(layout);
    }
    finally {
      master.returnMasterMgrClient(client);
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
    MasterMgrClient client = master.leaseMasterMgrClient();
    try {
      client.setArchiverMenuLayout(getLayout(pToolsetName));
    }
    finally {
      master.returnMasterMgrClient(client);
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   */ 
  @Override
  protected  PluginMenuLayout
  getLayout
  (
   String tname
  )
    throws PipelineException
  {
    return pDialog.getToolsetArchivers(tname);
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
    pDialog.setToolsetArchivers(tname, layout);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1374074545288637483L;


}
