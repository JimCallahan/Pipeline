// $Id: JBaseMonitorJobOutputDialog.java,v 1.2 2005/01/22 21:55:12 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

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
      
      body.add(UIFactory.createPanelLabel(label));
	       
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	JobMonitorPanel panel = new JobMonitorPanel();
	pJobMonitorPanel = panel;
      
	body.add(panel);
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
   * Get the current number of lines of output from the given job. <P> 
   * 
   * @param client
   *   The job manager connection.
   * 
   * @param jobID
   *   The unique job identifier. 
   *    
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */ 
  public abstract int
  getNumLinesMonitor
  (
   JobMgrClient client,
   long jobID
  ) 
    throws PipelineException;
  
  /**
   * Get the contents of the given region of lines of the output from the given job. 
   * 
   * @param client
   *   The job manager connection.
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @param start
   *   The line number of the first line of text.
   * 
   * @param lines
   *   The number of lines of text to retrieve. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public abstract String
  getLinesMonitor
  (
   JobMgrClient client,
   long jobID, 
   int start, 
   int lines
  ) 
    throws PipelineException;

  /**
   * Release any server resources associated with monitoring the output of the 
   * given job.
   * 
   * @param client
   *   The job manager connection.
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public abstract void
  closeMonitor
  (
   JobMgrClient client,
   long jobID
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
   * The output monitor panel.
   */ 
  private 
  class JobMonitorPanel
    extends JBaseMonitorPanel
  {
    public 
    JobMonitorPanel() 
    {
      super();

      pLock = new Object();
    }

    public void
    setJob
    (
     long jobID, 
     JobMgrClient client
    ) 
    {
      synchronized(pLock) {
	pJobID  = jobID; 
	pClient = client;
      }
    }    

    /** 
     * Get the current number of lines which may potentially be viewed.
     */
    protected int 
    getNumLines()
    {
      synchronized(pLock) {
	try {
	  if(pClient != null)
	    return getNumLinesMonitor(pClient, pJobID);
	}
	catch(PipelineException ex) {
	  UIMaster.getInstance().showErrorDialog(ex);
	}
      }

      return 0;
    }
    
    /** 
     * Get the current text for the given region of lines. <P> 
     * 
     * @param start
     *   The line number of the first line of text.
     * 
     * @param lines
     *   The number of lines of text to retrieve. 
     */
    protected String
    getLines
    (
     int start, 
     int lines
    )
    {
      synchronized(pLock) {
	try {
	  if(pClient != null)
	    return getLinesMonitor(pClient, pJobID, start, lines);
	}
	catch(PipelineException ex) {
	  UIMaster.getInstance().showErrorDialog(ex);
	}
      }
      
      return null;
    } 

    private static final long serialVersionUID = 6630563414902079487L;

    private Object        pLock;
    private long          pJobID; 
    private JobMgrClient  pClient; 
  }


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
      super("JMonitorJobOutputDialog:MonitorTask");

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
	pJobMonitorPanel.setJob(pJobID, client);

	boolean done = false;
	while(!done) {
	  pJobMonitorPanel.updateScrollBar();

	  try {
	    sleep(5000);
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
	if(client != null) {
	  try {
	    closeMonitor(client, pJobID);
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }

	  client.disconnect();
	}
      }
    }

    private String  pHostname;
    private long    pJobID; 
  }




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the dialog should update the output text from the job server.
   */ 
  private AtomicBoolean  pShouldUpdate; 

  /**
   * The output monitor panel.
   */ 
  private JobMonitorPanel  pJobMonitorPanel;
  
  /**
   * The thread monitoring the job.
   */ 
  private MonitorTask  pMonitorTask; 

}
