// $Id: JobGetNumStdOutLinesReq.java,v 1.1 2004/10/28 15:55:24 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   G E T   N U M   S T D O U T   L I N E S   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the current number of lines of STDOUT output from the given job. 
 */
public
class JobGetNumStdOutLinesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobID
   *   The unique job identifier.
   */
  public
  JobGetNumStdOutLinesReq
  (
   long jobID
  )
  { 
    pJobID = jobID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique job identifier. 
   */
  public long
  getJobID()
  {
    return pJobID; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4997352535812356459L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifier. 
   */ 
  private long  pJobID; 

}
  
