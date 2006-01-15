// $Id: NodeResubmitJobsReq.java,v 1.3 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S U B M I T   J O B S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to reesubmit the group of jobs needed to regenerate the selected 
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
   * @param priority 
   *   Overrides the priority of jobs associated with the root node of the job submission 
   *   relative to other jobs.  
   * 
   * @param rampUp
   *   Overrides the ramp-up interval (in milliseconds) for the job.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   */
  public
  NodeResubmitJobsReq
  (
   NodeID id, 
   TreeSet<FileSeq> targetSeqs, 
   Integer batchSize, 
   Integer priority, 
   Integer rampUp, 
   Set<String> selectionKeys  
  )
    throws PipelineException
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pTargetSeqs    = targetSeqs;
    pBatchSize     = batchSize;
    pPriority      = priority;
    pRampUp        = rampUp; 
    pSelectionKeys = selectionKeys;
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
    return pPriority;
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
    return pRampUp; 
  }

  /**
   * Overrides the set of selection keys an eligable host is required to have for jobs 
   * associated with the root node of the job submission.
   * 
   * @return 
   *   The selection keys or <CODE>null</CODE> to use node's original selection keys.
   */
  public Set<String>
  getSelectionKeys()
  {
    return pSelectionKeys;
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
   * For parallel jobs, this overrides the maximum number of frames assigned to each job
   * associated with the root node of the job submission.
   */ 
  protected Integer  pBatchSize;         

  /**
   * Overrides the priority of jobs associated with the root node of the job submission 
   * relative to other jobs.  
   */
  private Integer  pPriority;
  
  /** 
   * Overrides the ramp-up interval of jobs associated with the root node of the job 
   * submission.
   */
  private Integer  pRampUp; 

  /**
   * Overrides the set of selection keys an eligable host is required to have for jobs 
   * associated with the root node of the job submission.
   */
  private Set<String>  pSelectionKeys;

}
  
