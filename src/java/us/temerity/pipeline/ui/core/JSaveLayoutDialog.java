// $Id: JSaveLayoutDialog.java,v 1.7 2007/04/26 17:54:44 jim Exp $

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
/*   S A V E   L A Y O U T   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Saves the current panel layout to disk.
 */ 
public 
class JSaveLayoutDialog
  extends JBaseLayoutDialog
  implements ActionListener, TreeSelectionListener, CaretListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JSaveLayoutDialog
  (
   Frame owner
  )  
  {
    super(owner, "Save Layout");
    initUI();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   */ 
  protected void
  initUI()
  {
    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      vbox.add(Box.createRigidArea(new Dimension(0, 8)));

      {
	Box hbox = new Box(BoxLayout.X_AXIS);

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JLabel label = UIFactory.createLabel("Save As:", 54, JLabel.LEFT);

	  Dimension size = label.getPreferredSize();
	  label.setMaximumSize(new Dimension(54, size.height));

	  hbox.add(label);
	}

	{
	  JIdentifierField field = UIFactory.createIdentifierField(null, 60, JLabel.LEFT);
	  pNameField = field;

	  field.addActionListener(this);
	  field.addCaretListener(this);

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
	  
	  btn.setToolTipText(UIFactory.formatToolTip("Creates a new folder."));

	  hbox.add(btn);
	} 
	
	vbox.add(hbox);
      }
	  
      super.initUI("Save Layout:", vbox, "Save", null, "Cancel");

      pConfirmButton.setToolTipText(UIFactory.formatToolTip
	("Save the current panel layout as the selected name."));
      pCancelButton.setToolTipText(UIFactory.formatToolTip 				  
        ("Cancel saving the layout."));
    }  

    pTree.addTreeSelectionListener(this);
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
  updateLayouts
  ( 
   Path current
  ) 
    throws PipelineException
  {
    super.updateLayouts(current);
    
    if(current != null) 
      pNameField.setText(current.getName());
    else 
      pNameField.setText(null);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract pathname to use for the saved layout. <P> 
   * 
   * @return
   *   The layout name or <CODE>null</CODE> if none was chosen.
   */ 
  public Path
  getSelectedPath() 
  {
    String text = pNameField.getText();
    if((text == null) || (text.length() == 0)) 
      return null;

    Path path = null; 
    {
      TreePath tpath = pTree.getSelectionPath(); 
      if(tpath != null) {
	DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
	TreeData data = (TreeData) tnode.getUserObject();
	if(data.getName() == null) 
	  path = new Path(data.getPath(), text);
	else 
	  path = new Path(data.getPath().getParentPath(), text);
      }
      else {
	path = new Path(text);
      }
    }

    return path;
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
    if(e.getActionCommand().equals("new-folder")) 
      doNewFolder();
    else 
      super.actionPerformed(e);
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


  /*-- CARET LISTENER METHODS -------------------------------------------------------------*/

  /**
   * Called when the caret position is updated.
   */
  public void 
  caretUpdate
  (
   CaretEvent e
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
      /* find the parent node and associated user data */ 
      DefaultMutableTreeNode tnode = null;
      TreeData cdata = null;
      {
	TreePath tpath = pTree.getSelectionPath(); 
	if(tpath != null) {
	  tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
	  if(!tnode.getAllowsChildren()) 
	    tnode = (DefaultMutableTreeNode) tnode.getParent();

	  TreeData data = (TreeData) tnode.getUserObject();
	  cdata = new TreeData(new Path(data.getPath(), diag.getName()), null);
	}
	else {
	  tnode = (DefaultMutableTreeNode) pTree.getModel().getRoot();
	  cdata = new TreeData(new Path("/" + diag.getName()), null);
	}
      }

      /* create the new directory */ 
      {
	Path lpath = new Path(PackageInfo.getSettingsPath(), "layouts");
	Path path = new Path(lpath, cdata.getPath().getParentPath()); 
	File dir = path.toFile();
	if(!dir.isDirectory()) 
	  dir.mkdirs();
      }

      /* insert the new tree node in the correct sorted position */ 
      TreePath tpath = null;
      {
	int idx = 0;
	Enumeration e = tnode.children();
	if(e != null) {
	  while(e.hasMoreElements()) {
	    DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement(); 
	    TreeData data = (TreeData) child.getUserObject();

	    if((data.getName() != null) || 
	       (data.getPath().compareTo(cdata.getPath()) > 0))
	      break;

	    idx++;
	  }
	}

	DefaultMutableTreeNode child = new DefaultMutableTreeNode(cdata, true);
	tnode.insert(child, idx);

	tpath = new TreePath(child.getPath());
      }

      /* notify the tree model of the changes */ 
      DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
      model.nodeStructureChanged(tnode);
      
      /* select and make visible the new node */ 
      pTree.setSelectionPath(tpath);
      pTree.makeVisible(tpath);
    }
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1836402945207087332L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The field containing the chosen name of the selected layout. <P> 
   */
  protected JIdentifierField  pNameField;
}
