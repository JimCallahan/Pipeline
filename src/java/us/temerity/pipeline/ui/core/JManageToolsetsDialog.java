// $Id: JManageToolsetsDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

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
  implements ListSelectionListener, TreeSelectionListener, MouseListener
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
      pIsPrivileged    = false;
      pDefaultToolset  = null;
      pActiveToolsets  = new TreeSet<String>();
      pToolsets        = new TreeMap<String,Toolset>();
      pPackageVersions = new TreeMap<String,TreeMap<VersionID,PackageVersion>>();
      pPackageMods     = new TreeMap<String,PackageMod>();
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

      pToolsetsPopup.addSeparator();
      
      item = new JMenuItem("New...");
      pNewToolsetItem = item;
      item.setActionCommand("new-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  

      item = new JMenuItem("Clone...");
      pCloneToolsetItem = item;
      item.setActionCommand("clone-toolset");
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

      pPackagesPopup.addSeparator();

      item = new JMenuItem("New Package...");
      pNewPackageItem = item;
      item.setActionCommand("new-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);  
      
      item = new JMenuItem("New Version...");
      pNewPackageVersionItem = item;
      item.setActionCommand("new-package-version");
      item.addActionListener(this);
      pPackagesPopup.add(item);  
      
      item = new JMenuItem("Clone Version...");
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
	  lst.setCellRenderer(new JActiveToolsetsListCellRenderer(this));
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
	  Dimension size = new Dimension(sLWidth, sLHeight);
	  JList lst = UIFactory.createListComponents(left, "All Toolsets:", size);
	  pToolsetsList = lst;
	  lst.setCellRenderer(new JAllToolsetsListCellRenderer(this));
	  lst.addListSelectionListener(this);
	  lst.addMouseListener(this);
	}

	left.add(Box.createRigidArea(new Dimension(20, 0)));
      }

      
      Box right = new Box(BoxLayout.X_AXIS);
      {
	right.add(Box.createRigidArea(new Dimension(20, 0)));
	
	{
	  Dimension size = new Dimension(sLWidth, sLHeight);
	  JList lst = UIFactory.createListComponents(right, "Included Packages:", size);
	  pIncludedPackagesList = lst;
	  lst.setCellRenderer(new JPackagesListCellRenderer(this));
	  lst.addListSelectionListener(this);
	  lst.addMouseListener(this);
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
	    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);
	    DefaultTreeModel model = new DefaultTreeModel(root, true);

	    JTree tree = new JFancyTree(model); 
	    pPackagesTree = tree;
	    tree.setName("DarkTree");

	    tree.setCellRenderer(new JPackagesTreeCellRenderer());
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
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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

  /**
   * Is the given toolset name a working (not frozen) toolset.
   * 
   * @param name
   *   The toolset name.
   */ 
  public boolean
  isWorkingToolset
  (
   String name
  )
  {
    if(name != null) {
      Toolset tset = pToolsets.get(name);
      if((tset != null) && !tset.isFrozen())
	return true;
    }

    return false;
  }

  /**
   * Get the name of the currently selected toolset.
   */ 
  public String
  getSelectedToolsetName() 
  {
    String name = (String) pActiveToolsetsList.getSelectedValue();
    if(name == null) 
      name = (String) pToolsetsList.getSelectedValue();
    return name;
  }
  


  /**
   * Does the given working toolset have package conflicts or no packages at all?
   * 
   * @param name
   *   The toolset name.
   */ 
  public boolean
  hasPackageConflicts
  (
   String name
  )
  {
    if(name != null) {
      Toolset tset = pToolsets.get(name);
      if((tset != null) && !tset.isFrozen() && (tset.hasConflicts() || !tset.hasPackages()))
	return true;
    }

    return false;
  }

  /**
   * Does the package with the given index in the given working toolset 
   * have environmental variable conflicts?
   * 
   * @param name
   *   The toolset name.
   * 
   * @param idx
   *   The index of the package within the working toolset.
   */ 
  public boolean 
  hasPackageConflicts
  (
   String name, 
   int idx
  ) 
  {
    if(name != null) {
      Toolset tset = pToolsets.get(name);
      if((tset != null) && !tset.isFrozen() && tset.isPackageConflicted(idx))
	return true;
    }

    return false;
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
   Toolset tset
  ) 
  {
    pTestToolsetDialog.updateToolset(tset);
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
   PackageMod pkg
  ) 
  {
    if(pkg == null) 
      return;

    String rname = pkg.getName();
    pPackageMods.put(rname, pkg);
    
    {
      ArrayList<String> rebuild = new ArrayList<String>();
      for(Toolset tset : pToolsets.values()) {
	if((tset != null) && tset.hasModifiablePackage(rname)) 
	  rebuild.add(tset.getName());
      }

      for(String tname : rebuild) {
	Toolset tset = pToolsets.get(tname);

	ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	{
	  int wk;
	  for(wk=0; wk<tset.getNumPackages(); wk++) {
	    String pname  = tset.getPackageName(wk);
	    VersionID vid = tset.getPackageVersionID(wk);
	    if(vid != null) 
	      packages.add(lookupPackageVersion(pname, vid));
	    else  
	      packages.add(pPackageMods.get(pname));
	  }
	}
	
	updateToolset(new Toolset(tname, packages));
      }
    }

    /* update the UI components */ 
    updateAll();
    
    /* update the child dialogs */ 
    updateDialogs();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update all components.
   */ 
  public void 
  updateAll()
  { 
    /* get the current info from the server */ 
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      pIsPrivileged = client.isPrivileged(false);

      pActiveToolsets.addAll(client.getActiveToolsetNames());
      
      {
	TreeSet<String> names = client.getToolsetNames();
	for(String name : names) {
	  if(!pToolsets.containsKey(name))
	    pToolsets.put(name, null);
	}
      }

      {
	TreeMap<String,TreeSet<VersionID>> names = client.getToolsetPackageNames();
	for(String name : names.keySet()) {
	  TreeMap<VersionID,PackageVersion> table = pPackageVersions.get(name);
	  if(table == null) {
	    table = new TreeMap<VersionID,PackageVersion>();
	    pPackageVersions.put(name, table);
	  }

	  for(VersionID vid : names.get(name)) {
	    if(!table.containsKey(vid)) 
	      table.put(vid, null);
	  }
	}
      }
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      setVisible(false);
      return;      
    }

    pDefaultToolset = null;
    try {
      pDefaultToolset = client.getDefaultToolsetName();
    }
    catch(PipelineException ex) {
    }    


    /* rebuild active toolset list */ 
    {
      String sname = (String) pActiveToolsetsList.getSelectedValue();

      pDisableToolsetButton.setEnabled(false);

      pActiveToolsetsList.removeListSelectionListener(this);
      {
	DefaultListModel model = (DefaultListModel) pActiveToolsetsList.getModel();
	model.clear();

	for(String name : pActiveToolsets) 
	  model.addElement(name);

	if(sname != null) {
	  pActiveToolsetsList.setSelectedValue(sname, true);
	  pDisableToolsetButton.setEnabled(pIsPrivileged);
	}
      }
      pActiveToolsetsList.addListSelectionListener(this);      
    }

    /* rebuild active toolset list */ 
    {	
      String sname = (String) pToolsetsList.getSelectedValue();

      pEnableToolsetButton.setEnabled(false);
      //pExcludePackageButton.setEnabled(false);

      pToolsetsList.removeListSelectionListener(this);
      {
	DefaultListModel model = (DefaultListModel) pToolsetsList.getModel();
	model.clear();
	
	for(String name : pToolsets.keySet()) 
	  model.addElement(name);

	if(sname != null) {
	  pToolsetsList.setSelectedValue(sname, true);
	  pEnableToolsetButton.setEnabled(pIsPrivileged && !isWorkingToolset(sname));	
	}
      }
      pToolsetsList.addListSelectionListener(this);
    }

    /* rebuild the included packages list */  
    TreeData isdata = rebuildIncludedPackagesList();

    /* rebuild packages tree */ 
    {
      pIncludePackageButton.setEnabled(false);

      pPackagesTree.removeTreeSelectionListener(this);
      {
	/* get the selected package */ 
	TreeData sdata = null;
	{
	  TreePath tpath = pPackagesTree.getSelectionPath();
	  if(tpath != null) {
	    DefaultMutableTreeNode tnode = 
	      (DefaultMutableTreeNode) tpath.getLastPathComponent();
	    sdata = (TreeData) tnode.getUserObject();
	  }
	}
	
	/* get the expanded package names */ 
	ArrayList<String> expanded = getExpandedPackages();
	
	/* rebuild the package tree */ 
	{
	  TreeSet<String> pnames = new TreeSet<String>(pPackageVersions.keySet());
	  pnames.addAll(pPackageMods.keySet());
	  
	  DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeData());
	  
	  for(String pname : pnames) {
	    DefaultMutableTreeNode pnode = 
	      new DefaultMutableTreeNode(new TreeData(pname), true);
	    root.add(pnode);
	    
	    TreeMap<VersionID,PackageVersion> versions = pPackageVersions.get(pname);
	    if(versions != null) {
	      for(VersionID vid : versions.keySet()) {
		DefaultMutableTreeNode vnode = 
		  new DefaultMutableTreeNode(new TreeData(pname, vid), false);
		pnode.add(vnode);
	      }
	    }
	    
	    PackageMod pkg = pPackageMods.get(pname);
	    if(pkg != null) {
	      DefaultMutableTreeNode vnode = 
		new DefaultMutableTreeNode(new TreeData(pkg), false);
	      pnode.add(vnode);
	    }
	  }
	  
	  DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
	  model.setRoot(root);
	}
	
	/* reexpand the packages */ 
	expandPackages(expanded);
	
	/* reselect package */ 
	if(sdata != null) 
	  selectTreePackage(sdata);
	else if(isdata != null) 
	  selectTreePackage(isdata);
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
   * Rebeuild the included packges list.
   * 
   * @return 
   *   The data used to select the package tree node or <CODE>null</CODE> if no tree 
   *   selection should occur.
   */ 
  private TreeData
  rebuildIncludedPackagesList() 
  {
    TreeData sdata = null;

    pIncludedPackagesList.removeListSelectionListener(this);
    {
      DefaultListModel model = (DefaultListModel) pIncludedPackagesList.getModel();
      
      /* get the package */ 
      PackageCommon spkg = (PackageCommon) pIncludedPackagesList.getSelectedValue();
      int spidx = pIncludedPackagesList.getSelectedIndex();

      /* rebuild the package list */ 
      {
	model.clear();
	
	String tname = getSelectedToolsetName();
	if(tname != null) {
	  Toolset tset = lookupToolset(tname);
	  assert(tset != null);
	    
	  int wk;
	  for(wk=0; wk<tset.getNumPackages(); wk++) {
	    String pname  = tset.getPackageName(wk); 
	    VersionID vid = tset.getPackageVersionID(wk); 
	      
	    PackageCommon pkg = null;
	    if(vid != null) 
	      pkg = lookupPackageVersion(pname, vid);
	    else 
	      pkg = pPackageMods.get(pname);
	      
	    model.addElement(pkg);
	  }
	}
      }
      
      /* reselect the package */ 
      if(spkg != null) {
	boolean match = false;
	if((spidx != -1) && (spidx < model.getSize())) {
	  PackageCommon pkg = (PackageCommon) model.getElementAt(spidx);
	  if(pkg.getName().equals(spkg.getName())) {
	    pIncludedPackagesList.setSelectedIndex(spidx);
	    match = true;
	  }
	}
	
	if(!match) {
	  int wk; 
	  for(wk=0; wk<model.getSize(); wk++) {
	    PackageCommon pkg = (PackageCommon) model.getElementAt(wk);
	    if(packagesMatch(spkg, pkg)) {
	      pIncludedPackagesList.setSelectedIndex(wk);
	      match = true;
	      break;
	    }
	  }
	}

 	if(!match) {
	  if(spkg instanceof PackageMod) {
	    sdata = new TreeData((PackageMod) spkg);
	  }
	  else if(spkg instanceof PackageVersion) {
	    PackageVersion vsn = (PackageVersion) spkg;
	    sdata = new TreeData(vsn.getName(), vsn.getVersionID());
	  }
	}
      }
    }
    pIncludedPackagesList.addListSelectionListener(this);

    return sdata;
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
      pActiveTestToolsetItem.setEnabled(true);
      pActiveExportToolsetItem.setEnabled(true);

      String tname = (String) pActiveToolsetsList.getModel().getElementAt(idx);
      if((tname != null) && !tname.equals(pDefaultToolset))
	pDefaultToolsetItem.setEnabled(pIsPrivileged);

      pActiveToolsetsList.setSelectedIndex(idx);
    }
  }

  /**
   * Update the toolsets menu.
   * 
   * @param idx
   *   The index of the list element under the mouse.
   */ 
  private void 
  updateToolsetsMenu
  (
   int idx
  ) 
  {
    pToolsetDetailsItem.setEnabled(false);
    pTestToolsetItem.setEnabled(false);
    pExportToolsetItem.setEnabled(false);
    pNewToolsetItem.setEnabled(pIsPrivileged);
    pCloneToolsetItem.setEnabled(false);
    pFreezeToolsetItem.setEnabled(false);
    pDeleteToolsetItem.setEnabled(false);
    
    if(idx != -1) {
      pToolsetDetailsItem.setEnabled(true);
      pTestToolsetItem.setEnabled(true);
      pExportToolsetItem.setEnabled(true);
      
      pToolsetsList.setSelectedIndex(idx);

      String tname = (String) pToolsetsList.getSelectedValue();
      if(isWorkingToolset(tname)) {
	pFreezeToolsetItem.setEnabled(pIsPrivileged);
	pDeleteToolsetItem.setEnabled(pIsPrivileged);
      }

      pCloneToolsetItem.setEnabled(pIsPrivileged);
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
      pIncTestPackageItem.setEnabled(true);

      pIncludedPackagesList.setSelectedIndex(idx);
      
      if(isWorkingToolset((String) pToolsetsList.getSelectedValue())) {
	pPackageEarlierItem.setEnabled(pIsPrivileged && (idx > 0));

	DefaultListModel model = (DefaultListModel) pIncludedPackagesList.getModel();
	pPackageLaterItem.setEnabled(pIsPrivileged && (idx < (model.getSize()-1)));
      }
    }
  }

  /**
   * Update the packages menu.
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
    pNewPackageItem.setEnabled(pIsPrivileged);
    pNewPackageVersionItem.setEnabled(false);
    pClonePackageVersionItem.setEnabled(false);
    pFreezePackageItem.setEnabled(false);
    pDeletePackageItem.setEnabled(false);

    if(tpath != null) {
      pNewPackageVersionItem.setEnabled(pIsPrivileged);
      pClonePackageVersionItem.setEnabled(pIsPrivileged);

      pPackagesTree.addSelectionPath(tpath);

      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
      if(data.getName() != null) {
	pPackageDetailsItem.setEnabled(true);
	pTestPackageItem.setEnabled(true);

	if(data.getPackageMod() != null) {
	  pFreezePackageItem.setEnabled(pIsPrivileged);
	  pDeletePackageItem.setEnabled(pIsPrivileged);
	}
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

    TreePath tpath = pPackagesTree.getSelectionPath();
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
      String tname = (String) pToolsetsList.getSelectedValue();
      if((data.getName() != null) && isWorkingToolset(tname))
	pIncludePackageButton.setEnabled(pIsPrivileged);
    }
  }

  /**
   * Update the enabled status of the exclude packages button.
   */ 
  private void 
  updateExcludePackageButton()
  {
    pExcludePackageButton.setEnabled(false);

    if(pIncludedPackagesList.getSelectedValue() != null) {
      String tname = (String) pToolsetsList.getSelectedValue();
      if((tname != null) && isWorkingToolset(tname))
	pExcludePackageButton.setEnabled(pIsPrivileged);
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
      pPackageDetailsDialog.setVisible(false);
      pCreatePackageDialog.setVisible(false);
      pTestPackageDialog.setVisible(false);

      pToolsetDetailsDialog.setVisible(false);
      pCreateToolsetDialog.setVisible(false);
      pTestToolsetDialog.setVisible(false);
      pExportToolsetDialog.setVisible(false);
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

      if(pActiveToolsetsList.getSelectedValue() != null) {
	pToolsetsList.removeListSelectionListener(this);
  	  pToolsetsList.getSelectionModel().clearSelection();
	  pEnableToolsetButton.setEnabled(false);
	pToolsetsList.addListSelectionListener(this);

	pDisableToolsetButton.setEnabled(pIsPrivileged);
      }

      TreeData sdata = rebuildIncludedPackagesList();
      if(sdata != null) {
	pPackagesTree.removeTreeSelectionListener(this);
	  selectTreePackage(sdata);
	pPackagesTree.addTreeSelectionListener(this);
      }

      updateIncludePackageButton();
      updateExcludePackageButton();

      updateDialogs();    
    }
    else if(e.getSource() == pToolsetsList) {
      pEnableToolsetButton.setEnabled(false);

      String tname = (String) pToolsetsList.getSelectedValue();
      if(tname != null) {
	pActiveToolsetsList.removeListSelectionListener(this);
  	  pActiveToolsetsList.getSelectionModel().clearSelection();
	  pDisableToolsetButton.setEnabled(false);
	pActiveToolsetsList.addListSelectionListener(this);

	pEnableToolsetButton.setEnabled(pIsPrivileged && !isWorkingToolset(tname));
      }

      TreeData sdata = rebuildIncludedPackagesList();
      if(sdata != null) {
	pPackagesTree.removeTreeSelectionListener(this);
	  selectTreePackage(sdata);
	pPackagesTree.addTreeSelectionListener(this);
      }

      updateIncludePackageButton();
      updateExcludePackageButton();

      updateDialogs();
    }
    else if(e.getSource() == pIncludedPackagesList) {
      if(pIncludedPackagesList.getSelectedValue() != null) {
	pPackagesTree.removeTreeSelectionListener(this);
	  pPackagesTree.getSelectionModel().clearSelection();
	  updateIncludePackageButton();
	pPackagesTree.addTreeSelectionListener(this);
      }

      updateExcludePackageButton();
    }
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
    TreePath tpath = pPackagesTree.getSelectionPath();
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
      if(data.getName() != null) {
	pIncludedPackagesList.removeListSelectionListener(this);
	  pIncludedPackagesList.clearSelection();
	  updateExcludePackageButton();
	pIncludedPackagesList.addListSelectionListener(this);
      }
    }

    updateIncludePackageButton();
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
	  else if(comp == pToolsetsList) {
	    int idx = pToolsetsList.locationToIndex(e.getPoint());
	    if(idx != -1) {
	      Rectangle bounds = pToolsetsList.getCellBounds(idx, idx);
	      if(!bounds.contains(e.getX(), e.getY()))
		idx = -1;
	    }

	    if(idx != -1) 
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
	  else if(comp == pToolsetsList) {
	    int idx = pToolsetsList.locationToIndex(e.getPoint());
	    if(idx != -1) {
	      Rectangle bounds = pToolsetsList.getCellBounds(idx, idx);
	      if(!bounds.contains(e.getX(), e.getY()))
		idx = -1;
	    }

	    updateToolsetsMenu(idx);
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
  mouseReleased(MouseEvent e) {} 


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
    super.actionPerformed(e);

    if(e.getActionCommand().equals("toolset-details")) 
      doToolsetDetails();
    else if(e.getActionCommand().equals("test-toolset")) 
      doTestToolset();
    else if(e.getActionCommand().equals("export-toolset")) 
      doExportToolset();
    else if(e.getActionCommand().equals("new-toolset")) 
      doNewToolset();
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
    Toolset tset = getSelectedToolset(); 
    if(tset != null) {
      pToolsetDetailsDialog.updateToolset(tset);
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
    Toolset tset = getSelectedToolset();    
    if(tset != null) {
      pExportToolsetDialog.updateTargetName(tset.getName() + ".sh");
      pExportToolsetDialog.setVisible(true);
      
      if(pExportToolsetDialog.wasConfirmed()) {
	File file = pExportToolsetDialog.getSelectedFile();
	if(file != null) {
	  try {
	    StringBuffer buf = new StringBuffer();
	  
	    buf.append("export TOOLSET=" + tset.getName() + "\n");
	    buf.append("export USER=`whoami`\n");
	    buf.append("export HOME=" + PackageInfo.sHomeDir + "/$USER\n");
	    buf.append("export WORKING=" + PackageInfo.sWorkDir + "/$USER/default\n");
	    buf.append("export _=/bin/env\n\n");
  
	    TreeMap<String,String> env = tset.getEnvironment();
	    for(String ename : env.keySet()) {
	      String evalue = env.get(ename);
	      buf.append("export " + ename + "=" + 
			 ((evalue != null) ? (evalue + "\n") : "\n"));
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
      if(tname != null) {
	if(!pToolsets.containsKey(tname)) {
	  updateToolset(new Toolset(tname));

	  {
	    DefaultListModel model = (DefaultListModel) pToolsetsList.getModel();

	    int wk;
	    for(wk=0; wk<model.getSize(); wk++) {
	      String name = (String) model.elementAt(wk);
	      if(name.compareTo(tname) > 0)
		break;
	    }
	    model.insertElementAt(tname, wk);

	    pToolsetsList.setSelectedIndex(wk);
	  }

	  updateDialogs();
	}
      }
    }
  }

  /**
   * Create a new modifiable toolset which contains the same packages of the currently 
   * selected toolset.
   */ 
  public void 
  doCloneToolset()
  {
    Toolset stset = getSelectedToolset(); 
    if((stset != null) && pIsPrivileged) {
      JNewToolsetDialog diag = new JNewToolsetDialog(this);
      diag.setVisible(true);
    
      if(diag.wasConfirmed()) {
	String tname = diag.getName();
	if(tname != null) {
	  if(!pToolsets.containsKey(tname)) {
	    
	    ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	    {
	      int wk;
	      for(wk=0; wk<stset.getNumPackages(); wk++) {
		String pname  = stset.getPackageName(wk);
		VersionID vid = stset.getPackageVersionID(wk);
		if(vid != null) 
		  packages.add(lookupPackageVersion(pname, vid));
		else 
		  packages.add(pPackageMods.get(pname));
	      }
	    }
	    updateToolset(new Toolset(tname, packages));

	    {
	      DefaultListModel model = (DefaultListModel) pToolsetsList.getModel();
	      
	      int wk;
	      for(wk=0; wk<model.getSize(); wk++) {
		String name = (String) model.elementAt(wk);
		if(name.compareTo(tname) > 0)
		  break;
	      }
	      model.insertElementAt(tname, wk);
	      
	      pToolsetsList.setSelectedIndex(wk);
	    }

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
    Toolset tset = getSelectedToolset(); 
    if((tset != null) && !tset.isFrozen() && pIsPrivileged) {
      UIMaster master = UIMaster.getInstance();
      String tname = tset.getName();

      if(!tset.hasPackages()) {
	master.showErrorDialog
	  ("Error:", 
	   "Unable to freeze toolset (" + tname + ") which contained no packages!");
	return;
      }

      if(tset.hasModifiablePackages()) {
	StringBuffer buf = new StringBuffer();
	int wk;
	for(wk=0; wk<tset.getNumPackages(); wk++) {
	  if(tset.getPackageVersionID(wk) == null) 
	    buf.append("  " + tset.getPackageName(wk) + "\n");
	}

	master.showErrorDialog
	  ("Error:", 
	   "Unable to freeze toolset (" + tname + ") which contained the " + 
	   "following working packages:\n\n" + 
	   buf.toString());
	return;
      }
      
      if(tset.hasConflicts()) {
	StringBuffer buf = new StringBuffer();
	for(String ename : tset.getConflictedEnvNames()) 
	  buf.append("  " + ename + "\n");

	master.showErrorDialog
	  ("Error:", 
	   "Unable to freeze toolset (" + tname + ") which has unresolved " + 
	   "conflicts between its packages for the following environmental variables:\n\n" + 
	   buf.toString());
	return;
      }
      assert(tset.isFreezable());

      pCreateToolsetDialog.updateHeader("Create Toolset:  " + tname);
      pCreateToolsetDialog.setVisible(true);

      if(pCreateToolsetDialog.wasConfirmed()) {
	String desc = pCreateToolsetDialog.getDescription();
	assert((desc != null) && (desc.length() > 0));
	
	MasterMgrClient client = master.getMasterMgrClient();
	try {
	  ArrayList<PackageVersion> packages = new ArrayList<PackageVersion>();
	  int wk;
	  for(wk=0; wk<tset.getNumPackages(); wk++) {
	    String pname  = tset.getPackageName(wk);
	    VersionID vid = tset.getPackageVersionID(wk);
	    assert(vid != null);
	    packages.add(lookupPackageVersion(pname, vid));
	  }
	
	  Toolset ntset = client.createToolset(tname, desc, packages);
	  assert(ntset != null);
	  assert(ntset.getName().equals(tname));

	  pToolsets.put(tname, ntset);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	
	/* update the UI components */ 
	updateAll();

	/* update the child dialogs */ 
	updateDialogs();
      }
    }
  }

  /**
   * Delete the selected a modifiable toolset.
   */ 
  public void 
  doDeleteToolset()
  {
    Toolset tset = getSelectedToolset(); 
    if((tset != null) && !tset.isFrozen() && pIsPrivileged) {
      pToolsets.remove(tset.getName());
      updateAll();    
      updateDialogs();
    }
  }

  /**
   * Make the selected active toolset the default toolset.
   */ 
  public void 
  doDefaultToolset()
  {
    String tname = (String) pActiveToolsetsList.getSelectedValue();
    if(tname != null) {

      /* make the toolset the default toolset */ 
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
	
      /* update the UI components */ 
      updateAll();

      /* update the child dialogs */ 
      updateDialogs();
    }
  }
  
  /**
   * Make the selected toolset active.
   */ 
  public void 
  doEnableToolset()
  {
    String tname = (String) pToolsetsList.getSelectedValue();
    if(tname != null) {
      Toolset tset = lookupToolset(tname);
      if(tset.isFrozen() && !pActiveToolsets.contains(tname)) {
	/* make the toolset active */ 
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
	
	/* update the UI components */ 
	updateAll();

	/* select the active toolset */ 
	{
	  DefaultListModel model = (DefaultListModel) pActiveToolsetsList.getModel();
	  pActiveToolsetsList.setSelectedValue(tname, true);
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
    String tname = (String) pActiveToolsetsList.getSelectedValue();
    if(tname != null) {
      Toolset tset = lookupToolset(tname);

      /* make the toolset inactive */ 
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
	
      /* update the UI components */ 
      updateAll();
      
      /* select the inactive toolset */ 
      {
	DefaultListModel model = (DefaultListModel) pToolsetsList.getModel();
	pToolsetsList.setSelectedValue(tname, true);
      }
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
   * Move the currently selected package one place earlier in the evaluation order.
   */ 
  public void 
  doPackageEarlier()
  {
    int idx = pIncludedPackagesList.getSelectedIndex();
    if(idx > 0) {
      String tname = (String) pToolsetsList.getSelectedValue();
      if(isWorkingToolset(tname)) {  

	/* rebuild the toolset */ 
	{
	  Toolset tset = pToolsets.get(tname);
	  
	  ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	  {
	    PackageCommon spkg = null;
	    
	    int wk;
	    for(wk=0; wk<tset.getNumPackages(); wk++) {
	      PackageCommon pkg = null;
	      {
		String pname  = tset.getPackageName(wk);
		VersionID vid = tset.getPackageVersionID(wk);
		if(vid != null) 
		  pkg = lookupPackageVersion(pname, vid);
		else 
		  pkg = pPackageMods.get(pname);
	      }
	      
	      if(wk == (idx-1)) 
		spkg = pkg;
	      else if(wk == idx) {
		packages.add(pkg); 
		packages.add(spkg);
	      }
	      else {
		packages.add(pkg);
	      }
	    }
	  }
	  
	  updateToolset(new Toolset(tname, packages));
	}
	  
	/* update the dialog components */ 
	updateAll();

	/* shift the selection */ 
	pIncludedPackagesList.setSelectedIndex(idx-1);
	
	/* update the child dialogs */ 
	updateDialogs();
      }
    }
  }

  /**
   * Move the currently selected package one place later in the evaluation order.
   */ 
  public void 
  doPackageLater()
  {
    DefaultListModel model = (DefaultListModel) pIncludedPackagesList.getModel();
    int idx = pIncludedPackagesList.getSelectedIndex();
    if((idx != -1) && (idx < (model.getSize()-1))) {
      String tname = (String) pToolsetsList.getSelectedValue();
      if(isWorkingToolset(tname)) {  

	/* rebuild the toolset */ 
	{
	  Toolset tset = pToolsets.get(tname);

	  ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	  {
	    PackageCommon spkg = null;
	    
	    int wk;
	    for(wk=0; wk<tset.getNumPackages(); wk++) {
	      PackageCommon pkg = null;
	      {
		String pname  = tset.getPackageName(wk);
		VersionID vid = tset.getPackageVersionID(wk);
		if(vid != null) 
		  pkg = lookupPackageVersion(pname, vid);
		else 
		  pkg = pPackageMods.get(pname);
	      }
	      
	      if(wk == idx) 
		spkg = pkg;
	      else if(wk == (idx+1)) {
		packages.add(pkg); 
		packages.add(spkg);
	      }
	      else {
		packages.add(pkg);
	      }
	    }
	  }
	  
	  updateToolset(new Toolset(tname, packages));	
	}
	  
	/* update the UI components */ 
	updateAll();

	/* shift the selection */ 
	pIncludedPackagesList.setSelectedIndex(idx+1);

	/* update the child dialogs */ 
	updateDialogs();
      }
    } 
  }
  
  /**
   * Create a new empty modifiable version of an existing package. 
   */ 
  public void 
  doNewPackageVersion()
  {
    String pname = null;

    TreePath tpath = pPackagesTree.getSelectionPath();
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
    
      pname = data.getName();
      if(pname == null) 
	pname = data.getLabel();
    }    
    
    if((pname != null) && (pname.length() > 0))
      createPackage(pname);
  }

  /**
   * Create a new modifiable version of an existing package which is a clone of the selected
   * package.
   */ 
  public void 
  doClonePackageVersion()
  {
    PackageCommon com = getSelectedPackage(); 
    if(com != null) 
      createPackage(com.getName(), com);
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
      if((pname != null) && (pname.length() > 0))
	createPackage(pname);
    }
  }

  /**
   * Freeze the current state of the selected modifiable package to create a permanent
   * read-only package.
   */ 
  public void 
  doFreezePackage()
  {
    TreePath tpath = pPackagesTree.getSelectionPath();
    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
    TreeData data = (TreeData) tnode.getUserObject();
    PackageMod pkg = data.getPackageMod();
    String rname = pkg.getName();
    if(pkg != null) {

      /* query the user for the revision increment level and decription */ 
      {
	VersionID latest = null;
	{
	  TreeMap<VersionID,PackageVersion> versions = pPackageVersions.get(rname);
	  if(versions != null) 
	    latest = versions.lastKey();
	}
	
	pCreatePackageDialog.updateNameVersion("Create Package:  " + rname, latest);
	pCreatePackageDialog.setVisible(true);
      }

      if(pCreatePackageDialog.wasConfirmed()) {

	/* create the read-only package */ 
	PackageVersion npkg = null;
	{
	  String desc = pCreatePackageDialog.getDescription();
	  assert((desc != null) && (desc.length() > 0));
	  VersionID.Level level = pCreatePackageDialog.getLevel();

	  UIMaster master = UIMaster.getInstance();
	  MasterMgrClient client = master.getMasterMgrClient();
	  try {
	    npkg = client.createToolsetPackage(pkg, desc, level);
	    assert(npkg != null);
	    assert(npkg.getName().equals(rname));

	    TreeMap<VersionID,PackageVersion> versions = pPackageVersions.get(rname);
	    if(versions == null) {
	      versions = new TreeMap<VersionID,PackageVersion>();
	      pPackageVersions.put(rname, versions);
	    }
	    versions.put(npkg.getVersionID(), npkg);
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	}

	/* remove the working package */ 
	pPackageMods.remove(rname);

	/* replace the working package with the read-only package in all working toolsets */ 
	{
	  ArrayList<String> rebuild = new ArrayList<String>();
	  for(Toolset tset : pToolsets.values()) {
	    if((tset != null) && tset.hasModifiablePackage(rname)) 
	      rebuild.add(tset.getName());
	  }
	  
	  for(String tname : rebuild) {
	    Toolset tset = pToolsets.get(tname);
	    
	    ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	    {
	      int wk;
	      for(wk=0; wk<tset.getNumPackages(); wk++) {
		String pname  = tset.getPackageName(wk);
		VersionID vid = tset.getPackageVersionID(wk);
		if(vid != null) 
		  packages.add(lookupPackageVersion(pname, vid));
		else if(pname.equals(rname)) 
		  packages.add(npkg);
		else 
		  packages.add(pPackageMods.get(pname));
	      }
	    }
	    
	    updateToolset(new Toolset(tname, packages));
	  }
	}

	/* update the UI components */ 
	updateAll();

	/* select the frozen package */ 
	selectTreePackage(new TreeData(npkg.getName(), npkg.getVersionID()));

	/* update the child dialogs */ 
	updateDialogs();
      }
    }
  }

  /**
   * Delete the selected a modifiable package.
   */ 
  public void 
  doDeletePackage()
  {
    TreePath tpath = pPackagesTree.getSelectionPath();
    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
    TreeData data = (TreeData) tnode.getUserObject();
    PackageMod pkg = data.getPackageMod();
    if(pkg != null) {

      /* remove the working package */ 
      String rname = pkg.getName();
      pPackageMods.remove(rname);

      /* remove the deleted package from all working toolsets */ 
      {
	ArrayList<String> rebuild = new ArrayList<String>();
	for(Toolset tset : pToolsets.values()) {
	  if((tset != null) && tset.hasModifiablePackage(rname)) 
	    rebuild.add(tset.getName());
	}

	for(String tname : rebuild) {
	  Toolset tset = pToolsets.get(tname);

	  ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	  {
	    int wk;
	    for(wk=0; wk<tset.getNumPackages(); wk++) {
	      String pname  = tset.getPackageName(wk);
	      VersionID vid = tset.getPackageVersionID(wk);
	      if(vid != null) 
		packages.add(lookupPackageVersion(pname, vid));
	      else if(!pname.equals(rname)) 
		packages.add(pPackageMods.get(pname));
	    }
	  }
	    
	  updateToolset(new Toolset(tname, packages));
	}
      }

      /* update the UI components */ 
      updateAll();
      
      /* update the child dialogs */ 
      updateDialogs();

      /* hide the details dialog if its displaying the deleted package */ 
      {
	PackageCommon com = pPackageDetailsDialog.getPackage();
	if((com instanceof PackageMod) && com.getName().equals(pkg.getName())) 
	  pPackageDetailsDialog.setVisible(false);
      }
    }
  }

  /**
   * Include the selected package as a package of the selected toolset.
   */ 
  public void 
  doIncludePackage()
  {
    TreePath tpath = pPackagesTree.getSelectionPath();
    if(tpath != null) {

      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
      String dname = data.getName();
      if(dname != null) {

	String tname = (String) pToolsetsList.getSelectedValue();
	if(isWorkingToolset(tname)) {  

	  /* add the package to the selected toolset */ 
	  {
	    Toolset tset = pToolsets.get(tname);
	    
	    ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	    {
	      int wk;
	      for(wk=0; wk<tset.getNumPackages(); wk++) {
		String pname  = tset.getPackageName(wk);
		VersionID vid = tset.getPackageVersionID(wk);
		if(vid != null) 
		  packages.add(lookupPackageVersion(pname, vid));
		else 
		  packages.add(pPackageMods.get(pname));
	      }
	      
	      if(data.getPackageMod() != null) 
		packages.add(data.getPackageMod());
	      else 
		packages.add(lookupPackageVersion(data.getName(), data.getVersionID()));
	    }
	    
	    updateToolset(new Toolset(tname, packages));
	  }
	  
	  /* update the UI components */ 
	  updateAll();

	  /* select the newly included package */ 
	  {
	    DefaultListModel model = (DefaultListModel) pIncludedPackagesList.getModel();
	    pIncludedPackagesList.setSelectedIndex(model.getSize() - 1);
	  }

	  /* update the child dialogs */ 
	  updateDialogs();
	}
      }
    }
  }

  /**
   * Exclude the selected package from the packages of the selected toolset.
   */ 
  public void 
  doExcludePackage()
  {
    int idx = pIncludedPackagesList.getSelectedIndex();
    if(idx != -1) {

      String tname = (String) pToolsetsList.getSelectedValue();
      if(isWorkingToolset(tname)) {  
	
	/* remove the package from the selected toolset */ 
	{
	  Toolset tset = pToolsets.get(tname);
	
	  ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	  {
	    int wk;
	    for(wk=0; wk<tset.getNumPackages(); wk++) {
	      if(wk != idx) {
		String pname  = tset.getPackageName(wk);
		VersionID vid = tset.getPackageVersionID(wk);
		if(vid != null) 
		  packages.add(lookupPackageVersion(pname, vid));
		else 
		  packages.add(pPackageMods.get(pname));
	      }
	    }
	  }
	  
	  updateToolset(new Toolset(tname, packages));
	}

	/* update the UI components */ 
	updateAll();
	
	/* update the child dialogs */ 
	updateDialogs();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup the given toolset.
   */ 
  private Toolset
  lookupToolset
  (
   String tname
  ) 
  {
    Toolset tset = pToolsets.get(tname);
    if(tset == null) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      try {
	tset = client.getToolset(tname);
	pToolsets.put(tname, tset);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }

    return tset;
  }

  /**
   * Get the selected toolset.
   */ 
  private Toolset
  getSelectedToolset() 
  {
    String tname = getSelectedToolsetName();
    if(tname != null) 
      return lookupToolset(tname);
    return null;
  }

  /**
   * Update the toolset and all related UI components.
   */ 
  private void 
  updateToolset
  (
   Toolset tset
  ) 
  {
    pToolsets.put(tset.getName(), tset);

    {
      Toolset dtset = pToolsetDetailsDialog.getToolset();
      if((dtset != null) && dtset.getName().equals(tset.getName())) 
      pToolsetDetailsDialog.updateToolset(tset);
    }

    {
      Toolset dtset = pTestToolsetDialog.getToolset();
      if((dtset != null) && dtset.getName().equals(tset.getName())) 
      pTestToolsetDialog.updateToolset(tset);
    }
  }


  /**
   * Update the dialogs.
   */ 
  private void 
  updateDialogs()
  {
    Toolset tset = getSelectedToolset();
    int idx = pIncludedPackagesList.getSelectedIndex();
    PackageCommon com = getSelectedPackage(); 

    if(com != null) {
      if((tset != null) && (idx != -1)) 
	pPackageDetailsDialog.updatePackage(com, tset, idx);
      else 
	pPackageDetailsDialog.updatePackage(com, null, -1);
    }
    else {
      pPackageDetailsDialog.setVisible(false);
      pTestPackageDialog.setVisible(false);
    }

    if(tset != null) {
      pToolsetDetailsDialog.updateToolset(tset);
      pTestToolsetDialog.updateToolset(tset);
    }
    else {
      pToolsetDetailsDialog.setVisible(false);
      pTestToolsetDialog.setVisible(false);
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Do the given packages have the same type, name and revision number? 
   */ 
  private boolean
  packagesMatch
  (
   PackageCommon pkg1,
   PackageCommon pkg2
  ) 
  {
    if(pkg1.getName().equals(pkg2.getName())) {
      if((pkg1 instanceof PackageVersion) && (pkg2 instanceof PackageVersion)) {
	PackageVersion vsn1 = (PackageVersion) pkg1;
	PackageVersion vsn2 = (PackageVersion) pkg2;
	if(vsn1.getVersionID().equals(vsn2.getVersionID())) 
	  return true;
      }
      else if((pkg1 instanceof PackageMod) && (pkg2 instanceof PackageMod)) 
	return true;
    }

    return false;
  }

  /**
   * Lookup the given package version.
   */ 
  private PackageVersion 
  lookupPackageVersion
  (
   String pname, 
   VersionID vid
  ) 
  {
    TreeMap<VersionID,PackageVersion> versions = pPackageVersions.get(pname);
    PackageVersion pkg = versions.get(vid);
    if(pkg == null) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      try {
	pkg = client.getToolsetPackage(pname, vid);
	versions.put(vid, pkg);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }

    return pkg;
  }

  /**
   * Get the selected package.
   */ 
  private PackageCommon 
  getSelectedPackage() 
  {
    PackageCommon com = (PackageCommon) pIncludedPackagesList.getSelectedValue();
    if(com == null) {      
      TreePath tpath = pPackagesTree.getSelectionPath();
      if(tpath != null) {
	DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
	TreeData data = (TreeData) tnode.getUserObject();
      
	com = data.getPackageMod();
	if(com == null) {
	  String pname  = data.getName();
	  VersionID vid = data.getVersionID();
	  if((pname != null) && (vid != null)) 
	    com = lookupPackageVersion(pname, vid);
	}
      }
    }

    return com;
  }
  
  /** 
   * Select the package from the package tree which matches the given package.
   */ 
  private void 
  selectTreePackage
  (
   TreeData sdata
  ) 
  {
    pPackagesTree.clearSelection();
    if(sdata == null) 
      return;

    DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) root.getChildAt(wk);  
      TreeData tdata = (TreeData) tnode.getUserObject();
      if((tdata != null) && sdata.getName().equals(tdata.getLabel())) {
	int vk;
	for(vk=0; vk<tnode.getChildCount(); vk++) {
	  DefaultMutableTreeNode vnode = (DefaultMutableTreeNode) tnode.getChildAt(vk);  
	  TreeData vdata = (TreeData) vnode.getUserObject();	  
	  TreePath vpath = new TreePath(vnode.getPath());

	  assert(vdata.getName().equals(sdata.getName()));
	  if((sdata.getPackageMod() != null) && (vdata.getPackageMod() != null)) {
	    pPackagesTree.addSelectionPath(vpath);
	    return;
	  }
	  else if((sdata.getVersionID() != null) && 
		  sdata.getVersionID().equals(vdata.getVersionID())) {
	    pPackagesTree.addSelectionPath(vpath);
	    return;
	  }
	}
      }
    }
  }
  
  /**
   * Lookup the names of the expanded packages.
   */ 
  private ArrayList<String>
  getExpandedPackages()
  {
    ArrayList<String> expanded = new ArrayList<String>();

    DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) root.getChildAt(wk);  
      TreeData data = (TreeData) tnode.getUserObject();
      TreePath tpath = new TreePath(tnode.getPath());

      if(pPackagesTree.isExpanded(tpath))
	expanded.add(data.getLabel());
    }

    return expanded;
  }

  /**
   * Lookup expand the packages with the given names.
   */ 
  private void 
  expandPackages
  (
   ArrayList<String> expanded
  )
  {
    DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) root.getChildAt(wk);  
      TreeData data = (TreeData) tnode.getUserObject();
      TreePath tpath = new TreePath(tnode.getPath());
      if(expanded.contains(data.getLabel())) 
	pPackagesTree.expandPath(tpath);
    }
  }


  /**
   * Create a new empty modifiable package.
   */ 
  private void 
  createPackage
  (
   String pname
  ) 
  {
    createPackage(pname, null);
  }

  /**
   * Create a new modifiable package which is a clone of the given package.
   * 
   * @param pname
   *   The name of the new package.
   * 
   * @param com 
   *   The package to clone or <CODE>null</CODE> to create an empty package.
   */ 
  private void 
  createPackage
  (
   String pname, 
   PackageCommon com
  ) 
  {
    assert(pname != null);
    if(!pPackageMods.containsKey(pname)) {

      PackageMod pkg = null;
      {
	if(com != null) {
	  assert(com.getName().equals(pname));
	  pkg = new PackageMod(com);
	}
	else {
	  pkg = new PackageMod(pname);
	}
      }

      pPackageMods.put(pname, pkg);
      
      DefaultMutableTreeNode pnode = null;
      DefaultMutableTreeNode vnode = null;
      {
	ArrayList<String> expanded = getExpandedPackages();
	DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
	int wk;
	for(wk=0; wk<root.getChildCount(); wk++) {
	  DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) root.getChildAt(wk);
	  TreeData data = (TreeData) tnode.getUserObject();
	  if(data.toString().equals(pname)) {
	    pnode = tnode;
	    break;
	  }
	  else if(data.toString().compareTo(pname) > 0) 
	    break;
	}
	
	if(pnode == null) {
	  pnode = new DefaultMutableTreeNode(new TreeData(pname), true);
	  root.insert(pnode, wk);
	}

	vnode = new DefaultMutableTreeNode(new TreeData(pkg), false);
	pnode.add(vnode);

	model.reload();	

	expandPackages(expanded);
      }
      
      TreePath tpath2 = new TreePath(vnode.getPath());
      pPackagesTree.addSelectionPath(tpath2);
      doPackageDetails();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * User data of tree nodes.
   */ 
  public
  class TreeData
  {
    public 
    TreeData() 
    {}

    public 
    TreeData
    ( 
     String label
    )
    {
      pLabel = label;
    }

    public 
    TreeData
    ( 
     String name, 
     VersionID vid
    )
    {
      pLabel     = ("v" + vid);
      pName      = name;
      pVersionID = vid; 
    }

    public 
    TreeData
    ( 
     PackageMod pkg
    )
    {
      pLabel      = "working";
      pName       = pkg.getName();
      pPackageMod = pkg;
    }

    
    public String
    getLabel() 
    {
      return pLabel;
    }
    
    public String
    getName() 
    {
      return pName;
    }

    public VersionID
    getVersionID()
    {
      return pVersionID;
    }

    public PackageMod
    getPackageMod()
    {
      return pPackageMod;
    }


    public String
    toString()
    {
      return pLabel;
    }


    private String      pLabel;
    private String      pName;
    private VersionID   pVersionID;
    private PackageMod  pPackageMod;
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
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;


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
   * The cached table of all toolsets indexed by toolset name. <P> 
   * 
   * All existing toolsets will have a key in this table, but the value may be 
   * null if the toolset is not currently cached.  This table also contains temporary 
   * modifiable toolsets which have not been frozen.
   */ 
  private TreeMap<String,Toolset>  pToolsets;

  /**
   * The cached table of read-only toolset packages indexed by package name and 
   * revision number. <P> 
   * 
   * All existing toolset packages will have a key in this table, but the value may be 
   * null if the toolset package is not currently cached.  
   */   
  private TreeMap<String,TreeMap<VersionID,PackageVersion>>  pPackageVersions;

  /** 
   * The modifiable toolset packages which have not yet been frozen indexed by package name.
   */ 
  private TreeMap<String,PackageMod>  pPackageMods;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The list of active toolsets.
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
   * The list of all toolsets.
   */ 
  private JList  pToolsetsList;

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
  private JMenuItem  pCloneToolsetItem;
  private JMenuItem  pFreezeToolsetItem;
  private JMenuItem  pDeleteToolsetItem;


  /**
   * The list of packages included in the selected toolset.
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
   * All package versions grouped by package name.
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
  private JMenuItem  pNewPackageVersionItem;
  private JMenuItem  pClonePackageVersionItem;
  private JMenuItem  pFreezePackageItem;
  private JMenuItem  pDeletePackageItem;


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

}
