// $Id: ModifyHostsExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M O D I F Y   H O S T S   E X T   F A C T O R Y                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Task to perform after modifying host status or properties.
 */
public 
class ModifyHostsExtFactory
  implements QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory only for testing if the task is supported.
   */ 
  public 
  ModifyHostsExtFactory() 
  {}

  /**
   * Construct a task factory.
   * 
   * @param hosts
   *   The information about the modified hosts indexed by fully resolved hostname.
   */ 
  public 
  ModifyHostsExtFactory
  (
   TreeMap<String,QueueHostInfo> hosts
  )      
  {
    pHosts = hosts;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the extension support post-tasks for this kind of operation.
   */ 
  public boolean 
  hasTask
  (   
   BaseQueueExt ext
  ) 
  {
    return ext.hasPostModifyHostsTask();
  }

  /**
   * Create and start a new thread to run the post-operation task. 
   */ 
  public void 
  startTask
  (   
   QueueExtensionConfig config, 
   BaseQueueExt ext
  ) 
  {
    PostModifyHostsTask task = new PostModifyHostsTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostModifyHostsTask
    extends BaseQueueTask
  {
    public 
    PostModifyHostsTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostModifyHostsTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postModifyHostsTask(pHosts); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The information about the modified hosts indexed by fully resolved hostname.
   */ 
  private TreeMap<String,QueueHostInfo>  pHosts; 

}



