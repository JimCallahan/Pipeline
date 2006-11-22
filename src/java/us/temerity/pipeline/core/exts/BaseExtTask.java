// $Id: BaseExtTask.java,v 1.2 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E X T   T A S K                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class of all extension tasks.
 */
public 
class BaseExtTask
  extends BaseExtThread 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new task.
   * 
   * @param title
   *   An identifying title for the task.
   */ 
  public 
  BaseExtTask
  (
   String title
  )      
  {
    super(title); 
    
    sTaskMgr.add(this);
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   T A S K   M A N A G E M E N T                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Wait for all running tasks to finish.
   */ 
  public static void 
  joinAll() 
    throws InterruptedException
  {
    sTaskMgr.joinAll();
  }
  
  /**
   * Remove this task from the list of running tasks.
   */ 
  protected void 
  taskFinished() 
  {
    sTaskMgr.remove(this);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  protected String 
  getFullMessage
  (
   Throwable ex
  ) 
  {
    StringBuilder buf = new StringBuilder();
     
    if(ex.getMessage() != null) 
      buf.append(ex.getMessage() + "\n\n"); 	
    else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
      
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = ex.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");
   
    return (buf.toString());
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The set of running task instances.
   */
  private static ExtensionTaskMgr  sTaskMgr = new ExtensionTaskMgr();

}



