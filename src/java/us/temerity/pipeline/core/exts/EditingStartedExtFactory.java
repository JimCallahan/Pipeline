// $Id: EditingStartedExtFactory.java,v 1.1 2009/10/09 15:58:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import us.temerity.pipeline.event.*;

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E D I T I N G   S T A R T E D   E X T   F A C T O R Y                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Task to run after an Editor plugin has been started for a working version of a node.
 */
public 
class EditingStartedExtFactory
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
  EditingStartedExtFactory
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
    return ext.hasPostEditingStartedTask();
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
    PostEditingStartedTask task = new PostEditingStartedTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostEditingStartedTask
    extends BaseMasterTask
  {
    public 
    PostEditingStartedTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostEditingStartedTask:EventID[" + pEventID + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postEditingStartedTask(pEventID, pEvent); 
    }
  }
}



