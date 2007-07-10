package us.temerity.pipeline.stages;

import java.util.*;

import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   S T A G E   I N F O R M A T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class StageInformation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public StageInformation()
  {
    pAddedNodes = new TreeSet<String>();
    pAddedNodesUserMap = new TreeMap<String, String>();
    pAddedNodesViewMap = new TreeMap<String, String>();
    pDefaultSelectionKeys = new TreeSet<String>();
    pDefaultLicenseKeys = new TreeSet<String>();
    pDoAnnotations = false;
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

  public void
  setDefaultSelectionKeys
  (
    Set<String> keys
  )
  {
    if (keys == null)
      pDefaultSelectionKeys = new TreeSet<String>();
    else
      pDefaultSelectionKeys = new TreeSet<String>(keys);
  }
  
  public void
  setDefaultLicenseKeys
  (
    Set<String> keys
  )
  {
    if (keys == null)
      pDefaultLicenseKeys = new TreeSet<String>();
    else
      pDefaultLicenseKeys = new TreeSet<String>(keys);
  }
  
  public TreeSet<String> 
  getDefaultSelectionKeys()
  {
    return pDefaultSelectionKeys;
  }
  
  public TreeSet<String> 
  getDefaultLicenseKeys()
  {
    return pDefaultLicenseKeys;
  }
  
  public void 
  setUseDefaultSelectionKeys
  (
    boolean value
  )
  {
    pUseDefaultSelectionKeys = value;
  }
  
  public boolean 
  useDefaultSelectionKeys()
  {
    return pUseDefaultSelectionKeys; 
  }

  public void 
  setUseDefaultLicenseKeys
  (
    boolean value
  )
  {
    pUseDefaultLicenseKeys = value;
  }
  
  public boolean 
  useDefaultLicenseKeys()
  {
    return pUseDefaultLicenseKeys; 
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
  protected final boolean 
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
  
  public boolean 
  doAnnotations()
  {
    return pDoAnnotations;
  }

  
  public void 
  setDoAnnotations
  (
    boolean doAnnotations
  )
  {
    pDoAnnotations = doAnnotations;
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
  
  private TreeSet<String> pDefaultSelectionKeys = new TreeSet<String>();
  
  private TreeSet<String> pDefaultLicenseKeys = new TreeSet<String>();
  
  private boolean pUseDefaultSelectionKeys;
  
  private boolean pUseDefaultLicenseKeys;
  
  private boolean pDoAnnotations;

}
