// $Id: JManagePackagePluginsDialog.java,v 1.13 2010/01/08 20:42:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.event.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   P A C K A G E   P L U G I N S   D I A L O G                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The edits the plugins associated with toolset packages.
 */ 
public 
class JManagePackagePluginsDialog
  extends JTopLevelDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public
  JManagePackagePluginsDialog
  (
   JManageToolsetsDialog parent
  ) 
  {
    super("Manage Package Plugins");

    pPrivilegeDetails = new PrivilegeDetails();

    /* create dialog body components */ 
    {
      pParent = parent;

      pPluginPanels = new ArrayList<JBasePackagePluginsPanel>();
      pPluginPanels.add(new JPackageEditorPluginsPanel(parent));
      pPluginPanels.add(new JPackageComparatorPluginsPanel(parent));
      pPluginPanels.add(new JPackageActionPluginsPanel(parent));
      pPluginPanels.add(new JPackageToolPluginsPanel(parent));
      pPluginPanels.add(new JPackageAnnotationPluginsPanel(parent));
      pPluginPanels.add(new JPackageArchiverPluginsPanel(parent));
      pPluginPanels.add(new JPackageMasterExtPluginsPanel(parent));
      pPluginPanels.add(new JPackageQueueExtPluginsPanel(parent));
      pPluginPanels.add(new JPackageKeyChooserPluginsPanel(parent));
      pPluginPanels.add(new JPackageBuilderCollectionPluginsPanel(parent));
      
      JTabbedPanel tab = new JTabbedPanel();
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	tab.addTab(panel.getTitle(), panel);
      
      super.initUI("", tab, "Confirm", "Apply", null, "Close", null);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the currently displayed package.
   */ 
  public String
  getPackageName()
  {
    return pPackageName;
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
  {
    PluginMgrClient pclient = PluginMgrClient.getInstance();
    try {
      if(vid == null)
        setHeader("Package Plugins:  " + pname + " (working)");
      else
        setHeader("Package Plugins:  " + pname + " (v" + vid + ")");
      
      pPackageName = pname;
      
      if(privileges != null) 
	pPrivilegeDetails = privileges;

      pclient.update();     
      pParent.cacheFrozenPlugins(pname, vid);
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.update(pname, vid, pPrivilegeDetails);

      pConfirmButton.setEnabled(pPrivilegeDetails.isDeveloper());
      pApplyButton.setEnabled(pPrivilegeDetails.isDeveloper());
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      setVisible(false);
    }
  }

  /**
   * Copy the plugins associated with the given frozen package to initialize the plugins
   * associated with the working package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param vid
   *   The revision number of the frozen package.
   */ 
  public void 
  clone
  (
   String pname, 
   VersionID vid
  )
  {
    try {
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.clone(pname, vid);
      update(pname, null, null);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      setVisible(false);
    }
  }

  /**
   * Save the plugins associated with the given frozen package from copying them from the
   * plugins associated with the working package. 
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param vid
   *   The revision number of the frozen package.
   */ 
  public void 
  freeze
  (
   String pname,
   VersionID vid
  )
  {
    try {
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.freeze(pname, vid);
      update(pname, vid, null);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      setVisible(false);
    }
  }

  /**
   * Remove the plugins associated with the given working package.
   * 
   * @param pname
   *   The name of the toolset package.
   */ 
  public void 
  remove
  (
   String pname
  )
  {
    try {
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.remove(pname);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      setVisible(false);
    }
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  @Override
  public void 
  doConfirm()
  {
    doApply();
    super.doConfirm();
  }

  /**
   * Apply changes and continue. 
   */ 
  @Override
  public void 
  doApply()
  {
    try {
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.saveChanges();

      pParent.updateDialogs();
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8503182874046102580L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The master toolsets dialog.
   */ 
  private JManageToolsetsDialog  pParent;
  
  /**
   * The name of the currently displayed package.
   */ 
  private String pPackageName;

  /**
   * The panels for each plugin type.
   */
  private ArrayList<JBasePackagePluginsPanel>  pPluginPanels; 


}
