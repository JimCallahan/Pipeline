// $Id: NodeGarbageCollectExtFactory.java,v 1.3 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G A R B A G E   C O L L E C T O R   E X T   F A C T O R Y                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Task to perform after running the node garbage collector.
 */
public 
class NodeGarbageCollectExtFactory
  implements MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param cached
   *   The total number of node versions currently cached.
   * 
   * @param freed
   *   The number of node versions freed during this collection cycle.
   */ 
  public 
  NodeGarbageCollectExtFactory
  (
   long cached, 
   long freed
  )      
  {
    pCached = cached;
    pFreed  = freed;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all nodes associated with the operation.
   */ 
  public LinkedList<String> 
  getNodeNames()
  {
    return new LinkedList<String>();
  }

  /**
   * Get the name of the user performing the operation. 
   */ 
  public String 
  getWorkUser()
  {
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the extension support post-tasks for this kind of operation.
   */ 
  public boolean 
  hasTask
  (   
   BaseMasterExt ext
  ) 
  {
    return ext.hasPostNodeGarbageCollectTask();
  }

  /**
   * Get the requirements to for the post-operation task. 
   */ 
  public ExtReqs
  getTaskReqs
  (   
   BaseMasterExt ext
  ) 
  {
    return new ExtReqs();
  }

  /**
   * Create and start a new thread to run the post-operation task. 
   */ 
  public void 
  startTask
  (   
   MasterExtensionConfig config, 
   BaseMasterExt ext
  ) 
  {
    PostNodeGarbageCollectTask task = new PostNodeGarbageCollectTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostNodeGarbageCollectTask
    extends BaseMasterTask
  {
    public 
    PostNodeGarbageCollectTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostNodeGarbageCollectTask", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postNodeGarbageCollectTask(pCached, pFreed);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The total number of node versions currently cached.
   */ 
  private long  pCached; 

  /**
   * The number of node versions freed during this collection cycle.
   */ 
  private long  pFreed; 

}



