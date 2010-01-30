// $Id: NodeNoteTool.java,v 1.2 2009/06/02 18:17:54 jlee Exp $

package us.temerity.pipeline.plugin.TestFifoLogTool.v2_4_23;

import java.awt.Component;
import java.awt.Dimension;
import java.util.TreeMap;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*; 

import us.temerity.pipeline.*;


/*------------------------------------------------------------------------------------------*/
/*   T E S T   F I F O   L O G   T O O L                                                    */
/*------------------------------------------------------------------------------------------*/

public 
class TestFifoLogTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  TestFifoLogTool()
  {
    super("TestFifoLog", new VersionID("2.4.23"), "Temerity",
	  "Demonstrates spawning a thread which logs to a FIFO and is read by the Tool.");
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    pMessageTask = new AtomicReference<MessageTask>();
    pFifo = new LinkedBlockingDeque<String>();

    underDevelopment(); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public boolean 
  updateOnExit()
  {
    return false;
  }

  @Override
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    MessageTask task = pMessageTask.get();
    if(task == null) {
      task = new MessageTask();
      task.start();
      
      pMessageTask.set(task);

      return ": Starting Message Task..."; 
    }
    else {
      return pMessage; 
    }
  }
  
  @Override  
  public synchronized boolean
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    MessageTask task = pMessageTask.get();
    if(task == null) 
      return false;

    while(true) {
      pMessage = null;
      if(!task.isAlive()) 
        return false;

      try {
        String msg = pFifo.poll(100, TimeUnit.MILLISECONDS); 
        if(msg != null) {
          pMessage = ": " + msg;
          return true; 
        }
      }
      catch(InterruptedException ex) {
        return false;
      }
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class MessageTask
    extends Thread
  {
    public 
    MessageTask() 
    {
      super("TestFifoLogTool:MessageTask"); 
    }

    public void 
    run() 
    {
      try {
        LogMgr logMgr = LogMgr.getInstance("TestFifoLogTool"); 
        logMgr.logToFifo(pFifo); 

        int wk;
        for(wk=1; wk<=10; wk++) {
          logMgr.log(LogMgr.Kind.Ops, LogMgr.Level.Info, "Message " + wk + " of 10..."); 

          try {
            sleep(1500); 
          }
          catch(InterruptedException ex) {
            logMgr.log(LogMgr.Kind.Ops, LogMgr.Level.Warning, "MessageTask Interrupted.");
          }
        }        
      }
      catch(Exception ex) {
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5005748337009497225L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The thread which generates log messages.
   */ 
  private AtomicReference<MessageTask>  pMessageTask;   

  /**
   * The FIFO used to communicate log messages.
   */ 
  private LinkedBlockingDeque<String>   pFifo; 

  /**
   * The last message.
   */ 
  private String  pMessage; 

}
