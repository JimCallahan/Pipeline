// $Id: BaseJobExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

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
    pJob = job;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The job specification.
   */
  protected QueueJob  pJob; 

}



