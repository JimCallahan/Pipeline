// $Id: JTestPackageDialog.java,v 1.1 2004/05/29 06:38:43 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.toolset.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   P A C K A G E   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JTestPackageDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JTestPackageDialog() 
  {
    super("Test Package", false);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      body.add(UIMaster.createPanelLabel("Working Directory:"));
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));

      {
	Box hbox = new Box(BoxLayout.X_AXIS); 

	{
	  JTextField field = 
	    UIMaster.createEditableTextField(null, sSize-27, JLabel.LEFT);
	  pWorkDirField = field;

	  hbox.add(field);
	}
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JButton btn = new JButton();
	  pBrowseButton = btn;
	  btn.setName("BrowseButton");
	  
	  Dimension size = new Dimension(15, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("browse");
	  btn.addActionListener(this);
	  	  
	  hbox.add(btn);
	}

	body.add(hbox);
      }
      
      body.add(Box.createRigidArea(new Dimension(0, 20)));
      
      body.add(UIMaster.createPanelLabel("Test Command:"));

      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JTextArea area = UIMaster.createEditableTextArea(null, sSize, 3);
	pCommandArea = area;

	body.add(area);
      }

      JButton btns[] = super.initUI("X", false, body, null, "Run Test", null, "Close");

      pack();
      setResizable(false);
    }

    pBrowseDialog = 
      new JFileSelectDialog(this, "Select Directory", "Select Working Directory:", "Select");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the underlying package and UI components.
   */ 
  public void 
  updatePackage
  (
   PackageCommon com
  )
  { 
    if(com != null) {
      pPackage = com;

      if(pPackage instanceof PackageMod) 
	pHeaderLabel.setText("Test Package:  " + pPackage.getName());
      else if(pPackage instanceof PackageVersion) 
	pHeaderLabel.setText("Test Package:  " + pPackage.getName() + 
			     " (v" + ((PackageVersion) pPackage).getVersionID() + ")");
    }

    validateWorkingDir();
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

    if(e.getActionCommand().equals("browse"))
      doBrowse();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Run the command under the package environment.
   */ 
  public void 
  doApply()
  {
    File dir = validateWorkingDir();

    String text = pCommandArea.getText();
    if(text.length() == 0) 
      return;

    String program = null;
    ArrayList<String> args = new ArrayList<String>();
    String command = null;  
    {
      String argv[] = text.split("\\s");
      program = argv[0];

      StringBuffer buf = new StringBuffer();
      buf.append(program);

      int wk;
      for(wk=1; wk<argv.length; wk++) {
	args.add(argv[wk]);
	buf.append(" " + argv[wk]);
      }

      command = buf.toString();
    }

    Map<String,String> env = pPackage.getEnvironment(PackageInfo.sUser, "default");

    try {
      SubProcess proc = new SubProcess("TestPackage", program, args, env, dir);
    
      JTestPackageOutputDialog diag = new JTestPackageOutputDialog(pPackage, command, proc);
      diag.setVisible(true);
    }
    catch(IllegalArgumentException ex) {
      UIMaster.getInstance().showErrorDialog("Error:", ex.getMessage());
    }
  }
  
  /**
   * Browse for a new working directory.
   */ 
  public void 
  doBrowse()
  {
    pBrowseDialog.setVisible(true);

    if(pBrowseDialog.wasConfirmed()) {
      File dir = pBrowseDialog.getSelectedFile();
      pWorkDirField.setText(dir.toString());
      validateWorkingDir();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Validate the working directory field.
   */
  private File 
  validateWorkingDir() 
  {
    File dir = new File(System.getProperty("user.dir"));

    String cwd = pWorkDirField.getText();
    if(cwd != null) {
      File file = new File(cwd);
      try {
	File canon = file.getCanonicalFile();
	  if(canon.isDirectory()) 
	    dir = canon; 
      }
      catch(IOException ex) {
      }
    }
    
    pWorkDirField.setText(dir.getPath());  
    pBrowseDialog.updateTargetFile(dir);

    return dir;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 3273738289299034425L;
  
  private static final int sSize = 450;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The package.
   */ 
  private PackageCommon  pPackage;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The command output text area.
   */ 
  private JTextField  pWorkDirField;

  /**
   * The directory browse button.
   */ 
  private JButton  pBrowseButton;

  /**
   * The directory browsing dialog.
   */ 
  private JFileSelectDialog  pBrowseDialog;

  /**
   * The test command field.
   */ 
  private JTextArea  pCommandArea;
  

}
