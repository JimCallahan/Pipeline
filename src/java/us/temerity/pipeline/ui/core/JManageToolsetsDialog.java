// $Id: JManageToolsetsDialog.java,v 1.13 2006/01/19 13:57:54 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 
import us.temerity.pipeline.toolset.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   T O O L S E T S   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for creating, editing and testing Toolsets and Toolset Packages.
 */ 
public 
class JManageToolsetsDialog
  extends JBaseDialog
  implements ListSelectionListener, TreeSelectionListener, MouseListener, MouseMotionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageToolsetsDialog() 
  {
    super("Manage Toolsets", false);

    /* initialize fields */ 
    {
      pPrivilegeDetails = new PrivilegeDetails();

      pDefaultToolset = null;
      pActiveToolsets = new TreeSet<String>();
      pToolsets       = new TreeMap<String,TreeMap<OsType,Toolset>>();

      pFrozenToolsetEditors     = new ToolsetLayouts();
      pFrozenToolsetComparators = new ToolsetLayouts();
      pFrozenToolsetActions     = new ToolsetLayouts();
      pFrozenToolsetTools       = new ToolsetLayouts();
      pFrozenToolsetArchivers   = new ToolsetLayouts();

      pToolsetEditors     = new ToolsetLayouts();
      pToolsetComparators = new ToolsetLayouts();
      pToolsetActions     = new ToolsetLayouts();
      pToolsetTools       = new ToolsetLayouts();
      pToolsetArchivers   = new ToolsetLayouts();

      pPackageVersions = 
	new TreeMap<String,TreeMap<OsType,TreeMap<VersionID,PackageVersion>>>();
      pPackageMods     = new TreeMap<String,TreeMap<OsType,PackageMod>>();

      pFrozenPackageEditors     = new FrozenPackagePlugins();
      pFrozenPackageComparators = new FrozenPackagePlugins();
      pFrozenPackageActions     = new FrozenPackagePlugins();
      pFrozenPackageTools       = new FrozenPackagePlugins();
      pFrozenPackageArchivers   = new FrozenPackagePlugins();

      pPackageEditors     = new PackagePlugins();
      pPackageComparators = new PackagePlugins();
      pPackageActions     = new PackagePlugins();
      pPackageTools       = new PackagePlugins();
      pPackageArchivers   = new PackagePlugins();
    }

    /* toolsets popup menu */ 
    {
      JMenuItem item;
      
      pActiveToolsetsPopup = new JPopupMenu();  
 
      item = new JMenuItem("Details...");
      pActiveToolsetDetailsItem = item;
      item.setActionCommand("toolset-details");
      item.addActionListener(this);
      pActiveToolsetsPopup.add(item);  

      item = new JMenuItem("Test...");
      pActiveTestToolsetItem = item;
      item.setActionCommand("test-toolset");
      item.addActionListener(this);
      pActiveToolsetsPopup.add(item);   

      item = new JMenuItem("Export...");
      pActiveExportToolsetItem = item;
      item.setActionCommand("export-toolset");
      item.addActionListener(this);
      pActiveToolsetsPopup.add(item);    

      pActiveToolsetsPopup.addSeparator();    

      item = new JMenuItem("Default Toolset");
      pDefaultToolsetItem = item;
      item.setActionCommand("default-toolset");
      item.addActionListener(this);
      pActiveToolsetsPopup.add(item);    
    }

    /* toolsets popup menu */ 
    {
      JMenuItem item;
      
      pToolsetsPopup = new JPopupMenu();  
 
      item = new JMenuItem("Details...");
      pToolsetDetailsItem = item;
      item.setActionCommand("toolset-details");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  

      item = new JMenuItem("Test...");
      pTestToolsetItem = item;
      item.setActionCommand("test-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);        

      item = new JMenuItem("Export...");
      pExportToolsetItem = item;
      item.setActionCommand("export-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);        

      item = new JMenuItem("Plugin Menus...");
      pManagePluginMenusItem = item;
      item.setActionCommand("manage-toolset-plugins");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  

      pToolsetsPopup.addSeparator();
      
      item = new JMenuItem("New Unix Toolset...");
      pNewToolsetItem = item;
      item.setActionCommand("new-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  

      item = new JMenuItem("Clone Toolset...");
      pCloneToolsetItem = item;
      item.setActionCommand("clone-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  

      pToolsetsPopup.addSeparator();

      item = new JMenuItem("Add MacOS Toolset");
      pAddMacOSToolsetItem = item;
      item.setActionCommand("add-mac-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  

      item = new JMenuItem("Add Windows Toolset");
      pAddWindowsToolsetItem = item;
      item.setActionCommand("add-win-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  
   
      pToolsetsPopup.addSeparator();

      item = new JMenuItem("Freeze...");
      pFreezeToolsetItem = item;
      item.setActionCommand("freeze-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  

      item = new JMenuItem("Delete...");
      pDeleteToolsetItem = item;
      item.setActionCommand("delete-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  
    }

    /* included packages popup menu */ 
    {
      JMenuItem item;
      
      pIncludedPackagesPopup = new JPopupMenu();  
  
      item = new JMenuItem("Details...");
      pIncPackageDetailsItem = item;
      item.setActionCommand("package-details");
      item.addActionListener(this);
      pIncludedPackagesPopup.add(item);  

      item = new JMenuItem("Test...");
      pIncTestPackageItem = item;
      item.setActionCommand("test-package");
      item.addActionListener(this);
      pIncludedPackagesPopup.add(item);    

      pIncludedPackagesPopup.addSeparator();

      item = new JMenuItem("Earlier");
      pPackageEarlierItem = item;
      item.setActionCommand("package-earlier");
      item.addActionListener(this);
      pIncludedPackagesPopup.add(item);  

      item = new JMenuItem("Later");
      pPackageLaterItem = item;
      item.setActionCommand("package-later");
      item.addActionListener(this);
      pIncludedPackagesPopup.add(item);  
    }

    /* packages popup menu */ 
    {
      JMenuItem item;
      
      pPackagesPopup = new JPopupMenu();  
 
      item = new JMenuItem("Details...");
      pPackageDetailsItem = item;
      item.setActionCommand("package-details");
      item.addActionListener(this);
      pPackagesPopup.add(item);  

      item = new JMenuItem("Test...");
      pTestPackageItem = item;
      item.setActionCommand("test-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);    

      item = new JMenuItem("Package Plugins...");
      pManagePluginsItem = item;
      item.setActionCommand("manage-package-plugins");
      item.addActionListener(this);
      pPackagesPopup.add(item);  

      pPackagesPopup.addSeparator();

      item = new JMenuItem("New Unix Package...");
      pNewPackageItem = item;
      item.setActionCommand("new-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);  
      
      pPackagesPopup.addSeparator();

      item = new JMenuItem("Add MacOS Package");
      pAddMacOSPackageItem = item;
      item.setActionCommand("add-mac-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);  

      item = new JMenuItem("Add Windows Package");
      pAddWindowsPackageItem = item;
      item.setActionCommand("add-win-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);  
      
      pPackagesPopup.addSeparator();

      item = new JMenuItem("New Package Version...");
      pNewPackageVersionItem = item;
      item.setActionCommand("new-package-version");
      item.addActionListener(this);
      pPackagesPopup.add(item);  
      
      item = new JMenuItem("Clone Package Version...");
      pClonePackageVersionItem = item;
      item.setActionCommand("clone-package-version");
      item.addActionListener(this);
      pPackagesPopup.add(item);  
      
      pPackagesPopup.addSeparator();

      item = new JMenuItem("Freeze...");
      pFreezePackageItem = item;
      item.setActionCommand("freeze-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);  

      item = new JMenuItem("Delete...");
      pDeletePackageItem = item;
      item.setActionCommand("delete-package");
      item.addActionListener(this);
      pPackagesPopup.add(item); 
    }

    /* create dialog body components */ 
    {
      Box left = new Box(BoxLayout.X_AXIS);
      {
	left.add(Box.createRigidArea(new Dimension(20, 0)));

	{
	  Dimension size = new Dimension(sLWidth, sLHeight);
	  JList lst = UIFactory.createListComponents(left, "Active Toolsets:", size);
	  pActiveToolsetsList = lst;
	  lst.setCellRenderer(new JActiveToolsetListCellRenderer(this));
	  lst.addListSelectionListener(this);
	  lst.addMouseListener(this);
	}

	left.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  
	  vbox.add(Box.createVerticalGlue());
	  
	  {
	    JButton btn = new JButton();
	    pEnableToolsetButton = btn;
	    btn.setName("LeftArrowButton");
	    
	    Dimension size = new Dimension(16, 16);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);

	    btn.setActionCommand("enable-toolset");
	    btn.addActionListener(this);
	    
	    vbox.add(btn);
	  } 

	  vbox.add(Box.createRigidArea(new Dimension(0, 16)));
	  
	  {
	    JButton btn = new JButton();
	    pDisableToolsetButton = btn;
	    btn.setName("RightArrowButton");
	    
	    Dimension size = new Dimension(16, 16);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);

	    btn.setActionCommand("disable-toolset");
	    btn.addActionListener(this);
	    
	    vbox.add(btn);
	  } 

	  vbox.add(Box.createVerticalGlue());
	  
	  left.add(vbox);
	}

	left.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));

	  vbox.add(UIFactory.createPanelLabel("All Toolsets:"));
    
	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    ToolsetTreeData data = new ToolsetTreeData();
	    DefaultMutableTreeNode root = new DefaultMutableTreeNode(data, true);
	    DefaultTreeModel model = new DefaultTreeModel(root, true);

	    JTree tree = new JFancyTree(model); 
	    pToolsetsTree = tree;
	    tree.setName("DarkTree");

	    tree.setCellRenderer(new JToolsetTreeCellRenderer(this));
	    tree.getSelectionModel().setSelectionMode
	      (TreeSelectionModel.SINGLE_TREE_SELECTION);
	    tree.setExpandsSelectedPaths(true);

	    tree.addMouseListener(this);
	    tree.addTreeSelectionListener(this);

	    {
	      JScrollPane scroll = new JScrollPane(tree);
	  
	      Dimension size = new Dimension(sLWidth, sLHeight);
	      scroll.setMinimumSize(size);
	      scroll.setPreferredSize(size);
	      
	      scroll.setHorizontalScrollBarPolicy
		(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	      scroll.setVerticalScrollBarPolicy
		(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	      
	      vbox.add(scroll);
	    }

	    vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	    
	    left.add(vbox);
	  }
	}

	left.add(Box.createRigidArea(new Dimension(20, 0)));
      }
      
      Box right = new Box(BoxLayout.X_AXIS);
      {
	right.add(Box.createRigidArea(new Dimension(20, 0)));
	
	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);	
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);
	
	    hbox.add(Box.createRigidArea(new Dimension(4, 0)));
    
	    {
	      JLabel label = new JLabel("Included Packages:");
	      pIncludedPackagesLabel = label;
	      label.setName("PanelLabel");
	      
	      hbox.add(label);
	    }
	    
	    hbox.add(Box.createHorizontalGlue());
	    
	    vbox.add(hbox);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    JList lst = new JList(new DefaultListModel());
	    pIncludedPackagesList = lst;

	    lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    lst.setCellRenderer(new JPackageListCellRenderer(this));
	    
	    lst.addListSelectionListener(this);
	    lst.addMouseListener(this);
	    lst.addMouseMotionListener(this);

	    {
	      JScrollPane scroll = new JScrollPane(lst);
	      
	      scroll.setMinimumSize(new Dimension(150, 150));
	      scroll.setPreferredSize(new Dimension(sLWidth, sLHeight));
	      
	      scroll.setHorizontalScrollBarPolicy
		(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	      scroll.setVerticalScrollBarPolicy
		(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	      
	      vbox.add(scroll);
	    }
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  right.add(vbox);
	}


	right.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  
	  vbox.add(Box.createVerticalGlue());
	  
	  {
	    JButton btn = new JButton();
	    pIncludePackageButton = btn;
	    btn.setName("LeftArrowButton");
	    
	    Dimension size = new Dimension(16, 16);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);

	    btn.setActionCommand("include-package");
	    btn.addActionListener(this);
	    
	    vbox.add(btn);
	  } 

	  vbox.add(Box.createRigidArea(new Dimension(0, 16)));
	  
	  {
	    JButton btn = new JButton();
	    pExcludePackageButton = btn;
	    btn.setName("RightArrowButton");
	    
	    Dimension size = new Dimension(16, 16);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);

	    btn.setActionCommand("exclude-package");
	    btn.addActionListener(this);
	    
	    vbox.add(btn);
	  } 

	  vbox.add(Box.createVerticalGlue());
	  
	  right.add(vbox);
	}

	right.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));

	  vbox.add(UIFactory.createPanelLabel("All Packages:"));
    
	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    PackageTreeData data = new PackageTreeData();	    
	    DefaultMutableTreeNode root = new DefaultMutableTreeNode(data, true);
	    DefaultTreeModel model = new DefaultTreeModel(root, true);

	    JTree tree = new JFancyTree(model); 
	    pPackagesTree = tree;
	    tree.setName("DarkTree");

	    tree.setCellRenderer(new JPackageTreeCellRenderer());
	    tree.getSelectionModel().setSelectionMode
	      (TreeSelectionModel.SINGLE_TREE_SELECTION);
	    tree.setExpandsSelectedPaths(true);

	    tree.addMouseListener(this);
	    tree.addTreeSelectionListener(this);

	    {
	      JScrollPane scroll = new JScrollPane(tree);
	  
	      Dimension size = new Dimension(sLWidth, sLHeight);
	      scroll.setMinimumSize(size);
	      scroll.setPreferredSize(size);
	      
	      scroll.setHorizontalScrollBarPolicy
		(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	      scroll.setVerticalScrollBarPolicy
		(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	      
	      vbox.add(scroll);
	    }

	    vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	    
	    right.add(vbox);
	  }
	}

	right.add(Box.createRigidArea(new Dimension(20, 0)));
      }

      JHorzSplitPanel body = new JHorzSplitPanel(left, right);
      body.setMinimumSize(body.getPreferredSize());

      super.initUI("Manage Toolsets:", false, body, null, null, null, "Close");
    }

    pPackageDetailsDialog = new JPackageDetailsDialog(this);
    pCreatePackageDialog  = new JCreatePackageDialog(this);
    pTestPackageDialog    = new JTestPackageDialog();

    pToolsetDetailsDialog = new JToolsetDetailsDialog(this);
    pCreateToolsetDialog  = new JCreateToolsetDialog(this);
    pTestToolsetDialog    = new JTestToolsetDialog();

    pExportToolsetDialog = 
      new JFileSelectDialog(this, "Export Toolset", "Export Toolset Script:", 
			    "Export As:", 64, "Export");
    pExportToolsetDialog.updateTargetFile(null);

    pToolsetPluginsDialog = new JManageToolsetPluginsDialog(this);
    pPackagePluginsDialog = new JManagePackagePluginsDialog(this);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the toolset with the given name and operating system.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   */ 
  public Toolset
  lookupToolset
  (
   String tname, 
   OsType os
  ) 
  {
    if((tname != null) && (os != null)) {
      TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
      if(toolsets != null) {
	if(toolsets.containsKey(os)) {
	  Toolset toolset = toolsets.get(os);
	  if(toolset == null) {
	    UIMaster master = UIMaster.getInstance();
	    try {
	      MasterMgrClient client = master.getMasterMgrClient();
	      toolset = client.getToolset(tname, os);
	      toolsets.put(os, toolset);
	    }
	    catch(PipelineException ex) {
	      master.showErrorDialog(ex);
	    }
	  }

	  return toolset; 
	}
      }
    }

    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the editor plugin menu associated with the given tookset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   */ 
  public PluginMenuLayout
  getToolsetEditors
  (
   String tname, 
   OsType os
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");
       
    if(!toolset.isFrozen()) {
      return pToolsetEditors.get(tname, os);
    }
    else {
      if(!pFrozenToolsetEditors.isCached(tname, os)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenToolsetEditors.put(tname, os, 
				  client.getEditorMenuLayout(tname, os));
      }
      
      return pFrozenToolsetEditors.get(tname, os);
    }
  }

  /**
   * Set the editor plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetEditors
  ( 
   String tname, 
   OsType os, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");

    if(!toolset.isFrozen()) {
      pToolsetEditors.put(tname, os, layout);
    }
    else {
      pFrozenToolsetEditors.put(tname, os, layout);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setEditorMenuLayout(tname, os, layout);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the comparator plugin menu associated with the given tookset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   */ 
  public PluginMenuLayout
  getToolsetComparators
  (
   String tname, 
   OsType os
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");
       
    if(!toolset.isFrozen()) {
      return pToolsetComparators.get(tname, os);
    }
    else {
      if(!pFrozenToolsetComparators.isCached(tname, os)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenToolsetComparators.put(tname, os, 
				      client.getComparatorMenuLayout(tname, os));
      }
      
      return pFrozenToolsetComparators.get(tname, os);      
    }
  }

  /**
   * Set the comparator plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetComparators
  ( 
   String tname, 
   OsType os, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");

    if(!toolset.isFrozen()) {
      pToolsetComparators.put(tname, os, layout);
    }
    else {
      pFrozenToolsetComparators.put(tname, os, layout);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setComparatorMenuLayout(tname, os, layout);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the action plugin menu associated with the given tookset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   */ 
  public PluginMenuLayout
  getToolsetActions
  (
   String tname, 
   OsType os
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");
       
    if(!toolset.isFrozen()) {
      return pToolsetActions.get(tname, os);
    }
    else {
      if(!pFrozenToolsetActions.isCached(tname, os)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenToolsetActions.put(tname, os, 
				  client.getActionMenuLayout(tname, os));
      }
      
      return pFrozenToolsetActions.get(tname, os);
    }
  }

  /**
   * Set the action plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetActions
  ( 
   String tname, 
   OsType os, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");

    if(!toolset.isFrozen()) {
      pToolsetActions.put(tname, os, layout);
    }
    else {
      pFrozenToolsetActions.put(tname, os, layout);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setActionMenuLayout(tname, os, layout);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the tool plugin menu associated with the given tookset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   */ 
  public PluginMenuLayout
  getToolsetTools
  (
   String tname, 
   OsType os
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");
       
    if(!toolset.isFrozen()) {
      return pToolsetTools.get(tname, os);
    }
    else {
      if(!pFrozenToolsetTools.isCached(tname, os)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenToolsetTools.put(tname, os, 
				client.getToolMenuLayout(tname, os));
      }
      
      return pFrozenToolsetTools.get(tname, os);
    }
  }

  /**
   * Set the tool plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetTools
  ( 
   String tname, 
   OsType os, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");

    if(!toolset.isFrozen()) {
      pToolsetTools.put(tname, os, layout);
    }
    else {
      pFrozenToolsetTools.put(tname, os, layout);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setToolMenuLayout(tname, os, layout);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the archiver plugin menu associated with the given tookset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   */ 
  public PluginMenuLayout
  getToolsetArchivers
  (
   String tname, 
   OsType os
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");
       
    if(!toolset.isFrozen()) {
      return pToolsetArchivers.get(tname, os);
    }
    else {
      if(!pFrozenToolsetArchivers.isCached(tname, os)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenToolsetArchivers.put(tname, os, 
				  client.getArchiverMenuLayout(tname, os));
      }
      
      return pFrozenToolsetArchivers.get(tname, os);
    }
  }

  /**
   * Set the archiver plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetArchivers
  ( 
   String tname, 
   OsType os, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, os);
    if(toolset == null) 
      throw new PipelineException
	("No " + os + " toolset named (" + tname + ") exists!");

    if(!toolset.isFrozen()) {
      pToolsetArchivers.put(tname, os, layout);
    }
    else {
      pFrozenToolsetArchivers.put(tname, os, layout);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setArchiverMenuLayout(tname, os, layout);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the package with the given name, operating system and revision number.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public PackageCommon
  lookupPackage
  (
   String pname, 
   OsType os, 
   VersionID vid
  ) 
  {
    if((pname != null) && (os != null)) {
      if(vid != null) {
	TreeMap<OsType,TreeMap<VersionID,PackageVersion>> packages = 
	  pPackageVersions.get(pname);
	if(packages != null) {
	  if(packages.containsKey(os)) {
	    TreeMap<VersionID,PackageVersion> versions = packages.get(os);
	    if(versions != null) {
	      if(versions.containsKey(vid)) {
		PackageVersion pkg = versions.get(vid); 
		if(pkg == null) {
		  UIMaster master = UIMaster.getInstance();
		  MasterMgrClient client = master.getMasterMgrClient();
		  try {
		    pkg = client.getToolsetPackage(pname, vid, os);
		    versions.put(vid, pkg);
		  }
		  catch(PipelineException ex) {
		    master.showErrorDialog(ex);
		  }
		}

		return pkg;
	      }
	    }
	  }
	}
      }
      else {
	TreeMap<OsType,PackageMod> packages = pPackageMods.get(pname);
	if(packages != null) 
	  return packages.get(os);
      }
    }

    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether any packages for the given operating system and name exist.
   */ 
  public boolean
  hasPackages
  (
   String pname, 
   OsType os
  ) 
  {
    if(pPackageVersions.containsKey(pname) && 
       pPackageVersions.get(pname).containsKey(os))
      return true;

    if(pPackageMods.containsKey(pname) && 
       pPackageMods.get(pname).containsKey(os))
      return true;

    return false;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the editor plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public DoubleMap<String,String,TreeSet<VersionID>>
  getPackageEditors
  (
   String pname, 
   OsType os, 
   VersionID vid
  ) 
    throws PipelineException
  {
    if(vid == null) {
      return pPackageEditors.get(pname, os);
    }
    else {
      if(!pFrozenPackageEditors.isCached(pname, os, vid)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenPackageEditors.put(pname, os, vid, 
				  client.getPackageEditorPlugins(pname, os, vid));
      }

      return pFrozenPackageEditors.get(pname, os, vid);
    }
  }

  /**
   * Set the editor plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the plugins.
   */ 
  public void
  setPackageEditors
  ( 
   String pname, 
   OsType os, 
   VersionID vid,
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackageEditors.put(pname, os, plugins);
    }
    else {
      pFrozenPackageEditors.put(pname, os, vid, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageEditorPlugins(pname, os, vid, plugins);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the comparator plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public DoubleMap<String,String,TreeSet<VersionID>>
  getPackageComparators
  (
   String pname, 
   OsType os, 
   VersionID vid
  ) 
    throws PipelineException
  {
    if(vid == null) {
      return pPackageComparators.get(pname, os);
    }
    else {
      if(!pFrozenPackageComparators.isCached(pname, os, vid)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenPackageComparators.put(pname, os, vid, 
				  client.getPackageComparatorPlugins(pname, os, vid));
      }

      return pFrozenPackageComparators.get(pname, os, vid);
    }
  }

  /**
   * Set the comparator plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the plugins.
   */ 
  public void
  setPackageComparators
  ( 
   String pname, 
   OsType os, 
   VersionID vid,
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackageComparators.put(pname, os, plugins);
    }
    else {
      pFrozenPackageComparators.put(pname, os, vid, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageComparatorPlugins(pname, os, vid, plugins);
    }    
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the action plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public DoubleMap<String,String,TreeSet<VersionID>>
  getPackageActions
  (
   String pname, 
   OsType os, 
   VersionID vid
  ) 
    throws PipelineException
  {
    if(vid == null) {
      return pPackageActions.get(pname, os);
    }
    else {
      if(!pFrozenPackageActions.isCached(pname, os, vid)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenPackageActions.put(pname, os, vid, 
				  client.getPackageActionPlugins(pname, os, vid));
      }

      return pFrozenPackageActions.get(pname, os, vid);
    }
  }

  /**
   * Set the action plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the plugins.
   */ 
  public void
  setPackageActions
  ( 
   String pname, 
   OsType os, 
   VersionID vid,
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackageActions.put(pname, os, plugins);
    }
    else {
      pFrozenPackageActions.put(pname, os, vid, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageActionPlugins(pname, os, vid, plugins);
    }    
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the tool plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public DoubleMap<String,String,TreeSet<VersionID>>
  getPackageTools
  (
   String pname, 
   OsType os, 
   VersionID vid
  ) 
    throws PipelineException
  {
    if(vid == null) {
      return pPackageTools.get(pname, os);
    }
    else {
      if(!pFrozenPackageTools.isCached(pname, os, vid)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenPackageTools.put(pname, os, vid, 
				  client.getPackageToolPlugins(pname, os, vid));
      }

      return pFrozenPackageTools.get(pname, os, vid);
    }
  }

  /**
   * Set the tool plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the plugins.
   */ 
  public void
  setPackageTools
  ( 
   String pname, 
   OsType os, 
   VersionID vid,
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackageTools.put(pname, os, plugins);
    }
    else {
      pFrozenPackageTools.put(pname, os, vid, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageToolPlugins(pname, os, vid, plugins);
    }    
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the archiver plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public DoubleMap<String,String,TreeSet<VersionID>>
  getPackageArchivers
  (
   String pname, 
   OsType os, 
   VersionID vid
  ) 
    throws PipelineException
  {
    if(vid == null) {
      return pPackageArchivers.get(pname, os);
    }
    else {
      if(!pFrozenPackageArchivers.isCached(pname, os, vid)) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pFrozenPackageArchivers.put(pname, os, vid, 
				  client.getPackageArchiverPlugins(pname, os, vid));
      }

      return pFrozenPackageArchivers.get(pname, os, vid);
    }
  }

  /**
   * Set the archiver plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the plugins.
   */ 
  public void
  setPackageArchivers
  ( 
   String pname, 
   OsType os, 
   VersionID vid,
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackageArchivers.put(pname, os, plugins);
    }
    else {
      pFrozenPackageArchivers.put(pname, os, vid, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageArchiverPlugins(pname, os, vid, plugins);
    }    
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I V E   T O O L S E T S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Is the given toolset name the default toolset.
   * 
   * @param name
   *   The toolset name.
   */ 
  public boolean
  isDefaultToolset
  (
   String name
  )
  {
    return ((name != null) && name.equals(pDefaultToolset));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the currently selected active toolset. 
   */ 
  public String
  getSelectedActiveToolsetName() 
  {
    return ((String) pActiveToolsetsList.getSelectedValue());
  }

  /**
   * Get the currently selected active toolset. 
   */ 
  public Toolset
  getSelectedActiveToolset() 
  {
    String tname = getSelectedActiveToolsetName();
    if(tname != null) 
      return lookupToolset(tname, OsType.Unix);

    return null;
  }

  /**
   * Select the active toolset node with the given name.
   */ 
  public void 
  selectActiveToolset
  (
   String tname
  ) 
  {
    pDisableToolsetButton.setEnabled(false);
    if(tname != null) {
      pActiveToolsetsList.setSelectedValue(tname, true);
      pDisableToolsetButton.setEnabled(pPrivilegeDetails.isMasterAdmin());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A L L   T O O L S E T S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the toolset data associated with the currently selected tree node. 
   */ 
  public ToolsetTreeData
  getSelectedToolsetData() 
  {
    TreePath tpath = pToolsetsTree.getSelectionPath();
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      return (ToolsetTreeData) tnode.getUserObject();
    }

    return null;
  }

  /**
   * Get the currently selected toolset. 
   */ 
  public Toolset
  getSelectedToolset() 
  {
    ToolsetTreeData data = getSelectedToolsetData();
    if(data != null) 
      return lookupToolset(data.getName(), data.getOsType());

    return null;
  }

  /**
   * Select the toolset node with the given data.
   */ 
  public void 
  selectToolset
  (
   ToolsetTreeData selected
  ) 
  {
    pEnableToolsetButton.setEnabled(false);
    if(selected == null) 
      return;

    DefaultTreeModel model = (DefaultTreeModel) pToolsetsTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode nnode = (DefaultMutableTreeNode) root.getChildAt(wk); 
      ToolsetTreeData ndata = (ToolsetTreeData) nnode.getUserObject();
 
      if(ndata.getName().equals(selected.getName())) {	  
	int nk;
	for(nk=0; nk<nnode.getChildCount(); nk++) {
	  DefaultMutableTreeNode onode = (DefaultMutableTreeNode) nnode.getChildAt(nk); 
	  ToolsetTreeData odata = (ToolsetTreeData) onode.getUserObject();
	  if(odata.equals(selected)) {
	    pToolsetsTree.setSelectionPath(new TreePath(onode.getPath()));

	    Toolset toolset = lookupToolset(odata.getName(), odata.getOsType());
	    pEnableToolsetButton.setEnabled
	      (pPrivilegeDetails.isMasterAdmin() && (odata.getOsType() == OsType.Unix) && 
	       (toolset != null) && toolset.isFrozen());

	    return;
	  }
	}
      }
    }
  }

  /*
   * Select the toolset node with the given name and operating system.
   */ 
  public void 
  selectToolset
  (
   String tname, 
   OsType os
  ) 
  {
    if(tname == null) 
      return;

    DefaultTreeModel model = (DefaultTreeModel) pToolsetsTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode nnode = (DefaultMutableTreeNode) root.getChildAt(wk); 
      ToolsetTreeData ndata = (ToolsetTreeData) nnode.getUserObject();
 
      if(ndata.getName().equals(tname)) {
	int nk;
	for(nk=0; nk<nnode.getChildCount(); nk++) {
	  DefaultMutableTreeNode onode = (DefaultMutableTreeNode) nnode.getChildAt(nk); 
	  ToolsetTreeData odata = (ToolsetTreeData) onode.getUserObject();
	  if(odata.getOsType().equals(os)) {
	    TreePath tpath = new TreePath(onode.getPath());
	    pToolsetsTree.setSelectionPath(tpath);
	    pToolsetsTree.scrollPathToVisible(tpath);
	    return;
	  }
	}
      }
    }
  }

 
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the names of the expanded toolset nodes.
   */ 
  public TreeSet<String> 
  getExpandedToolsets() 
  {
    TreeSet<String> tnames = new TreeSet<String>();

    DefaultTreeModel tmodel = (DefaultTreeModel) pToolsetsTree.getModel();
    Enumeration<TreePath> e = 
      pToolsetsTree.getExpandedDescendants(new TreePath(tmodel.getRoot()));
    if(e != null) {
      while(e.hasMoreElements()) {
	TreePath tpath = e.nextElement();
	DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
	ToolsetTreeData tdata = (ToolsetTreeData) tnode.getUserObject();
	if(tdata.getName() != null) 
	  tnames.add(tdata.getName());
      }
    }

    return tnames;
  }

  /** 
   * Expand the toolset nodes with the given names.
   */ 
  public void 
  expandToolsets
  (
   TreeSet<String> expanded
  ) 
  { 
    DefaultTreeModel model = (DefaultTreeModel) pToolsetsTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) root.getChildAt(wk); 
      ToolsetTreeData data = (ToolsetTreeData) tnode.getUserObject();
      if(expanded.contains(data.getName()))
 	pToolsetsTree.expandPath(new TreePath(tnode.getPath()));
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N C L U D E D   P A C K A G E S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the package data associatd with the currently selected included package.
   */ 
  public PackageListData
  getSelectedIncludedPackageData() 
  {
    return ((PackageListData) pIncludedPackagesList.getSelectedValue());
  }

  /**
   * Get the currently selected included package.
   */ 
  public PackageCommon
  getSelectedIncludedPackage() 
  {
    PackageListData data = getSelectedIncludedPackageData();
    if(data != null) 
      return lookupPackage(data.getName(), data.getOsType(), data.getVersionID());

    return null;
  }

  /**
   * Select the included package.
   */ 
  public void 
  selectIncludedPackage
  (
   PackageListData selected
  ) 
  {
    if(selected == null) 
      return;
    
    pIncludedPackagesList.setSelectedValue(selected, true);
  }

  /**
   * Get the index of the current drag selection.
   */ 
  public int 
  getIncludedPackageDragIndex() 
  {
    return pDragCurrentIdx;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A L L   P A C K A G E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the package data associated with the currently selected tree node. 
   */ 
  public PackageTreeData
  getSelectedPackageData() 
  {
    TreePath tpath = pPackagesTree.getSelectionPath();
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      return (PackageTreeData) tnode.getUserObject();
    }

    return null;
  }

  /**
   * Get the currently selected package. 
   */ 
  public PackageCommon
  getSelectedPackage() 
  {
    PackageTreeData data = getSelectedPackageData();
    if(data != null) 
      return lookupPackage(data.getName(), data.getOsType(), data.getVersionID());

    return null;
  }

  /**
   * Select the package node with the given data.
   */ 
  public void 
  selectPackage
  (
   PackageTreeData selected
  ) 
  {
    if(selected == null) 
      return;    

    DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode nnode = (DefaultMutableTreeNode) root.getChildAt(wk); 
      PackageTreeData ndata = (PackageTreeData) nnode.getUserObject();

      if(ndata.getName().equals(selected.getName())) {	  
	int nk;
	for(nk=0; nk<nnode.getChildCount(); nk++) {
	  DefaultMutableTreeNode onode = (DefaultMutableTreeNode) nnode.getChildAt(nk); 
	  PackageTreeData odata = (PackageTreeData) onode.getUserObject();

	  if(odata.getOsType().equals(selected.getOsType())) {  
	    int vk;
	    for(vk=0; vk<onode.getChildCount(); vk++) {
	      DefaultMutableTreeNode vnode = (DefaultMutableTreeNode) onode.getChildAt(vk); 
	      PackageTreeData vdata = (PackageTreeData) vnode.getUserObject();
	      if(vdata.equals(selected)) {
		pPackagesTree.setSelectionPath(new TreePath(vnode.getPath()));
		return;
	      }
	    }
	  }
	}
      }
    }
  }

  /**
   * Select the package with the given name, operating system and revision number.
   */ 
  public void 
  selectPackage
  (
   String pname,
   OsType os, 
   VersionID vid
  ) 
  {
    if(pname == null)
      return; 

    DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode nnode = (DefaultMutableTreeNode) root.getChildAt(wk); 
      PackageTreeData ndata = (PackageTreeData) nnode.getUserObject();

      if(ndata.getName().equals(pname)) {	  
	int nk;
	for(nk=0; nk<nnode.getChildCount(); nk++) {
	  DefaultMutableTreeNode onode = (DefaultMutableTreeNode) nnode.getChildAt(nk); 
	  PackageTreeData odata = (PackageTreeData) onode.getUserObject();

	  if(odata.getOsType().equals(os)) {
	    int vk;
	    for(vk=0; vk<onode.getChildCount(); vk++) {
	      DefaultMutableTreeNode vnode = (DefaultMutableTreeNode) onode.getChildAt(vk); 
	      PackageTreeData vdata = (PackageTreeData) vnode.getUserObject();
	      if(((vid == null) && (vdata.getVersionID() == null)) || 
		 ((vid != null) && (vdata.getVersionID().equals(vid)))) {
		TreePath tpath = new TreePath(vnode.getPath());
		pPackagesTree.setSelectionPath(tpath);
		pPackagesTree.scrollPathToVisible(tpath);
		return;
	      }
	    }
	  }
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the names and operating systems of the expanded package nodes.  
   */ 
  public TreeMap<String,TreeSet<OsType>> 
  getExpandedPackages() 
  {
    TreeMap<String,TreeSet<OsType>> packages = new TreeMap<String,TreeSet<OsType>>();

    DefaultTreeModel tmodel = (DefaultTreeModel) pPackagesTree.getModel();
    Enumeration<TreePath> e = 
      pPackagesTree.getExpandedDescendants(new TreePath(tmodel.getRoot()));
    if(e != null) {
      while(e.hasMoreElements()) {
	TreePath tpath = e.nextElement();
	DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
	PackageTreeData data = (PackageTreeData) tnode.getUserObject();

	if(data.getName() != null) {
	  TreeSet<OsType> oss = packages.get(data.getName());
	  if(oss == null) {
	    oss = new TreeSet<OsType>();
	    packages.put(data.getName(), oss);
	  }
	  
	  if(data.getOsType() != null) 
	    oss.add(data.getOsType());
	}
      }
    }

    return packages;
  }

  /** 
   * Expand the toolset nodes with the given names.
   */ 
  public void 
  expandPackages
  (
    TreeMap<String,TreeSet<OsType>> expanded
  ) 
  { 
    DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode nnode = (DefaultMutableTreeNode) root.getChildAt(wk); 
      PackageTreeData ndata = (PackageTreeData) nnode.getUserObject();
      
      if(expanded.containsKey(ndata.getName())) {
	pPackagesTree.expandPath(new TreePath(nnode.getPath()));

	TreeSet<OsType> oss = expanded.get(ndata.getName());
	int nk;
	for(nk=0; nk<nnode.getChildCount(); nk++) {
	  DefaultMutableTreeNode onode = (DefaultMutableTreeNode) nnode.getChildAt(nk); 
	  PackageTreeData odata = (PackageTreeData) onode.getUserObject();
      
	  if(oss.contains(odata.getOsType())) 
	    pPackagesTree.expandPath(new TreePath(onode.getPath()));
	}
      }				     
    }
  }
   
  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the package test dialog for the given package.
   */
  public void 
  showTestPackageDialog
  (
   PackageCommon com 
  ) 
  {
    pTestPackageDialog.updatePackage(com);
    pTestPackageDialog.setVisible(true);
  }

  /**
   * Show the toolset test dialog for the given toolset.
   */
  public void 
  showTestToolsetDialog
  (
   Toolset toolset
  ) 
  {
    pTestToolsetDialog.updateToolset(toolset);
    pTestToolsetDialog.setVisible(true);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Replace the value of a the given package, updating all working toolsets which contain
   * the package and the UI components which depend of the package and updated toolsets.
   */ 
  public void 
  refreshPackage
  (
   OsType os, 
   PackageMod pkg
  ) 
  {
    if(pkg == null) 
      return;

    String rname = pkg.getName();
    pPackageMods.get(rname).put(os, pkg);

    {
      ArrayList<String> rebuild = new ArrayList<String>();
      for(String tname : pToolsets.keySet()) {
	Toolset tset = pToolsets.get(tname).get(os);
 	if((tset != null) && tset.hasModifiablePackage(rname)) 
 	  rebuild.add(tset.getName());
      }

      for(String tname : rebuild) {
 	Toolset tset = pToolsets.get(tname).get(os);

 	ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
 	{
 	  int wk;
 	  for(wk=0; wk<tset.getNumPackages(); wk++) {
 	    String pname  = tset.getPackageName(wk);
 	    VersionID vid = tset.getPackageVersionID(wk);
	    packages.add(lookupPackage(pname, os, vid));
 	  }
 	}
	
 	updateToolset(os, new Toolset(tname, packages));
      }
    }

    updateAll();
    updateDialogs();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update all components.
   */ 
  public void 
  updateAll()
  { 
    /* update the toolset/package table keys from the server */ 
    {
      pDefaultToolset = null;
      pActiveToolsets.clear();
      
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      try {
	pDefaultToolset = client.getDefaultToolsetName();
      }
      catch(PipelineException ex){
      }

      try {
	pPrivilegeDetails = client.getPrivilegeDetails();
	pActiveToolsets.addAll(client.getActiveToolsetNames());
      
	{
	  TreeMap<String,TreeSet<OsType>> tnames = 
	    client.getAllToolsetNames();

	  for(String tname : tnames.keySet()) {
	    TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	    if(toolsets == null) {
	      toolsets = new TreeMap<OsType,Toolset>();
	      pToolsets.put(tname, toolsets);
	    }

	    for(OsType os : tnames.get(tname)) {
	      if(!toolsets.containsKey(os)) 
		toolsets.put(os, null);
	    }
	  }
	}

	{
	  TreeMap<String,TreeMap<OsType,TreeSet<VersionID>>> pnames = 
	    client.getAllToolsetPackageNames();
	
	  for(String pname : pnames.keySet()) {
	    TreeMap<OsType,TreeMap<VersionID,PackageVersion>> versions = 
	      pPackageVersions.get(pname);
	    if(versions == null) {
	      versions = new TreeMap<OsType,TreeMap<VersionID,PackageVersion>>();
	      pPackageVersions.put(pname, versions);
	    }

	    for(OsType os : pnames.get(pname).keySet()) {
	      TreeMap<VersionID,PackageVersion> packages = versions.get(os);
	      if(packages == null) {
		packages = new TreeMap<VersionID,PackageVersion>();
		versions.put(os, packages);
	      }

	      for(VersionID vid : pnames.get(pname).get(os)) {
		if(!packages.containsKey(vid)) 
		  packages.put(vid, null);
	      }
	    }
	  }
	}
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	setVisible(false);
	return;      
      }
    }

    /* rebuild active toolset list */ 
    {
      String selected = getSelectedActiveToolsetName();

      pActiveToolsetsList.removeListSelectionListener(this);
      {
	DefaultListModel model = (DefaultListModel) pActiveToolsetsList.getModel();
	model.clear();

	for(String name : pActiveToolsets) 
	  model.addElement(name);

	selectActiveToolset(selected);
      }
      pActiveToolsetsList.addListSelectionListener(this);      
    }
    
    /* rebuild all toolsets tree */ 
    {	
      ToolsetTreeData selected = getSelectedToolsetData();
      TreeSet<String> expanded = getExpandedToolsets();
      
      pToolsetsTree.removeTreeSelectionListener(this);
      {
	{
	  DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ToolsetTreeData());
	  for(String tname : pToolsets.keySet()) {
	    DefaultMutableTreeNode nnode = 
	      new DefaultMutableTreeNode(new ToolsetTreeData(tname), true);
	    root.add(nnode);
	    
	    for(OsType os : pToolsets.get(tname).keySet()) {
	      DefaultMutableTreeNode onode = 
		new DefaultMutableTreeNode(new ToolsetTreeData(tname, os), false);
	      nnode.add(onode);
	    }
	  }
	  
	  DefaultTreeModel model = (DefaultTreeModel) pToolsetsTree.getModel();
	  model.setRoot(root);
	}

	selectToolset(selected);
	expandToolsets(expanded);
      }
      pToolsetsTree.addTreeSelectionListener(this);
    }

    /* rebuild the included packages list */  
    rebuildIncludedPackagesList();

    /* rebuild all packages tree */ 
    {
      PackageTreeData selected = getSelectedPackageData();
      TreeMap<String,TreeSet<OsType>> expanded = getExpandedPackages(); 

      pPackagesTree.removeTreeSelectionListener(this);
      {
	{
	  DefaultMutableTreeNode root = new DefaultMutableTreeNode(new PackageTreeData());

	  TreeSet<String> pnames = new TreeSet<String>();
	  pnames.addAll(pPackageVersions.keySet());
	  pnames.addAll(pPackageMods.keySet());

	  for(String pname : pnames) {
	    DefaultMutableTreeNode nnode = 
	      new DefaultMutableTreeNode(new PackageTreeData(pname), true);
	    root.add(nnode);

	    TreeSet<OsType> oss = new TreeSet<OsType>();
	    if(pPackageVersions.containsKey(pname)) 
	      oss.addAll(pPackageVersions.get(pname).keySet());
	    if(pPackageMods.containsKey(pname)) 
	      oss.addAll(pPackageMods.get(pname).keySet());

	    for(OsType os : oss) {
	      DefaultMutableTreeNode onode = 
		new DefaultMutableTreeNode(new PackageTreeData(pname, os), true);
	      nnode.add(onode);

	      if(pPackageVersions.containsKey(pname) && 
		 pPackageVersions.get(pname).containsKey(os)) {
		for(VersionID vid : pPackageVersions.get(pname).get(os).keySet()) {
		  DefaultMutableTreeNode vnode = 
		    new DefaultMutableTreeNode(new PackageTreeData(pname, os, vid), false);
		  onode.add(vnode);
		}
	      }

	      if(pPackageMods.containsKey(pname) && 
		 pPackageMods.get(pname).containsKey(os)) {
		DefaultMutableTreeNode vnode = 
		  new DefaultMutableTreeNode(new PackageTreeData(pname, os, null), false);
		onode.add(vnode);
	      }
	    }
	  }

	  DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
	  model.setRoot(root);
	}
	
	selectPackage(selected);
	expandPackages(expanded);
      }
      pPackagesTree.addTreeSelectionListener(this);
    }

    /* update the package buttons */ 
    {
      updateIncludePackageButton();
      updateExcludePackageButton();
    }
  }

  /**
   * Rebuild the included packages list.
   */ 
  private void 
  rebuildIncludedPackagesList() 
  {
    pIncludedPackagesList.removeListSelectionListener(this);
    {
      DefaultListModel model = (DefaultListModel) pIncludedPackagesList.getModel();
      
      /* get the currmently selected package */ 
      PackageListData selected = getSelectedIncludedPackageData();

      /* rebuild the package list */ 
      {
	model.clear();
	
	OsType os = OsType.Unix;
	Toolset toolset = getSelectedActiveToolset();
	if(toolset == null) {
	  ToolsetTreeData data = getSelectedToolsetData();
	  if(data != null) {
	    os = data.getOsType();
	    toolset = lookupToolset(data.getName(), os);
	  }
	}

	if(toolset != null) {
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname  = toolset.getPackageName(wk); 
	    VersionID vid = toolset.getPackageVersionID(wk); 
	    PackageCommon pkg = lookupPackage(pname, os, vid);
	      
	    model.addElement(new PackageListData(pname, os, vid));
	  }
	}
      }
      
      /* reselect the package */ 
      if(selected != null) {
	int wk; 
	for(wk=0; wk<model.getSize(); wk++) {
	  PackageListData data = (PackageListData) model.getElementAt(wk);
	  if(data.equals(selected)) {
	    pIncludedPackagesList.setSelectedIndex(wk);
	    break;
	  }
	}
      }
    }
    pIncludedPackagesList.addListSelectionListener(this);
  }

  /**
   * Update the active toolsets menu.
   * 
   * @param idx
   *   The index of the list element under the mouse.
   */ 
  private void 
  updateActiveToolsetsMenu
  (
   int idx
  ) 
  {
    pActiveToolsetDetailsItem.setEnabled(false);
    pActiveTestToolsetItem.setEnabled(false);
    pActiveExportToolsetItem.setEnabled(false);
    pDefaultToolsetItem.setEnabled(false);

    if(idx != -1) {
      pActiveToolsetDetailsItem.setEnabled(true);
      pActiveTestToolsetItem.setEnabled(PackageInfo.sOsType.equals(OsType.Unix));
      pActiveExportToolsetItem.setEnabled(true);

      String tname = (String) pActiveToolsetsList.getModel().getElementAt(idx);
      if((tname != null) && !tname.equals(pDefaultToolset))
	pDefaultToolsetItem.setEnabled(pPrivilegeDetails.isMasterAdmin());

      pActiveToolsetsList.setSelectedIndex(idx);
    }
  }

  /**
   * Update the all toolsets menu.
   * 
   * @param tpath
   *   The path of the tree node under the mouse.
   */ 
  private void
  updateToolsetsMenu
  (
   TreePath tpath
  ) 
  {
    pToolsetDetailsItem.setEnabled(false);
    pTestToolsetItem.setEnabled(false);
    pExportToolsetItem.setEnabled(false);
    pNewToolsetItem.setEnabled(pPrivilegeDetails.isMasterAdmin());
    pAddMacOSToolsetItem.setEnabled(false);
    pAddWindowsToolsetItem.setEnabled(false);
    pCloneToolsetItem.setEnabled(false);
    pFreezeToolsetItem.setEnabled(false);
    pDeleteToolsetItem.setEnabled(false);
    pManagePluginMenusItem.setEnabled(false);

    if(tpath != null) {
      pToolsetsTree.addSelectionPath(tpath);

      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      ToolsetTreeData data = (ToolsetTreeData) tnode.getUserObject();
      Toolset toolset = lookupToolset(data.getName(), data.getOsType());
      if(toolset != null) {
	pToolsetDetailsItem.setEnabled(true);
	pExportToolsetItem.setEnabled(true);
	pTestToolsetItem.setEnabled(data.getOsType().equals(PackageInfo.sOsType));

	if(!toolset.isFrozen()) {
	  pFreezeToolsetItem.setEnabled(pPrivilegeDetails.isMasterAdmin());
	  pDeleteToolsetItem.setEnabled(pPrivilegeDetails.isMasterAdmin());
	}

	pManagePluginMenusItem.setEnabled(true);
      }

      if((data != null) && (data.getName() != null)) {
	TreeMap<OsType,Toolset> toolsets = pToolsets.get(data.getName());
	if(toolsets != null) {
	  pAddMacOSToolsetItem.setEnabled
	    (pPrivilegeDetails.isMasterAdmin() && !toolsets.containsKey(OsType.MacOS));
	  pAddWindowsToolsetItem.setEnabled
	    (pPrivilegeDetails.isMasterAdmin() && !toolsets.containsKey(OsType.Windows));
	  pCloneToolsetItem.setEnabled(pPrivilegeDetails.isMasterAdmin());
	}
      }
    }
  }

  /**
   * Update the included packages menu.
   *
   * @param idx
   *   The index of the list element under the mouse.
   */ 
  private void 
  updateIncludedPackagesMenu
  (
   int idx
  ) 
  {
    pIncludedPackagesList.clearSelection();
    pPackagesTree.clearSelection();

    pIncPackageDetailsItem.setEnabled(false);
    pIncTestPackageItem.setEnabled(false);
    pPackageEarlierItem.setEnabled(false);
    pPackageLaterItem.setEnabled(false);

    if(idx != -1) {
      pIncPackageDetailsItem.setEnabled(true);

      pIncludedPackagesList.setSelectedIndex(idx);
      
      Toolset toolset = getSelectedToolset();
      if(toolset != null) {
	ToolsetTreeData data = getSelectedToolsetData();      
	pIncTestPackageItem.setEnabled(data.getOsType().equals(PackageInfo.sOsType));

	if(!toolset.isFrozen()) {
	  pPackageEarlierItem.setEnabled(pPrivilegeDetails.isMasterAdmin() && (idx > 0));
	  
	  DefaultListModel model = (DefaultListModel) pIncludedPackagesList.getModel();
	  pPackageLaterItem.setEnabled(pPrivilegeDetails.isMasterAdmin() && 
				       (idx < (model.getSize()-1)));
	}
      }
    }
  }

  /**
   * Update the all packages menu.
   *
   * @param tpath
   *   The path of the tree node under the mouse.
   */ 
  private void 
  updatePackagesMenu
  (
   TreePath tpath
  ) 
  { 
    pIncludedPackagesList.clearSelection();
    pPackagesTree.clearSelection();

    pPackageDetailsItem.setEnabled(false);
    pTestPackageItem.setEnabled(false);
    pNewPackageItem.setEnabled(pPrivilegeDetails.isMasterAdmin());
    pAddMacOSPackageItem.setEnabled(false);
    pAddWindowsPackageItem.setEnabled(false);
    pNewPackageVersionItem.setEnabled(false);
    pClonePackageVersionItem.setEnabled(false);
    pFreezePackageItem.setEnabled(false);
    pDeletePackageItem.setEnabled(false);
    pManagePluginsItem.setEnabled(false);

    if(tpath != null) {

      pPackagesTree.addSelectionPath(tpath);

      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      PackageTreeData data = (PackageTreeData) tnode.getUserObject();
      if(data.isPackage()) {
	pPackageDetailsItem.setEnabled(true);
	pTestPackageItem.setEnabled(data.getOsType().equals(PackageInfo.sOsType));

	if(data.getVersionID() == null) {
	  pFreezePackageItem.setEnabled(pPrivilegeDetails.isMasterAdmin());
	  pDeletePackageItem.setEnabled(pPrivilegeDetails.isMasterAdmin());
	}
	else {
	  pClonePackageVersionItem.setEnabled(pPrivilegeDetails.isMasterAdmin());
	}

	pManagePluginsItem.setEnabled(true);
      }
      
      if((data != null) && (data.getName() != null)) {
	pAddMacOSPackageItem.setEnabled
	  (pPrivilegeDetails.isMasterAdmin() && !hasPackages(data.getName(), OsType.MacOS));
	pAddWindowsPackageItem.setEnabled
	  (pPrivilegeDetails.isMasterAdmin() && !hasPackages(data.getName(), OsType.Windows));
	pNewPackageVersionItem.setEnabled
	  (pPrivilegeDetails.isMasterAdmin() && (data.getOsType() != null));
      }
    }
  }


  /**
   * Update the enabled status of the include packages button.
   */ 
  private void 
  updateIncludePackageButton() 
  {
    pIncludePackageButton.setEnabled(false);

    if(pPrivilegeDetails.isMasterAdmin()) {
      ToolsetTreeData tdata = getSelectedToolsetData();
      PackageTreeData pdata = getSelectedPackageData();
      if((tdata != null) && (pdata != null)) {
	Toolset toolset = lookupToolset(tdata.getName(), tdata.getOsType());
	
	if((toolset != null) && !toolset.isFrozen() && pdata.isPackage() && 
	   tdata.getOsType().equals(pdata.getOsType())) 
	  pIncludePackageButton.setEnabled(true);
      }
    }
  }

  /**
   * Update the enabled status of the exclude packages button.
   */ 
  private void 
  updateExcludePackageButton()
  {
    pExcludePackageButton.setEnabled(false);

    Toolset toolset = getSelectedToolset();
    PackageTreeData data = getSelectedIncludedPackageData();

    if((toolset != null) && (data != null)) {
      if(data.isPackage() && !toolset.isFrozen()) 
	pExcludePackageButton.setEnabled(pPrivilegeDetails.isMasterAdmin());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shows or hides this component.
   */ 
  public void 
  setVisible
  (
   boolean isVisible
  )
  {
    if(!isVisible) {
      /* clear caches */ 
      pToolsets.clear();
      pPackageVersions.clear();
      pPackageMods.clear();
      
      pFrozenToolsetEditors.clear();
      pFrozenToolsetComparators.clear();
      pFrozenToolsetActions.clear();
      pFrozenToolsetTools.clear();
      pFrozenToolsetArchivers.clear();
      
      pToolsetEditors.clear();
      pToolsetComparators.clear();
      pToolsetActions.clear();
      pToolsetTools.clear();
      pToolsetArchivers.clear();

      pFrozenPackageEditors.clear();
      pFrozenPackageComparators.clear();
      pFrozenPackageActions.clear();
      pFrozenPackageTools.clear();
      pFrozenPackageArchivers.clear();

      pPackageEditors.clear();
      pPackageComparators.clear();
      pPackageActions.clear();
      pPackageTools.clear();
      pPackageArchivers.clear();

      
      /* hide child dialogs */ 
      pPackageDetailsDialog.setVisible(false);
      pCreatePackageDialog.setVisible(false);
      pTestPackageDialog.setVisible(false);

      pToolsetDetailsDialog.setVisible(false);
      pCreateToolsetDialog.setVisible(false);
      pTestToolsetDialog.setVisible(false);
      pExportToolsetDialog.setVisible(false);

      pToolsetPluginsDialog.setVisible(false);
      pPackagePluginsDialog.setVisible(false);
    }

    super.setVisible(isVisible);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 	
  valueChanged
  (
   ListSelectionEvent e
  )
  {
    if(e.getValueIsAdjusting()) 
      return;

    if(e.getSource() == pActiveToolsetsList) {
      pDisableToolsetButton.setEnabled(false);

      if(getSelectedActiveToolsetName() != null) {
	pIncludedPackagesLabel.setText("Included Unix Packages:");

	pToolsetsTree.removeTreeSelectionListener(this);
  	  pToolsetsTree.getSelectionModel().clearSelection();
	  pEnableToolsetButton.setEnabled(false);
	pToolsetsTree.addTreeSelectionListener(this);

	pDisableToolsetButton.setEnabled(pPrivilegeDetails.isMasterAdmin());
      }

      rebuildIncludedPackagesList();
      updateDialogs();    
    }
    else if(e.getSource() == pIncludedPackagesList) {
      if(getSelectedIncludedPackageData() != null) {
	pPackagesTree.removeTreeSelectionListener(this);
	  pPackagesTree.getSelectionModel().clearSelection();
	pPackagesTree.addTreeSelectionListener(this);
      }
    }

    updateIncludePackageButton();
    updateExcludePackageButton();
  }


  /*-- TREE SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 
  valueChanged
  (
   TreeSelectionEvent e
  )
  { 
    if(e.getSource() == pToolsetsTree) {
      pEnableToolsetButton.setEnabled(false);

      ToolsetTreeData data = getSelectedToolsetData();
      if(data != null) {
	OsType os = data.getOsType();
	if(os != null) 
	  pIncludedPackagesLabel.setText("Included " + os + " Packages:");
	else 
	  pIncludedPackagesLabel.setText("Included Packages:");

	pActiveToolsetsList.removeListSelectionListener(this);
  	  pActiveToolsetsList.getSelectionModel().clearSelection();
	  pDisableToolsetButton.setEnabled(false);
	pActiveToolsetsList.addListSelectionListener(this);

	Toolset toolset = lookupToolset(data.getName(), os);
	if(toolset != null) 
	  pEnableToolsetButton.setEnabled
	    (pPrivilegeDetails.isMasterAdmin() && toolset.isFrozen() && (os == OsType.Unix));
      }

      rebuildIncludedPackagesList();
      updateDialogs();
    }
    else if(e.getSource() == pPackagesTree) {
      PackageTreeData data = getSelectedPackageData();
      if(data != null) {
	pIncludedPackagesList.removeListSelectionListener(this);
	  pIncludedPackagesList.clearSelection();
	pIncludedPackagesList.addListSelectionListener(this);
      }
    }

    updateIncludePackageButton();
    updateExcludePackageButton();
  }

          
  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered(MouseEvent e) {} 
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {} 
  
  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    pDragStartIdx   = -1;
    pDragCurrentIdx = -1;

    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON1:
      {
	int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
       
	int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	/* BUTTON1 (double click): show details */ 
	if(((mods & (on1 | off1)) == on1) && (e.getClickCount() == 2)) {
	  Component comp = e.getComponent();
	  if(comp == pActiveToolsetsList) {
	    int idx = pActiveToolsetsList.locationToIndex(e.getPoint());
	    if(idx != -1) {
	      Rectangle bounds = pActiveToolsetsList.getCellBounds(idx, idx);
	      if(!bounds.contains(e.getX(), e.getY()))
		idx = -1;
	    }

	    if(idx != -1) 
	      doToolsetDetails();
	  }
	  else if(comp == pToolsetsTree) {
	    TreePath tpath = pToolsetsTree.getClosestPathForLocation(e.getX(), e.getY());
	    if(tpath != null) {
	      Rectangle bounds = pToolsetsTree.getPathBounds(tpath);
	      if(!bounds.contains(e.getX(), e.getY()))
		tpath = null;
	    }
	   
	    if(tpath != null) 
	      doToolsetDetails();
	  }
	  else if(comp == pIncludedPackagesList) {
	    int idx = pIncludedPackagesList.locationToIndex(e.getPoint());
	    if(idx != -1) {
	      Rectangle bounds = pIncludedPackagesList.getCellBounds(idx, idx);
	      if(!bounds.contains(e.getX(), e.getY()))
		idx = -1;
	    }
	   
	    if(idx != -1) 
	      doPackageDetails();
	  }
	  else if(comp == pPackagesTree) {
	    TreePath tpath = pPackagesTree.getClosestPathForLocation(e.getX(), e.getY());
	    if(tpath != null) {
	      Rectangle bounds = pPackagesTree.getPathBounds(tpath);
	      if(!bounds.contains(e.getX(), e.getY()))
		tpath = null;
	    }
	   
	    if(tpath != null) 
	      doPackageDetails();
	  }
	}
      }
      break;
     
    case MouseEvent.BUTTON2:
      {
	int on1  = (MouseEvent.BUTTON2_DOWN_MASK);
       
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	/* BUTTON2: package reorder drag start */ 
	if((mods & (on1 | off1)) == on1) {
	  Component comp = e.getComponent();
	  if(comp == pIncludedPackagesList) {
	    int idx = pIncludedPackagesList.locationToIndex(e.getPoint());
	    if(idx != -1) {
	      Rectangle bounds = pIncludedPackagesList.getCellBounds(idx, idx);
	      if(!bounds.contains(e.getX(), e.getY()))
		idx = -1;
	    }

	    pIncludedPackagesList.setSelectedIndex(idx);
	    pDragStartIdx = idx;
	  }
	}
      }
      break;

    case MouseEvent.BUTTON3:
      {
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
       
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
       
	/* BUTTON3: popup menu */ 
	if((mods & (on1 | off1)) == on1) {
	  Component comp = e.getComponent();
	  if(comp == pActiveToolsetsList) {
	    int idx = pActiveToolsetsList.locationToIndex(e.getPoint());
	    if(idx != -1) {
	      Rectangle bounds = pActiveToolsetsList.getCellBounds(idx, idx);
	      if(!bounds.contains(e.getX(), e.getY()))
		idx = -1;
	    }
	   
	    updateActiveToolsetsMenu(idx);
	    pActiveToolsetsPopup.show(comp, e.getX(), e.getY());
	  }
	  else if(comp == pToolsetsTree) {
	    TreePath tpath = pToolsetsTree.getClosestPathForLocation(e.getX(), e.getY());
	    if(tpath != null) {
	      Rectangle bounds = pToolsetsTree.getPathBounds(tpath);
	      if(!bounds.contains(e.getX(), e.getY()))
		tpath = null;
	    }
	   
 	    updateToolsetsMenu(tpath);
 	    pToolsetsPopup.show(comp, e.getX(), e.getY());
	  }
	  else if(comp == pIncludedPackagesList) {
	    int idx = pIncludedPackagesList.locationToIndex(e.getPoint());
	    if(idx != -1) {
	      Rectangle bounds = pIncludedPackagesList.getCellBounds(idx, idx);
	      if(!bounds.contains(e.getX(), e.getY()))
		idx = -1;
	    }
	   
	    updateIncludedPackagesMenu(idx);
	    pIncludedPackagesPopup.show(comp, e.getX(), e.getY());
	  }
	  else if(comp == pPackagesTree) {
	    TreePath tpath = pPackagesTree.getClosestPathForLocation(e.getX(), e.getY());
	    if(tpath != null) {
	      Rectangle bounds = pPackagesTree.getPathBounds(tpath);
	      if(!bounds.contains(e.getX(), e.getY()))
		tpath = null;
	    }
	   
	    updatePackagesMenu(tpath);
	    pPackagesPopup.show(comp, e.getX(), e.getY());
	  }
	}
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased 
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON2:
      {
	int on1  = MouseEvent.ALT_DOWN_MASK;   // THIS IS A REQUIRED AWT EVENT HACK!
       
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	/* BUTTON2: package reorder drag stop */ 
	if((mods & (on1 | off1)) == on1) {
	  Component comp = e.getComponent();
	  if(comp == pIncludedPackagesList) {
	    int idx = pIncludedPackagesList.locationToIndex(e.getPoint());
	    if(idx != -1) {
	      Rectangle bounds = pIncludedPackagesList.getCellBounds(idx, idx);
	      if(!bounds.contains(e.getX(), e.getY()))
		idx = -1;
	    }

	    if((pDragStartIdx != -1) && (idx != -1)) 
	      doReorderPackage(pDragStartIdx, idx);
	  }
	}
      }
    }

    pDragCurrentIdx = -1;
    pIncludedPackagesList.repaint();
  }


  /*-- MOUSE MOTION LISTNER METHODS --------------------------------------------------------*/
  
  /**
   * Invoked when a mouse button is pressed on a component and then dragged. 
   */ 
  public void 	
  mouseDragged
  (
   MouseEvent e
  )
  {
    if(!pPrivilegeDetails.isMasterAdmin()) 
      return;

    int mods = e.getModifiersEx();

    int on1  = (MouseEvent.BUTTON2_DOWN_MASK);
	
    int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		MouseEvent.BUTTON3_DOWN_MASK | 
		MouseEvent.SHIFT_DOWN_MASK |
		MouseEvent.ALT_DOWN_MASK |
		MouseEvent.CTRL_DOWN_MASK);

    pDragCurrentIdx = -1; 
    if((mods & (on1 | off1)) == on1) {
      int idx = pIncludedPackagesList.locationToIndex(e.getPoint());
      if(idx != -1) {
	Rectangle bounds = pIncludedPackagesList.getCellBounds(idx, idx);
	if(!bounds.contains(e.getX(), e.getY()))
	  idx = -1;
      }

      pDragCurrentIdx = idx;
    }

    pIncludedPackagesList.repaint();
  }

  /**
   * Invoked when the mouse cursor has been moved onto a component but no buttons have 
   * been pushed. 
   */ 
  public void 	
  mouseMoved(MouseEvent e) {} 



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
    if(e.getActionCommand().equals("toolset-details")) 
      doToolsetDetails();
    else if(e.getActionCommand().equals("test-toolset")) 
      doTestToolset();
    else if(e.getActionCommand().equals("export-toolset")) 
      doExportToolset();
    else if(e.getActionCommand().equals("new-toolset")) 
      doNewToolset();
    else if(e.getActionCommand().equals("add-mac-toolset")) 
      doAddToolset(OsType.MacOS);
    else if(e.getActionCommand().equals("add-win-toolset")) 
      doAddToolset(OsType.Windows);
    else if(e.getActionCommand().equals("clone-toolset")) 
      doCloneToolset();
    else if(e.getActionCommand().equals("freeze-toolset")) 
      doFreezeToolset();
    else if(e.getActionCommand().equals("delete-toolset")) 
      doDeleteToolset();
    else if(e.getActionCommand().equals("default-toolset")) 
      doDefaultToolset();
    else if(e.getActionCommand().equals("enable-toolset")) 
      doEnableToolset();
    else if(e.getActionCommand().equals("disable-toolset")) 
      doDisableToolset();
    else if(e.getActionCommand().equals("package-details")) 
      doPackageDetails();
    else if(e.getActionCommand().equals("test-package")) 
      doTestPackage();
    else if(e.getActionCommand().equals("package-earlier")) 
      doPackageEarlier();
    else if(e.getActionCommand().equals("package-later")) 
      doPackageLater();
    else if(e.getActionCommand().equals("new-package")) 
      doNewPackage();
    else if(e.getActionCommand().equals("add-mac-package")) 
      doAddPackage(OsType.MacOS);
    else if(e.getActionCommand().equals("add-win-package")) 
      doAddPackage(OsType.Windows);
    else if(e.getActionCommand().equals("new-package-version")) 
      doNewPackageVersion();
    else if(e.getActionCommand().equals("clone-package-version")) 
      doClonePackageVersion();
    else if(e.getActionCommand().equals("freeze-package")) 
      doFreezePackage();
    else if(e.getActionCommand().equals("delete-package")) 
      doDeletePackage();
    else if(e.getActionCommand().equals("include-package")) 
      doIncludePackage();
    else if(e.getActionCommand().equals("exclude-package")) 
      doExcludePackage();
    else if(e.getActionCommand().equals("manage-toolset-plugins")) 
      doManageToolsetPlugins();
    else if(e.getActionCommand().equals("manage-package-plugins")) 
      doManagePackagePlugins();    
    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Show the details of the currently selected toolset.
   */ 
  public void 
  doToolsetDetails()
  {
    OsType os = OsType.Unix;
    Toolset toolset = getSelectedActiveToolset();
    if(toolset == null) {
      ToolsetTreeData data = getSelectedToolsetData();
      os = data.getOsType();
      toolset = lookupToolset(data.getName(), os); 
    }

    if(toolset != null) {
      pToolsetDetailsDialog.updateToolset(os, toolset);
      pToolsetDetailsDialog.setVisible(true);
    }
  }

  /**
   * Test executing a shell command using the environment of the selected toolset.
   */ 
  public void 
  doTestToolset()
  {
    Toolset tset = getSelectedToolset();    
    if(tset != null)
      showTestToolsetDialog(tset);
  }

  /**
   * Export the currently selected toolset as a bash(1) shell script.
   */ 
  public void 
  doExportToolset()
  {
    Toolset toolset = getSelectedActiveToolset();
    OsType os = OsType.Unix;
    if(toolset == null) {
      ToolsetTreeData data = getSelectedToolsetData();
      if(data != null) {
	os = data.getOsType();
	toolset = lookupToolset(data.getName(), os);
      }
    }
    
    if((toolset != null) && (os != null)) {
      pExportToolsetDialog.updateTargetName(toolset.getName() + "-" + os + ".sh");
      pExportToolsetDialog.setVisible(true);    
      if(pExportToolsetDialog.wasConfirmed()) {
 	File file = pExportToolsetDialog.getSelectedFile();
 	if(file != null) {
 	  try {
 	    StringBuffer buf = new StringBuffer();

	    switch(os) {
	    case Unix:
	    case MacOS:
	      {
		buf.append
		  ("export TOOLSET=" + toolset.getName() + "\n" +
		   "export USER=`whoami`\n" +
		   "export HOME=" + PackageInfo.getHomeDir(os) + "/$USER\n" +
		   "export WORKING=" + PackageInfo.getWorkDir(os) + "/$USER/default\n");
		   
		TreeMap<String,String> env = toolset.getEnvironment();
		for(String ename : env.keySet()) {
		  String evalue = env.get(ename);
		  buf.append("export " + ename + "=");
		  if((evalue != null) && (evalue.length() > 0)) 
		    buf.append("'" + evalue + "'");
		  buf.append("\n");
		}  
	      }
	      break;
	    
	    case Windows:
	      assert(false);
	    }

 	    {
 	      FileWriter out = new FileWriter(file);
 	      out.write(buf.toString());
 	      out.flush();
 	      out.close();
 	    }
 	  }
 	  catch(IOException ex) {
 	    UIMaster.getInstance().showErrorDialog(ex);
 	  }
 	}
      }
    }
  }
  
  /**
   * Create a new modifiable toolset.
   */ 
  public void 
  doNewToolset()
  {
    JNewToolsetDialog diag = new JNewToolsetDialog(this);
    diag.setVisible(true);
    
    if(diag.wasConfirmed()) {
      String tname = diag.getName();
      if((tname != null) && !pToolsets.containsKey(tname)) {
	updateToolset(OsType.Unix, new Toolset(tname));
	updateAll();
	selectToolset(tname, OsType.Unix);
	updateDialogs();
      }
    }
  }

  /**
   * Add a non-Unix specialized toolset with a naming matching the currently selected
   * toolset tree node.
   */ 
  public void 
  doAddToolset
  (
   OsType os
  ) 
  {
    ToolsetTreeData data = getSelectedToolsetData();
    if((data != null) && (data.getName() != null)) {
      String tname = data.getName();
      if(pToolsets.containsKey(tname) && !pToolsets.get(tname).containsKey(os)) {
	updateToolset(os, new Toolset(data.getName()));
	updateAll();
	selectToolset(tname, os);
	updateDialogs();
      }
    }
  }

  /**
   * Clone all OS specializations of the currently selected toolset.
   */ 
  public void 
  doCloneToolset()
  {
    ToolsetTreeData data = getSelectedToolsetData();
    if((data != null) && (data.getName() != null) && (data.getOsType() == null)) {
      String stname = data.getName();
      TreeMap<OsType,Toolset> stoolsets = pToolsets.get(stname);
      if((stoolsets != null) && !stoolsets.isEmpty()) {

	JNewToolsetDialog diag = new JNewToolsetDialog(this);
	diag.setVisible(true);
	
	if(diag.wasConfirmed()) {
	  String tname = diag.getName();
	  if(tname != null) {
	    for(OsType os : stoolsets.keySet()) {
	      Toolset stoolset = lookupToolset(stname, os); 
	      if((stoolset != null) && pPrivilegeDetails.isMasterAdmin()) {
		ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
		int wk;
		for(wk=0; wk<stoolset.getNumPackages(); wk++) {
		  String pname  = stoolset.getPackageName(wk);
		  VersionID vid = stoolset.getPackageVersionID(wk);
		  packages.add(lookupPackage(pname, os, vid));
		}
		
		updateToolset(os, new Toolset(tname, packages));

		pToolsetPluginsDialog.clone(stname, tname, os);
	      }
	    }

	    updateAll();
	    selectToolset(tname, OsType.Unix);
	    updateDialogs();
	  }
	}
      }
    }
  }

  /**
   * Freeze the current state of the selected modifiable toolset to create a permanent
   * read-only toolset.
   */ 
  public void 
  doFreezeToolset()
  {
    ToolsetTreeData data = getSelectedToolsetData();
    if(data != null) {
      String tname = data.getName();
      OsType os = data.getOsType();

      Toolset toolset = lookupToolset(tname, data.getOsType());
      if((toolset != null) && !toolset.isFrozen() && pPrivilegeDetails.isMasterAdmin()) {
	UIMaster master = UIMaster.getInstance();
	
	if(!toolset.hasPackages()) {
	  master.showErrorDialog
	    ("Error:", 
	     "Unable to freeze toolset (" + tname + ") which contained no packages!");
	  return;
	}

	if(toolset.hasModifiablePackages()) {
	  StringBuffer buf = new StringBuffer();
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    if(toolset.getPackageVersionID(wk) == null) 
	      buf.append("  " + toolset.getPackageName(wk) + "\n");
	  }

	  master.showErrorDialog
	    ("Error:", 
	     "Unable to freeze toolset (" + tname + ") which contained the " + 
	     "following working packages:\n\n" + 
	     buf.toString());
	  return;
	}
   
	if(toolset.hasConflicts()) {
	  StringBuffer buf = new StringBuffer();
	  for(String ename : toolset.getConflictedEnvNames()) 
	    buf.append("  " + ename + "\n");
	  
	  master.showErrorDialog
	    ("Error:", 
	     "Unable to freeze toolset (" + tname + ") which has unresolved " + 
	     "conflicts between its packages for the following environmental " + 
	     "variables:\n\n" + 
	     buf.toString());
	  return;
	}
	assert(toolset.isFreezable());
	
	pCreateToolsetDialog.updateHeader("Create Toolset:  " + tname);
	pCreateToolsetDialog.setVisible(true);

	if(pCreateToolsetDialog.wasConfirmed()) {
	  String desc = pCreateToolsetDialog.getDescription();
	  assert((desc != null) && (desc.length() > 0));
	
	  MasterMgrClient client = master.getMasterMgrClient();
	  try {
	    ArrayList<PackageVersion> packages = new ArrayList<PackageVersion>();
	    int wk;
	    for(wk=0; wk<toolset.getNumPackages(); wk++) {
	      String pname  = toolset.getPackageName(wk);
	      VersionID vid = toolset.getPackageVersionID(wk);
	      assert(vid != null);
	      packages.add((PackageVersion) lookupPackage(pname, os, vid));
	    }
	
	    Toolset ntoolset = client.createToolset(tname, desc, packages, os);
	    assert(ntoolset != null);
	    assert(ntoolset.getName().equals(tname));

	    updateToolset(os, ntoolset);

	    setToolsetEditors(tname, os, pToolsetEditors.get(tname, os));
	    setToolsetComparators(tname, os, pToolsetComparators.get(tname, os));
	    setToolsetActions(tname, os, pToolsetActions.get(tname, os));
	    setToolsetTools(tname, os, pToolsetTools.get(tname, os));
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	
	  updateAll();
	  updateDialogs();
	}
      }
    }
  }

  /**
   * Delete the selected a modifiable toolset.
   */ 
  public void 
  doDeleteToolset()
  {
    ToolsetTreeData data = getSelectedToolsetData();
    if(data != null) {
      String tname = data.getName();
      OsType os = data.getOsType();

      Toolset toolset = lookupToolset(tname, os);
      if((toolset != null) && !toolset.isFrozen() && pPrivilegeDetails.isMasterAdmin()) {

	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets != null) {
	  toolsets.remove(os);
	  
	  if(toolsets.isEmpty()) 
	    pToolsets.remove(tname);
	}

	pToolsetPluginsDialog.remove(tname, os);

	updateAll();    
	updateDialogs();
      }
    }
  }

  /**
   * Make the selected active toolset the default toolset.
   */ 
  public void 
  doDefaultToolset()
  {
    String tname = getSelectedActiveToolsetName();
    if(tname != null) {
      {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	try {
	  client.setDefaultToolsetName(tname);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	
	pDefaultToolset = tname;
      }
	
      updateAll();
      updateDialogs();
    }
  }
  
  /**
   * Make the selected toolset active.
   */ 
  public void 
  doEnableToolset()
  {
    ToolsetTreeData data = getSelectedToolsetData();
    if(data != null) {
      Toolset toolset = lookupToolset(data.getName(), data.getOsType());
      if(toolset != null) {
	String tname = toolset.getName();
	if(toolset.isFrozen() && 
	   !pActiveToolsets.contains(tname) && 
	   data.getOsType().equals(OsType.Unix)) {

	  {
	    UIMaster master = UIMaster.getInstance();
	    MasterMgrClient client = master.getMasterMgrClient();
	    try {
	      client.setToolsetActive(tname, true);
	    }
	    catch(PipelineException ex) {
	      master.showErrorDialog(ex);
	      return;
	    }
	    
	    pActiveToolsets.add(tname);
	  }
	
	  updateAll();
	  selectActiveToolset(tname);
	}
      }
    }
  }
  
  /**
   * Make the selected toolset inactive.
   */ 
  public void 
  doDisableToolset()
  {
    String tname = getSelectedActiveToolsetName();
    if(tname != null) {
      {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	try {
	  client.setToolsetActive(tname, false);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	
	pActiveToolsets.remove(tname);

	if((pDefaultToolset != null) && pDefaultToolset.equals(tname))
	  pDefaultToolset = null;
      }
	
      updateAll();
      selectToolset(tname, OsType.Unix);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Manage the set of plugin menu layouts associated with a toolset.
   */ 
  public void 
  doManageToolsetPlugins() 
  {
    ToolsetTreeData data = getSelectedToolsetData();
    if((data != null) && (data.getName() != null) && (data.getOsType() != null)) {
      pToolsetPluginsDialog.update(data.getName(), data.getOsType(), pPrivilegeDetails);
      pToolsetPluginsDialog.setVisible(true);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
    * Show the details of the currently selected package.
    */ 
  public void 
  doPackageDetails()
  {
    updateDialogs();
    pPackageDetailsDialog.setVisible(true);
  }

  /**
   * Test executing a shell command using the environment of the selected package.
   */ 
  public void 
  doTestPackage()
  {
    PackageCommon com = getSelectedPackage();
    if(com != null)
      showTestPackageDialog(com);
  }

  /**
   * Move an included package to a different position in the evaluation order.
   */ 
  public void 
  doReorderPackage
  (
   int oldIdx, 
   int newIdx
  ) 
  {
    if((oldIdx == newIdx) || (oldIdx < 0) || (newIdx < 0)) 
      return;

    ToolsetTreeData data = getSelectedToolsetData();
    if(data != null) {
      OsType os = data.getOsType();
      Toolset toolset = getSelectedToolset();
      if((os != null) && (toolset != null) && !toolset.isFrozen()) {
	if((oldIdx < toolset.getNumPackages()) && (newIdx < toolset.getNumPackages())) {
	  ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	  {
	    PackageCommon oldPkg = null;
	    {
	      String pname  = toolset.getPackageName(oldIdx);
	      VersionID vid = toolset.getPackageVersionID(oldIdx);
	      oldPkg = lookupPackage(pname, os, vid);
	    }
	    
	    int wk, idx;
	    for(wk=0, idx=0; wk<toolset.getNumPackages(); wk++) {
	      if(idx == newIdx) {
		packages.add(oldPkg);
		idx++;
	      }

	      if(wk != oldIdx) {
		String pname  = toolset.getPackageName(wk);
		VersionID vid = toolset.getPackageVersionID(wk);
		packages.add(lookupPackage(pname, os, vid));
		idx++;
	      }
	    }

	    if(idx == newIdx) {
	      packages.add(oldPkg);
	      idx++;
	    }
	  }

	  updateToolset(os, new Toolset(toolset.getName(), packages));
	  updateAll();
	  pIncludedPackagesList.setSelectedIndex(newIdx);

	  updateDialogs();
	}
      }
    }  
  }

  /**
   * Move the currently selected package one place earlier in the evaluation order.
   */ 
  public void 
  doPackageEarlier()
  {
    int idx = pIncludedPackagesList.getSelectedIndex();
    doReorderPackage(idx, idx-1);
  }

  /**
   * Move the currently selected package one place later in the evaluation order.
   */ 
  public void 
  doPackageLater()
  {
    int idx = pIncludedPackagesList.getSelectedIndex();
    doReorderPackage(idx, idx+1);
  }
  
  /**
   * Create a new empty modifiable version of an existing package. 
   */ 
  public void 
  doNewPackageVersion()
  {
    PackageTreeData data = getSelectedPackageData();
    if(data != null) {
      String pname = data.getName();
      OsType os = data.getOsType();
      if((pname != null) && (os != null)) {
	TreeMap<OsType,PackageMod> packages = pPackageMods.get(pname);
	if(packages == null) {
	  packages = new TreeMap<OsType,PackageMod>();
	  pPackageMods.put(pname, packages);
	}
	packages.put(os, new PackageMod(pname));

	updateAll();
	selectPackage(pname, os, null);
	updateDialogs();

	pPackagePluginsDialog.clone(pname, os, null);
      }
    }
  }

  /**
   * Create a new modifiable version of an existing package which is a clone of the selected
   * package.
   */ 
  public void 
  doClonePackageVersion()
  {
    PackageTreeData data = getSelectedPackageData();
    if(data != null) {
      String pname = data.getName();
      OsType os = data.getOsType();
      VersionID vid = data.getVersionID();
      if((pname != null) && (os != null) && (vid != null)) {
	PackageCommon com = lookupPackage(pname, os, vid);
	if(com != null) {
	  TreeMap<OsType,PackageMod> packages = pPackageMods.get(pname);
	  if(packages == null) {
	    packages = new TreeMap<OsType,PackageMod>();
	    pPackageMods.put(pname, packages);
	  }
	  packages.put(os, new PackageMod(com));

	  updateAll();
	  selectPackage(pname, os, null);
	  updateDialogs();

	  pPackagePluginsDialog.clone(pname, os, vid);
	}
      }
    }
  }
  
  /**
   * Create a new package. 
   */ 
  public void 
  doNewPackage()
  {
    JNewPackageDialog diag = new JNewPackageDialog(this);
    diag.setVisible(true);
    
    if(diag.wasConfirmed()) {
      String pname = diag.getName();
      if((pname != null) && (pname.length() > 0) && !pPackageMods.containsKey(pname)) {
	TreeMap<OsType,PackageMod> packages = new TreeMap<OsType,PackageMod>();
	pPackageMods.put(pname, packages);
	packages.put(OsType.Unix, new PackageMod(pname));

	updateAll();
	selectPackage(pname, OsType.Unix, null);
	updateDialogs();

	pPackagePluginsDialog.update(pname, OsType.Unix, null, pPrivilegeDetails);
      }
    }
  }

  /**
   * Add a non-Unix specialized pacakge with a naming matching the currently selected
   * package tree node.
   */ 
  public void 
  doAddPackage
  (
   OsType os
  ) 
  {
    PackageTreeData data = getSelectedPackageData();
    if(data != null) {
      String pname = data.getName();
      if((pname != null) && (os != null)) {
	TreeMap<OsType,PackageMod> packages = pPackageMods.get(pname);
	if(packages == null) {
	  packages = new TreeMap<OsType,PackageMod>();
	  pPackageMods.put(pname, packages);
	}

	if(!packages.containsKey(os)) {
	  packages.put(os, new PackageMod(pname));

	  updateAll();
	  selectPackage(pname, os, null);
	  updateDialogs();

	  pPackagePluginsDialog.update(pname, os, null, pPrivilegeDetails);
	}
      }
    }
  }

  /**
   * Freeze the current state of the selected modifiable package to create a permanent
   * read-only package.
   */ 
  public void 
  doFreezePackage()
  {
    PackageTreeData data = getSelectedPackageData();
    if((data != null) && data.isPackage() && (data.getVersionID() == null)) {
      String pname = data.getName();
      OsType os = data.getOsType();
      PackageCommon pcom = lookupPackage(pname, os, null);
      if((pcom != null) && (pcom instanceof PackageMod)) {
	PackageMod pmod = (PackageMod) pcom;
	
	/* query the user for the revision increment level and decription */ 
	VersionID latest = null;
	if(pPackageVersions.containsKey(pname) && 
	   pPackageVersions.get(pname).containsKey(os)) 
	  latest = pPackageVersions.get(pname).get(os).lastKey();
	
	pCreatePackageDialog.updateNameVersion
	  ("Create " + os + " Package:  " + pname, latest);
	
	pCreatePackageDialog.setVisible(true);
	if(pCreatePackageDialog.wasConfirmed()) {
	  
	  /* create the read-only package */ 
	  PackageVersion pvsn = null;
	  {
	    String desc = pCreatePackageDialog.getDescription();
	    assert((desc != null) && (desc.length() > 0));
	    VersionID.Level level = pCreatePackageDialog.getLevel();
	    
	    UIMaster master = UIMaster.getInstance();
	    MasterMgrClient client = master.getMasterMgrClient();
	    try {
	      pvsn = client.createToolsetPackage(pmod, desc, level, os);
	      assert(pvsn != null);
	      assert(pvsn.getName().equals(pname));
	      
	      TreeMap<OsType,TreeMap<VersionID,PackageVersion>> packages = 
		pPackageVersions.get(pname);
	      if(packages == null) {
		packages = new TreeMap<OsType,TreeMap<VersionID,PackageVersion>>();
		pPackageVersions.put(pname, packages);
	      }
	      
	      TreeMap<VersionID,PackageVersion> versions = packages.get(os);
	      if(versions == null) {
		versions = new TreeMap<VersionID,PackageVersion>();
		packages.put(os, versions);
	      }
	      
	      versions.put(pvsn.getVersionID(), pvsn);
	    }
	    catch(PipelineException ex) {
	      master.showErrorDialog(ex);
	      return;
	    }
	  }
	  
	  /* remove the working package */ 
	  {
	    TreeMap<OsType,PackageMod> packages = pPackageMods.get(pname);
	    if(packages != null) {
	      packages.remove(os);
	      
	      if(packages.isEmpty()) 
		pPackageMods.remove(pname);
	    }
	  }
	  
	  /* replace the working package with the frozen package in all working toolsets */ 
	  for(String tname : pToolsets.keySet()) {
	    Toolset toolset = lookupToolset(tname, os);
	    if((toolset != null) && !toolset.isFrozen()) {
	      boolean modified = false;
	      ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	      int wk;
	      for(wk=0; wk<toolset.getNumPackages(); wk++) {
		String name   = toolset.getPackageName(wk);
		VersionID vid = toolset.getPackageVersionID(wk);
		if(pname.equals(name) && (vid == null)) {
		  packages.add(pvsn);
		  modified = true;
		}
		else {
		  packages.add(lookupPackage(name, os, vid));
		}
	      }
	      
	      if(modified) 
		updateToolset(os, new Toolset(tname, packages));
	    }
	  }
	  
	  updateAll();
	  updateDialogs();

	  pPackagePluginsDialog.freeze(pname, os, pvsn.getVersionID());
	}
      }
    }
  }

  /**
   * Delete the selected modifiable package.
   */ 
  public void 
  doDeletePackage()
  {
    PackageTreeData data = getSelectedPackageData();
    if(data != null) {
      String dpname = data.getName();
      OsType os = data.getOsType();
      if((dpname != null) && (os != null)) {
	/* remove the working package */ 
	{
	  TreeMap<OsType,PackageMod> packages = pPackageMods.get(dpname);
	  if(packages != null) {
	    packages.remove(os);
	    
	    if(packages.isEmpty()) 
	      pPackageMods.remove(dpname);
	  }
	}
	
	/* remove the deleted package from all working toolsets */ 
	for(String tname : pToolsets.keySet()) {
	  Toolset toolset = lookupToolset(tname, os);
	  if((toolset != null) && !toolset.isFrozen()) {
	    boolean modified = false;
	    ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	    int wk;
	    for(wk=0; wk<toolset.getNumPackages(); wk++) {
	      String pname  = toolset.getPackageName(wk);
	      VersionID vid = toolset.getPackageVersionID(wk);
	      if(!pname.equals(dpname) || (vid != null)) 
		packages.add(lookupPackage(pname, os, vid));
	      else 
		modified = true;
	    }
	    
	    if(modified) 
	      updateToolset(os, new Toolset(tname, packages));
	  }
	}
	
	updateAll();
	updateDialogs();

	pPackagePluginsDialog.remove(dpname, os);

	PackageCommon com = pPackageDetailsDialog.getPackage();
	if(com != null) {
	  if(com.getName().equals(dpname))
	    pPackageDetailsDialog.setVisible(false);	

	  if(com.getName().equals(pPackagePluginsDialog.getPackageName()))
	    pPackagePluginsDialog.setVisible(false);
	}
      }
    }
  }
     
  /**
   * Include the selected package as a package of the selected toolset.
   */ 
  public void 
  doIncludePackage()
  {
    Toolset toolset = getSelectedToolset();
    PackageTreeData data = getSelectedPackageData();
    if((data != null) && (toolset != null) && !toolset.isFrozen()) {
      OsType os = data.getOsType();
      PackageCommon com = lookupPackage(data.getName(), os, data.getVersionID());
      if(com != null) {
  
	/* rebuild the toolset adding the new package */ 
	ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	int wk;
	for(wk=0; wk<toolset.getNumPackages(); wk++) {
	  String pname  = toolset.getPackageName(wk);
	  VersionID vid = toolset.getPackageVersionID(wk);
	  packages.add(lookupPackage(pname, os, vid));
	}
	packages.add(com);

	updateToolset(os, new Toolset(toolset.getName(), packages));
   
	updateAll();
	updateDialogs();
      }
    }
  }

  /**
   * Exclude the selected package from the packages of the selected toolset.
   */ 
  public void 
  doExcludePackage()
  {
    Toolset toolset = getSelectedToolset();
    PackageTreeData data = getSelectedIncludedPackageData();
    if((data != null) && (toolset != null) && !toolset.isFrozen()) {
      OsType os = data.getOsType();
      int idx = pIncludedPackagesList.getSelectedIndex();
      if(idx != -1) {
	/* rebuild the toolset removing the selected package */ 
	ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	int wk;
	for(wk=0; wk<toolset.getNumPackages(); wk++) {
	  String pname  = toolset.getPackageName(wk);
	  VersionID vid = toolset.getPackageVersionID(wk);
	  if(wk != idx) 
	    packages.add(lookupPackage(pname, os, vid));
	}

	updateToolset(os, new Toolset(toolset.getName(), packages));
   
	updateAll();
	selectPackage(data.getName(), os, data.getVersionID());
	updateDialogs();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Manage the set of plugins associated with a toolset package.
   */ 
  public void 
  doManagePackagePlugins() 
  {
    PackageTreeData data = getSelectedPackageData();
    if((data != null) && data.isPackage()) {
      pPackagePluginsDialog.update(data.getName(), data.getOsType(), data.getVersionID(), 
				   pPrivilegeDetails);
      pPackagePluginsDialog.setVisible(true);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T   T R E E   H E L P E R S                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the toolset and all related UI components.
   */ 
  private void 
  updateToolset
  (
   OsType os, 
   Toolset toolset
  ) 
  {
    String tname = toolset.getName();

    TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
    if(toolsets == null) {
      toolsets = new TreeMap<OsType,Toolset>();
      pToolsets.put(tname, toolsets);
    }
    toolsets.put(os, toolset);

    {
      Toolset dtoolset = pToolsetDetailsDialog.getToolset();
      if((dtoolset != null) && tname.equals(dtoolset.getName())) 
	pToolsetDetailsDialog.updateToolset(os, toolset);
      else 
	pToolsetDetailsDialog.setVisible(false);
    }

    {
      Toolset dtoolset = pTestToolsetDialog.getToolset();
      if((dtoolset != null) && tname.equals(dtoolset.getName()) && 
	 os.equals(PackageInfo.sOsType)) 
	pTestToolsetDialog.updateToolset(toolset);
      else 
	pTestToolsetDialog.setVisible(false);
    }
  }

  /**
   * Update the dialogs.
   */ 
  public void 
  updateDialogs()
  {
    Toolset toolset = null;
    OsType os = null;
    {
      ToolsetTreeData data = getSelectedToolsetData();
      if(data != null) {
	String tname = data.getName();
	os = data.getOsType();
	toolset = lookupToolset(tname, os);
	if(toolset != null) {
	  pToolsetDetailsDialog.updateToolset(os, toolset);
	  if(os.equals(PackageInfo.sOsType)) 
	    pTestToolsetDialog.updateToolset(toolset);

	  pToolsetPluginsDialog.update(tname, os, pPrivilegeDetails);
	}
      }
    }
      
    int idx = pIncludedPackagesList.getSelectedIndex();
    PackageCommon com = getSelectedIncludedPackage();	
    if((com != null) && (idx != -1) && (toolset != null)) {
      pPackageDetailsDialog.updatePackage(os, com, toolset, idx);
    }
    else {
      PackageTreeData data = getSelectedPackageData();
      if(data != null) {
	com = lookupPackage(data.getName(), data.getOsType(), data.getVersionID());
	if(com != null) 
	  pPackageDetailsDialog.updatePackage(data.getOsType(), com, null, -1);

	pPackagePluginsDialog.update
	  (data.getName(), data.getOsType(), data.getVersionID(), pPrivilegeDetails);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names and revision numbers of plugins associated with a working toolset package.
   */ 
  private class
  FrozenPackagePlugins
    extends TripleMap<String,OsType,VersionID,DoubleMap<String,String,TreeSet<VersionID>>>
  {
    public FrozenPackagePlugins() 
    {
      super();
    }

    public boolean
    isCached
    (
     String pname,
     OsType os, 
     VersionID vid
    ) 
    {
      return (super.get(pname, os, vid) != null);
    }

    public DoubleMap<String,String,TreeSet<VersionID>>
    get
    (
     String pname,
     OsType os, 
     VersionID vid
    ) 
    {
      DoubleMap<String,String,TreeSet<VersionID>> plugins = super.get(pname, os, vid);
      if(plugins != null) {
	DoubleMap<String,String,TreeSet<VersionID>> table = 
	  new DoubleMap<String,String,TreeSet<VersionID>>();
	for(String vendor : plugins.keySet()) {
	  for(String name : plugins.get(vendor).keySet()) 
	    table.put(vendor, name, new TreeSet<VersionID>(plugins.get(vendor).get(name)));
	}
	return table;
      }
      
      return new DoubleMap<String,String,TreeSet<VersionID>>();
    }
     
    public void 
    put
    (
     String pname,
     OsType os, 
     VersionID vid,
     DoubleMap<String,String,TreeSet<VersionID>> plugins
    ) 
    {
      if(plugins != null) {
	DoubleMap<String,String,TreeSet<VersionID>> table = 
	  new DoubleMap<String,String,TreeSet<VersionID>>();
	for(String vendor : plugins.keySet()) {
	  for(String name : plugins.get(vendor).keySet()) 
	    table.put(vendor, name, new TreeSet<VersionID>(plugins.get(vendor).get(name)));
	}

	super.put(pname, os, vid, table);
      }
      else {
	super.remove(pname, os, vid);
      }
    }

    private static final long serialVersionUID = 7809754910797720803L;
  }

  /**
   * The names and revision numbers of plugins associated with a working toolset package.
   */ 
  private class
  PackagePlugins
    extends DoubleMap<String,OsType,DoubleMap<String,String,TreeSet<VersionID>>>
  {
    public PackagePlugins() 
    {
      super();
    }

    public DoubleMap<String,String,TreeSet<VersionID>>
    get
    (
     String pname,
     OsType os
    ) 
    {
      DoubleMap<String,String,TreeSet<VersionID>> plugins = super.get(pname, os);
      if(plugins != null) {
	DoubleMap<String,String,TreeSet<VersionID>> table = 
	  new DoubleMap<String,String,TreeSet<VersionID>>();
	for(String vendor : plugins.keySet()) {
	  for(String name : plugins.get(vendor).keySet()) 
	    table.put(vendor, name, new TreeSet<VersionID>(plugins.get(vendor).get(name)));
	}

	return table;
      }
      
      return new DoubleMap<String,String,TreeSet<VersionID>>();
    }
     
    public void 
    put
    (
     String pname,
     OsType os, 
     DoubleMap<String,String,TreeSet<VersionID>> plugins
    ) 
    {
      if(plugins != null) {
	DoubleMap<String,String,TreeSet<VersionID>> table = 
	  new DoubleMap<String,String,TreeSet<VersionID>>();
	for(String vendor : plugins.keySet()) {
	  for(String name : plugins.get(vendor).keySet()) 
	    table.put(vendor, name, new TreeSet<VersionID>(plugins.get(vendor).get(name)));
	}

	super.put(pname, os, table);
      }
      else {
	super.remove(pname, os);
      }
    }

    private static final long serialVersionUID = 3911049299607257455L;
  }

  /**
   * The plugin menu layouts indexed by toolset name and operation system.
   */ 
  private class
  ToolsetLayouts
    extends DoubleMap<String,OsType,PluginMenuLayout>
  {
    public ToolsetLayouts() 
    {
      super();
    }

    public boolean
    isCached
    (
     String tname,
     OsType os
    ) 
    {
      return (super.get(tname, os) != null);
    }

    public PluginMenuLayout
    get
    (
     String tname,
     OsType os
    ) 
    {
      PluginMenuLayout layout = super.get(tname, os);
      if(layout != null) 
	return layout;

      return new PluginMenuLayout();
    }
	     
    public void 
    put
    (
     String tname,
     OsType os, 
     PluginMenuLayout layout
    ) 
    {
      if(layout != null) 
	super.put(tname, os, layout);
      else 
	remove(tname, os);
    }

    private static final long serialVersionUID = -8290422438068739410L;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5071693156853738683L;
  
  protected static final int  sLWidth  = 240;
  protected static final int  sLHeight = 400;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the default toolset.
   */ 
  private String  pDefaultToolset;

  /**
   * The names of the active toolsets.
   */ 
  private TreeSet<String>  pActiveToolsets;

  /**
   * The cached table of all toolsets indexed by toolset name an operating system. <P> 
   * 
   * All existing toolsets will have a key in this table, but the value may be 
   * null if the toolset is not currently cached.  This table also contains temporary 
   * modifiable toolsets which have not been frozen.
   */ 
  private TreeMap<String,TreeMap<OsType,Toolset>>  pToolsets;

  /**
   * A cache of frozen toolset plugins menu layouts.
   */ 
  private ToolsetLayouts pFrozenToolsetEditors;
  private ToolsetLayouts pFrozenToolsetComparators;
  private ToolsetLayouts pFrozenToolsetActions;
  private ToolsetLayouts pFrozenToolsetTools;
  private ToolsetLayouts pFrozenToolsetArchivers;

  /**
   * A cache of working (unfrozen) toolset plugins menu layouts.
   */ 
  private ToolsetLayouts pToolsetEditors;
  private ToolsetLayouts pToolsetComparators;
  private ToolsetLayouts pToolsetActions;
  private ToolsetLayouts pToolsetTools;
  private ToolsetLayouts pToolsetArchivers;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached table of read-only toolset packages indexed by package name, operating 
   * system and revision number. <P> 
   * 
   * All existing toolset packages will have a key in this table, but the value may be 
   * null if the toolset package is not currently cached.  
   */   
  private TreeMap<String,TreeMap<OsType,TreeMap<VersionID,PackageVersion>>>  pPackageVersions;

  /** 
   * The modifiable toolset packages which have not yet been frozen indexed by package 
   * and operating system.
   */ 
  private TreeMap<String,TreeMap<OsType,PackageMod>>  pPackageMods;

  /**
   * A cache of plugins associated with frozen packages.
   */ 
  private FrozenPackagePlugins pFrozenPackageEditors;
  private FrozenPackagePlugins pFrozenPackageComparators;
  private FrozenPackagePlugins pFrozenPackageActions;
  private FrozenPackagePlugins pFrozenPackageTools;
  private FrozenPackagePlugins pFrozenPackageArchivers;

  /**
   * A cache of plugins associated with working (unfrozen) packages.
   */ 
  private PackagePlugins pPackageEditors;
  private PackagePlugins pPackageComparators;
  private PackagePlugins pPackageActions;
  private PackagePlugins pPackageTools;
  private PackagePlugins pPackageArchivers;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The list of active toolsets. <P> 
   * 
   * Contains String (package name) instances.
   */ 
  private JList  pActiveToolsetsList;

  /**
   * The active toolsets popup menu.
   */ 
  private JPopupMenu  pActiveToolsetsPopup; 

  /**
   * The active toolsets popup menu items.
   */
  private JMenuItem  pActiveToolsetDetailsItem;
  private JMenuItem  pActiveTestToolsetItem;
  private JMenuItem  pActiveExportToolsetItem;
  private JMenuItem  pDefaultToolsetItem;

  /**
   * The toolset buttons.
   */ 
  private JButton  pEnableToolsetButton;
  private JButton  pDisableToolsetButton;

  /**
   * All toolsets grouped by toolset name and operating system type. <P> 
   * 
   * Contains ToolsetTreeData instances.
   */ 
  private JTree  pToolsetsTree;

  /**
   * The toolsets popup menu.
   */ 
  private JPopupMenu  pToolsetsPopup; 

  /**
   * The toolsets popup menu items.
   */
  private JMenuItem  pToolsetDetailsItem;
  private JMenuItem  pTestToolsetItem;
  private JMenuItem  pExportToolsetItem;
  private JMenuItem  pNewToolsetItem;
  private JMenuItem  pAddMacOSToolsetItem;
  private JMenuItem  pAddWindowsToolsetItem;
  private JMenuItem  pCloneToolsetItem;
  private JMenuItem  pFreezeToolsetItem;
  private JMenuItem  pDeleteToolsetItem;
  private JMenuItem  pManagePluginMenusItem;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The header label display above the included packages list.
   */    
  private JLabel  pIncludedPackagesLabel; 

  /**
   * The list of packages included in the selected toolset. <P> 
   * 
   * Contains PackageListData instances.
   */ 
  private JList  pIncludedPackagesList;

  /**
   * The package buttons.
   */ 
  private JButton  pIncludePackageButton;
  private JButton  pExcludePackageButton;

  /**
   * The included packages popup menu.
   */ 
  private JPopupMenu  pIncludedPackagesPopup;

  /**
   * The included packages popup menu items.
   */
  private JMenuItem  pIncPackageDetailsItem;
  private JMenuItem  pIncTestPackageItem;
  private JMenuItem  pPackageEarlierItem;
  private JMenuItem  pPackageLaterItem;


  /**
   * All package versions grouped by package name, operating system and revision number. <P> 
   * 
   * Contains PackageTreeData instances.
   */ 
  private JTree  pPackagesTree;

  /**
   * The packages popup menu.
   */ 
  private JPopupMenu  pPackagesPopup; 

  /**
   * The packages popup menu items.
   */
  private JMenuItem  pPackageDetailsItem;
  private JMenuItem  pTestPackageItem;
  private JMenuItem  pNewPackageItem;
  private JMenuItem  pAddMacOSPackageItem;
  private JMenuItem  pAddWindowsPackageItem;
  private JMenuItem  pNewPackageVersionItem;
  private JMenuItem  pClonePackageVersionItem;
  private JMenuItem  pFreezePackageItem;
  private JMenuItem  pDeletePackageItem;
  private JMenuItem  pManagePluginsItem;

  /**
   * Package reordering drag start index or <CODE>-1</CODE> if not dragging.
   */ 
  private int  pDragStartIdx; 
  private int  pDragCurrentIdx;

  /*----------------------------------------------------------------------------------------*/

  /**
   * Child dialogs.
   */
  private JPackageDetailsDialog  pPackageDetailsDialog;
  private JCreatePackageDialog   pCreatePackageDialog;
  private JTestPackageDialog     pTestPackageDialog;


  private JToolsetDetailsDialog  pToolsetDetailsDialog;
  private JCreateToolsetDialog   pCreateToolsetDialog;
  private JTestToolsetDialog     pTestToolsetDialog;
  private JFileSelectDialog      pExportToolsetDialog;


  private JManageToolsetPluginsDialog  pToolsetPluginsDialog; 
  private JManagePackagePluginsDialog  pPackagePluginsDialog; 

}
