// $Id: JBaseFileSelectDialog.java,v 1.6 2004/11/21 18:39:56 jim Exp $

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
/*   B A S E   F I L E   S E L E C T   D I A L O G                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class of file selection dialogs.
 */ 
public abstract 
class JBaseFileSelectDialog
  extends JBaseDialog
implements ListSelectionListener, DocumentListener, MouseListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog for selecting files.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  public 
  JBaseFileSelectDialog
  (
   String title
  ) 
  {
    super(title, true);
    pRootDir = new File("/");
  }

  /**
   * Construct a new dialog for selecting files.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  public 
  JBaseFileSelectDialog
  (
   Dialog owner,
   String title
  ) 
  {
    super(owner, title, true);
    pRootDir = new File("/");
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   * 
   * If the <CODE>fieldTitle</CODE> argument is <CODE>null</CODE> no selected file text 
   * field is displayed and the <CODE>fieldTitleSize</CODE> argument is ignored.
   * 
   * @param header
   *   The header label.
   *
   * @param renderer
   *   The list cell renderer.
   * 
   * @param fieldTitle
   *   The title of the filename field.
   * 
   * @param fieldTitleSize
   *   The width of the filename field title.
   * 
   * @param fileField
   *   The filename text field.
   * 
   * @param confirm
   *   The name of the confirm button.
   */ 
  protected void
  initUI
  (
   String header,
   ListCellRenderer renderer, 
   String fieldTitle, 
   int fieldTitleSize, 
   JTextField fileField, 
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

	  btn.setToolTipText(UIMaster.formatToolTip("Creates a new folder."));

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
	lst.setCellRenderer(renderer);
	lst.addListSelectionListener(this);
	lst.addMouseListener(this);

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
	    pFileField = fileField;

	    fileField.addActionListener(this);
	    fileField.setActionCommand("confirm");

	    fileField.getDocument().addDocumentListener(this);

	    hbox.add(fileField);
	  }
	  
	  body.add(hbox);
	}  
      }

      super.initUI(header, true, body, confirm, null, null, "Close");

      pack();
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the enabled status of the confirm button.
   */ 
  protected void 
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
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set the root directory.
   * 
   * @param root
   *   The root directory under which the dialog will browse.
   */ 
  public void 
  setRootDir
  (
   File root
  ) 
  {
    if(root == null) 
      throw new IllegalArgumentException
	("The root directory cannot be (null)!");

    if(!root.isDirectory()) 
      throw new IllegalArgumentException
	("The root directory must exist!");

    pRootDir = root;
  }

  /**
   * Get the current directory.
   */ 
  public File 
  getDirectory() 
  {
    String dir = pDirField.getText();
    if((dir == null) || (dir.length() == 0))
      return null;

    return new File(dir);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public abstract void 	
  valueChanged
  (
   ListSelectionEvent e
  );


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


  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered(MouseEvent e) {}
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    switch(e.getButton()) {
    case MouseEvent.BUTTON1:
      if(e.getClickCount() == 2) {
	int idx = pFileList.locationToIndex(e.getPoint());
	if(idx != -1) {
	  doHandleDoubleClick(pFileList.getModel().getElementAt(idx));
	}
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}


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
   * Handle double-click on the given list element.
   */ 
  protected abstract void 
  doHandleDoubleClick
  (
   Object elem
  );

  /**
   * Jump to the directory named by the directory field.
   */ 
  protected abstract void 
  doJumpDir();

  /**
   * Jump to the home directory.
   */ 
  protected abstract void 
  doJumpHome();

  /**
   * Create a new directory under the current working directory and jump to it.
   */ 
  protected abstract void 
  doNewFolder();



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root directory.
   */ 
  protected File  pRootDir;


  /**
   * The current directory field.
   */ 
  protected JTextField  pDirField;

  /**
   * The file listing.
   */ 
  protected JList  pFileList;

  /**
   * The selected file field. <P> 
   * 
   * May be <CODE>null</CODE> if this a directory only dialog.
   */ 
  protected JTextField  pFileField;

}
