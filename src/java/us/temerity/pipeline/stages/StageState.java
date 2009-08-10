// $Id: StageState.java,v 1.12 2009/08/10 20:50:13 jesse Exp $

package us.temerity.pipeline.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;

/*------------------------------------------------------------------------------------------*/
/*   S T A G E   S T A T E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Information that is shared between all stages in all builders.
 * <p>
 * This class was designed to get around the restrictions on static data in builders. Instead
 * of having a single shared data-store for this information, each invocation of a builder
 * contains an instance of StageState contained inside its {@link BuilderInformation}. As the
 * BuilderInformation is passed around among the various sub-builders, the StageState goes
 * along with it.
 * <p>
 * There is no direct access to this class in any user builder code. Instead, access is
 * mediated through the use of the {@link StageInformation} class contained inside the
 * {@link BuilderInformation} class. This safeguard is in place to prevent inadvertent use of
 * an alternative StageState. It also allows for single calls to StageInformation to update
 * information that is localized the particular builder and global data stored in the
 * StageState.
 */
public 
class StageState
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Default constructor.
   */
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
    pLockedNodes = new TreeSet<String>();
    
    pAOEModes = new TreeMap<String, ActionOnExistence>();
    pAOEOverrides = new DoubleMap<String, String, ActionOnExistence>();
    
    for (String mode : sDefaultModes) {
      pAOEModes.put(mode, ActionOnExistence.valueFromString(mode));
    }
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
   * Gets a list of all the nodes that have been locked using the {@link BaseStage#lock()}
   * method.
   */
  public TreeSet<String>
  getLockedNodes()
  {
    return new TreeSet<String>(pLockedNodes);
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
    if(pAddedNodes.keySet().contains(name))
      return false;
    pAddedNodes.put(name, new NodeID(author, view, name));
    return true;
  }
  
  /**
   * Add a node to the list of things that have been checked out by a stage.
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
   * Add a node to the list of things that have been locked by a stage.
   */
  public final void
  addLockedNode
  (
    String name  
  )
  {
    pLockedNodes.add(name);
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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  D E F A U L T   E D I T O R S                                                         */
  /*----------------------------------------------------------------------------------------*/
  
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
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  Q U E U E   K E Y S                                                                   */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets a default selection keys for a particular stage function type.
   * <p>
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type.  This allows high-level builders to override their child builders if
   * they do not agree on what the default keys should be.  It is important to remember
   * this when writing a builder with sub-builders.  A Builder should always set the
   * default keys in its Stage State class before instantiating any of its 
   * sub-builders.  Failure to do so may result in the default keys values being
   * set by the sub-builder.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
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
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
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
   * this when writing a builder with sub-builders.  A Builder should always set the
   * default keys in its Stage State class before instantiating any of its 
   * sub-builders.  Failure to do so may result in the default keys values being
   * set by the sub-builder.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
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
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
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
   * this when writing a builder with sub-builders.  A Builder should always set the
   * default keys in its Stage State class before instantiating any of its 
   * sub-builders.  Failure to do so may result in the default keys values being
   * set by the sub-builder.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
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
  /*  A C T I O N   O N   E X I S T E N C E                                                 */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a new AoE mode with a default response.
   * <p>
   * The first builder to add an AoE mode wins.  This means that a parent builder can set a
   * default AoE for a mode that will override any defaults that it sub-builders might attempt
   * to set for those modes.
   *
   * @param mode
   *   The name of the AoE mode to add.  This cannot be any of the 4 AOE names and should
   *   never be <code>null</code>
   *   
   * @param aoe
   *   The default AoE for the mode.  This should never be <code>null</code>
   * 
   * @return 
   *   Whether the AoE mode was added.  This will return false is another builder has already
   *   set an AoE default for this mode.
   *   
   * @throws PipelineException
   *   If an attempt is made to override one of the four built-in AOE modes: 
   *   Abort, Continue, Conform, or CheckOut
   */
  public boolean
  addAOEMode
  (
    String mode,
    ActionOnExistence aoe
  ) 
    throws PipelineException
  {
    if (sDefaultModes.contains(mode) || mode.equals(ActionOnExistence.Lock.toString()))
      throw new PipelineException
        ("The mode (" + mode + ") is a default mode and cannot be set by a builder");
    if (pAOEModes.containsKey(mode))
      return false;
    pAOEModes.put(mode, aoe);
    return true;
  }
  
  /**
   * Add a per-node override to the mode's default AoE.
   * <p>
   * The first builder to add an AoE override wins.  This means that a parent builder can set a
   * AoE for a node that will override any defaults that it sub-builders might attempt
   * to set for those nodes.
   * 
   * @param mode
   *   The name of the mode.  A pipeline exception will be thrown is this is not a valid AoE 
   *   mode.
   * 
   * @param nodeName
   *   The name of the node to add the override for.
   * 
   * @param aoe
   *   The {@link ActionOnExistence} that will be applied to the node in the given mode.
   * 
   * @return
   *   Whether the AoE override was added.  This will return false is another builder has 
   *   already set an AoE override for this mode and node.
   *   
   * @throws PipelineException
   *   If an attempt is made to add an override to one of the four default AoE modes:
   *   Abort, Continue, Conform, or CheckOut 
   */
  public boolean
  addAOEOverride
  (
    String mode,
    String nodeName,
    ActionOnExistence aoe
  )
    throws PipelineException
  {
    if (sDefaultModes.contains(mode))
      throw new PipelineException
        ("The mode (" + mode + ") is a default mode and cannot have overrides set on it");
    if (pAOEOverrides.containsKey(mode, nodeName))
      return false;
    pAOEOverrides.put(mode, nodeName, aoe);
    return true;
  }
  
  /**
   * Get the AoE Modes.
   */
  public Set<String>
  getAOEModes()
  {
    return Collections.unmodifiableSet(pAOEModes.keySet());
  }

  /**
   * Get the default AoE for a given mode.
   * 
   * @param mode
   *   The name of the mode.  Cannot be null.

   * @return
   *   The default AoE.
   *   
   * @throws IllegalArgumentException
   *   If a non-existent mode is specified.
   */
  public ActionOnExistence
  getDefaultAOE
  (
    String mode  
  )
  {
    if (!pAOEModes.containsKey(mode))
      throw new IllegalArgumentException("No mode name (" + mode + ") exists.");
    return pAOEModes.get(mode);
  }
  
  /**
   * Get the AoE that should be used for a particular node in the given mode.
   * <p>
   * If there is no node-specific override, then the default AoE for the mode 
   * is returned.
   * 
   * @param mode
   *   The name of the mode.
   *   
   * @param node
   *   The name of the node.
   * 
   * @return
   *   The AoE to be used for the node.
   *   
   * @throws IllegalArgumentException
   *   If a non-existent mode is specified.
   */
  public ActionOnExistence
  getNodeAOE
  (
    String mode,
    String node
  )
  {
    if (!pAOEModes.containsKey(mode))
      throw new IllegalArgumentException("No mode name (" + mode + ") exists.");
    if (pAOEOverrides.containsKey(mode, node))
      return pAOEOverrides.get(mode, node);
    
    return pAOEModes.get(mode);
  }
  
  /**
   * Get the AoE that should be used for a particular node in the given mode.
   * <p>
   * If there is no node-specific override, then <code>null</code> is returned.
   * 
   * @param mode
   *   The name of the mode.
   *   
   * @param node
   *   The name of the node.
   * 
   * @return
   *   The AoE to be used for the node.
   *   
   * @throws IllegalArgumentException
   *   If a non-existent mode is specified.
   */
  public ActionOnExistence
  getBaseNodeAOE
  (
    String mode,
    String node
  )
  {
    if (!pAOEModes.containsKey(mode))
      throw new IllegalArgumentException("No mode name (" + mode + ") exists.");
    if (pAOEOverrides.containsKey(mode, node))
      return pAOEOverrides.get(mode, node);
    
    return null;
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

  /**
   * A list of all the nodes that have been conformed by a stage. 
   * <p>
   * All stages are responsible for ensuring conformed nodes end up here. 
   */
  private TreeSet<String> pConformedNodes;
  
  /**
   * A list of all the nodes that have been checked-out by a stage. 
   * <p>
   * All stages are responsible for ensuring checked-out nodes end up here. 
   */
  private TreeSet<String> pCheckedOutNodes;
  
  /**
   * A list of all the nodes that have been skipped by a stage. 
   * <p>
   * All stages are responsible for ensuring skipped nodes end up here. 
   */
  private TreeSet<String> pSkippedNodes;
  
  /**
   * A list of all the nodes that have been locked by a stage. 
   * <p>
   * All stages are responsible for ensuring locked nodes end up here. 
   */
  private TreeSet<String> pLockedNodes;
  
  private TreeMap<String, PluginContext> pDefaultEditors;
  private MappedSet<String, String> pStageFunctionSelectionKeys;
  private MappedSet<String, String> pStageFunctionLicenseKeys;
  private MappedSet<String, String> pStageFunctionHardwareKeys;
  
  /**
   * A list of AOE modes and their default action. 
   */
  private TreeMap<String, ActionOnExistence> pAOEModes;
  
  /**
   * A list of overrides for AOE modes indexed by AOE Mode and node name.
   */
  private DoubleMap<String, String, ActionOnExistence> pAOEOverrides;
  
  private static TreeSet<String> sDefaultModes;
  static {
    sDefaultModes = new TreeSet<String>();
    Collections.addAll(sDefaultModes, 
      ActionOnExistence.Abort.toTitle(),
      ActionOnExistence.CheckOut.toTitle(),
      ActionOnExistence.Conform.toTitle(),
      ActionOnExistence.Continue.toTitle());
  }
}
