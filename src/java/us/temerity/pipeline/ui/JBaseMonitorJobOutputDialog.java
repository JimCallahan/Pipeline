// $Id: JBaseMonitorJobOutputDialog.java,v 1.1 2004/09/05 06:54:56 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   M O N I T O R   J O B   O U T P U T   D I A L O G                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Monitor the output from a running job.
 */ 
public abstract 
class JBaseMonitorJobOutputDialog
  extends JBaseDialog
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
   * @param prefix
   *   A string to prepend to the header text.
   * 
   * @param label
   *   The text of the label above the scrolled output area.
   * 
   * @param job
   *   The queue job.
   * 
   * @param info
   *   The current job status information. 
   */ 
  public 
  JBaseMonitorJobOutputDialog
  (
   String title, 
   String prefix,
   String label, 
   QueueJob job, 
   QueueJobInfo info
  ) 
  {
    super(title, false);

    pShouldUpdate = new AtomicBoolean(false);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      
      body.add(UIMaster.createPanelLabel(label));
	       
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JTextArea area = new JTextArea(null, 20, 90);
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
            
      ActionAgenda agenda = job.getActionAgenda();
      String header = 
	(prefix + " - Job " + job.getJobID() + ":  " + agenda.getPrimaryTarget() + 
	 "    [" + info.getHostname() + "]");
      
      super.initUI(header, false, body, null, null, null, "Close");
    }
    
    addWindowListener(this);

    {    
      pMonitorTask = new MonitorTask(info.getHostname(), job.getJobID());
      pMonitorTask.start();
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current collected lines of captured output from the job server for the given 
   * job starting at the given line. 
   * 
   * @param client
   *   The job manager connection.
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @param start 
   *   The index of the first line of output to return.  
   */
  protected abstract String[]
  getOutputLines
  (
   JobMgrClient client,
   long jobID, 
   int start   
  )
    throws PipelineException;


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
    pShouldUpdate.set(false);
    if(pMonitorTask.isAlive()) 
      pMonitorTask.interrupt();
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
  windowDeiconified
  (
   WindowEvent e
  ) 
  {
    pShouldUpdate.set(true);
  }

  /**
   * Invoked when a window is changed from a normal to a minimized state.
   */ 
  public void 	
  windowIconified
  (
   WindowEvent e
  ) 
  {
    pShouldUpdate.set(false);
  }

  /**
   * Invoked the first time a window is made visible.	
   */ 
  public void     
  windowOpened
  (
   WindowEvent e
  ) 
  {
    pShouldUpdate.set(true);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Disconnect from job server and close window.
   */ 
  public void 
  doCancel()
  {
    super.doCancel();

    pShouldUpdate.set(false);
    if(pMonitorTask.isAlive()) 
      pMonitorTask.interrupt();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Periodically retrieves the output from the job until the job has completed.
   */ 
  private 
  class MonitorTask
    extends Thread
  {
    public 
    MonitorTask
    (
     String hostname, 
     long jobID
    )
    {
      super("JMonitorJobStdOutDialog:MonitorTask");

      pHostname = hostname; 
      pJobID    = jobID; 
    }
    
    public void 
    run()
    {
      UIMaster master = UIMaster.getInstance();
      JobMgrClient client = null;
      try {
	client = new JobMgrClient(pHostname, master.getJobPort());	
	
	int i = 0;
	boolean done = false;
	while(!done) {
	  if(pShouldUpdate.get()) {
	    String[] lines = getOutputLines(client, pJobID, i);
	    i += lines.length;
	    
	    int wk;
	    for(wk=0; wk<lines.length; wk++) {
	      if(lines[wk] == null) {
		done = true; 
		break;
	      }
	      else {
		pOutputArea.append(lines[wk] + "\n");
	      }
	    }
	    
	    if(lines.length > 0) 
	      SwingUtilities.invokeLater(new ScrollTask());
	  }

	  try {
	    sleep(2000);
	  }
	  catch (InterruptedException ex) {
	    return;
	  }
	}
      }
      catch (Exception ex) {
	master.showErrorDialog(ex);	
      }
      finally {
	if(client != null)
	  client.disconnect();
      }
    }

    private String  pHostname;
    private long    pJobID; 
  }

  /** 
   * Scroll to the end of the output.
   */
  private
  class ScrollTask
    extends Thread
  {
    public 
    ScrollTask() 
    {
      super("JBaseMonitorJobOutputDialog:ScrollTask");
    }

    public void 
    run()
    {
      Dimension size = pOutputArea.getSize();
      Rectangle rect = new Rectangle(0, size.height-1, 1, size.height);
      pOutputScroll.getViewport().scrollRectToVisible(rect);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the dialog should update the output text from the job server.
   */ 
  private AtomicBoolean  pShouldUpdate; 


  /**
   * The output text area.
   */ 
  private JTextArea  pOutputArea;
  
  /**
   * The output scroll pane.
   */ 
  private JScrollPane  pOutputScroll;

  
  /**
   * The thread monitoring the job.
   */ 
  private MonitorTask  pMonitorTask; 

}
