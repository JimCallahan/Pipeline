// $Id: BaseBuilder.java,v 1.37 2008/02/05 08:10:38 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import javax.swing.SwingUtilities;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.MultiMap.MultiMapNamedEntry;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.builder.ui.JBuilderParamDialog;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.stages.BaseStage;
import us.temerity.pipeline.ui.UIFactory;
import us.temerity.pipeline.ui.core.UIMaster;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The parent class of all Builders.
 * <p>
 * <h2>Introduction</h2>
 * Builders are Utilities designed to make creating applications that interact with Pipeline
 * node networks simpler to write and maintain. The primary focus of Builders is
 * programatically creating large and complicated node networks while needing minimal
 * information and intervention from a user. However, that is not the only use for Builders
 * which can be used in an situation that requires a framework for complicated access to node
 * networks.
 * <p>
 * Builders are designed to function in either command-line or graphical mode, with all UI
 * code being automatically generated from the builder's configuration. They are also designed
 * to be modular, allowing builders to be nested inside other builders to create higher level
 * builders quickly and easily.
 * <p>
 * <h2>Parameters</h2>
 * <p>
 * BaseBuilder declares two parameters in its constructor. Any class which inherits from
 * BaseBaseBuilder that is using parameters and parameter layouts will need to have a way to
 * account for these parameters as well as the parameters which come from {@link BaseUtil}.
 * <ul>
 * <li> ActionOnExistence - This parameter is used to control the behavior of the Builder when
 * a node it is supposed to build already exists.
 * <li> ReleaseOnError - This parameter controls the behavior of the Builder if it encounters
 * an error during the course of its execution. If this parameter is set to <code>true</code>,
 * an error will cause the Builder to release all the nodes that have been created during its
 * run.
 * </ul>
 * BaseBuilder also contains utility methods for creating two other parameters that a majority
 * of Builders may want to implement.
 * <ul>
 * 
 * <li> CheckinWhenDone - This parameter can be used to specify whether the Builder should
 * check in nodes it has created when it finishes execution. By default this parameter's value
 * is used in the {@link #performCheckIn()} method.
 * 
 * <li> SelectionKeys - This parameter is a list of all the Selection Keys that exist in the
 * Pipeline install, allowing the user of the Builder to specify a subset of them for some
 * purpose.  Usually this parameter is used to specify a list of keys that all nodes being
 * built will contain.
 * 
 * </ul>
 * <p>
 * <h2>Terminology used within these java docs.</h2>
 * <dl>
 * <dt>Builder</dt>
 * <dd>Refers to any instance of this class. Usually preceded with 'parent' when being used
 * with Sub-Builder to make the relation clear. </dd>
 * <dt>Sub-Builder</dt>
 *   <dd>Refers to any instance of {@link BaseUtil} that is being used as a child of the
 *       current Builder.</dd>
 * <dt>child Builder</dt>
 * <dd>Refers to any instance of this class that is being used as a child of the current
 * builder. The child Builders of a Builder are a subset of the Sub-Builders.</dd>
 * <dt>child Namer</dt>
 * <dd>Refers to any instance of {@link BaseNames} that is being used as a child of the
 * current builder. The child Namers of a Builder are a subset of the Sub-Builders.</dd>
 * <dt>New Sub-Builder
 * <dd>Refers to any Sub-Builder before it has been prepared by the First Loop, either by
 * executing the SetupPasses if it is a child Builder or by calling
 * {@link BaseNames#generateNames()} if it is a child Namer</dd>
 * <dt>First Loop
 * <dd>The period of Builder execution during which all the SetupPasses are run and child
 * Namers have {@link BaseNames#generateNames()} called.
 * <dt>Second Loop
 * <dd>The period of Builder execution during with all the ConstructPasses are run.
 * </dl>
 * <h2>Requirements</h2>
 * <p>
 * Builder operate under the following conditions.
 * <ul>
 * <li> Any Builder which is going to be run as a standalone builder (i.e., it can be launched
 * by itself) needs to have a constructor which matches the following form
 * <code>Constructor({@link MasterMgrClient}, {@link QueueMgrClient}, 
 * {@link BuilderInformation})</code>.  If such a constructor does not exist, then any attempts
 * to invoke the Builder from the command-line or from inside plui will fail.  In addition,
 * a Builder may have as many other constructors in whatever form, for when it is used as
 * a Child Builder.  A Builder which is only intended to be a Child Builder (see 
 * {@link ModelPiecesBuilder} for an example) does not have the same restrictions on its 
 * constructor.  A majority of the Temerity builder define at least one additional 
 * Constructor which take all the Namer classes that the Builder uses.  This makes it easy
 * to subclass the Builder and pass in different implementations of the Namer classes and 
 * makes it easy for parent Builder to have their own Namer classes which are passed to their
 * children.  In cases where this is true, it is very important to make use of the 
 * {@link BaseNames#isGenerated() isGenerated} method of Namers to ensure that they are
 * not displayed more than once.
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
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient);
    pBuilderInformation = builderInformation;
    pStageInfo = builderInformation.getNewStageInformation();
    pSubBuilders = new TreeMap<String, BaseBuilder>();
    pSubNames = new TreeMap<String, BaseNames>();
    pPreppedBuilders = new TreeMap<String, BaseBuilder>();
    pGeneratedNames = new TreeMap<String, BaseNames>();
    pFirstLoopPasses = new ArrayList<SetupPass>();
    pRemainingFirstLoopPasses = new LinkedList<SetupPass>();
    pSecondLoopPasses = new ArrayList<ConstructPass>();
    pLockBundles = new ArrayList<LockBundle>();
    {
      UtilityParam param = 
	new EnumUtilityParam
	(aActionOnExistence,
	 "What action should the Builder take when a node already exists.",
	 ActionOnExistence.Continue.toString(),
	 ActionOnExistence.titles());
      addParam(param);
    }
    {
      UtilityParam param = 
	new BooleanUtilityParam
	(aReleaseOnError,
	 "Release all the created nodes if an exception is thrown.", 
	 true);
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
  
  /**
   * Adds a Selection Key parameter to the current Builder.
   * <p>
   * This parameter is a list of all the Selection Keys that exist in the Pipeline install,
   * allowing the user of the Builder to specify a subset of them for some purpose. Usually
   * this parameter is used to specify a list of keys that all nodes being built will contain.
   * 
   * @throws PipelineException
   */
  protected final void
  addSelectionKeyParam()
    throws PipelineException
  {
    UtilityParam param = 
      ListUtilityParam.createSelectionKeyParam
      (aSelectionKeys, 
       "Which Selection Keys Should be assigned to the constructed nodes", 
       null,
       pQueue);
    addParam(param);
  }
  
  /**
   * Adds a Check-In when done parameter to the current Builder.
   * <p>
   * This parameter can be used to specify whether the Builder should check in nodes it has
   * created when it finishes execution. By default this parameter's value is used in the
   * {@link #performCheckIn()} method.
   * 
   * @throws PipelineException
   */
  protected final void
  addCheckinWhenDoneParam()
  {
    UtilityParam param = 
      new BooleanUtilityParam
      (aCheckinWhenDone,
       "Automatically check-in all the nodes when building is finished.", 
       false); 
    addParam(param);
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
	addMappedParam(instanceName, aActionOnExistence, aActionOnExistence);
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
       "The full attempted mapping was of " + masterParam+ " in the master " +
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
      addMappedParam(subBuilderName, sub, master);
    }
  }
  
  /**
   * Sets a null mapping for a parameter that will keep it from being displayed in the 
   * gui or from accepting a commandline value.
   */
  public final void
  disableParam
  (
    ParamMapping param
  )
  {
    addParamMapping(param, ParamMapping.NullMapping);
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
      
      if (masterParamMapping.equals(ParamMapping.NullMapping))
	continue;
      
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
  
  @SuppressWarnings("unchecked")
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
    ListMappedArrayList<String, MultiMapNamedEntry<String, String>> commandLineValues = 
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
	    pLog.log(Kind.Arg, Level.Finest, 
	      "Setting command line parameter (" + mapping + ") from builder " +
	      "(" + prefixName + ") with the value (" + value + ").");
	    if (utility.canSetSimpleParamFromString(mapping)) {
	      try {
		if (keys == null || keys.isEmpty()) {
		  SimpleParamFromString param = (SimpleParamFromString) utility.getParam(mapping);
		  param.fromString(value);
		}
		else {
		  ComplexParamAccess<UtilityParam> param = (ComplexParamAccess<UtilityParam>) utility.getParam(paramName);
		  param.fromString(keys, value);
		}
	      } 
	      catch (IllegalArgumentException ex) {
		String message = "There was an error setting the value of a Parameter " +
		  "from a command line argument.\n" + ex.getMessage(); 
		if (abort)
    		  throw new PipelineException(message);
		pLog.log(Kind.Arg, Level.Warning, message);
	      }
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
      checkActions();
      buildSecondLoopExecutionOrder();
      executeSecondLoop();
      waitForJobs(queueJobs());
      finish = areAllFinished(pBuilderInformation.getQueueList());
    }
    catch (Exception ex) {
      String logMessage = "An Exception was thrown during the course of execution.\n";
      logMessage += ex.getMessage() + "\n";
      if (pReleaseOnError) 
	logMessage += "All the nodes that were registered will now be released.";

      if (pReleaseOnError)
	BaseStage.cleanUpAddedNodes(pClient, pBuilderInformation.getStageState());

      throw new PipelineException(logMessage);
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
    pActionOnExistence = 
      ActionOnExistence.valueOf(getStringParamValue(new ParamMapping(aActionOnExistence)));
    pStageInfo.setActionOnExistence(pActionOnExistence);
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
  addConstructPass
  (
    ConstructPass pass
  ) 
    throws PipelineException
  {
    if (pass == null)
      throw new PipelineException("Cannot add a null ConstructPass");
    pSecondLoopPasses.add(pass);
    if (!pBuilderInformation.addConstructPass(pass, this))
      throw new PipelineException
        ("The attempt to add the Construct Pass (" + pass.getName() + ") to " +
         "the Builder identified with (" + this.getPrefixedName() + ") was invalid.  " +
         "That exact pass already existed in the table");
    
    pLog.log(Kind.Bld, Level.Fine, 
      "Adding a ConstructPass named (" + pass.getName() + ") to the " +
      "Builder identified by (" + getPrefixedName() + ")");
  }
  
  /**
   * Adds a {@link LockBundle} to the current Builder.
   * 
   * @throws PipelineException
   *   If a null LinkBundle is passed in.
   */
  public final void
  addLockBundle
  (
    LockBundle bundle  
  )
    throws PipelineException
  {
    if (bundle == null)
      throw new PipelineException("Cannot add a null LockBundle");
    pLockBundles.add(bundle);
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
    pBuilderInformation.addPassDependency(sourcePass, targetPass);
  }
  
  public ConstructPass
  getConstructPass
  (
    String name  
  )
  {
    ConstructPass toReturn = null;
    for (ConstructPass pass : pSecondLoopPasses) {
      if (pass.getName().equals(name)) {
	toReturn = pass;
	break;
      }
    }
    return toReturn;
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
  
  private final void
  checkActions()
    throws PipelineException
  {
    MappedArrayList<String, PluginContext> badPlugs = new MappedArrayList<String, PluginContext>();
    checkActionsHelper(badPlugs);
    if (badPlugs.size() > 0) {
      StringBuffer msg = new StringBuffer();
      for (String toolset: badPlugs.keySet()) {
	msg.append("Toolset: " + toolset + "\n");
	for (PluginContext plug : badPlugs.get(toolset))
	  msg.append("\t" + plug.toString() + "\n");
      }
      throw new PipelineException
        ("The following required plugins are missing from the toolsets.\n\n" + msg.toString());
    }
  }
  
  private final void
  checkActionsHelper
  (
    MappedArrayList<String, PluginContext> badPlugs
  )
    throws PipelineException
  {
    MappedArrayList<String, PluginContext> plugs = getNeededActions();
    if (plugs != null) {
      for (String toolset : plugs.keySet()) {
	ArrayList<PluginContext> needed = plugs.get(toolset);
	PluginSet toolsetPlugs =  pClient.getToolsetActionPlugins(toolset);
	for (PluginContext each : needed) {
	  Set<VersionID> ids = 
	    toolsetPlugs.getVersions(each.getPluginVendor(), each.getPluginName());
	  if (ids == null || ids.size() == 0) {
	    badPlugs.put(toolset, each);
	    continue;
	  }
	  Range<VersionID> range = each.getRange();
	  boolean found = false;
	  for (VersionID id : ids) {
	    if (range.isInside(id)) {
	      found = true;
	      break;
	    }
	  }
	  if (!found)
	    badPlugs.put(toolset, each);
	} // for (PluginContext each : needed)
      } // for (String toolset : plugs.keySet())
    }
    for (BaseBuilder sub : pPreppedBuilders.values()) {
      sub.checkActionsHelper(badPlugs);
    }
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
    
    Map<ConstructPass, PassDependency> passDependencies = 
      pBuilderInformation.getPassDependencies();
    
    for (ConstructPass pass : pBuilderInformation.getAllConstructPasses()) {
      if (!passDependencies.containsKey(pass)) {
	pExecutionOrder.add(pass);
	pLog.log(Kind.Bld, Level.Finest, 
	  "Adding (" + pass.toString() + ") to the execution order.");
      }
      else
	dependencies.add(passDependencies.get(pass));
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
	pLog.log(Kind.Ops, Level.Fine, 
	  "Beginning check-in for builder ("+ builder.getPrefixedName() + ").");
	builder.checkInNodes(builder.getNodesToCheckIn(), builder.getCheckinLevel(), builder.getCheckInMessage());
	if (builder.pLockBundles.size() > 0) {
	  pLog.log(Kind.Ops, Level.Fine, 
	    "Locking appropriate nodes for builder ("+ builder.getPrefixedName() + ").");
	  for (LockBundle bundle : builder.pLockBundles) {
	    for (String node : bundle.getNodesToLock())
	      lockLatest(node);
	    TreeSet<String> neededNodes = new TreeSet<String>(bundle.getNodesToCheckin());
	    LinkedList<QueueJobGroup> jobs = queueNodes(neededNodes);
	    waitForJobs(jobs);
	    if (!areAllFinished(neededNodes))
	      throw new PipelineException("The jobs did not finish correctly");
	    checkInNodes(bundle.getNodesToCheckin(), VersionID.Level.Micro, "The tree is now properly locked.");
	  }
	}
      }
      else
	pLog.log(Kind.Ops, Level.Fine, 
	  "Check-in was not activated for builder (" + builder.getPrefixedName() + ").");
    }
    pLog.log(Kind.Ops, Level.Info, "Execution of the check-ins is now finished.");
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
  protected abstract LinkedList<String>
  getNodesToCheckIn();
  
  /**
   * Should this builder check-in the nodes that it has specified with
   * {@link #getNodesToCheckIn()}.
   * <p>
   * If the parameter CheckinWhenDone exists, this method will attempt to read its value. If
   * the read fails or if the parameter does not exist, <code>false</code> will be returned.
   * <p>
   * This method can be overridden if different behavior is desired.
   */
  protected boolean
  performCheckIn()
  {
    boolean toReturn = false;
    try {
    if (hasParam(aCheckinWhenDone))
      toReturn = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
    } 
    catch (PipelineException ex) {
      pLog.log(Kind.Ops, Level.Warning, 
      "There was an error in the performCheckIn method of Builder " +
      "(" + getPrefixedName() + ") attempting to access the CheckinWhenDone parameter.  " +
      "This builder will not attempt to check-in its nodes.\n" + ex.getMessage());
    }
    return toReturn;
  }
  
  /**
   * Level of check-in that the builder should perform.
   * <p>
   * Override this method to change it from the default Minor.
   */
  protected VersionID.Level
  getCheckinLevel()
  {
    return VersionID.Level.Minor;
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
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(nodeName, false);
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    State state = treeComps.getState(nodeName);
    switch(state) {
    case WorkingOtherCheckedInNone:
      throw new PipelineException
        ("The node (" + nodeName + ") exists, but in a different working area and was " +
         "never checked in.  The Builder is aborting due to this problem.");
    case WorkingCurrentCheckedInSome:
      if (pActionOnExistence == ActionOnExistence.CheckOut) {
	pClient.checkOut(getAuthor(), getView(), nodeName, null, 
	  CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	pLog.log(Kind.Bld, Level.Finest, "Checking out the node.");
      }
      break;
    case WorkingNoneCheckedInSome:
    case WorkingOtherCheckedInSome:
      pClient.checkOut(getAuthor(), getView(), nodeName, null, 
	CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      pLog.log(Kind.Bld, Level.Finest, "Checking out the node.");
      break;
    default:
      break;
    }
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
	pLog.log(Kind.Ops, Level.Finest, 
	  "No job was generated for node ("+nodeName+")\n" + ex.getMessage()); 
      }
    }
    return toReturn;
  }
  
  protected final JobProgress 
  getJobProgress
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
	  return JobProgress.Problem;
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
	return JobProgress.InProgress;
    return JobProgress.Complete;
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
      JobProgress state = getJobProgress(jobs);
      if(state.equals(JobProgress.InProgress)) {
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
  areAllFinished
  (
    TreeSet<String> queuedNodes
  ) 
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Finer, "Checking if all the queued nodes finished correctly");
    boolean toReturn = true;
    for(String nodeName : queuedNodes) {
      NodeStatus status = pClient.status(getAuthor(), getView(), nodeName);
      toReturn = isTreeFinished(status);
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
  
  protected final LinkedList<String> 
  getCheckInList()
  {
    return new LinkedList<String>(pNodesToCheckIn);
  }
  
  @Override
  public final int
  getCurrentPass()
  {
    return pCurrentPass;
  }
  
  /**
   * Sets a default editor for a particular stage function type.
   * <p>
   * This is a wrapper function for the
   * {@link BuilderInformation.StageInformation#setDefaultEditor(String, PluginContext)}
   * method. If this method is called, it is not necessary to set the same values in the
   * {@link BuilderInformation.StageInformation}.
   * <p>
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type. This allows high-level builders to override their child builders if they
   * do not agree on what the default editor should be. It is important to remember this when
   * writing builders with sub-builder. A Builder should always set its default editors before
   * instantiating any of its sub-builders. Failure to do so may result in the default editor
   * values being set by the sub-builder.
   */
  public void
  setDefaultEditor
  (
    String function,
    PluginContext plugin
  )
  {
    pBuilderInformation.getStageState().setDefaultEditor(function, plugin);
  }
  
  /**
   * Sets a default selection keys for a particular stage function type.
   * <p>
   * This is a wrapper function for the
   * {@link BuilderInformation.StageInformation#setStageFunctionSelectionKeys(String, TreeSet)}
   * method. If this method is called, it is not necessary to set the same values in the
   * {@link BuilderInformation.StageInformation}.
   * <p>
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type. This allows high-level builders to override their child builders if they
   * do not agree on what the default keys should be. It is important to remember this when
   * writing builders with sub-builder. A Builder should always set its default keys before
   * instantiating any of its sub-builders. Failure to do so may result in the default keys
   * values being set by the sub-builder.
   */
  public void
  setStageFunctionSelectionKeys
  (
    String function,
    TreeSet<String> keys
  )
  {
    pBuilderInformation.getStageState().setStageFunctionSelectionKeys(function, keys);
  }
  
  /**
   * Sets a default license keys for a particular stage function type.
   * <p>
   * This is a wrapper function for the
   * {@link BuilderInformation.StageInformation#setStageFunctionLicenseKeys(String, TreeSet)}
   * method. If this method is called, it is not necessary to set the same values in the
   * {@link BuilderInformation.StageInformation}.
   * <p>
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type. This allows high-level builders to override their child builders if they
   * do not agree on what the default keys should be. It is important to remember this when
   * writing builders with sub-builder. A Builder should always set its default keys before
   * instantiating any of its sub-builders. Failure to do so may result in the default keys
   * values being set by the sub-builder.
   */
  public void
  setStageFunctionLicenseKeys
  (
    String function,
    TreeSet<String> keys
  )
  {
    pBuilderInformation.getStageState().setStageFunctionLicenseKeys(function, keys);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  V E R I F I C A T I O N                                                               */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns a list of Actions required by this Builder, indexed by the toolset that
   * needs to contain them.
   * <p>
   * Builders should override this method to provide their own requirements.  This
   * validation gets performed after all the Setup Passes have been run but before
   * any Construct Passes are run.
   */
  protected MappedArrayList<String, PluginContext>
  getNeededActions()
  {
    return null;
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
	pBuilderInformation.addToCheckinList(pCurrentBuilder);
	pNextBuilder = parent;
	getNextSetupPass(first);
      }
      // if not, add the current builder to the check-in list.
      else {
	pBuilderInformation.addToCheckinList(pCurrentBuilder);
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
  
  public final boolean
  terminateAppOnQuit()
  {
    return pBuilderInformation.terminateAppOnQuit();
  }
  
  public final void
  disconnectClients()
  {
    pClient.disconnect();
    pQueue.disconnect();
    LogMgr.getInstance().cleanup();
  }
  
  public final void
  releaseNodes() 
    throws PipelineException
  {
    BaseStage.cleanUpAddedNodes(pClient, pBuilderInformation.getStageState());
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
  public final static String aActionOnExistence = "ActionOnExistence";
  public final static String aSelectionKeys = "SelectionKeys";
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  

  
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
  private LinkedList<String> pNodesToCheckIn = new LinkedList<String>();

  /**
   * The list of all associated subBuilders
   */
  private TreeMap<String, BaseBuilder> pSubBuilders;
  
  private TreeMap<String, BaseBuilder> pPreppedBuilders;
  
  private TreeMap<String, BaseNames> pSubNames;
  
  private TreeMap<String, BaseNames> pGeneratedNames;
  
  private ArrayList<SetupPass> pFirstLoopPasses;
  
  private ArrayList<ConstructPass> pSecondLoopPasses;
  
  private ArrayList<LockBundle> pLockBundles;

  private int pCurrentPass;
  
  private ActionOnExistence pActionOnExistence;
  
  private ArrayList<ConstructPass> pExecutionOrder;
  
  protected StageInformation pStageInfo; 
  
  
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
  enum JobProgress
  {
    Complete, Problem, InProgress
  }
  
  /**
   *  Defines the use of a node in a particular builder setup.
   *  <P>
   *  This enumeration is meant to be used with {@link BaseStage#getStageFunction()}, which
   *  can cast any of its values to a string and with the get
   *  This list is not complete and should not be considered limiting.  It will be 
   *  added to as 
   */
  public static
  class StageFunction
  {
    public final static String aNone               = "None";
    public final static String aMayaScene          = "MayaScene";
    public final static String aRenderedImage      = "RenderedImage";
    public final static String aTextFile           = "TextFile";
    public final static String aSourceImage        = "SourceImage";
    public final static String aScriptFile         = "ScriptFile";
    public final static String aMotionBuilderScene = "MotionBuilderScene";
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  P A S S   S U B C L A S S E S                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A pass which is responsible for gathering input from the user, checking that the input
   * is correct and makes sense, and creating any new Sub-Builders which may need to be run
   * as a result of the input.
   * <p>
   * Each Setup Pass is broken down into three phases.  The distinction between these three
   * phases is purely cosmetic, as there is no difference in how any of them are invoked.  It
   * exists to make sub-classing easy, allowing specific sorts of functionality to be
   * overriden without having to change other parts. 
   */
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
    
    /**
     * Phase in which parameter values should be extracted from parameters and checked
     * for consistency and applicability.
     * <p>
     * This Phase is when any class variables that need to be set from Parameter values
     * should be set.  It is also a good time to start constructing variable lists of nodes
     * which will be needed by the builder, based upon which parameters are set.  These lists
     * can be used in the {@link ConstructPass#nodesDependedOn() nodesDependedOn()} method
     * of later ConstructPasses.
     * 
     * @throws PipelineException  If any of the parameter values do not make sense or 
     * result in conflicting circumstances.
     */
    @SuppressWarnings("unused")
    public void 
    validatePhase() 
      throws PipelineException
    {}
    
    /**
     * Phase in which outside sources of information can be consulted to ascertain information.
     * <p>
     * Examples might include talking to an SQL database or opening up a Maya scene to extract
     * information about which characters are in a shot or which characters should be in a shot
     * as compared to which characters actually are in a shot.
     * @throws PipelineException  If there is an error involved while invoking the outside
     * source of information.
     */
    @SuppressWarnings("unused")
    public void 
    gatherPhase() 
      throws PipelineException
    {}
    
    /**
     * Phase in which new Sub-Builders should be created and added to the current Builder.
     * <p>
     * The logic on whether or not a Sub-Builder should be added is probably properly done
     * in either the validate or the gather Phase.  The init phase should be solely devoted
     * to creating the new instances.
     * @throws PipelineException
     */
    @SuppressWarnings("unused")
    public void
    initPhase()
      throws PipelineException
    {}

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
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finer, 
        "Starting the validate phase in the (" + getName() + ").");
      validatePhase();
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finer, 
        "Starting the gather phase in the (" + getName() + ").");
      gatherPhase();
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finer, 
        "Starting the init phase in the (" + getName() + ").");
      initPhase();
    }
    
    @Override
    public String
    toString()
    {
      return pDescription;
    }
    
    private static final long serialVersionUID = -2836639845295302403L;
  }
  
  /**
   * A pass which is responsible for building and/or modifying a group of nodes.
   * <p>
   * Construct passes are run after all the {@link SetupPass SetupPasses} have been run.
   * <p>
   * The execution path of a Construct Pass is as follows.
   * <ol>
   * <li> Check for all the nodes that the Pass depends on (as set by the
   * {@link ConstructPass#nodesDependedOn() nodesDependedOn} method) and make sure they are
   * all in the current working area. Abort Builder execution if any of those nodes are
   * missing.
   * <li> Queue all the nodes that are specified by the
   * {@link ConstructPass#preBuildPhase() preBuildPhase} method.  Wait for these jobs to
   * finish before continuing.  If all the jobs do not successfully complete, terminated
   * Builder execution.
   * <li> Run the build method to construct/modify nodes.
   * 
   */
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
      return new TreeSet<String>();
    }
    
    @SuppressWarnings("unused")
    public void
    buildPhase()
      throws PipelineException
    {}
    
    /**
     * A list of nodes which have to exist before this Pass is run.
     * <p>
     * The Builder will search for each of these nodes and makes sure that it exists in the
     * current working area. If the nodes does not exist in the current area, then it will
     * check out the node. If the node does exist, then it will not check it out, unless the
     * {@link ActionOnExistence} is set to {@link ActionOnExistence#CheckOut CheckOut}. If
     * the node does not exist or cannot be checked-out (so if it is Pending in a different
     * working area) then a {@link PipelineException} will get thrown and builder execution
     * will terminate.
     * <p>
     * It is important to note that the Builder does not care if a needed node is in a
     * Finished or Identical state. The most common use of this method should be for nodes
     * which have been created by other processes, either manual or other builders. Obviously
     * a Builder should be able to rely on its own internal consistency to make sure that all
     * of the nodes it needs that it itself builds exist. If it needs to make sure that a node
     * that it builds is in a Finished state, then the {@link #preBuildPhase() preBuildPhase}
     * method should be used instead.
     * 
     * @see #preBuildPhase()
     */
    public TreeSet<String>
    nodesDependedOn()
    {
      return new TreeSet<String>();
    }
    
    public final void
    run()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finer, 
        "Starting the pre-build phase in the (" + getName() + ").");
      TreeSet<String> neededNodes = preBuildPhase();
      if (neededNodes.size() > 0) {
	LinkedList<QueueJobGroup> jobs = queueNodes(neededNodes);
	waitForJobs(jobs);
	if (!areAllFinished(neededNodes))
	  throw new PipelineException("The jobs did not finish correctly");
      }
      for (String needed : this.nodesDependedOn())
	neededNode(needed);
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finer, 
        "Starting the build phase in the (" + getName() + ").");
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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  P U B L I C   S U B C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/

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
  
  
  public static 
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
    
    @Override
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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  G U I   T H R E A D   C L A S S E S                                                   */
  /*----------------------------------------------------------------------------------------*/
  
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
      catch (PipelineException ex) {
	quit(ex, 1);
      }
      catch(Exception ex) {
	quit(ex, 1);
      }
    }
    
    private void
    quit
    (
      Exception ex,
      int exitCode  
    )
    {
      LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Severe,
       getFullMessage(ex));
      if (pBuilder.terminateAppOnQuit()) {
        System.err.println("Problem initializing builder in gui mode.\n" + ex.getMessage());
        System.exit(exitCode);
      } else {
        pGuiDialog.setVisible(false);
        UIMaster.getInstance().showErrorDialog(ex);
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
	checkActions();
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
	pJobsFinishedCorrectly = areAllFinished(pBuilderInformation.getQueueList());
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
