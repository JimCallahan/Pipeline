// $Id: JTestPackageOutputDialog.java,v 1.1 2004/05/29 06:38:43 jim Exp $

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
/*   T E S T   P A C K A G E   O U T P U T   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JTestPackageOutputDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JTestPackageOutputDialog
  (
   PackageCommon pkg, 
   String command, 
   SubProcess proc
  ) 
  {
    super("Test Package Output", false);

    pProc = proc;
    
    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      body.add(UIMaster.createPanelLabel("Command Output:"));
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));

      {
	JTextArea area = new JTextArea("COMMAND: " + command + "\n\n", 30, 100);
	pOutputArea = area;
	area.setName("CodeTextArea");
	area.setLineWrap(true);
	area.setWrapStyleWord(true);
	area.setEditable(true);
      }

      {
	JScrollPane scroll = new JScrollPane(pOutputArea);
	pOutputScroll = scroll;
	
	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	
	body.add(scroll);
      }
      
      String header = null;
      if(pkg instanceof PackageMod) 
	header = ("Test Package:  " + pkg.getName());
      else if(pkg instanceof PackageVersion) 
	header = ("Test Package:  " + pkg.getName() + 
		  " (v" + ((PackageVersion) pkg).getVersionID() + ")");

      JButton btns[] = super.initUI(header, false, body, "Kill", null, null, "Close");
    }

    pRunTask = new RunTask();
    pRunTask.start();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Kill the running command and close.
   */ 
  public void 
  doConfirm()
  {
    if(pProc != null)
      pProc.kill();

    try {
      pRunTask.join();
    }
    catch (InterruptedException ex) {
    }

    super.doConfirm();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the test and monitor its progress.
   */ 
  private 
  class RunTask 
    extends Thread
  {
    public void 
    run() 
    {
      if(pProc == null) 
	return;

      pProc.start();

      PrintStdOutTask outTask = new PrintStdOutTask();
      outTask.start();

      PrintStdErrTask errTask = new PrintStdErrTask();
      errTask.start();

      try {
	pProc.join();
	outTask.join();
 	errTask.join();

	if(pProc.wasSuccessful()) 
	  pOutputArea.append("\nSUCCESS.\n");
	else 
	  pOutputArea.append("\nFAILED: Exit Code = " + pProc.getExitCode() + "\n");
      }
      catch (InterruptedException ex) {
	outTask.interrupt();
 	errTask.interrupt();
      }
    }
  }

  /** 
   * Print the STDOUT output from the test in the output text area.
   */
  private 
  class PrintStdOutTask
    extends Thread
  {
    public void 
    run()
    {
      int i = 0;
      do {
	try {
	  sleep(500);
	}
	catch (InterruptedException ex) {
	  return;
	}

	String lines[] = pProc.getStdOutLines(i);
	i += lines.length;

	int wk;
	for(wk=0; wk<lines.length; wk++) 
	  pOutputArea.append(lines[wk] + "\n");

	if(lines.length > 0) 
	  SwingUtilities.invokeLater(new ScrollTask());
      } 
      while(pProc.isAlive());
    }
  }
  
  /** 
   * Print the STDERR output from the test in the output text area.
   */
  private 
  class PrintStdErrTask
    extends Thread
  {
    public void 
    run()
    {
      int i = 0;
      do {
	try {
	  sleep(500);
	}
	catch (InterruptedException ex) {
	  return;
	}

	String lines[] = pProc.getStdErrLines(i);
	i += lines.length;

	int wk;
	for(wk=0; wk<lines.length; wk++) 
	  pOutputArea.append(lines[wk] + "\n");

	if(lines.length > 0) 
	  SwingUtilities.invokeLater(new ScrollTask());
      } 
      while(pProc.isAlive());
    }
  }
  
  /** 
   * Scroll to the end of the output.
   */
  private 
  class ScrollTask
    extends Thread
  {
    public void 
    run()
    {
      Dimension size = pOutputArea.getSize();
      Rectangle rect = new Rectangle(0, size.height-1, 1, size.height);
      pOutputScroll.getViewport().scrollRectToVisible(rect);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 3273738289299034425L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The test process.
   */
  private SubProcess  pProc;

  /**
   * The task monitoring the test process.
   */ 
  private RunTask  pRunTask;


  /**
   * The test output text area.
   */ 
  private JTextArea  pOutputArea;
  
  /**
   * The test output scroll pane.
   */ 
  private JScrollPane  pOutputScroll;
}
