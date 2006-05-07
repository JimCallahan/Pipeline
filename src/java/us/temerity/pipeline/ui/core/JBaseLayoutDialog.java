// $Id: JBaseLayoutDialog.java,v 1.2 2006/05/07 21:30:14 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

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
  protected JButton[]
  initUI
  (
   String title,  
   JComponent extraComps, 
   String confirm,
   String[][] extra,
   String cancel
  )
  {
    JButton[] extraBtns = null;

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      
      body.add(UIFactory.createPanelLabel("Existing Layouts:"));

      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeData(), true);
	DefaultTreeModel model = new DefaultTreeModel(root, true);

	JTree tree = new JFancyTree(model); 
	pTree = tree;
	tree.setName("DarkTree");

	tree.setCellRenderer(new JLayoutTreeCellRenderer());
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

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
	  
      extraBtns = super.initUI(title, true, body, confirm, null, extra, cancel);
    }  
    
    return extraBtns;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
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
   Path current
  ) 
    throws PipelineException
  {
    DefaultMutableTreeNode root = null;
    {
      root = new DefaultMutableTreeNode(new TreeData(), true);

      {
	Path path = new Path(PackageInfo.sHomePath, 
			     PackageInfo.sUser + "/.pipeline/layouts"); 
	rebuildTreeModel(path, new Path("/"), root);
      }
      
      DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
      model.setRoot(root);

      {
         Enumeration e = root.depthFirstEnumeration();
	 if(e != null) {
	   while(e.hasMoreElements()) {
	     DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) e.nextElement(); 
	     pTree.expandPath(new TreePath(tnode.getPath()));
	   }
	 }
      }
    }

    pTree.clearSelection();
    if(current != null) {
      TreePath tpath = null;
      DefaultMutableTreeNode tnode = root;
      for(String comp : current.getComponents()) {
	DefaultMutableTreeNode next = null;
	Enumeration e = tnode.children();
	if(e != null) {
	  while(e.hasMoreElements()) {
	    DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement(); 
	    TreeData data = (TreeData) child.getUserObject();
	    if(data.toString().equals(comp)) {
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
   *   The full abstract path to the root saved layout directory.
   * 
   * @param local 
   *   The current directory relative to root (null if none).
   * 
   * @param tnode
   *   The current parent tree node.
   */ 
  private void 
  rebuildTreeModel
  (
   Path root, 
   Path local, 
   DefaultMutableTreeNode tnode
  ) 
  {
    TreeSet<Path> subdirs = new TreeSet<Path>();
    TreeSet<String> layouts = new TreeSet<String>();
    {
      Path current = new Path(root, local);
      File files[] = current.toFile().listFiles();
      int wk;
      for(wk=0; wk<files.length; wk++) {
	String name = files[wk].getName();
	if(files[wk].isDirectory()) 
	  subdirs.add(new Path(local, name)); 
	else if(files[wk].isFile()) 
	  layouts.add(name);
      }
    }
    
    for(Path subdir : subdirs) {
      TreeData data = new TreeData(subdir);
      DefaultMutableTreeNode child = new DefaultMutableTreeNode(data, true);
      tnode.add(child);
      
      rebuildTreeModel(root, subdir, child);
    }
     
    for(String lname : layouts) {
      TreeData data = new TreeData(new Path(local, lname), lname);
      DefaultMutableTreeNode child = new DefaultMutableTreeNode(data, false);
      tnode.add(child);
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
    {
      this(new Path("/"), null);
    }

    public 
    TreeData
    ( 
     Path path
    )
    {
      this(path, null);
    }

    public 
    TreeData
    ( 
     Path path, 
     String name
    )
    {
      pPath = path;
      pName = name;
    }

    public Path
    getPath()
    {
      return pPath;
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
      return pPath.getName();
    }

    private Path    pPath; 
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
