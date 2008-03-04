// $Id: BaseBuilderExecution.java,v 1.2 2008/03/04 08:15:15 jesse Exp $

package us.temerity.pipeline.builder.execution;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.MultiMap.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.*;
import us.temerity.pipeline.math.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   B U I L D E R   E X E C U T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Execute a builder.
 *
 */
public abstract
class BaseBuilderExecution
{
  /**
   * Constructor.
   * 
   * @param builder
   *   The Builder to execute.
   *   
   * @throws PipelineException
   *   If <code>null</code> is passed in for the Builder.
   */
  public
  BaseBuilderExecution
  (
    BaseBuilder builder  
  )
    throws PipelineException
  {
    if (builder == null)
      throw new PipelineException("The builder param cannot be (null");
    
    pBuilder = builder;
    pRunningBuilder = null;
    pRunBuilders = new LinkedList<BaseBuilder>();
    
    pSetupPassQueue = new LinkedList<SetupPassBundle>();
    pExecutionOrder = new LinkedList<BaseConstructPass>();
    pExecutionOrderNames = new LinkedList<String>();
    
    pLog = LogMgr.getInstance();
    
    pDidNodesReleaseCorrectly = null;
    
    setPhase(ExecutionPhase.SetupPass);
  }
  
  public abstract void
  run()
    throws PipelineException;
  
  protected abstract void
  handleException
  (
    Exception ex
  )
    throws PipelineException;
  
  
  /**
   * Runs all the SetupPasses.
   * <p>
   * It will run each SetupPass, then run this method recursively for all the child Builders
   * that exist. Once they finish running, it will return and this Builder will run its next
   * SetupPass. This will continue until there are no more SetupPasses to run.
   */
  protected final void
  executeFirstLoop()
    throws PipelineException
  {
    initialSetupPases();
    
    while (!pSetupPassQueue.isEmpty()) {
      initNextSetupPass();
      executeNextSetupPass();
    }
  }
  
  /**
   * Initializes the first group of setup passes.
   */
  protected final void
  initialSetupPases()
  {
    ArrayList<SetupPass> initialPasses = new ArrayList<SetupPass>(pBuilder.getSetupPasses());
    Collections.reverse(initialPasses);
    for (SetupPass pass : initialPasses)
      pSetupPassQueue.addFirst(new SetupPassBundle(pass, pBuilder));
  }
  
  /**
   * Assigns command line params to the next SetupPass and its namers.
   * 
   * @throws PipelineException
   *   If pass execution fails.
   */
  protected final void
  initNextSetupPass()
    throws PipelineException
  {
    SetupPassBundle bundle = pSetupPassQueue.peek();
    if (bundle == null)
      throw new PipelineException
        ("A call was made to initNextSetupPass() when there were no passes " +
         "in the queue to be executed.");
    if (getPhase() != ExecutionPhase.SetupPass)
      throw new PipelineException
        ("Illegal attempt to inita Setup Pass when the Builder Executor is not in the " +
         "appropriate phase.  The current phase is (" + getPhase() + ")");

    BaseBuilder builder = bundle.getOwningBuilder();
    BuilderInformation info = builder.getBuilderInformation();
    
    assignCommandLineParams(builder, info);
    
    for (BaseNames namer : builder.getNamers().values()) {
      assignCommandLineParams(namer, info);
      initializeSubBuilder(namer, builder);
    }

  }
  
  /**
   * Removes the next SetupPass from the Queue and runs it.
   * <p>
   * This will result in any all new SetupPasses from any child Builders created during
   * this pass being added to the front of the queue.
   * 
   * @throws PipelineException
   *   If pass execution fails.
   */
  protected final void
  executeNextSetupPass()
    throws PipelineException
  {
    SetupPassBundle bundle = pSetupPassQueue.poll();
    if (bundle == null)
      throw new PipelineException
        ("A call was made to executeNextSetupPass() when there were no passes " +
         "in the queue to be executed.");
    if (getPhase() != ExecutionPhase.SetupPass)
      throw new PipelineException
        ("Illegal attempt to execute a Setup Pass when the Builder Executor is not in the " +
         "appropriate phase.  The current phase is (" + getPhase() + ")");
    
    SetupPass pass = bundle.getPass();
    BaseBuilder builder = bundle.getOwningBuilder();
    
    pass.run();
    
    for (BaseBuilder child : pass.getSubBuildersAdd()) {
      initializeSubBuilder(child, builder);
      ArrayList<SetupPass> initialPasses = 
        new ArrayList<SetupPass>(child.getSetupPasses());
      Collections.reverse(initialPasses);
      for (SetupPass initialPass: initialPasses)
        pSetupPassQueue.addFirst(new SetupPassBundle(initialPass, child));
    }
  }
  
  /**
   * Get the SetupPassBundle that is at the top of the queue.
   * 
   * @return
   *   The Next SetupPassBundle to run or <code>null</code> if there are no more Passes
   *   to be run.
   */
  protected final SetupPassBundle
  peekNextSetupPass()
  {
    return pSetupPassQueue.peek();
  }
  
  /**
   * Creates an execution order for all the ConstructPasses.
   * <p>
   * Loops through all the ConstructPasses that have been registered. Those that have
   * dependencies as set by 
   * {@link BaseBuilder#addPassDependency(ConstructPass, ConstructPass)} will be deferred 
   * until their dependencies have run.
   * 
   * @throws PipelineException
   *   If there is no way to resolve the execution order due to unresolvable dependencies.
   */
  protected final void
  buildSecondLoopExecutionOrder() 
  {
    pLog.log(Kind.Bld, Level.Finer, "Building the Second Loop Execution Order.");
    pExecutionOrder = new LinkedList<BaseConstructPass>();
    for (BaseBuilder subBuilder : pBuilder.getOrderedSubBuilders())
      collectConstructPasses(subBuilder);
    for (BaseConstructPass pass : pBuilder.getConstructPasses()) {
      pExecutionOrder.add(pass);
      pExecutionOrderNames.add(pass.toString());
    }
  }
  
  private void 
  collectConstructPasses
  (
    BaseBuilder builder
  )
  {
    for (BaseBuilder subBuilder : builder.getOrderedSubBuilders())
      collectConstructPasses(subBuilder);
    for (BaseConstructPass pass : builder.getConstructPasses()) {
      pExecutionOrder.add(pass);
      pExecutionOrderNames.add(pass.toString());
    }
  }

  /**
   * Runs all the ConstructPasses in the order that was determined by
   * {@link #buildSecondLoopExecutionOrder()}.
   * 
   * @throws PipelineException
   *   If there are any errors during pass execution.
   */
  protected final void
  executeSecondLoop()
    throws PipelineException
  {
    pLog.log(Kind.Ops, Level.Fine, "Beginning execution of ConstructPasses.");
    while (!pExecutionOrder.isEmpty()) {
      executeNextConstructPass();
    }
  }
  
  /**
   * Get the ConstructPass that is at the top of the queue.
   * 
   * @return
   *   The Next ConstructPassto run or <code>null</code> if there are no more Passes
   *   to be run.
   */
  protected final BaseConstructPass
  peekNextConstructPass()
  {
    return pExecutionOrder.peek();
  }
  
  /**
   * Removes the next ConstructPass from the Queue and runs it.
   * @throws PipelineException
   *   If pass execution fails or if there are no passes in the queue to run.
   */
  protected final void
  executeNextConstructPass()
    throws PipelineException
  {
    BaseConstructPass pass = pExecutionOrder.poll();
    if (pass == null)
      throw new PipelineException
        ("A call was made to executeNextConstructPass() when there were no passes " +
         "in the queue to be executed.");
    BaseBuilder oldBuilder = pRunningBuilder;
    pRunningBuilder = pass.getParentBuilder();
    if (oldBuilder != pRunningBuilder)
      pRunBuilders.add(oldBuilder);
    setPhase(pass.getExecutionPhase());
    pass.run();
  }
  
  /**
   * Gets a list of the full names (including the builder who created it) of all the 
   * ConstructPasses in the order that they are being executed.
   * 
   * @return
   *   The list of pass names.
   */
  protected final List<String>
  getExecutionOrderNames()
  {
    return Collections.unmodifiableList(pExecutionOrderNames);  
  }
  
  /**
   * Releases any registered nodes.
   * <p>
   * This catches and deals with any exceptions that occur while releasing the nodes.  These
   * are reported in the log, but the exception does not escape.
   * 
   * @param builder
   *   The builder whose nodes need to be released.
   * 
   * @throws PipelineException
   *   May be thrown depending on the behavior of {@link #handleException(Exception)}.
   *   
   */
  protected void
  releaseNodes
  (
    BaseBuilder builder  
  )
    throws PipelineException
  {
    try {
      setPhase(ExecutionPhase.Release);
      builder.releaseNodes();
    }
    catch(Exception ex) {
      handleException(ex);
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
  protected final void
  assignCommandLineParams
  (
    BaseUtil utility,
    BuilderInformation info
  )
    throws PipelineException
  {
    boolean abort = info.abortOnBadParam();
    
    int currentPass = utility.getCurrentPass();
    TreeSet<String> passParams = utility.getPassParamNames(currentPass);
    
    String prefixName = utility.getPrefixedName().toString();
    
    MultiMap<String, String> specificEntrys = 
      info.getCommandLineParams().get(prefixName);
    
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
  protected void
  initializeSubBuilder
  (
    BaseUtil subBuilder,
    BaseBuilder parent
  )
  {
    SortedMap<ParamMapping, ParamMapping> paramMapping = subBuilder.getMappedParams();
    pLog.log(Kind.Ops, Level.Fine, "Initializing the subBuilder (" + subBuilder.getPrefixedName() + ").");
    for (ParamMapping subParamMapping : paramMapping.keySet()) {
      ParamMapping masterParamMapping = paramMapping.get(subParamMapping);
      
      if (masterParamMapping.equals(ParamMapping.NullMapping))
        continue;
      
      UtilityParam subParam = subBuilder.getParam(subParamMapping);
      UtilityParam masterParam = parent.getParam(masterParamMapping);
      
      assert (subParam != null) : "The subParam value should never be null.";
      assert (masterParam != null) : "The masterParam value should never be null.";
      
      subBuilder.setParamValue(subParamMapping, 
        ((SimpleParamAccess) masterParam).getValue());
      pLog.log(Kind.Ops, Level.Finer, 
        "Mapped param (" + subParamMapping + ") in subBuilder " + 
        "(" + subBuilder.getPrefixedName() + ") to value from param (" + masterParamMapping + ") " + 
        "in builder (" + parent.getPrefixedName() + ")");
    }
  }

  
  /**
   * Verifies that all the needed Actions for all Builders are actually part of 
   * the appropriate toolsets.
   * 
   * @throws PipelineException 
   *   If any of the needed actions are not present in the right toolsets.
   */
  protected final void
  checkActions()
    throws PipelineException
  {
    MappedArrayList<String, PluginContext> badPlugs = new MappedArrayList<String, PluginContext>();
    checkActionsHelper(pBuilder, badPlugs);
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
    BaseBuilder builder,
    MappedArrayList<String, PluginContext> badPlugs
  )
    throws PipelineException
  {
    MappedArrayList<String, PluginContext> plugs = builder.getNeededActions();
    if (plugs != null) {
      for (String toolset : plugs.keySet()) {
        ArrayList<PluginContext> needed = plugs.get(toolset);
        PluginSet toolsetPlugs =  builder.getMasterMgrClient().getToolsetActionPlugins(toolset);
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
    for (BaseBuilder sub : builder.getSubBuilders().values()) {
      checkActionsHelper(sub, badPlugs);
    }
  }
  
  /**
   * Were all the nodes created released correctly after an error occurred.
   * 
   * @return
   *   <code>null</code> if the Builder did not error out and attempt to release nodes.  
   *   Otherwise, returns a boolean depending on whether the attempt at releasing the nodes
   *   was successful.
   */
  protected Boolean
  didNodesReleaseCorrectly()
  {
    return pDidNodesReleaseCorrectly;
  }
  
  protected BaseBuilder 
  getBuilder()
  {
    return pBuilder;
  }
  
  protected BaseBuilder
  getRunningBuilder()
  {
    return pRunningBuilder; 
  }
  
  /**
   * Gets a list of Builders 
   * @return
   */
  protected List<BaseBuilder>
  getRunBuilders()
  {
    return Collections.unmodifiableList(pRunBuilders);
  }
  
  protected synchronized final ExecutionPhase 
  getPhase()
  {
    return pPhase;
  }

  
  protected synchronized final void 
  setPhase
  (
    ExecutionPhase phase
  )
  {
    pPhase = phase;
  }

  
  
  
  protected
  class SetupPassBundle
  {
    /**
     * Private constructor
     * 
     * @param pass
     *   The Setup Pass
     * @param builder
     *   The Builder the Setup Pass comes from.
     */
    private 
    SetupPassBundle
    (
      SetupPass pass,
      BaseBuilder builder
    )
    {
      super();
      pPass = pass;
      pOwningBuilder = builder;
    }
    
    protected SetupPass 
    getPass()
    {
      return pPass;
    }
    
    protected BaseBuilder 
    getOwningBuilder()
    {
      return pOwningBuilder;
    }
    
    @Override
    public String 
    toString()
    {
      return pPass.toString();
    }
    
    private SetupPass pPass;
    private BaseBuilder pOwningBuilder;
  }
  
  
  protected LogMgr pLog;
  
  private LinkedList<SetupPassBundle> pSetupPassQueue;
  
  private BaseBuilder pBuilder;
  
  private LinkedList<BaseConstructPass> pExecutionOrder;
  private LinkedList<String> pExecutionOrderNames;
  
  private Boolean pDidNodesReleaseCorrectly;
  
  /**
   * The currently executing builder
   */
  private ExecutionPhase pPhase;
  
  /**
   * The currently running builder.
   */
  private BaseBuilder pRunningBuilder;
  
  /**
   * List of builders that have already run.
   */
  private LinkedList<BaseBuilder> pRunBuilders;

  
  
}
