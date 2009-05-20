// $Id: BaseExtTask.java,v 1.4 2009/05/20 08:14:21 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E X T   T A S K                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class of all extension tasks.
 */
public 
class BaseExtTask
  extends BaseExtThread 
  implements Comparable<BaseExtTask> 
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

    pTaskID = pNextTaskID.incrementAndGet();

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
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof BaseExtTask)) {
      BaseExtTask task = (BaseExtTask) obj;
      return (pTaskID == task.pTaskID); 
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this <CODE>BaseExtTask</CODE> with the given <CODE>BaseExtTask</CODE> for order.
   * 
   * @param vid 
   *   The <CODE>BaseExtTask</CODE> to be compared.
   */
  public int
  compareTo
  (
   BaseExtTask task
  )
  {
    if(pTaskID > task.pTaskID)
      return 1; 
    else if(pTaskID < task.pTaskID)
      return -1;
    else 
      return 0;
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The next available task ID.
   */
  private static AtomicLong  pNextTaskID = new AtomicLong(1L);

  /**
   * The set of running task instances.
   */
  private static ExtensionTaskMgr  sTaskMgr = new ExtensionTaskMgr();
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * A unique task identifier.
   */
  private long pTaskID; 

}



