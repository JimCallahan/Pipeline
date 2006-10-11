// $Id: MasterTaskFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   T A S K   F A C T O R Y                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A creator of threads to run master extension plugin post-operation tasks.
 */
public 
interface MasterTaskFactory
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
   BaseMasterExt ext
  ); 

  /**
   * Create and start a new thread to run the post-operation task. 
   */ 
  public void
  startTask
  (   
   MasterExtensionConfig config, 
   BaseMasterExt ext
  );

}



