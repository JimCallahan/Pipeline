// $Id: JManagePackagePluginsDialog.java,v 1.2 2005/09/07 21:11:17 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 
import us.temerity.pipeline.math.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   P A C K A G E   P L U G I N S   D I A L O G                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The edits the plugins associated with toolset packages.
 */ 
public 
class JManagePackagePluginsDialog
  extends JBaseDialog
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
    super("Manage Package Plugins", false);

    /* create dialog body components */ 
    {
      pParent = parent;

      pPluginPanels = new ArrayList<JBasePackagePluginsPanel>();
      pPluginPanels.add(new JPackageEditorPluginsPanel(parent));
      pPluginPanels.add(new JPackageComparatorPluginsPanel(parent));
      pPluginPanels.add(new JPackageActionPluginsPanel(parent));
      pPluginPanels.add(new JPackageToolPluginsPanel(parent));
      pPluginPanels.add(new JPackageArchiverPluginsPanel(parent));
      
      JTabbedPanel tab = new JTabbedPanel();
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	tab.add(panel);
      
      super.initUI("", false, tab, "Confirm", "Apply", null, "Close");
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
   * @param os
   *   The package operating system.
   * 
   * @param vid
   *   The revision number of the package or <CODE>null</CODE> for working package.
   */ 
  public void 
  update
  (
   String pname, 
   OsType os,
   VersionID vid
  )
  {
    if(vid == null)
      pHeaderLabel.setText(os + " Package Plugins:  " + pname + " (working)");
    else
      pHeaderLabel.setText(os + " Package Plugins:  " + pname + " (v" + vid + ")");

    pPackageName = pname;

    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      boolean isPrivileged = client.isPrivileged(false);

      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.update(pname, os, vid, isPrivileged);

      pConfirmButton.setEnabled(isPrivileged);
      pApplyButton.setEnabled(isPrivileged);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
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
   * @param os
   *   The package operating system.
   * 
   * @param vid
   *   The revision number of the frozen package.
   */ 
  public void 
  clone
  (
   String pname, 
   OsType os,
   VersionID vid
  )
  {
    try {
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.clone(pname, os, vid);
      update(pname, os, null);
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
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
   * @param os
   *   The package operating system.
   * 
   * @param vid
   *   The revision number of the frozen package.
   */ 
  public void 
  freeze
  (
   String pname, 
   OsType os,
   VersionID vid
  )
  {
    try {
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.freeze(pname, os, vid);
      update(pname, os, vid);
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
      setVisible(false);
    }
  }

  /**
   * Remove the plugins associated with the given working package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param os
   *   The package operating system.
   */ 
  public void 
  remove
  (
   String pname, 
   OsType os
  )
  {
    try {
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.remove(pname, os);
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
      setVisible(false);
    }
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm()
  {
    doApply();
    super.doConfirm();
  }

  /**
   * Apply changes and continue. 
   */ 
  public void 
  doApply()
  {
    try {
      for(JBasePackagePluginsPanel panel : pPluginPanels) 
	panel.saveChanges();

      pParent.updateDialogs();
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
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
