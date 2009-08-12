// $Id: TemplateBuildInfo.java,v 1.6 2009/08/12 20:33:05 jesse Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

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
   *   The map of product nodes. A list of all the nodes that the nodes being built depend 
   *   on. These will either be locked or checked-out frozen depending on how the template is 
   *   designed. If this value is <code>null</code> or an empty set, then the Template Builder 
   *   will generate the list itself.  The map value is whether the product is considered 
   *   optional.
   * 
   * @param productContexts
   *   The context names associated with each product node.  A mapping of each product to the 
   *   set of contexts that it is identified as being part of on nodes in the template. The 
   *   first key should be the name of the product node. The second key should be the name of 
   *   the node in the template that contained the TemplateContextLink Annotations that 
   *   identified the contexts. The value is the set of context names. If this variable is 
   *   <code>null</code> then the Template Builder will generated the list itself.
   *   
   * @param optionalBranches
   *   An optional branch is a node which may or may not be built when the template is 
   *   instantiated.  The keys in this data structure will be matched to the keys passed
   *   into the TemplateBuilder as part of the externals data structure.  If the template
   *   has a <code>false</code> value for a key, then any nodes in this data structure and any
   *   nodes which are upstream of it and not used anywhere else in the template, will not
   *   be built.  The Option Type parameter determines the exact behavior of the template.  
   *   If it is set to BuildOnly, then when the option is disabled, the network is not built 
   *   and the rest of the template will ignore the node. If Option Type is set to As Product, 
   *   then the node will be ignored in terms of construction by the template but will be used 
   *   as a product node if a version exists in the repository.  If no version exists in the 
   *   repository, then the behavior will be identical to how BuildOnly works.If the template 
   *   generates its own lists, it will rebuild this data structure using the 
   *   TemplateOptionalBranch Annotations.  
   */
  public TemplateBuildInfo
  (
    TreeSet<String> nodesToBuild,
    MappedSet<String, String> nodesDependingOnMe,
    MappedSet<String, String> nodesIDependedOn,
    TreeMap<String, Boolean> productNodes,
    DoubleMap<String, String, TreeSet<String>> productContexts,
    DoubleMap<String, String, OptionalBranchType> optionalBranches
  )
  {
    setNodesToBuild(nodesToBuild);
    setNodesDependingOnMe(nodesDependingOnMe);
    setNodesIDependedOn(nodesIDependedOn);
    setProductNodes(productNodes);
    setProductContexts(productContexts);
    setOptionalBranches(optionalBranches);
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
   * Get the list of product nodes and whether they are optional.
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
  
  /**
   * Get the map of optional branches indexed by the name of the optional branch with a 
   * value representing the branch type.
   * <p>
   * An optional branch is a node which may or may not be built when the template is 
   * instantiated.  The keys in this data structure will be matched to the keys passed
   * into the TemplateBuilder as part of the externals data structure.  If the template
   * has a <code>false</code> value for a key, then any nodes in this data structure and any
   * nodes which are upstream of it and not used anywhere else in the template, will not
   * be built.  
   * <p>
   * The Option Type parameter determines the exact behavior of the template.  If it is set to 
   * BuildOnly, then when the option is disabled, the network is not built and the rest of the 
   * template will ignore the node. If Option Type is set to As Product, then the node 
   * will be ignored in terms of construction by the template but will be used as a product 
   * node if a version exists in the repository.  If no version exists in the repository, 
   * then the behavior will be identical to how BuildOnly works.
   * <p>
   * If the template generates its own lists, it will rebuild this data structure
   * using the TemplateOptionalBranch Annotations.  
   */
  public final DoubleMap<String, String, OptionalBranchType>
  getOptionalBranches()
  {
    return pOptionalBranches;
  }

  /**
   * Set the map of optional branches indexed by the name of the optional branch with a 
   * value representing the branch type.
   * <p>
   * An optional branch is a node which may or may not be built when the template is 
   * instantiated.  The keys in this data structure will be matched to the keys passed
   * into the TemplateBuilder as part of the externals data structure.  If the template
   * has a <code>false</code> value for a key, then any nodes in this data structure and any
   * nodes which are upstream of it and not used anywhere else in the template, will not
   * be built.  
   * <p>
   * The Option Type parameter determines the exact behavior of the template.  If it is set to 
   * BuildOnly, then when the option is disabled, the network is not built and the rest of the 
   * template will ignore the node. If Option Type is set to As Product, then the node 
   * will be ignored in terms of construction by the template but will be used as a product 
   * node if a version exists in the repository.  If no version exists in the repository, 
   * then the behavior will be identical to how BuildOnly works.
   * <p>
   * If the template generates its own lists, it will rebuild this data structure
   * using the TemplateOptionalBranch Annotations.  
   */
  public final void
  setOptionalBranches
  (
    DoubleMap<String, String, OptionalBranchType> optionalBranches
  )
  {
    if (optionalBranches != null)
      pOptionalBranches = 
        new DoubleMap<String, String, OptionalBranchType>(optionalBranches);
    else
      pOptionalBranches = new DoubleMap<String, String, OptionalBranchType>();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private TreeSet<String> pNodesToBuild;
  
  private MappedSet<String, String> pNodesIDependedOn;
  
  private MappedSet<String, String> pNodesDependingOnMe;

  private TreeMap<String, Boolean> pProductNodes;

  private DoubleMap<String, String, TreeSet<String>> pProductContexts;
  
  private DoubleMap<String, String, OptionalBranchType> pOptionalBranches;
}
