// $Id: JManageLayoutsDialog.java,v 1.7 2007/04/26 17:54:44 jim Exp $

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
/*   M A N A G E   L A Y O U T S   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Manage the set of saved panel layouts.
 */ 
public 
class JManageLayoutsDialog
  extends JBaseLayoutDialog
  implements ActionListener, TreeSelectionListener
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
  JManageLayoutsDialog
  (
   Frame owner
  )  
  {
    super(owner, "Manage Saved Layouts");
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
      String[][] extra = {
	{ "Rename",   "rename-layout" },
	{ "Delete",   "delete-layout" }, 
	{ "Default",  "default-layout" }
      };

      JButton[] extraBtns = super.initUI("Manage Saved Layouts:", null, null, extra, "Close");

      pRenameButton  = extraBtns[0];
      pDeleteButton  = extraBtns[1];
      pDefaultButton = extraBtns[2];
      
      pRenameButton.setToolTipText(UIFactory.formatToolTip
	("Rename the selected panel layout."));
      pDeleteButton.setToolTipText(UIFactory.formatToolTip
        ("Delete the selected panel layout."));
      pDefaultButton.setToolTipText(UIFactory.formatToolTip
        ("Make selected panel layout the initial layout when plui(1) is restarted."));
      pCancelButton.setToolTipText(UIFactory.formatToolTip 				  
        ("Close the dialog."));
    }

    pTree.addTreeSelectionListener(this);
    pRenameButton.setEnabled(false);
    pDeleteButton.setEnabled(false);
    pDefaultButton.setEnabled(false);
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
    if(e.getActionCommand().equals("rename-layout")) 
      doRenameLayout();
    else if(e.getActionCommand().equals("delete-layout")) 
      doDeleteLayout();
    else if(e.getActionCommand().equals("default-layout")) 
      doDefaultLayout();
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

      pRenameButton.setEnabled(true);
      pDeleteButton.setEnabled(true);
      pDefaultButton.setEnabled(data.getName() != null);
    }
    else {
      pRenameButton.setEnabled(false);
      pDeleteButton.setEnabled(false);
      pDefaultButton.setEnabled(false);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Rename the currently selected layout or folder.
   */ 
  private void 
  doRenameLayout()
  {
    TreePath tpath = pTree.getSelectionPath(); 
    if(tpath == null)
      return;
    
    UIMaster master = UIMaster.getInstance();

    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
    TreeData data = (TreeData) tnode.getUserObject();

    Path selected = null;
    if(data.getName() == null) {
      JNewIdentifierDialog diag = 
	new JNewIdentifierDialog(this, "Rename Folder", "New Folder Name:", 
				 data.getPath().getName(), "Rename");
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
	String oname = data.getPath().toString();
	Path npath = new Path(data.getPath().getParentPath(), diag.getName());

	renameFiles(oname, npath.toString());
	
	Path lpath = master.getLayoutPath();
	if(lpath != null) {
	  String lname = lpath.toString();
	  if(lname.startsWith(oname)) 
	    master.setLayoutPath(new Path(npath, lname.substring(oname.length())));
	}
	
	Path dpath = master.getDefaultLayoutPath(); 
	if(dpath != null) {
	  String dname = dpath.toString();
	  if(dname.startsWith(oname)) 
	    master.doDefaultLayout(new Path(npath, dname.substring(oname.length())));
	}
	
	selected = npath;
      }
    }
    else {
      JNewIdentifierDialog diag = 
	new JNewIdentifierDialog(this, "Rename Layout", "New Layout Name:", 
				 data.getName(), "Rename");
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
	String oname = data.getPath().toString();
	Path npath = new Path(data.getPath().getParentPath(), diag.getName());

	renameFiles(oname, npath.toString()); 

	Path lpath = master.getLayoutPath();
	if((lpath != null) && lpath.toString().equals(oname))
	  master.setLayoutPath(npath);
	
	Path dpath = master.getDefaultLayoutPath(); 
	if((dpath != null) && dpath.toString().equals(oname))
	  master.doDefaultLayout(npath);

	selected = npath;
      }
    }

    try {
      updateLayouts(selected);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }    
  }
   
  /**
   * Rename the underlying file/directory associated with the layout/folder.
   */ 
  private void 
  renameFiles
  (
   String oname,
   String nname
  ) 
  {
    Path base = new Path(PackageInfo.getSettingsPath(), "layouts"); 

    Path opath = new Path(base, oname);
    File ofile = opath.toFile();

    Path npath = new Path(base, nname);
    File nfile = npath.toFile();
    
    UIMaster master = UIMaster.getInstance();

    Files.deleteAll(nfile);
    if(!ofile.renameTo(nfile)) {
      showErrorDialog
	("I/O Error:", "Unable to rename (" + oname + ") to (" + nname + ")!");
      return;
    }  
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Delete the currently selected layout or folder.
   */ 
  private void 
  doDeleteLayout()
  {
    UIMaster master = UIMaster.getInstance();

    String selected = null;
    {
      TreePath tpath = pTree.getSelectionPath(); 
      if(tpath == null)
	return;

      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
      selected = data.getPath().toString();
    }
    if(selected == null) 
      return;
    
    Path lpath = new Path(PackageInfo.getSettingsPath(), "layouts"); 
    Path path = new Path(lpath, selected);
    Files.deleteAll(path.toFile()); 

    Path opath = master.getLayoutPath();
    if((opath != null) && opath.toString().startsWith(selected)) 
      master.setLayoutPath(null);
    
    Path dpath = master.getDefaultLayoutPath();
    if((dpath != null) && dpath.toString().startsWith(selected)) 
      master.doDefaultLayout(null);

    try {
      updateLayouts(null);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }      
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Make the currently selected layout the default layout.
   */ 
  private void 
  doDefaultLayout()
  {
    UIMaster master = UIMaster.getInstance();

    Path selected = null;
    {
      TreePath tpath = pTree.getSelectionPath(); 
      if(tpath == null)
	return;

      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
      if(data.getName() != null) 
	selected = data.getPath();
    }

    if(selected == null) 
      return;

    master.doDefaultLayout(selected);

    try {
      updateLayouts(selected);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }      
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 262356898038054868L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The panel buttons.
   */ 
  private JButton  pRenameButton;
  private JButton  pDeleteButton;
  private JButton  pDefaultButton;

}
