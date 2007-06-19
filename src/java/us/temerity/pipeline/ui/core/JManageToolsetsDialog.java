// $Id: JManageToolsetsDialog.java,v 1.27 2007/06/19 20:16:06 jim Exp $

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
  extends JTopLevelDialog
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
    super("Manage Toolsets");

    /* initialize fields */ 
    {
      pPrivilegeDetails = new PrivilegeDetails();

      pDefaultToolset = null;
      pActiveToolsets = new TreeSet<String>();
      pToolsets       = new DoubleMap<String,OsType,Toolset>();

      pFrozenToolsetLayouts = new DoubleMap<String,PluginType,PluginMenuLayout>();
      pToolsetLayouts       = new DoubleMap<String,PluginType,PluginMenuLayout>();

      pPackageVersions = new TripleMap<String,OsType,VersionID,PackageVersion>();
      pPackageMods     = new DoubleMap<String,OsType,PackageMod>();
      
      pFrozenPackagePlugins = new TripleMap<String,VersionID,PluginType,PluginSet>();
      pPackagePlugins       = new DoubleMap<String,PluginType,PluginSet>();
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
      JPanel left = new JPanel();
      left.setName("MainPanel");
      left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
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
      
      JPanel right = new JPanel();
      right.setName("MainPanel");
      right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
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

      super.initUI("Manage Toolsets:", body, null, null, null, "Close");
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
   * Get a message warning about unfrozen toolsets or packages.
   * 
   * @return
   *   The message or <CODE>null</CODE> if all toolsets/packages are frozen.
   */ 
  public String
  getUnfrozenWarning() 
  {
    String toolsetMsg = null;
    {
      boolean hasUnfrozen = false;
      StringBuilder buf = new StringBuilder();
      for(String tname : pToolsets.keySet()) {
	for(OsType os : pToolsets.keySet(tname)) {
	  Toolset toolset = pToolsets.get(tname, os);
	  if((toolset != null) && !toolset.isFrozen()) {
	    buf.append("  " + tname + " (" + os.toTitle() + ")\n");
	    hasUnfrozen = true;
	  }
	}
      }
      
      if(hasUnfrozen) 
	toolsetMsg = buf.toString();
    }

    if((toolsetMsg != null) || !pPackageMods.isEmpty()) {
      StringBuilder buf = new StringBuilder();

      if(toolsetMsg != null) 
	buf.append("Working Toolsets:\n" + toolsetMsg + "\n");
      
      if(!pPackageMods.isEmpty()) {
	buf.append("Working Toolset Packages:\n"); 
	for(String pname : pPackageMods.keySet()) {
	  for(OsType os : pPackageMods.keySet(pname)) 
	    buf.append("  " + pname + " (" + os.toTitle() + ")\n");
	}
      }

      return buf.toString();
    }

    return null;
  }


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
      if(pToolsets.containsKey(tname, os)) {
	Toolset toolset = pToolsets.get(tname, os);
	if(toolset == null) {
	  UIMaster master = UIMaster.getInstance();
	  try {
	    MasterMgrClient client = master.getMasterMgrClient();
	    toolset = client.getToolset(tname, os);
	    pToolsets.put(tname, os, toolset);
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
	  }
	}
	
	return toolset; 
      }
    }

    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the editor plugin menu associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   */ 
  public PluginMenuLayout
  getToolsetEditors
  (
   String tname
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");
       
    PluginMenuLayout layout = null;
    if(!toolset.isFrozen()) 
      layout = pToolsetLayouts.get(tname, PluginType.Editor);
    else 
      layout = pFrozenToolsetLayouts.get(tname, PluginType.Editor);

    if(layout == null) 
      layout = new PluginMenuLayout();

    return layout;
  }

  /**
   * Set the editor plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetEditors
  ( 
   String tname, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");

    PluginMenuLayout layout2 = layout;
    if(layout2 == null) 
      layout2 = new PluginMenuLayout();

    if(!toolset.isFrozen()) {
      pToolsetLayouts.put(tname, PluginType.Editor, layout2);
    }
    else {
      pFrozenToolsetLayouts.put(tname, PluginType.Editor, layout2);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setEditorMenuLayout(tname, layout2);
    }    
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the comparator plugin menu associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   */ 
  public PluginMenuLayout
  getToolsetComparators
  (
   String tname
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");
       
    PluginMenuLayout layout = null;
    if(!toolset.isFrozen()) 
      layout = pToolsetLayouts.get(tname, PluginType.Comparator);
    else 
      layout = pFrozenToolsetLayouts.get(tname, PluginType.Comparator);

    if(layout == null) 
      layout = new PluginMenuLayout();

    return layout;
  }

  /**
   * Set the comparator plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetComparators
  ( 
   String tname, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");

    PluginMenuLayout layout2 = layout;
    if(layout2 == null) 
      layout2 = new PluginMenuLayout();

    if(!toolset.isFrozen()) {
      pToolsetLayouts.put(tname, PluginType.Comparator, layout2);
    }
    else {
      pFrozenToolsetLayouts.put(tname, PluginType.Comparator, layout2);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setComparatorMenuLayout(tname, layout2);
    }    
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the action plugin menu associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   */ 
  public PluginMenuLayout
  getToolsetActions
  (
   String tname
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");
       
    PluginMenuLayout layout = null;
    if(!toolset.isFrozen()) 
      layout = pToolsetLayouts.get(tname, PluginType.Action);
    else 
      layout = pFrozenToolsetLayouts.get(tname, PluginType.Action);

    if(layout == null) 
      layout = new PluginMenuLayout();

    return layout;
  }

  /**
   * Set the action plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetActions
  ( 
   String tname, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");

    PluginMenuLayout layout2 = layout;
    if(layout2 == null) 
      layout2 = new PluginMenuLayout();

    if(!toolset.isFrozen()) {
      pToolsetLayouts.put(tname, PluginType.Action, layout2);
    }
    else {
      pFrozenToolsetLayouts.put(tname, PluginType.Action, layout2);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setActionMenuLayout(tname, layout2);
    }    
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the tool plugin menu associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   */ 
  public PluginMenuLayout
  getToolsetTools
  (
   String tname
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");
       
    PluginMenuLayout layout = null;
    if(!toolset.isFrozen()) 
      layout = pToolsetLayouts.get(tname, PluginType.Tool);
    else 
      layout = pFrozenToolsetLayouts.get(tname, PluginType.Tool);

    if(layout == null) 
      layout = new PluginMenuLayout();

    return layout;
  }

  /**
   * Set the tool plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetTools
  ( 
   String tname, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");

    PluginMenuLayout layout2 = layout;
    if(layout2 == null) 
      layout2 = new PluginMenuLayout();

    if(!toolset.isFrozen()) {
      pToolsetLayouts.put(tname, PluginType.Tool, layout2);
    }
    else {
      pFrozenToolsetLayouts.put(tname, PluginType.Tool, layout2);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setToolMenuLayout(tname, layout2);
    }    
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the archiver plugin menu associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   */ 
  public PluginMenuLayout
  getToolsetArchivers
  (
   String tname
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");
       
    PluginMenuLayout layout = null;
    if(!toolset.isFrozen()) 
      layout = pToolsetLayouts.get(tname, PluginType.Archiver);
    else 
      layout = pFrozenToolsetLayouts.get(tname, PluginType.Archiver);

    if(layout == null) 
      layout = new PluginMenuLayout();

    return layout;
  }

  /**
   * Set the archiver plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetArchivers
  ( 
   String tname, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");

    PluginMenuLayout layout2 = layout;
    if(layout2 == null) 
      layout2 = new PluginMenuLayout();

    if(!toolset.isFrozen()) {
      pToolsetLayouts.put(tname, PluginType.Archiver, layout2);
    }
    else {
      pFrozenToolsetLayouts.put(tname, PluginType.Archiver, layout2);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setArchiverMenuLayout(tname, layout2);
    }    
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the master extension plugin menu associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   */ 
  public PluginMenuLayout
  getToolsetMasterExts
  (
   String tname
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");
       
    PluginMenuLayout layout = null;
    if(!toolset.isFrozen()) 
      layout = pToolsetLayouts.get(tname, PluginType.MasterExt);
    else 
      layout = pFrozenToolsetLayouts.get(tname, PluginType.MasterExt);

    if(layout == null) 
      layout = new PluginMenuLayout();

    return layout;
  }

  /**
   * Set the master extension plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetMasterExts
  ( 
   String tname, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");

    PluginMenuLayout layout2 = layout;
    if(layout2 == null) 
      layout2 = new PluginMenuLayout();

    if(!toolset.isFrozen()) {
      pToolsetLayouts.put(tname, PluginType.MasterExt, layout2);
    }
    else {
      pFrozenToolsetLayouts.put(tname, PluginType.MasterExt, layout2);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setMasterExtMenuLayout(tname, layout2);
    }    
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the queue extension plugin menu associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   */ 
  public PluginMenuLayout
  getToolsetQueueExts
  (
   String tname
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");
       
    PluginMenuLayout layout = null;
    if(!toolset.isFrozen()) 
      layout = pToolsetLayouts.get(tname, PluginType.QueueExt);
    else 
      layout = pFrozenToolsetLayouts.get(tname, PluginType.QueueExt);

    if(layout == null) 
      layout = new PluginMenuLayout();

    return layout;
  }

  /**
   * Set the queue extension plugins associated with the given toolset.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param layout
   *   The plugin menu layout. 
   */ 
  public void
  setToolsetQueueExts
  ( 
   String tname, 
   PluginMenuLayout layout
  ) 
    throws PipelineException
  {
    Toolset toolset = lookupToolset(tname, OsType.Unix);
    if(toolset == null) 
      throw new PipelineException
	("No toolset named (" + tname + ") exists!");

    PluginMenuLayout layout2 = layout;
    if(layout2 == null) 
      layout2 = new PluginMenuLayout();

    if(!toolset.isFrozen()) {
      pToolsetLayouts.put(tname, PluginType.QueueExt, layout2);
    }
    else {
      pFrozenToolsetLayouts.put(tname, PluginType.QueueExt, layout2);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setQueueExtMenuLayout(tname, layout2);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Cache all of the checked-in packages associated with a toolset.
   */ 
  private void 
  cachePackages
  (
   Toolset toolset, 
   OsType os
  ) 
  {
    DoubleMap<String,VersionID,TreeSet<OsType>> index =
      new DoubleMap<String,VersionID,TreeSet<OsType>>();
    
    TreeSet<OsType> supports = new TreeSet<OsType>();
    supports.add(os);

    int wk;
    for(wk=0; wk<toolset.getNumPackages(); wk++) {
      String pname  = toolset.getPackageName(wk); 
      VersionID vid = toolset.getPackageVersionID(wk); 
      if((vid != null) && (pPackageVersions.get(pname, os, vid) == null))
	index.put(pname, vid, supports);
    }

    if(!index.isEmpty()) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      try {
	TripleMap<String,VersionID,OsType,PackageVersion> packages = 
	  client.getToolsetPackages(index);
	for(String pname : packages.keySet()) {
	  for(VersionID vid : packages.keySet(pname)) {
	    for(OsType pos : packages.keySet(pname, vid)) {
	      pPackageVersions.put(pname, pos, vid, packages.get(pname, vid, os));
	    }
	  }
	}
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
      }
    }
  }

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
   * 
   * @param queryDialogChanges
   *   Whether to incorporate any unsaved changed from the details dialog.
   */ 
  public PackageCommon
  lookupPackage
  (
   String pname, 
   OsType os, 
   VersionID vid, 
   boolean queryDialogChanges
  ) 
  {
    if((pname != null) && (os != null)) {
      if(vid != null) {
	PackageVersion pkg = pPackageVersions.get(pname, os, vid); 
	if(pkg == null) {
	  UIMaster master = UIMaster.getInstance();
	  MasterMgrClient client = master.getMasterMgrClient();
	  try {
	    pkg = client.getToolsetPackage(pname, vid, os);
	    pPackageVersions.put(pname, os, vid, pkg);
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
	  }
	}
	
	return pkg;
      }
      else {
	if(queryDialogChanges) {
	  String dname = pPackageDetailsDialog.getPackageName(); 
	  if(pname.equals(dname) && os.equals(pPackageDetailsDialog.getPackageOsType())) {
	    PackageCommon com = pPackageDetailsDialog.getPackage();
	    if((com != null) && (com instanceof PackageMod)) 
	      pPackageMods.put(pname, os, (PackageMod) com);
	  }
	}
	
	return pPackageMods.get(pname, os);
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
    return (pPackageVersions.containsKey(pname, os) || 
	    pPackageMods.containsKey(pname, os));
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Make sure all of the plugins associated with all packages and the plugin menu layouts
   * for the given frozen toolset are currently locally cached.
   */ 
  public void 
  cacheFrozenPluginsAndLayouts
  (
   Toolset toolset
  ) 
    throws PipelineException
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();

    /* cache package plugins */ 
    {
      TreeMap<String,TreeSet<VersionID>> uncached = new TreeMap<String,TreeSet<VersionID>>();
      {
	int wk;
	for(wk=0; wk<toolset.getNumPackages(); wk++) {
	  String pname = toolset.getPackageName(wk);
	  VersionID pvid = toolset.getPackageVersionID(wk);
	  
	  /* its a frozen and uncached package */ 
	  if(pvid != null && (pFrozenPackagePlugins.keySet(pname, pvid) == null)) {
	    TreeSet<VersionID> vids = uncached.get(pname);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      uncached.put(pname, vids);
	    }
	    vids.add(pvid);
	  }
	}
      }
      
      if(!uncached.isEmpty()) {
	TripleMap<String,VersionID,PluginType,PluginSet> psets = 
	  client.getSelectPackagePlugins(uncached);
	
	for(String pname : psets.keySet()) {
	  for(VersionID pvid : psets.keySet(pname)) 
	    pFrozenPackagePlugins.put(pname, pvid, psets.get(pname).get(pvid));
	}
      }
    }

    /* cache menu layouts */ 
    String tname = toolset.getName();
    if(!pFrozenToolsetLayouts.containsKey(tname))
       pFrozenToolsetLayouts.put(tname, client.getPluginMenuLayouts(tname));
  }

  /**
   * Make sure all of the plugins associated with a given frozen package are locally cached.
   */ 
  public void 
  cacheFrozenPlugins
  (
   String pname, 
   VersionID pvid 
  ) 
    throws PipelineException
  {
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();

    /* cache package plugins */ 
    if(pvid != null) 
      pFrozenPackagePlugins.put(pname, pvid, client.getSelectPackagePlugins(pname, pvid));
  }

  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the editor plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public PluginSet
  getPackageEditors
  (
   String pname, 
   VersionID vid
  ) 
    throws PipelineException
  {
    PluginSet pset = null;
    if(vid == null) 
      pset = pPackagePlugins.get(pname, PluginType.Editor); 
    else {
      pset = pFrozenPackagePlugins.get(pname, vid, PluginType.Editor);
      if(pset == null) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pset = client.getPackageEditorPlugins(pname, vid);
	pFrozenPackagePlugins.put(pname, vid, PluginType.Editor, pset);
      }
    }

    if(pset == null) 
      pset = new PluginSet();

    return pset;
  }

  /**
   * Set the editor plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
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
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackagePlugins.put(pname, PluginType.Editor, plugins); 
    }
    else {
      pFrozenPackagePlugins.put(pname, vid, PluginType.Editor, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageEditorPlugins(pname, vid, plugins);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the comparator plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public PluginSet
  getPackageComparators
  (
   String pname, 
   VersionID vid
  ) 
    throws PipelineException
  {
    PluginSet pset = null;
    if(vid == null) 
      pset = pPackagePlugins.get(pname, PluginType.Comparator); 
    else {
      pset = pFrozenPackagePlugins.get(pname, vid, PluginType.Comparator);
      if(pset == null) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pset = client.getPackageComparatorPlugins(pname, vid);
	pFrozenPackagePlugins.put(pname, vid, PluginType.Comparator, pset);
      }
    }

    if(pset == null) 
      pset = new PluginSet();

    return pset;
  }

  /**
   * Set the comparator plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
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
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackagePlugins.put(pname, PluginType.Comparator, plugins); 
    }
    else {
      pFrozenPackagePlugins.put(pname, vid, PluginType.Comparator, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageComparatorPlugins(pname, vid, plugins);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the action plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public PluginSet
  getPackageActions
  (
   String pname, 
   VersionID vid
  ) 
    throws PipelineException
  {
    PluginSet pset = null;
    if(vid == null) 
      pset = pPackagePlugins.get(pname, PluginType.Action); 
    else {
      pset = pFrozenPackagePlugins.get(pname, vid, PluginType.Action);
      if(pset == null) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pset = client.getPackageActionPlugins(pname, vid);
	pFrozenPackagePlugins.put(pname, vid, PluginType.Action, pset);
      }
    }

    if(pset == null) 
      pset = new PluginSet();

    return pset;
  }

  /**
   * Set the action plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
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
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackagePlugins.put(pname, PluginType.Action, plugins); 
    }
    else {
      pFrozenPackagePlugins.put(pname, vid, PluginType.Action, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageActionPlugins(pname, vid, plugins);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the tool plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public PluginSet
  getPackageTools
  (
   String pname, 
   VersionID vid
  ) 
    throws PipelineException
  {
    PluginSet pset = null;
    if(vid == null) 
      pset = pPackagePlugins.get(pname, PluginType.Tool); 
    else {
      pset = pFrozenPackagePlugins.get(pname, vid, PluginType.Tool);
      if(pset == null) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pset = client.getPackageToolPlugins(pname, vid);
	pFrozenPackagePlugins.put(pname, vid, PluginType.Tool, pset);
      }
    }

    if(pset == null) 
      pset = new PluginSet();

    return pset;
  }

  /**
   * Set the tool plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
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
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackagePlugins.put(pname, PluginType.Tool, plugins); 
    }
    else {
      pFrozenPackagePlugins.put(pname, vid, PluginType.Tool, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageToolPlugins(pname, vid, plugins);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the archiver plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public PluginSet
  getPackageArchivers
  (
   String pname, 
   VersionID vid
  ) 
    throws PipelineException
  {
    PluginSet pset = null;
    if(vid == null) 
      pset = pPackagePlugins.get(pname, PluginType.Archiver); 
    else {
      pset = pFrozenPackagePlugins.get(pname, vid, PluginType.Archiver);
      if(pset == null) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pset = client.getPackageArchiverPlugins(pname, vid);
	pFrozenPackagePlugins.put(pname, vid, PluginType.Archiver, pset);
      }
    }

    if(pset == null) 
      pset = new PluginSet();

    return pset;
  }

  /**
   * Set the archiver plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
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
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackagePlugins.put(pname, PluginType.Archiver, plugins); 
    }
    else {
      pFrozenPackagePlugins.put(pname, vid, PluginType.Archiver, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageArchiverPlugins(pname, vid, plugins);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the master extension plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public PluginSet
  getPackageMasterExts
  (
   String pname, 
   VersionID vid
  ) 
    throws PipelineException
  {
    PluginSet pset = null;
    if(vid == null) 
      pset = pPackagePlugins.get(pname, PluginType.MasterExt); 
    else {
      pset = pFrozenPackagePlugins.get(pname, vid, PluginType.MasterExt);
      if(pset == null) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pset = client.getPackageMasterExtPlugins(pname, vid);
	pFrozenPackagePlugins.put(pname, vid, PluginType.MasterExt, pset);
      }
    }

    if(pset == null) 
      pset = new PluginSet();

    return pset;
  }

  /**
   * Set the master extension plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the plugins.
   */ 
  public void
  setPackageMasterExts
  ( 
   String pname,
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackagePlugins.put(pname, PluginType.MasterExt, plugins); 
    }
    else {
      pFrozenPackagePlugins.put(pname, vid, PluginType.MasterExt, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageMasterExtPlugins(pname, vid, plugins);
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the queue extension plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   */ 
  public PluginSet
  getPackageQueueExts
  (
   String pname, 
   VersionID vid
  ) 
    throws PipelineException
  {
    PluginSet pset = null;
    if(vid == null) 
      pset = pPackagePlugins.get(pname, PluginType.QueueExt); 
    else {
      pset = pFrozenPackagePlugins.get(pname, vid, PluginType.QueueExt);
      if(pset == null) {
	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	pset = client.getPackageQueueExtPlugins(pname, vid);
	pFrozenPackagePlugins.put(pname, vid, PluginType.QueueExt, pset);
      }
    }

    if(pset == null) 
      pset = new PluginSet();

    return pset;
  }

  /**
   * Set the queue extension plugins associated with the given package.
   * 
   * @param pname
   *   The toolset name.
   * 
   * @param vid
   *   The package revision number or <CODE>null</CODE> for working packages.
   * 
   * @param plugins
   *   The vendors, names and revision numbers of the plugins.
   */ 
  public void
  setPackageQueueExts
  ( 
   String pname,
   VersionID vid,
   PluginSet plugins
  ) 
    throws PipelineException
  {
    if(vid == null) {
      pPackagePlugins.put(pname, PluginType.QueueExt, plugins); 
    }
    else {
      pFrozenPackagePlugins.put(pname, vid, PluginType.QueueExt, plugins);

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      client.setPackageQueueExtPlugins(pname, vid, plugins);
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
      pDisableToolsetButton.setEnabled(pPrivilegeDetails.isDeveloper());
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
	      (pPrivilegeDetails.isDeveloper() && (odata.getOsType() == OsType.Unix) && 
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
      return lookupPackage(data.getName(), data.getOsType(), data.getVersionID(), false);

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
      return lookupPackage(data.getName(), data.getOsType(), data.getVersionID(), true);

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
   PackageMod pkg, 
   boolean updateUI
  ) 
  {
    if(pkg == null)
      return;

    String rname = pkg.getName();
    if(!pPackageMods.containsKey(rname)) 
      return;

    pPackageMods.put(rname, os, pkg);

    {
      ArrayList<String> rebuild = new ArrayList<String>();

      for(String tname : pToolsets.keySet()) {
	Toolset tset = pToolsets.get(tname, os);
 	if((tset != null) && tset.hasModifiablePackage(rname)) 
 	  rebuild.add(tset.getName());
      }

      for(String tname : rebuild) {
 	Toolset tset = pToolsets.get(tname, os);

 	ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
 	{
 	  int wk;
 	  for(wk=0; wk<tset.getNumPackages(); wk++) {
 	    String pname  = tset.getPackageName(wk);
 	    VersionID vid = tset.getPackageVersionID(wk);
	    packages.add(lookupPackage(pname, os, vid, false));
 	  }
 	}
	
 	updateToolset(os, new Toolset(tname, packages, os));
      }
    }

    if(updateUI) {
      updateAll();
      updateDialogs();
    }
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
	  TreeMap<String,TreeSet<OsType>> tnames = client.getAllToolsetNames();

	  for(String tname : tnames.keySet()) {
	    for(OsType os : tnames.get(tname)) {
	      if(!pToolsets.containsKey(tname, os)) 
		pToolsets.put(tname, os, null);
	    }
	  }
	}

	{
	  DoubleMap<String,OsType,TreeSet<VersionID>> pnames = 
	    client.getAllToolsetPackageNames();

	  for(String pname : pnames.keySet()) {
	    for(OsType os : pnames.keySet(pname)) {
	      for(VersionID vid : pnames.get(pname, os)) {
		if(!pPackageVersions.containsKey(pname, os, vid)) 
		  pPackageVersions.put(pname, os, vid, null);
	      }
	    }
	  }
	}
      }
      catch(PipelineException ex) {
	showErrorDialog(ex);
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
	      oss.addAll(pPackageVersions.keySet(pname));
	    if(pPackageMods.containsKey(pname)) 
	      oss.addAll(pPackageMods.keySet(pname));

	    for(OsType os : oss) {
	      DefaultMutableTreeNode onode = 
		new DefaultMutableTreeNode(new PackageTreeData(pname, os), true);
	      nnode.add(onode);

	      if(pPackageVersions.containsKey(pname, os)) {
		for(VersionID vid : pPackageVersions.keySet(pname, os)) {
		  DefaultMutableTreeNode vnode = 
		    new DefaultMutableTreeNode(new PackageTreeData(pname, os, vid), false);
		  onode.add(vnode);
		}
	      }

	      if(pPackageMods.containsKey(pname, os)) {
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
	  cachePackages(toolset, os);

	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname  = toolset.getPackageName(wk); 
	    VersionID vid = toolset.getPackageVersionID(wk); 
	    PackageCommon pkg = lookupPackage(pname, os, vid, false);
	      
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
	pDefaultToolsetItem.setEnabled(pPrivilegeDetails.isDeveloper());

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
    pNewToolsetItem.setEnabled(pPrivilegeDetails.isDeveloper());
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
	  pFreezeToolsetItem.setEnabled(pPrivilegeDetails.isDeveloper());
	  pDeleteToolsetItem.setEnabled(pPrivilegeDetails.isDeveloper());
	}

	if(data.getOsType() == OsType.Unix)
	  pManagePluginMenusItem.setEnabled(true);
      }

      if((data != null) && (data.getName() != null)) {
	TreeMap<OsType,Toolset> toolsets = pToolsets.get(data.getName());
	if(toolsets != null) {
	  pAddMacOSToolsetItem.setEnabled
	    (pPrivilegeDetails.isDeveloper() && !toolsets.containsKey(OsType.MacOS));
	  pAddWindowsToolsetItem.setEnabled
	    (pPrivilegeDetails.isDeveloper() && !toolsets.containsKey(OsType.Windows));
	  pCloneToolsetItem.setEnabled
	    ((data.getOsType() == null) && pPrivilegeDetails.isDeveloper());
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
	  pPackageEarlierItem.setEnabled(pPrivilegeDetails.isDeveloper() && (idx > 0));
	  
	  DefaultListModel model = (DefaultListModel) pIncludedPackagesList.getModel();
	  pPackageLaterItem.setEnabled(pPrivilegeDetails.isDeveloper() && 
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
    pNewPackageItem.setEnabled(pPrivilegeDetails.isDeveloper());
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
	  pFreezePackageItem.setEnabled(pPrivilegeDetails.isDeveloper());
	  pDeletePackageItem.setEnabled(pPrivilegeDetails.isDeveloper());
	}
	else {
	  pClonePackageVersionItem.setEnabled(pPrivilegeDetails.isDeveloper());
	}
	
	if(data.getOsType() == OsType.Unix)
	  pManagePluginsItem.setEnabled(true);
      }
      
      if((data != null) && (data.getName() != null)) {
	pAddMacOSPackageItem.setEnabled
	  (pPrivilegeDetails.isDeveloper() && !hasPackages(data.getName(), OsType.MacOS));
	pAddWindowsPackageItem.setEnabled
	  (pPrivilegeDetails.isDeveloper() && !hasPackages(data.getName(), OsType.Windows));
	pNewPackageVersionItem.setEnabled
	  (pPrivilegeDetails.isDeveloper() && (data.getOsType() != null));
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

    if(pPrivilegeDetails.isDeveloper()) {
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
	pExcludePackageButton.setEnabled(pPrivilegeDetails.isDeveloper());
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
      /* clear caches of frozen data, leave working versions in place */ 
      {
	DoubleMap<String,OsType,Toolset> working = new DoubleMap<String,OsType,Toolset>();
	for(String tname : pToolsets.keySet()) {
	  for(OsType os : pToolsets.keySet(tname)) {
	    Toolset toolset = pToolsets.get(tname, os);
	    if((toolset != null) && !toolset.isFrozen())
	      working.put(tname, os, toolset);
	  }
	}

	pToolsets.clear();
	pToolsets.putAll(working);
      }

      pPackageVersions.clear();
      pFrozenToolsetLayouts.clear();
      pFrozenPackagePlugins.clear();
      
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

	pDisableToolsetButton.setEnabled(pPrivilegeDetails.isDeveloper());
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
	    (pPrivilegeDetails.isDeveloper() && toolset.isFrozen() && (os == OsType.Unix));
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
    if(!pPrivilegeDetails.isDeveloper()) 
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
      {
	String prefix = (toolset.getName() + "-" + os);
	String tname = null;
	switch(os) {
	case Unix:
	case MacOS:
	  tname = (prefix + ".sh");
	  break;
	  
	case Windows:
	  tname = (prefix + ".bat");
	}

	pExportToolsetDialog.updateTargetName(tname);
      }

      pExportToolsetDialog.setVisible(true);    
      if(pExportToolsetDialog.wasConfirmed()) {
 	File file = pExportToolsetDialog.getSelectedFile();
 	if(file != null) {
 	  try {
 	    StringBuilder buf = new StringBuilder();

	    switch(os) {
	    case Unix:
	    case MacOS:
	      {
		buf.append
		  ("export TOOLSET=" + toolset.getName() + "\n" +
		   "export USER=`whoami`\n" +
		   "export HOME=" + PackageInfo.getHomePath(os) + "/$USER\n" +
		   "export WORKING=" + PackageInfo.getWorkPath(os) + "/$USER/default\n");
		   
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
	      {
		buf.append("set TOOLSET=" + toolset.getName() + "\n");

                Path profile = PackageInfo.getUserProfilePath();
                if(profile != null) {
                  buf.append("set HOMEPATH=" + profile.toOsString(os));
                  if((PackageInfo.sWinUserProfileNeedsUser != null) && 
                     PackageInfo.sWinUserProfileNeedsUser)
                    buf.append("\\%USERNAME%"); 
                }

		String working = PackageInfo.getWorkPath(os).toOsString(os);
                buf.append("\n" +
                           "set WORKING=" + working + "\\%USERNAME%\\default\n");
		
		TreeMap<String,String> env = toolset.getEnvironment();
		for(String ename : env.keySet()) {
		  String evalue = env.get(ename);
		  buf.append("set " + ename + "=");
		  if((evalue != null) && (evalue.length() > 0)) 
		    buf.append("\"" + evalue + "\"");
		  buf.append("\n");
		}  
	      }
	    }

 	    {
 	      FileWriter out = new FileWriter(file);
 	      out.write(buf.toString());
 	      out.flush();
 	      out.close();
 	    }
 	  }
 	  catch(IOException ex) {
 	    showErrorDialog(ex);
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
	updateToolset(OsType.Unix, new Toolset(tname, OsType.Unix));
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
	updateToolset(os, new Toolset(data.getName(), os));
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
	      if((stoolset != null) && pPrivilegeDetails.isDeveloper()) {
		ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
		int wk;
		for(wk=0; wk<stoolset.getNumPackages(); wk++) {
		  String pname  = stoolset.getPackageName(wk);
		  VersionID vid = stoolset.getPackageVersionID(wk);
		  packages.add(lookupPackage(pname, os, vid, true));
		}
		
		updateToolset(os, new Toolset(tname, packages, os));

		if(os == OsType.Unix) {
		  pToolsetPluginsDialog.update(data.getName(), pPrivilegeDetails);
		  pToolsetPluginsDialog.clone(stname, tname);
		}
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
      if((toolset != null) && !toolset.isFrozen() && pPrivilegeDetails.isDeveloper()) {
	UIMaster master = UIMaster.getInstance();
	
	if(!toolset.hasPackages()) {
	  showErrorDialog
	    ("Error:", 
	     "Unable to freeze toolset (" + tname + ") which contained no packages!");
	  return;
	}

	if(toolset.hasModifiablePackages()) {
	  StringBuilder buf = new StringBuilder();
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    if(toolset.getPackageVersionID(wk) == null) 
	      buf.append("  " + toolset.getPackageName(wk) + "\n");
	  }

	  showErrorDialog
	    ("Error:", 
	     "Unable to freeze toolset (" + tname + ") which contained the " + 
	     "following working packages:\n\n" + 
	     buf.toString());
	  return;
	}
   
	if(toolset.hasConflicts()) {
	  StringBuilder buf = new StringBuilder();
	  for(String ename : toolset.getConflictedEnvNames()) 
	    buf.append("  " + ename + "\n");
	  
	  showErrorDialog
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
	      packages.add((PackageVersion) lookupPackage(pname, os, vid, false));
	    }
	
	    Toolset ntoolset = client.createToolset(tname, desc, packages, os);
	    assert(ntoolset != null);
	    assert(ntoolset.getName().equals(tname));

	    updateToolset(os, ntoolset);

	    setToolsetEditors(tname, pToolsetLayouts.get(tname, PluginType.Editor));
	    setToolsetComparators(tname, pToolsetLayouts.get(tname, PluginType.Comparator));
	    setToolsetActions(tname, pToolsetLayouts.get(tname, PluginType.Action));
	    setToolsetTools(tname, pToolsetLayouts.get(tname, PluginType.Tool));
	    setToolsetArchivers(tname, pToolsetLayouts.get(tname, PluginType.Archiver));
	    setToolsetMasterExts(tname, pToolsetLayouts.get(tname, PluginType.MasterExt));
	    setToolsetQueueExts(tname, pToolsetLayouts.get(tname, PluginType.QueueExt));
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
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
      if((toolset != null) && !toolset.isFrozen() && pPrivilegeDetails.isDeveloper()) {

	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets != null) {
	  toolsets.remove(os);
	  
	  if(toolsets.isEmpty()) 
	    pToolsets.remove(tname);
	}

	if(os == OsType.Unix) 
	  pToolsetPluginsDialog.remove(tname);

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
	  showErrorDialog(ex);
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
	      showErrorDialog(ex);
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
	  showErrorDialog(ex);
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
    if((data != null) && (data.getName() != null) && (data.getOsType() == OsType.Unix)) {
      pToolsetPluginsDialog.update(data.getName(), pPrivilegeDetails);
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
    /* first record any unsaved package changes */ 
    {
      PackageCommon com = pPackageDetailsDialog.getPackage();
      if((com != null) && (com instanceof PackageMod)) 
	refreshPackage(pPackageDetailsDialog.getPackageOsType(), (PackageMod) com, false); 
    }      

    updateDialogs(true);
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
	      oldPkg = lookupPackage(pname, os, vid, true);
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
		packages.add(lookupPackage(pname, os, vid, true));
		idx++;
	      }
	    }

	    if(idx == newIdx) {
	      packages.add(oldPkg);
	      idx++;
	    }
	  }

	  updateToolset(os, new Toolset(toolset.getName(), packages, os));
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
	pPackageMods.put(pname, os, new PackageMod(pname));

	updateAll();
	selectPackage(pname, os, null);
	updateDialogs();

	if(os == OsType.Unix)
	  pPackagePluginsDialog.clone(pname, null);
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
	PackageCommon com = lookupPackage(pname, os, vid, false);
	if(com != null) {
	  pPackageMods.put(pname, os, new PackageMod(com));

	  updateAll();
	  selectPackage(pname, os, null);
	  updateDialogs();

	  if(os == OsType.Unix)
	    pPackagePluginsDialog.clone(pname, vid);
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
	pPackageMods.put(pname, OsType.Unix, new PackageMod(pname));

	updateAll();
	selectPackage(pname, OsType.Unix, null);
	updateDialogs();

	pPackagePluginsDialog.update(pname, null, pPrivilegeDetails);
      }
    }
  }

  /**
   * Add a non-Unix specialized package with a naming matching the currently selected
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
	if(!pPackageMods.containsKey(pname, os)) {
	  pPackageMods.put(pname, os, new PackageMod(pname));

	  updateAll();
	  selectPackage(pname, os, null);
	  updateDialogs();
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
      PackageCommon pcom = lookupPackage(pname, os, null, true);
      if((pcom != null) && (pcom instanceof PackageMod)) {
	PackageMod pmod = (PackageMod) pcom;
	
	/* query the user for the revision increment level and decription */ 
	VersionID latest = null;
	if(pPackageVersions.containsKey(pname, os)) 
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
	      
	      pPackageVersions.put(pname, os, pvsn.getVersionID(), pvsn);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
	      return;
	    }
	  }
	  
	  /* remove the working package */ 
	  pPackageMods.remove(pname, os); 
	  
	  /* replace the working package with the frozen package in all working toolsets */ 
	  for(String tname : pToolsets.keySet()) {
	    Toolset toolset =  pToolsets.get(tname, os);
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
		  packages.add(lookupPackage(name, os, vid, false));
		}
	      }
	      
	      if(modified) 
		updateToolset(os, new Toolset(tname, packages, os));
	    }
	  }
	  
	  updateAll();
	  updateDialogs();

	  if(os == OsType.Unix)
	    pPackagePluginsDialog.freeze(pname, pvsn.getVersionID());
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
	pPackageMods.remove(dpname, os);
	
	/* remove the deleted package from all working toolsets */ 
	for(String tname : pToolsets.keySet()) {
	  Toolset toolset = pToolsets.get(tname, os); 
	  if((toolset != null) && !toolset.isFrozen()) {
	    boolean modified = false;
	    ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	    int wk;
	    for(wk=0; wk<toolset.getNumPackages(); wk++) {
	      String pname  = toolset.getPackageName(wk);
	      VersionID vid = toolset.getPackageVersionID(wk);
	      if(!pname.equals(dpname) || (vid != null)) 
		packages.add(lookupPackage(pname, os, vid, false));
	      else 
		modified = true;
	    }
	    
	    if(modified) 
	      updateToolset(os, new Toolset(tname, packages, os));
	  }
	}
	
	updateAll();
	updateDialogs();

	if(os == OsType.Unix) 
	  pPackagePluginsDialog.remove(dpname);

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
      PackageCommon com = lookupPackage(data.getName(), os, data.getVersionID(), true);
      if(com != null) {
	/* rebuild the toolset adding the new package */ 
	ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	int wk;
	for(wk=0; wk<toolset.getNumPackages(); wk++) {
	  String pname  = toolset.getPackageName(wk);
	  VersionID vid = toolset.getPackageVersionID(wk);
	  packages.add(lookupPackage(pname, os, vid, false));
	}
	packages.add(com);

	updateToolset(os, new Toolset(toolset.getName(), packages, os));
   
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
	    packages.add(lookupPackage(pname, os, vid, false));
	}

	updateToolset(os, new Toolset(toolset.getName(), packages, os));
   
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
    if((data != null) && data.isPackage() && (data.getOsType() == OsType.Unix)) {
      pPackagePluginsDialog.update(data.getName(), data.getVersionID(), pPrivilegeDetails);
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

    pToolsets.put(tname, os, toolset); 

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
    updateDialogs(false);
  }

  /**
   * Update the dialogs.
   */ 
  public void 
  updateDialogs
  (
   boolean alwaysPackageDetails
  ) 
  {
    Toolset toolset = null;
    String tname = null;
    OsType os = null;
    {
      ToolsetTreeData data = getSelectedToolsetData();
      if(data != null) {
	tname = data.getName();
	os = data.getOsType();

	if(pToolsetDetailsDialog.isVisible() || 
	   (pTestToolsetDialog.isVisible() && (os == PackageInfo.sOsType)) || 
	   (pToolsetPluginsDialog.isVisible() && (os == OsType.Unix))) {

	  toolset = lookupToolset(tname, os);
	  if(toolset != null) {
	    if(pToolsetDetailsDialog.isVisible())
	      pToolsetDetailsDialog.updateToolset(os, toolset);
	    
	    if(pTestToolsetDialog.isVisible() && (os == PackageInfo.sOsType))
	      pTestToolsetDialog.updateToolset(toolset);
	    
	    if(pToolsetPluginsDialog.isVisible() && (os == OsType.Unix)) 
	      pToolsetPluginsDialog.update(tname, pPrivilegeDetails);
	  }
	}
      }
    }
      
    int idx = pIncludedPackagesList.getSelectedIndex();
    PackageCommon com = getSelectedIncludedPackage();	
    if((com != null) && (idx != -1) && 
       (pPackageDetailsDialog.isVisible() || alwaysPackageDetails)) {
      if((toolset == null) && (tname != null) && (os != null)) 
	toolset = lookupToolset(tname, os);

      if((toolset != null) && (pPackageDetailsDialog.isVisible() || alwaysPackageDetails))
	pPackageDetailsDialog.updatePackage(os, com, toolset, idx);
    }
    else {
      PackageTreeData data = getSelectedPackageData();
      if((data != null) && 
	 ((pPackageDetailsDialog.isVisible() || alwaysPackageDetails) ||
	  (pPackagePluginsDialog.isVisible() && (data.getOsType() == OsType.Unix)))) { 

	com = lookupPackage(data.getName(), data.getOsType(), data.getVersionID(), false);
	if((com != null) && (pPackageDetailsDialog.isVisible() || alwaysPackageDetails))
	  pPackageDetailsDialog.updatePackage(data.getOsType(), com, null, -1);

	if(pPackagePluginsDialog.isVisible() && (data.getOsType() == OsType.Unix))
	  pPackagePluginsDialog.update
	    (data.getName(), data.getVersionID(), pPrivilegeDetails);
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
    extends DoubleMap<String,VersionID,PluginSet>
  {
    public FrozenPackagePlugins() 
    {
      super();
    }

    public boolean
    isCached
    (
     String pname,
     VersionID vid
    ) 
    {
      return (super.get(pname, vid) != null);
    }

    public PluginSet
    get
    (
     String pname,
     VersionID vid
    ) 
    {
      PluginSet plugins = super.get(pname, vid);
      if(plugins != null) 
	return new PluginSet(plugins);
      return new PluginSet();
    }
     
    public void 
    put
    (
     String pname,
     VersionID vid,
     PluginSet plugins
    ) 
    {
      if(plugins != null) 
	super.put(pname, vid, new PluginSet(plugins));
      else 
	super.remove(pname, vid);
    }

    private static final long serialVersionUID = 7809754910797720803L;
  }

  /**
   * The names and revision numbers of plugins associated with a working toolset package.
   */ 
  private class
  PackagePlugins
    extends TreeMap<String,PluginSet>
  {
    public PackagePlugins() 
    {
      super();
    }

    public PluginSet
    get
    (
     String pname
    ) 
    {
      PluginSet plugins = super.get(pname);
      if(plugins != null) 
	return new PluginSet(plugins);
      return new PluginSet();
    }
     
    public PluginSet
    put
    (
     String pname,
     PluginSet plugins
    ) 
    {
      if(plugins != null) 
	super.put(pname, new PluginSet(plugins));
      else 
	super.remove(pname);

      return null;
    }

    private static final long serialVersionUID = 3911049299607257455L;
  }

  /**
   * The plugin menu layouts indexed by toolset name.
   */ 
  private class
  ToolsetLayouts
    extends TreeMap<String,PluginMenuLayout>
  {
    public ToolsetLayouts() 
    {
      super();
    }

    public boolean
    isCached
    (
     String tname
    ) 
    {
      return (super.get(tname) != null);
    }

    public PluginMenuLayout
    get
    (
     String tname
    ) 
    {
      PluginMenuLayout layout = super.get(tname);
      if(layout != null) 
	return layout;

      return new PluginMenuLayout();
    }
	     
    public PluginMenuLayout
    put
    (
     String tname,
     PluginMenuLayout layout
    ) 
    {
      if(layout != null) 
	super.put(tname, layout);
      else 
	remove(tname);

      return null;
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
  private DoubleMap<String,OsType,Toolset>  pToolsets;

  /**
   * A cache of frozen toolset plugins menu layouts.
   */ 
  private DoubleMap<String,PluginType,PluginMenuLayout>  pFrozenToolsetLayouts; 

  /**
   * A cache of working (unfrozen) toolset plugins menu layouts.
   */ 
  private DoubleMap<String,PluginType,PluginMenuLayout>  pToolsetLayouts; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached table of read-only toolset packages indexed by package name, operating 
   * system and revision number. <P> 
   * 
   * All existing toolset packages will have a key in this table, but the value may be 
   * null if the toolset package is not currently cached.  
   */   
  private TripleMap<String,OsType,VersionID,PackageVersion>  pPackageVersions;

  /** 
   * The modifiable toolset packages which have not yet been frozen indexed by package 
   * and operating system.
   */ 
  private DoubleMap<String,OsType,PackageMod>  pPackageMods;

  /**
   * A cache of plugins associated with frozen packages.
   */ 
  private TripleMap<String,VersionID,PluginType,PluginSet>  pFrozenPackagePlugins; 

  /**
   * A cache of plugins associated with working (unfrozen) packages.
   */ 
  private DoubleMap<String,PluginType,PluginSet>  pPackagePlugins; 


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
