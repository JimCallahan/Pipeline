// $Id: NodeKillJobsReq.java,v 1.2 2004/08/22 22:04:34 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   K I L L   J O B S   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to kill the jobs with the given IDs. <P> 
 * 
 * @see MasterMgr
 */
public
class NodeKillJobsReq
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
  NodeKillJobsReq
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

  private static final long serialVersionUID = -3516689695721074417L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifiers.
   */ 
  private TreeSet<Long>  pJobIDs;

}
  
