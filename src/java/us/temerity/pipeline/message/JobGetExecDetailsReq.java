// $Id: JobGetExecDetailsReq.java,v 1.1 2006/07/03 06:38:42 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   G E T   E X E C   D E T A I L S   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the execution details for a given job.
 */
public
class JobGetExecDetailsReq
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
  JobGetExecDetailsReq
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

  private static final long serialVersionUID = -7286665130472712422L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifier. 
   */ 
  private long  pJobID; 

}
  
