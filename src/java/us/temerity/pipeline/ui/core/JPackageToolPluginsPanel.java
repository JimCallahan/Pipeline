// $Id: JPackageToolPluginsPanel.java,v 1.5 2006/10/23 11:30:20 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   T O O L   P L U G I N S   P A N E L                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the tool plugins associated with a toolset package .
 */ 
public 
class JPackageToolPluginsPanel
  extends JBasePackagePluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JPackageToolPluginsPanel
  (
   JManageToolsetsDialog parent
  )
  {
    super("Tool"); 
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
    updateHelper(pname, vid, includedPlugins, pclient.getTools(), privileges);
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
  protected PluginSet
  getPlugins
  (
   String pname, 
   VersionID vid 
  )
    throws PipelineException
  {
    return pParent.getPackageTools(pname, vid);
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
  protected void
  setPlugins
  (
   String pname, 
   VersionID vid, 
   PluginSet plugins
  )
    throws PipelineException
  {
    pParent.setPackageTools(pname, vid, plugins);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4928292596082877924L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageToolsetsDialog  pParent;
  
}
