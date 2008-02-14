// $Id: BaseBuilder.java,v 1.45 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.MultiMap.*;
import us.temerity.pipeline.NodeTreeComp.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.ui.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.*;
import us.temerity.pipeline.stages.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.ui.core.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The parent class of all Builders.
 * <p>
 * <h2>Introduction</h2>
 * 
 * Builders are Utilities designed to make creating applications that interact with Pipeline
 * node networks simpler to write and maintain. The primary focus of Builders is
 * programatically creating large and complicated node networks while needing minimal
 * information and intervention from a user. However, that is not the only use for Builders
 * which can be used in any situation that requires a framework for complicated access to node
 * networks.
 * <p>
 * Builders are designed to function in either command-line or graphical mode, with all UI
 * code being automatically generated from the builder's configuration. They are also designed
 * to be modular, allowing builders to be nested inside other builders to create higher level
 * builders quickly and easily.
 * <p>
 * <h2>Parameters</h2>
 * 
 * BaseBuilder declares two parameters in its constructor. Any class which inherits from
 * BaseBaseBuilder that is using parameters and parameter layouts will need to have a way to
 * account for these parameters as well as the parameters which come from {@link BaseUtil}.
 * 
 * <DIV style="margin-left: 40px;">
 *   ActionOnExistence <br>
 *   <DIV style="margin-left: 40px;">
 *     This parameter is used to control the behavior of the Builder when
 *     a node it is supposed to build already exists.
 *     </DIV><br>
 * 
 *   ReleaseOnError <br>
 *   <DIV style="margin-left: 40px;">
 *     This parameter controls the behavior of the Builder if it encounters an error during 
 *     the course of its execution. If this parameter is set to <code>true</code>, an error 
 *     will cause the Builder to release all the nodes that have been created during its
 *     run.
 *   </DIV><br>
 * </DIV>
 * 
 * BaseBuilder also contains utility methods for creating two other parameters that a majority
 * of Builders may want to implement.
 * 
 * <DIV style="margin-left: 40px;">
 *   CheckinWhenDone <br>
 *   <DIV style="margin-left: 40px;">
 *     This parameter can be used to specify whether the Builder should check in nodes it has 
 *     created when it finishes execution. By default this parameter's value is used in the 
 *     {@link #performCheckIn()} method.
 *   </DIV><br>
 *   
 *   SelectionKeys
 *   <DIV style="margin-left: 40px;"> 
 *     This parameter is a list of all the Selection Keys that exist in the Pipeline install, 
 *     allowing the user of the Builder to specify a subset of them for some purpose.  Usually 
 *     this parameter is used to specify a list of keys that all nodes being built will have.
 *   </DIV>
 * </DIV>
 * 
 * <p>
 * <h2>Terminology used within these java docs.</h2>
 * <dl>
 * <dt>Builder</dt>
 *   <dd>Refers to any instance of this class. Usually preceded with 'parent' when being used
 *       with Sub-Builder to make the relation clear. </dd>
 *       
 * <dt>Sub-Builder</dt>
 *   <dd>Refers to any instance of {@link BaseUtil} that is being used as a child of the
 *       current Builder.</dd>
 * 
 * <dt>child Builder</dt>
 *   <dd>Refers to any instance of this class that is being used as a child of the current
 *       builder. The child Builders of a Builder are a subset of the Sub-Builders.</dd>
 * 
 * <dt>child Namer</dt>
 *   <dd>Refers to any instance of {@link BaseNames} that is being used as a child of the
 *       current builder. The child Namers of a Builder are a subset of the Sub-Builders.</dd>
 *       
 * <dt>New Sub-Builder
 *   <dd>Refers to any Sub-Builder before it has been prepared by the First Loop, either by
 *       executing the SetupPasses if it is a child Builder or by calling
 *       {@link BaseNames#generateNames()} if it is a child Namer</dd>
 * 
 * <dt>First Loop
 *   <dd>The period of Builder execution during which all the SetupPasses are run and child
 *       Namers have {@link BaseNames#generateNames()} called.
 * 
 * <dt>Second Loop
 *   <dd>The period of Builder execution during with all the ConstructPasses are run.
 * </dl>
 * 
 * <h2>Passes</h2>
 * Builders contain two different sorts of passes, which are executed at different times.
 * {@link SetupPass Setup Passes} are run at the start of builder execution and are used
 * to gather information from the user and from existing files/node structure in order to
 * figure out what sorts of networks needs to be built or modified.  Once all 
 * {@link SetupPass Setup Passes} have executed, {@link ConstructPass Construct Passes} are
 * run.  These passes do the actual heavy lifting of manipulating and creating node networks.
 * There are rules which govern the order in which these passes execute and how the different
 * Phases inside each Pass are run.
 * <br>
 * <h3>The Passes</h3>
 * <p>
 * <dl>
 * <dt>SetupPasses
 *   <dd>A pass which is responsible for gathering input from the user, checking that the input
 *       is correct and makes sense, and creating any new Sub-Builders which may need to be run
 *       as a result of the input. <br>
 *       Each Setup Pass is broken down into three phases.  The distinction between these three
 *       phases is purely cosmetic, as there is no difference in how any of them are invoked.  
 *       They exists to make sub-classing easy, allowing specific sorts of functionality to be
 *       overridden without having to change other parts and to provide a clear delineation for 
 *       people reading the code. <br>
 *       Builders execute Setup Passes in the order that they were added to the Builder.  All
 *       Setup Passes should be added in the Builder's constructor, prior to 
 *       {@link #setLayout(PassLayoutGroup) setLayout()} being called.
 *     <p>
 *     <dl>
 *     <dt>validatePhase
 *       <dd>Phase in which parameter values should be extracted from parameters and checked
 *           for consistency and applicability. This Phase is when any class variables that 
 *           need to be set from Parameter values should be set.  It is also a good time to 
 *           start constructing lists of nodes which will be needed by the builder, based upon 
 *           which parameters are set.  These lists can be used in the 
 *           {@link ConstructPass#nodesDependedOn() nodesDependedOn()} method of later 
 *           ConstructPasses.
 *     
 *     <dt>gatherPhase
 *       <dd>Phase in which outside sources of information can be consulted to ascertain information.
 *           Examples might include talking to an SQL database or opening up a Maya scene to extract
 *           information about which characters are in a shot or which characters should be in a shot
 *           as compared to which characters actually are in a shot.
 *     
 *     <dt>initPhase
 *       <dd>Phase in which new Sub-Builders should be created and added to the current Builder
 *           and in which PlaceHolder Parameters should have their values adjusted to be actual
 *           parameters. The logic on whether or not a Sub-Builder should be added is probably 
 *           properly done in either the validate or the gather Phase.  The init phase should 
 *           be solely devoted to creating the new instances and adjusting parameters.
 *     </dl>
 * <dt>ConstructPasses
 *   <dd>A pass which is responsible for building and/or modifying a group of nodes.
 *       Construct passes are run after all the {@link SetupPass SetupPasses} have been run.
 *       The execution path of a Construct Pass is as follows.
 *       <ol>
 *       
 *       <li> Check for all the nodes that the Pass depends on (as set by the
 *            {@link ConstructPass#nodesDependedOn() nodesDependedOn} method) and make sure 
 *            they are all in the current working area. Abort Builder execution if any of 
 *            those nodes are missing.
 *       
 *       <li> Queue all the nodes that are specified by the 
 *            {@link ConstructPass#preBuildPhase() preBuildPhase} method.  Wait for these jobs 
 *            to finish before continuing.  If all the jobs do not successfully complete, 
 *            terminate Builder execution.
 *       
 *       <li> Run the build method to construct/modify nodes.
 *       
 *       </ol>
 *     Builders can create dependencies between any two Construct Passes that exist.  It might
 *     be something as simple as having two Construct Passes, one which builds nodes and one
 *     which finalizes nodes, and the finalize pass depends on the build pass.  Or it could be
 *     something more complicated, where a Construct Pass in one child Builder has to wait to
 *     run until another child Builder's Construct Pass has run.   
 *     <p>
 *     <dl>
 *     <dt>prebuildPhase
 *       <dd>Returns a set of nodes that have to be in a Finished queue state before the
 *           build method is called. All the nodes that are in this list will be queued and 
 *           the Builder will wait for them to finish executing.  Once they finish executing, 
 *           it will perform a status update on the nodes and make sure that they are in the 
 *           Finished state.  If they are not, a {@link PipelineException} will be thrown and 
 *           execution will stop.
 *           
 *     <dt>buildPhase
 *       <dd>Constructs or modifies nodes. This method is responsible for the meat of Builder 
 *       execution.  In it, {@link BaseStage stages} should be created and their build methods 
 *       should be called.
 *       
 *     </dl> 
 * </dl>
 * <h2>Execution</h2>
 * The execution path of builder is rather involved and complicated.  The following will attempt 
 * to layout, without too much explanation of what each step is, the steps that are followed. 
 * Further discussion of what each step is and how it works is available in other parts of the
 * Javadocs.
 * <ul>
 * <li> The first Setup Pass of the top level Builder is run.  It performs the following actions.
 *   <ol>
 *   <li> Assign any command-line parameters that are applicable to the current layout pass of the
 *        Builder and any Namers which have been added to the current Builder as Sub-Builders.  If
 *        there are any Namers who have had their parameter values mapped to parent values, assign
 *        the parent values to the child Namer's params.
 *   <li> If the Builder is executing in GUI mode, allow the user to input values for parameters
 *        in the current layout which accept user values as well as any Namers which the Builder
 *        has added as Sub-Builders.  Namers which do not have an exposed parameters will not be
 *        displayed in the GUI, but will have their generateNames() method called.
 *   <li> Run the {@link BaseNames#generateNames() generateNames()} method of each Namer that
 *   <li> Run the {@link SetupPass#validatePhase() validatePhase()}.
 *   <li> Run the {@link SetupPass#gatherPhase() gatherPhase()}.
 *   <li> Run the {@link SetupPass#initPhase() initPhase()}.
 *   <li> Loop through all the Builders that have been added to the current Builder as child
 *        Builders.  Find all the parameters whose values have had their values mapped to parent
 *        values and assign the parent values to the child Builder's params. 
 *   <li> Run the child Builder's SetupPasses (which follows the same pattern as the current 
 *        Builder).
 *   </ol>
 * <li> Once all existing Sub-Builders have run all of their Setup Passes run the next Setup
 *      Pass in the current Builder.
 * <li> Continue until all Setup Passes in all Builders have been run.
 * <li> Using the {@link #getNeededActions()} method of each Builder, determine if there are
 *      any Actions needed for Builder execution which are not part of the chosen toolsets.
 *      If there are any Actions missing, then an Exception will be thrown and execution
 *      will be halted.
 * <li> Examine all the Construct Passes and, based upon which passes depend on which other 
 *      passes generate an execution order that guarantees that no passes are executed before
 *      a pass which they depend on.  If it is impossible to generate such and order due to
 *      circular dependencies, an Exception will be thrown and execution will be halted.
 * <li> Using the order generated by the proceeding step, execute each Construct Pass.  They
 *      perform the following actions
 *   <ol>
 *   <li> Run the {@link ConstructPass#preBuildPhase() preBuildPhase()} and wait for the jobs
 *        that is generates to complete successfully.  If the jobs do not all complete 
 *        successfully, an Exception will be thrown and execution will be halted.
 *   <li> Using the list of nodes returned by 
 *        {@link ConstructPass#nodesDependedOn() nodesDependedOn()}, search for all of the 
 *        nodes and make sure that copies exist in the local working area.  If any of 
 *        these nodes does not exist in the local working area, check it out.  If, for 
 *        some reason, it is impossible to acquire a local copy of one of the nodes (either 
 *        because the node does not exist or it has never been checked-in, an error will
 *        be thrown and execution will be halted.
 *   <li> Run the {@link ConstructPass#buildPhase() buildPhase()}
 *   </ol>
 * <li> Queue all of the nodes which have been added to the queue list using the 
 *      {@link #addToQueueList(String)} method and wait until the jobs finish.  Once the jobs
 *      have finished, do a status update on each of the queued nodes and check to make sure
 *      that the entire tree is in the Finished queue state.  If all the trees are not, an
 *      error is thrown and execution is aborted.
 * <li> For each builder, the {@link #performCheckIn()} method is consulted to see if the nodes
 *      that were created should be checked-in.  If they should be, the 
 *      {@link #getNodesToCheckIn()} method is used to generate the list of nodes that should 
 *      be checked-in.  Additionally, the {@link #getCheckInMessage()} and 
 *      {@link #getCheckinLevel()} can be overridden to provide custom check-in messages and
 *      check-in {@link us.temerity.pipeline.VersionID.Level Levels}.  Check-in (and Lock
 *      Bundles, discussed next) are executed in the same order as SetupPasses finished
 *      executing.  Therefore, the lowest level Sub-Builder will always check-in its nodes
 *      first and the top level Builder will always check-in its nodes last.
 * <li> Lock Bundles are executed to correctly setup networks that need to have portions of
 *      then locked.  Lock Bundles contain two lists of nodes, a list of nodes to lock and a
 *      list of nodes to check-in.  First, all the nodes in the lock list are locked to the 
 *      latest version.  Then, all the nodes that need to be checked-in are queued.  If any jobs
 *      are generated, the Builder waits for them to finish and then checks-in all of the 
 *      nodes. 
 * </ul>
 * 
 * <h2>Requirements</h2>
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
  
  /**
   * The default constructor for all Builders. 
   * 
   * @param name
   *   The name of the Builder.
   * @param desc
   *   A brief description of what the Builder is supposed to do.
   * @param mclient
   *   The instance of the Master Manager that the Builder is going to use.
   * @param qclient
   *   The instance of the Queue Manager that the Builder is going to use
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   */
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
  

  /**
   * Sets the hierarchical grouping of builder names which determine the layout of UI 
   * components. 
   * <P> 
   * The given layouts must contain exactly one entry for each builder name.  All 
   * <CODE>null</CODE> entries will cause additional space to be added between the menu items. 
   * Each layout subgroup will be represented by its own submenu off the main layout group. 
   * 
   * This method should be called by subclasses in their constructor.
   * 
   * @param layout
   *   The layout group.
   */
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
   * @param subBuilder
   *   The instance of BaseUtil that is being added as a Sub-Builder. 
   * @param defaultMapping 
   *   Should the Sub-Builder be setup with the default parameter mappings.  This means that
   *   all the parameters which are defined as part of the BaseUtil and BaseBuilder will
   *   be mapped from the parent to this child Builder.
   * @param paramMapping 
   *   A TreeMap containing a set of parameter mappings to make between the child Builder's 
   *   parameters and the parent Builder. 
   * @throws PipelineException
   *   If an attempt is made to add a Sub-Builder with the same name as one that already
   *   exists.
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
   * @param subBuilder
   *   The instance of BaseUtil that is being added as a Sub-Builder. 
   * @param defaultMapping 
   *   Should the Sub-Builder be setup with the default parameter mappings.  This means that
   *   all the parameters which are defined as part of the BaseUtil and BaseBuilder will
   *   be mapped from the parent to this child Builder.
   * @throws PipelineException
   *   If an attempt is made to add a Sub-Builder with the same name as one that already
   *   exists.
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
   * @param subBuilder
   *   The instance of BaseUtil that is being added as a Sub-Builder. 
   * @throws PipelineException
   *   If an attempt is made to add a Sub-Builder with the same name as one that already
   *   exists.
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
   * Gets a mapping of all the child Builders, keyed by the names of the Sub-Builders.
   * <p>
   * This includes Builders which have been prepped already and all of them which haven't.
   * This does not include any Namers.
   * 
   * @return A TreeMap containing all the child Builders indexed by their name.
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
   * violated. If code is properly written, this exception should never be thrown, since a
   * parent Builder should never ask for a non-existent Sub-Builder. If some sort of
   * verification of existence is needed, the {@link #getSubBuilders()} method can be used to
   * get a Map of all the Sub-Builder which can be queried to determine existence.
   * 
   * @param instanceName
   *   The name of the Sub-Builder.
   *   
   * @return
   *   Either the BaseNamer or the BaseBuilder that is identified with this name.
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
  
  /**
   * Gets a list of all the child Namers which have not have their generateNames()
   * method run yet.
   * @return
   *   The Map of BaseNames indexed by name.
   */
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
   * Error checking will cover the existence of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values.
   * <p>
   * Note that it is not possible to create mappings for parameters which do not have an
   * established type when the mapping is established.  Attempts to make a mapping of 
   * this sort may result in unpredictable results.
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
   * Error checking will cover the existence of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values.
   * <p>
   * Note that it is not possible to create mappings for parameters which do not have an
   * established type when the mapping is established.  Attempts to make a mapping of 
   * this sort may result in unpredictable results.
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
   * Creates a mapping between parameters in the named Sub-Builder and the parameters
   * in the parent Builder. 
   * <p>
   * Error checking will cover the existence of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values.
   * <p>
   * Note that it is not possible to create mappings for parameters which do not have an
   * established type when the mapping is established.  Attempts to make a mapping of 
   * this sort may result in unpredictable results.
   * 
   * @param subBuilderName
   *   The subBuilder the mapping is being created in.
   * @param mapping
   *   A map of parameter mappings.  The keys are the names of the Sub-Builder parameters 
   *   that are being driven.  The values are the names of the Sub-Builder parameters that 
   *   are being driven
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
   * gui or from accepting a command line value.
   * 
   * @param param
   *   The name of the parameter that is to be disabled.
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
  initializeSubBuilder
  (
    String name
  )
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
  
  /**
   * Assigns all the command-line params for a given pass of a Utility.
   * 
   * @param utility
   *   The instance of BaseUtil that is having its parameter values set.
   * @throws PipelineException
   *   If an attempt is made to set a command line parameter which does not implement the
   *   {@link SimpleParamAccess} interface and the --abort command line flag has been set.
   */
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
      PipelineException toThrow = PipelineException.getDetailedException(ex);
      String logMessage = "An Exception was thrown during the course of execution.\n";
      logMessage += toThrow.getMessage() + "\n";
      if (pReleaseOnError) 
	logMessage += "All the nodes that were registered will now be released.";

      if (pReleaseOnError)
	BaseStage.cleanUpAddedNodes(pClient, pStageInfo);

      throw new PipelineException(logMessage);
    }
    if (finish)
      executeCheckIn();
    else
      throw new PipelineException("Execution halted.  Jobs didn't finish correctly");
  }
  
  /**
   * Runs the GUI mode of the Builder.
   */
  private final void
  runGUI()
  {
    pCurrentBuilder = this;
    pNextBuilder = null;
    SwingUtilities.invokeLater(new BuilderGuiThread(this));
  }

  /**
   * Test method needed for simulating Builder UI implementations while not actually running
   * a Builder.
   * <p>
   * Probably not needed for anyone except core developers.
   */
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
   * <p>
   * This takes care of the following parameters.
   * <ul>
   * <li> UtilContext - Extracts the Util Context value and sets the Builder's context from it.
   * <li> ReleaseOnError - Extracts the Release On Error value and sets the pReleaseOnError 
   *      variable from it.
   * <li> ActionOnExistence - Extracts the Action on Existance value and sets the correct 
   *      value in the Stage Information for the Builder. 
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
   * 
   * @param sourcePass
   *   The source pass.  This is the pass that will be run first.
   * @param targetPass
   *   The Target Pass.  This is the pass that will be run after the source path.
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
  
  /**
   * Get a Construct pass with the given name.
   * <p>
   * This can be used to get a Construct Pass from a Sub-Builder which can be used to
   * setup Pass dependencies.  
   * 
   * @param name
   *   The name of the Construct pass.
   * @return
   *   The Construct pass.
   */
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
  
  /**
   * Verifies that all the needed Actions for all Builders are actually part of 
   * the appropriate toolsets.
   */
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

  /**
   * A recursive helper for checking all the actions.
   */
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
  
  /**
   * Gets a list of Construct Passes in the order in which they will be executed.
   * <p>
   * This method should only be called after the Execution Order has been established,
   * after all the Setup Passes have been run and before the Construct Passes have been
   * run.
   * @return
   */
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
   * Returns a list of nodes to be checked-in.
   * <p>
   * By default, this returns the list from {@link #getNodesToCheckIn()} that Builders  
   * can add to using the {@link #addToCheckInList(String)}.
   * <p>
   * This method can be overriden to return a different list of nodes if the Builder is
   * using some other method of determining what should be checked-in.
   */
  protected LinkedList<String>
  getNodesToCheckIn()
  {
    return getCheckInList();
  }
  
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
  
  /**
   * Searches for a node in the current working area.
   * <p>
   * If the node exists in the current working area, no action is taken.  If the node does
   * not exist in the current working area, but does exist in the repository, it will be 
   * checked-out.  If the node does not exist anywhere that is accessible or if no node exists
   * with the given name, then a {@link PipelineException} will be thrown.
   */
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
   * <p>
   * Users may want to call {@link #clearDisableList()} after calls to this method in 
   * order to clear the data structure for future use.
   */
  protected final void 
  disableActions() 
    throws PipelineException
  {
    for(String nodeName : pNodesToDisable) {
      disableAction(nodeName);
    }
  }

  /**
   * Queues all the nodes that all Builder has specified for queuing using the 
   * {@link #addToQueueList(String)} method.
   * @return
   */
  private final LinkedList<QueueJobGroup> 
  queueJobs() 
  {
    return queueNodes(pBuilderInformation.getQueueList());
  }

  /**
   * Queues a group of nodes and returns the job groups that were created for all of the nodes.
   * <p>
   * Any nodes which do not need jobs generated for them will be skipped.
   * @param nodes
   *  The list of node names to be queued
   * @return
   *  The Job Groups that were created for the nodes that were queued.
   */
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
  
  /**
   * Return the current status of a group of queue jobs as relates to Builder
   * execution.
   * <p>
   * There are only three states which this can return.
   * <ul>
   * <li> Complete - Indicates that all jobs in all the job groups have been successfully
   * completed.
   * <li> In Progress - Indicates that the jobs are still running, but that none of them have
   * encountered any problems.
   * <li> Problem - Indicates that at least one of the jobs which was running encountered an 
   * error.
   * 
   * @param queueJobs
   *   The list of Job Groups to search for progress.
   * @return
   *   The state of the job groups.
   */
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
  
  /**
   * Causes the current thread to wait for a group of job groups to finish execution.
   * <p>
   * The thread will check if all the jobs have completed.  If they have not, it will
   * sleep for 7 seconds and check again until all the jobs in the job groups have
   * completed.
   * @param jobGroups
   *   The list of job groups to wait on.
   */
  protected final void
  waitForJobs
  (
    LinkedList<QueueJobGroup> jobGroups
  ) 
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Fine, "Waiting for the jobs to finish");
    do {
      JobProgress state = getJobProgress(jobGroups);
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

  /**
   * Adds a node to the global queue list.
   * <p>
   * This is the list of nodes which will be queued after all the Construct Passes have been
   * executed.  This is a global list which is added to by all builders.
   * 
   * @param nodeName
   *   The name of the node.
   */
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

  /**
   * Removes a node from the global queue list.
   * <p>
   * Since this is a global list, this has the potential of affecting other Builders which may
   * have added the node to the this list.  This may have the result of causing their check-ins
   * to fail, so it should be used with caution.
   * 
   * @param nodeName
   *   The name of the node.
   */
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

  /**
   * Adds a node to the Builder's disable list.
   * <p>
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders.
   * <p>
   * All nodes in this list will have their Actions disabled when the 
   * {@link #disableActions()} method is called.
   * 
   * @param nodeName
   *   The name of the node.
   */
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

  /**
   * Removes a node from the Builder's disable list.
   * <p>
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders.
   * 
   * @param nodeName
   *   The name of the node.
   *   
   * @see #clearDisableList()
   */
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
  
  /**
   * Adds a node to the Builder's check-in list.
   * <p>
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders.
   * <p>
   * By default, the {@link #getNodesToCheckIn()} method returns this list when it is
   * called.
   * 
   * @param nodeName
   *   The name of the node.
   */
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

  /**
   * Removes a node from the Builder's check-in list.
   * <p>
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders.
   * 
   * @param nodeName
   *   The name of the node.
   */
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

  /**
   * Clears the global queue list.
   * <p>
   * This method will affect all Builders.  After it is run, none of the nodes that have been
   * added to the queue list will actually be queued.  It should only be used in extreme cases.
   */
  protected final void 
  clearQueueList()
  {
    pBuilderInformation.clearQueueList();
    pLog.log(Kind.Bld, Level.Finest, 
      "Clearing queue list in Builder (" + getPrefixedName() + ").");
  }

  /**
   * Clears the builder disable list.
   */
  protected final void 
  clearDisableList()
  {
    pNodesToDisable.clear();
    pLog.log(Kind.Bld, Level.Finest, 
      "Clearing disable list in Builder (" + getPrefixedName() + ").");
  }

  /**
   * Gets the list of all the nodes currently in the disable list.
   */
  protected final TreeSet<String> 
  getDisableList()
  {
    return new TreeSet<String>(pNodesToDisable);
  }

  /**
   * Gets a list of all the nodes currently in the check-in list. 
   */
  protected final LinkedList<String> 
  getCheckInList()
  {
    return new LinkedList<String>(pNodesToCheckIn);
  }
  
  /**
   * Gets the current pass of the builder.
   */
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
    pStageInfo.setDefaultEditor(function, plugin);
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
    pStageInfo.setStageFunctionSelectionKeys(function, keys);
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
    pStageInfo.setStageFunctionLicenseKeys(function, keys);
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

  /**
   * Gets the next Builder ready to run.
   * <p>
   * Assigns it command line params and runs its initialize method.  Sets the 
   * {@link #pNextBuilder} variable to be builder that is has found to run next.
   * @throws PipelineException
   */
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
  
  /**
   * Runs the next setup pass.
   * <p>
   * This method should not be called by users. 
   */
  public final synchronized void
  runNextSetupPass() 
    throws PipelineException
  {
    pNextSetupPass.run();
    pCurrentBuilder.pCurrentPass++;
  }
  
  /**
   * Get the Builder which is currently being executed.
   */
  public final BaseBuilder
  getCurrentBuilder()
  {
    return pCurrentBuilder;
  }
  
  /**
   * Gets the Setup Pass that is about to be run.
   */
  public final SetupPass
  getCurrentSetupPass()
  {
    return pNextSetupPass;
  }
  
  /**
   * What is the value of the Release On Error variable set to.
   * @return
   */
  public boolean
  releaseOnError()
  {
    return pReleaseOnError;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S H U T D O W N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Should the instance of the JVM that is running the Builder be terminated when this
   * Builder finishes execution?
   */
  public final boolean
  terminateAppOnQuit()
  {
    return pBuilderInformation.terminateAppOnQuit();
  }
  
  /**
   * Should the Builder use its own builder logging panel or should it disable it and allow 
   * the plui instance that is running the builder to log to its own Logging Panel?
   */
  public final boolean
  useBuilderLogging()
  {
    return pBuilderInformation.useBuilderLogging();
  }
  
  /**
   * Disconnect all the instances of the server clients that the Builder has been using.
   */
  public final void
  disconnectClients()
  {
    pClient.disconnect();
    pQueue.disconnect();
    LogMgr.getInstance().cleanup();
  }
  
  /**
   * Releases all the nodes that were created during Builder execution.
   */
  public final void
  releaseNodes() 
    throws PipelineException
  {
    BaseStage.cleanUpAddedNodes(pClient, pStageInfo);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T Y   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Utility method for adding a value to a Set only if it isn't <code>null</code>.
   * 
   * @param value
   *   The value being added if it is not null. 
   * @param set 
   *   The set to add the value to.
   * @return 
   *   A boolean which indicates if the value was added.  A value of <code>false</code> means
   *   the value passed in was <code>null</code>.
   */
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
  /*  E N U M E R A T I O N S                                                               */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Enum that represents the different states a group of Job Groups can be in.
   */
  protected 
  enum JobProgress
  {
    /**
     * All the jobs in the job groups have completed successfully.
     */
    Complete, 
    /**
     * At least one job in the job groups has failed.
     */
    Problem, 
    
    /**
     * At least one job in the job groups is still running or waiting to run, but no
     * jobs have failed yet. 
     */
    InProgress
  }
  
  /**
   * A pass which is responsible for gathering input from the user, checking that the input
   * is correct and makes sense, and creating any new Sub-Builders which may need to be run
   * as a result of the input.
   * <p>
   * Each Setup Pass is broken down into three phases.  The distinction between these three
   * phases is purely cosmetic, as there is no difference in how any of them are invoked.  It
   * exists to make sub-classing easy, allowing specific sorts of functionality to be
   * overridden without having to change other parts. 
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
     * 
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
     * 
     * @throws PipelineException
     */
    @SuppressWarnings("unused")
    public void
    initPhase()
      throws PipelineException
    {}

    /**
     * Executes the pass.
     */
    public final void
    run()
      throws PipelineException
    {
      TreeSet<String> listOfNames = new TreeSet<String>(pSubNames.keySet());
      for(String name : listOfNames) {
	BaseNames names = pSubNames.get(name);
	initializeSubBuilder(name);
	names.run();
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
    
    /**
     * Returns a set of nodes that have to be in a Finished queue state before the
     * build() method is called.
     * <p>
     * All the nodes that are in this list will be queued and the Builder will wait for
     * them to finish executing.  Once they finish executing, it will do a status update
     * on the nodes and make sure that they are in a Finished state.  If they are not,
     * a {@link PipelineException} will be thrown and execution will stop.
     * 
     * @return This method should never return <code>null</code>.  If there are no nodes
     * that need to be queued, it should just return an empty TreeSet.
     * @throws PipelineException
     */
    @SuppressWarnings("unused")
    public TreeSet<String>
    preBuildPhase()
      throws PipelineException
    {
      return new TreeSet<String>();
    }
    
    /**
     * Constructs or modifies nodes.
     * <p>
     * This method is responsible for the meat of Builder execution.  In it, Stages should
     * be created and their build methods should be called.
     * 
     * @see BaseStage
     * @see StandardStage
     * @throws PipelineException  If something goes wrong while building nodes.
     */
    @SuppressWarnings("unused")
    public void
    buildPhase()
      throws PipelineException
    {}
    
    /**
     * A list of nodes which have to exist before this Pass is run.
     * <p>
     * The Builder will search for each of these nodes and makes sure that it exists in the
     * current working area. If the node does not exist in the current area, then it will
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
    
    /**
     * Executes the stage.
     */
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
    
    /**
     * Gets the name of the Builder which created this pass.
     */
    public PrefixedName
    getParentBuilderName()
    {
      return pBuilderInformation.getBuilderFromPass(this).getPrefixedName();
    }
    
    /**
     * Gets the instance of BaseBuilder which created this pass.
     */
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
    
    /**
     * Equals is calculated in the strictest sense of the two objects being the
     * exact same object.  This is because it is completely possible for there to
     * be two instances of the same Builder running (say two Shot Builders which are
     * Sub Builders of a Sequence Builder) which have the same Construct Passes.  From
     * any comparison of parameters, they would be completely identical.  So in order
     * to be able to differentiate between those two, it has to use the actual equals. 
     */
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

  /**
   * Thread for use with the Builder GUI code.
   * <p>
   * Users should never need to call this and unpredicatable results may occur if it is used.
   */
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
	quit(PipelineException.getDetailedException(ex), 1);
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
       Exceptions.getFullMessage(ex));

      if (pBuilder.terminateAppOnQuit()) {
        System.err.println("Problem initializing builder in gui mode.\n" + ex.getMessage());
        System.exit(exitCode);
      } 
      else {
        pGuiDialog.setVisible(false);
        UIMaster.getInstance().showErrorDialog(ex);
      }
    }

    private BaseBuilder pBuilder;
  }
  
  /**
   * Thread for use with the Builder GUI code.
   * <p>
   * Users should never need to call this and unpredicatable results may occur if it is used.
   */
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
      catch (Exception ex) {
        pGuiDialog.handleException(PipelineException.getDetailedException(ex));
      }
    }
  }
  
  /**
   * Thread for use with the Builder GUI code.
   * <p>
   * Users should never need to call this and unpredicatable results may occur if it is used.
   */
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
      catch (Exception ex) {
        pGuiDialog.handleException(PipelineException.getDetailedException(ex));
      }
    }
  }
  
  /**
   * Thread for use with the Builder GUI code.
   * <p>
   * Users should never need to call this and unpredicatable results may occur if it is used.
   */
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
      catch (Exception ex) {
        pGuiDialog.handleException(PipelineException.getDetailedException(ex));
      }
    }
  }
  
  /**
   * Thread for use with the Builder GUI code.
   * <p>
   * Users should never need to call this and unpredicatable results may occur if it is used.
   */
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
  
  /**
   * Thread for use with the Builder GUI code.
   * <p>
   * Users should never need to call this and unpredicatable results may occur if it is used.
   */
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

}
