// $Id: JManageToolsetPluginsDialog.java,v 1.13 2010/01/08 20:42:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 
import us.temerity.pipeline.toolset.*; 
import us.temerity.pipeline.math.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   T O O L S E T   P L U G I N S   D I A L O G                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The edits the plugin menu layouts associated with toolsets.
 */ 
public 
class JManageToolsetPluginsDialog
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
  JManageToolsetPluginsDialog
  (
   JManageToolsetsDialog parent
  ) 
  {
    super("Manage Toolset Plugin Menus");

    pPrivilegeDetails = new PrivilegeDetails();

    /* create dialog body components */ 
    {
      pParent = parent;

      pPluginPanels = new ArrayList<JBaseToolsetPluginsPanel>();
      pPluginPanels.add(new JToolsetEditorPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetComparatorPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetActionPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetToolPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetAnnotationPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetArchiverPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetMasterExtPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetQueueExtPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetKeyChooserPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetBuilderCollectionPluginsPanel(parent, this));
      
      pTab = new JTabbedPanel();
      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	pTab.addTab(panel.getTitle(), panel);
      
      String extra[][] = {
	null,
	{ "Default",      "default" },
	{ "Save Default", "save-default" }
      };

      JButton btns[] = super.initUI("", pTab, "Confirm", "Apply", extra, "Close", null);

      pDefaultButton     = btns[1];
      pSaveDefaultButton = btns[2];

      pConfirmButton.setToolTipText(UIFactory.formatToolTip
        ("Save the current plugin menu layouts and close the dialog."));
      pApplyButton.setToolTipText(UIFactory.formatToolTip
        ("Save the current plugin menu layouts."));
      pDefaultButton.setToolTipText(UIFactory.formatToolTip 				  
        ("Replace the current plugin menu layout for the selected tab with the default " + 
	 "menu layout for that plugin type."));
      pSaveDefaultButton.setToolTipText(UIFactory.formatToolTip 			   
        ("Save the current plugin menu layout for the selected tab as the default menu " + 
	 "layout for that plugin type."));
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the currently displayed toolset.
   */ 
  public String
  getToolsetName()
  {
    return pToolsetName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the the UI components to display the current plugin menu layouts associated 
   * with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  public void 
  update
  (
   String tname, 
   PrivilegeDetails privileges
  )
  { 
    UIMaster master = UIMaster.getInstance();
    PluginMgrClient pclient = PluginMgrClient.getInstance();
    try {
      Toolset toolset = pParent.lookupToolset(tname, OsType.Unix);
      if(toolset == null) 
	throw new PipelineException
	  ("No toolset named (" + tname + ") exists!");
      
      setHeader("Toolset Plugin Menus:  " + tname + 
		(toolset.isFrozen() ? "" : " (working)"));
      
      pToolsetName = toolset.getName();
      
      if(privileges != null) 
	pPrivilegeDetails = privileges;

      pclient.update();
      pParent.cacheFrozenPluginsAndLayouts(toolset);
      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	panel.update(toolset, pPrivilegeDetails);

      pConfirmButton.setEnabled(pPrivilegeDetails.isDeveloper());
      pApplyButton.setEnabled(pPrivilegeDetails.isDeveloper());
      pDefaultButton.setEnabled(pPrivilegeDetails.isDeveloper());
      pSaveDefaultButton.setEnabled(pPrivilegeDetails.isDeveloper());
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      setVisible(false);
    }
  }

  /**
   * Copy the plugin menus associated with one toolset to initialize the plugin menus 
   * associated with another toolset.
   * 
   * @param source
   *   The name of the source toolset package.
   * 
   * @param target
   *   The name of the target toolset package.
   */ 
  public void 
  clone
  (
   String source, 
   String target
  )
  {
    try {
      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	panel.clone(source, target);
      update(target, null);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      setVisible(false);
    }
  }

  /**
   * Remove the plugins associated with the given working toolset.
   * 
   * @param tname
   *   The name of the toolset toolset.
   */ 
  public void 
  remove
  (
   String tname
  )
  {
    try {
      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	panel.remove(tname);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      setVisible(false);
    }

    if(tname.equals(pToolsetName)) {
      pToolsetName = null;
      setVisible(false);
    }
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("default")) 
      doDefaultLayout();
    else if(cmd.equals("save-default")) 
      doSaveDefaultLayout();
    else 
      super.actionPerformed(e);
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
      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	panel.saveChanges();
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
  }
  
  /**
   * Reset the layout of the selected tab to the default menu layout.
   */ 
  public 
  void
  doDefaultLayout() 
  {
    JConfirmDialog diag = new JConfirmDialog(this, "Are you sure?"); 
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      try {
	pPluginPanels.get(pTab.getSelectedIndex()).defaultLayout();
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
      }
    }
  }

  /**
   * Save the current menu layout of the selected tab as the default layout.
   */ 
  public 
  void
  doSaveDefaultLayout() 
  {
    JConfirmDialog diag = new JConfirmDialog(this, "Are you sure?"); 
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      try {
	pPluginPanels.get(pTab.getSelectedIndex()).saveDefaultLayout();
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
      }
    }
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5321721776120722399L;



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
   * The name of the currently displayed toolset.
   */ 
  private String pToolsetName;

  /**
   * The container of the plugin tabs.
   */ 
  private JTabbedPanel  pTab; 

  /**
   * The panels for each plugin type.
   */
  private ArrayList<JBaseToolsetPluginsPanel>  pPluginPanels; 

  /**
   * Footer buttons.
   */ 
  private JButton  pDefaultButton;
  private JButton  pSaveDefaultButton;

}
