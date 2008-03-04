package us.temerity.pipeline.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;

/*------------------------------------------------------------------------------------------*/
/*   S T A G E   S T A T E                                                                  */
/*------------------------------------------------------------------------------------------*/

public 
class StageState
{
  public
  StageState()
  {
    pAddedNodes = new TreeMap<String, NodeID>();
    pDefaultEditors = new TreeMap<String, PluginContext>();
    pStageFunctionLicenseKeys = new MappedSet<String, String>();
    pStageFunctionSelectionKeys = new MappedSet<String, String>();
    pStageFunctionHardwareKeys = new MappedSet<String, String>();
    
    pConformedNodes = new TreeSet<String>();
    pCheckedOutNodes = new TreeSet<String>();
    pSkippedNodes = new TreeSet<String>();
    
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets a map that contains the NodeIDs of all the nodes that have been built by stages
   * indexed by node name.
   * 
   * @return The map containing the node names.
   */
  public TreeMap<String, NodeID> 
  getAddedNodes()
  {
    return new TreeMap<String, NodeID>(pAddedNodes);
  }
  
  /**
   * Gets a list of all the nodes that have been conformed by a stage.
   */
  public TreeSet<String>
  getConformedNodes()
  {
    return new TreeSet<String>(pConformedNodes);
  }
  
  /**
   * Gets a list of all the nodes that have been checked out using the
   * {@link BaseStage#checkOut(VersionID, CheckOutMode, CheckOutMethod)} method.
   * <p>
   * This does not include nodes that were checked out as part of the neededNode
   * functionality in Builders.
   */
  public TreeSet<String>
  getCheckedOutNodes()
  {
    return new TreeSet<String>(pCheckedOutNodes);
  }
  
  /**
   * Gets a list of all the nodes that have been checked out using the
   * {@link BaseStage#checkOut(VersionID, CheckOutMode, CheckOutMethod)} method.
   * <p>
   * This does not include nodes that were checked out as part of the neededNode
   * functionality in Builders.
   */
  public TreeSet<String>
  getSkippedNodes()
  {
    return new TreeSet<String>(pSkippedNodes);
  }
  
  /**
   * Adds a node name to the list of nodes created during the session.
   * <P>
   * The method will return a boolean based on whether the node already existed in the
   * current list. A return value of <code>false</code> indicates that the name was not
   * added to the list since it already existed then. A return value of <code>true</code>
   * indicates that the add was successful. 
   * 
   * @param name
   *   The name of the node
   * @param author
   *   The user who created the node
   * @param view
   *   The working area the node was created in.
   * @throws PipelineException
   * @see #initializeAddedNodes()
   */
  public final boolean 
  addNode
  (
    String name,
    String author,
    String view
  ) 
    throws PipelineException
  {
    if(pAddedNodes == null)
      throw new PipelineException(
      "It appears that initializeAddedNodes() was never called, leading to an error");
    if(pAddedNodes.keySet().contains(name))
      return false;
    pAddedNodes.put(name, new NodeID(author, view, name));
    return true;
  }
  
  /**
   * Adds a node to the list of things that have been checked out by a stage.
   */
  public final void
  addCheckedOutNode
  (
    String name  
  )
  {
    pCheckedOutNodes.add(name);
  }
  
  /**
   * Adds a node to the list of things that have been checked out by a stage.
   */
  public final void
  addSkippedNode
  (
    String name  
  )
  {
    pSkippedNodes.add(name);
  }
  
  /**
   * Adds a node to the list of things that have been conformed by a stage.
   */
  public final void
  addConformedNode
  (
    String name  
  )
  {
    pConformedNodes.add(name);
  }
  
  /**
   * Gets the default editor for a particular function.
   * 
   * @return The PluginContext or <code>null</code> is there is no default editor
   */
  public PluginContext 
  getDefaultEditor
  (
    String function  
  )
  {
    return pDefaultEditors.get(function);
  }
  
  /**
   * Sets a default editor for a particular stage function type.
   * <p>
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type.  This allows high-level builders to override their child builders if
   * they do not agree on what the default editor should be.  It is important to remember
   * this when writing builders with sub-builder.  A Builder should always set the
   * default editors in its Stage Information class before instantiating any of its 
   * sub-builders.  Failure to do so may result in the default editor values being
   * set by the sub-builder.
   */
  public void
  setDefaultEditor
  (
    String function,
    PluginContext plugin
  )
  {
    if (!pDefaultEditors.containsKey(function)) {
      pDefaultEditors.put(function, plugin);
    }
  }
  
  /**
   * Gets the default selection keys for a particular function.
   * 
   * @return A list of keys or an empty list if no keys exist
   */
  public Set<String>
  getStageFunctionSelectionKeys
  (
    String function  
  )
  {
    Set<String> toReturn = pStageFunctionSelectionKeys.get(function);
    if (toReturn == null)
      toReturn = new TreeSet<String>();
    return toReturn;
  }
  
  /**
   * Sets a default selection keys for a particular stage function type.
   * <p>
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type.  This allows high-level builders to override their child builders if
   * they do not agree on what the default keys should be.  It is important to remember
   * this when writing builders with sub-builder.  A Builder should always set the
   * default keys in its Stage State class before instantiating any of its 
   * sub-builders.  Failure to do so may result in the default keys values being
   * set by the sub-builder.
   */
  public void
  setStageFunctionSelectionKeys
  (
    String function,
    TreeSet<String> keys
  )
  {
    if (!pStageFunctionSelectionKeys.containsKey(function)) {
      pStageFunctionSelectionKeys.put(function, keys);
    }
  }
  
  /**
   * Gets the default license keys for a particular function.
   * 
   * @return A list of keys or an empty list if no keys exist
   */
  public Set<String>
  getStageFunctionLicenseKeys
  (
    String function  
  )
  {
    Set<String> toReturn = pStageFunctionLicenseKeys.get(function);
    if (toReturn == null)
      toReturn = new TreeSet<String>();
    return toReturn;
  }
  
  /**
   * Sets a default license keys for a particular stage function type.
   * <p>
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type.  This allows high-level builders to override their child builders if
   * they do not agree on what the default keys should be.  It is important to remember
   * this when writing builders with sub-builder.  A Builder should always set the
   * default keys in its Stage State class before instantiating any of its 
   * sub-builders.  Failure to do so may result in the default keys values being
   * set by the sub-builder.
   */
  public void
  setStageFunctionLicenseKeys
  (
    String function,
    TreeSet<String> keys
  )
  {
    if (!pStageFunctionLicenseKeys.containsKey(function)) {
      pStageFunctionLicenseKeys.put(function, keys);
    }
  }
  
  /**
   * Gets the default hardware keys for a particular function.
   * 
   * @return A list of keys or an empty list if no keys exist
   */
  public Set<String>
  getStageFunctionHardwareKeys
  (
    String function  
  )
  {
    Set<String> toReturn = pStageFunctionHardwareKeys.get(function);
    if (toReturn == null)
      toReturn = new TreeSet<String>();
    return toReturn;
  }
  
  /**
   * Sets a default hardware keys for a particular stage function type.
   * <p>
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type.  This allows high-level builders to override their child builders if
   * they do not agree on what the default keys should be.  It is important to remember
   * this when writing builders with sub-builder.  A Builder should always set the
   * default keys in its Stage State class before instantiating any of its 
   * sub-builders.  Failure to do so may result in the default keys values being
   * set by the sub-builder.
   */
  public void
  setStageFunctionHardwareKeys
  (
    String function,
    TreeSet<String> keys
  )
  {
    if (!pStageFunctionHardwareKeys.containsKey(function)) {
      pStageFunctionHardwareKeys.put(function, keys);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A list containing the nodeIDs for all the nodes that have been added by stages, indexed
   * by their node name.
   * <p>
   * All stages are responsible for ensuring that all created nodes end up in this data
   * structure.
   */
  private TreeMap<String, NodeID> pAddedNodes;

  private TreeSet<String> pConformedNodes;
  private TreeSet<String> pCheckedOutNodes;
  private TreeSet<String> pSkippedNodes;
  
  private TreeMap<String, PluginContext> pDefaultEditors;
  private MappedSet<String, String> pStageFunctionSelectionKeys;
  private MappedSet<String, String> pStageFunctionLicenseKeys;
  private MappedSet<String, String> pStageFunctionHardwareKeys;
}
