// $Id: JManageToolsetPluginsDialog.java,v 1.2 2005/07/15 02:16:46 jim Exp $

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
  JManageToolsetPluginsDialog
  (
   JManageToolsetsDialog parent
  ) 
  {
    super("Manage Toolset Plugin Menus", false);

    /* create dialog body components */ 
    {
      pParent = parent;

      pPluginPanels = new ArrayList<JBaseToolsetPluginsPanel>();
      pPluginPanels.add(new JToolsetEditorPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetComparatorPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetActionPluginsPanel(parent, this));
      pPluginPanels.add(new JToolsetToolPluginsPanel(parent, this));
      
      pTab = new JTabbedPanel();
      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	pTab.add(panel);
      
      String extra[][] = {
	null,
	{ "Default",      "default" },
	{ "Save Default", "save-default" }
      };

      JButton btns[] = 
	super.initUI("", false, pTab, "Confirm", "Apply", extra, "Close");

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
   * @param os
   *   The toolset operating system.
   */ 
  public void 
  update
  (
   String tname, 
   OsType os
  )
  {
    UIMaster master = UIMaster.getInstance();
    try {
      Toolset toolset = pParent.lookupToolset(tname, os);
      if(toolset == null) 
	throw new PipelineException
	  ("No " + os + " toolset named (" + tname + ") exists!");
      
      pHeaderLabel.setText(os + " Toolset Plugin Menus:  " + tname + 
			   (toolset.isFrozen() ? "" : " (working)"));
      
      pToolsetName = toolset.getName();

      MasterMgrClient client = master.getMasterMgrClient();
      boolean isPrivileged = client.isPrivileged(false);

      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	panel.update(toolset, os, isPrivileged);

      pConfirmButton.setEnabled(isPrivileged);
      pApplyButton.setEnabled(isPrivileged);
      pDefaultButton.setEnabled(isPrivileged);
      pSaveDefaultButton.setEnabled(isPrivileged);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
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
   * 
   * @param os
   *   The package operating system.
   */ 
  public void 
  clone
  (
   String source, 
   String target, 
   OsType os
  )
  {
    try {
      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	panel.clone(source, target, os);
      update(target, os);
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
      setVisible(false);
    }
  }

  /**
   * Remove the plugins associated with the given working toolset.
   * 
   * @param tname
   *   The name of the toolset toolset.
   * 
   * @param os
   *   The toolset operating system.
   */ 
  public void 
  remove
  (
   String tname, 
   OsType os
  )
  {
    try {
      for(JBaseToolsetPluginsPanel panel : pPluginPanels) 
	panel.remove(tname, os);
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
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
      UIMaster.getInstance().showErrorDialog(ex);
    }
  }
  
  /**
   * Reset the layout of the selected tab to the default menu layout.
   */ 
  public 
  void
  doDefaultLayout() 
  {
    try {
      pPluginPanels.get(pTab.getSelectedIndex()).defaultLayout();
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
  }

  /**
   * Save the current menu layout of the selected tab as the default layout.
   */ 
  public 
  void
  doSaveDefaultLayout() 
  {
    try {
      pPluginPanels.get(pTab.getSelectedIndex()).saveDefaultLayout();
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
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
