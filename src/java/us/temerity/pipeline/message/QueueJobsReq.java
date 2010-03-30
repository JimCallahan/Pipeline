// $Id: QueueJobsReq.java,v 1.1 2006/11/11 20:45:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B S   R E Q                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to perform an operation on jobs with the given IDs. <P> 
 */
public
class QueueJobsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobIDs
   *   The unique job identifiers.
   */
  public
  QueueJobsReq
  (
   TreeSet<Long> jobIDs
  )
  { 
    super();

    if(jobIDs == null) 
      throw new IllegalArgumentException("The job IDs cannot be (null)!");
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

  private static final long serialVersionUID = 5346988266848429849L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifiers.
   */ 
  private TreeSet<Long>  pJobIDs;

}

  
