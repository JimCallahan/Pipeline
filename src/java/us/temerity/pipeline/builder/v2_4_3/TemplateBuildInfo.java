// $Id: TemplateBuildInfo.java,v 1.3 2009/03/26 00:04:16 jesse Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   B U I L D   I N F O                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Contains the basic information about what is being made that can be passed into the
 * Template Builder 
 */
public 
class TemplateBuildInfo
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create an empty info.
   */
  public
  TemplateBuildInfo()
  {}


  /**
   * Create an info.
   * 
   * @param nodesToBuild
   *   The list of all the nodes in the template.  This should only contain the nodes that 
   *   are actually going to be built, not any nodes which are products.
   *    
   * @param nodesDependingOnMe
   *   The list of nodes that depend on the given node. For each node in the nodesToBuild 
   *   list, the set of all the nodes that are directly downstream in the template. If this 
   *   value is <code>null</code> or empty, the Template Builder will generate it.
   *   
   * @param nodesIDependedOn
   *   The list of nodes that each node depends on.  For each node in the nodesToBuild 
   *   list, the set of all the nodes that are directly upstream in the template. If this 
   *   value is <code>null</code> or empty, the Template Builder will generate it.
   * 
   * @param productNodes
   *   The list of product nodes. A list of all the nodes that the nodes being built depend 
   *   on. These will either be locked or checked-out frozen depending on how the template is 
   *   designed. If this value is <code>null</code> or an empty set, then the Template Builder 
   *   will generate the list itself.
   * 
   * @param productContexts
   *   The context names associated with each product node.  A mapping of each product to the 
   *   set of contexts that it is identified as being part of on nodes in the template. The 
   *   first key should be the name of the product node. The second key should be the name of 
   *   the node in the template that contained the TemplateContextLink Annotations that 
   *   identified the contexts. The value is the set of context names. If this variable is 
   *   <code>null</code> then the Template Builder will generated the list itself.
   */
  public TemplateBuildInfo
  (
    TreeSet<String> nodesToBuild,
    MappedSet<String, String> nodesDependingOnMe,
    MappedSet<String, String> nodesIDependedOn,
    TreeMap<String, Boolean> productNodes,
    DoubleMap<String, String, TreeSet<String>> productContexts
  )
  {
    setNodesToBuild(nodesToBuild);
    setNodesDependingOnMe(nodesDependingOnMe);
    setNodesIDependedOn(nodesIDependedOn);
    setProductNodes(productNodes);
    setProductContexts(productContexts);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  



  /**
   * Get the list of all the nodes in the template.
   * <p>
   * This should only contain the nodes that are actually going to be built, not any nodes
   * which are products.
   */
  public final TreeSet<String> 
  getNodesToBuild()
  {
    return pNodesToBuild;
  }

  /**
   * Set the list of all the nodes in the template.
   * <p>
   * This should only contain the nodes that are actually going to be built, not any nodes
   * which are products. If this value is <code>null</code> or empty, the Template Builder
   * will generate it.
   */
  public final void 
  setNodesToBuild
  (
    TreeSet<String> nodesToBuild
  )
  {
    if (nodesToBuild != null)
      pNodesToBuild = new TreeSet<String>(nodesToBuild);
    else 
      pNodesToBuild = null;
  }

  /**
   * Get the list of nodes that each node depends on.
   * <p>
   * For each node in the nodesToBuild list, the set of all the nodes that are directly
   * upstream in the template. If this value is <code>null</code> or empty, the Template
   * Builder will generate it.
   */
  public final MappedSet<String, String> 
  getNodesIDependedOn()
  {
    return pNodesIDependedOn;
  }

  /**
   * Set the list of nodes that each node depends on.
   * <p>
   * For each node in the nodesToBuild list, the set of all the nodes that are directly
   * upstream in the template. If this value is <code>null</code> or empty, the Template
   * Builder will generate it.
   */  public final void 
   setNodesIDependedOn
   (
    MappedSet<String, String> nodesIDependedOn
  )
  {
     if (nodesIDependedOn != null)
       pNodesIDependedOn = new MappedSet<String, String>(nodesIDependedOn);
     else
       pNodesIDependedOn = null;
  }

  /**
   * Get the list of nodes that depend on the given node.
   * <p>
   * For each node in the nodesToBuild list, the set of all the nodes that are directly
   * downstream in the template. If this value is <code>null</code> or empty, the Template
   * Builder will generate it.
   */
  public final MappedSet<String, String> 
  getNodesDependingOnMe()
  {
    return pNodesDependingOnMe;
  }

  
  /**
   * Set the list of nodes that depend on the given node.
   * <p>
   * For each node in the nodesToBuild list, the set of all the nodes that are directly
   * downstream in the template. If this value is <code>null</code> or empty, the Template
   * Builder will generate it.
   */
  public final void 
  setNodesDependingOnMe
  (
    MappedSet<String, String> nodesDependingOnMe
  )
  {
    if (nodesDependingOnMe != null)
      pNodesDependingOnMe = new MappedSet<String, String>(nodesDependingOnMe);
    else
      pNodesDependingOnMe = null;
  }

  /**
   * Get the list of product nodes.
   * <p>
   * A list of all the nodes that the nodes being built depend on. These will either be locked
   * or checked-out frozen depending on how the template is designed. If this value is
   * <code>null</code> or an empty set, then the Template Builder will generate the list
   * itself.
   */
  public final TreeMap<String, Boolean> 
  getProductNodes()
  {
    return pProductNodes;
  }

  /**
   * Set the list of product nodes and whether they are optional.
   * <p>
   * A list of all the nodes that the nodes being built depend on. These will either be locked
   * or checked-out frozen depending on how the template is designed. If this value is
   * <code>null</code> or an empty set, then the Template Builder will generate the list
   * itself.
   */
  public final void 
  setProductNodes
  (
    TreeMap<String, Boolean> productNodes
  )
  {
    if (productNodes != null)
      pProductNodes = new TreeMap<String, Boolean>(productNodes);
    else 
      pProductNodes = null;
  }

  /**
   * Get the context names associated with each product node.
   * <p>
   * A mapping of each product to the set of contexts that it is identified as being part of
   * on nodes in the template. The first key should be the name of the product node. The
   * second key should be the name of the node in the template that contained the
   * TemplateContextLink Annotations that identified the contexts. The value is the set of
   * context names. If this value is <code>null</code> then the Template Builder will
   * generated the list itself.
   */
  public final DoubleMap<String, String, TreeSet<String>> 
  getProductContexts()
  {
    return pProductContexts;
  }

  /**
   * Set the context names associated with each product node.
   * <p>
   * A mapping of each product to the set of contexts that it is identified as being part of
   * on nodes in the template. The first key should be the name of the product node. The
   * second key should be the name of the node in the template that contained the
   * TemplateContextLink Annotations that identified the contexts. The value is the set of
   * context names. If this value is <code>null</code> then the Template Builder will
   * generated the list itself.
   */  
  public final void 
  setProductContexts
  (
    DoubleMap<String, String, TreeSet<String>> productMaps
  )
  {
    if (productMaps != null)
      pProductContexts = new DoubleMap<String, String, TreeSet<String>>(productMaps);
    else
      pProductContexts = null;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private TreeSet<String> pNodesToBuild;
  
  private MappedSet<String, String> pNodesIDependedOn;
  
  private MappedSet<String, String> pNodesDependingOnMe;

  private TreeMap<String, Boolean> pProductNodes;

  private DoubleMap<String, String, TreeSet<String>> pProductContexts;
}
