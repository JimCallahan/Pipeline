// $Id: QueueTaskFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   T A S K   F A C T O R Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A creator of threads to run queue extension plugin post-operation tasks.
 */
public 
interface QueueTaskFactory
{  
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
  ); 

  /**
   * Create and start a new thread to run the post-operation task. 
   */ 
  public void
  startTask
  (   
   QueueExtensionConfig config, 
   BaseQueueExt ext
  );

}



