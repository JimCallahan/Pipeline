// $Id: JManageToolsetsDialog.java,v 1.2 2004/05/29 06:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.toolset.*; 

import java.awt.*;
import java.awt.event.*;
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
  implements ListSelectionListener, MouseListener
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
      pToolsetDetailsItem = item;
      item.setActionCommand("toolset-details");
      item.addActionListener(this);
      pActiveToolsetsPopup.add(item);  

      item = new JMenuItem("Test...");
      pTestToolsetItem = item;
      item.setActionCommand("test-toolset");
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

      pToolsetsPopup.addSeparator();
      
      item = new JMenuItem("New...");
      pNewToolsetItem = item;
      item.setActionCommand("new-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  
   
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
      pPackageDetailsItem = item;
      item.setActionCommand("package-details");
      item.addActionListener(this);
      pIncludedPackagesPopup.add(item);  

      item = new JMenuItem("Test...");
      pTestPackageItem = item;
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
      Box body = new Box(BoxLayout.X_AXIS);
      {
	body.add(Box.createRigidArea(new Dimension(20, 0)));

	{
	  Dimension size = new Dimension(sLSize, sLHt);
	  JList lst = UIMaster.createListComponents(body, "Active Toolsets:", size);
	  pActiveToolsetsList = lst;
	  lst.setCellRenderer(new JActiveToolsetsListCellRenderer(this));
	  lst.addListSelectionListener(this);
	  lst.addMouseListener(this);
	}

	body.add(Box.createRigidArea(new Dimension(4, 0)));

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
	  
	  body.add(vbox);
	}

	body.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  Dimension size = new Dimension(sLSize, sLHt);
	  JList lst = UIMaster.createListComponents(body, "All Toolsets:", size);
	  pToolsetsList = lst;
	  lst.setCellRenderer(new JAllToolsetsListCellRenderer(this));
	  lst.addListSelectionListener(this);
	  lst.addMouseListener(this);
	}

	body.add(Box.createRigidArea(new Dimension(40, 0)));

	{
	  Dimension size = new Dimension(sLSize, sLHt);
	  JList lst = UIMaster.createListComponents(body, "Included Packages:", size);
	  pIncludedPackagesList = lst;
	  lst.setCellRenderer(new JPackagesListCellRenderer());
	  lst.addMouseListener(this);
	}

	body.add(Box.createRigidArea(new Dimension(4, 0)));

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
	  
	  body.add(vbox);
	}

	body.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));

	  vbox.add(UIMaster.createPanelLabel("All Packages:"));
    
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

	    {
	      JScrollPane scroll = new JScrollPane(tree);
	  
	      Dimension size = new Dimension(sLSize, sLHt);
	      scroll.setMinimumSize(size);
	      scroll.setPreferredSize(size);
	      
	      scroll.setHorizontalScrollBarPolicy
		(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	      scroll.setVerticalScrollBarPolicy
		(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	      
	      vbox.add(scroll);
	    }

	    vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	    
	    body.add(vbox);
	  }
	}

	body.add(Box.createRigidArea(new Dimension(40, 0)));
      }

      super.initUI("Manage Toolsets:", false, body, null, null, null, "Close");
    }

    pPackageDetailsDialog = new JPackageDetailsDialog(this);
    pCreatePackageDialog  = new JCreatePackageDialog(this);
    pTestPackageDialog    = new JTestPackageDialog();
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
   * Is the given toolset name a modifiable toolset.
   * 
   * @param name
   *   The toolset name.
   */ 
  public boolean
  isModifiableToolset
  (
   String name
  )
  {
    return ((name != null) && pPackageMods.containsKey(name));
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

    /* update the menu items */ 
    {
      pNewToolsetItem.setEnabled(pIsPrivileged);
      pFreezeToolsetItem.setEnabled(pIsPrivileged);
      pDeleteToolsetItem.setEnabled(pIsPrivileged);

      pPackageEarlierItem.setEnabled(pIsPrivileged);
      pPackageLaterItem.setEnabled(pIsPrivileged);

      pNewPackageItem.setEnabled(pIsPrivileged);
      pNewPackageVersionItem.setEnabled(pIsPrivileged);
      pFreezePackageItem.setEnabled(pIsPrivileged);
      pDeletePackageItem.setEnabled(pIsPrivileged);
    }

    /* update the panel buttons */ 
    {
      pEnableToolsetButton.setEnabled(pIsPrivileged);
      pDisableToolsetButton.setEnabled(pIsPrivileged);
      pIncludePackageButton.setEnabled(pIsPrivileged);
      pExcludePackageButton.setEnabled(pIsPrivileged);
    }
    
    /* rebuild active toolset list */ 
    {
      pActiveToolsetsList.removeListSelectionListener(this);

      DefaultListModel model = (DefaultListModel) pActiveToolsetsList.getModel();
      model.clear();

      for(String name : pActiveToolsets) 
	  model.addElement(name);

      pActiveToolsetsList.addListSelectionListener(this);
    }

    /* rebuild active toolset list */ 
    {
      pToolsetsList.removeListSelectionListener(this);

      DefaultListModel model = (DefaultListModel) pToolsetsList.getModel();
      model.clear();

      for(String name : pToolsets.keySet()) 
	model.addElement(name);

      pToolsetsList.addListSelectionListener(this);
    }

    /* clear the included packages list */ 
    {
      DefaultListModel model = (DefaultListModel) pIncludedPackagesList.getModel();
      model.clear();
    }

    /* rebuild packages tree */ 
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

    // ...

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

    // ...

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
    pFreezePackageItem.setEnabled(false);
    pDeletePackageItem.setEnabled(false);

    if(tpath != null) {
      pNewPackageVersionItem.setEnabled(pIsPrivileged);

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
	  if(comp == pPackagesTree) {
	    TreePath tpath = pPackagesTree.getClosestPathForLocation(e.getX(), e.getY());
	    if(tpath != null) {
	      Rectangle bounds = pPackagesTree.getPathBounds(tpath);
	      if(!bounds.contains(e.getX(), e.getY()))
		tpath = null;
	    }

	    if(tpath != null) {
	      pPackagesTree.addSelectionPath(tpath);
	      doPackageDetails();
	    }
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

	    updateActiveToolsetsMenu(idx);
	    pActiveToolsetsPopup.show(comp, e.getX(), e.getY());
	  }
	  else if(comp == pToolsetsList) {
	    int idx = pToolsetsList.locationToIndex(e.getPoint());

	    updateToolsetsMenu(idx);
	    pToolsetsPopup.show(comp, e.getX(), e.getY());
	  }
	  else if(comp == pIncludedPackagesList) {
	    int idx = pIncludedPackagesList.locationToIndex(e.getPoint());

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
    else if(e.getActionCommand().equals("new-toolset")) 
      doNewToolset();
    else if(e.getActionCommand().equals("freeze-toolset")) 
      doFreezeToolset();
    else if(e.getActionCommand().equals("delete-toolset")) 
      doDeleteToolset();
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
    else if(e.getActionCommand().equals("freeze-package")) 
      doFreezePackage();
    else if(e.getActionCommand().equals("delete-package")) 
      doDeletePackage();
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
    
  }

  /**
   * Test executing a shell command using the environment of the selected toolset.
   */ 
  public void 
  doTestToolset()
  {
    
  }

  /**
   * Create a new modifiable toolset.
   */ 
  public void 
  doNewToolset()
  {
    
  }

  /**
   * Freeze the current state of the selected modifiable toolset to create a permanent
   * read-only toolset.
   */ 
  public void 
  doFreezeToolset()
  {
    
  }

  /**
   * Delete the selected a modifiable toolset.
   */ 
  public void 
  doDeleteToolset()
  {
    
  }

  /**
   * Show the details of the currently selected package.
   */ 
  public void 
  doPackageDetails()
  {
    PackageCommon com = getSelectedPackage();    
    if(com != null) {
      pPackageDetailsDialog.updatePackage(com);
      pPackageDetailsDialog.setVisible(true);
    }
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
    
  }

  /**
   * Move the currently selected package one place later in the evaluation order.
   */ 
  public void 
  doPackageLater()
  {
    
  }
  
  /**
   * Create a new modifiable version of an existing package. 
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
    
    if(pname != null) 
      createPackage(pname);
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
      if(pname != null) 
	createPackage(pname);
    }
  }

  /**
   * Create a new modifiable package.
   */ 
  private void 
  createPackage
  (
   String pname
  ) 
  {
    assert(pname != null);
    if(!pPackageMods.containsKey(pname)) {
      PackageMod pkg = new PackageMod(pname);
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
    if(pkg != null) {
      VersionID latest = null;
      {
	TreeMap<VersionID,PackageVersion> versions = pPackageVersions.get(pkg.getName());
	if(versions != null) 
	  latest = versions.lastKey();
      }

      pCreatePackageDialog.updateNameVersion("Create Package:  " + pkg.getName(), latest);
      pCreatePackageDialog.setVisible(true);

      if(pCreatePackageDialog.wasConfirmed()) {
	String desc = pCreatePackageDialog.getDescription();
	VersionID.Level level = pCreatePackageDialog.getLevel();

	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
	try {
	  client.createToolsetPackage(pkg, desc, level);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}

	ArrayList<String> expanded = getExpandedPackages();
	pPackageMods.remove(pkg.getName());
	updateAll();
	expandPackages(expanded);

	{
	  DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
	  DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
	  
	  int wk;
	  for(wk=0; wk<root.getChildCount(); wk++) {
	    DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) root.getChildAt(wk);
	    TreeData pdata = (TreeData) pnode.getUserObject();
	    if(pdata.getLabel().equals(pkg.getName())) {
	      if(pnode.getChildCount() > 0) {
		DefaultMutableTreeNode vnode = (DefaultMutableTreeNode) pnode.getLastChild();
		TreeData vdata = (TreeData) vnode.getUserObject();

		PackageVersion vsn = 
		  lookupPackageVersion(vdata.getName(), vdata.getVersionID());
		pPackageDetailsDialog.updatePackage(vsn);
		pPackageDetailsDialog.setVisible(true);
	      }

	      break;
	    }
	  }
	}
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
      pPackageMods.remove(pkg.getName());

      DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) tnode.getParent();
      tnode.removeFromParent();
      
      DefaultTreeModel model = (DefaultTreeModel) pPackagesTree.getModel();
      ArrayList<String> expanded = getExpandedPackages();
      if(pnode.getChildCount() == 0) 
	pnode.removeFromParent();
      model.reload();
      expandPackages(expanded);

      pPackageDetailsDialog.setVisible(false);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

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

    return com;
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
  
  protected static final int  sLSize = 150;
  protected static final int  sLHt   = 250;



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
  private JMenuItem  pNewToolsetItem;
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
  private JMenuItem  pFreezePackageItem;
  private JMenuItem  pDeletePackageItem;


  /**
   * Child dialogs.
   */
  private JPackageDetailsDialog  pPackageDetailsDialog;
  private JCreatePackageDialog   pCreatePackageDialog;
  private JTestPackageDialog     pTestPackageDialog;

}
