// $Id: BuilderInformation.java,v 1.20 2008/02/25 06:19:50 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseBuilder.ConstructPass;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   I N F O R M A T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Contains information that is being shared among all Builders in a given invocation.
 * <p>
 * Only one of these should exist for an given invocation of a Builder. Users should never
 * need to create one of these on their own, unless it is being used in a call to
 * {@link BaseBuilderCollection#instantiateBuilder(String, MasterMgrClient, QueueMgrClient, 
 * BuilderInformation) instantiateBuilder}.  If the same instance of BuilderInformation
 * is not passed to all Sub-Builders, unexpected and wrong behavior may occur.
 */
public 
class BuilderInformation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make a new Builder Information.
   * <p>
   * Sets the terminateAppWithGui value to <code>true</code>.
   * 
   * @param usingGui
   *   Should the Builder execute in GUI mode.  Setting this to <code>false</code> will cause
   *   the Builder to execute in batch mode. 
   * @param abortOnBadParam
   *   Should the Builder terminate execution if an attempt is made to assign a command-line
   *   value to a parameter which cannot accept such a value. 
   * @param useBuilderLogging
   *   Should the Builder use its own builder logging panel or should it disable it and allow 
   *   the plui instance that is running the builder to log to its own Logging Panel? 
   * @param commandLineParams
   *   A MultiMap of parameter values to assign to the Builders, in which the first keys is the
   *   full {@link PrefixedName} of the the Builder or Namer the value is for and the remaining
   *   keys are the parameter names, while the leaf values are the actual values to be 
   *   assigned. In command line execution, this data structure is built automatically from 
   *   the command line flags.  When Builders are being invoked in other contexts, preset
   *   values can be passed into the Builder using this parameter.
   */
  public
  BuilderInformation
  (
    boolean usingGui,
    boolean abortOnBadParam,
    boolean useBuilderLogging,
    MultiMap<String, String> commandLineParams
  )
  {
    this(usingGui, true, abortOnBadParam, useBuilderLogging, commandLineParams);
  }
  
  /**
   * Make a new Builder Information.
   * 
   * @param usingGui
   *   Should the Builder execute in GUI mode.  Setting this to <code>false</code> will cause
   *   the Builder to execute in batch mode. 
   * @param terminateAppWithGui
   *   Should the instance of the JVM that is running the Builder be terminated when this
   *   Builder finishes execution?
   * @param abortOnBadParam
   *   Should the Builder terminate execution if an attempt is made to assign a command-line
   *   value to a parameter which cannot accept such a value. 
   * @param useBuilderLogging
   *   Should the Builder use its own builder logging panel or should it disable it and allow 
   *   the plui instance that is running the builder to log to its own Logging Panel? 
   * @param commandLineParams
   *   A MultiMap of parameter values to assign to the Builders, in which the first keys is the
   *   full {@link PrefixedName} of the the Builder or Namer the value is for and the remaining
   *   keys are the parameter names, while the leaf values are the actual values to be 
   *   assigned. In command line execution, this data structure is built automatically from 
   *   the command line flags.  When Builders are being invoked in other contexts, preset
   *   values can be passed into the Builder using this parameter.
   */
  public
  BuilderInformation
  (
    boolean usingGui,
    boolean terminateAppWithGui,
    boolean abortOnBadParam,
    boolean useBuilderLogging,
    MultiMap<String, String> commandLineParams
  )
  {
    pUsingGUI = usingGui;
    pAbortOnBadParam = abortOnBadParam;
    pUseBuilderLogging = useBuilderLogging;
    pTerminateAppOnQuit = terminateAppWithGui;
    pCommandLineParams = new MultiMap<String, String>(commandLineParams);
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
   * Add a node to the list of things to be queued at the end of the builder run.
   * <p>
   * This method is wrapped by the BaseBuilder method 
   * {@link BaseBuilder#addToQueueList(String) addToQueueList()}.
   * 
   * @param nodeName
   *   The name of the node to add.
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
   * Remove a node from the list of things to be queued at the end of the builder run.
   * <p>
   * This method is wrapped by the BaseBuilder method 
   * {@link BaseBuilder#removeFromQueueList(String) removeFromQueueList()}.
   * 
   * @param nodeName
   *   The name of the node to add.
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
   * Clear the list of things to be queued at the end of the builder run.
   *  <p>
   * This method is wrapped by the BaseBuilder method 
   * {@link BaseBuilder#clearQueueList() clearQueueList()}.
   * <p>
   * This method will affect all Builders.  After it is run, none of the nodes that have been
   * added to the queue list will actually be queued.  It should only be used in extreme cases.
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
  
  /**
   * Add a new pass to the list of Construct Passes that will be executed by the Builder.
   * <p>
   * This method does not need to be called directly.  Calling 
   * {@link BaseBuilder#addConstructPass(ConstructPass)} is a better method to call, since it
   * handles this step as well as other Builder internals.
   * 
   * @param pass
   *   The Construct Pass to add.
   * 
   * @param builder
   *   The Builder which is adding the Construct Pass.  This is needed for doing reverse 
   *   lookups from Construct Passes back to Builders.
   * 
   * @return <code>True</code> if the pass was successfully added or <code>false</code> if 
   *  there if the pass has been added already.
   *  
   *  @see BaseBuilder#addConstructPass(ConstructPass)
   */
  public final boolean
  addConstructPass
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
   * Get the Builder that is associated with a given ConstructPass.
   * 
   * @param pass
   *   The Construct Pass whose creator is being found.
   * 
   * @return
   *   The Builder which created the Construct Pass. 
   */
  public final BaseBuilder
  getBuilderFromPass
  (
    ConstructPass pass
  ) 
  {
    return pPassToBuilderMap.get(pass);
  }
  
  /**
   * Get a list of all the Construct Passes that Builders have created.
   * @return
   *   The list of passes.
   */
  public final LinkedList<ConstructPass>
  getAllConstructPasses()
  {
    return new LinkedList<ConstructPass>(pAllConstructPasses);
  }
  
  /**
   * Create a dependency between two ConstructPasses.
   * <p>
   * The target ConstructPass will not be run until the source ConstructPass has completed.
   * This allows for Builders to specify the order in which their passes run.
   * <p>
   * This method is wrapped by the 
   * {@link BaseBuilder#addPassDependency(ConstructPass, ConstructPass)} method for
   * convenience.  It is irrelevant which one is used.
   * 
   * @param sourcePass
   *   The source Construct Pass which will be run earlier.
   *   
   * @param targetPass
   *   The target Construct pass which will not be run until the source pass has finished.
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
	new PassDependency(targetPass, sourcePass);
      pPassDependencies.put(targetPass, pd);
    }
  }
  
  public final Map<ConstructPass, PassDependency>
  getPassDependencies()
  {
    return Collections.unmodifiableMap(pPassDependencies);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N   L I S T                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a Builder to the list that controls the order in which nodes are checked-in.
   * <p>
   * The addition of Builders to this list is handled internally by Builders and this method
   * should not be called in user code.
   * 
   * @param builder
   *   The Builder to add.
   */
  public void
  addToCheckinList
  (
    BaseBuilder builder
  )
  {
    pCheckInOrder.add(builder);
  }
  
  /**
   * Get the list of Builders in the order in which they will check-in nodes.
   * 
   * @return
   *   The list of Builders.
   */
  public List<BaseBuilder>
  getCheckinList()
  {
    return Collections.unmodifiableList(pCheckInOrder);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M M A N D   L I N E   P A R A M S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return a {@link MultiMap} of all the command line parameters.
   * <p>
   * The first level in the MultiMap is made up of the names of all the builders, the second
   * level is all the parameter names, and every level after that (if they exist) are keys
   * into Complex Parameters.  Values are stored as map values on the leaf nodes.
   * 
   * @return 
   *   The map of params.
   */
  public final MultiMap<String, String>
  getCommandLineParams()
  {
    return pCommandLineParams;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  C A L L   H I E R A R C H Y                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Used in GUI code to determine how many Builders remain to be called during Setup Pass
   * execution.
   * <p>
   * This method should not be called from user code.
   * 
   * @return
   *   The number of Builders still to run their Setup Passes.
   */
  public int
  getCallHierarchySize()
  {
    return pCallHierarchy.size();
  }
  
  /**
   * Used in GUI code to get the next Builder to run its Setup Passes.
   * <p>
   * Calling this method in user code <b>WILL</b> cause Builder execution to behave
   * incorrectly and should be avoided.
   * 
   * @return
   *   The next Builder.
   */
  public BaseBuilder
  pollCallHierarchy()
  {
    return pCallHierarchy.poll();
  }
  
  /**
   * Adds a Builder to the list of Builders waiting to run their Setup Passes.
   * <p>
   * Calling this method in user code <b>WILL</b> cause Builder execution to behave
   * incorrectly and should be avoided.
   * 
   * @param builder
   *   The Builder to add.
   */
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
  
  /**
   * Is the Builder executing in GUI mode?
   */
  public final boolean
  usingGui()
  {
    return pUsingGUI;
  }
  
  /**
   * Will the Builder abort when attempting to set a command line value for a parameter which
   * does not implement the {@link SimpleParamAccess} interface?
   */
  public final boolean
  abortOnBadParam()
  {
    return pAbortOnBadParam;
  }
  
  /**
   * Is the Builder using its own logging code.
   */
  public final boolean
  useBuilderLogging()
  {
    return pUseBuilderLogging;
  }
  
  /**
   * Is the Builder terminating its JVM when it finished execution.
   */
  public final boolean
  terminateAppOnQuit()
  {
    return pTerminateAppOnQuit;
  }

  /**
   * Gets a new instance of {@link StageInformation}, initialized with the correct
   * {@link StageState}.
   * <p>
   * This is done automatically during Builder initialization and user code should only
   * need to call this method if there is a pressing need for a discrete instance of a
   * Stage Information class, potentially if radically different Selection Key settings
   * are needed.
   * 
   * @return
   *   The new instance of Stage Information, initialized with the global StageState.
   */
  public final StageInformation
  getNewStageInformation()
  {
    return new StageInformation(pStageState);
  }
  
  
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
    
    /**
     * Private constructor to prevent people from being too clever.
     */
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
      pDefaultHardwareKeys = new TreeSet<String>();
      pSelectionKeyStack = new LinkedList<TreeSet<String>>();
      pLicenseKeyStack = new LinkedList<TreeSet<String>>();
      pDoAnnotations = false;
      pActionOnExistence = ActionOnExistence.Continue;
    }

    
    
    /*--------------------------------------------------------------------------------------*/
    /*   A C C E S S                                                                        */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Replace the current list of default Selection Keys with a new list.
     * 
     * @param keys
     *   The new list of keys or <code>null</code> to clear the list.
     */
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
    
    /**
     * Adds a Selection Key to the existing list of default Selection Keys.
     * 
     * @param key
     *   The key to add.
     */
    public void
    addDefaultSelectionKey
    (
      String key  
    )
    {
      pDefaultSelectionKeys.add(key);
    }
    
    /**
     * Replace the current list of default License Keys with a new list.
     * 
     * @param keys
     *   The new list of keys or <code>null</code> to clear the list.
     */
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
    
    /**
     * Adds a License Key to the existing list of default License Keys.
     * 
     * @param key
     *   The key to add.
     */
    public void
    addDefaultLicenseKey
    (
      String key  
    )
    {
      pDefaultLicenseKeys.add(key);
    }
    
    /**
     * Replace the current list of default Hardware Keys with a new list.
     * 
     * @param keys
     *   The new list of keys or <code>null</code> to clear the list.
     */
    public void
    setDefaultHardwareKeys
    (
      Set<String> keys
    )
    {
      if (keys == null)
        pDefaultHardwareKeys = new TreeSet<String>();
      else
        pDefaultHardwareKeys = new TreeSet<String>(keys);
    }
    
    /**
     * Adds a Hardware Key to the existing list of default Hardware Keys.
     * 
     * @param key
     *   The key to add.
     */
    public void
    addDefaultHardwareKey
    (
      String key  
    )
    {
      pDefaultHardwareKeys.add(key);
    }
    
    /**
     * Gets all the default Selection Keys at the current instant.
     * <p>
     * This includes all the keys on the default Selection Key list and all the Selection Keys
     * currently in the stack.
     * 
     * @return
     *   The list of keys.
     */
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
    
    /**
     * Gets all the default License Keys at the current instant.
     * <p>
     * This includes all the keys on the default License Key list and all the License Keys
     * currently in the stack.
     * 
     * @return
     *   The list of keys.
     */
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
    
    /**
     * Gets all the default Hardware Keys at the current instant.
     * <p>
     * This includes all the keys on the default Hardware Key list and all the Hardware Keys
     * currently in the stack.
     * 
     * @return
     *   The list of keys.
     */
    public TreeSet<String> 
    getDefaultHardwareKeys()
    {
      TreeSet<String> toReturn = new TreeSet<String>();
      if (pDefaultHardwareKeys != null)
        toReturn.addAll(pDefaultHardwareKeys);
      for (TreeSet<String> keys : pHardwareKeyStack) 
        if (keys != null)
          toReturn.addAll(keys);
      
      return toReturn;
    }
    
    /**
     * Set whether the default Selection Keys should be assigned to nodes.
     */
    public void 
    setUseDefaultSelectionKeys
    (
      boolean value
    )
    {
      pUseDefaultSelectionKeys = value;
    }
    
    /**
     * Whether the default Selection Keys should be assigned to nodes.
     */
    public boolean 
    useDefaultSelectionKeys()
    {
      return pUseDefaultSelectionKeys; 
    }

    /**
     * Set whether the default License Keys should be assigned to nodes.
     */
    public void 
    setUseDefaultLicenseKeys
    (
      boolean value
    )
    {
      pUseDefaultLicenseKeys = value;
    }
    
    /**
     * Whether the default License Keys should be assigned to nodes.
     */
    public boolean 
    useDefaultLicenseKeys()
    {
      return pUseDefaultLicenseKeys; 
    }
    
    /**
     * Set whether the default Hardware Keys should be assigned to nodes.
     */
    public void 
    setUseDefaultHardwareKeys
    (
      boolean value
    )
    {
      pUseDefaultHardwareKeys = value;
    }
    
    /**
     * Whether the default Hardware Keys should be assigned to nodes.
     */
    public boolean 
    useDefaultHardwareKeys()
    {
      return pUseDefaultHardwareKeys; 
    }
    
    /**
     * Add a group of Selection Keys to the top of the stack.
     * <p>
     * This functionality allows for Selection Keys to be temporarily added to the list of 
     * default keys without having to manage what has been added and when.  Keys can be 
     * pushed onto the stack when they are needed and then popped off when they are no 
     * longer needed.  All the keys that are added in an invocation of push() are treated
     * as a single entitiy and will all be removed from the stack when pop() is called.
     * <p>
     * One example of this might be with a Builder that creates nodes for multiple departments.
     * There would be a set of default Selection Keys which would represent settings for the 
     * project.  Then, as the nodes for each department were created, different Selection Keys
     * would be pushed on to the stack and then popped off once all the nodes for that 
     * department were created.  Since it is a stack, it is possible to nest these calls.
     * As an example, say nodes are being created for the Animation department.  Before node
     * creation begins, the Animation Selection Key is pushed on to the stack.  Then, the User 
     * key is pushed, indicating that the nodes being built are going to be queued by a user
     * who will probably want quick feedback.  These settings are used to create a Submit
     * network.  Once that is done, pop() is called, which removes the User key, but leaves
     * the Animation key on the stack.  The Auto key is then pushed onto the stack for the 
     * building of the Approval network.  Once that is complete, pop() is called twice to 
     * remove the Auto key and then the Animation key.
     * 
     * @param keys
     *   The list of keys to put on the stack.
     */
    public void
    pushSelectionKeys
    (
      TreeSet<String> keys  
    )
    {
      pSelectionKeyStack.addFirst(keys);
    }
    
    /**
     * Remove the top group of Selection Keys from the stack.
     * 
     * @see #pushSelectionKeys(TreeSet)
     */
    public void
    popSelectionKeys()
    {
      pSelectionKeyStack.poll();
    }
    
    /**
     * Add a group of License Keys to the top of the stack.
     * <p>
     * See the #pushSelectionKeys(TreeSet) for an explanation of how key stacks work.
     * 
     * @param keys
     *  The list of keys.
     */
    public void
    pushLicenseKeys
    (
      TreeSet<String> keys  
    )
    {
      pLicenseKeyStack.addFirst(keys);
    }
    
    /**
     * Remove the top group of License Keys from the stack.
     * 
     * @see #pushSelectionKeys(TreeSet)
     */
    public void
    popLicenseKeys()
    {
      pLicenseKeyStack.poll();
    }
    
    /**
     * Add a group of Hardware Keys to the top of the stack.
     * <p>
     * See the #pushSelectionKeys(TreeSet) for an explanation of how key stacks work.
     * 
     * @param keys
     *  The list of keys.
     */
    public void
    pushHardwareKeys
    (
      TreeSet<String> keys  
    )
    {
      pHardwareKeyStack.addFirst(keys);
    }
    
    /**
     * Remove the top group of Hardware Keys from the stack.
     * 
     * @see #pushSelectionKeys(TreeSet)
     */
    public void
    popHardwareKeys()
    {
      pHardwareKeyStack.poll();
    }
    
    /**
     * Should stages add annotations to the nodes that they are building?
     * <p>
     * This is a global setting and will effect all nodes that are being built.
     */
    public boolean 
    doAnnotations()
    {
      return pDoAnnotations;
    }
    
    /**
     * Sets whether stages should add annotations to the nodes that they are building.
     * <p>
     * This is a global setting and will effect all nodes that are being built.
     */
    public void 
    setDoAnnotations
    (
      boolean doAnnotations
    )
    {
      pDoAnnotations = doAnnotations;
    }
    
    /**
     * Sets what stages should do when they encounter a node that already exists.
     * 
     * @param aoe
     *   The Action
     *  
     * @see ActionOnExistence 
     */
    public void
    setActionOnExistence
    (
      ActionOnExistence aoe  
    )
    {
      pActionOnExistence = aoe;
    }
    
    /**
     * What stages should do when they encounter a node that already exists.
     *  
     * @see ActionOnExistence 
     */
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
     * Gets a list of all the nodes that have been conformed by a stage.
     */
    public TreeSet<String>
    getConformedNodes()
    {
      return pStageState.getConformedNodes();
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
      return pStageState.getCheckedOutNodes();
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
      return pStageState.getSkippedNodes();
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
     * Adds a node name to the list of nodes created during the session.
     * <P>
     * The method will return a boolean based on whether the node already existed in the
     * current list. A return value of <code>false</code> indicates that the name was not
     * added to the list since it already existed then. A return value of <code>true</code>
     * indicates that the add was successful.  A PipelineException is thrown if the
     * <code>initializeAddedNodes</code> method was not called before calling this method.
     * 
     * @param name
     *   The name of the node
     * @param author
     *   The author whose working area the node was created in.
     * @param view
     *   The working area where the node was created.
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
    
    /**
     * Adds a node to the list of things that have been checked out by a stage.
     */
    public final void
    addCheckedOutNode
    (
      String name  
    )
    {
      pStageState.addCheckedOutNode(name);
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
      pStageState.addSkippedNode(name);
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
      pStageState.addConformedNode(name);
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
      return pStageState.getStageFunctionHardwareKeys(function);
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
      pStageState.setStageFunctionHardwareKeys(function, keys);
    }
    
    
    
    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/
    
    private TreeSet<String> pDefaultSelectionKeys = new TreeSet<String>();
    
    private TreeSet<String> pDefaultLicenseKeys = new TreeSet<String>();
    
    private TreeSet<String> pDefaultHardwareKeys = new TreeSet<String>();
    
    private LinkedList<TreeSet<String>> pSelectionKeyStack;
    
    private LinkedList<TreeSet<String>> pLicenseKeyStack;
    
    private LinkedList<TreeSet<String>> pHardwareKeyStack;
    
    private boolean pUseDefaultSelectionKeys;
    
    private boolean pUseDefaultLicenseKeys;
    
    private boolean pUseDefaultHardwareKeys;
    
    private boolean pDoAnnotations;
    
    private ActionOnExistence pActionOnExistence;
  }  //Stage Information
  
  
  
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
  
  private boolean pUseBuilderLogging = false;
  
  private boolean pTerminateAppOnQuit;
  
  private LinkedList<BaseBuilder> pCheckInOrder;
  
  private LinkedList<BaseBuilder> pCallHierarchy;
  
  private StageState pStageState;
  
  private ListMap<ConstructPass, PassDependency> pPassDependencies; 
}
