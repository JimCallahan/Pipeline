// $Id: BaseBuilder.java,v 1.75 2009/07/01 16:43:14 jim Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.NodeTreeComp.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.execution.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.*;
import us.temerity.pipeline.stages.*;

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
 * networks.<p>
 * 
 * Builders are designed to function in either command-line or graphical mode, with all UI
 * code being automatically generated from the builder's configuration. They are also designed
 * to be modular, allowing builders to be nested inside other builders to create higher level
 * builders quickly and easily.<p>
 * 
 * <h2>Parameters</h2>
 * 
 * BaseBuilder declares two parameters in its constructor. Any class which inherits from
 * BaseBaseBuilder that is using parameters and parameter layouts will need to have a way to
 * account for these parameters as well as the parameters which come from {@link BaseUtil}.
 * 
 * <DIV style="margin-left: 40px;">
 *   ActionOnExistence <br>
 *   <DIV style="margin-left: 40px;">
 *     This parameter is used to control the behavior of the Builder when a node it is 
 *     supposed to build already exists.<br>
 *     <b>Important</b> This parameter is added to the builder during the setLayout call.  All
 *     Builder AoE modes must be specified before this happens.  If the builder should have a
 *     default value for this parameter, it must be set after setLayout is called.  Otherwise
 *     it will fail since the parameter will not exist yet. 
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
 *     allowing the user of the Builder to specify a subset of them for some purpose.  
 *     Usually this parameter is used to specify a list of keys that all nodes being built 
 *     will have.
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
 * 
 *   <dd>A pass which is responsible for gathering input from the user, checking that the 
 *       input is correct and makes sense, and creating any new Sub-Builders which may need 
 *       to be run as a result of the input. <br>
 *
 *       Each Setup Pass is broken down into three phases.  The distinction between these 
 *       three phases is purely cosmetic, as there is no difference in how any of them are 
 *       invoked.  They exists to make sub-classing easy, allowing specific sorts of 
 *       functionality to be overridden without having to change other parts and to provide 
 *       a clear delineation for people reading the code. <br>
 * 
 *       Builders execute Setup Passes in the order that they were added to the Builder.  All
 *       Setup Passes should be added in the Builder's constructor, prior to 
 *       {@link #setLayout(PassLayoutGroup) setLayout()} being called.
 *     <p>
 *     <dl>
 *     <dt>validatePhase
 *       <dd>Phase in which parameter values should be extracted from parameters and checked
 *           for consistency and applicability. This Phase is when any class variables that 
 *           need to be set from Parameter values should be set.  It is also a good time to 
 *           start constructing lists of nodes which will be needed by the builder, based 
 *           upon which parameters are set.  These lists can be used in the 
 *           {@link BaseBuilder.ConstructPass#nodesDependedOn} method of later 
 *           ConstructPasses.
 *     
 *     <dt>gatherPhase
 *       <dd>Phase in which outside sources of information can be consulted to ascertain 
 *           information. Examples might include talking to an SQL database or opening up 
 *           a Maya scene to extract information about which characters are in a shot or 
 *           which characters should be in a shot as compared to which characters actually 
 *           are in a shot.
 *     
 *     <dt>initPhase
 *       <dd>Phase in which new Sub-Builders should be created and added to the current 
 *           Builder and in which PlaceHolder Parameters should have their values adjusted 
 *           to be actual parameters. The logic on whether or not a Sub-Builder should be
 *           added is probably properly done in either the validate or the gather Phase.  
 *           The init phase should be solely devoted to creating the new instances and 
 *           adjusting parameters.
 *     </dl>
 * 
 * <dt>ConstructPasses
 * 
 *   <dd>A pass which is responsible for building and/or modifying a group of nodes.
 *       Construct passes are run after all the {@link SetupPass SetupPasses} have been run.
 *       The execution path of a Construct Pass is as follows.
 *       <ol>
 *       
 *       <li> Check for all the nodes that the Pass depends on (as set by the
 *            {@link BaseBuilder.ConstructPass#nodesDependedOn} method) and make sure 
 *            they are all in the current working area. Abort Builder execution if any of 
 *            those nodes are missing.
 *       
 *       <li> Queue all the nodes that are specified by the 
 *            {@link BaseBuilder.ConstructPass#preBuildPhase} method.  Wait for these jobs 
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
 *       execution.  In it, {@link BaseStage stages} should be created and their build 
 *       methods should be called.
 *       
 *     </dl> 
 * </dl>
 * <h2>Execution</h2>
 * The execution path of builder is rather involved and complicated.  The following will 
 * attempt to layout, without too much explanation of what each step is, the steps that are 
 * followed. Further discussion of what each step is and how it works is available in other 
 * parts of the Javadocs.
 * <ul>
 * <li> The first Setup Pass of the top level Builder is run.  It performs the following 
 *      actions.
 *   <ol>
 *   <li> Assign any command-line parameters that are applicable to the current layout pass 
 *        of the Builder and any Namers which have been added to the current Builder as 
 *        Sub-Builders.  If there are any Namers who have had their parameter values mapped 
 *        to parent values, assign the parent values to the child Namer's params.
 *   <li> If the Builder is executing in GUI mode, allow the user to input values for 
 *        parameters in the current layout which accept user values as well as any Namers 
 *        which the Builder has added as Sub-Builders.  Namers which do not have an exposed 
 *        parameters will not be displayed in the GUI, but will have their generateNames() 
 *        method called.
 *   <li> Run the {@link BaseNames#generateNames() generateNames()} method of each Namer that
 *   <li> Run the {@link SetupPass#validatePhase() validatePhase()}.
 *   <li> Run the {@link SetupPass#gatherPhase() gatherPhase()}.
 *   <li> Run the {@link SetupPass#initPhase() initPhase()}.
 *   <li> Loop through all the Builders that have been added to the current Builder as child
 *        Builders.  Find all the parameters whose values have had their values mapped to 
 *        parent values and assign the parent values to the child Builder's params. 
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
 *   <li> Run the {@link BaseBuilder.ConstructPass#preBuildPhase} and wait for the jobs
 *        that is generates to complete successfully.  If the jobs do not all complete 
 *        successfully, an Exception will be thrown and execution will be halted.
 *   <li> Using the list of nodes returned by 
 *        {@link BaseBuilder.ConstructPass#nodesDependedOn}, search for all of the 
 *        nodes and make sure that copies exist in the local working area.  If any of 
 *        these nodes does not exist in the local working area, check it out.  If, for 
 *        some reason, it is impossible to acquire a local copy of one of the nodes (either 
 *        because the node does not exist or it has never been checked-in, an error will
 *        be thrown and execution will be halted.
 *   <li> Run the {@link BaseBuilder.ConstructPass#buildPhase}.
 *   </ol>
 * <li> Queue all of the nodes which have been added to the queue list using the 
 *      {@link #addToQueueList(String)} method and wait until the jobs finish.  Once the jobs
 *      have finished, do a status update on each of the queued nodes and check to make sure
 *      that the entire tree is in the Finished queue state.  If all the trees are not, an
 *      error is thrown and execution is aborted.
 * <li> For each builder, the {@link #performCheckIn()} method is consulted to see if the 
 *      nodes that were created should be checked-in.  If they should be, the 
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
 *      latest version.  Then, all the nodes that need to be checked-in are queued.  If any 
 *      jobs are generated, the Builder waits for them to finish and then checks-in all of 
 *      the nodes. 
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
    
    pNoBuild = false;
    
    pBuilderInformation = builderInformation;
    pStageInfo = builderInformation.getNewStageInformation();
    pSubBuilders = new TreeMap<String, BaseBuilder>();
    pOrderedSubBuilders = new TreeMap<Integer, BaseBuilder>();
    pSubBuilderOrder = new TreeMap<String, Integer>();
    pSubNames = new TreeMap<String, BaseNames>();
    pGeneratedNames = new TreeMap<String, BaseNames>();
    pSetupPasses = new ArrayList<SetupPass>();
    pConstructPasses = new ArrayList<BaseConstructPass>();
    pLockBundles = new ArrayList<LockBundle>();
    pSubBuildersByPass = new MappedArrayList<Integer, String>();
    pQueuedNodes = new TreeSet<String>();
    
    {
      UtilityParam param = 
	new BooleanUtilityParam
	(aReleaseOnError,
	 "Release all the created nodes if an exception is thrown.", 
	 true);
      addParam(param);
    }
    pCurrentPass = 1;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L A Y O U T                                                                          */
  /*----------------------------------------------------------------------------------------*/
  

  /**
   * Sets the hierarchical grouping of builder names which determine the layout of UI 
   * components. <P> 
   * 
   * The given layouts must contain exactly one entry for each builder name.  All 
   * <CODE>null</CODE> entries will cause additional space to be added between the menu 
   * items. Each layout subgroup will be represented by its own submenu off the main layout 
   * group. <P> 
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
    int setupNum = pSetupPasses.size();
    if (num > setupNum)
      throw new IllegalArgumentException
        ("There are more passes in the layout than SetupPasses exist for " +
         "builder (" + getName() + ").");
    if (!pNoBuild) {
      try {
        addConstructPass(new QueueConstructPass());
        addConstructPass(new CheckInConstructPass());
      }
      catch (PipelineException ex) {
        throw new IllegalStateException
         ("Problem adding built in Construct Passes to the Builder.  " +
          "There must be an existing Construct Pass named (CheckInPass) or (QueuePass)." +
          "This needs to be renamed before continuing.");
      }
    }
    {
      UtilityParam param = 
        new EnumUtilityParam
        (aActionOnExistence,
         "What action should the Builder take when a node already exists.",
         ActionOnExistence.Continue.toString(),
         new ArrayList<String>(pBuilderInformation.getAOEModes()));
      addParam(param);
    }
    super.setLayout(layout);
  }
  
  @Override
  public final PassLayoutGroup
  getLayout()
  {
    return getActualLayout();
  }
  
  /**
   * Adds a Selection Key parameter to the current Builder.
   * 
   * This parameter is a list of all the Selection Keys that exist in the Pipeline install,
   * allowing the user of the Builder to specify a subset of them for some purpose. Usually
   * this parameter is used to specify a list of keys that all nodes being built will contain.
   * 
   * @throws PipelineException
   * 
   * @deprecated
   *   The practice of setting selection keys in a Builder has largely been made 
   *   obsolete by the creation of KeyChooser plugins which are able to determine 
   *   the settings for keys at runtime rather than when the node is created by the 
   *   builder.  
   */
  @Deprecated
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
   * Adds a CheckinWhenDone parameter to the current Builder.
   * 
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
  
  /**
   * Adds a ReleaseView parameter to the current builder.
   * 
   * This parameter can be used to specify whether the Builder should release the working area
   * when builder execution is finished.  
   * 
   * @throws PipelineException
   */
  protected final void
  addReleaseViewParam()
  {
    
    UtilityParam param = 
      new EnumUtilityParam
      (aReleaseView,
       "Should the view be released when the builder finishes.",
       ReleaseView.Never.toTitle(),
       ReleaseView.titles()); 
    addParam(param);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds a Sub-Builder to the current Builder, with the given Builder Parameter mapping.
   * <p>
   * Using this method acquires a builder from outside the current jar.  That means there is 
   * certainty about what is exactly being acquired.  Changes made to other builders may have 
   * significant unintentional impact on builders which use this methods.
   * <p>
   * Due to initialization issues, these methods cannot be called from the constructor of a 
   * Builder.  They should only be called in the initPhase of a SetupPass. 
   * <p>
   * @param collectionName
   *   The name of the BuilderCollection.
   *   
   * @param version
   *   The version id of the BuilderCollection
   *   
   * @param vendor
   *   The vendor of the BuilderCollection
   *   
   * @param builderName
   *   The name of the builder in the collection to instantiate.
   * 
   * @param defaultMapping 
   *   Should the Sub-Builder be setup with the default parameter mappings.  This means that
   *   all the parameters which are defined as part of the BaseUtil and BaseBuilder will
   *   be mapped from the parent to this child Builder.  This includes the ForceActionOnExt,
   *   but does not include the CheckinWhenDone param.
   * 
   * @param paramMapping 
   *   A TreeMap containing a set of parameter mappings to make between the child Builder's 
   *   parameters and the parent Builder. 
   * 
   * @param order
   *   The execution order of the child Builder.  The lower the order, the sooner the child
   *   Builder will run.
   * 
   * @throws PipelineException
   *   If an attempt is made to add a Sub-Builder with the same name as one that already
   *   exists or with an order that is already being used.
   */
  public final void 
  addSubBuilder
  (
    String collectionName,
    VersionID version,
    String vendor,
    String builderName,
    boolean defaultMapping,
    TreeMap<ParamMapping, ParamMapping> paramMapping,
    int order
  ) 
    throws PipelineException
  {
    BaseBuilderCollection col = 
      pPlug.newBuilderCollection(collectionName, version, vendor);
    BaseBuilder builder = col.instantiateBuilder(builderName, pClient, pQueue, pBuilderInformation);
  
    addSubBuilder(builder, defaultMapping, paramMapping, order);
  }
  
  /**
   * Adds a Sub-Builder to the current Builder, with the given Builder Parameter mapping.
   * 
   * @param subBuilder
   *   The instance of BaseUtil that is being added as a Sub-Builder. 
   * 
   * @param defaultMapping 
   *   Should the Sub-Builder be setup with the default parameter mappings.  This means that
   *   all the parameters which are defined as part of the BaseUtil and BaseBuilder will
   *   be mapped from the parent to this child Builder.  This includes the ForceActionOnExt,
   *   but does not include the CheckinWhenDone param.
   * 
   * @param paramMapping 
   *   A TreeMap containing a set of parameter mappings to make between the child Builder's 
   *   parameters and the parent Builder. 
   * 
   * @param order
   *   The execution order of the child Builder.  The lower the order, the sooner the child
   *   Builder will run.
   * 
   * @throws PipelineException
   *   If an attempt is made to add a Sub-Builder with the same name as one that already
   *   exists or with an order that is already being used.
   */
  public final void 
  addSubBuilder
  (
    BaseUtil subBuilder,
    boolean defaultMapping,
    TreeMap<ParamMapping, ParamMapping> paramMapping,
    int order
  ) 
    throws PipelineException
  {
    String instanceName = subBuilder.getName();
    if (pSubBuilders.containsKey(instanceName) || pGeneratedNames.containsKey(instanceName) 
      || pSubNames.containsKey(instanceName))
      throw new PipelineException
        ("Cannot add a SubBuilder with the same name ("+ instanceName+") " +
         "as one that already exists.");
    
    if (pOrderedSubBuilders.containsKey(order))
      throw new PipelineException
        ("Cannot add a child builder with order (" + order + ").  " +
         "The child Builder (" + pOrderedSubBuilders.get(order).getPrefixedName() + ") " +
         "already uses that order.");
    
    PrefixedName prefixed = new PrefixedName(getPrefixedName(), instanceName);
    subBuilder.setPrefixedName(prefixed);
    
    pLog.log(Kind.Bld, Level.Fine, 
      "Adding a SubBuilder with instanceName (" + prefixed.toString() + ") to " +
      "Builder (" + getName() + ").");
    
    if (subBuilder instanceof BaseNames)
      pSubNames.put(instanceName, (BaseNames) subBuilder);
    else {
      BaseBuilder sub = (BaseBuilder) subBuilder;
      PassLayoutGroup layout = sub.getLayout();
      if (layout == null)
        throw new PipelineException
          ("The child Builder (" + subBuilder.getName() + ") does not have a valid layout.");
      pSubBuilders.put(instanceName, sub);
      pOrderedSubBuilders.put(order, sub);
      pSubBuilderOrder.put(instanceName, order);
      pSubBuildersByPass.put(getCurrentPass(), instanceName);
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
   * Using this method acquires a builder from outside the current jar.  That means there is 
   * certainty about what is exactly being acquired.  Changes made to other builders may have 
   * significant unintentional impact on builders which use this methods.
   * <p>
   * Due to initialization issues, these methods cannot be called from the constructor of a 
   * Builder.  They should only be called in the initPhase of a SetupPass.
   * 
   * @param collectionName
   *   The name of the BuilderCollection.
   *   
   * @param version
   *   The version id of the BuilderCollection
   *   
   * @param vendor
   *   The vendor of the BuilderCollection
   *   
   * @param builderName
   *   The name of the builder in the collection to instantiate.
   * 
   * @param defaultMapping 
   *   Should the Sub-Builder be setup with the default parameter mappings.  This means that
   *   all the parameters which are defined as part of the BaseUtil and BaseBuilder will
   *   be mapped from the parent to this child Builder.
   * 
   * @param order
   *   The execution order of the child Builder.  The lower the order, the sooner the child
   *   Builder will run.
   * 
   * @throws PipelineException
   *   If an attempt is made to add a Sub-Builder with the same name as one that already
   *   exists or with an order that is already being used.
   */
  public final void 
  addSubBuilder
  (
    String collectionName,
    VersionID version,
    String vendor,
    String builderName,
    boolean defaultMapping,
    int order
  ) 
    throws PipelineException
  {
    BaseBuilderCollection col = 
      pPlug.newBuilderCollection(collectionName, version, vendor);
    BaseBuilder builder = col.instantiateBuilder(builderName, pClient, pQueue, pBuilderInformation);
    
    addSubBuilder(builder, defaultMapping, order);
  }

  /**
   * Adds a Sub-Builder to the current Builder.
   * <p>
   * @param subBuilder
   *   The instance of BaseUtil that is being added as a Sub-Builder. 
   * 
   * @param defaultMapping 
   *   Should the Sub-Builder be setup with the default parameter mappings.  This means that
   *   all the parameters which are defined as part of the BaseUtil and BaseBuilder will
   *   be mapped from the parent to this child Builder.
   * 
   * @param order
   *   The execution order of the child Builder.  The lower the order, the sooner the child
   *   Builder will run.
   * 
   * @throws PipelineException
   *   If an attempt is made to add a Sub-Builder with the same name as one that already
   *   exists or with an order that is already being used.
   */
  public final void 
  addSubBuilder
  (
    BaseUtil subBuilder,
    boolean defaultMapping,
    int order
  ) 
    throws PipelineException
  {
    addSubBuilder(subBuilder, defaultMapping,
                  new TreeMap<ParamMapping, ParamMapping>(), order);
  }

  /**
   * Adds a Sub-Builder to the current Builder.
   * 
   * If there are no existing child Builders, it sets a value of 50 for the child Builder's 
   * order.  If there are other existing child Builders, it adds the Builder to the end of
   * the execution order, incrementing the largest current order by 50.
   * <p>
   * Using this method acquires a builder from outside the current jar.  That means there is 
   * certainty about what is exactly being acquired.  Changes made to other builders may have 
   * significant unintentional impact on builders which use this methods.
   * <p>
   * Due to initialization issues, these methods cannot be called from the constructor of a 
   * Builder.  They should only be called in the initPhase of a SetupPass.
   * 
   * @param collectionName
   *   The name of the BuilderCollection.
   *   
   * @param version
   *   The version id of the BuilderCollection
   *   
   * @param vendor
   *   The vendor of the BuilderCollection
   *   
   * @param builderName
   *   The name of the builder in the collection to instantiate.
   * 
   * @throws PipelineException
   *   If an attempt is made to add a Sub-Builder with the same name as one that already
   *   exists.
   */
  public final void
  addSubBuilder
  (
    String collectionName,
    VersionID version,
    String vendor,
    String builderName
  )
    throws PipelineException
  {
    BaseBuilderCollection col = 
      pPlug.newBuilderCollection(collectionName, version, vendor);
    BaseBuilder builder = col.instantiateBuilder(builderName, pClient, pQueue, pBuilderInformation);
    addSubBuilder(builder);
  }
  
  /**
   * Adds a Sub-Builder to the current Builder.
   * 
   * If there are no existing child Builders, it sets a value of 50 for the child Builder's 
   * order.  If there are other existing child Builders, it adds the Builder to the end of
   * the execution order, incrementing the largest current order by 50.
   * 
   * @param subBuilder
   *   The instance of BaseUtil that is being added as a Sub-Builder. 
   * 
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
    
    int order = 50;
    if (!pOrderedSubBuilders.isEmpty())
      order = pOrderedSubBuilders.lastKey() + 50;
    addSubBuilder(subBuilder, true, new TreeMap<ParamMapping, ParamMapping>(), order);
  }

  
  /**
   * Gets a mapping of all the child Builders, keyed by the names of the Sub-Builders.
   * 
   * This includes Builders which have been prepped already and all of them which haven't.
   * This does not include any Namers.
   * 
   * @return 
   *   A TreeMap containing all the child Builders indexed by their name.
   */
  public final TreeMap<String, BaseBuilder>
  getSubBuilders()
  {
    TreeMap<String, BaseBuilder> toReturn = new TreeMap<String, BaseBuilder>();
    toReturn.putAll(pSubBuilders);
    return toReturn;
  }
  
  public final ArrayList<BaseBuilder>
  getOrderedSubBuilders()
  {
    return new ArrayList<BaseBuilder>(pOrderedSubBuilders.values());
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
   *   If the instance name passed in does not correspond to an existing Sub-Builder.
   */
  private final BaseUtil 
  getSubBuilder
  (
    String instanceName
  )
  {
    if (pSubBuilders.containsKey(instanceName))
      return pSubBuilders.get(instanceName);
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
   * 
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
   * 
   * This means either a child Builder before its Setup Passes were run or a child Namer
   * before generateNames is run.
   * 
   * @param instanceName
   *   The name of the child Namer or child Builder.
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
   * Gets a mapping of all the child Builders that were added in a pass.
   * 
   * This includes Builders which have been prepped already and all of them which haven't.
   * This does not include any Namers.
   * 
   * @param passNumber
   *   The pass number. 
   * 
   * @return 
   *   An ArrayList of all the child Builders that were added in the specified pass
   *   or an empty ArrayList is there were no child Builders added in that pass. 
   */
  public final ArrayList<BaseBuilder>
  getSubBuildersInPass
  (
    int passNumber  
  )
  {
    ArrayList<BaseBuilder> toReturn = new ArrayList<BaseBuilder>();
    
    ArrayList<String> builderNames = pSubBuildersByPass.get(passNumber);
    if (builderNames != null) {
      TreeMap<Integer, BaseBuilder> ordered = new TreeMap<Integer, BaseBuilder>();
      for (String name : builderNames) {
        int order = pSubBuilderOrder.get(name);
        ordered.put(order, (BaseBuilder) getSubBuilder(name));
      }
      toReturn.addAll(ordered.values());
    }
    return toReturn;
  }
  
  /**
   * Get all the Setup Passes for the Builder.
   * 
   * @return
   *   An unmodifiable list of passes
   */
  public final List<SetupPass> 
  getSetupPasses()
  {
    return Collections.unmodifiableList(pSetupPasses);
  }
  
  /**
   * Get all the Lock Bundles for the Builder.
   * 
   * @return
   *   An unmodifiable list of bundles.
   */
  public final List<LockBundle> 
  getLockBundles()
  {
    return Collections.unmodifiableList(pLockBundles);
  }
  
  /**
   * Get all the Construct Passes for the Builder.
   * 
   * @return
   *   An unmodifiable list of passes
   */
  public final List<BaseConstructPass>
  getConstructPasses()
  {
    return Collections.unmodifiableList(pConstructPasses);
  }

  /**
   * Creates a mapping between a parameter in the named Sub-Builder and a parameter
   * in the parent Builder. 
   * 
   * Error checking will cover the existence of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values.<P> 
   *
   * Note that it is not possible to create mappings for parameters which do not have an
   * established type when the mapping is established.  Attempts to make a mapping of 
   * this sort may result in unpredictable results.
   *
   * @param subBuilderName
   * 	The subBuilder the mapping is being created in.
   * 
   * @param subParam
   * 	The Sub-Builder parameter that is being driven.
   * 
   * @param masterParam
   * 	The parent Builder parameter that is driving the Sub-Builder parameter.
   * 
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
   * 
   * Error checking will cover the existence of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values.
   * 
   * Note that it is not possible to create mappings for parameters which do not have an
   * established type when the mapping is established.  Attempts to make a mapping of 
   * this sort may result in unpredictable results.
   *
   * @param subBuilderName
   * 	The subBuilder the mapping is being created in.
   * 
   * @param subParamName
   * 	The name of the Sub-Builder parameter that is being driven.
   * 
   * @param masterParamName
   * 	The name of the parent Builder parameter that is driving the Sub-Builder parameter.
   * 
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
   * 
   * Error checking will cover the existence of both parameters and their implementation
   * of {@link SimpleParamAccess}.  It does not cover that the values in the parameters are
   * of similar types, so it is completely possible that the mapping may fail during
   * execution.  It is up to the authors of Builders to ensure that they are only mapping
   * parameters with identical values. <p>
   *
   * Note that it is not possible to create mappings for parameters which do not have an
   * established type when the mapping is established.  Attempts to make a mapping of 
   * this sort may result in unpredictable results.
   * 
   * @param subBuilderName
   *   The subBuilder the mapping is being created in.
   * 
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
      new GUIExecution(this).run();
    else
      new CommandLineExecution(this).run();
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   L O O P S   A N D   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Method that assigns the built-in params to the right places, so that the other pieces of
   * {@link BaseBuilder} that depends on them can use them.
   * 
   * It is highly advised that all Builders call this as the first action in the validate
   * method in their first {@link SetupPass}.  Otherwise there may be unpredictable 
   * behavior.<p>
   * 
   * This takes care of the following parameters.
   * <ul>
   * <li> UtilContext - Extracts the Util Context value and sets the Builder's context 
   *      from it.
   * <li> ReleaseOnError - Extracts the Release On Error value and sets the pReleaseOnError 
   *      variable from it.
   * <li> ActionOnExistence - Extracts the Action on Existence value and sets the correct 
   *      value in the Stage Information for the Builder.
   * <li> ForceActionOnExt - Extract the boolean value and set the correct flag in the
   *      Stage Information for the Builder.  
   * </ul>
   */
  public final void 
  validateBuiltInParams() 
    throws PipelineException 
  {
    pLog.log(Kind.Bld, Level.Finest, "Validating the built-in Parameters.");
    setContext((UtilContext) getParamValue(aUtilContext));
    pReleaseOnError = getBooleanParamValue(new ParamMapping(aReleaseOnError));
    pStageInfo.setAOEMode(getStringParamValue(new ParamMapping(aActionOnExistence)));
  }
  
  /**
   * Adds a {@link SetupPass} to the current Builder.
   * 
   * @throws PipelineException
   *   If a null SetupPass is passed in.
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
    if (isLocked())
      throw new IllegalStateException
        ("Illegal attempt to add a Setup Pass(" + pass.getName() + ") after " +
         "the Builder has been locked.");
    pSetupPasses.add(pass);
    pass.pPassNumber = pSetupPasses.size();
    pLog.log(Kind.Bld, Level.Fine, 
      "Adding a SetupPass named (" + pass.getName() + ") to the " +
      "Builder identified by (" + getPrefixedName() + ")");
  }
  
  /**
   * Adds a {@link ConstructPass} to the current Builder.
   * 
   * @throws PipelineException
   *   If a null ConstructPass is passed in or if an attempt is made to add a pass which
   *   has already been added.
   */
  public final void
  addConstructPass
  (
    BaseConstructPass pass
  ) 
    throws PipelineException
  {
    if (pass == null)
      throw new PipelineException("Cannot add a null ConstructPass");
    if (isLocked())
      throw new IllegalStateException
        ("Illegal attempt to add a Construct Pass(" + pass.getName() + ") after " +
         "the Builder has been locked.");
    pConstructPasses.add(pass);
    pass.setParentBuilder(this);
    
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
   * Get a Construct pass with the given name.
   * 
   * This can be used to get a Construct Pass from a Sub-Builder which can be used to
   * setup Pass dependencies.  
   * 
   * @param name
   *   The name of the Construct pass.
   * 
   * @return
   *   The Construct pass.
   */
  public BaseConstructPass
  getConstructPass
  (
    String name  
  )
  {
    BaseConstructPass toReturn = null;
    for (BaseConstructPass pass : pConstructPasses) {
      if (pass.getName().equals(name)) {
	toReturn = pass;
	break;
      }
    }
    return toReturn;
  }
  
  /**
   * Have all the setup passes for the current builder and all its child Builder been run?
   */
  public final boolean
  isSetupFinished()
  {
    for (BaseBuilder sub : pSubBuilders.values()) {
       if (!sub.isSetupFinished())
         return false;
    }
    if (pCurrentPass > pSetupPasses.size())
      return true;
    else
      return false;
  }
  
  /**
   * This can be called in the constructor of a builder (must be before 
   * {@link #setLayout(PassLayoutGroup)}) to prevent the default Queue and Check-In passes 
   * from being added to the builder.  
   * <p>
   * Intended for builders which exist solely to handle data-input and which are not
   * actually building any nodes of their own.
   */
  protected void
  noDefaultConstructPasses()
  {
    pNoBuild = true;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * This method can be over-ridden to change the check-in message that is associated with
   * this Builder.
   */
  public String
  getCheckInMessage()
  {
    return "Node tree created using the (" + getName() + ") Builder";
  }

  /**
   * Returns a list of nodes to be checked-in.
   * 
   * By default, this returns the list from {@link #getNodesToCheckIn()} that Builders  
   * can add to using the {@link #addToCheckInList(String)}.<P> 
   * 
   * This method can be overriden to return a different list of nodes if the Builder is
   * using some other method of determining what should be checked-in.
   */
  public LinkedList<String>
  getNodesToCheckIn()
  {
    return getCheckInList();
  }
  
  /**
   * Should this builder check-in the nodes that it has specified with
   * {@link #getNodesToCheckIn()}.
   * 
   * If the parameter CheckinWhenDone exists, this method will attempt to read its value. If
   * the read fails or if the parameter does not exist, <code>false</code> will be returned.
   * <p>
   * This method can be overridden if different behavior is desired.
   */
  public boolean
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
   * 
   * Override this method to change it from the default Minor.
   */
  public VersionID.Level
  getCheckinLevel()
  {
    return VersionID.Level.Minor;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   R E L E A S E    V I E W                                                             */
  /*----------------------------------------------------------------------------------------*/
  
  public final ReleaseView
  getReleaseView()
  {
    if (!hasParam(aReleaseView))
      return ReleaseView.Never;
    else
      return ReleaseView.valueFromString((String) getParamValue(aReleaseView));
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   B U I L D E R   U T I L I T I E S                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Searches for a node in the current working area.
   * 
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
    pLog.log(Kind.Bld, Level.Finer, "Getting the needed node (" + nodeName + ")");
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(nodeName, false);
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    State state = treeComps.getState(nodeName);
    if (state == null  || state == State.Branch)
      throw new PipelineException
        ("The needed node (" + nodeName + ") for builder (" + getPrefixedName() + ") " +
         "does not exist.  Stopping execution.");
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
  
  private final void
  verifyAndLock
  (
    String nodeName  
  )
    throws PipelineException
  {
    pLog.log(Kind.Bld, Level.Finer, "Getting the product node (" + nodeName + ")");
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(nodeName, false);
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    State state = treeComps.getState(nodeName);
    if (state == null  || state == State.Branch)
      throw new PipelineException
        ("The product node (" + nodeName + ") for builder (" + getPrefixedName() + ") " +
         "does not exist.  Stopping execution.");
    switch(state) {
    case WorkingCurrentCheckedInNone:
    case WorkingOtherCheckedInNone:
      throw new PipelineException
        ("The product node (" + nodeName + ") for builder (" + getPrefixedName() + ") " +
         "has never been checked in and cannot be locked.  Stopping execution.");
    default:
      lockLatest(nodeName);
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
   * Traverse the nodes, queuing stale nodes and vouching for Dubious nodes.
   * <p>
   * All dubious nodes need to have already been specified as vouchable using the
   * {@link #addToVouchableList(String)}.
   * 
   * @param nodes
   *   The list of node names to be queued
   * 
   * @throws PipelineException
   */
  public final void 
  queueAndVouch
  (
    List<String> nodes
  ) 
    throws PipelineException
  {
    if (nodes == null)
      return;
    
    ArrayList<String> toQueue = new ArrayList<String>();
    
    TreeSet<String> roots = new TreeSet<String>(nodes);
    TreeMap<String, NodeStatus> stati =
      pClient.status(getAuthor(), getView(), roots, roots, DownstreamMode.None);

    TreeMap<String, NodeStatus> dubiousNodes = new TreeMap<String, NodeStatus>();
    MappedSet<String, String> nodesIDependOn = new MappedSet<String, String>();
    MappedSet<String, String> nodesDependingOnMe = new MappedSet<String, String>();
    
    for (String node : nodes) {
      NodeStatus stat = stati.get(node);
      searchForDubious(stat, dubiousNodes, nodesIDependOn, nodesDependingOnMe, null);
        pClient.status(getAuthor(), getView(), node, false, DownstreamMode.None);
      OverallQueueState qstate = stat.getHeavyDetails().getOverallQueueState();
      if (qstate != OverallQueueState.Dubious)
        toQueue.add(node);
    }
    
    while (!dubiousNodes.isEmpty()) {
      String nodeName = findNextNode(dubiousNodes, nodesIDependOn, nodesDependingOnMe);
      ArrayList<String> node = new ArrayList<String>();
      node.addAll(dubiousNodes.get(nodeName).getSourceNames());
      queueSomeStuff(node);
      pClient.vouch(getAuthor(), getView(), nodeName);
      
      dubiousNodes.remove(nodeName);
      TreeSet<String> targets = nodesDependingOnMe.get(nodeName);
      if (targets != null && !targets.isEmpty()) {
        for (String target : targets ) {
          TreeSet<String> temp = nodesIDependOn.get(target);
          temp.remove(nodeName);
          if (temp.isEmpty())
            nodesIDependOn.remove(target);
          else
            nodesIDependOn.put(target, temp);
        }
      }
    }
  
    queueSomeStuff(toQueue);
  }
  
  /**
   * Recursively search up a tree of nodes to find the dubious nodes.
   * 
   * @param status
   *   Status of the current node.
   * 
   * @param dubiousNodes
   *   A map of the status of the dubious nodes which have been found already.
   * 
   * @param nodesIDependOn
   *   A mapped set of the dubious nodes that each dubious node depends on.
   * 
   * @param nodesDependingOnMe
   *   A mapped set of dubious nodes that are depended on.
   * 
   * @param parent
   *   The previous dubious node found or <code>null</code> if none were found yet.
   */
  private void
  searchForDubious
  (
    NodeStatus status,
    TreeMap<String,NodeStatus> dubiousNodes,
    MappedSet<String, String> nodesIDependOn,
    MappedSet<String, String> nodesDependingOnMe,
    String parent
  )
    throws PipelineException
  {
    String newParent = parent;
    
    String nodeName = status.getName();
    
    OverallQueueState qstate = status.getHeavyDetails().getOverallQueueState();
    if (qstate == OverallQueueState.Dubious) {
      if (pVouchableNodes.contains(nodeName)) {
        dubiousNodes.put(nodeName, status);
        if (parent != null) {
          nodesIDependOn.put(parent, nodeName);
          nodesDependingOnMe.put(nodeName, parent);
        }
        newParent = nodeName;
      }
      else {
        throw new PipelineException
          ("The node (" + nodeName + ") is in a dubious state, but is not in the " +
           "vouchable list.  The queue process cannot continue.");
      }
    }
    for (NodeStatus child : status.getSources()) {
      searchForDubious(child, dubiousNodes, nodesIDependOn, nodesDependingOnMe, newParent);
    }
  }
  
  /**
   * Find the next node that can be queued.
   * 
   * @param dubiousNodes
   *   A map of the status of the dubious nodes which have been found already.
   * 
   * @param nodesIDependOn
   *   A mapped set of the dubious nodes that each dubious node depends on.
   * 
   * @param nodesDependingOnMe
   *   A mapped set of dubious nodes that are depended on.
   * 
   * @return
   *   The next dubious node to address.
   */
  private String
  findNextNode
  (
    TreeMap<String,NodeStatus> dubiousNodes,
    MappedSet<String, String> nodesIDependOn,
    MappedSet<String, String> nodesDependingOnMe 
  )
  {
    if (nodesIDependOn.isEmpty() && nodesDependingOnMe.isEmpty())
      return dubiousNodes.firstKey();
    for (String node : dubiousNodes.keySet()) {
      TreeSet<String> set = nodesIDependOn.get(node);
      if ( set == null)
        return node;
    }
    return null;
  }
  
  private void
  queueAndVouchHlp
  (
    String start,
    NodeStatus stat  
  ) 
    throws PipelineException
  {
    NodeStatus myStat = stat;
    String nodeName = myStat.getName();
    boolean children = false;
    for (NodeStatus child : myStat.getSources()) {
      queueAndVouchHlp(start, child);
      children = true;
    }
    
    if (children)
      myStat = pClient.status(getAuthor(), getView(), nodeName, false, DownstreamMode.None);
    OverallQueueState qstate = myStat.getHeavyDetails().getOverallQueueState();
    switch(qstate) {
      case Dubious:
        {
          if (pVouchableNodes.contains(nodeName)) {
            queueSomeStuff(myStat);
            pClient.vouch(getAuthor(), getView(), nodeName);
          }
          else
            throw new PipelineException
              ("The node (" + nodeName + ") is in a dubious state, but is not in the " +
               "vouchable list.  The queue process cannot continue.");
        }
        break;
      default:
        pLog.log(Kind.Ops, Level.Finest, "Final State (" + qstate + ") on node (" + nodeName + ")");
    }
  }


  /**
   * Queue a bunch of nodes, wait for them to finish, and then check to make sure the node tree
   * is in a finished state.
   */
  private void 
  queueSomeStuff
  (
    List<String> toQueue
  )
    throws PipelineException
  {
    LinkedList<QueueJobGroup> groups = 
      queueNodes(toQueue);
    waitForJobs(groups);
    /* Sleep for 3 seconds to give nfs caching a chance to catch up */
    try {
      Thread.sleep(5000);
    }
    catch(InterruptedException ex) {
      throw new PipelineException
       ("The execution thread was interrupted while waiting for jobs to complete.\n" + 
        Exceptions.getFullMessage(ex));
    }
    if (!areAllFinished(toQueue)) {
      TreeSet<String> badNodes = collectNamesAndKill(toQueue, groups);
      throw new PipelineException
        ("The queue jobs in pass (" + toString() + ") did not " + 
         "finish correctly.\n" +
         "The following nodes reported failure: " + badNodes.toString() + "\n" +
         "All remaining jobs have been terminated.");
    }
  }

  /**
   * @param myStat
   * @throws PipelineException
   */
  private void 
  queueSomeStuff
  (
    NodeStatus myStat
  )
    throws PipelineException
  {
    ArrayList<String> toQueue = 
      new ArrayList<String>(myStat.getSourceNames());
    queueSomeStuff(toQueue);
  }

  /**
   * Queues a group of nodes and returns the job groups that were created for all of the 
   * nodes.
   * 
   * Any nodes which do not need jobs generated for them will be skipped.
   * 
   * @param nodes
   *   The list of node names to be queued
   * 
   * @return
   *   The Job Groups that were created for the nodes that were queued.
   */
  public final LinkedList<QueueJobGroup>
  queueNodes
  (
     List<String> nodes
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
	  "No job was generated for node (" + nodeName + ")\n" + ex.getMessage()); 
      }
    }
    return toReturn;
  }
  
  
  /**
   * Return the current status of a group of queue jobs as relates to Builder
   * execution.
   * 
   * There are only three states which this can return.
   * <ul>
   * <li> Complete - Indicates that all jobs in all the job groups have been successfully
   *      completed.
   * <li> In Progress - Indicates that the jobs are still running, but that none of them have
   *      encountered any problems.
   * <li> Problem - Indicates that at least one of the jobs which was running encountered an 
   *      error.
   * </ul>
   * 
   * @param queueJobs
   *   The list of Job Groups to search for progress.
   * 
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
	case Limbo:
	  pLog.log(Kind.Ops, Level.Warning, "\tThe Job is in limbo."); // FIX THIS!!!
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
   * 
   * The thread will check if all the jobs have completed.  If they have not, it will
   * sleep for 7 seconds and check again until all the jobs in the job groups have
   * completed.
   * 
   * @param jobGroups
   *   The list of job groups to wait on.
   */
  public final void
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
	catch(InterruptedException ex) {
          throw new PipelineException
            ("The execution thread was interrupted while waiting for jobs to complete.\n" + 
             Exceptions.getFullMessage(ex));
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
   * @return 
   *   <code>true</code> if all the node trees are in the Finished state.
   *   Otherwise <code>false</code> is returned.
   * 
   * @throws PipelineException
   *   If there is an error while getting the status for the nodes.
   */
  public final boolean 
  areAllFinished
  (
    List<String> queuedNodes
  ) 
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Finer, "Checking if all the queued nodes finished correctly");
    boolean toReturn = true;
    TreeSet<String> roots = new TreeSet<String>(queuedNodes);
    TreeMap<String, NodeStatus> stati = 
      pClient.status(getAuthor(), getView(), roots, roots, DownstreamMode.None);
    for(String nodeName : queuedNodes) {
      NodeStatus status = stati.get(nodeName);
      toReturn = isTreeFinished(status);
      if(!toReturn)
	break;
    }
    return toReturn;
  }
  
  public final void
  killJobs()
    throws PipelineException
  {
    for (String node : pQueuedNodes) {
      pQueue.killJobs(new NodeID(getAuthor(), getView(), node));
    }
  }

  
  /**
   * Collects a list of all the nodes whose jobs have not completed successfully from
   * a list of roots and then kills all remaining jobs.
   * 
   * @param queuedNodes
   *   The list of node names which were queued.
   * 
   * @param jobGroups
   *   The list of jobGroups which were created when the nodes in the list above were queued.
   * 
   * @return
   *   The list of nodes which did not finish correctly.
   * 
   * @throws PipelineException
   *   If anything bad happens when talking to the queue.
   */
  private TreeSet<String>
  collectNamesAndKill
  (
    List<String> queuedNodes,
    LinkedList<QueueJobGroup> jobGroups
  ) 
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Finest, 
      "Collecting the names of nodes which did not finish correctly.");
    TreeSet<String> toReturn = new TreeSet<String>();
    for(String nodeName : queuedNodes) {
      NodeStatus status = pClient.status(getAuthor(), getView(), nodeName);
      findBadNodes(toReturn, status);
    }
    TreeSet<Long> jobs = new TreeSet<Long>();
    for (QueueJobGroup group : jobGroups) {
      for (Long id : group.getAllJobIDs()) {
        jobs.add(id);
      }
    }
    pQueue.killJobs(jobs);

    return toReturn;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Adds a node to the queue list.
   * <p>
   * This is the list of nodes which will be queued after all the Construct Passes for this 
   * particular builder have been run.
   * <p>
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders. 
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
    if (nodeName == null)
      throw new IllegalArgumentException
        ("It is illegal to pass a (null) value to addToQueueList()");
    pNodesToQueue.add(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding node (" + nodeName + ") to queue list in Builder (" + getPrefixedName() + ").");
  }

  /**
   * Removes a node from the queue list.
   * <p>
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders.
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
    if (nodeName == null)
      throw new IllegalArgumentException
        ("It is illegal to pass a (null) value to removeFromQueueList()");
    pNodesToQueue.remove(nodeName);
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
    if (nodeName == null)
      throw new IllegalArgumentException
        ("It is illegal to pass a (null) value to addToDisableList()");
    pNodesToDisable.add(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding node (" + nodeName + ") to disable list in " +
      "Builder (" + getPrefixedName() + ").");
  }

  /**
   * Removes a node from the Builder's disable list.
   * 
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
    if (nodeName == null)
      throw new IllegalArgumentException
        ("It is illegal to pass a (null) value to removeFromDisableList()");
    pNodesToDisable.remove(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Removing node (" + nodeName + ") from disable list in " +
      "Builder (" + getPrefixedName() + ").");
  }
  
  /**
   * Adds a node to the Builder's check-in list.
   * 
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders.<p>
   * 
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
    if (nodeName == null)
      throw new IllegalArgumentException
        ("It is illegal to pass a (null) value to addToCheckInList()");
    pNodesToCheckIn.add(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding node (" + nodeName + ") to check-in list in " +
      "Builder (" + getPrefixedName() + ").");
  }

  /**
   * Removes a node from the Builder's check-in list.
   * 
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
    if (nodeName == null)
      throw new IllegalArgumentException
        ("It is illegal to pass a (null) value to removeFromCheckInList()");
    pNodesToCheckIn.remove(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Removing node (" + nodeName + ") from check-in list in " +
      "Builder (" + getPrefixedName() + ").");
  }
  
  /**
   * Add a node to the Builder's vouchable list.
   * <p>
   * Nodes in this list can be automatically vouched for 
   * 
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders.<p>
   * 
   * @param nodeName
   *   The name of the node.
   */
  protected final void 
  addToVouchableList
  (
    String nodeName
  )
  {
    if (nodeName == null)
      throw new IllegalArgumentException
        ("It is illegal to pass a (null) value to addToVouchableList()");
    pVouchableNodes.add(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding node (" + nodeName + ") to the vouch list in " +
      "Builder (" + getPrefixedName() + ").");
  }
  
  /**
   * Remove a node from the Builder's vouchable list.
   * 
   * This is a Builder specific list, so adding and removing nodes will not effect
   * other Builders.
   * 
   * @param nodeName
   *   The name of the node.
   */
  protected final void 
  removeFromVouchableList
  (
    String nodeName
  )
  {
    if (nodeName == null)
      throw new IllegalArgumentException
        ("It is illegal to pass a (null) value to removeFromVouchableList()");
    pNodesToCheckIn.remove(nodeName);
    pLog.log(Kind.Bld, Level.Finest, 
      "Removing node (" + nodeName + ") from check-in list in " +
      "Builder (" + getPrefixedName() + ").");
  }

  /**
   * Clears the global queue list.
   * 
   * This method will affect all Builders.  After it is run, none of the nodes that have been
   * added to the queue list will actually be queued.  It should only be used in extreme 
   * cases.
   */
  protected final void 
  clearQueueList()
  {
    pNodesToQueue.clear();
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
   * Gets the list of all the nodes currently in the queue list.
   */
  public final LinkedList<String> 
  getQueueList()
  {
    return new LinkedList<String>(pNodesToQueue);
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


  
  /*----------------------------------------------------------------------------------------*/
  /*  I N F O   W R A P P E R                                                               */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets a default editor for a particular stage function type.
   * 
   * This is a wrapper function for the
   * {@link BuilderInformation.StageInformation#setDefaultEditor(String, PluginContext)}
   * method. If this method is called, it is not necessary to set the same values in the
   * {@link BuilderInformation.StageInformation}.<p>
   * 
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type. This allows high-level builders to override their child builders if they
   * do not agree on what the default editor should be. It is important to remember this when
   * writing builders with sub-builder. A Builder should always set its default editors before
   * instantiating any of its sub-builders. Failure to do so may result in the default editor
   * values being set by the sub-builder.
   */
  public final void
  setDefaultEditor
  (
    String function,
    PluginContext plugin
  )
  {
    pStageInfo.setDefaultEditor(function, plugin);
  }
  
  /**
   * Sets a default editors for a particular stage function types.
   * 
   * This is a wrapper function for the
   * {@link BuilderInformation.StageInformation#setDefaultEditor(String, PluginContext)}
   * method. If this method is called, it is not necessary to set the same values in the
   * {@link BuilderInformation.StageInformation}.<p>
   * 
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type. This allows high-level builders to override their child builders if they
   * do not agree on what the default editor should be. It is important to remember this when
   * writing builders with sub-builder. A Builder should always set its default editors before
   * instantiating any of its sub-builders. Failure to do so may result in the default editor
   * values being set by the sub-builder.
   */
  public final void
  setDefaultEditors
  (
    TreeMap<String, PluginContext> defaultEditors
  )
  {
    if (defaultEditors != null) {
      for (String function : defaultEditors.keySet()) {
        PluginContext plugin = defaultEditors.get(function);
        pStageInfo.setDefaultEditor(function, plugin);
      }
    }
  }
  
  /**
   * Sets a default selection keys for a particular stage function type.
   * 
   * This is a wrapper function for the
   * {@link BuilderInformation.StageInformation#setStageFunctionSelectionKeys(String, TreeSet)}
   * method. If this method is called, it is not necessary to set the same values in the
   * {@link BuilderInformation.StageInformation}.<p>
   * 
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type. This allows high-level builders to override their child builders if they
   * do not agree on what the default keys should be. It is important to remember this when
   * writing builders with sub-builder. A Builder should always set its default keys before
   * instantiating any of its sub-builders. Failure to do so may result in the default keys
   * values being set by the sub-builder.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  public final void
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
   * 
   * This is a wrapper function for the
   * {@link BuilderInformation.StageInformation#setStageFunctionLicenseKeys(String, TreeSet)}
   * method. If this method is called, it is not necessary to set the same values in the
   * {@link BuilderInformation.StageInformation}.<p>
   * 
   * Note that this method is only effective the FIRST time it is called for a particular
   * function type. This allows high-level builders to override their child builders if they
   * do not agree on what the default keys should be. It is important to remember this when
   * writing builders with sub-builder. A Builder should always set its default keys before
   * instantiating any of its sub-builders. Failure to do so may result in the default keys
   * values being set by the sub-builder.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  public final void
  setStageFunctionLicenseKeys
  (
    String function,
    TreeSet<String> keys
  )
  {
    pStageInfo.setStageFunctionLicenseKeys(function, keys);
  }
  
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
  public final boolean
  addAOEMode
  (
    String mode,
    ActionOnExistence aoe
  ) 
    throws PipelineException
  {
    return pBuilderInformation.addAOEMode(mode, aoe);
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
  public final boolean
  addAOEOverride
  (
    String mode,
    String nodeName,
    ActionOnExistence aoe
  )
    throws PipelineException
  {
    return pBuilderInformation.addAOEOverride(mode, nodeName, aoe);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  V E R I F I C A T I O N                                                               */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns a list of Actions required by this Builder, indexed by the toolset that
   * needs to contain them.
   * 
   * Builders should override this method to provide their own requirements.  This
   * validation gets performed after all the Setup Passes have been run but before
   * any Construct Passes are run.
   */
  public MappedArrayList<String, PluginContext>
  getNeededActions()
  {
    return null;
  }
  
  /**
   * What is the value of the Release On Error variable set to.
   */
  public boolean
  releaseOnError()
  {
    return pReleaseOnError;
  }
  
  public BuilderInformation
  getBuilderInformation()
  {
    return pBuilderInformation;
  }
  
  public final StageInformation
  getStageInformation()
  {
    return pStageInfo;
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
   * Release all the nodes that were created during Builder execution.
   */
  public final void
  releaseNodes() 
    throws PipelineException
  {
    BaseStage.cleanUpAddedNodes(pClient, pStageInfo);
  }
  
  /**
   * Release the view the builder is working in. 
   */
  public final void
  releaseView()
    throws PipelineException
  {
    TreeSet<String> names = pClient.getWorkingNames(getAuthor(), getView(), null);
    if (names != null && !names.isEmpty())
      pClient.release(getAuthor(), getView(), names, true);
    pClient.removeWorkingArea(getAuthor(), getView());
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T Y   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Utility method for adding a value to a Set only if it isn't <code>null</code>.
   * 
   * @param value
   *   The value being added if it is not null. 
   * 
   * @param set 
   *   The set to add the value to.
   * 
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
   * as a result of the input.<P>
   * 
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
     * can be used in the {@link BaseBuilder.ConstructPass#nodesDependedOn} method
     * of later ConstructPasses.
     * 
     * @throws PipelineException  If any of the parameter values do not make sense or 
     * result in conflicting circumstances.
     */
    public void 
    validatePhase() 
      throws PipelineException
    {}
    
    /**
     * Phase in which outside sources of information can be consulted to ascertain 
     * information.
     * <p>
     * Examples might include talking to an SQL database or opening up a Maya scene to 
     * extract information about which characters are in a shot or which characters should 
     * be in a shot as compared to which characters actually are in a shot.
     * 
     * @throws PipelineException  If there is an error involved while invoking the outside
     * source of information.
     */
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
      pCurrentPass++;
    }
    
    public final ArrayList<BaseBuilder>
    getSubBuildersAdd()
    {
      return getSubBuildersInPass(pPassNumber);
    }
    
    @Override
    public String
    toString()
    {
      return pDescription;
    }
    
    private static final long serialVersionUID = -2836639845295302403L;
    
    private int pPassNumber;
  }
  
  /**
   *  An abstract parent class for Construct Passes.
   *  <p>
   *  This allows shared functionality between the different sorts of Construct
   *  Passes (only one of which is publicly available to users).  It also allows
   *  users to add their own implementation of Construct Passes, if they want to adjust
   *  how Builders execute.  It is not advised to attempt to add new Construct Passes that 
   *  mirror the functionality provided by the {@link ConstructPass} class (creating nodes
   *  using stages).  There are numerous internal variables that that class manipulates 
   *  inside the Builder structure to ensure proper behavior that are not accessible from
   *  outside this class.  
   */
  public abstract
  class BaseConstructPass
    extends Described
  {
    public 
    BaseConstructPass
    (
      String name,
      String desc
    )
    {
      super(name, desc);
    }
    
    /**
     * The method that will be called when the Construct Pass is run.
     * <p>
     * This needs to be implemented to provide the unique functionality that the 
     * Construct Pass type will provide.
     * 
     * @throws PipelineException
     *   If an error occurs during execution of the pass.
     */
    public abstract void
    run()
      throws PipelineException;
    
    /**
     * What phase of execution does this pass represent.
     * 
     * @return
     *   The phase.
     */
    public ExecutionPhase
    getExecutionPhase()
    {
      return ExecutionPhase.ConstructPass;
    }
    
    private void
    setParentBuilder
    (
      BaseBuilder parent  
    )
    {
      pParent = parent; 
    }

    /**
     * Gets the name of the Builder which created this pass.
     * 
     * @return
     *   The fully prefixed name of the Builder.
     */
    public final PrefixedName
    getParentBuilderName()
    {
      return pParent.getPrefixedName();
    }
    
    /**
     * Gets the Builder which created this pass.
     * 
     * @return
     *   The fully prefixed name of the Builder.
     */
    public final BaseBuilder
    getParentBuilder()
    {
      return pParent;
    }
    
    @Override
    public final String
    toString()
    {
      String message = "";
      if (pParent != null)
        message += pParent.getPrefixedName() + " : ";
      message += getName();
      
      return message;
    }
    
    /**
     * Equals is calculated in the strictest sense of the two objects being the exact same
     * object.
     * <p>
     * This is because it is completely possible for there to be two instances of the same
     * Builder running (say two Shot Builders which are Sub Builders of a Sequence Builder)
     * which have the same Construct Passes. From any comparison of parameters, they would be
     * completely identical. So in order to be able to differentiate between those two, it has
     * to use the actual equals.
     */
    @Override
    public final boolean
    equals
    (
      Object that
    )
    {
      return this == that; 
    }
    
    private static final long serialVersionUID = -8439662271370924857L;

    private BaseBuilder pParent;
  }
  
  /**
   * A pass which is responsible for building and/or modifying a group of nodes.
   * <p>
   * Construct passes are run after all the {@link SetupPass SetupPasses} have been run.
   * <p>
   * The execution path of a Construct Pass is as follows.
   * <ol>
   * <li> Check for all the nodes that the Pass depends on (as set by the
   * {@link BaseBuilder.ConstructPass#nodesDependedOn} method) and make sure they are
   * all in the current working area. Abort Builder execution if any of those nodes are
   * missing.
   * <li> Queue all the nodes that are specified by the
   * {@link BaseBuilder.ConstructPass#preBuildPhase} method.  Wait for these jobs to
   * finish before continuing.  If all the jobs do not successfully complete, terminated
   * Builder execution.
   * <li> Run the build method to construct/modify nodes.
   * 
   */
  public 
  class ConstructPass
    extends BaseConstructPass
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
    public LinkedList<String>
    preBuildPhase()
      throws PipelineException
    {
      return new LinkedList<String>();
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
     * that it builds is in a Finished state, then the 
     * {@link BaseBuilder.ConstructPass#preBuildPhase} method should be used instead.
     */
    public TreeSet<String>
    nodesDependedOn()
    {
      return new TreeSet<String>();
    }
    
    /**
     * A list of nodes which need to exist and should be locked before the pass is run.
     * <p>
     */
    public TreeSet<String>
    getProductNodes()
    {
      return new TreeSet<String>();
    }
    
    /**
     * Executes the stage.
     */
    @Override
    public final void
    run()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finer, 
        "Starting the pre-build phase in the (" + getName() + ").");
      LinkedList<String> neededNodes = preBuildPhase();
      pQueuedNodes.clear();
      if (neededNodes.size() > 0) {
        pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finer, 
          "List of nodes returned by the pre-build phase: (" + neededNodes + ").");
        queueAndVouch(neededNodes);
      }
      
      TreeSet<String> nodesDependedOn = this.nodesDependedOn();
      if (nodesDependedOn == null)
        nodesDependedOn = new TreeSet<String>();
        
      TreeSet<String> productNodes = this.getProductNodes();
      if (productNodes == null)
        productNodes = new TreeSet<String>();
      
      for (String needed : nodesDependedOn)
	neededNode(needed);
      for (String product : productNodes) {
        verifyAndLock(product);
      }
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finer, 
        "Starting the build phase in the (" + getName() + ").");
      buildPhase();
    }
    
    private static final long serialVersionUID = 2397375949761850587L;
  }
  
  private 
  class QueueConstructPass
    extends BaseConstructPass
  {
    public 
    QueueConstructPass()
    {
      super("QueuePass", "Pass which will queue all the nodes add to the queue list, wait" +
      		"for them to finish and then check the networks for correctness.");
    }
    
    @Override
    public void 
    run()
      throws PipelineException
    {
      pQueuedNodes.clear();
      queueAndVouch(getQueueList());
   }
    
    @Override
    public final ExecutionPhase
    getExecutionPhase()
    {
      return ExecutionPhase.Queue;
    }

    private static final long serialVersionUID = -5972737857562423668L;
  }
  
  private 
  class CheckInConstructPass
    extends BaseConstructPass
  {
    public 
    CheckInConstructPass()
    {
      super("CheckInPass", 
            "Pass which checks in all the nodes which were scheduled for checkin");
    }
    
    @Override
    public void 
    run()
      throws PipelineException
    {
      if (performCheckIn()) {
        pLog.log(Kind.Ops, Level.Fine, 
          "Beginning check-in for builder ("+ getPrefixedName() + ").");
        checkInNodes(getNodesToCheckIn(), getCheckinLevel(), getCheckInMessage());
        if (getLockBundles().size() > 0) {
          pLog.log(Kind.Ops, Level.Fine, 
            "Locking appropriate nodes for builder ("+ getPrefixedName() + ").");
          for (LockBundle bundle : getLockBundles()) {
            for (String node : bundle.getNodesToLock())
              lockLatest(node);
            List<String> neededNodes = bundle.getNodesToCheckin();
            queueSomeStuff(neededNodes);
            checkInNodes(bundle.getNodesToCheckin(), VersionID.Level.Micro, 
                         "The tree is now properly locked.");
          }
        }
      }
      else
        pLog.log(Kind.Ops, Level.Fine, 
          "Check-in was not activated for builder (" + getPrefixedName() + ").");
    }
    
    @Override
    public final ExecutionPhase
    getExecutionPhase()
    {
      return ExecutionPhase.Checkin;
    }
    
    private static final long serialVersionUID = -8784882749410198882L;
  }


  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /*
   * Parameter names.
   */
  public final static String aReleaseOnError = "ReleaseOnError";
  public final static String aReleaseView = "ReleaseView";
  public final static String aActionOnExistence = "ActionOnExistence";
  public final static String aSelectionKeys = "SelectionKeys";
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  
  
  private static final long serialVersionUID = 1157778379895394547L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private BuilderInformation pBuilderInformation;
  
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
   * A list of nodes names that need to be queued.
   */
  private LinkedList<String> pNodesToQueue = new LinkedList<String>();
  
  /**
   * A list of nodes which the builder is allowed to automatically vouch for.
   */
  private TreeSet<String> pVouchableNodes = new TreeSet<String>();

  /**
   * The list of all associated subBuilders
   */
  private TreeMap<String, BaseBuilder> pSubBuilders;
  
  private TreeMap<Integer, BaseBuilder> pOrderedSubBuilders;
  
  private TreeMap<String, Integer> pSubBuilderOrder;
  
  private MappedArrayList<Integer, String> pSubBuildersByPass;
  
  private TreeMap<String, BaseNames> pSubNames;
  
  private TreeMap<String, BaseNames> pGeneratedNames;
  
  private ArrayList<SetupPass> pSetupPasses;
  
  private ArrayList<BaseConstructPass> pConstructPasses;
  
  private ArrayList<LockBundle> pLockBundles;

  private int pCurrentPass;
  
  private ActionOnExistence pActionOnExistence;
  
  private StageInformation pStageInfo;
  
  /**
   * A list of nodes that the builder knows have been queued (either in a preBuild phase or
   * a {@link BaseBuilder.QueueConstructPass}.  This list is used when an error has occurred 
   * and jobs need to be killed.
   */
  private TreeSet<String> pQueuedNodes;
  
  private boolean pNoBuild;
}
