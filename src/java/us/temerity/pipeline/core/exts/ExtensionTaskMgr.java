// $Id: ExtensionTaskMgr.java,v 1.2 2009/05/16 02:06:19 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*; 

/*------------------------------------------------------------------------------------------*/
/*   E X T E N S I O N   T A S K   M G R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Keeps track of running server extension tasks.
 */
public class
ExtensionTaskMgr
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new manager.
   */ 
  public 
  ExtensionTaskMgr() 
  {
    pIsJoining = new AtomicBoolean(false);
    pTasks = new TreeSet<BaseExtTask>();
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   T A S K   M A N A G E M E N T                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Begin managing the given task.
   */ 
  public void 
  add
  (
   BaseExtTask task
  ) 
  {
    synchronized(pTasks) {
      pTasks.add(task);
    }
  }

  /**
   * Finish managing the given task.
   */ 
  public void 
  remove
  (
   BaseExtTask task
  ) 
  {
    if(!pIsJoining.get()) {
      synchronized(pTasks) { 
	pTasks.remove(task);
      }
    }
  }

  /**
   * Wait for all running tasks to finish.
   */ 
   public void 
   joinAll() 
     throws InterruptedException
  {
    pIsJoining.set(true);
    synchronized(pTasks) { 
      for(BaseExtTask task : pTasks) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Info,
	   "Waiting on " + task.getName() + "...");
	LogMgr.getInstance().flush();
	
	task.join();
      }
      pTasks.clear();
    }
    pIsJoining.set(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a join in is progress.
   */ 
  private AtomicBoolean  pIsJoining; 

  /**
   * The manage tasks.
   */ 
  private TreeSet<BaseExtTask>  pTasks; 

}
