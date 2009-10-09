// $Id: EditingFinishedExtFactory.java,v 1.1 2009/10/09 15:58:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import us.temerity.pipeline.event.*;

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E D I T I N G   S T A R T E D   E X T   F A C T O R Y                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Task to run after an Editor plugin has finished for a working version of a node.
 */
public 
class EditingFinishedExtFactory
  extends BaseEditingExtFactory
  implements MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param editID
   *   The unique ID for the editing session.
   * 
   * @param event
   *   The information known about the editing session.
   */ 
  public 
  EditingFinishedExtFactory
  (
   long editID, 
   EditedNodeEvent event   
  )      
  {
    super(editID, event); 
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
   BaseMasterExt ext
  ) 
  {
    return ext.hasPostEditingFinishedTask();
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
    PostEditingFinishedTask task = new PostEditingFinishedTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostEditingFinishedTask
    extends BaseMasterTask
  {
    public 
    PostEditingFinishedTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostEditingFinishedTask:EventID[" + pEventID + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postEditingFinishedTask(pEventID, pEvent); 
    }
  }
}



