// $Id: JBaseLayoutDialog.java,v 1.3 2004/05/29 06:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   L A Y O U T   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of all panel layout dialogs.
 */ 
public 
class JBaseLayoutDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel layout dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  public 
  JBaseLayoutDialog
  (    
   String title
  ) 
  {
    super(title, true);
  }

  /**
   * Construct a new panel layout dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  public 
  JBaseLayoutDialog
  (
   Dialog owner,    
   String title
  ) 
  {
    super(owner, title, true);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   */ 
  protected void
  initUI
  (
   String title,  
   JComponent extraComps, 
   String confirm,
   String cancel
  )
  {
    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      
      body.add(UIMaster.createPanelLabel("Existing Layouts:"));

      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeData(), true);
	DefaultTreeModel model = new DefaultTreeModel(root, true);

	JTree tree = new JFancyTree(model); 
	pTree = tree;
	tree.setName("DarkTree");

	tree.setCellRenderer(new JLayoutTreeCellRenderer());
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.setExpandsSelectedPaths(true);

	{
	  JScrollPane scroll = new JScrollPane(pTree);
	  
	  scroll.setMinimumSize(new Dimension(230, 120));
	  scroll.setPreferredSize(new Dimension(230, 150));
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	  
	  body.add(scroll);
	}
      }

      if(extraComps != null) 
	body.add(extraComps);
	  
      super.initUI(title, true, body, confirm, null, null, cancel);
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the layouts tree.
   * 
   * @param current
   *   The name of the current layout or <CODE>null</CODE> if none.
   */ 
  public void 
  updateLayouts
  ( 
   String current
  ) 
    throws PipelineException
  {
    DefaultMutableTreeNode root = null;
    {
      root = new DefaultMutableTreeNode(new TreeData(), true);
      File dir = new File(PackageInfo.sHomeDir, PackageInfo.sUser + "/.pipeline/layouts"); 
      rebuildTreeModel(dir, dir, root);
      
      DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
      model.setRoot(root);
    }

    pTree.clearSelection();
    if(current != null) {
      TreePath tpath = null;
      String comps[] = current.split("/");
      DefaultMutableTreeNode tnode = root;
      int wk;
      for(wk=1; wk<comps.length; wk++) {
	DefaultMutableTreeNode next = null;
	Enumeration e = tnode.children();
	if(e != null) {
	  while(e.hasMoreElements()) {
	    DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement(); 
	    TreeData data = (TreeData) child.getUserObject();
	    if(data.toString().equals(comps[wk])) {
	      tpath = new TreePath(child.getPath());
	      next = child;
	      break;
	    }
	  }
	}
	
	if(next == null) 
	  break;

	tnode = next;
      }
      
      if(tpath != null) {
	pTree.setSelectionPath(tpath);
	pTree.makeVisible(tpath);
      }
    }
  }

  /**
   * Recursively rebuild the tree nodes.
   * 
   * @param root
   *   The root saved layout directory.
   * 
   * @param dir 
   *   The current directory.
   * 
   * @param tnode
   *   The current parent tree node.
   */ 
  private void 
  rebuildTreeModel
  (
   File root, 
   File dir,
   DefaultMutableTreeNode tnode
  ) 
  {
    TreeMap<String,File> table = new TreeMap<String,File>();
    {
      File files[] = dir.listFiles();
      int wk;
      for(wk=0; wk<files.length; wk++) 
	if(files[wk].isFile() || files[wk].isDirectory()) 
	  table.put(files[wk].getName(), files[wk]);
    }
    
    int rlen = root.getPath().length();
    for(String name : table.keySet()) {
      File file = table.get(name);
      File ddir = new File("/" + file.getPath().substring(rlen));
      if(file.isDirectory()) {
	TreeData data = new TreeData(ddir, null);
	DefaultMutableTreeNode child = new DefaultMutableTreeNode(data, true);
	tnode.add(child);

	rebuildTreeModel(root, file, child);
      }
    }

    for(String name : table.keySet()) {
      File file = table.get(name);
      File ddir = new File("/" + file.getPath().substring(rlen));
      if(file.isFile()) {
	TreeData data = new TreeData(ddir.getParentFile(), name);
	DefaultMutableTreeNode child = new DefaultMutableTreeNode(data, false);
	tnode.add(child);
      }
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
     File dir, 
     String name
    )
    {
      pDir  = dir;
      pName = name;
    }

    public File
    getDir()
    {
      return pDir;
    }
    
    public String
    getName() 
    {
      return pName;
    }

    public String
    toString()
    {
      if(pName != null) 
	return pName;
      else if(pDir != null) 
	return pDir.getName();
      else 
	return "";
    }

    private File    pDir; 
    private String  pName;
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6397533004329010874L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The tree of existing layout names.
   */ 
  protected JTree  pTree;

}
