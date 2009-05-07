// $Id: BuilderInformation.java,v 1.22 2009/05/07 03:25:29 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
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
    pBuilderStageState = new StageState();
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A G E   S T A T E   W R A P P E R                                                 */
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
    return pBuilderStageState.addAOEMode(mode, aoe);
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
    return pBuilderStageState.addAOEOverride(mode, nodeName, aoe);
  }
  
  /**
   * Get the AoE Modes.
   */
  public Set<String>
  getAOEModes()
  {
    return pBuilderStageState.getAOEModes();
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
    return getDefaultAOE(mode);
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
    return pBuilderStageState.getNodeAOE(mode, node);
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
    return pBuilderStageState.getBaseNodeAOE(mode, node);
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
    return new StageInformation(pBuilderStageState);
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
      pDefaultAOE = ActionOnExistence.Continue;
      pAOEMode = ActionOnExistence.Continue.toString();
      pAddedNodes = new TreeMap<String, NodeID>();
      
      pConformedNodes = new TreeSet<String>();
      pCheckedOutNodes = new TreeSet<String>();
      pSkippedNodes = new TreeSet<String>();
    }

    
    
    /*--------------------------------------------------------------------------------------*/
    /*   Q U E U E   K E Y S                                                                */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Replace the current list of default Selection Keys with a new list.
     * 
     * @param keys
     *   The new list of keys or <code>null</code> to clear the list.
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *  
     * @deprecated
     *   Due to the addition of key choosers, it is not recomemnded to hard code keys on nodes.
     */
    @Deprecated
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
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
    public boolean 
    useDefaultSelectionKeys()
    {
      return pUseDefaultSelectionKeys; 
    }

    /**
     * Set whether the default License Keys should be assigned to nodes.
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
    public boolean 
    useDefaultLicenseKeys()
    {
      return pUseDefaultLicenseKeys; 
    }
    
    /**
     * Set whether the default Hardware Keys should be assigned to nodes.
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *   
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *  
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     *  
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
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
     * 
     * @deprecated
     *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
     */
    @Deprecated
    public void
    popHardwareKeys()
    {
      pHardwareKeyStack.poll();
    }

    
    
    /*--------------------------------------------------------------------------------------*/
    /*   A N N O T A T I O N S                                                              */
    /*--------------------------------------------------------------------------------------*/
    
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


    
    /*--------------------------------------------------------------------------------------*/
    /*   A C T I O N    O N    E X I S T E N C E                                            */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Sets what stages should do when they encounter a node that already exists.
     * 
     * @param aoe
     *   The Action
     *  
     * @see ActionOnExistence 
     * 
     * @deprecated
     *   The introduction of AoE modes makes this method unnecessary and even somewhat 
     *   dangerous.  Calls to this method will change the default AoE for the current builder, 
     *   but will not incorporate any of the per-mode node overrides, which could lead to 
     *   non-intended behavior.  The method has been preserved to prevent older builders from
     *   breaking, but those builders should change their code to use the new AoE modes as
     *   this method will eventually be removed. 
     */
    @Deprecated
    public void
    setActionOnExistence
    (
      ActionOnExistence aoe  
    )
    {
      pDefaultAOE = aoe;
    }
    
    /**
     * What stages should do when they encounter a node that already exists.
     *  
     * @see ActionOnExistence
     * @see #getActionOnExistence(String)
     * 
     * @deprecated
     *   The introduction of AoE modes makes this method unnecessary. The method has been 
     *   preserved to prevent older builders from breaking, but those builders should change 
     *   their code to use the new AoE modes as this method will eventually be removed.
     */
    @Deprecated
    public ActionOnExistence
    getActionOnExistence()
    {
      return pDefaultAOE;
    }
    
    /**
     * Get the ActionOnExistence for the specified node.
     * <p>
     * This method will take into account the default AoE that has been set in the 
     * StageInformation, preferably using the AoE mode code.
     * 
     * @param nodeName
     *   The name of the node, which is used to search for per-node mode overrides.
     * 
     * @return
     *   The response to the node existing.
     */
    public ActionOnExistence
    getActionOnExistence
    (
      String nodeName  
    )
    {
      ActionOnExistence toReturn = pStageState.getBaseNodeAOE(pAOEMode, nodeName);
      if (toReturn == null)
        toReturn = pDefaultAOE;
      return toReturn;
    }
    
    /**
     * Set the AoE mode in the current Stage Information.
     * <p>
     * This call only effects the current instance of Stage Information, which means that 
     * each builder needs to make this call for itself.  That is handled by default in
     * the {@link BaseBuilder#validateBuiltInParams()} method.
     * 
     * @param mode
     *   The name of the AoE mode.
     *   
     * @throws IllegalArgumentException
     *   If a non-existent mode is specified.
     */
    public void
    setAOEMode
    (
      String mode  
    )
    {
      pDefaultAOE = pStageState.getDefaultAOE(mode);
      pAOEMode = mode;
    }
    
    /**
     * Get the name of the current AoE mode.
     */
    public String
    getAOEMode()
    {
      return pAOEMode;
    }
    

    
    /*--------------------------------------------------------------------------------------*/
    /*   S T A G E   S T A T E   W R A P P E R                                              */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Gets a list of all the nodes that have been conformed by a stage.
     */
    public TreeSet<String>
    getAllConformedNodes()
    {
      return pStageState.getConformedNodes();
    }
    
    /**
     * Get a list of all the nodes that have been conformed by a stage in this builder.
     */
    public TreeSet<String>
    getConformedNodes()
    {
      return new TreeSet<String>(pConformedNodes);
    }
    
    /**
     * Get a list of all the nodes that have been checked out using the
     * {@link BaseStage#checkOut(VersionID, CheckOutMode, CheckOutMethod)} method.
     * <p>
     * This does not include nodes that were checked out as part of the neededNode
     * functionality in Builders.
     */
    public TreeSet<String>
    getAllCheckedOutNodes()
    {
      return pStageState.getCheckedOutNodes();
    }
    

    /**
     * Get a list of all the nodes that have been checked out using the
     * {@link BaseStage#checkOut(VersionID, CheckOutMode, CheckOutMethod)} method
     * in the current Builder.
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
     * Get a list of all the nodes that have been skipped by the build() method
     * of their stage
     */
    public TreeSet<String>
    getAllSkippedNodes()
    {
      return pStageState.getSkippedNodes();
    }
    

    /**
     * Get a list of all the nodes in the current Builder that have been skipped 
     * by the build() method of their stage
     */
    public TreeSet<String>
    getSkippedNodes()
    {
      return new TreeSet<String>(pSkippedNodes);
    }
    
    /**
     * Get a map that contains the NodeIDs of all the nodes that have been built by stages
     * indexed by node name.
     * 
     * @return The map containing the node names.
     */
    public TreeMap<String, NodeID> 
    getAllAddedNodes()
    {
      return pStageState.getAddedNodes();
    }
    
    /**
     * Get a map that contains the NodeIDs of all the nodes that have been built by stages in
     * the current Builder indexed by node name.
     * 
     * @return The map containing the node names.
     */
    public TreeMap<String, NodeID> 
    getAddedNodes()
    {
      return new TreeMap<String, NodeID>(pAddedNodes);
    }

    /**
     * Add a node name to the list of nodes created during the session.
     * <P>
     * The method will return a boolean based on whether the node already existed in the
     * current list. A return value of <code>false</code> indicates that the name was not
     * added to the list since it already existed then. A return value of <code>true</code>
     * indicates that the add was successful.
     * <p>
     * Note that if another Builder executed in the same invocation has already registered 
     * the node, it will not be marked as having been created by this Builder.
     * 
     * @param name
     *   The name of the node
     * @param author
     *   The author whose working area the node was created in.
     * @param view
     *   The working area where the node was created.
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
      boolean built = pStageState.addNode(name, author, view);
      if (built)
        pAddedNodes.put(name, new NodeID(author, view, name));
      return built;
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
      pStageState.addCheckedOutNode(name);
      pCheckedOutNodes.add(name);
    }
    
    /**
     * Add a node to the list of things that have been checked out by a stage.
     */
    public final void
    addSkippedNode
    (
      String name  
    )
    {
      pStageState.addSkippedNode(name);
      pSkippedNodes.add(name);
    }
    
    /**
     * Add a node to the list of things that have been conformed by a stage.
     */
    public final void
    addConformedNode
    (
      String name  
    )
    {
      pStageState.addConformedNode(name);
      pConformedNodes.add(name);
    }
    
    /**
     * Get the default editor for a particular stage function.
     * 
     * @param function
     *   The name of the function
     * 
     * @return
     *   The PluginContext representing the editor.
     */
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
      pStageState.setStageFunctionSelectionKeys(function, keys);
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
      pStageState.setStageFunctionLicenseKeys(function, keys);
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
      pStageState.setStageFunctionHardwareKeys(function, keys);
    }
    
    
    
    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * A map containing all the NodeIDs for nodes that that have been added by stages in the
     * current builder, indexed by node name.
     * <p>
     * All stages are responsible for ensuring that all created nodes end up in this data
     * structure.
     */
    private TreeMap<String, NodeID> pAddedNodes;

    
    private TreeSet<String> pConformedNodes;
    private TreeSet<String> pCheckedOutNodes;
    private TreeSet<String> pSkippedNodes;

    
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
    
    private ActionOnExistence pDefaultAOE;
    
    private String pAOEMode;
    
    private StageState pStageState;
  }  //Stage Information
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private MultiMap<String, String> pCommandLineParams; 
  
  /**
   * Is this Builder in GUI mode.
   */
  private boolean pUsingGUI = false;
  
  private boolean pAbortOnBadParam = false;
  
  private boolean pUseBuilderLogging = false;
  
  private boolean pTerminateAppOnQuit;
  
  private StageState pBuilderStageState;
}
