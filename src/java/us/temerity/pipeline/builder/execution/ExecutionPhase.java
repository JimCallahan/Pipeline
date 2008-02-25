// $Id: ExecutionPhase.java,v 1.1 2008/02/25 05:03:05 jesse Exp $

package us.temerity.pipeline.builder.execution;

/**
 * What phase of execution the Builder is currently in.
 * <p>
 * This is passed into the handleException method that each executor provides, allowing it
 * to provide context appropriate handling of errors. 
 *
 */
enum ExecutionPhase
{
  /**
   * Phase that the executor starts in and exits once it starts running ConstructPasses.
   */
  SetupPass, 
  
  /**
   * Phase the executor is in when it is running Construct Passes.
   */
  ConstructPass, 
  
  /**
   * Once construction has finished and nodes are being queued.
   */
  Queue, 
  
  /**
   * Once all the nodes have been successfully queued and the executor is attempting to check
   * nodes in.
   */
  Checkin, 
  
  /**
   * Once everything is completely done without error.
   */
  Finished, 
  
  /**
   * After an error has occurred.
   */
  Error,
  
  /**
   * When the executor has encountered an error and has gone ahead to release the nodes
   * that it has made.
   */
  Release;
  
  /**
   * Is the builder in one of its ending phases.
   * @return <code>true</code> if the Builder is in the Checkin, Error, or Release phase.
   */
  public boolean
  isEndingPhase()
  {
    switch(this ) {
    case Checkin:
    case Error:
    case Release:
      return true;
    default:
      return false;
    }
  }
  
  /**
   * Is the builder in one of its phases where nodes have been made but an error or a check-in
   * has no occured.
   * @return <code>true</code> if the Builder is in the ConstructPass or Queue phase.
   */
  public boolean
  haveNodesBeenMade()
  {
    switch(this ) {
    case ConstructPass:
    case Queue:
      return true;
    default:
      return false;
    }
  }
}
