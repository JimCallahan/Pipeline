package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseBuilder.*;
import us.temerity.pipeline.stages.StageState;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   I N F O R M A T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

public 
class BuilderInformation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  BuilderInformation
  (
    boolean usingGui,
    boolean abortOnBadParam,
    MultiMap<String, String> commandLineParams
  )
  {
    pUsingGUI = usingGui;
    pAbortOnBadParam = abortOnBadParam;
    pCommandLineParams = commandLineParams;
    pAllConstructPasses = new LinkedList<ConstructPass>();
    pPassToBuilderMap = new ListMap<ConstructPass, BaseBuilder>();
    pNodesToQueue = new TreeSet<String>();
    pCheckInOrder = new LinkedList<BaseBuilder>();
    pCallHierarchy = new LinkedList<BaseBuilder>();
    pStageState = new StageState();
    pPassDependencies = new ListMap<ConstructPass, PassDependency>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   Q U E U E   L I S T                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds a node to the list of things to be queued at the end of the builder run.
   */
  public final void 
  addToQueueList
  (
    String nodeName
  )
  {
    pNodesToQueue.add(nodeName);
  }

  /**
   * Removes a node from the list of things to be queued at the end of the builder run.
   */
  public final void 
  removeFromQueueList
  (
    String nodeName
  )
  {
    pNodesToQueue.remove(nodeName);
  }
  
  /**
   * Clears the list of things to be queued at the end of the builder run.
   */
  public final void 
  clearQueueList()
  {
    pNodesToQueue.clear();
  }

  /**
   * Returns the list of things to be queued at the end of the builder run.
   */
  public final TreeSet<String> 
  getQueueList()
  {
    return new TreeSet<String>(pNodesToQueue);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final boolean
  addConstuctPass
  (
    ConstructPass pass,
    BaseBuilder builder
  )
  {
    if (pPassToBuilderMap.containsKey(pass))
      return false;
    pPassToBuilderMap.put(pass, builder);
    pAllConstructPasses.add(pass);
    return true;
  }
  
  /**
   * Gets the Builder that is associated with a given ConstructPass.
   */
  public final BaseBuilder
  getBuilderFromPass
  (
    ConstructPass pass
  ) 
  {
    return pPassToBuilderMap.get(pass);
  }
  
  public final LinkedList<ConstructPass>
  getAllConstructPasses()
  {
    return new LinkedList<ConstructPass>(pAllConstructPasses);
  }
  
  /**
   * Creates a dependency between two ConstructPasses.
   * <p>
   * The target ConstructPass will not be run until the source ConstructPass has completed.
   * This allows for Builders to specify the order in which their passes run.
   */
  protected final void
  addPassDependency
  (
    ConstructPass sourcePass,
    ConstructPass targetPass
  )
  {
    if (pPassDependencies.containsKey(targetPass)) {
      PassDependency pd = pPassDependencies.get(targetPass);
      pd.addSource(sourcePass);
    }
    else {
      PassDependency pd = 
	new PassDependency(targetPass, ComplexParam.listFromObject(sourcePass));
      pPassDependencies.put(targetPass, pd);
    }
  }
  
  protected final Map<ConstructPass, PassDependency>
  getPassDependencies()
  {
    return Collections.unmodifiableMap(pPassDependencies);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N   L I S T                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  public void
  addToCheckinList
  (
    BaseBuilder builder
  )
  {
    pCheckInOrder.add(builder);
  }
  
  public List<BaseBuilder>
  getCheckinList()
  {
    return Collections.unmodifiableList(pCheckInOrder);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M M A N D   L I N E   P A R A M S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns a {@link MultiMap} of all the command line parameters.
   * <p>
   * The first level in the MultiMap is made up of the names of all the builders, the second
   * level is all the parameter names, and every level after that (if they exist) are keys
   * into Complex Parameters.  Values are stored in the leaf nodes.
   */
  public final MultiMap<String, String>
  getCommandLineParams()
  {
    return pCommandLineParams;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  C A L L   H I E R A R C H Y                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  public int
  getCallHierarchySize()
  {
    return pCallHierarchy.size();
  }
  
  public BaseBuilder
  pollCallHierarchy()
  {
    return pCallHierarchy.poll();
  }
  
  public void
  addToCallHierarchy
  (
    BaseBuilder builder
  )
  {
    pCallHierarchy.addFirst(builder);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  public final boolean
  usingGui()
  {
    return pUsingGUI;
  }
  
  public final boolean
  abortOnBadParam()
  {
    return pAbortOnBadParam;
  }
  
  public final StageInformation
  getNewStageInformation()
  {
    return new StageInformation(pStageState);
  }
  
  public final StageState
  getStageState()
  {
    return pStageState;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A list of all the ConstructPasses 
   */
  private LinkedList<ConstructPass> pAllConstructPasses; 
  
  /**
   * A mapping of each ConstructPass to the BaseBuilder that created it.
   */
  private ListMap<ConstructPass, BaseBuilder> pPassToBuilderMap; 

  /**
   * A list of nodes names that need to be queued.
   */
  private TreeSet<String> pNodesToQueue;
  
  private MultiMap<String, String> pCommandLineParams; 
  
  /**
   * Is this Builder in GUI mode.
   */
  private boolean pUsingGUI = false;
  
  private boolean pAbortOnBadParam = false;
  
  private LinkedList<BaseBuilder> pCheckInOrder;
  
  private LinkedList<BaseBuilder> pCallHierarchy;
  
  private StageState pStageState;
  
  private ListMap<ConstructPass, PassDependency> pPassDependencies; 

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L   C L A S S E S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A G E   I N F O R M A T I O N                                                    */
  /*----------------------------------------------------------------------------------------*/

  public 
  class StageInformation
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/
    
    private 
    StageInformation
    (
      StageState state  
    )
    {
      if (state == null)
        throw new IllegalArgumentException
          ("Cannot pass a null StageStage into StageInformation");
      pStageState = state;
      pDefaultSelectionKeys = new TreeSet<String>();
      pDefaultLicenseKeys = new TreeSet<String>();
      pSelectionKeyStack = new LinkedList<TreeSet<String>>();
      pLicenseKeyStack = new LinkedList<TreeSet<String>>();
      pDoAnnotations = false;
      pActionOnExistence = ActionOnExistence.Continue;
    }

    
    
    /*--------------------------------------------------------------------------------------*/
    /*   A C C E S S                                                                        */
    /*--------------------------------------------------------------------------------------*/
    
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
    addDefaultSelectionKey
    (
      String key  
    )
    {
      pDefaultSelectionKeys.add(key);
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
    
    public void
    addDefaultLicenseKey
    (
      String key  
    )
    {
      pDefaultLicenseKeys.add(key);
    }
    
    public TreeSet<String> 
    getDefaultSelectionKeys()
    {
      TreeSet<String> toReturn = new TreeSet<String>();
      if (pDefaultSelectionKeys != null)
	toReturn.addAll(pDefaultSelectionKeys);
      for (TreeSet<String> keys : pSelectionKeyStack ) 
	if (keys != null)
	  toReturn.addAll(keys);
      
      return toReturn;
    }
    
    public TreeSet<String> 
    getDefaultLicenseKeys()
    {
      TreeSet<String> toReturn = new TreeSet<String>();
      if (pDefaultLicenseKeys != null)
	toReturn.addAll(pDefaultLicenseKeys);
      for (TreeSet<String> keys : pLicenseKeyStack) 
	if (keys != null)
	  toReturn.addAll(keys);
      
      return toReturn;
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
    
    public void
    pushSelectionKeys
    (
      TreeSet<String> keys  
    )
    {
      pSelectionKeyStack.addFirst(keys);
    }
    
    public void
    popSelectionKeys()
    {
      pSelectionKeyStack.poll();
    }
    
    public void
    pushLicenseKeys
    (
      TreeSet<String> keys  
    )
    {
      pLicenseKeyStack.addFirst(keys);
    }
    
    public void
    popLicenseKeys()
    {
      pLicenseKeyStack.poll();
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
    
    
    public void
    setActionOnExistence
    (
      ActionOnExistence aoe  
    )
    {
      pActionOnExistence = aoe;
    }
    
    public ActionOnExistence
    getActionOnExistence()
    {
      return pActionOnExistence;
    }
    
    

    /*--------------------------------------------------------------------------------------*/
    /*   S T A G E   S T A T E   W R A P P E R                                              */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Clears all added nodes that are currently being kept track of.
     */
    public void 
    initializeAddedNodes()
    {
      pStageState.initializeAddedNodes();
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
      return pStageState.getAddedNodes();
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
      return pStageState.getAddedNodesUserMap();
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
      return pStageState.getAddedNodesViewMap();
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
      return pStageState.addNode(name, author, view);
    }
    
    public PluginContext 
    getDefaultEditor
    (
      String function  
    )
    {
      return pStageState.getDefaultEditor(function);
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
      pStageState.setDefaultEditor(function, plugin);
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
      return pStageState.getStageFunctionSelectionKeys(function);
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
      pStageState.setStageFunctionSelectionKeys(function, keys);
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
      return pStageState.getStageFunctionLicenseKeys(function);
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
      pStageState.setStageFunctionLicenseKeys(function, keys);
    }
    
    
    
    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/
    
    private TreeSet<String> pDefaultSelectionKeys = new TreeSet<String>();
    
    private TreeSet<String> pDefaultLicenseKeys = new TreeSet<String>();
    
    private LinkedList<TreeSet<String>> pSelectionKeyStack;
    
    private LinkedList<TreeSet<String>> pLicenseKeyStack;
    
    private boolean pUseDefaultSelectionKeys;
    
    private boolean pUseDefaultLicenseKeys;
    
    private boolean pDoAnnotations;
    
    private ActionOnExistence pActionOnExistence;
  }
}
