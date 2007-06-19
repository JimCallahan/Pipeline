// $Id: MasterTestFactory.java,v 1.2 2007/06/19 22:05:03 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   T E S T   F A C T O R Y                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A master manager extension plugin pre-operation test.
 */
public 
interface MasterTestFactory
  extends MasterFactory
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
   BaseMasterExt ext
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
   BaseMasterExt ext
  )
    throws PipelineException;

}



