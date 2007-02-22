// $Id: JTestEnvironmentDialog.java,v 1.8 2007/02/22 16:12:39 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 
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
class JTestEnvironmentDialog
  extends JTopLevelDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JTestEnvironmentDialog
  (
   String title
  ) 
  {
    super(title);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      body.add(UIFactory.createPanelLabel("Working Directory:"));
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));

      {
	Box hbox = new Box(BoxLayout.X_AXIS); 

	{
	  JTextField field = 
	    UIFactory.createEditableTextField(null, sSize-27, JLabel.LEFT);
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
      
      body.add(UIFactory.createPanelLabel("Test Command:"));

      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JTextArea area = UIFactory.createEditableTextArea(null, sSize, 3);
	pCommandArea = area;

	body.add(area);
      }

      JButton btns[] = super.initUI("X", body, null, "Run Test", null, "Close");

      pack();
    }

    pBrowseDialog = 
      new JFileSelectDialog(this, "Select Directory", "Select Working Directory:", "Select");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the header, underlying environment and UI components.
   * 
   * @param header
   *   The header label of the dialog.
   * 
   * @param env
   *   The environment.
   */ 
  public void 
  updateEnvironment
  (
   String header, 
   Map<String,String> env
  )
  { 
    pHeaderLabel.setText(header);
    pEnvironment = env;

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
    if(e.getActionCommand().equals("browse"))
      doBrowse();
    else 
      super.actionPerformed(e);
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

      StringBuilder buf = new StringBuilder();
      buf.append(program);

      int wk;
      for(wk=1; wk<argv.length; wk++) {
	args.add(argv[wk]);
	buf.append(" " + argv[wk]);
      }

      command = buf.toString();
    }

    File outFile = null;
    try {
      outFile = File.createTempFile("plui-test-output.", ".tmp", 
				    PackageInfo.sTempPath.toFile());
      FileCleaner.add(outFile);
    }
    catch(IOException ex) {
      showErrorDialog(ex);
      return;
    }

    File errFile = null;
    try {
      errFile = File.createTempFile("plui-test-errors.", ".tmp", 
				    PackageInfo.sTempPath.toFile());
      FileCleaner.add(errFile);
    }
    catch(IOException ex) {
      showErrorDialog(ex);
      return;
    }

    try {
      SubProcessHeavy proc = 
	new SubProcessHeavy("TestEnvironment", program, args, 
			    pEnvironment, dir, outFile, errFile);
    
      JMonitorSubProcessDialog diag = 
	new JMonitorSubProcessDialog("Test Environmant Output", pHeaderLabel.getText(), proc);
      diag.setVisible(true);
    }
    catch(PipelineException ex) {
      showErrorDialog("Error:", ex.getMessage());
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

  private static final long serialVersionUID = -1308056963863107944L;
  
  private static final int sSize = 450;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The environment.
   */ 
  private Map<String,String>  pEnvironment;  


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
