// $Id: JSaveLayoutDialog.java,v 1.2 2004/05/11 19:16:33 jim Exp $

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
/*   S A V E   L A Y O U T   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Saves the current panel layout to disk.
 */ 
public 
class JSaveLayoutDialog
  extends JBaseDialog
  implements ActionListener, TreeSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JSaveLayoutDialog()
  {
    super("Save Layout", true);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("SaveLayoutPanel");

      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JLabel label = new JLabel("Existing Layouts:");
	  label.setName("PanelLabel");
	  
	  hbox.add(label);
	}
	
	hbox.add(Box.createHorizontalGlue());
	
	body.add(hbox);
      }
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeData(), true);
	DefaultTreeModel model = new DefaultTreeModel(root, true);

	JTree tree = new JFancyTree(model); 
	pTree = tree;

	tree.setCellRenderer(new JSaveLayoutTreeCellRenderer());
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.setExpandsSelectedPaths(true);
	tree.addTreeSelectionListener(this);

	{
	  JScrollPane scroll = new JScrollPane(pTree);
	  
	  scroll.setMinimumSize(new Dimension(230, 120));
	  scroll.setPreferredSize(new Dimension(230, 150));
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	  
	  body.add(scroll);
	}
      }

      body.add(Box.createRigidArea(new Dimension(0, 8)));

      {
	Box hbox = new Box(BoxLayout.X_AXIS);

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JLabel label = UIMaster.createLabel("Save As:", 54, JLabel.LEFT);
	  label.setMaximumSize(new Dimension(60, label.getPreferredSize().height));

	  hbox.add(label);
	}

	{
	  JIdentifierField field = UIMaster.createIdentifierField(null, 60, JLabel.LEFT);
	  pNameField = field;
	  field.addActionListener(this);
	  
	  hbox.add(field);
	}
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JButton btn = new JButton();
	  btn.setName("FolderButton");
	  
	  Dimension size = new Dimension(26, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("new-folder");
	  btn.addActionListener(this);
	  
	  hbox.add(btn);
	} 
	
	body.add(hbox);
      }
	  
      super.initUI("Save Layout:", true, body, "Save", null, null, "Cancel");
    }  

    pConfirmButton.setEnabled(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the layouts tree and text field.
   * 
   * @param current
   *   The name of the current layout or <CODE>null</CODE> if none.
   */ 
  public void 
  update
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
	
      File path = new File(current);
      pNameField.setText(path.getName());
    }
    else {
      pNameField.setText(null);
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

  /**
   * Get the name to use for the saved layout. <P> 
   * 
   * @return
   *   The layout name or <CODE>null</CODE> if none was chosen.
   */ 
  public String
  getSelectedName() 
  {
    String text = pNameField.getText();
    if((text == null) || (text.length() == 0)) 
      return null;

    String dir = null;
    {
      TreePath tpath = pTree.getSelectionPath(); 
      if(tpath != null) {
	DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
	TreeData data = (TreeData) tnode.getUserObject();
	dir = data.getDir().getPath();
      }
    }

    if((dir != null) && (dir.length() > 1))
      return (dir + "/" + text);
    return ("/" + text);
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
    super.actionPerformed(e);

    if(e.getActionCommand().equals("new-folder")) 
      doNewFolder();
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
    TreePath tpath = pTree.getSelectionPath(); 
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
      pNameField.setText(data.getName());
    }
    else {
      pNameField.setText(null);
    }

    pConfirmButton.setEnabled(pNameField.getText() != null);
  }


  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  public void 
  changedUpdate(DocumentEvent e) {}

  /**
   * Gives notification that there was an insert into the document.
   */
  public void
  insertUpdate
  (
   DocumentEvent e
  )
  {
    String text = pNameField.getText();
    pConfirmButton.setEnabled((text != null) && (text.length() > 0));
  }
  
  /**
   * Gives notification that a portion of the document has been removed.
   */
  public void 
  removeUpdate
  (
   DocumentEvent e
  )
  {
    String text = pNameField.getText();
    pConfirmButton.setEnabled((text != null) && (text.length() > 0));    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new folder.
   */ 
  public void 
  doNewFolder()
  {
    JNewFolderDialog diag = new JNewFolderDialog(this);
    diag.setVisible(true);
    
    if(diag.wasConfirmed()) {
      DefaultMutableTreeNode tnode = null;
      TreeData cdata = null;
      {
	TreePath tpath = pTree.getSelectionPath(); 
	if(tpath != null) {
	  tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
	  TreeData data = (TreeData) tnode.getUserObject();
	  cdata = new TreeData(new File(data.getDir(), diag.getName()), null);
	}
	else {
	  tnode = (DefaultMutableTreeNode) pTree.getModel().getRoot();
	  cdata = new TreeData(new File("/" + diag.getName()), null);
	}
      }

      {
	File dir = new File(PackageInfo.sHomeDir, 
			    PackageInfo.sUser + "/.pipeline/layouts/" + cdata.getDir());
	if(!dir.isDirectory()) 
	  dir.mkdirs();
      }

      DefaultMutableTreeNode child = new DefaultMutableTreeNode(cdata, true);
      tnode.add(child);
      
      DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
      model.nodeStructureChanged(tnode);

      TreePath tpath = new TreePath(child.getPath());
      pTree.setSelectionPath(tpath);
      pTree.makeVisible(tpath);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1836402945207087332L;


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
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The tree of existing layout names.
   */ 
  private JTree  pTree;

  /**
   * The field containing the chosen name of the saved layout. <P> 
   */
  private JIdentifierField  pNameField;

}
