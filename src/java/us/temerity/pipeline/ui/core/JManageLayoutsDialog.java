// $Id: JManageLayoutsDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

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
   */ 
  public 
  JManageLayoutsDialog()
  {
    super("Manage Saved Layouts");
    initUI();
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JManageLayoutsDialog
  (
   Dialog owner
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
	{ "Rename", "rename-layout" },
	{ "Delete",    "delete-layout" }, 
	{ "Default",   "default-layout" }
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
    super.actionPerformed(e);

    if(e.getActionCommand().equals("rename-layout")) 
      doRenameLayout();
    else if(e.getActionCommand().equals("delete-layout")) 
      doDeleteLayout();
    else if(e.getActionCommand().equals("default-layout")) 
      doDefaultLayout();
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

    String selected = null;
    if(data.getName() == null) {
      JNewIdentifierDialog diag = 
	new JNewIdentifierDialog(this, "Rename Folder", "New Folder Name:", 
				 data.getDir().getName(), "Rename");
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
	String oname = (data.getDir().getPath()); 

	String nname = null;
	if(data.getDir().getParent().length() > 1) 
	  nname = (data.getDir().getParent() + "/" + diag.getName());
	else 
	  nname = ("/" + diag.getName());

	renameFiles(oname, nname);
	
	String lname = master.getLayoutName();
	if((lname != null) && lname.startsWith(oname)) 
	  master.setLayoutName(nname + lname.substring(oname.length()));
	
	String dname = master.getDefaultLayoutName();
	if((dname != null) && dname.startsWith(oname)) 
	  master.doDefaultLayout(nname + dname.substring(oname.length()));

	selected = nname;
      }
    }
    else {
      JNewIdentifierDialog diag = 
	new JNewIdentifierDialog(this, "Rename Layout", "New Layout Name:", 
				 data.getName(), "Rename");
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
	String oname = (data.getDir() + "/" + data.getName());

	String nname = null;
	if(data.getDir().getPath().length() > 1) 
	  nname = (data.getDir() + "/" + diag.getName());
	else 
	  nname = ("/" + diag.getName());

	renameFiles(oname, nname);

	String lname = master.getLayoutName();
	if((lname != null) && lname.equals(oname)) 
	  master.setLayoutName(nname);
	
	String dname = master.getDefaultLayoutName();
	if((dname != null) && dname.equals(oname))
	  master.doDefaultLayout(nname);

	selected = nname;
      }
    }

    try {
      updateLayouts(selected);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
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
    File base = new File(PackageInfo.sHomeDir, PackageInfo.sUser + "/.pipeline/layouts");
    File ofile = new File(base + oname);
    File nfile = new File(base + nname);
    
    UIMaster master = UIMaster.getInstance();

    deleteFiles(nfile);
    if(!ofile.renameTo(nfile)) {
      master.showErrorDialog
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
      
      if(data.getName() != null) {
	if(data.getDir().getPath().length() > 1) 
	  selected = (data.getDir() + "/" + data.getName());
	else 
	  selected = ("/" + data.getName());
      }
      else {
	selected = (data.getDir().getPath());
      }
    }

    System.out.print("Delete: " + selected + "\n");

    File base = new File(PackageInfo.sHomeDir, PackageInfo.sUser + "/.pipeline/layouts");
    File file = new File(base + selected);
    deleteFiles(file);

    String lname = master.getLayoutName();
    if((lname != null) && lname.startsWith(selected)) 
      master.setLayoutName(null);
    
    String dname = master.getDefaultLayoutName();
    if((dname != null) && dname.startsWith(selected)) 
      master.doDefaultLayout(null);

    try {
      updateLayouts(null);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }      
  }

  /**
   * Delete the underlying file/directory associated with the layout/folder.
   */ 
  private void 
  deleteFiles
  (
   File file
  ) 
  {
    if(!file.exists()) 
      return; 

    UIMaster master = UIMaster.getInstance();

    if(file.isFile()) {
      if(!file.delete()) {
	master.showErrorDialog
	  ("I/O Error:", "Unable to delete (" + file + ")!");
	return;
      }
    }
    else {
      Map<String,String> env = System.getenv();
      File dir = PackageInfo.sTempDir;
      
      ArrayList<String> args = new ArrayList<String>();
      args.add("-rf");
      args.add(file.getPath());
      
      SubProcessLight proc = 
	new SubProcessLight("RemoveLayout", "rm", args, env, dir);
      proc.start();
      
      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	master.showErrorDialog(ex);	
	return;
      }
      
      if(!proc.wasSuccessful()) {
	master.showErrorDialog
	  ("I/O Error:", "Unable to delete (" + file + ")!");
	return;
      }
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

    String selected = null;
    {
      TreePath tpath = pTree.getSelectionPath(); 
      if(tpath == null)
	return;

      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      TreeData data = (TreeData) tnode.getUserObject();
      
      if(data.getName() != null) {
	if(data.getDir().getPath().length() > 1) 
	  selected = (data.getDir() + "/" + data.getName());
	else 
	  selected = ("/" + data.getName());
      }
    }

    if(selected == null) 
      return;

    master.doDefaultLayout(selected);

    try {
      updateLayouts(selected);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
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
