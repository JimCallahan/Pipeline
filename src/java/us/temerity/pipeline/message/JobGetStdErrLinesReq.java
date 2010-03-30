// $Id: JobGetStdErrLinesReq.java,v 1.2 2004/10/28 15:55:24 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   G E T   S T D E R R   L I N E S   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the contents of the given region of lines of the STDERR output 
 * from the given job. 
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
   *   The line number of the first line of text.
   * 
   * @param lines
   *   The number of lines of text to retrieve. 
   */
  public
  JobGetStdErrLinesReq
  (
   long jobID, 
   int start, 
   int lines
  )
  { 
    pJobID = jobID;
    pStart = start;
    pLines = lines;
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

  /**
   * Gets the number of lines of text to retrieve. 
   */
  public int
  getLines()
  {
    return pLines;
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

  /**
   * The number of lines of text to retrieve. 
   */
  private int  pLines; 

}
  
