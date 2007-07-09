package us.temerity.pipeline.builder;

import java.util.*;

import javax.swing.SwingUtilities;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.MultiMap.MultiMapNamedEntry;
import us.temerity.pipeline.builder.ui.JBuilderParamDialog;
import us.temerity.pipeline.stages.BaseStage;
import us.temerity.pipeline.ui.UIFactory;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The parent class of all Builders.
 * <p>
 * Stuff here.
 * <p>
 * Terminology used within these java docs.
 * <dl>
 * <dt>Builder</dt>
 * <dd>Refers to any instance of this class. Usually preceded with 'parent' when being used
 * with Sub-Builder to make the relation clear. </dd>
 * <dt>Sub-Builder</dt>
 * <dd>Refers to any instance of {@link BaseUtil} that is being used as a child of
 * the current Builder.</dd>
 * <dt>child Builder</dt>
 * <dd>Refers to any instance of this class that is being used as a child of the current
 * builder.</dd>
 * <dt>child Namer</dt>
 * <dd>Refers to any instance of {@link BaseNames} that is being used as a child of the
 * current builder</dd>
 * <dt>New Sub-Builder
 * <dd>Referes to any Sub-Builder before it has been prepared by the First Loop, either by
 * executing the SetupPasses if it is a child Builder or by calling
 * {@link BaseNames#generateNames()} if it is a child Namer</dd>
 * <dt>First Loop
 * <dd>The period of Builder execution during which all the SetupPasses are run and child
 * Namers have {@link BaseNames#generateNames()} called.
 * <dt>Second Loop
 * <dd>The period of Builder execution during with all the ConstructPasses are run.
 * </dl>
 */
public abstract 
class BaseBuilder
  extends BaseUtil
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected
  BaseBuilder
  (
    String name,
    VersionID vid,
    String vendor,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super(name, vid, vendor, desc, mclient, qclient);
    pBuilderInformation = builderInformation;
    pSubBuilders = new TreeMap<String, BaseBuilder>();
    pSubNames = new TreeMap<String, BaseNames>();
    pPreppedBuilders = new TreeMap<String, BaseBuilder>();
    pGeneratedNames = new TreeMap<String, BaseNames>();
    pFirstLoopPasses = new ArrayList<SetupPass>();
    pRemainingFirstLoopPasses = new LinkedList<SetupPass>();
    pSecondLoopPasses = new ArrayList<ConstructPass>();
    {
      UtilityParam param = 
	new EnumUtilityParam
	(aActionOnExistance,
	 "What action should the Builder take when a node already exists.",
	 ActionOnExistance.Continue.toString(),
	 ActionOnExistance.getStringList());
      addParam(param);
    }
    {
      UtilityParam param = 
	new BooleanUtilityParam
	(aReleaseOnError,
	 "Release all the created nodes if an exception is thrown.", 
	 false);
      addParam(param);
    }
    pCurrentPass = 1;
    pInitialized = false;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   L A Y O U T                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected final void
  setLayout
  (
    PassLayoutGroup layout
  )
  {
    int num = layout.getNumberOfPasses();
    int setupNum = pFirstLoopPasses.size();
    if (num > setupNum)
      throw new IllegalArgumentException
        ("There are more passes in the layout than SetupPasses exist for " +
         "builder (" + getName() + ").");
    super.setLayout(layout);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds a Sub-Builder to the current Builder, with the given Builder Parameter mapping.
   * <p>
   * Should never be called in a Builder constructor.  
   * @throws PipelineException
   *         If an attempt is made to add a Sub-Builder with the same name as on that already
   *         exists.
   */
  public final void 
  addSubBuilder
  (
    BaseUtil subBuilder,
    boolean defaultMapping,
    TreeMap<ParamMapping, ParamMapping> paramMapping
  ) 
    throws PipelineException
  {
    String instanceName = subBuilder.getName();
    if (pSubBuilders.containsKey(instanceName) || pPreppedBuilders.containsKey(instanceName)
      || pGeneratedNames.containsKey(instanceName) || pSubNames.containsKey(instanceName))
      throw new PipelineException
        ("Cannot add a SubBuilder with the same name ("+ instanceName+") " +
         "as one that already exists.");
    
    PrefixedName prefixed = new PrefixedName(getPrefixedName(), instanceName);
    subBuilder.setPrefixedName(prefixed);
    
    pLog.log(Kind.Bld, Level.Fine, 
      "Adding a SubBuilder with instanceName (" + prefixed.toString() + ") to " +
      "Builder (" + getName() + ").");
    
    if (subBuilder instanceof BaseNames)
      pSubNames.put(instanceName, (BaseNames) subBuilder);
    else {
      pSubBuilders.put(instanceName, (BaseBuilder) subBuilder);
    }
    
    if (defaultMapping) {
      addMappedParam(instanceName, aUtilContext, aUtilContext);
      if (subBuilder instanceof BaseBuilder) {
	addMappedParam(instanceName, aActionOnExistance, aActionOnExistance);
	addMappedParam(instanceName, aReleaseOnError, aReleaseOnError);
      }
    }
    
    if (paramMapping != null)
      addMappedParams(instanceName, paramMapping);
  }

  /**
   * Adds a Sub-Builder to the current Builder.
   * <p>
   * Should never be called in a Builder constructor.  
   * @throws PipelineException
   *         If an attempt is made to add a Sub-Builder with the same name as on that already
   *         exists.
   */
  public final void 
  addSubBuilder
  (
    BaseUtil subBuilder,
    boolean defaultMapping
  ) 
    throws PipelineException
  {
    addSubBuilder(subBuilder, defaultMapping, new TreeMap<ParamMapping, ParamMapping>());
  }

  /**
   * Adds a Sub-Builder to the current Builder.
   * <p>
   * Should never be called in a Builder constructor.   
   * @throws PipelineException
   *         If an attempt is made to add a Sub-Builder with the same name as on that already
   *         exists.
   */
  public final void 
  addSubBuilder
  (
    BaseUtil subBuilder
  ) 
    throws PipelineException
  {
    addSubBuilder(subBuilder, true, new TreeMap<ParamMapping, ParamMapping>());
  }

  
  /**
   * Gets a mapping of all the Sub-Builders, keyed by the names of the Sub-Builders.
   */
  public final TreeMap<String, BaseBuilder>
  getSubBuilders()
  {
    TreeMap<String, BaseBuilder> toReturn = new TreeMap<String, BaseBuilder>();
    toReturn.putAll(pSubBuilders);
    toReturn.putAll(pPreppedBuilders);
    return toReturn;
  }
  
  /**
   * Gets the Sub-Builder identified by the given instance name.
   * <p>
   * It is assumed that a Builder actually knows what all its Sub-Builders are named.
   * Therefore this method throws an {@link IllegalArgumentException} if this contract is
   * violated. If code is properly writen, this exception should never be thrown, since a
   * parent Builder should never ask for a non-existant Sub-Builder. If some sort of
   * verification of existance is needed, the {@link #getSubBuilders()} method can be used to
   * get a Map of all the Sub-Builder which can be queried to determine existance.
   * 
   * @throws IllegalArgumentException
   *         If the instance name passed in does not correspond to an existing Sub-Builder.
   */
  public final BaseUtil 
  getSubBuilder
  (
    String instanceName
  )
  {
    if (pSubBuilders.containsKey(instanceName))
      return pSubBuilders.get(instanceName);
    else if (pPreppedBuilders.containsKey(instanceName))
      return pPreppedBuilders.get(instanceName);
    else if (pSubNames.containsKey(instanceName))
      return pSubNames.get(instanceName);
    else if (pGeneratedNames.containsKey(instanceName))
      return pGeneratedNames.get(instanceName);
    else
      throw new IllegalArgumentException
        ("This builder does not contain a Sub-Builder with the name (" + instanceName + ").");
  }
  
  public final Map<String, BaseNames>
  getNamers()
  {
    return Collections.unmodifiableMap(pSubNames);
  }

  /**
   * Gets a Sub-Builder that has not been initialized yet.
   * <p>
   * This means either a child Builder before its Setup Passes were run or a child Namer
   * before generateNames is run.
   */
  public final BaseUtil 
  getNewSubBuilder
  (
    String instanceName
  )
  {
    if (pSubBuilders.containsKey(instanceName))
      return pSubBuilders.get(instanceName);
    else if (pSubNames.containsKey(instanceName))
      return pSubNames.get(instanceName);
    else
      throw new IllegalArgumentException
        ("This builder does not contain a Sub-Builder with the name (" + instanceName + ").");
  }

  /**
   * Creates a mapping between a parameter in the named Sub-Builder and a parameter
   * in the parent Builder. 
   * <p>
   * Error checking will cover the existance of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values.
   *
   * @param subBuilderName
   * 	The subBuilder the mapping is being created in.
   * @param subParam
   * 	The Sub-Builder parameter that is being driven.
   * @param masterParam
   * 	The parent Builder parameter that is driving the Sub-Builder parameter.
   * @throws PipelineException
   * 	When either Parameter is not a Simple Parameter or doesn't exist.
   */
  public final void
  addMappedParam
  (
    String subBuilderName,
    ParamMapping subParam,
    ParamMapping masterParam
  ) 
    throws PipelineException
  {
    BaseUtil subBuilder = getSubBuilder(subBuilderName);
    
    if (!subBuilder.hasSimpleParam(subParam))
      throw new PipelineException
      ("Illegal attempt mapping a Builder parameter to a SubBuilder.\n" +
       subParam + " is not a Simple Parameter in the Sub Builder " +
       "identified with (" + subBuilderName +  "), making the attempted mapping invalid.\n" +
       "The full attempted mapping was of " + masterParam + " in the master " +
       	"to " + subParam + " in the sub Builder (" + subBuilderName + ").");
      
    if (!hasSimpleParam(masterParam))
      throw new PipelineException
      ("Illegal attempt mapping a Builder parameter to a SubBuilder.\n" +
       masterParam + " is not a Simple Parameter in this Builder " +
       "named (" + getName() +  "), making the attempted mapping invalid.\n" +
       "The full attempted mapping was of " + masterParam+ "in the master " +
       "to " + subParam + " in the sub Builder(" + subBuilderName + ").");
    
    subBuilder.addParamMapping(subParam, masterParam);

    pLog.log(Kind.Bld, Level.Finer, 
      "Creating a parameter mapping between " + subParam + " in " +
      "SubBuilder (" + subBuilderName + ") and " + masterParam + " in " +
      "Builder (" + getName() + ")");
  }

  /**
   * Creates a mapping between a parameter in the named Sub-Builder and a parameter
   * in the parent Builder. 
   * <p>
   * Error checking will cover the existance of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values.
   *
   * @param subBuilderName
   * 	The subBuilder the mapping is being created in.
   * @param subParamName
   * 	The name of the Sub-Builder parameter that is being driven.
   * @param masterParamName
   * 	The name of the parent Builder parameter that is driving the Sub-Builder parameter.
   * @throws PipelineException
   * 	When either Parameter is not a Simple Parameter or doesn't exist.
   */
  public final void
  addMappedParam
  (
    String subBuilderName, 
    String subParamName,
    String masterParamName
  )
    throws PipelineException
  {

    addMappedParam(subBuilderName, 
                   new ParamMapping(subParamName), 
                   new ParamMapping(masterParamName));
    
  }
  
  /**
   * Creates a mapping between a parameter in the named Sub-Builder and a parameter
   * in the parent Builder. 
   * <p>
   * Error checking will cover the existance of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values.
   *
   * @param subBuilderName
   * 	The subBuilder the mapping is being created in.
   * @param subParamName
   * 	The name of the Sub-Builder parameter that is being driven.
   * @param masterParamName
   * 	The name of the parent Builder parameter that is driving the Sub-Builder parameter.
   * @throws PipelineException
   * 	When either Parameter is not a Simple Parameter or doesn't exist.
   */
  public final void
  addMappedParam
  (
    String subBuilderName, 
    String subParamName,
    List<String> subKeys,
    String masterParamName,
    List<String> masterKeys
  )
    throws PipelineException
  {
    
    ParamMapping subMapping = new ParamMapping(subParamName, subKeys);
    ParamMapping masterMapping = new ParamMapping(masterParamName, masterKeys);
    
    addMappedParam(subBuilderName, subMapping, masterMapping);
  }

  /**
   * Adds a group of Parameter mappings to a Sub-Builder.
   */
  public final void
  addMappedParams
  (
    String subBuilderName, 
    SortedMap<ParamMapping, ParamMapping> mapping
  ) 
    throws PipelineException
  {
    for (ParamMapping sub : mapping.keySet()) {
      ParamMapping master = mapping.get(sub);
      addMappedParam(subBuilderName, sub.getParamName(), sub.getKeys(), 
	             master.getParamName(), master.getKeys());
    }
  }

  /**
   * Gets the named Sub-Builder ready for execution.
   * <p>
   * Gets a list of all the mapped Builder Parameters for the Sub-Builder and then sets them
   * to the correct value based on the parent's values.
   * 
   * @throws PipelineException
   *         If there is no Sub-Builder that hasn't been initialized with the name that is
   *         passed in.
   */
  private void
  initializeSubBuilder(String name)
    throws PipelineException
  {
    BaseUtil subBuilder = getNewSubBuilder(name);
    if (subBuilder == null)
      throw new PipelineException
        ("Somehow a SubBuilder/Name with the name (" + name + ") was submited for " +
         "initialization by the Builder named (" + getName() + ").\n" +
         "This SubBuilder does not exists.\n" +
      	 "This exception most likely represents a fundamental problem with the Builder " +
      	 "backend and should be reported to Temerity.");
    SortedMap<ParamMapping, ParamMapping> paramMapping = subBuilder.getMappedParams();
    pLog.log(Kind.Ops, Level.Fine, "Initializing the subBuilder (" + name + ").");
    for (ParamMapping subParamMapping : paramMapping.keySet()) {
      ParamMapping masterParamMapping = paramMapping.get(subParamMapping);
      
      UtilityParam subParam = subBuilder.getParam(subParamMapping);
      UtilityParam masterParam = this.getParam(masterParamMapping);
      
      assert (subParam != null) : "The subParam value should never be null.";
      assert (masterParam != null) : "The masterParam value should never be null.";
      
      subBuilder.setParamValue(subParamMapping, 
	((SimpleParamAccess) masterParam).getValue());
      pLog.log(Kind.Ops, Level.Finer, 
	"Mapped param (" + subParamMapping + ") in subBuilder " + 
	"(" + name + ") to value from param (" + masterParamMapping + ") " + 
	"in builder (" + getName() + ")");
    }
  }
  
  private final void
  assignCommandLineParams(BaseUtil utility)
    throws PipelineException
  {
    boolean abort = pBuilderInformation.abortOnBadParam();
    
    int currentPass = utility.getCurrentPass();
    TreeSet<String> passParams = utility.getPassParamNames(currentPass);
    
    String prefixName = utility.getPrefixedName().toString();
    
    MultiMap<String, String> specificEntrys = 
      pBuilderInformation.getCommandLineParams().get(prefixName);
    
    pLog.log(Kind.Arg, Level.Fine, 
      "Assigning command line parameters for Builder identified by (" + prefixName + ") " +
      "for pass number (" + currentPass + ")");
    
    if (specificEntrys == null) {
      pLog.log(Kind.Arg, Level.Finer, 
	"No command line parameters for Builder with prefixName (" + prefixName + ")");
      return;
    }
    
    /* This creates a Mapped ArrayList with Parameter name as the key set and
     * the list of keys contained in the MultiMapNamedEntry as the key set.
     */
    MappedArrayList<String, MultiMapNamedEntry<String, String>> commandLineValues = 
      specificEntrys.namedEntries();
    
    
    if(commandLineValues != null) {

      Set<ParamMapping> mappedParams = utility.getMappedParams().keySet();
      
      for (String paramName : commandLineValues.keySet()) {
	if (!passParams.contains(paramName))
	  continue;
	for (MultiMapNamedEntry<String, String> entry : commandLineValues.get(paramName)) {
	  List<String> keys = entry.getKeys();
	  ParamMapping mapping = new ParamMapping(paramName, keys);
	  if (!mappedParams.contains(mapping)) {
	    String value = entry.getValue();
	    if (utility.canSetSimpleParamFromString(mapping)) {
	      SimpleParamFromString param = (SimpleParamFromString) utility.getParam(mapping);
	      try {
		param.fromString(value);
	      } 
	      catch (IllegalArgumentException ex) {
		String message = "There was an error setting the value of a Parameter " +
		  "from a command line argument.\n" + ex.getMessage(); 
		if (abort)
    		  throw new PipelineException(message);
		pLog.log(Kind.Arg, Level.Warning, message);
	      }
	      pLog.log(Kind.Arg, Level.Finest, 
		"Setting command line parameter (" + mapping + ") from builder " +
		"(" + prefixName + ") with the value (" + value + ").");
	    }
	    else {
	      String message = "Cannot set command line parameter (" + mapping + ") " +
	      	"from builder (" + prefixName + ") with the value (" + value + ").\n" +
	        "Parameter is not a Simple Parameter"; 
	      if (abort)
		throw new PipelineException(message);
	      pLog.log(Kind.Arg, Level.Warning, message);
	    }
	  }
	}
      }
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   E X E C U T I O N                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Runs the builder, based on the settings that BuilderApp passes in.
   */
  public final void 
  run()
    throws PipelineException
  {
    if (pBuilderInformation.usingGui())
      runGUI();
    else
      runCommandLine();
  }
  
  /**
   * Runs the command line version of the Builder.
   */
  private final void 
  runCommandLine()
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Fine, "Starting the command line execution.");
    boolean finish = false;
    try {
      pLog.log(Kind.Ops, Level.Fine, "Beginning execution of SetupPasses.");
      executeFirstLoop();
      buildSecondLoopExecutionOrder();
      executeSecondLoop();
      waitForJobs(queueJobs());
      finish = didTheNodesFinishCorrectly(pBuilderInformation.getQueueList());
    }
    catch (Exception ex) {
      String logMessage = "An Exception was thrown during the course of execution.\n";
      logMessage += ex.getMessage() + "\n";
      if (pReleaseOnError) 
	logMessage += "All the nodes that were registered will now be released.";
      pLog.log(Kind.Ops, Level.Severe, logMessage);
      if (pReleaseOnError)
	BaseStage.cleanUpAddedNodes(pClient, pBuilderInformation.getStageInformation());
      return;
    }
    if (finish)
      executeCheckIn();
    else
      throw new PipelineException("Execution halted.  Jobs didn't finish correctly");
  }
  
  private final void
  runGUI()
  {
    pCurrentBuilder = this;
    pNextBuilder = null;
    SwingUtilities.invokeLater(new BuilderGuiThread(this));
  }

  public void
  testGuiInit()
  {
    pCurrentBuilder = this;
    pNextBuilder = null;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L O O P S   A N D   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Method that assigns the built-in params to the right places, so that the other pieces of
   * {@link BaseBuilder} that depends on them can use them.
   * <p>
   * It is highly advised that all Builders call this as the first action in the validate
   * method in their first {@link SetupPass}.  Otherwise there may be unpredictable behavior.
   * @throws PipelineException 
   */
  public final void 
  validateBuiltInParams() 
    throws PipelineException 
  {
    pLog.log(Kind.Bld, Level.Finest, "Validating the built-in Parameters.");
    setContext((UtilContext) getParamValue(aUtilContext));
    pReleaseOnError = getBooleanParamValue(new ParamMapping(aReleaseOnError));
    pActionOnExistance = 
      ActionOnExistance.valueOf(getStringParamValue(new ParamMapping(aActionOnExistance)));
  }
  
  /**
   * Adds a {@link SetupPass} to the current Builder.
   * 
   * @throws PipelineException
   *         If a null SetupPass is passed in.
   */
  public final void
  addSetupPass
  (
    SetupPass pass
  ) 
    throws PipelineException
  {
    if (pass == null)
      throw new PipelineException("Cannot add a null SetupPass");
    pFirstLoopPasses.add(pass);
    pRemainingFirstLoopPasses.add(pass);
    pLog.log(Kind.Bld, Level.Fine, 
      "Adding a SetupPass named (" + pass.getName() + ") to the " +
      "Builder identified by (" + getPrefixedName() + ")");
  }
  
  /**
   * Adds a {@link ConstructPass} to the current Builder.
   * 
   * @throws PipelineException
   *         If a null ConstructPass is passed in or if an attempt is made to add a pass which
   *         has already been added.
   */
  public final void
  addConstuctPass
  (
    ConstructPass pass
  ) 
    throws PipelineException
  {
    if (pass == null)
      throw new PipelineException("Cannot add a null ConstructPass");
    pSecondLoopPasses.add(pass);
    if (!pBuilderInformation.addConstuctPass(pass, this))
      throw new PipelineException
        ("The attempt to add the Construct Pass (" + pass.getName() + ") to " +
         "the Builder identified with (" + this.getPrefixedName() + ") was invalid.  " +
         "That exact pass already existed in the table");
    
    pLog.log(Kind.Bld, Level.Fine, 
      "Adding a ConstructPass named (" + pass.getName() + ") to the " +
      "Builder identified by (" + getPrefixedName() + ")");
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
    if (sPassDependencies.containsKey(targetPass)) {
      PassDependency pd = sPassDependencies.get(targetPass);
      pd.addSource(sourcePass);
    }
    else {
      PassDependency pd = 
	new PassDependency(targetPass, ComplexParam.listFromObject(sourcePass));
      sPassDependencies.put(targetPass, pd);
    }
  }
  
  /**
   * Runs all the SetupPasses.
   * <p>
   * It will run each SetupPass, then run this method recursively for all the child Builders
   * that exist. Once they finish running, it will return and this Builder will run its next
   * SetupPass. This will continue until there are no more SetupPasses to run.
   */
  private final void
  executeFirstLoop()
    throws PipelineException
  {
    for (SetupPass pass : pFirstLoopPasses) {
      assignCommandLineParams(this);
      for (BaseNames namers : pSubNames.values())
	assignCommandLineParams(namers);
      pass.run();
      Iterator<String> subBuilders = pSubBuilders.keySet().iterator();
      while (subBuilders.hasNext()) {
	String subBuilderName = subBuilders.next();
	initializeSubBuilder(subBuilderName);
	BaseBuilder subBuilder = pSubBuilders.get(subBuilderName);
	subBuilder.executeFirstLoop();
	subBuilders.remove();
	pPreppedBuilders.put(subBuilderName, subBuilder);
      }
      pCurrentPass++;
    }
    pBuilderInformation.addToCheckinList(this);
  }
  
  /**
   * Creates an execution order for all the ConstructPasses.
   * <p>
   * Loops through all the ConstructPasses that have been registered. Those that have
   * dependencies as set by {@link #addPassDependency(ConstructPass, ConstructPass)} will be
   * deferred until their dependencies have run.
   * <p>
   * Users should never need to call this method.  It is only public to facilitate GUI
   * interaction.
   * 
   * @throws PipelineException
   *         If there is no way to resolve the execution order due to unresolvable
   *         dependencies.
   */
  private final void
  buildSecondLoopExecutionOrder() 
    throws PipelineException
  {
    pLog.log(Kind.Bld, Level.Finer, "Building the Second Loop Execution Order.");
    ArrayList<PassDependency> dependencies = new ArrayList<PassDependency>();
    pExecutionOrder = new ArrayList<ConstructPass>();  
    
    for (ConstructPass pass : pBuilderInformation.getAllConstructPasses()) {
      if (!sPassDependencies.containsKey(pass)) {
	pExecutionOrder.add(pass);
	pLog.log(Kind.Bld, Level.Finest, 
	  "Adding (" + pass.toString() + ") to the execution order.");
      }
      else
	dependencies.add(sPassDependencies.get(pass));
    }
    
    int number = dependencies.size();
    while (number > 0) {
      Iterator<PassDependency> iter = dependencies.iterator();
      while (iter.hasNext()) {
	PassDependency pd = iter.next();
	boolean ready = true; 
	LinkedList<ConstructPass> depends = pd.getSources();
	for (ConstructPass depend : depends) {
	  if (!pExecutionOrder.contains(depend)) {
	    ready = false;
	    continue;
	  }
	}
	if (ready) {
	  pExecutionOrder.add(pd.getTarget());
	  iter.remove();
	  pLog.log(Kind.Bld, Level.Finest, 
	    "Adding (" + pd.toString() + ") to the execution order.");
	}
      }
      int newNumber = dependencies.size();
      if (newNumber >= number) {
	String message = "Unable to proceed, due to conflicts in builder dependencies.  " +
			 "None of the following passes can be scheduled to run due to " +
			 "conflicts:\n";
	for (PassDependency pd : dependencies)
	  message += pd.toString() + "\n"; 
	throw new PipelineException(message);
      }
      number = newNumber;
    }
  }
  
  public final List<ConstructPass>
  getExecutionOrder()
  {
    return Collections.unmodifiableList(pExecutionOrder);
  }

  /**
   * Runs all the ConstructPasses in the order that was determined by
   * {@link #buildSecondLoopExecutionOrder()}.
   */
  private final void
  executeSecondLoop()
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Fine, "Beginning execution of ConstructPasses.");
    for (ConstructPass pass : pExecutionOrder)
      pass.run();
  }
  
  /**
   * Check-ins all the nodes that have been mentioned for check-in.
   */
  private final void 
  executeCheckIn()
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Info, "Beginning execution of the check-ins.");
    for (BaseBuilder builder : pBuilderInformation.getCheckinList()) {
      if (builder.performCheckIn()) {
	builder.checkInNodes(getNodesToCheckIn(), VersionID.Level.Minor, getCheckInMessage());
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * This method can be over-ridden to change the check-in message that is associated with
   * this Builder.
   */
  protected String
  getCheckInMessage()
  {
    return "Node tree created using the (" + getName() + ") Builder";
  }

  /**
   * This method needs to be overriden to return a list of nodes that this Builder will
   * check-in.
   */
  protected abstract TreeSet<String>
  getNodesToCheckIn();
  
  /**
   * Should this builder check-in the nodes that it has specified with
   * {@link #getNodesToCheckIn()}.
   */
  protected boolean
  performCheckIn()
  {
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   B U I L D E R   U T I L I T I E S                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private final void
  neededNode
  (
    String nodeName
  ) 
    throws PipelineException
  {
    pLog.log(Kind.Bld, Level.Finer, "Getting a needed node (" + nodeName + ")");
    boolean exists = nodeExists(nodeName);
    if (!exists)
      throw new PipelineException
        ("The needed node (" + nodeName + ") for builder (" + getPrefixedName() + ") " +
         "does not exist.  Stopping execution.");
    NodeLocation location = getNodeLocation(nodeName);
    switch(location) {
    case OTHER:
      throw new PipelineException
        ("The node (" + nodeName + ") exists, but in a different working area and was " +
         "never checked in.  The Builder is aborting due to this problem.");
    case LOCAL:
      if (pActionOnExistance.equals(ActionOnExistance.CheckOut)) {
	pClient.checkOut(getAuthor(), getView(), nodeName, null, 
	  CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	pLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
      }
      break;
    case REP:
      pClient.checkOut(getAuthor(), getView(), nodeName, null, 
	CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      pLog.log(Kind.Bld, Level.Finest, "Checking out the node.");
      break;
    default:
      break;
    }
  }
  
  protected final boolean
  checkExistance
  (
    String nodeName
  ) 
    throws PipelineException
  {
    if (nodeName == null)
      return false;
    pLog.log(Kind.Ops, Level.Finer, "Checking for existance of the node (" + nodeName + ")");
    boolean exists = nodeExists(nodeName);
    if (!exists) 
      return false;
    pLog.log(Kind.Ops, Level.Finest, "The node exists.");
    if (pActionOnExistance.equals(ActionOnExistance.Abort))
      throw new PipelineException
        ("The node (" + nodeName + ") exists.  Aborting Builder operation as per " +
         "the setting of the ActionOnExistance parameter for the builder " +
         "( " + getName() +  " )");
    NodeLocation location = getNodeLocation(nodeName);
    switch(location) {
    case OTHER:
      throw new PipelineException
        ("The node (" + nodeName + ") exists, but in a different working area and was " +
         "never checked in.  The Builder is aborting due to this problem.");
    case LOCALONLY:
      return true;
    case LOCAL:
      switch(pActionOnExistance) {
      case CheckOut:
	 pClient.checkOut(getAuthor(), getView(), nodeName, null, 
	   CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	 pLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
	return true;
      case Continue:
	return true;
      }
    case REP:
      switch(pActionOnExistance) {
      case CheckOut:
	 pClient.checkOut(getAuthor(), getView(), nodeName, null, 
           CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	 pLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
	 return true;
      case Continue:
	throw new PipelineException
          ("The node (" + nodeName + ") exists, but is not checked out in the current " +
           "working area.  Since ActionOnExistance was set to Continue, " +
           "the Builder is unable to procede.");
      }
    }
    return false;
  }
  
  /**
   * Disable the Actions for all the nodes that have been specified for disabling by the
   * {@link #addToDisableList(String)}.
   */
  protected final void 
  disableActions() 
    throws PipelineException
  {
    for(String nodeName : pNodesToDisable) {
      disableAction(nodeName);
    }
  }

  private final LinkedList<QueueJobGroup> 
  queueJobs() 
  {
    return queueNodes(pBuilderInformation.getQueueList());
  }

  protected final LinkedList<QueueJobGroup>
  queueNodes
  (
     TreeSet<String> nodes
  )
  {
    pLog.log(Kind.Ops, Level.Fine, "Queuing the following nodes " + nodes );
    LinkedList<QueueJobGroup> toReturn = new LinkedList<QueueJobGroup>();
    for(String nodeName : nodes) {
      try {
	toReturn.addAll(pClient.submitJobs(getAuthor(), getView(), nodeName, null));
      }
      catch(PipelineException ex) {
	pLog.log(Kind.Ops, Level.Warning, 
	  "No job was generated for node ("+nodeName+")\n" + ex.getMessage()); 
      }
    }
    return toReturn;
  }
  
  protected final JobsState 
  areJobsFinished
  (
    LinkedList<QueueJobGroup> queueJobs
  ) 
    throws PipelineException
  {
    boolean done = true;
    int finished = 0;
    int waiting = 0;
    int running = 0;
    int total = 0;
    for(QueueJobGroup job : queueJobs) {
      TreeSet<Long> stuff = new TreeSet<Long>();
      stuff.add(job.getGroupID());
      TreeMap<Long, JobStatus> statuses = pQueue.getJobStatus(stuff);
      for(JobStatus status : statuses.values()) {
	total++;
	String nodeName = status.getNodeID().getName();
	pLog.log(Kind.Ops, Level.Finest, 
	  "Checking the status of Job (" + status.getJobID() + ") " +
	  "for node (" + nodeName + ").");
	JobState state = status.getState();
	switch(state) {
	case Failed:
	case Aborted:
	  pLog.log(Kind.Ops, Level.Finest, "\tThe Job did not completely successfully"); 
	  return JobsState.Problem;
	case Paused:
	case Preempted:
	case Queued:
	  pLog.log(Kind.Ops, Level.Finest, "\tThe Job has not started running."); 
	  done = false;
	  waiting++;
	  break;
	case Running:
	  pLog.log(Kind.Ops, Level.Finest, "\tThe Job is still running."); 
	  done = false;
	  running++;
	  break;
	case Finished:
	  finished++;
	  break;
	}
      }
    }
    pLog.log(Kind.Ops, Level.Fine, 
      "Out of (" + total + ") total jobs, (" + finished + ") are finished, " +
      "(" + running + ") are running, and (" + waiting + ") are waiting.");
    if (!done)
	return JobsState.InProgress;
    return JobsState.Complete;
  }
  
  protected final void
  waitForJobs
  (
    LinkedList<QueueJobGroup> jobs
  ) 
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Fine, "Waiting for the jobs to finish");
    do {
      JobsState state = areJobsFinished(jobs);
      if(state.equals(JobsState.InProgress)) {
        try {
          pLog.log(Kind.Ops, Level.Finer, 
            "Sleeping for 7 seconds before checking jobs again.");
          Thread.sleep(7000);
        }
	catch(InterruptedException e) {
          e.printStackTrace();
        }
      }
      else
	break;
    } while(true);
  } 

  /**
   * Returns a boolean that indicates whether all the node trees that were queued are in
   * the Finished state.
   * 
   * @return <code>true</code> if all the node trees are in the Finished state.
   *         Otherwise <code>false</code> is returned.
   * @throws PipelineException
   */
  protected final boolean 
  didTheNodesFinishCorrectly
  (
    TreeSet<String> queuedNodes
  ) 
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Finer, "Checking if all the queued nodes finished correctly");
    boolean toReturn = true;
    for(String nodeName : queuedNodes) {
      NodeStatus status = pClient.status(getAuthor(), getView(), nodeName);
      toReturn = getTreeState(status);
      if(!toReturn)
	break;
    }
    return toReturn;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  protected final void 
  addToQueueList
  (
    String nodeName
  )
  {
    pBuilderInformation.addToQueueList(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding node (" + nodeName + ") to queue list in Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  removeFromQueueList
  (
    String nodeName
  )
  {
    pBuilderInformation.removeFromQueueList(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Removing node (" + nodeName + ") from queue list in " +
      "Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  addToDisableList
  (
    String nodeName
  )
  {
    pNodesToDisable.add(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding node (" + nodeName + ") to disable list in " +
      "Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  removeFromDisableList
  (
    String nodeName
  )
  {
    pNodesToDisable.remove(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Removing node (" + nodeName + ") from disable list in " +
      "Builder (" + getPrefixedName() + ").");
  }
  
  protected final void 
  addToCheckInList
  (
    String nodeName
  )
  {
    pNodesToCheckIn.add(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding node (" + nodeName + ") to check-in list in " +
      "Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  removeFromCheckInList
  (
    String nodeName
  )
  {
    pNodesToCheckIn.remove(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Removing node (" + nodeName + ") from check-in list in " +
      "Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  clearQueueList()
  {
    pBuilderInformation.clearQueueList();
    pLog.log(Kind.Bld, Level.Finest, 
      "Clearing queue list in Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  clearDisableList()
  {
    pNodesToDisable.clear();
    pLog.log(Kind.Bld, Level.Finest, 
      "Clearing disable list in Builder (" + getPrefixedName() + ").");
  }

  protected final TreeSet<String> 
  getDisableList()
  {
    return new TreeSet<String>(pNodesToDisable);
  }
  
  protected final TreeSet<String> 
  getCheckInList()
  {
    return new TreeSet<String>(pNodesToCheckIn);
  }
  
  public final int
  getCurrentPass()
  {
    return pCurrentPass;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  G U I   S P E C I F I C   M E T H O D S                                               */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the next setup pass for the builder to run in GUI mode.
   * <P>
   * Users should never have to call this method.
   * 
   * @param first
   *        Is this the first time we're asking for a Setup Pass?
   */
  public final synchronized boolean
  getNextSetupPass(boolean first) 
    throws PipelineException
  {
    pNextSetupPass = null;
    
    if (pNextBuilder != null)
      pCurrentBuilder = pNextBuilder;
    
    int totalPasses = pCurrentBuilder.pFirstLoopPasses.size();
    int remainingPasses = pCurrentBuilder.pRemainingFirstLoopPasses.size();
    
    // This means that current builder has the next pass to get run.
    if (totalPasses == remainingPasses && totalPasses > 0) {
      if (first) {
	assignCommandLineParams(pCurrentBuilder);
	for (BaseNames namer : pCurrentBuilder.getNamers().values())
	  assignCommandLineParams(namer);
      }
      pNextSetupPass = pCurrentBuilder.pRemainingFirstLoopPasses.poll();
      if (pCurrentBuilder.pSubBuilders.size() > 0) {
	initNextBuilder();
      }
    }
    // This means the current builder is out of passes, so look at the first subbuilder.
    else if (pCurrentBuilder.pSubBuilders.size() > 0) {
      initNextBuilder();
      getNextSetupPass(first);
    }
    // This means the current builder has passes left.
    else if (pCurrentBuilder.pRemainingFirstLoopPasses.size() > 0) {
      assignCommandLineParams(pCurrentBuilder);
      for (BaseNames namer : pCurrentBuilder.getNamers().values())
	assignCommandLineParams(namer);
      pNextSetupPass = pCurrentBuilder.pRemainingFirstLoopPasses.poll();
      if (pCurrentBuilder.pSubBuilders.size() > 0) {
	initNextBuilder();
      }
    }
    // No passes and no children
    else {
      // Are we in the middle of nested builders
      if (pBuilderInformation.getCallHierarchySize() > 0) {
	BaseBuilder parent = pBuilderInformation.pollCallHierarchy();
	parent.pSubBuilders.remove(pCurrentBuilder.getName());
	parent.pPreppedBuilders.put(pCurrentBuilder.getName(), pCurrentBuilder);
	pBuilderInformation.addToCheckinList(parent);
	pNextBuilder = parent;
	getNextSetupPass(first);
      }
    }
    return (pNextSetupPass != null);
  }

  private void 
  initNextBuilder()
    throws PipelineException
  {
    pBuilderInformation.addToCallHierarchy(pCurrentBuilder);
    String nextName = new TreeSet<String>(pCurrentBuilder.pSubBuilders.keySet()).first();
    pNextBuilder = pCurrentBuilder.pSubBuilders.get(nextName);
    assignCommandLineParams(pNextBuilder);
    for (BaseNames namer : pNextBuilder.getNamers().values())
      assignCommandLineParams(namer);
    if (!pNextBuilder.pInitialized) {
      pCurrentBuilder.initializeSubBuilder(nextName);
      pNextBuilder.pInitialized = true;
    }
  }
  
  public final synchronized void
  runNextSetupPass() 
    throws PipelineException
  {
    pNextSetupPass.run();
    pCurrentBuilder.pCurrentPass++;
  }
  
  public final BaseBuilder
  getCurrentBuilder()
  {
    return pCurrentBuilder;
  }
  
  public final SetupPass
  getCurrentSetupPass()
  {
    return pNextSetupPass;
  }
  
  public boolean
  releaseOnError()
  {
    return pReleaseOnError;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S H U T D O W N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void
  disconnectClients()
  {
    pClient.disconnect();
    pQueue.disconnect();
  }
  
  public void
  releaseNodes() 
    throws PipelineException
  {
    BaseStage.cleanUpAddedNodes(pClient, pBuilderInformation.getStageInformation());
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T Y   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  public <E> boolean
  addNonNullValue
  (
    E value,
    Set<E> set
  )
  {
    if (value == null)
      return false;
    return set.add(value);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /*
   * Parameter names.
   */
  public final static String aReleaseOnError = "ReleaseOnError";
  public final static String aActionOnExistance = "ActionOnExistance";
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  protected BuilderInformation pBuilderInformation;
  
  /**
   * Should the builder release all the registered nodes if it fails.
   */
  private boolean pReleaseOnError;
  
  /**
   * A list of node names whose actions need to be disabled. 
   */
  private TreeSet<String> pNodesToDisable = new TreeSet<String>();
  
  /**
   * A list of nodes which need to be checked in.
   */
  private TreeSet<String> pNodesToCheckIn = new TreeSet<String>();

  /**
   * The list of all associated subBuilders
   */
  private TreeMap<String, BaseBuilder> pSubBuilders;
  
  private TreeMap<String, BaseBuilder> pPreppedBuilders;
  
  private TreeMap<String, BaseNames> pSubNames;
  
  private TreeMap<String, BaseNames> pGeneratedNames;
  
  private ArrayList<SetupPass> pFirstLoopPasses;
  
  private ArrayList<ConstructPass> pSecondLoopPasses;

  private int pCurrentPass;
  
  private ActionOnExistance pActionOnExistance;
  
  private ArrayList<ConstructPass> pExecutionOrder;
  
  private ListMap<ConstructPass, PassDependency> sPassDependencies = 
    new ListMap<ConstructPass, PassDependency>();

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  G U I   S P E C I F I C   I N T E R N A L S                                           */
  /*----------------------------------------------------------------------------------------*/

  private BaseBuilder pCurrentBuilder;
  
  private BaseBuilder pNextBuilder;
  
  private boolean pInitialized;
  
  /**
   * List of {@link SetupPass SetupPasses} which have not been run yet. 
   */
  private LinkedList<SetupPass> pRemainingFirstLoopPasses;
  
  private SetupPass pNextSetupPass;
  
  private JBuilderParamDialog pGuiDialog;

  private boolean pJobsFinishedCorrectly;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  E N U M E R A T I O N S                                                               */
  /*----------------------------------------------------------------------------------------*/

  protected 
  enum JobsState
  {
    Complete, Problem, InProgress
  }
  
  public static 
  enum ActionOnExistance
  {
    CheckOut, Continue, Abort;
    
    public static ArrayList<String>
    getStringList()
    {
      ArrayList<String> toReturn = new ArrayList<String>();
      for (ActionOnExistance each : ActionOnExistance.values())
	toReturn.add(each.toString());
      return toReturn;
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  P A S S   S U B C L A S S E S                                                         */
  /*----------------------------------------------------------------------------------------*/

  public 
  class SetupPass
    extends Described
  {
    public SetupPass
    (
      String name,
      String desc
    )
    {
      super(name, desc);
    }
    
    @SuppressWarnings("unused")
    public void 
    validatePhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, 
	"Stub validate phase in the " + pName + ".");      
    }
    
    @SuppressWarnings("unused")
    public void 
    gatherPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, 
	"Stub gather phase in the " + pName + ".");
    }
    
    @SuppressWarnings("unused")
    public void
    initPhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, 
	"Stub init phase in the " + pName + ".");
    }

    public final void
    run()
      throws PipelineException
    {
      TreeSet<String> listOfNames = new TreeSet<String>(pSubNames.keySet());
      for(String name : listOfNames) {
	BaseNames names = pSubNames.get(name);
	initializeSubBuilder(name);
	names.generateNames();
	pSubNames.remove(name);
	pGeneratedNames.put(name, names);
      }
      validatePhase();
      gatherPhase();
      initPhase();
    }
    
    public String
    toString()
    {
      return pDescription;
    }
    
    private static final long serialVersionUID = -2836639845295302403L;
  }
  
  public 
  class ConstructPass
    extends Described
  {
    public 
    ConstructPass
    (
      String name,
      String desc
    )
    {
      super(name, desc);
    }
    
    @SuppressWarnings("unused")
    public TreeSet<String>
    preBuildPhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, 
	"Stub preBuild phase in the " + pName + ".");
      return new TreeSet<String>();
    }
    
    @SuppressWarnings("unused")
    public void
    buildPhase()
      throws PipelineException
    {
      pLog.log(Kind.Ops, Level.Finest, 
	"Stub build phase in the " + pName + ".");
    }
    
    public TreeSet<String>
    nodesDependedOn()
    {
      return new TreeSet<String>();
    }
    
    public final void
    run()
      throws PipelineException
    {
      TreeSet<String> neededNodes = preBuildPhase();
      if (neededNodes.size() > 0) {
	LinkedList<QueueJobGroup> jobs = queueNodes(neededNodes);
	waitForJobs(jobs);
	if (!didTheNodesFinishCorrectly(neededNodes))
	  throw new PipelineException("The jobs did not finish correctly");
      }
      for (String needed : this.nodesDependedOn())
	neededNode(needed);
      buildPhase();
    }
    
    public PrefixedName
    getParentBuilderName()
    {
      return pBuilderInformation.getBuilderFromPass(this).getPrefixedName();
    }
    
    private BaseBuilder
    getParentBuilder()
    {
      return pBuilderInformation.getBuilderFromPass(this);
    }
    
    @Override
    public String
    toString()
    {
      BaseBuilder builder = getParentBuilder();
      
      String message = "";
      if (builder != null)
	message += builder.getPrefixedName() + " : ";
      message += getName();
      
      return message;
    }
    
    @Override
    public boolean
    equals
    (
      Object that
    )
    {
      return this == that; 
    }
    
    private static final long serialVersionUID = 2397375949761850587L;
  }
  
  public static
  class PassName
  {
    public
    PassName
    (
      String passName,
      PrefixedName builderName
    )
    {
      pPassName = passName;
      pBuilderName = new PrefixedName(builderName);
    }
    
    public String
    getPassName()
    {
      return pPassName;
    }
    
    public PrefixedName
    getBuilderPath()
    {
      return new PrefixedName(pBuilderName);
    }
    
    private String pPassName;
    private PrefixedName pBuilderName;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  P R I V A T E   S U B C L A S S E S                                                   */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class PassDependency
  {
    public
    PassDependency
    (
      ConstructPass target,
      LinkedList<ConstructPass> source
    )
    {
      pTarget = target;
      if (source != null) {
	for (ConstructPass pass : source)
	  addSource(pass);
      }
      else
	pSources = new LinkedList<ConstructPass>(); 
    }
    
    public boolean
    hasSources()
    {
      if (pSources == null || pSources.isEmpty())
	return false;
      return true;
    }
    
    public ConstructPass 
    getTarget()
    {
      return pTarget;
    }

    public LinkedList<ConstructPass> 
    getSources()
    {
      return pSources;
    }
    
    public void 
    addSource
    (
      ConstructPass pass
    )
    {
      if (pSources == null)
	pSources = new LinkedList<ConstructPass>();
      if (!pSources.contains(pass))
	pSources.add(pass);
    }
    
    public String
    toString()
    {
      String message = pTarget.toString() + " Depends on: {";
      for (ConstructPass pass :  pSources) {
	message += pass.toString();
	if (!pSources.getLast().equals(pass))
	  message += ", ";
      }
      message += "}";
      return message;
    }
    
    private ConstructPass pTarget;
    private LinkedList<ConstructPass> pSources;
  }
  
  private
  class BuilderGuiThread
    extends Thread
  {
    BuilderGuiThread
    (
      BaseBuilder builder
    )
    {
      pBuilder = builder;
    }
    
    @Override
    public void 
    run() 
    {  
      UIFactory.initializePipelineUI();
      try {
	getNextSetupPass(true);
	pGuiDialog = new JBuilderParamDialog(pBuilder);
	pGuiDialog.initUI();
	pGuiDialog.setVisible(true);
      }
      catch (PipelineException e) {
	System.err.println("Problem initializing builder in gui mode.\n" + e.getMessage());
	System.exit(1);
      }
    }
    
    private BaseBuilder pBuilder;
  }
  
  public class
  ExecutionOrderThread
    extends Thread
  {
    @Override
    public void 
    run() 
    {
      try {
	buildSecondLoopExecutionOrder();
	SwingUtilities.invokeLater(pGuiDialog.new PrepareConstructPassesTask());
      }
      catch (PipelineException ex) {
	pGuiDialog.handleException(ex);
      }
    }
  }
  
  public 
  class CheckinTask
    extends Thread
  {
    @Override
    public void
    run()
    {
      try {
	executeCheckIn();
	SwingUtilities.invokeLater(new AfterCheckinTask());
      }
      catch (PipelineException ex) {
	pGuiDialog.handleException(ex);
      }
    }
  }
  
  public
  class QueueThread
    extends Thread
  {
    @Override
    public void
    run()
    {
      try {
	waitForJobs(queueJobs());
	pJobsFinishedCorrectly = didTheNodesFinishCorrectly(pBuilderInformation.getQueueList());
	SwingUtilities.invokeLater(new AfterQueueTask());
      }
      catch (PipelineException ex) {
	pGuiDialog.handleException(ex);
      }
      
    }
  }
  
  public
  class AfterCheckinTask
    extends Thread
  {
    @Override
    public void
    run()
    {
      pGuiDialog.reallyFinish();
    }
  }
  
  public class AfterQueueTask
  extends Thread
  {
    @Override
    public void 
    run()
    {
      pGuiDialog.afterQueue(pJobsFinishedCorrectly); 
    }
  }
}
