// $Id: NodeStatus.java,v 1.9 2004/04/17 19:49:01 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S U M M A R Y                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The status of a node with respect to a particular user and view.
 * 
 * @see NodeDetails
 */
public
class NodeStatus
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given name.
   * 
   * @param id 
   *   The unique working version identifier.
   */
  public 
  NodeStatus
  (
   NodeID id
  ) 
  {
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pSources = new TreeMap<String,NodeStatus>();
    pTargets = new TreeMap<String,NodeStatus>();
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
   * Gets the fully resolved node name.
   */ 
  public String
  getName() 
  {
    return pNodeID.getName();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the detailed status information for this node.
   * 
   * @return 
   *   The node details or <CODE>null</CODE> if this node is downstream of the focus node.
   */ 
  public NodeDetails
  getDetails()
  {
    return pDetails;
  }

  /**
   * Set the detailed status information for this node.
   * 
   * @param details 
   *   The status details.
   */
  public void 
  setDetails
  (
   NodeDetails details
  ) 
  {
    if(!details.getName().equals(pNodeID.getName())) 
      throw new IllegalArgumentException
	("The node details name (" + details.getName() + ") didn't match the " + 
	 "node ID name (" + pNodeID.getName() + ")!");      
    pDetails = details;
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
   *   The node status or <CODE>null</CODE> if no upstream node exits with the given name.
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
   * This method is used to initialize instances of this class and should not
   * be called directly by the user.
   * 
   * @param status  
   *   The status of the upstream node.
   */ 
  public void 
  addSource
  (
   NodeStatus status
  ) 
  {
    pSources.put(status.getName(), status);
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
   *   The node status or <CODE>null</CODE> if no downstream node exits with the given name.
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
   * This method is used to initialize instances of this class and should not
   * be called directly by the user.
   * 
   * @param status  
   *   The status of the downstream node.
   */ 
  public void 
  addTarget
  (
   NodeStatus status
  ) 
  {
    pTargets.put(status.getName(), status);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7006898232931436818L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;


  /**
   * The detailed status information for this node. <P> 
   * 
   * May be <CODE>null</CODE> if this node is downstream of the focus node.
   */
  private NodeDetails  pDetails;
  

  /** 
   * The upstream nodes connected to this node.
   */
  private TreeMap<String,NodeStatus>  pSources;

  /** 
   * The downstream nodes connected to this node.
   */
  private TreeMap<String,NodeStatus>  pTargets;
  
}

