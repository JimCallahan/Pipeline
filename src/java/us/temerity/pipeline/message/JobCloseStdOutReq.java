// $Id: JobCloseStdOutReq.java,v 1.1 2004/10/28 15:55:24 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   C L O S E   S T D O U T   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to release any server resources associated with monitoring the STDOUT output 
 * of the given job.
 */
public
class JobCloseStdOutReq
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
  JobCloseStdOutReq
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

  private static final long serialVersionUID = -3491960691320004906L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifier. 
   */ 
  private long  pJobID; 

}
  
