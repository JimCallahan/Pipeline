// $Id: JPackageEditorPluginsPanel.java,v 1.3 2006/01/15 06:29:26 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   E D I T O R   P L U G I N S   P A N E L                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the editor plugins associated with a toolset package .
 */ 
public 
class JPackageEditorPluginsPanel
  extends JBasePackagePluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JPackageEditorPluginsPanel
  (
   JManageToolsetsDialog parent
  )
  {
    super("Editor"); 
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
   * @param os
   *   The package operating system.
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
   OsType os,
   VersionID vid,
   PrivilegeDetails privileges
  ) 
    throws PipelineException
  {
    DoubleMap<String,String,TreeSet<VersionID>> includedPlugins = getPlugins(pname, os, vid);
    
    PluginMgrClient pclient = PluginMgrClient.getInstance();
    pclient.update();
      
    updateHelper(pname, os, vid, includedPlugins, pclient.getEditors(), privileges);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugins associated with a toolset package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param os
   *   The package operating system.
   * 
   * @param vid
   *   The revision number of the package or <CODE>null</CODE> for working package.
   */ 
  protected DoubleMap<String,String,TreeSet<VersionID>>
  getPlugins
  (
   String pname, 
   OsType os, 
   VersionID vid 
  )
    throws PipelineException
  {
    return pParent.getPackageEditors(pname, os, vid);
  }

  /**
   * Set the plugins associated with a toolset package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param os
   *   The package operating system.
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
   OsType os, 
   VersionID vid, 
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  )
    throws PipelineException
  {
    pParent.setPackageEditors(pname, os, vid, plugins);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2771682805409699537L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageToolsetsDialog  pParent;
  
}
