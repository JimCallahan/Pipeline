// $Id: JobGetStdErrLinesReq.java,v 1.1 2004/07/28 19:10:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   G E T   S T D E R R   L I N E S   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get current collected lines of captured STDERR output from the given job.
 */
public
class JobGetStdErrLinesReq
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
   * 
   * @param start 
   *   The index of the first line of output to return.  
   */
  public
  JobGetStdErrLinesReq
  (
   long jobID, 
   int start
  )
  { 
    pJobID = jobID;
    pStart = start;
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

  /**
   * Gets the index of the first line of output.
   */
  public int
  getStart()
  {
    return pStart;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8146377456833693764L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifier. 
   */ 
  private long  pJobID; 

  /**
   * The index of the first line of output.
   */ 
  private int  pStart; 

}
  
