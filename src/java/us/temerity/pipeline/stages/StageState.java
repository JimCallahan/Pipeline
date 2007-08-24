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
    pAddedNodes = new TreeSet<String>();
    pAddedNodesUserMap = new TreeMap<String, String>();
    pAddedNodesViewMap = new TreeMap<String, String>();
    pDefaultEditors = new TreeMap<String, PluginContext>();
    pDefaultLicenseKeys = new MappedArrayList<String, String>();
    pDefaultSelectionKeys = new MappedArrayList<String, String>();
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Clears all added nodes that are currently being kept track of.
   */
  public void 
  initializeAddedNodes()
  {
    pAddedNodes = new TreeSet<String>();
    pAddedNodesUserMap = new TreeMap<String, String>();
    pAddedNodesViewMap = new TreeMap<String, String>();
  }
  
  /**
   * Gets a list that contains the names of all the nodes that have been built by stages.
   * 
   * @return The {@link TreeSet} containing the node names.
   * @see #getAddedNodesUserMap()
   * @see #getAddedNodesViewMap()
   */
  public TreeSet<String> 
  getAddedNodes()
  {
    return pAddedNodes;
  }

  /**
   * Gets a mapping of each added node to the user in whose working area the node was
   * added.
   * 
   * @return The {@link TreeMap} containing the user names.
   * @see #getAddedNodes()
   * @see #getAddedNodesViewMap()
   */
  public TreeMap<String, String> 
  getAddedNodesUserMap()
  {
    return pAddedNodesUserMap;
  }

  /**
   * Gets a mapping of each added node to the working area where the node was added.
   * 
   * @return The {@link TreeMap} containing the working area names.
   * @see #getAddedNodes()
   * @see #getAddedNodesUserMap()
   */
  public TreeMap<String, String> 
  getAddedNodesViewMap()
  {
    return pAddedNodesViewMap;
  }
  
  /**
   * Adds a node name to the list of nodes created duing the session.
   * <P>
   * The method will return a boolean based on whether the node already existed in the
   * current list. A return value of <code>false</code> indicates that the name was not
   * added to the list since it already existed then. A return value of <code>true</code>
   * indicates that the add was succesful. A PipelineException is thrown if the
   * <code>initializeAddedNodes</code> method was not called before calling this method.
   * 
   * @param name
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
    if(pAddedNodes.contains(name))
      return false;
    pAddedNodes.add(name);
    pAddedNodesUserMap.put(name, author);
    pAddedNodesViewMap.put(name, view);
    return true;
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
  public ArrayList<String>
  getDefaultSelectionKeys
  (
    String function  
  )
  {
    ArrayList<String> toReturn = pDefaultSelectionKeys.get(function);
    if (toReturn == null)
      toReturn = new ArrayList<String>();
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
  setDefaultSelectionKeys
  (
    String function,
    ArrayList<String> keys
  )
  {
    if (!pDefaultSelectionKeys.containsKey(function)) {
      pDefaultSelectionKeys.put(function, keys);
    }
  }
  
  public ArrayList<String>
  getDefaultLicenseKeys
  (
    String function  
  )
  {
    ArrayList<String> toReturn = pDefaultLicenseKeys.get(function);
    if (toReturn == null)
      toReturn = new ArrayList<String>();
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
  setDefaultLicenseKeys
  (
    String function,
    ArrayList<String> keys
  )
  {
    if (!pDefaultLicenseKeys.containsKey(function)) {
      pDefaultLicenseKeys.put(function, keys);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A list containing all the nodes that have been added by stages. All stages are
   * responsible for ensuring that all created nodes end up in this data structure.
   */
  private TreeSet<String> pAddedNodes;

  /**
   * A mapping of added nodes to the user who added them.
   */
  private TreeMap<String, String> pAddedNodesUserMap;

  /**
   * A mapping of added nodes to the working area they were added in.
   */
  private TreeMap<String, String> pAddedNodesViewMap;
  
  private TreeMap<String, PluginContext> pDefaultEditors;
  private MappedArrayList<String, String> pDefaultSelectionKeys;
  private MappedArrayList<String, String> pDefaultLicenseKeys;
}
