// $Id: NodeStatus.java,v 1.18 2006/07/10 10:55:55 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T A T U S                                                                  */
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

    pStaleLinks = new TreeSet<String>();
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
   * Are there any nodes upstream of this node?
   */ 
  public boolean
  hasSources()
  {
    return (!pSources.isEmpty());
  }

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
   * Are there any nodes downstream of this node?
   */ 
  public boolean
  hasTargets()
  {
    return (!pTargets.isEmpty());
  }

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

  /**
   * Whether the given node is one of the upstream nodes.
   * 
   * @param name
   *   The fully resolved name of the node.
   */ 
  public boolean
  hasUpstreamNamed
  (
   String name
  ) 
  {
    if(getName().equals(name)) 
      return true;

    for(NodeStatus status : pSources.values()) {
      if(status.hasUpstreamNamed(name)) 
	return true;
    }

    return false;
  }

  /**
   * Whether the given node is one of the downstream nodes.
   *
   * @param name
   *   The fully resolved name of the node.
   */ 
  public boolean
  hasDownstreamNamed
  (
   String name
  ) 
  {
    if(getName().equals(name)) 
      return true;

    for(NodeStatus status : pTargets.values()) {
      if(status.hasDownstreamNamed(name)) 
	return true;
    }

    return false;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Is staleness being propogated from any upstream node?
   */ 
  public boolean
  hasStaleLinks()
  {
    return (!pStaleLinks.isEmpty());
  }

  /**
   * Is staleness being propogated from any upstream node?
   * 
   * @param name  
   *   The fully resolved name of the upstream node.
   */ 
  public boolean
  isStaleLink
  (
   String name
  )    
  {
    return pStaleLinks.contains(name);
  }

  /** 
   * Get the fully resolved names names of upstream nodes which propagate staleness. <P> 
   */ 
  public Set<String>
  getStaleLinks() 
  {
    return Collections.unmodifiableSet(pStaleLinks);
  }

  /** 
   * Mark the given node as source of propagated staleness.
   * 
   * This method is used to initialize instances of this class and should not
   * be called directly by the user.
   * 
   * @param name  
   *   The fully resolved name of the upstream node.
   */ 
  public void 
  addStaleLink
  (
   String name
  ) 
  {
    pStaleLinks.add(name);
  }

  /** 
   * Unmark the given node as source of propagated staleness.
   * 
   * This method is used to initialize instances of this class and should not
   * be called directly by the user.
   * 
   * @param name  
   *   The fully resolved name of the upstream node.
   */ 
  public void 
  removeStaleLink
  (
   String name
  ) 
  {
    pStaleLinks.remove(name);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string representation of the primary file sequence if details exist or the 
   * short node name if details are missing.
   */ 
  public String
  toString() 
  {
    if(pDetails != null) 
      return pDetails.toString();

    Path path = new Path(pNodeID.getName());
    return path.getName();
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
  

  /**
   * The names of the upstream nodes connected to this node which propagate staleness. <P> 
   * 
   * Note that this does not mean that all of these nodes are Stale themselves, only that
   * they have timestamps which are newer than this node and which will be propogated 
   * downstream and may eventually cause staleness.
   */ 
  private TreeSet<String>  pStaleLinks;
}

