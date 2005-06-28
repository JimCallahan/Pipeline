// $Id: JPackageComparatorPluginsPanel.java,v 1.1 2005/06/28 18:05:22 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   C O M P A R A T O R   P L U G I N S   P A N E L                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the comparator plugins associated with a toolset package .
 */ 
public 
class JPackageComparatorPluginsPanel
  extends JBasePackagePluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JPackageComparatorPluginsPanel
  (
   JManageToolsetsDialog parent
  )
  {
    super("Comparator"); 
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
   * @param isPrivileged
   *   Whether the current user is privileged.
   */ 
  public void 
  update
  (
   String pname, 
   OsType os,
   VersionID vid,
   boolean isPrivileged
  ) 
    throws PipelineException
  {
    TreeMap<String,TreeSet<VersionID>> includedPlugins = getPlugins(pname, os, vid);
    
    PluginMgrClient pclient = PluginMgrClient.getInstance();
    pclient.update();
      
    updateHelper(pname, os, vid, includedPlugins, pclient.getComparators(), isPrivileged);
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
  protected TreeMap<String,TreeSet<VersionID>>
  getPlugins
  (
   String pname, 
   OsType os, 
   VersionID vid 
  )
    throws PipelineException
  {
    return pParent.getPackageComparators(pname, os, vid);
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
   *   The names and revision numbers of the plugins or <CODE>null</CODE> to remove.
   */
  protected void
  setPlugins
  (
   String pname, 
   OsType os, 
   VersionID vid, 
   TreeMap<String,TreeSet<VersionID>> plugins
  )
    throws PipelineException
  {
    pParent.setPackageComparators(pname, os, vid, plugins);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7055441412737237098L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageToolsetsDialog  pParent;
  
}
