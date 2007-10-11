// $Id: QueueJobReqsReq.java,v 1.1 2007/10/11 18:52:07 jesse Exp $

package us.temerity.pipeline.message;

import java.util.LinkedList;

import us.temerity.pipeline.JobReqs;
import us.temerity.pipeline.JobReqsDelta;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B S   R E Q S   R E Q                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to perform an operation on {@link JobReqs} for the jobs with the given IDs. <P> 
 */
public 
class QueueJobReqsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobReqsChanges
   *    The job requirements indexed by unique job identifiers.
   */
  public
  QueueJobReqsReq
  (
    LinkedList<JobReqsDelta> jobReqsChanges
  )
  {
    super();

    if(jobReqsChanges == null) 
      throw new IllegalArgumentException("The job reqs deltas cannot be (null)!");
    
    pJobReqsChanges = jobReqsChanges;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the change to the job requirements.
   */
  public LinkedList<JobReqsDelta>
  getJobReqsChanges() 
  {
    return pJobReqsChanges;
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1165882052014283325L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The changes to the job requirements.
   */ 
  private LinkedList<JobReqsDelta> pJobReqsChanges;

}
