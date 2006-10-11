// $Id: QueueTestFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   T E S T   F A C T O R Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A queue manager extension plugin pre-operation test.
 */
public 
interface QueueTestFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the extension support pre-tests for this type of operation.
   */ 
  public boolean 
  hasTest
  (   
   BaseQueueExt ext
  ); 

  /**
   * Perform the pre-test passed for this type of operation.
   * 
   * @throws PipelineException
   *   If the test fails.
   */ 
  public void
  performTest
  (   
   BaseQueueExt ext
  )
    throws PipelineException;

}



