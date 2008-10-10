// $Id: NodeStatus.java,v 1.23 2008/10/10 12:46:58 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T A T U S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The status of a node with respect to a particular user and view.
 */
public
class NodeStatus
  implements Glueable, Serializable
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

  /**
   * Copy an existing node status. <P> 
   * 
   * @param status
   *   The original node status to copy.
   * 
   * @param ignoreTargets
   *   Whether to ignore downstream targets when copying.
   */
  public 
  NodeStatus
  (
   NodeStatus status,
   boolean ignoreTargets
  ) 
  {
    pNodeID = status.pNodeID;

    pDetailsCheckedIn = status.pDetailsCheckedIn;
    pDetailsLight     = status.pDetailsLight;
    pDetailsHeavy     = status.pDetailsHeavy;

    pSources = new TreeMap<String,NodeStatus>(status.pSources);
    if(!ignoreTargets) 
      pTargets = new TreeMap<String,NodeStatus>(status.pTargets);

    pStaleLinks = new TreeSet<String>(status.pStaleLinks);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether only downstream node status information exists for the node.
   */ 
  public boolean 
  hasDownstreamOnly() 
  {
    return ((pDetailsCheckedIn == null) && 
            (pDetailsLight == null) && 
            (pDetailsHeavy == null));
  }

  /**
   * Whether detailed information is available about a specific checked-in version of 
   * the node. 
   */
  public boolean 
  hasCheckedInDetails() 
  {
    return (pDetailsCheckedIn != null);
  }

  /**
   * Whether a lightweight collection of node state information is available with respect 
   * to a particular working area view. 
   */
  public boolean 
  hasLightDetails() 
  {
    return (pDetailsLight != null);
  }
   
  /**
   * Whether a heavyweight collection of node state information is available with respect 
   * to a particular working area view. 
   */
  public boolean 
  hasHeavyDetails() 
  {
    return (pDetailsHeavy != null);
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
   * Get the detailed information available about a specific checked-in version of the node. 
   * 
   * @return 
   *   The checked-in node details or <CODE>null</CODE> if not available.
   */ 
  public NodeDetailsCheckedIn
  getCheckedInDetails()
  {
    return pDetailsCheckedIn;
  }

  /**
   * Get the lightweight collection of node state information available with respect 
   * to a particular working area view. 
   * 
   * @return 
   *   The lightweight node details or <CODE>null</CODE> if not available.
   */ 
  public NodeDetailsLight
  getLightDetails()
  {
    return pDetailsLight;
  }

  /**
   * Get the heavyweight collection of node state information available with respect 
   * to a particular working area view. 
   * 
   * @return 
   *   The heavyweight node details or <CODE>null</CODE> if not available.
   */ 
  public NodeDetailsHeavy
  getHeavyDetails()
  {
    return pDetailsHeavy;
  }

 
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the checked-in node details.
   * 
   * @param details 
   *   The node details.
   */
  public void 
  setCheckedInDetails
  (
   NodeDetailsCheckedIn details
  ) 
  {
    pDetailsCheckedIn = details;
    pDetailsLight     = null;
    pDetailsHeavy     = null;
  }

  /**
   * Set the lightweight node details.
   * 
   * @param details 
   *   The node details.
   */
  public void 
  setLightDetails
  (
   NodeDetailsLight details
  ) 
  {
    pDetailsCheckedIn = null;
    pDetailsLight     = details;
    pDetailsHeavy     = null;
  }

  /**
   * Set the heavyweight node details.
   * 
   * @param details 
   *   The node details.
   */
  public void 
  setHeavyDetails
  (
   NodeDetailsHeavy details
  ) 
  {
    pDetailsCheckedIn = null;
    pDetailsLight     = details;
    pDetailsHeavy     = details;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table of node annotation plugin instances indexed by annotation name. 
   * 
   * @return
   *   The annotations (may be empty) or <CODE>null</CODE> if annotations were ignored
   *   during the status operation.
   */ 
  public TreeMap<String,BaseAnnotation>
  getAnnotations()
  {
    return pAnnotations;
  }

  /**
   * Set the table of node annotation plugin instances. 
   * 
   * @param annotations
   *   The table of node annotation plugin instances indexed by annotation name. 
   */ 
  public void 
  setAnnotations
  (
   TreeMap<String,BaseAnnotation> annotations
  )
  {
    pAnnotations = annotations;
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

  /** 
   * Remove all upstream nodes. <P> 
   * 
   * This method is used to initialize instances of this class and should not
   * be called directly by the user.
   */ 
  public void 
  clearSources() 
  {
    pSources.clear(); 
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

  /** 
   * Remove all downstream nodes. <P> 
   * 
   * This method is used to initialize instances of this class and should not
   * be called directly by the user.
   */ 
  public void 
  clearTargets() 
  {
    pTargets.clear(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Find the status of the given if it is upstream.
   * 
   * @param name
   *   The fully resolved name of the node.
   */ 
  public NodeStatus
  findUpstreamNamed
  (
   String name
  ) 
  {
    if(getName().equals(name)) 
      return this;

    for(NodeStatus status : pSources.values()) {
      NodeStatus found = status.findUpstreamNamed(name);
      if(found != null)
        return found;
    }

    return null;
  }

  /**
   * Find the status of the given if it is downstream.
   * 
   * @param name
   *   The fully resolved name of the node.
   */ 
  public NodeStatus
  findDownstreamNamed
  (
   String name
  ) 
  {
    if(getName().equals(name)) 
      return this;

    for(NodeStatus status : pSources.values()) {
      NodeStatus found = status.findDownstreamNamed(name);
      if(found != null)
        return found;
    }

    return null;
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
    return (findUpstreamNamed(name) != null);
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
    return (findDownstreamNamed(name) != null);
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
    if(hasHeavyDetails()) 
      return pDetailsHeavy.toString();
    else if(hasLightDetails())  
      return pDetailsLight.toString();
    else if(hasCheckedInDetails())
      return pDetailsCheckedIn.toString();
    else {
      Path path = new Path(pNodeID.getName());
      return path.getName();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the node to convert a dynamically loaded action plugin instance into a 
   * generic staticly loaded BaseAction instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    out.writeObject(pNodeID);
    out.writeObject(pSources);
    out.writeObject(pTargets);

    TreeMap<String,BaseAnnotation> annots = null;
    if(pAnnotations != null) {
      annots = new TreeMap<String,BaseAnnotation>(); 
      
      for(String name : pAnnotations.keySet()) {
        BaseAnnotation annot = pAnnotations.get(name);
        if(annot != null) 
          annots.put(name, new BaseAnnotation(annot));
      }
    }
    out.writeObject(annots);

    out.writeObject(pDetailsCheckedIn);
    out.writeObject(pDetailsLight);
    out.writeObject(pDetailsHeavy);

    out.writeObject(pStaleLinks);
  }


  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance and copy
   * its parameters from the generic staticly loaded BaseAction instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
     pNodeID  = (NodeID) in.readObject();
     pSources = (TreeMap<String,NodeStatus>) in.readObject();
     pTargets = (TreeMap<String,NodeStatus>) in.readObject();

     TreeMap<String,BaseAnnotation> annots = (TreeMap<String,BaseAnnotation>) in.readObject();
     if(annots != null) {
       pAnnotations = new TreeMap<String,BaseAnnotation>(); 

       for(String name : annots.keySet()) {
         BaseAnnotation annot = annots.get(name);
         if(annot != null) {
           try {
             PluginMgrClient client = PluginMgrClient.getInstance();
             BaseAnnotation nannot = client.newAnnotation(annot.getName(), 
                                                          annot.getVersionID(), 
                                                          annot.getVendor());
             nannot.setParamValues(annot);
             pAnnotations.put(name, nannot);
           }
           catch(PipelineException ex) {
             throw new IOException(ex.getMessage());
           }
         }
       }
     }

     pDetailsCheckedIn = (NodeDetailsCheckedIn) in.readObject();
     pDetailsLight = (NodeDetailsLight) in.readObject();
     pDetailsHeavy = (NodeDetailsHeavy) in.readObject();

     pStaleLinks = (TreeSet<String>) in.readObject();
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
    encoder.encode("NodeID", pNodeID);

    if(!pSources.isEmpty()) 
      encoder.encode("Sources", pSources);

    if(!pTargets.isEmpty()) 
      encoder.encode("Targets", pTargets);

    if((pAnnotations != null) && !pAnnotations.isEmpty()) 
      encoder.encode("Annotations", pAnnotations);       

    if(pDetailsCheckedIn != null) 
      encoder.encode("NodeDetailsHeavy", pDetailsHeavy);
    else if(pDetailsLight != null) 
      encoder.encode("NodeDetailsLight", pDetailsLight);
    else if(pDetailsCheckedIn != null) 
      encoder.encode("NodeDetailsCheckedIn", pDetailsCheckedIn);

    if(!pStaleLinks.isEmpty()) 
      encoder.encode("StaleLinks", pStaleLinks);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    throw new GlueException("NodeStatus does not support GLUE decoding!");
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
   * The upstream nodes connected to this node.
   */
  private TreeMap<String,NodeStatus>  pSources;

  /** 
   * The downstream nodes connected to this node.
   */
  private TreeMap<String,NodeStatus>  pTargets;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of node annotation plugin instances indexed by annotation name. 
   */ 
  private TreeMap<String,BaseAnnotation>  pAnnotations; 

  /**
   * The detailed status information for this node. <P>
   * 
   * There are four different potential types of status operations: downstream, checked-in, 
   * lighweight and heavyweight.  For downstream nodes, all of the following fields will 
   * be <CODE>null</CODE>.  For checked-in and lighweight status, only the respective detail
   * fields will be non-<CODE>null</CODE>.  For heavyweight status, both the light and heavy 
   * fields will be set using the same node details since heavyweight status is a superset of 
   * lightweight status. <P> 
   * 
   * Consumers of node detail information can then choose to lookup the appropriate type of 
   * details which suit their needs without respect to the implementation of these details.
   */
  private NodeDetailsCheckedIn  pDetailsCheckedIn;
  private NodeDetailsLight      pDetailsLight;
  private NodeDetailsHeavy      pDetailsHeavy;
  
  /**
   * The names of the upstream nodes connected to this node which propagate staleness
   * for a heavyweight node status. <P> 
   * 
   * Note that this does not mean that all of these nodes are Stale themselves, only that
   * they have timestamps which are newer than this node and which will be propogated 
   * downstream and may eventually cause staleness.
   */ 
  private TreeSet<String>  pStaleLinks; 

}

