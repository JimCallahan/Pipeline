// $Id: JFileSelectDialog.java,v 1.3 2004/06/03 09:25:53 jim Exp $

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
  extends JBaseDialog
  implements ListSelectionListener, DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
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
    super(owner, title, true);
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
    super(owner, title, true);
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
    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JLabel label = UIMaster.createLabel("Directory:", 60, JLabel.LEFT);
	  
	  Dimension size = label.getPreferredSize();
	  label.setMaximumSize(new Dimension(60, size.height));
	  
	  hbox.add(label);
	}
	  
	{
	  JTextField field = UIMaster.createEditableTextField(null, 60, JLabel.LEFT);
	  pDirField = field;

	  field.addActionListener(this);
	  field.setActionCommand("jump-dir");

	  field.getDocument().addDocumentListener(this);

	  hbox.add(field);
	}
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JButton btn = new JButton();
	  btn.setName("FolderButton");
	  
	  Dimension size = new Dimension(24, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("new-folder");
	  btn.addActionListener(this);
	  
	  hbox.add(btn);
	} 

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JButton btn = new JButton();
	  btn.setName("HomeButton");
	  
	  Dimension size = new Dimension(24, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("jump-home");
	  btn.addActionListener(this);
	  
	  hbox.add(btn);
	} 

	body.add(hbox);
      }
      
      body.add(Box.createRigidArea(new Dimension(0, 8)));

      {
	JList lst = new JList();
	pFileList = lst;

	lst.setModel(new DefaultListModel());
	lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	lst.setCellRenderer(new JFileListCellRenderer());
	lst.addListSelectionListener(this);

	{
	  JScrollPane scroll = new JScrollPane(lst);
	  
	  Dimension size = new Dimension(690, 120);
	  scroll.setMinimumSize(size);
	  scroll.setPreferredSize(size);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	  
	  body.add(scroll);
	}
      }

      if(fieldTitle != null) {
	body.add(Box.createRigidArea(new Dimension(0, 8)));
      
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
	    pFileField = field;

	    field.addActionListener(this);
	    field.setActionCommand("confirm");

	    field.getDocument().addDocumentListener(this);

	    hbox.add(field);
	  }
	  
	  body.add(hbox);
	}  
      }

      super.initUI(header, true, body, confirm, null, null, "Close");

      pack();
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the selected file (or directory).
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
    /* determine the canonical path to the target directory */ 
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
	  canon = dir.getCanonicalFile();
	}
	catch(IOException ex) {
	}
      }
      
      if(canon == null) 
	canon = new File("/");
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
	while(file.getPath().length() > 1) {
	  if(file.isDirectory()) {
	    dir = file;
	    break;
	  }

	  file = file.getParentFile();
	}
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
	
	if(!dir.getPath().equals("/")) 
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

  /**
   * Update the enabled status of the confirm button.
   */ 
  private void 
  updateConfirmButton()
  {
    boolean enabled = true;
    
    String dir = pDirField.getText();
    if((dir == null) || (dir.length() == 0))
      enabled = false;

    if(pFileField != null) {
      String file = pFileField.getText();
      if((file == null) || (file.length() == 0))
	enabled = false;
    }

    pConfirmButton.setEnabled(enabled);
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
    if(target != null) 
      updateTargetFile(target);
  }


  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Gives notification that an attribute or set of attributes changed.
   */ 
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
    updateConfirmButton();
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
    updateConfirmButton();    
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
    super.actionPerformed(e);

    if(e.getActionCommand().equals("jump-dir")) 
      doJumpDir();
    else if(e.getActionCommand().equals("jump-home")) 
      doJumpHome();
    else if(e.getActionCommand().equals("new-folder")) 
      doNewFolder();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /**
   * Jump to the directory named by the directory field.
   */ 
  public void 
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
  public void 
  doJumpHome()
  { 
    updateTargetFile(new File(PackageInfo.sHomeDir, PackageInfo.sUser));
  }

  /**
   * Create a new directory under the current working directory and jump to it.
   */ 
  public void 
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
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current directory field.
   */ 
  private JTextField  pDirField;

  /**
   * The file listing.
   */ 
  private JList  pFileList;

  /**
   * The selected file field. <P> 
   * 
   * May be <CODE>null</CODE> if this a directory only dialog.
   */ 
  private JTextField  pFileField;

}
