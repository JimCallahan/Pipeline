// $Id: NodeStatus.java,v 1.5 2004/03/23 07:40:37 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.core.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T A T U S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The high-level status of a Pipeline node.
 */
public
class NodeStatus
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a node status with the given overall node and queue states.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param nodeState 
   *   The overall revision control state of the node.
   * 
   * @param queueState 
   *   The overall state of queue jobs associated with the node.
   * 
   */
  protected 
  NodeStatus
  (
   String name, 
   OverallNodeState nodeState, 
   OverallQueueState queueState
  ) 
  {
    super(name);

    pTimeStamp = new Date();

    pOverallNodeState  = nodeState;
    pOverallQueueState = queueState;
  }

  /**
   * Construct a node status with undefined overall node and queue states.
   * 
   * @param name 
   *   The fully resolved node name.
   */
  protected 
  NodeStatus
  (
   String name
  ) 
  {
    super(name);

    pTimeStamp = new Date();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get when the node status was determined.
   */ 
  public Date
  getTimeStamp() 
  {
    assert(pTimeStamp != null);
    return (Date) pTimeStamp.clone();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the overall revision control state of the node.
   * 
   * @return
   *   The node status or <CODE>null</CODE> if the node status is undefined.
   */
  public OverallNodeState
  getOverallNodeState() 
  {
    return pOverallNodeState;
  }
  
  /**
   * Get the overall state of queue jobs associated with the node.
   * 
   * @return
   *   The queue status or <CODE>null</CODE> if the queue status is undefined.
   */
  public OverallQueueState
  getOverallQueueState() 
  {
    return pOverallQueueState;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the fully resolved names names of the upstream nodes.
   */ 
  public Set<String>
  getSourceNames() 
  {
    return Collections.unmodifiableSet(pSources.keySet());
  }

  /** 
   * Get the node status of the given upstream node.
   * 
   * @param name  
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The node state or <CODE>null</CODE> if no upstream node exits with the given name.
   */ 
  public NodeStatus
  getSource
  ( 
   String name
  ) 
  {
    return pSources.get(name);
  }
  
  /** 
   * Get the node status of all of the upstream nodes.
   */ 
  public Collection<NodeStatus>
  getSources()
  {
    return Collections.unmodifiableCollection(pSources.values());
  }
  
  /** 
   * Add a node status to the set of upstream nodes. <P> 
   * 
   * This method is used internally by the {@link NodeMgr#status NodeMgr.status} method 
   * and should not be called directly by the user.
   * 
   * @param state  
   *   The state of the upstream node.
   */ 
  public void 
  addSource
  (
   NodeStatus state
  ) 
  {
    pSources.put(state.getName(), state);
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the fully resolved names names of the downstream nodes.
   */ 
  public Set<String>
  getTargetNames() 
  {
    return Collections.unmodifiableSet(pTargets.keySet());
  }

  /** 
   * Get the node status of the given downstream node.
   * 
   * @param name  
   *   The fully resolved node name of the downstream node.
   * 
   * @return 
   *   The node state or <CODE>null</CODE> if no downstream node exits with the given name.
   */ 
  public NodeStatus 
  getTarget
  ( 
   String name
  ) 
  {
    return pTargets.get(name);
  }
  
  /** 
   * Get the node status of all of the downstream nodes.
   */ 
  public Collection<NodeStatus>
  getTargets()
  {
    return Collections.unmodifiableCollection(pTargets.values());
  }

  /** 
   * Add a node status to the set of downstream nodes. <P> 
   * 
   * This method is used internally by the {@link NodeMgr#status NodeMgr.status} method 
   * and should not be called directly by the user.
   * 
   * @param state  
   *   The state of the downstream node.
   */ 
  public void 
  addTarget
  (
   NodeStatus state
  ) 
  {
    pTargets.put(state.getName(), state);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5489205652602559487L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * When the message node status was determined.
   */ 
  private Date  pTimeStamp; 


  /** 
   * The overall revision control state of the node.
   */
  private OverallNodeState pOverallNodeState;
  
  /** 
   * The overall file and job queue state of the node.
   */
  private OverallQueueState pOverallQueueState;

  
  /** 
   * The states of the upstream nodes connected to this node.
   */
  private TreeMap<String,NodeStatus>  pSources;

  /** 
   * The states of the downstream nodes connected to this node.
   */
  private TreeMap<String,NodeStatus>  pTargets;
  
}

