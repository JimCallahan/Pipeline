// $Id: BaseJobInfoExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   J O B   I N F O   E X T   F A C T O R Y                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The task to perform on a per-job basis which includes execution information. 
 */
public 
class BaseJobInfoExtFactory
  extends BaseJobExtFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param job
   *   The job specification.
   * 
   * @param info
   *   Information about the job.
   */ 
  public 
  BaseJobInfoExtFactory
  (
   QueueJob job, 
   QueueJobInfo info
  )      
  {
    super(job); 

    pInfo = info;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Information about the job.
   */
  protected QueueJobInfo  pInfo; 

}



