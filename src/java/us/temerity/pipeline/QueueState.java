// $Id: QueueState.java,v 1.12 2005/12/31 20:42:58 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E    S T A T E                                                                 */
/*------------------------------------------------------------------------------------------*/

/**
 * The status of individual files associated with a node with respect to the queue jobs
 * which generate them. <P> 
 * 
 * If the node has secondary file sequences, the <CODE>QueueState</CODE> is shared for the 
 * corresponding files in the primary and secondary file sequences.  This is due to the fact
 * that secondary files are generated by the same queue job which generates the primary file
 * and therefore cannot have independent <CODE>QueueState</CODE> values. <P> 
 * 
 * The <CODE>QueueState</CODE> is computed within the context of a previously determined
 * {@link FileState FileState} for the primary/secondary files.  The following state 
 * descriptions will frequently refer to this <CODE>FileState</CODE> context. <P> 
 * 
 * Except for the <CODE>Undefined</CODE> and <CODE>Stale</CODE> states, there is a direct
 * correspondence between the <CODE>QueueState</CODE> of a file and the 
 * {@link JobState JobState} of the queue job which generates the file.
 * 
 * @see JobState
 * @see OverallQueueState
 */
public
enum QueueState
{  
  /**
   * No working version exists, therefore the state is undefined. <P> 
   * 
   * This is the only possible state when the <CODE>VersionState</CODE> is 
   * {@link VersionState#CheckedIn CheckedIn}.
   */ 
  Undefined, 

  /**
   * The <CODE>FileState</CODE> of one or more of the primary/secondary files is 
   * {@link FileState#Missing Missing} or requires regeneration. <P> 
   * 
   * Files require regeneration when the working version of the node has been modified since 
   * the time the primary/secondary files where created.  Files may also become 
   * <CODE>Stale</CODE> if one or more of the upstream files upon which they depend have a 
   * <CODE>QueueState</CODE> other than <CODE>Finished</CODE>, have a <CODE>FileState</CODE>
   * of <CODE>Missing</CODE> or are newer.
   */
  Stale,

  /**
   * A queue job has been submitted which will regenerate the primary/secondary files, but 
   * has not started running.  Alternatively, a job has been preempted causing it to be 
   * automatically resubmitted. <P> 
   * 
   * This state has precedence over the <CODE>Stale</CODE> state.
   */
  Queued,

  /**
   * The queue job has been submitted which will regenerate the primary/secondary files, but 
   * was paused by the user before if began execution.  <P> 
   * 
   * This state has precedence over the <CODE>Stale</CODE> state.
   */
  Paused,

  /**
   * The last queue job submitted which would have regenerated these primary/secondary files 
   * was aborted (cancelled) by the user before it began execution. <P> 
   * 
   * This state has precedence over the <CODE>Stale</CODE> state. <P> 
   * 
   * Note that when a new job is queued it clears the <CODE>Failed</CODE> or 
   * <CODE>Aborted</CODE> state of any previous jobs for the same file.  Therefore a state 
   * of <CODE>Aborted</CODE> means that no job has been resubmitted for the file since the 
   * time of the job that was aborted.
   */
  Aborted,

  /**
   * A queue job is currently running which will regenerate these primary/secondary files.<P> 
   * 
   * This state has precedence over the <CODE>Stale</CODE> state.
   */
  Running,

  /**
   * The file associated with the working node exists and is up-to-date. <P>
   * 
   * If there is a regeneration action associated with the node owning this file, the last 
   * queue job executing this action has completed successfully.  This state means that none 
   * of the conditions for any other <CODE>QueueState</CODE> have been met for the file. <P> 
   * 
   * If the node owning this file does not have a regeneration action, then this is the 
   * only possible <CODE>QueueState</CODE> for the file.  Any other <CODE>QueueState</CODE>
   * therefore implies that the working version of the owning node has a regeneration action.
   */
  Finished,

  /**
   * The last queue job submitted which would have regenerated these primary/secondary files 
   * failed to execute successfully. <P> 
   * 
   * This state has precedence over the <CODE>Stale</CODE> state. <P> 
   * 
   * Note that when a new job is queued it clears the <CODE>Failed</CODE> or 
   * <CODE>Aborted</CODE> state of any previous jobs for the same file.  Therefore a state 
   * of <CODE>Failed</CODE> means that no job has been resubmitted for the file since the 
   * time of the job that failed.
   */
  Failed;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<QueueState>
  all() 
  {
    QueueState values[] = values();
    ArrayList<QueueState> all = new ArrayList<QueueState>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  

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
