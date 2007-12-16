// $Id: BaseJobExtFactory.java,v 1.2 2007/12/16 06:28:42 jesse Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   J O B   E X T   F A C T O R Y                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The task to perform on a per-job basis. 
 */
public 
class BaseJobExtFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param job
   *   The job specification.
   */ 
  public 
  BaseJobExtFactory
  (
   QueueJob job
  )      
  {
    pJob = job.queryOnlyCopy();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The job specification.
   */
  protected QueueJob  pJob; 

}



