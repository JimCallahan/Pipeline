// $Id: QueueState.java,v 1.4 2004/06/14 22:39:17 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E    S T A T E                                                                 */
/*------------------------------------------------------------------------------------------*/

/**
 * The status of individual files associated with a node with respect to the queue jobs
 * which generate them. 
 * 
 * The <CODE>QueueState</CODE> is computed within the context of a previously determined
 * {@link FileState FileState} for the file.  The following state descriptions will 
 * frequently refer to this <CODE>FileState</CODE> context. 
 * 
 * @see OverallQueueState
 */
public
enum QueueState
{  
  /**
   * The file associated with the working node exists and is up-to-date. <P>
   * 
   * If there is a regeneration action associated with the node owning this file, the last 
   * queue job executing this action has completed successfully.  This state means that none 
   * of the conditions for any other <CODE>QueueState</CODE> have been met for the file. <P> 
   * 
   * If the node owning this file does not have a regeneration action, then this is the 
   * only possible <CODE>QueueState</CODE> for the file.  Any other <CODE>QueueState</CODE>
   * therefore implies that the owning node has a regeneration action.
   */
  Finished,

  /**
   * The file associated with the working node does not exist. <P> 
   * 
   * This is the only possible state if the <CODE>FileState</CODE> is 
   * {@link FileState#Missing Missing}.
   */
  Missing,

  /**
   * The <CODE>FileState</CODE> is not {@link FileState#Missing Missing}, but changes to 
   * the upstream files or nodes upon which this file depends mean that this file needs to 
   * be regenerated to reflect the changes. <P> 
   * 
   * This state can be caused by changes to upstream node links or node properties of the 
   * working version of the node owning this file since the time that the file was 
   * regenerated.  This state can also be due to any of upstream files upon which this file 
   * depends having a <CODE>QueueState</CODE> other than <CODE>Finished</CODE> or being 
   * newer than this file.
   */
  Stale,

  /**
   * A queue job has been submitted which will regenerate this file, but has not been 
   * yet been run. <P> 
   * 
   * This state has precedence over the <CODE>Missing</CODE> and <CODE>Stale</CODE> states.
   */
  Queued,

  /**
   * A queue job is currently running which will regenerate this file. <P> 
   * 
   * This state has precedence over the <CODE>Missing</CODE> and <CODE>Stale</CODE> states.
   */
  Running,

  /**
   * The last queue job submitted which would have regenerated this file failed to 
   * execute successfully. <P> 
   * 
   * This state has precedence over the <CODE>Missing</CODE> and <CODE>Stale</CODE> 
   * states. <P> 
   * 
   * Note that when a new job is queued it clears the <CODE>Failed</CODE> state of any 
   * previous jobs for the same file.  Therefore a state of <CODE>Failed</CODE> means that 
   * no job has been resubmitted for the file since the time of the last job failure.
   */
  Failed;



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return toString();
  }

}
