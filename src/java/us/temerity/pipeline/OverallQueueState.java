// $Id: OverallQueueState.java,v 1.9 2004/07/22 00:07:36 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   O V E R A L L   Q U E U E   S T A T E                                                  */
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
   * No working version exists, therefore the state is undefined. <P> 
   * 
   * This is the only possible state when the <CODE>VersionState</CODE> is 
   * {@link VersionState#CheckedIn CheckedIn}.
   */ 
  Undefined, 

  /**
   * All of files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Finished Finished}.
   */
  Finished,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Stale Stale}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE> states.
   */
  Stale,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Queued Queued}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE> and <CODE>Stale</CODE> states.
   */
  Queued,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Running Running}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE>, <CODE>Stale</CODE> and 
   * <CODE>Queued</CODE> states.
   */
  Running,

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Aborted Aborted}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE>, <CODE>Stale</CODE>, 
   * <CODE>Queued</CODE> and <CODE>Running</CODE> states.
   */
  Aborted, 

  /**
   * One or more of the files associated with the node have a <CODE>QueueState<CODE> of 
   * {@link QueueState#Failed Failed}. <P> 
   * 
   * This state has precedence over the <CODE>Finished</CODE>, <CODE>Stale</CODE>, 
   * <CODE>Queued</CODE>, <CODE>Running</CODE> and <CODE>Aborted</CODE> states.
   */
  Failed;




  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<OverallQueueState>
  all() 
  {
    OverallQueueState values[] = values();
    ArrayList<OverallQueueState> all = new ArrayList<OverallQueueState>(values.length);
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
