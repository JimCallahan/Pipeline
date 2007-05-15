package us.temerity.pipeline.builder;

import java.util.*;

import javax.swing.SwingUtilities;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.stages.BaseStage;
import us.temerity.pipeline.builder.ui.JBuilderParamDialog;
import us.temerity.pipeline.ui.UIFactory;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The parent class of all Builder.
 * <p>
 * Stuff here.
 * <p>
 * Terminology used within these java docs.
 * <dl>
 * <dt>Builder</dt>
 * <dd>Refers to any instance of this class. Usually preceded with 'parent' when being used
 * with Sub-Builder to make the relation clear. </dd>
 * <dt>Sub-Builder</dt>
 * <dd>Refers to any instance of {@link HasBuilderParams} that is being used as a child of
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
  extends HasBuilderParams
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected
  BaseBuilder
  (
    String name,
    String desc
  )
    throws PipelineException
  {
    super(name, desc, true);
    pSubBuilders = new TreeMap<String, BaseBuilder>();
    pSubNames = new TreeMap<String, BaseNames>();
    pPreppedBuilders = new TreeMap<String, BaseBuilder>();
    pGeneratedNames = new TreeMap<String, BaseNames>();
    pFirstLoopPasses = new ArrayList<SetupPass>();
    pSecondLoopPasses = new ArrayList<ConstructPass>();
    {
      UtilContext context = BaseUtil.getDefaultUtilContext();
      BuilderParam param = 
      new UtilContextBuilderParam
      (aUtilContext, 
       "The User, View, and Toolset to perform these actions in.", 
       context);
      setGlobalContext(context);
      addParam(param);
    }
    {
      BuilderParam param = 
	new EnumBuilderParam
	(aActionOnExistance,
	 "What action should the Builder take when a node already exists.",
	 ActionOnExistance.Continue.toString(),
	 ActionOnExistance.getStringList());
      addParam(param);
    }
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aReleaseOnError,
	 "Release all the created nodes if an exception is thrown.", 
	 false);
      addParam(param);
    }
    pCurrentPass = 1;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds a Sub-Builder to the current Builder, with the given Builder Parameter mapping.
   * 
   * @throws PipelineException
   *         If an attempt is made to add a Sub-Builder with the same name as on that already
   *         exists.
   */
  public final void 
  addSubBuilder
  (
    HasBuilderParams subBuilder, 
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
    
    if (paramMapping != null)
      addMappedParams(instanceName, paramMapping);

    PrefixedName prefixed = new PrefixedName(getPrefixedName(), instanceName);
    subBuilder.setPrefixedName(prefixed);
    
    sLog.log(Kind.Bld, Level.Fine, 
      "Adding a SubBuilder with instanceName (" + prefixed.toString() + ") to " +
      "Builder (" + getName() + ").");
    
    if (subBuilder instanceof BaseNames)
      pSubNames.put(instanceName, (BaseNames) subBuilder);
    else {
      pSubBuilders.put(instanceName, (BaseBuilder) subBuilder);
    }
  }

  /**
   * Adds a Sub-Builder to the current Builder.
   * 
   * @throws PipelineException
   *         If an attempt is made to add a Sub-Builder with the same name as on that already
   *         exists.
   */
  public final void 
  addSubBuilder
  (
    HasBuilderParams subBuilder
  ) 
    throws PipelineException
  {
    addSubBuilder(subBuilder, new TreeMap<ParamMapping, ParamMapping>());
  }
  
  /**
   * Gets a mapping of all the Sub-Builders, keyed by the names of the Sub-Builders.
   */
  public TreeMap<String, BaseBuilder>
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
  public HasBuilderParams 
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

  /**
   * Gets a Sub-Builder that has not been initialized yet.
   * <p>
   * This means either a child Builder before its Setup Passes were run or a child Namer
   * before generateNames is run.
   */
  public HasBuilderParams 
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
  public void
  addMappedParam
  (
    String subBuilderName,
    ParamMapping subParam,
    ParamMapping masterParam
  ) 
    throws PipelineException
  {
    HasBuilderParams subBuilder = getSubBuilder(subBuilderName);
    
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

    sLog.log(Kind.Bld, Level.Finer, 
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
  public void
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
  public void
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
  public void
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
    HasBuilderParams subBuilder = getNewSubBuilder(name);
    if (subBuilder == null)
      throw new PipelineException
        ("Somehow a SubBuilder/Name with the name (" + name + ") was submited for " +
         "initialization by the Builder named (" + getName() + ").\n" +
         "This SubBuilder does not exists.\n" +
      	 "This exception most likely represents a fundamental problem with the Builder " +
      	 "backend and should be reported to Temerity.");
    SortedMap<ParamMapping, ParamMapping> paramMapping = subBuilder.getMappedParams();
    sLog.log(Kind.Ops, Level.Fine, "Initializing the subBuilder (" + name + ").");
    for (ParamMapping subParamMapping : paramMapping.keySet()) {
      ParamMapping masterParamMapping = paramMapping.get(subParamMapping);
      
      BuilderParam subParam = subBuilder.getParam(subParamMapping);
      BuilderParam masterParam = this.getParam(masterParamMapping);
      
      assert (subParam != null) : "The subParam value should never be null.";
      assert (masterParam != null) : "The masterParam value should never be null.";
      
      subBuilder.setParamValue(subParamMapping, 
	((SimpleParamAccess) masterParam).getValue());
      sLog.log(Kind.Ops, Level.Finer, 
	"Mapped param (" + subParamMapping + ") in subBuilder " + 
	"(" + name + ") to value from param (" + masterParamMapping + ") " + 
	"in builder (" + getName() + ")");
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
    if (sUsingGUI == true)
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
    sLog.log(Kind.Ops, Level.Fine, "Starting the command line execution.");
    boolean finish = false;
    try {
      executeFirstLoop();
      buildSecondLoopExecutionOrder();
      executeSecondLoop();
      waitForJobs(queueJobs());
      finish = didTheNodesFinishCorrectly(sNodesToQueue);
    }
    catch (Exception ex) {
      String logMessage = "An Exception was thrown during the course of execution.\n";
      logMessage += ex.getMessage() + "\n";
      if (pReleaseOnError) 
	logMessage += "All the nodes that were registered will now be released.";
      sLog.log(Kind.Ops, Level.Severe, logMessage);
      if (pReleaseOnError) 
	BaseStage.cleanUpAddedNodes();
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
    SwingUtilities.invokeLater(new BuilderGuiThread(this));
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
  public void 
  validateBuiltInParams() 
    throws PipelineException 
  {
    sLog.log(Kind.Bld, Level.Finest, "Validating the built-in Parameters.");
    setGlobalContext((UtilContext) getParamValue(aUtilContext));
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
    sLog.log(Kind.Bld, Level.Fine, 
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
    if (sPassToBuilderMap.put(pass, this) != null) 
      throw new PipelineException
        ("The attempt to add the Construct Pass (" + pass.getName() + ") to " +
         "the Builder identified with (" + this.getPrefixedName() + ") was invalid.  " +
         "That exact pass already existed in the table");
    sAllConstructPasses.add(pass);
    
    sLog.log(Kind.Bld, Level.Fine, 
      "Adding a ConstructPass named (" + pass.getName() + ") to the " +
      "Builder identified by (" + getPrefixedName() + ")");
  }
  
  /**
   * Gets the Builder that is associated with a given {@link ConstructPass}.
   */
  protected BaseBuilder
  getBuilderFromPass
  (
    ConstructPass pass
  ) 
  {
    if (pass == null)
      throw new IllegalArgumentException("Cannot have a null ConstructPass.");
    return sPassToBuilderMap.get(pass);
  }

  /**
   * Creates a dependency between two ConstructPasses.
   * <p>
   * The target ConstructPass will not be run until the source ConstructPass has completed.
   * This allows for Builders to specify the order in which their passes run.
   */
  protected void
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
    sLog.log(Kind.Ops, Level.Fine, "Beginning execution of SetupPasses.");
    assignCommandLineParams();
    for (SetupPass pass : pFirstLoopPasses) {
      pass.run();
      for (String subBuilderName : pSubBuilders.keySet()) {
	initializeSubBuilder(subBuilderName);
	BaseBuilder subBuilder = pSubBuilders.get(subBuilderName);
	subBuilder.executeFirstLoop();
      }
      pCurrentPass++;
    }
  }
  
  /**
   * Creates an execution order for all the ConstructPasses.
   * <p>
   * Loops through all the ConstructPasses that have been registered. Those that have
   * dependencies as set by {@link #addPassDependency(ConstructPass, ConstructPass)} will be
   * deferred until their dependencies have run.
   * 
   * @throws PipelineException
   *         If there is no way to resolve the execution order due to unresolvable
   *         dependencies.
   */
  private final void
  buildSecondLoopExecutionOrder() 
    throws PipelineException
  {
    sLog.log(Kind.Bld, Level.Finer, "Building the Second Loop Execution Order.");
    ArrayList<PassDependency> dependencies = new ArrayList<PassDependency>();
    pExecutionOrder = new ArrayList<ConstructPass>();  
    
    for (ConstructPass pass : sAllConstructPasses) {
      if (!sPassDependencies.containsKey(pass)) {
	pExecutionOrder.add(pass);
	sLog.log(Kind.Bld, Level.Finest, 
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
	  sLog.log(Kind.Bld, Level.Finest, 
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

  /**
   * Runs all the ConstructPasses in the order that was determined by
   * {@link #buildSecondLoopExecutionOrder()}.
   */
  private final void
  executeSecondLoop()
    throws PipelineException
  {
    sLog.log(Kind.Ops, Level.Fine, "Beginning execution of ConstructPasses.");
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
    sLog.log(Kind.Ops, Level.Fine, "Beginning execution of the check-ins.");
    if (performCheckIn()) {
      checkInNodes(getNodesToCheckIn(), VersionID.Level.Minor, getCheckInMessage());
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
    sLog.log(Kind.Bld, Level.Finer, "Getting a needed node (" + nodeName + ")");
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
	sClient.checkOut(getAuthor(), getView(), nodeName, null, 
	  CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	sLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
      }
      break;
    case REP:
      sClient.checkOut(getAuthor(), getView(), nodeName, null, 
	CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      sLog.log(Kind.Bld, Level.Finest, "Checking out the node.");
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
    sLog.log(Kind.Ops, Level.Finer, "Checking for existance of the node (" + nodeName + ")");
    boolean exists = nodeExists(nodeName);
    if (!exists) 
      return false;
    sLog.log(Kind.Ops, Level.Finest, "The node exists.");
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
	 sClient.checkOut(getAuthor(), getView(), nodeName, null, 
	   CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	 sLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
	return true;
      case Continue:
	return true;
      }
    case REP:
      switch(pActionOnExistance) {
      case CheckOut:
	 sClient.checkOut(getAuthor(), getView(), nodeName, null, 
           CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	 sLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
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
    return queueNodes(sNodesToQueue);
  }

  protected final LinkedList<QueueJobGroup>
  queueNodes
  (
     TreeSet<String> nodes
  )
  {
    sLog.log(Kind.Ops, Level.Fine, "Queuing the following nodes " + nodes );
    LinkedList<QueueJobGroup> toReturn = new LinkedList<QueueJobGroup>();
    for(String nodeName : nodes) {
      try {
	toReturn.add(sClient.submitJobs(getAuthor(), getView(), nodeName, null));
      }
      catch(PipelineException ex) {
	sLog.log(Kind.Ops, Level.Warning, 
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
      TreeMap<Long, JobStatus> statuses = sQueue.getJobStatus(stuff);
      for(JobStatus status : statuses.values()) {
	total++;
	String nodeName = status.getNodeID().getName();
	sLog.log(Kind.Ops, Level.Finest, 
	  "Checking the status of Job (" + status.getJobID() + ") " +
	  "for node (" + nodeName + ").");
	JobState state = status.getState();
	switch(state) {
	case Failed:
	case Aborted:
	  sLog.log(Kind.Ops, Level.Finest, "\tThe Job did not completely successfully"); 
	  return JobsState.Problem;
	case Paused:
	case Preempted:
	case Queued:
	  sLog.log(Kind.Ops, Level.Finest, "\tThe Job has not started running."); 
	  done = false;
	  waiting++;
	  break;
	case Running:
	  sLog.log(Kind.Ops, Level.Finest, "\tThe Job is still running."); 
	  done = false;
	  running++;
	  break;
	case Finished:
	  finished++;
	  break;
	}
      }
    }
    sLog.log(Kind.Ops, Level.Fine, 
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
    sLog.log(Kind.Ops, Level.Fine, "Waiting for the jobs to finish");
    do {
      JobsState state = areJobsFinished(jobs);
      if(state.equals(JobsState.InProgress)) {
        try {
          sLog.log(Kind.Ops, Level.Finer, 
            "Sleeping for 5 seconds before checking jobs again.");
          Thread.sleep(5000);
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
    sLog.log(Kind.Ops, Level.Finer, "Checking if all the queued nodes finished correctly");
    boolean toReturn = true;
    for(String nodeName : queuedNodes) {
      NodeStatus status = sClient.status(getAuthor(), getView(), nodeName);
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
    sNodesToQueue.add(nodeName);
    sLog.log(Kind.Bld, Level.Finest, 
      "Adding node (" + nodeName + ") to queue list in Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  removeFromQueueList
  (
    String nodeName
  )
  {
    sNodesToQueue.remove(nodeName);
    sLog.log(Kind.Bld, Level.Finest, 
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
    sLog.log(Kind.Bld, Level.Finest, 
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
    sLog.log(Kind.Bld, Level.Finest, 
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
    sLog.log(Kind.Bld, Level.Finest, 
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
    sLog.log(Kind.Bld, Level.Finest, 
      "Removing node (" + nodeName + ") from check-in list in " +
      "Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  clearQueueList()
  {
    sNodesToQueue.clear();
    sLog.log(Kind.Bld, Level.Finest, 
      "Clearing queue list in Builder (" + getPrefixedName() + ").");
  }

  protected final void 
  clearDisableList()
  {
    pNodesToDisable.clear();
    sLog.log(Kind.Bld, Level.Finest, 
      "Clearing disable list in Builder (" + getPrefixedName() + ").");
  }

  protected final TreeSet<String> 
  getQueueList()
  {
    return new TreeSet<String>(sNodesToQueue);
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
  /*   P A R S E R   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  public static void
  setUsingGUI
  (
    boolean usingGUI
  )
  {
    sUsingGUI = usingGUI;
  }
  
  public static void
  setAbortOnBadParam
  (
    boolean abortOnBadParam
  )
  {
    sAbortOnBadParam = abortOnBadParam;
  }
  
  public static boolean
  getAbortOnBadParam()
  {
    return sAbortOnBadParam;
  }
  
  public static void
  setCommandLineParam
  (
    String builder, 
    LinkedList<String> keys, 
    String value
  )
  {
    sLog.log(Kind.Arg, Level.Finest, 
      "Reading command line arg for Builder (" + builder + ").\n" +
      "Keys are (" + keys + ").\n" +
      "Value is (" + value + ").");
    if (builder == null)
      throw new IllegalArgumentException
        ("Illegal attempt in setting a Parameter value before specifying the Builder " +
         "that the Parameter resides in.");
    LinkedList<String> list;
    if (keys == null)
      list = new LinkedList<String>();
    else
      list = new LinkedList<String>(keys);
    list.addFirst(builder);
    sCommandLineParams.putValue(list, value, true);
  }
  
  /**
   * Returns a {@link MultiMap} of all the command line parameters.
   * <p>
   * The first level in the MultiMap is made up of the names of all the builders, the second
   * level is all the parameter names, and every level after that (if they exist) are keys
   * into Complex Parameters.  Values are stored in the leaf nodes.
   */
  public static MultiMap<String, String>
  getCommandLineParams()
  {
    return sCommandLineParams;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
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

  /**
   * A list of nodes names that need to be queued.
   */
  private static TreeSet<String> sNodesToQueue = new TreeSet<String>();

  /*
   * Parameter names.
   */
  public final static String aUtilContext = "UtilContext";
  public final static String aReleaseOnError = "ReleaseOnError";
  public final static String aActionOnExistance = "ActionOnExistance";
  
  private static ListMap<ConstructPass, BaseBuilder> sPassToBuilderMap = 
    new ListMap<ConstructPass, BaseBuilder>();
  
  ListMap<ConstructPass, PassDependency> sPassDependencies = 
    new ListMap<ConstructPass, PassDependency>();
  
  private static LinkedList<ConstructPass> sAllConstructPasses = 
    new LinkedList<ConstructPass>();
  
  /**
   * 
   */
  private static MultiMap<String, String> sCommandLineParams = 
    new MultiMap<String, String>();
  
  /**
   * Is this Builder in GUI mode.
   */
  private static boolean sUsingGUI = false;
  
  private static boolean sAbortOnBadParam = false;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

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


  
  /*----------------------------------------------------------------------------------------*/
  /*  G U I   S P E C I F I C   I N T E R N A L S                                           */
  /*----------------------------------------------------------------------------------------*/

  private HasBuilderParams pCurrentBuilder;
  
  

  
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
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, 
	"Stub validate phase in the " + pName + ".");      
    }
    
    @SuppressWarnings("unused")
    public void 
    gatherPhase() 
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, 
	"Stub gather phase in the " + pName + ".");
    }
    
    @SuppressWarnings("unused")
    public void
    initPhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, 
	"Stub init phase in the " + pName + ".");
    }

    public final void
    run()
      throws PipelineException
    {
      TreeSet<String> listOfNames = new TreeSet<String>(pSubNames.keySet());
      for(String name : listOfNames) {
	BaseNames names = pSubNames.get(name);
	names.assignCommandLineParams();
	initializeSubBuilder(name);
	names.generateNames();
	pSubNames.remove(name);
	pGeneratedNames.put(name, names);
      }
      validatePhase();
      gatherPhase();
      initPhase();
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
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, 
	"Stub preBuild phase in the " + pName + ".");
      return new TreeSet<String>();
    }
    
    @SuppressWarnings("unused")
    public void
    buildPhase()
      throws PipelineException
    {
      sLog.log(Kind.Ops, Level.Finest, 
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
      return getBuilderFromPass(this).getPrefixedName();
    }
    
    private BaseBuilder
    getParentBuilder()
    {
      return getBuilderFromPass(this);
    }
    
    public String
    toString()
    {
      String message = "Pass (" + getName() + ") contained in Builder (";
      BaseBuilder builder = getParentBuilder();
      
      if (builder != null)
	message += builder.getPrefixedName();
      message += ")";
      return message;
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
    
    public void 
    run() 
    {  
      UIFactory.initializePipelineUI();
      JBuilderParamDialog main = new JBuilderParamDialog(pBuilder);
      main.initUI();
      main.setVisible(true);
    }
    
    private BaseBuilder pBuilder;
  }
}
