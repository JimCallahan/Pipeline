// $Id: NodeSummary.java,v 1.1 2004/04/15 00:10:27 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S U M M A R Y                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A compact summary of the state of node with respect to a particular user and view.
 */
public
class NodeSummary
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct from component state information. <P> 
   * 
   * The <CODE>workingID</CODE> argument may be null if either there is no working version
   * of the node or this is an initial working version.
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @param workingID
   *   The revision number of the checked-in version upon which the working version 
   *   is based.
   * 
   * @param overallNodeState
   *   The overall revision control state of the node.
   * 
   * @param overallQueueState
   *   The overall state of queue jobs associated with the node.
   */
  public 
  NodeSummary
  (
   String name, 
   VersionID workingID, 
   OverallNodeState overallNodeState, 
   OverallQueueState overallQueueState
  ) 
  {
    super(name);

    pTimeStamp = new Date();
    
    pWorkingID         = workingID;
    pOverallNodeState  = overallNodeState;
    pOverallQueueState = overallQueueState;
  }

  /**
   * Construct from node details.
   * 
   * @param details
   *   The source of detailed node state information.
   */
  public 
  NodeSummary
  (
   NodeDetails details
  ) 
  {
    super(details.getName());

    pTimeStamp = details.getTimeStamp();

    if(details.getWorkingVersion() != null)
      pWorkingID = details.getWorkingVersion().getWorkingID();

    pOverallNodeState  = details.getOverallNodeState();
    pOverallQueueState = details.getOverallQueueState();
  }

  /**
   * Construct an undefined state.
   * 
   * @param name 
   *   The fully resolved node name.
   */
  public 
  NodeSummary
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
   * Get when the node state was determined.
   */ 
  public Date
  getTimeStamp() 
  {
    assert(pTimeStamp != null);
    return (Date) pTimeStamp.clone();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the checked-in version upon which the working version is based.
   * 
   * @return 
   *   The revision number or <CODE>null</CODE>.
   */ 
  public VersionID
  getWorkingID()
  {
    return pWorkingID;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the overall revision control state of the node.
   * 
   * @return
   *   The node state or <CODE>null</CODE> if the node state is undefined.
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
   *   The queue state or <CODE>null</CODE> if the queue state is undefined.
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
   * Get the node state of the given upstream node.
   * 
   * @param name  
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The node state or <CODE>null</CODE> if no upstream node exits with the given name.
   */ 
  public NodeSummary
  getSource
  ( 
   String name
  ) 
  {
    return pSources.get(name);
  }
  
  /** 
   * Get the node state of all of the upstream nodes.
   */ 
  public Collection<NodeSummary>
  getSources()
  {
    return Collections.unmodifiableCollection(pSources.values());
  }
  
  /** 
   * Add a node state to the set of upstream nodes. <P> 
   * 
   * This method is used to initialize instances of this class and should not
   * be called directly by the user.
   * 
   * @param state  
   *   The state of the upstream node.
   */ 
  public void 
  addSource
  (
   NodeSummary state
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
   * Get the node state of the given downstream node.
   * 
   * @param name  
   *   The fully resolved node name of the downstream node.
   * 
   * @return 
   *   The node state or <CODE>null</CODE> if no downstream node exits with the given name.
   */ 
  public NodeSummary 
  getTarget
  ( 
   String name
  ) 
  {
    return pTargets.get(name);
  }
  
  /** 
   * Get the node state of all of the downstream nodes.
   */ 
  public Collection<NodeSummary>
  getTargets()
  {
    return Collections.unmodifiableCollection(pTargets.values());
  }

  /** 
   * Add a node state to the set of downstream nodes. <P> 
   * 
   * This method is used to initialize instances of this class and should not
   * be called directly by the user.
   * 
   * @param state  
   *   The state of the downstream node.
   */ 
  public void 
  addTarget
  (
   NodeSummary state
  ) 
  {
    pTargets.put(state.getName(), state);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 5489205652602559487L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * When the message node state was determined.
   */ 
  private Date  pTimeStamp; 


  /**
   * The revision number of the checked-in version upon which the working version 
   * is based.
   */ 
  private VersionID  pWorkingID;


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
  private TreeMap<String,NodeSummary>  pSources;

  /** 
   * The states of the downstream nodes connected to this node.
   */
  private TreeMap<String,NodeSummary>  pTargets;
  
}

