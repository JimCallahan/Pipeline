// $Id: BaseQueueTask.java,v 1.2 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   Q U E U E   T A S K                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class of all queue extension tasks.
 */
public abstract
class BaseQueueTask
  extends BaseExtTask 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new task.
   * 
   * @param title
   *   An identifying title for the task.
   * 
   * @param name
   *   The name of the extension configuration. 
   * 
   * @param ext
   *   The queue manager extension plugin.
   */ 
  public 
  BaseQueueTask
  (
   String title, 
   QueueExtensionConfig config, 
   BaseQueueExt ext
  )      
  {
    super(title + ":Config[" + config.getName() + "]"); 

    pConfig    = config; 
    pExtension = ext;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform the task.
   */ 
  public void 
  run() 
  {
    try {
      TaskTimer timer = new TaskTimer(getName()); 

      runTask();

      LogMgr.getInstance().logStage
	(LogMgr.Kind.Ext, LogMgr.Level.Fine,
	 timer); 
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Severe,
	 ex.getMessage()); 
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      taskFinished();
    }
  }

  /**
   * Call the extension plugin method corresponding to this task. 
   */ 
  public abstract void 
  runTask()
    throws PipelineException;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The extension configuration. 
   */
  protected QueueExtensionConfig  pConfig; 

  /**
   * The queue manager extension.
   */
  protected BaseQueueExt  pExtension; 

}



