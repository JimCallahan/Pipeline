// $Id: UpdateEvent.java,v 1.1 2004/10/18 04:00:00 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   U P D A T E   E V E N T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * This event notifies the Supervisor that a significant step has been accomplished toward 
 * the completion of the {@link Task Task} by its Owner. <P> 
 * 
 * This event may only be generated when the current {@link CompletionState CompletionState}
 * of the task is {@link CompletionState#Unfinished Unfinished} or 
 * {@link CompletionState#CouldBeBetter CouldBeBetter}.  As part of the event, the Owner 
 * of the task may change the CompletionState to 
 * {@link CompletionState#PendingReview PendingReview} to signal that they cannot proceed 
 * until the task has been reviewed by the Supervisor.  <P> 
 * 	
 * A message must be supplied by the Owner which explains the work which has been 
 * accomplished.  When a node currently associated with a task is checked-in, an Update 
 * event will be automatically generated which uses the check-in log message as the 
 * explanitory message for the event. <P> 
 * 
 * The task may also modified by adding, removing and/or changing the revision numbers 
 * of the checked-in node vesions associated with the task.  When a node previously 
 * associated with the task is checked-in the revision number is updated automatically.
 * The node versions associated with a task can also be manually changed. <P> 
 */
public
class UpdateEvent
  extends BaseEvent
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  UpdateEvent()
  {}

  /**
   * Construct a new event.
   * 
   * @param nodes
   *   The current revision numbers of the nodes currently associated with the task indexed 
   *   by fully resolved node name or <CODE>null</CODE> to leave unchanged.
   * 
   * @param review
   *   Whether to change the completion state to 
   *   {@link CompletionState#PendingReview PendingReview}.
   * 
   * @param msg 
   *   The update message text.
   */ 
  public 
  UpdateEvent
  (
   TreeMap<String,VersionID> nodes, 
   boolean review, 
   String msg
  )
  {
    super(msg);

    if(msg == null) 
      throw new IllegalArgumentException
	("The message cannot be (null)!");

    if(nodes != null) 
      pNodes = nodes;

    if(review) 
      pCompletionState = CompletionState.PendingReview;
  } 

    

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the new revision numbers of the nodes currently associated with the task indexed by 
   * fully resolved node name.
   * 
   * @return
   *   The node revisions or <CODE>null</CODE> if the nodes are unchanged.
   */ 
  public TreeMap<String,VersionID>
  getNodes() 
  {
    return pNodes;
  }

  /**
   * Get the new completion state of the task.
   * 
   * @return
   *   The completion state or <CODE>null</CODE> if the state is unchanged.
   */ 
  public CompletionState
  getCompletionState() 
  {
    return pCompletionState;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the this event is valid given the current status of the task.
   * 
   * @param status
   *   The current task status.
   * 
   * @param privileged
   *   The names of the privileged users.
   */ 
  public boolean
  isValid
  (
   TaskStatus status, 
   TreeSet<String> privileged
  )
  {
    if(!getAuthor().equals(status.getOwner()) && 
       !getAuthor().equals(status.getSupervisor()) && 
       !privileged.contains(getAuthor()))
      return false;
    
    switch(status.getLatestCompletionState()) {
    case Unfinished:
    case CouldBeBetter:
      break;

    default:
      return false;
    }

    return true;
  }

  /**
   * Verify that kind of event is valid given the current status of the task.
   * 
   * @param status
   *   The current task status.
   * 
   * @param privileged
   *   The names of the privileged users.
   * 
   * @throws PipelineException
   *   If the event is not valid with a message explaining why.
   */ 
  public void
  validate
  (
   TaskStatus status, 
   TreeSet<String> privileged
  )
    throws PipelineException
  {
    if(!getAuthor().equals(status.getOwner()) && 
       !getAuthor().equals(status.getSupervisor()) && 
       !privileged.contains(getAuthor()))
      throw new PipelineException 
	("The user (" + getAuthor() + ") is not authorized to edit the properties of " + 
	 "task (" + status + ")!");

    switch(status.getLatestCompletionState()) {
    case Unfinished:
    case CouldBeBetter:
      break;

    default:
      throw new PipelineException
	("The task (" + status + ") cannot be updated while the completion state is " + 
	 "(" + status.getLatestCompletionState().toTitle() + ")!");
    }
  }
  


  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    if(pNodes != null)
      encoder.encode("Nodes", pNodes);

    if(pCompletionState != null) 
      encoder.encode("CompletionState", pCompletionState);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,VersionID> nodes = (TreeMap<String,VersionID>) decoder.decode("Nodes");
    if(nodes != null) 
      pNodes = nodes;

    CompletionState state = (CompletionState) decoder.decode("CompletionState");
    if(state != null) 
      pCompletionState = state;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5113358025106349737L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The new revision numbers of the nodes currently associated with the task indexed by 
   * fully resolved node name.
   */ 
  private TreeMap<String,VersionID>  pNodes;

  /**
   * The new completion state of the task.
   */ 
  private CompletionState  pCompletionState;

}
