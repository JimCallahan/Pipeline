// $Id: NodeResumeJobsReq.java,v 1.1 2004/08/30 02:48:15 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E S U M E   J O B S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to resume execution of the paused jobs with the given IDs. <P> 
 * 
 * @see MasterMgr
 */
public
class NodeResumeJobsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param jobIDs
   *   The unique job identifiers.
   */
  public
  NodeResumeJobsReq
  (
   TreeSet<Long> jobIDs
  )
  { 
    if(jobIDs == null)
      throw new IllegalArgumentException
	("The job IDs cannot be (null)!");
    pJobIDs = jobIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job identifiers.
   */
  public TreeSet<Long>
  getJobIDs() 
  {
    return pJobIDs;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6708841332696979754L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifiers.
   */ 
  private TreeSet<Long>  pJobIDs;

}
  
