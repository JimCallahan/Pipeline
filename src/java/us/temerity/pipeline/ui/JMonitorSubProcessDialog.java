// $Id: JMonitorSubProcessDialog.java,v 1.5 2006/09/25 12:11:45 jim Exp $

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
/*   M O N I T O R   S U B P R O C E S S   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Monitor the output from a running subprocess.
 */ 
public 
class JMonitorSubProcessDialog
  extends JTopLevelDialog
  implements WindowListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param header
   *   The header label text.
   * 
   * @param proc
   *   The subprocess to monitor.
   */ 
  public 
  JMonitorSubProcessDialog
  (
   String title, 
   String header, 
   SubProcessHeavy proc
  ) 
  {
    super(title);
    
    pHeader = header;
    pProc   = proc;
    
    /* create dialog body components */ 
    {
      JPanel cpanel = new JPanel();
      {
	cpanel.setName("MainDialogPanel");
	cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.Y_AXIS));

	cpanel.add(UIFactory.createPanelLabel("Command:"));
	
	cpanel.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  JTextArea area = new JTextArea(proc.getCommand(), 2, 80);
	  area.setName("CodeTextArea");
	  area.setLineWrap(true);
	  area.setWrapStyleWord(true);
	  area.setEditable(false);
	
	  {
	    JScrollPane scroll = new JScrollPane(area);
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    
	    scroll.setMinimumSize(new Dimension(100, 50));
	    
	    cpanel.add(scroll);
	  }	
	}
      }

      JSplitPane split = null;
      {
	JPanel opanel = new JPanel();
	{
	  opanel.setName("MainDialogPanel");
	  opanel.setLayout(new BoxLayout(opanel, BoxLayout.Y_AXIS));

	  opanel.add(UIFactory.createPanelLabel("Output:"));
	  
	  opanel.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    JFileMonitorPanel panel = new JFileMonitorPanel(proc.getStdOutFile());
	    pStdOutPanel = panel;
	    
	    opanel.add(panel);
	  }

	  opanel.setMinimumSize(new Dimension(100, 20));
	}
	
	JPanel epanel = new JPanel();
	{
	  epanel.setName("MainDialogPanel");
	  epanel.setLayout(new BoxLayout(epanel, BoxLayout.Y_AXIS));
	  
	  epanel.add(UIFactory.createPanelLabel("Errors:"));
	  
	  epanel.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  {
	    JFileMonitorPanel panel = new JFileMonitorPanel(proc.getStdErrFile());
	    pStdErrPanel = panel;
	    
	    epanel.add(panel);
	  }

	  epanel.setMinimumSize(new Dimension(100, 20));
	}

	split = new JVertSplitPanel(opanel, epanel);
      }  
      
      JSplitPane body = new JVertSplitPanel(cpanel, split);

      body.setAlignmentX(0.5f);
      body.setMinimumSize(new Dimension(100, 350));
      body.setPreferredSize(new Dimension(800, 800));

      body.setDividerLocation(100);

      super.initUI(header, body, null, null, null, "Kill");
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
    public 
    RunTask() 
    {
      super("JMonitorSubProcessDialog:RunTask");
    }

    public void 
    run() 
    {
      if(pProc == null) 
	return;

      pProc.start();

      UpdateTask update = new UpdateTask();
      update.start();

      try {
	pProc.join();
	update.join();

	SwingUtilities.invokeLater(new DoneTask());
      }
      catch (InterruptedException ex) {
	update.interrupt();
      }
    }
  }

  /** 
   * Update the output monitor panels at regular intervals.
   */
   private 
   class UpdateTask
     extends Thread
   {
     public 
     UpdateTask() 
     {
       super("JMonitorSubProcessDialog:UpdateTask");
     }

    public void 
    run()
    {
      do {
	try {
	  sleep(500);
	}
	catch (InterruptedException ex) {
	  return;
	}

	SwingUtilities.invokeLater(new UpdatePanelsTask());
      } 
      while(pProc.isAlive());
    }
  }
  
  /** 
   * Update the output monitor panels.
   */
   private 
   class UpdatePanelsTask
     extends Thread
   {
     public 
     UpdatePanelsTask() 
     {
       super("JMonitorSubProcessDialog:UpdatePanelsTask");
     }

    public void 
    run()
    {
      pHeaderLabel.setText(pHeader + "   (Running)");
      pStdOutPanel.updateScrollBar();
      pStdErrPanel.updateScrollBar();
    }
  }

  /** 
   * Update UI once the test has exited.
   */
  private 
  class DoneTask
    extends Thread
  {
    public 
    DoneTask() 
    {
      super("JMonitorSubProcessDialog:DoneTask");
    }

    public void 
    run()
    {
      if(pProc.wasSuccessful()) 
	pHeaderLabel.setText(pHeader + "   (SUCCESS)");
      else 
	pHeaderLabel.setText(pHeader + "   (FAILED: " + pProc.getExitCode() + ")");
      
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
  private SubProcessHeavy  pProc;

  /**
   * The task monitoring the test process.
   */ 
  private RunTask  pRunTask;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The base header text. 
   */ 
  private String  pHeader; 

  /**
   * The panel monitoring the process STDOUT.
   */ 
  private JFileMonitorPanel  pStdOutPanel;
  
  /**
   * The panel monitoring the process STDOUT.
   */ 
  private JFileMonitorPanel  pStdErrPanel;
  
}
