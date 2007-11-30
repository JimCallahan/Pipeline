// $Id: NodeResubmitJobsReq.java,v 1.6 2007/11/30 20:14:25 jesse Exp $

package us.temerity.pipeline.message;

import java.util.Set;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S U B M I T   J O B S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to resubmit the group of jobs needed to regenerate the selected 
 * {@link QueueState#Stale Stale} primary file sequences for the tree of nodes rooted at 
 * the given node. <P> 
 * 
 * @see MasterMgr
 */
public
class NodeResubmitJobsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param targetSeqs
   *   The target primary file sequences to regenerate.
   * 
   * @param batchSize 
   *   For parallel jobs, this overrides the maximum number of frames assigned to each job
   *   associated with the root node of the job submission.  
   * 
   * @param reqs
   *   The list of all the job requirements that are going to overridden.
   */
  public
  NodeResubmitJobsReq
  (
   NodeID id, 
   TreeSet<FileSeq> targetSeqs, 
   Integer batchSize, 
   JobReqsDelta reqs 
  )
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pTargetSeqs    = targetSeqs;
    pBatchSize     = batchSize;
    pReqs          = reqs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
  }
  
  /**
   * Gets the target primary file sequences to regenerate.
   */
  public TreeSet<FileSeq>
  getTargetFileSequences() 
  {
    return pTargetSeqs; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * For parallel jobs, this overrides the maximum number of frames assigned to each job
   * associated with the root node of the job submission.
   * 
   * @return 
   *   The batch size or <CODE>null</CODE> use node's original batch size.
   */ 
  public Integer
  getBatchSize() 
  {
    return pBatchSize;
  }

  /** 
   * Overrides the priority of jobs associated with the root node of the job submission 
   * relative to other jobs.  
   * 
   * @return 
   *   The priority or <CODE>null</CODE> to use node's original priority.
   */
  public Integer
  getPriority()
  {
    return pReqs.getPriority();
  }

  /** 
   * Overrides the ramp-up interval of jobs associated with the root node of the job 
   * submission.
   * 
   * @return 
   *   The ramp-up interval or <CODE>null</CODE> to use node's original ramp-up.
   */
  public Integer
  getRampUp()
  {
    return pReqs.getRampUp(); 
  }
  
  /** 
   * Overrides the max load of jobs associated with the root node of the job 
   * submission.
   * 
   * @return 
   *   The max load or <CODE>null</CODE> to use node's original max load.
   */
  public Float
  getMaxLoad() 
  {
    return pReqs.getMaxLoad();
  }

  /** 
   * Overrides the min memory of jobs associated with the root node of the job 
   * submission.
   * 
   * @return 
   *   The min memory or <CODE>null</CODE> to use node's original min memory.
   */
  public Long 
  getMinMemory() 
  {
    return pReqs.getMinMemory();
  }

  /** 
   * Overrides the min disk of jobs associated with the root node of the job 
   * submission.
   * 
   * @return 
   *   The min disk or <CODE>null</CODE> to use node's original min disk.
   */
  public Long 
  getMinDisk() 
  {
    return pReqs.getMinDisk();
  }

  /**
   * Overrides the set of selection keys an eligible host is required to have for jobs 
   * associated with the root node of the job submission.
   * 
   * @return 
   *   The selection keys or <CODE>null</CODE> to use node's original selection keys.
   */
  public Set<String>
  getSelectionKeys()
  {
    return pReqs.getSelectionKeys();
  }

  /**
   * Overrides the set of license keys required by them job associated with the root 
   * node of the job submission.
   * 
   * @return 
   *   The license keys or <CODE>null</CODE> to use node's original license keys.
   */
  public Set<String>
  getLicenseKeys()
  {
    return pReqs.getLicenseKeys();
  }
  
  /**
   * Overrides the set of hardware keys required by them job associated with the root 
   * node of the job submission.
   * 
   * @return 
   *   The hardware keys or <CODE>null</CODE> to use node's original hardware keys.
   */
  public Set<String>
  getHardwareKeys()
  {
    return pReqs.getHardwareKeys();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3721204159915554667L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The target primary file sequences to regenerate.
   */
  private TreeSet<FileSeq>  pTargetSeqs; 

  /**
   * The file sequence indices of the files to regenerate or <CODE>null</CODE> to 
   * regenerate all <CODE>Stale</CODE> or <CODE>Missing</CODE> files.
   */
  private TreeSet<Integer>  pFileIndices; 
  
  /**
   * The list of all the special queue requirements for the jobs.
   */
  private JobReqsDelta pReqs;
  
  /**
   * For parallel jobs, this overrides the maximum number of frames assigned to each job
   * associated with the root node of the job submission.
   */ 
  protected Integer  pBatchSize;         
}
  
