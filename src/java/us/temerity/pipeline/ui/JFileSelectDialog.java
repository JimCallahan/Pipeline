// $Id: JFileSelectDialog.java,v 1.6 2004/10/31 20:02:39 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S E L E C T   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog which allows the user to select a specific file or directory.
 */ 
public 
class JFileSelectDialog
  extends JBaseFileSelectDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog for selecting files.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param header
   *   The header label.
   * 
   * @param fieldTitle
   *   The title of the filename field.
   * 
   * @param fieldTitleSize
   *   The width of the filename field title.
   * 
   * @param confirm
   *   The name of the confirm button.
   */ 
  public 
  JFileSelectDialog
  (
   String title,
   String header,
   String fieldTitle, 
   int fieldTitleSize, 
   String confirm
  ) 
  {
    super(title);
    initUI(title, header, fieldTitle, fieldTitleSize, confirm);
  }

  /**
   * Construct a new dialog for selecting files.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param header
   *   The header label.
   * 
   * @param fieldTitle
   *   The title of the filename field.
   * 
   * @param fieldTitleSize
   *   The width of the filename field title.
   * 
   * @param confirm
   *   The name of the confirm button.
   */ 
  public 
  JFileSelectDialog
  (
   Dialog owner,
   String title,
   String header,
   String fieldTitle, 
   int fieldTitleSize, 
   String confirm
  ) 
  {
    super(owner, title);
    initUI(title, header, fieldTitle, fieldTitleSize, confirm);
  }

  
  /**
   * Construct a new dialog for selecting directories only.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param header
   *   The header label.
   * 
   * @param confirm
   *   The name of the confirm button.
   */ 
  public 
  JFileSelectDialog
  (
   Dialog owner,
   String title, 
   String header, 
   String confirm
  ) 
  {
    super(owner, title);
    initUI(title, header, null, 0, confirm);
  }




  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   * 
   * If the <CODE>fieldTitle</CODE> argument is <CODE>null</CODE> no selected file text 
   * field is displayed and the <CODE>fieldTitleSize</CODE> argument is ignored.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param header
   *   The header label.
   *
   * @param fieldTitle
   *   The title of the filename field.
   * 
   * @param fieldTitleSize
   *   The width of the filename field title.
   * 
   * @param confirm
   *   The name of the confirm button.
   */ 
  protected void
  initUI
  (
   String title, 
   String header,
   String fieldTitle, 
   int fieldTitleSize, 
   String confirm
  ) 
  {    
    JFileListCellRenderer renderer = new JFileListCellRenderer();
    JIdentifierField field = UIMaster.createIdentifierField(null, 60, JLabel.LEFT);
    super.initUI(header, renderer, fieldTitle, fieldTitleSize, field, confirm);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the selected file (or directory).
   * 
   * @return 
   *   The file or <CODE>null</CODE> if none is selected.
   */ 
  public File
  getSelectedFile() 
  {
    String dtext = pDirField.getText(); 
    if(dtext == null) 
      return null;

    if(pFileField == null) {
      File dir = new File(dtext);
      try {
	return dir.getCanonicalFile();
      }
      catch(IOException ex) {
	return null;
      }
    }
    else {
      String ftext = pFileField.getText();
      if(ftext == null) 
	return null;

      File file = new File(dtext + "/" + ftext);
      try {
	return file.getCanonicalFile();
      }
      catch(IOException ex) {
	return null;
      }
    }
  }

    


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the initial name of the selected target file in the current directory.
   */ 
  public void 
  updateTargetName
  (
   String name
  ) 
  {
    pFileField.setText(name);
  }

  /**
   * Update target file or directory.
   */ 
  public void 
  updateTargetFile
  (
   File target
  ) 
  {
    /* determine the canonical path to the target */ 
    File canon = null;
    {
      if(target != null) {
	try {
	  canon = target.getCanonicalFile();
	}
	catch(IOException ex) {
	}
      }
      
      if(canon == null) {
	try {
	  File dir = new File(System.getProperty("user.dir"));
	  if(dir.getPath().startsWith(pRootDir.getPath())) 
	    canon = dir.getCanonicalFile();
	}
	catch(IOException ex) {
	}
      }
      
      if(canon == null) 
	canon = pRootDir;
    }

    /* determine the target directory and file name */ 
    File dir = null;
    String name = null;
    {
      if(canon.isDirectory()) 
	dir = canon;
      else if(canon.isFile()) {
	dir = canon.getParentFile();
	name = canon.getName();
      }
      else {
	Toolkit.getDefaultToolkit().beep();

	File file = canon;
	while(file.getPath().startsWith(pRootDir.getPath())) {
	  if(file.isDirectory()) {
	    dir = file;
	    break;
	  }

	  file = file.getParentFile();
	}

	if(dir == null) 
	  return;
      }
    }

    /* initialize the UI components */ 
    if(name == null) {
      File fs[] = dir.listFiles();
      if(fs != null) {
	DefaultListModel model = (DefaultListModel) pFileList.getModel();
	model.clear();
	
	TreeSet<File> dirs  = new TreeSet<File>();
	TreeSet<File> files = new TreeSet<File>();
	
	int wk;
	for(wk=0; wk<fs.length; wk++) {
	  if((pFileField != null) && fs[wk].isFile())
	    files.add(fs[wk]);
	  else if(fs[wk].isDirectory()) 
	    dirs.add(fs[wk]);
	}
	
	if(!dir.equals(pRootDir)) 
	  model.addElement(new File(dir, ".."));
	
	for(File file : dirs) 
	  model.addElement(file);
	
	for(File file : files) 
	  model.addElement(file);
	
	pDirField.setText(dir.getPath());
      }
      else {
	Toolkit.getDefaultToolkit().beep();
	return;
      }
    }

    if((pFileField != null) && (name != null))
      pFileField.setText(name);
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
    if(e.getValueIsAdjusting()) 
      return; 

    File target = (File) pFileList.getSelectedValue();
    if((target != null) && !target.isDirectory())
      updateTargetFile(target);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle double-click on the given list element.
   */ 
  protected void 
  doHandleDoubleClick
  (
   Object elem
  )
  { 
    if((elem != null) && (elem instanceof File)) {
      File file = (File) elem;
      if(file.isDirectory()) 
	updateTargetFile(file);
    }
  }

  /**
   * Jump to the directory named by the directory field.
   */ 
  protected void 
  doJumpDir()
  {
    File dir = new File(pDirField.getText());
    if(dir.isDirectory()) 
      updateTargetFile(dir);
    else 
      Toolkit.getDefaultToolkit().beep();
  }

  /**
   * Jump to the user's home directory.
   */ 
  protected void 
  doJumpHome()
  { 
    updateTargetFile(new File(PackageInfo.sHomeDir, PackageInfo.sUser));
  }

  /**
   * Create a new directory under the current working directory and jump to it.
   */ 
  protected void 
  doNewFolder()
  {
    File dir = new File(pDirField.getText());
    if(dir.isDirectory()) {
      JNewFolderDialog diag = new JNewFolderDialog(this);
      diag.setVisible(true);
      
      if(diag.wasConfirmed()) {
	File ndir = new File(dir, diag.getName());
	if(!ndir.mkdirs()) {
	  UIMaster.getInstance().showErrorDialog
	    ("I/O Error:", 
	     "Unable to create directory (" + ndir + ")!");
	}
	
	updateTargetFile(ndir);
      }
    }
    else {
      Toolkit.getDefaultToolkit().beep();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 257228426004498837L;

}
