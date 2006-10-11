// $Id: JPackageQueueExtPluginsPanel.java,v 1.1 2006/10/11 22:45:41 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   Q U E U E   E X T   P L U G I N S   P A N E L                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Edits the queue extensions plugins associated with a toolset package.
 */ 
public 
class JPackageQueueExtPluginsPanel
  extends JBasePackagePluginsPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel
   */ 
  public 
  JPackageQueueExtPluginsPanel
  (
   JManageToolsetsDialog parent
  )
  {
    super("Queue Extension"); 
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
    DoubleMap<String,String,TreeSet<VersionID>> includedPlugins = getPlugins(pname, vid);
    
    PluginMgrClient pclient = PluginMgrClient.getInstance();
    updateHelper(pname, vid, includedPlugins, pclient.getQueueExts(), privileges);
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
  protected DoubleMap<String,String,TreeSet<VersionID>>
  getPlugins
  (
   String pname, 
   VersionID vid 
  )
    throws PipelineException
  {
    return pParent.getPackageQueueExts(pname, vid);
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
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  )
    throws PipelineException
  {
    pParent.setPackageQueueExts(pname, vid, plugins);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5964006957854211918L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageToolsetsDialog  pParent;
  
}
