// $Id: NodeLinks.java,v 1.1 2005/08/21 00:49:46 jim Exp $

package us.temerity.pipeline.core;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   L I N K S                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A simple bidirectional datastructure representing the links between nodes.
 */
public
class NodeLinks
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given name.
   * 
   * @param name
   *   The fully resolved node name. 
   */
  public 
  NodeLinks
  (
   String name
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The name cannot be (null)!");
    pName = name;

    pSources = new TreeMap<String,NodeLinks>();
    pTargets = new TreeMap<String,NodeLinks>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved name of the node.
   */ 
  public String
  getName() 
  {
    return pName;
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
   * Get the node links of the given upstream node.
   * 
   * @param name  
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The node links or <CODE>null</CODE> if no upstream node exits with the given name.
   */ 
  public NodeLinks
  getSource
  ( 
   String name
  ) 
  {
    return pSources.get(name);
  }
  
  /** 
   * Get the node links of all of the upstream nodes.
   */ 
  public Collection<NodeLinks>
  getSources()
  {
    return Collections.unmodifiableCollection(pSources.values());
  }
  
  /** 
   * Add a node links to the set of upstream nodes. <P> 
   * 
   * @param links  
   *   The links of the upstream node.
   */ 
  public void 
  addSource
  (
   NodeLinks links
  ) 
  {
    pSources.put(links.getName(), links);
  }

  /** 
   * Remove a node links from the set of upstream nodes. <P> 
   * 
   * @param links  
   *   The links of the upstream node.
   */ 
  public void 
  removeSource
  (
   NodeLinks links
  ) 
  {
    pSources.remove(links.getName());
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
   * Get the node links of the given downstream node.
   * 
   * @param name  
   *   The fully resolved node name of the downstream node.
   * 
   * @return 
   *   The node links or <CODE>null</CODE> if no downstream node exits with the given name.
   */ 
  public NodeLinks 
  getTarget
  ( 
   String name
  ) 
  {
    return pTargets.get(name);
  }
  
  /** 
   * Get the node links of all of the downstream nodes.
   */ 
  public Collection<NodeLinks>
  getTargets()
  {
    return Collections.unmodifiableCollection(pTargets.values());
  }

  /** 
   * Add a node links to the set of downstream nodes. <P> 
   * 
   * @param links  
   *   The links of the downstream node.
   */ 
  public void 
  addTarget
  (
   NodeLinks links
  ) 
  {
    pTargets.put(links.getName(), links);
  }

  /** 
   * Remove a node links from the set of downstream nodes. <P> 
   * 
   * @param links  
   *   The links of the downstream node.
   */ 
  public void 
  removeTarget
  (
   NodeLinks links   
  ) 
  {
    pTargets.remove(links.getName());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string representation.
   */ 
  public String
  toString() 
  {
    return pName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name.
   */
  private String  pName;        

  /** 
   * The upstream nodes connected to this node.
   */
  private TreeMap<String,NodeLinks>  pSources;

  /** 
   * The downstream nodes connected to this node.
   */
  private TreeMap<String,NodeLinks>  pTargets;
  
}

