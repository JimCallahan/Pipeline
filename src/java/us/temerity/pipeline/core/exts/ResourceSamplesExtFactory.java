// $Id: ResourceSamplesExtFactory.java,v 1.2 2006/11/21 19:55:51 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S O U R C E   S A M P L E S   E X T   F A C T O R Y                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Task to perform after writing job server dynamic resource samples to disk.
 */
public 
class ResourceSamplesExtFactory
  implements QueueTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   */ 
  public 
  ResourceSamplesExtFactory() 
  {}

  /**
   * Construct a task factory.
   * 
   * @param samples
   *   The dynamic resource samples indexed by fully resolved hostname.
   */ 
  public 
  ResourceSamplesExtFactory
  (
   TreeMap<String,ResourceSampleCache> samples
  )      
  {
    pSamples = samples; 
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
    return ext.hasPostResourceSamplesTask();
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
    PostResourceSamplesTask task = new PostResourceSamplesTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostResourceSamplesTask
    extends BaseQueueTask
  {
    public 
    PostResourceSamplesTask
    (
     QueueExtensionConfig config, 
     BaseQueueExt ext
    )      
    {
      super("PostResourceSamplesTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postResourceSamplesTask(pSamples); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The dynamic resource samples indexed by fully resolved hostname.
   */ 
  private TreeMap<String,ResourceSampleCache>  pSamples; 

}



