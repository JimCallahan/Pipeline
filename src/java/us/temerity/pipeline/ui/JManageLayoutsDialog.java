// $Id: JManageLayoutsDialog.java,v 1.3 2004/05/12 04:00:36 jim Exp $

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
/*   M A N A G E   L A Y O U T S   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Manage the set of saved panel layouts.
 */ 
public 
class JManageLayoutsDialog
  extends JBaseLayoutDialog
  implements ActionListener, MouseListener
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
    /* popup menu */ 
    {
      JMenuItem item;
      
      pPopup = new JPopupMenu();  
      
      item = new JMenuItem("Rename...");
      item.setActionCommand("rename-layout");
      item.addActionListener(this);
      pPopup.add(item);  
	
      item = new JMenuItem("Delete");
      item.setActionCommand("delete-layout");
      item.addActionListener(this);
      pPopup.add(item);  
    }

    /* create dialog body components */ 
    {
      super.initUI("Manage Saved Layouts:", null, null, "Close");
      pTree.addMouseListener(this);
    }
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
  }



  /*-- MOUSE LISTENER METHODS -----------------------------------------------------------*/

  public void 
  mouseClicked(MouseEvent e) {}
  
  public void 
  mouseEntered(MouseEvent e) {}

  public void 
  mouseExited(MouseEvent e) {}

  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON3:
      {
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	/* BUTTON3 */ 
	if((mods & (on1 | off1)) == on1) {
	  pTree.setSelectionPath(pTree.getPathForLocation(e.getX(), e.getY()));
	  pPopup.show(e.getComponent(), e.getX(), e.getY()); 
	}
      }
    }
  }

  public void 
  mouseReleased(MouseEvent e) {}

  

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
    
    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
    TreeData data = (TreeData) tnode.getUserObject();

    if(data.getName() == null) {
      JNewIdentifierDialog diag = 
	new JNewIdentifierDialog(this, "Rename Folder", "New Folder Name:", 
				 data.getDir().getName(), "Rename");
      diag.setVisible(true);
      if(diag.wasConfirmed()) 
	renameFiles(data.getDir().getPath(), 
		    data.getDir().getParent() + "/" + diag.getName());
    }
    else {
      JNewIdentifierDialog diag = 
	new JNewIdentifierDialog(this, "Rename Layout", "New Layout Name:", 
				 data.getName(), "Rename");
      diag.setVisible(true);
      if(diag.wasConfirmed()) 
	renameFiles(data.getDir() + "/" + data.getName(), 
		    data.getDir() + "/" + diag.getName());
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

    try {
      updateLayouts(nname);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }      
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Rename the currently selected layout or folder.
   */ 
  private void 
  doDeleteLayout()
  {
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

    try {
      updateLayouts(null);
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
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
      
      SubProcess proc = 
	new SubProcess("RemoveLayout", "rm", args, env, dir);
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 262356898038054868L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The popup menu.
   */ 
  private JPopupMenu  pPopup; 

}
