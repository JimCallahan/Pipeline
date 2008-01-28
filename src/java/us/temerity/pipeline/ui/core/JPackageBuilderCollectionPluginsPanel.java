// $Id: JPackageBuilderCollectionPluginsPanel.java,v 1.1 2008/01/28 11:58:50 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   B U I L D E R   C O L L E C T I O N   P L U G I N S   P A N E L        */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the builder collection plugins associated with a toolset package.
 */ 
public 
class JPackageBuilderCollectionPluginsPanel
  extends JBasePackagePluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JPackageBuilderCollectionPluginsPanel
  (
   JManageToolsetsDialog parent
  )
  {
    super("Builder Collection"); 
    pParent = parent;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the the UI components to display the current plugins associated with a 
   * toolset package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param vid
   *   The revision number of the package or <CODE>null</CODE> for working package.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  @Override
  public void 
  update
  (
   String pname, 
   VersionID vid,
   PrivilegeDetails privileges
  ) 
    throws PipelineException
  {
    PluginSet includedPlugins = getPlugins(pname, vid);
    
    PluginMgrClient pclient = PluginMgrClient.getInstance();
    updateHelper(pname, vid, includedPlugins, pclient.getBuilderCollections(), privileges);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugins associated with a toolset package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param vid
   *   The revision number of the package or <CODE>null</CODE> for working package.
   */ 
  @Override
  protected PluginSet
  getPlugins
  (
   String pname, 
   VersionID vid 
  )
    throws PipelineException
  {
    return pParent.getPackageBuilderCollections(pname, vid);
  }

  /**
   * Set the plugins associated with a toolset package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param vid
   *   The revision number of the package or <CODE>null</CODE> for working package.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the plugins or <CODE>null</CODE> to remove.
   */
  @Override
  protected void
  setPlugins
  (
   String pname, 
   VersionID vid, 
   PluginSet plugins
  )
    throws PipelineException
  {
    pParent.setPackageBuilderCollections(pname, vid, plugins);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5105445207057087610L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageToolsetsDialog  pParent;
  
}
