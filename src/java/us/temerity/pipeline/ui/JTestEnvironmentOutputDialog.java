// $Id: JTestEnvironmentOutputDialog.java,v 1.2 2004/06/14 22:51:28 jim Exp $

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
/*   T E S T   E N V I R O N M E N T   O U T P U T   D I A L O G                            */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class JTestEnvironmentOutputDialog
  extends JBaseDialog
  implements WindowListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JTestEnvironmentOutputDialog
  (
   String header, 
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
	area.setEditable(false);
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
      
      super.initUI(header, false, body, null, null, null, "Kill");
    }

    addWindowListener(this);

    pRunTask = new RunTask();
    pRunTask.start();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- WINDOW LISTENER METHODS -------------------------------------------------------------*/

  /**
   * Invoked when the Window is set to be the active Window.
   */
  public void 
  windowActivated(WindowEvent e) {} 

  /**
   * Invoked when a window has been closed as the result of calling dispose on the window.
   */ 
  public void 	
  windowClosed(WindowEvent e) {} 

  /**
   * Invoked when the user attempts to close the window from the window's system menu.
   */ 
  public void 	
  windowClosing
  (
   WindowEvent e
  ) 
  {
    cleanupTestProcess();
  }

  /**
   * Invoked when a Window is no longer the active Window.
   */ 
  public void 	
  windowDeactivated(WindowEvent e) {}

  /**
   * Invoked when a window is changed from a minimized to a normal state.
   */ 
  public void 	
  windowDeiconified(WindowEvent e) {}

  /**
   * Invoked when a window is changed from a normal to a minimized state.
   */ 
  public void 	
  windowIconified(WindowEvent e) {}

  /**
   * Invoked the first time a window is made visible.	
   */ 
  public void     
  windowOpened(WindowEvent e) {}



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Kill the command if its still running and close.
   */ 
  public void 
  doCancel()
  {
    cleanupTestProcess();
    super.doCancel();    
  }

  /**
   * Kill the command if its still running.
   */ 
  private void 
  cleanupTestProcess()
  {
    if((pProc != null) && (pRunTask != null) && pRunTask.isAlive())
      pProc.kill();

    try {
      pRunTask.join();
    }
    catch (InterruptedException ex) {
    }
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

	SwingUtilities.invokeLater(new DoneTask());
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

  /** 
   * Update UI once the test has exited.
   */
  private 
  class DoneTask
    extends Thread
  {
    public void 
    run()
    {
      pCancelButton.setText("Close");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7362522614371871492L;
  


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
