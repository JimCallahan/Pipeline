// $Id: OverallQueueState.java,v 1.2 2004/04/15 00:19:45 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E    S T A T E                                                                 */
/*------------------------------------------------------------------------------------------*/

/**
 * A single state computed from the combination of the individual 
 * {@link QueueState QueueState} and {@link FileState FileState} of each file associated 
 * with the node. <P>
 * 
 * @see QueueState
 * @see FileState
 */
public
enum OverallQueueState
{  
  /**
   * All of files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Finished Finished}.
   */
  Finished,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Missing Missing}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE> state.
   */
  Missing,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Stale Stale}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE> and <CODE>Missing</CODE> states.
   */
  Stale,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Queued Queued}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE>, <CODE>Missing</CODE> and 
   * <CODE>Stale</CODE> states.
   */
  Queued,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Running Running}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE>, <CODE>Missing</CODE>, 
   * <CODE>Stale</CODE> and <CODE>Queued</CODE> states.
   */
  Running,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Failed Failed}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE>, <CODE>Missing</CODE>, 
   * <CODE>Stale</CODE>, <CODE>Queued</CODE> and <CODE>Running</CODE> states.
   */
  Failed;
}
