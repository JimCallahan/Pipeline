// $Id: JBasePackagePluginsPanel.java,v 1.2 2005/09/07 21:11:17 jim Exp $

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
/*   B A S E   P A C K A G E   P L U G I N S   P A N E L                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The base clase of all panels which edit the plugins associated with a toolset package.
 */ 
public abstract
class JBasePackagePluginsPanel
  extends JPanel
  implements ActionListener, TreeSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel.
   * 
   * @param title
   *   The plugin type title.
   */ 
  protected
  JBasePackagePluginsPanel
  (
   String title
  ) 
  {
    /* initialize fields */ 
    {
      pIncludedVersions = new DoubleMap<String,String,TreeSet<VersionID>>();
      pAllVersions      = new DoubleMap<String,String,TreeSet<VersionID>>();

      pIsPrivileged = false;
    }

    /* create dialog body components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));  

      add(Box.createRigidArea(new Dimension(20, 0)));

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	
	vbox.add(UIFactory.createPanelLabel("Included " + title + "s:"));
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  DefaultMutableTreeNode root = 
	    new DefaultMutableTreeNode(new PluginTreeData(), true);
	  DefaultTreeModel model = new DefaultTreeModel(root, true);
	  
	  JTree tree = new JFancyTree(model); 
	  pIncludedTree = tree;
	  tree.setName("DarkTree");
	  
 	  tree.setCellRenderer(new JPluginTreeCellRenderer());
	  tree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  
	  tree.addTreeSelectionListener(this);
	  
	  {
	    JScrollPane scroll = new JScrollPane(tree);
	    
	    scroll.setMinimumSize(new Dimension(150, 100));
	    scroll.setPreferredSize(new Dimension(250, 500));
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    vbox.add(scroll);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  add(vbox);
	}
      }

      add(Box.createRigidArea(new Dimension(4, 0)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createVerticalGlue());
	
	{
	  JButton btn = new JButton();
	  pIncludeButton = btn;
	  btn.setName("LeftArrowButton");
	  
	  Dimension size = new Dimension(16, 16);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("include-plugin");
	  btn.addActionListener(this);
	  
	  vbox.add(btn);
	} 
	
	vbox.add(Box.createRigidArea(new Dimension(0, 16)));
	
	{
	  JButton btn = new JButton();
	  pExcludeButton = btn;
	  btn.setName("RightArrowButton");
	  
	  Dimension size = new Dimension(16, 16);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("exclude-plugin");
	  btn.addActionListener(this);
	  
	  vbox.add(btn);
	} 
	
	vbox.add(Box.createVerticalGlue());
	
	add(vbox);
      }
      
      add(Box.createRigidArea(new Dimension(4, 0)));
	
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	
	vbox.add(UIFactory.createPanelLabel("All " + title + " Plugins:"));
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  DefaultMutableTreeNode root = 
	    new DefaultMutableTreeNode(new PluginTreeData(), true);
	  DefaultTreeModel model = new DefaultTreeModel(root, true);
	  
	  JTree tree = new JFancyTree(model); 
	  pAllTree = tree;
	  tree.setName("DarkTree");
	  
 	  tree.setCellRenderer(new JPluginTreeCellRenderer());
	  tree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  
	  tree.addTreeSelectionListener(this);
	  
	  {
	    JScrollPane scroll = new JScrollPane(tree);
	    
	    scroll.setMinimumSize(new Dimension(150, 100));
	    scroll.setPreferredSize(new Dimension(250, 500));
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    vbox.add(scroll);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  add(vbox);
	}
      }

      add(Box.createRigidArea(new Dimension(20, 0)));
    }
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
   *   The revision number of the package.
   * 
   * @param isPrivileged
   *   Whether the current user is privileged.
   */ 
  public abstract void 
  update
  (
   String pname, 
   OsType os,
   VersionID vid,
   boolean isPrivileged
  ) 
    throws PipelineException;

  /**
   * Update the UI components to display the current plugins associated with a 
   * toolset package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param os
   *   The package operating system.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param includedPlugins
   *   The vendors, names and versions of the plugins associated with the package. 
   * 
   * @param allPlugins
   *   The vendors, names and versions of all currently loaded plugins.
   * 
   * @param isPrivileged
   *   Whether the current user is privileged.
   */ 
  protected void 
  updateHelper
  (
   String pname, 
   OsType os, 
   VersionID vid, 
   DoubleMap<String,String,TreeSet<VersionID>> includedPlugins,
   DoubleMap<String,String,TreeSet<VersionID>> allPlugins,
   boolean isPrivileged
  ) 
  {
    {
      pPackageName      = pname;
      pPackageOsType    = os;
      pPackageVersionID = vid; 
      
      pIncludedVersions = includedPlugins;
      pAllVersions      = allPlugins;
      
      pIsPrivileged = isPrivileged;
    }

    rebuildPluginTree(pIncludedVersions, pIncludedTree);
    rebuildPluginTree(pAllVersions, pAllTree);

    pIncludeButton.setEnabled(false);
    pExcludeButton.setEnabled(false);
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
    throws PipelineException
  {
    setPlugins(pname, os, null, getPlugins(pname, os, vid));
  }

  /**
   * Save the plugins associated with the given frozen package from copying them from the
   * plugins associated with the working package. 
   * 
   * @param pname
   *   The name of the toolset package.

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
    throws PipelineException
  {
    setPlugins(pname, os, vid, getPlugins(pname, os, null));
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
    throws PipelineException
  {
    setPlugins(pname, os, null, null);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugins associated with a frozen package.
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
  protected abstract DoubleMap<String,String,TreeSet<VersionID>>
  getPlugins
  (
   String pname, 
   OsType os, 
   VersionID vid 
  )
    throws PipelineException; 

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
  protected abstract void
  setPlugins
  (
   String pname, 
   OsType os, 
   VersionID vid, 
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Save any changes. 
   */ 
  public void 
  saveChanges()
    throws PipelineException
  {
    setPlugins(pPackageName, pPackageOsType, pPackageVersionID, pIncludedVersions);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    if(e.getSource() == pIncludedTree) {
      PluginTreeData vdata = getSelectedPluginData(pIncludedTree);
      pExcludeButton.setEnabled
	((vdata != null) && (vdata.getName() != null) && (vdata.getVersionID() != null));
    }
    else if(e.getSource() == pAllTree) {
      PluginTreeData vdata = getSelectedPluginData(pAllTree);
      pIncludeButton.setEnabled
	((vdata != null) && (vdata.getName() != null) && (vdata.getVersionID() != null));
    }
  }


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
    if(cmd.equals("include-plugin")) 
      doIncludePlugin();
    else if(cmd.equals("exclude-plugin")) 
      doExcludePlugin();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the selected plugin to the set of plugins associated with the toolset package.
   */ 
  public void 
  doIncludePlugin()
  {
    PluginTreeData vdata = getSelectedPluginData(pAllTree);
    if((vdata != null) && 
       (vdata.getVendor() != null) && 
       (vdata.getName() != null) && 
       (vdata.getVersionID() != null)) {
      TreeSet<VersionID> versions = pIncludedVersions.get(vdata.getVendor(), vdata.getName());
      if(versions == null) {
	versions = new TreeSet<VersionID>();
	pIncludedVersions.put(vdata.getVendor(), vdata.getName(), versions);
      }
      versions.add(vdata.getVersionID());

      pExcludeButton.setEnabled(false);
      rebuildPluginTree(pIncludedVersions, pIncludedTree);
      selectPluginVersion(vdata, pIncludedTree, pExcludeButton);
    }
  }

  /**
   * Remove the selected plugin from the set of plugins associated with the toolset package.
   */ 
  public void 
  doExcludePlugin()
  {
    PluginTreeData vdata = getSelectedPluginData(pIncludedTree);
    if((vdata != null) && 
       (vdata.getVendor() != null) && 
       (vdata.getName() != null) && 
       (vdata.getVersionID() != null)) {
      TreeSet<VersionID> versions = pIncludedVersions.get(vdata.getVendor(), vdata.getName());
      if(versions != null) {
	versions.remove(vdata.getVersionID());
	if(versions.isEmpty()) 
	  pIncludedVersions.remove(vdata.getVendor(), vdata.getName()); 
      }

      pExcludeButton.setEnabled(false);
      rebuildPluginTree(pIncludedVersions, pIncludedTree);
      selectPluginVersion(vdata, pAllTree, pIncludeButton);      
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Rebuild the plugin version tree nodes 
   */ 
  private void 
  rebuildPluginTree
  (
   DoubleMap<String,String,TreeSet<VersionID>> versions,
   JTree tree
  )
  {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(new PluginTreeData());

    for(String vendor : versions.keySet()) {
      DefaultMutableTreeNode rnode = 
	new DefaultMutableTreeNode(new PluginTreeData(vendor), true);
      root.add(rnode);

      for(String name : versions.get(vendor).keySet()) {
	DefaultMutableTreeNode pnode = 
	  new DefaultMutableTreeNode(new PluginTreeData(name), true);
	rnode.add(pnode);
	
	for(VersionID vid : versions.get(vendor).get(name)) {
	  DefaultMutableTreeNode vnode = 
	    new DefaultMutableTreeNode(new PluginTreeData(name, vid, vendor), false);
	  pnode.add(vnode);
	}
      }
    }
    
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    model.setRoot(root);

    Enumeration e = root.depthFirstEnumeration();
    if(e != null) {
      while(e.hasMoreElements()) {
	DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) e.nextElement(); 
	TreePath tpath = new TreePath(mnode.getPath());
	tree.expandPath(tpath);
      }
    }    
  }

  /**
   * Select the plugin version tree node for the given plugin version.
   */ 
  private void 
  selectPluginVersion
  (
   PluginTreeData data, 
   JTree tree, 
   JButton button
  ) 
  {
    if((data == null) || (data.getName() == null) || (data.getVersionID() == null))
      return;

    tree.removeTreeSelectionListener(this);
    try {
      tree.clearSelection();

      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
     
      int rk;
      for(rk=0; rk<root.getChildCount(); rk++) {
	DefaultMutableTreeNode rnode = (DefaultMutableTreeNode) root.getChildAt(rk);  

	int wk;
	for(wk=0; wk<rnode.getChildCount(); wk++) {
	  DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) rnode.getChildAt(wk);  
	
	  int vk;
	  for(vk=0; vk<pnode.getChildCount(); vk++) {
	    DefaultMutableTreeNode vnode = (DefaultMutableTreeNode) pnode.getChildAt(vk); 
	    PluginTreeData vdata = (PluginTreeData) vnode.getUserObject();
	    TreePath vpath = new TreePath(vnode.getPath());
	    
	    if(data.getName().equals(vdata.getName()) && 
	       data.getVersionID().equals(vdata.getVersionID()) &&
	       data.getVendor().equals(vdata.getVendor())) {
	      tree.addSelectionPath(vpath);
	      button.setEnabled(true);
	      return;
	    }
	  }
	}
      }
    }
    finally {
      tree.addTreeSelectionListener(this);
    }
  }

  /**
   * Get the selected plugin version tree node.
   * 
   * @return 
   *   The node or <CODE>null</CODE> if none is selected.
   */ 
  private DefaultMutableTreeNode
  getSelectedPluginNode
  (
   JTree tree
  ) 
  {
    TreePath mpath = tree.getSelectionPath();
    if(mpath != null) 
      return ((DefaultMutableTreeNode) mpath.getLastPathComponent());
    return null;
  }

  /**
   * Get the selected plugin version tree node user data.
   * 
   * @return 
   *   The data or <CODE>null</CODE> if none is selected.
   */ 
  private PluginTreeData
  getSelectedPluginData
  (
   JTree tree
  ) 
  {
    DefaultMutableTreeNode mnode = getSelectedPluginNode(tree);
    if(mnode != null) 
      return ((PluginTreeData) mnode.getUserObject());
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the toolset package.
   */ 
  protected String  pPackageName; 

  /**
   * The package operating system.
   */ 
  protected OsType  pPackageOsType; 

  /**
   * The revision number of the package.
   */ 
  protected VersionID  pPackageVersionID; 


  /**
   * The names and versions of the plugins associated with the package. 
   */ 
  protected DoubleMap<String,String,TreeSet<VersionID>>  pIncludedVersions;

  /**
   * The names and versions of all currently loaded plugins.
   */ 
  private DoubleMap<String,String,TreeSet<VersionID>>  pAllVersions;


  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The included plugin versions tree.
   */ 
  private JTree  pIncludedTree;
     
  /**
   * Plugin buttons.
   */ 
  private JButton  pIncludeButton; 
  private JButton  pExcludeButton; 

  /**
   * The all plugin versions tree.
   */ 
  private JTree  pAllTree;

}
