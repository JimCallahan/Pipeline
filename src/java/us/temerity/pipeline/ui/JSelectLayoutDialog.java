// $Id: JSelectLayoutDialog.java,v 1.2 2004/05/12 04:03:53 jim Exp $

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
/*   S E L E C T   L A Y O U T   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides listing and selection of saved panel layouts.
 */ 
public 
class JSelectLayoutDialog
  extends JBaseLayoutDialog
  implements ActionListener, TreeSelectionListener
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
  JSelectLayoutDialog
  (    
   String title
  ) 
  {
    super(title);
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
  JSelectLayoutDialog
  (
   Dialog owner,    
   String title
  ) 
  {
    super(owner, title);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   */ 
  protected void
  initUI
  (
   String title,  
   String fieldTitle,
   int fieldTitleSize, 
   String confirm 
  )
  {
    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      vbox.add(Box.createRigidArea(new Dimension(0, 8)));

      {
	Box hbox = new Box(BoxLayout.X_AXIS);

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JLabel label = UIMaster.createLabel(fieldTitle, fieldTitleSize, JLabel.LEFT);

	  Dimension size = label.getPreferredSize();
	  label.setMaximumSize(new Dimension(fieldTitleSize, size.height));

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
	
	vbox.add(hbox);
      }
	  
      super.initUI(title, vbox, confirm, "Cancel");
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
  updateLayuouts
  ( 
   String current
  ) 
    throws PipelineException
  {
    super.updateLayouts(current);
    
    if(current != null) {
      File path = new File(current);
      pNameField.setText(path.getName());
    }
    else {
      pNameField.setText(null);
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
      /* find the parent node and associated user data */ 
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

	if(!tnode.getAllowsChildren()) 
	  tnode = (DefaultMutableTreeNode) tnode.getParent();
      }

      /* create the new directory */ 
      {
	File dir = new File(PackageInfo.sHomeDir, 
			    PackageInfo.sUser + "/.pipeline/layouts/" + cdata.getDir());
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
	       (data.getDir().compareTo(cdata.getDir()) > 0))
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

  private static final long serialVersionUID = -7314019705131781936L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The field containing the chosen name of the selected layout. <P> 
   */
  protected JIdentifierField  pNameField;

}
