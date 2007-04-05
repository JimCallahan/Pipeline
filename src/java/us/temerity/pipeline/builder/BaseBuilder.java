package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.stages.BaseStage;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

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
    pSubBuilderOffset = new TreeMap<String, Integer>();
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
  
  public final void 
  addSubBuilder
  (
    String instanceName, 
    HasBuilderParams subBuilder, 
    TreeMap<ParamMapping, ParamMapping> paramMapping,
    int offset
  ) 
    throws PipelineException
  {
    if ( pSubBuilders.containsKey(instanceName) || pPreppedBuilders.containsKey(instanceName)
      || pGeneratedNames.containsKey(instanceName) || pSubNames.containsKey(instanceName) )
      throw new PipelineException
      ("Cannot add a SubBuilder with the same name ("+ instanceName+")" +
       " as one that already exists.");
    
    
    addMappedParams(instanceName, paramMapping);

    {
      String prefix = getNamedPrefix();

      if(prefix == null)
	prefix = this.getName();
      prefix += "-" + instanceName;

      subBuilder.setNamedPrefix(prefix);
    }
    
    if (subBuilder instanceof BaseNames)
      pSubNames.put(instanceName, (BaseNames) subBuilder);
    else {
      pSubBuilders.put(instanceName, (BaseBuilder) subBuilder);
      pSubBuilderOffset.put(instanceName, offset);
    }
  }

  /**
   * Adds a Sub-Builder to this builder.
   * <p>
   * This methods uses the {@link BaseBuilder#getName()} method on the Sub-Builder 
   * to set the name of the Sub-Builder.  This means that if this method is called twice
   * with two identical  
   * @param subBuilder
   */
  public final void 
  addSubBuilder
  (
    HasBuilderParams subBuilder
  ) 
    throws PipelineException
  {
    String instanceName = subBuilder.getName();
    sLog.log(Kind.Ops, Level.Finer, 
             "Adding a Sub-Builder named (" + instanceName + ") to " +
             "the Builder named (" + getName() + ")");
    addSubBuilder(instanceName, subBuilder, new TreeMap<ParamMapping, ParamMapping>(), 1);
  }
  
  public final void 
  addSubBuilder
  (
    HasBuilderParams subBuilder,
    int offset
  ) 
    throws PipelineException
  {
    String instanceName = subBuilder.getName();
    addSubBuilder(instanceName, subBuilder, new TreeMap<ParamMapping, ParamMapping>(), offset);
  }

  public TreeMap<String, BaseBuilder>
  getSubBuilders()
  {
    return pSubBuilders;
  }
  
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
    HasBuilderParams subBuilder = getSubBuilder(subBuilderName);
    
    if (!subBuilder.hasSimpleParam(subParamName))
      throw new PipelineException
      ("Illegal attempt mapping a Builder parameter to a SubBuilder.\n" +
       "The Parameter (" + subParamName + ") is not a Simple Parameter in the Sub Builder " +
       "identified with (" + subBuilder.getName() +  "), " +
       "making the attempted mapping invalid.\n" +
       "The full attempted mapping was of (" + masterParamName + ") " +
       "in the master to ("+ subParamName + ") in the sub Builder." );
      
    if (!hasSimpleParam(masterParamName))
      throw new PipelineException
      ("Illegal attempt mapping a Builder parameter to a SubBuilder.\n" +
       "The Parameter (" + masterParamName + ") is not a Simple Parameter in this Builder " +
       "named (" + getName() +  "), making the attempted mapping invalid.\n" +
       "The full attempted mapping was of (" + masterParamName + ") " +
       "in the master to ("+ subParamName + ") in the sub Builder." );

    subBuilder.addParamMapping(new ParamMapping(subParamName), 
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
    HasBuilderParams subBuilder = getSubBuilder(subBuilderName);
    
    if (!subBuilder.hasSimpleParam(subParamName, subKeys)) 
      throw new PipelineException
        ("Illegal attempt mapping a Builder parameter to a SubBuilder.\n" +
	 "The Parameter (" + subParamName + ") does not contain " +
	 "a Simple Parameter defined by the keys " + subKeys + " " +   
	 "in the Sub Builder identified with (" + subBuilder.getName() +  "), " +
	 "making the attempted mapping invalid.\n" +
	 "The full attempted mapping was of (" + masterParamName + ") " + masterKeys + " " +
	 "in the master to ("+ subParamName + ") " + subKeys + " in the sub Builder." );

    if (!hasSimpleParam(masterParamName, masterKeys))
      throw new PipelineException
        ("Illegal attempt mapping a Builder parameter to a SubBuilder.\n" +
	 "The Parameter (" + masterParamName + ") does not contain " +
	 "a Simple Parameter defined by the keys " + masterKeys + " " +   
	 "in this Builder identified with (" + getName() +  "), " +
	 "making the attempted mapping invalid.\n" +
	 "The full attempted mapping was of (" + masterParamName + ") " + masterKeys + " " +
	 "in the master to ("+ subParamName + ") " + subKeys + " in the sub Builder." );
    
    ParamMapping subMapping = new ParamMapping(subParamName, subKeys);
    ParamMapping masterMapping = new ParamMapping(masterParamName, masterKeys);
    
    subBuilder.addParamMapping(subMapping, masterMapping);
  }
  
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
    sLog.log(Kind.Ops, Level.Finer, "Initializing the subBuilder (" + name + ").");
    for (ParamMapping subParamMapping : paramMapping.keySet()) {
      ParamMapping masterParamMapping = paramMapping.get(subParamMapping);
      
      BuilderParam subParam = subBuilder.getParam(subParamMapping);
      BuilderParam masterParam = this.getParam(masterParamMapping);
      
      assert (subParam != null) : "The subParam value should never be null.";
      assert (masterParam != null) : "The masterParam value should never be null.";
      
      subBuilder.setParamValue(subParamMapping, 
	((SimpleParamAccess) masterParam).getValue());
      sLog.log(Kind.Ops, Level.Finest, 
	"Mapped param (" + subParamMapping + ") in subBuilder " + 
	"(" + name + ") to value from param (" + masterParamMapping + ") " + 
	"in builder (" + getName() + ")");
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   E X E C U T I O N                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  public final void 
  run()
    throws PipelineException
  {
    if (sUsingGUI == true)
      ;
    else
      runCommandLine();
  }
  
  private final void 
  runCommandLine()
    throws PipelineException
  {
    boolean finish = false;
    try {
      executeFirstLoop();
      executeSecondLoop();
      waitForJobs(queueJobs());
      finish = didTheNodesFinishCorrectly(sNodesToQueue);
    }
    catch (Exception ex) {
      String logMessage = "An Exception was thrown during the course of execution.\n";
      if (pReleaseOnError) {
	logMessage += "All the nodes that were registered will now be released.\n";
	BaseStage.cleanUpAddedNodes();
      }
      logMessage += ex.getMessage();
      ex.printStackTrace();
      sLog.log(Kind.Ops, Level.Severe, logMessage);
      return;
    }
    if (finish)
      executeCheckIn();
    else
      throw new PipelineException("Execution halted.  Jobs didn't finish correctly");
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
    setGlobalContext((UtilContext) getParamValue(aUtilContext));
    pReleaseOnError = getBooleanParamValue(new ParamMapping(aReleaseOnError));
    pActionOnExistance = 
      ActionOnExistance.valueOf(getStringParamValue(new ParamMapping(aActionOnExistance)));
  }
  
  public final void
  addSetupPass
  (
    SetupPass pass
  )
  {
    pFirstLoopPasses.add(pass);
  }
  
  public final void
  addConstuctPass
  (
    ConstructPass pass
  )
  {
    pSecondLoopPasses.add(pass);
  }
  
  private final void
  executeFirstLoop()
    throws PipelineException
  {
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

  private final void
  executeSecondLoop()
    throws PipelineException
  {
    MappedLinkedList<Integer, String> subBuilders =
      new MappedLinkedList<Integer, String>(pSubBuilderOffset);
    if(subBuilders == null)
      subBuilders = new MappedLinkedList<Integer, String>();
    LinkedList<String> runFirst = subBuilders.get(0);
    if(runFirst != null) {
      for(String sub : runFirst) {
	BaseBuilder builder = pPreppedBuilders.get(sub);
	if (builder != null)
	  builder.executeSecondLoop();
      }
      subBuilders.remove(0);
    }
    int i = 1;
    for(ConstructPass pass : pSecondLoopPasses) {
      pass.run();
      LinkedList<String> run = subBuilders.get(i);
      if(run != null) {
	for(String sub : run) {
	  BaseBuilder builder = pPreppedBuilders.get(sub);
	  if (builder != null)
	    builder.executeSecondLoop();
	}
	subBuilders.remove(i);
      }
      i++;
    }
    for (Integer order : subBuilders.keySet()) {
      LinkedList<String> run = subBuilders.get(order);
      for(String sub : run) {
	BaseBuilder builder = pPreppedBuilders.get(sub);
	if (builder != null)
	  builder.executeSecondLoop();
      }
    }
  }
  
  private final void 
  executeCheckIn()
    throws PipelineException
  {
    if (performCheckIn()) {
      checkInNodes(getNodesToCheckIn(), VersionID.Level.Minor, getCheckInMessage());
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  protected String
  getCheckInMessage()
  {
    return "Node tree created using the (" + getName() + ") Builder";
  }
  
  protected abstract TreeSet<String>
  getNodesToCheckIn();
  
  protected boolean
  performCheckIn()
  {
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   B U I L D E R   U T I L I T I E S                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  protected final boolean
  checkExistance
  (
    String nodeName
  ) 
    throws PipelineException
  {
    boolean exists = nodeExists(nodeName);
    if (!exists) 
      return false;
    if (pActionOnExistance.equals(ActionOnExistance.Abort))
      throw new PipelineException
        ("The node (" + nodeName + ") exists.  Aborting Builder operation as per " +
         "the setting of the ActionOnExistance parameter for the builder " +
         "( " + getName() +  " )");
    NodeLocation location = getNodeLocation(nodeName);
    switch(location) {
    case OTHER:
      throw new PipelineException
        ("The node (" + nodeName + ") exists, but in a different working area and was never " +
         "checked in.  The Builder is aborting due to this problem.");
    case LOCALONLY:
      return true;
    case LOCAL:
      switch(pActionOnExistance) {
      case CheckOut:
	 sClient.checkOut(getAuthor(), getView(), nodeName, null, 
	   CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	return true;
      case Continue:
	return true;
      }
    case REP:
      switch(pActionOnExistance) {
      case CheckOut:
	 sClient.checkOut(getAuthor(), getView(), nodeName, null, 
           CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
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
    boolean error = false;
    boolean done = false;
    for(QueueJobGroup job : queueJobs) {
      TreeSet<Long> stuff = new TreeSet<Long>();
      stuff.add(job.getGroupID());
      TreeMap<Long, JobStatus> statuses = sQueue.getJobStatus(stuff);
      for(JobStatus status : statuses.values()) {
	String nodeName = status.getNodeID().getName();
	sLog.log(Kind.Ops, Level.Finest, 
	  "Checking the status of Job (" + status.getJobID() + ") " +
	  "for node (" + nodeName + ").");
	
	JobState state = status.getState();
	if(state.equals(JobState.Failed) || state.equals(JobState.Aborted)) {
	  sLog.log(Kind.Ops, Level.Finest, "\tThe Job did not completely successfully"); 
	  error = true;
	  break;
	}
	if(!state.equals(JobState.Finished)) {
	  sLog.log(Kind.Ops, Level.Finest, "\tThe Job is still running."); 
	  done = false;
	  break;
	}
	done = true;
      }
      if(error)
	return JobsState.Problem;
      if(!done)
	return JobsState.InProgress;
    }
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
          sLog.log(Kind.Ops, Level.Finer, "Sleeping for 5 seconds before checking jobs again.");
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
  }

  protected final void 
  removeFromQueueList
  (
    String nodeName
  )
  {
    sNodesToQueue.remove(nodeName);
  }

  protected final void 
  addToDisableList
  (
    String nodeName
  )
  {
    pNodesToDisable.add(nodeName);
  }

  protected final void 
  removeFromDisableList
  (
    String nodeName
  )
  {
    pNodesToDisable.remove(nodeName);
  }
  
  protected final void 
  addToCheckInList
  (
    String nodeName
  )
  {
    pNodesToCheckIn.add(nodeName);
  }

  protected final void 
  removeFromCheckInList
  (
    String nodeName
  )
  {
    pNodesToCheckIn.remove(nodeName);
  }

  protected final void 
  clearQueueList()
  {
    sNodesToQueue.clear();
  }

  protected final void 
  clearDisableList()
  {
    pNodesToDisable.clear();
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

  /**
   * A list of nodes names who need to be queued.
   */
  private static TreeSet<String> sNodesToQueue = new TreeSet<String>();

  /**
   * Instance of the log manager for builder logging purposes.
   */
  protected static LogMgr sLog = LogMgr.getInstance();

  /*
   * Parameter names.
   */
  public final static String aUtilContext = "UtilContext";
  public final static String aReleaseOnError = "ReleaseOnError";
  public final static String aActionOnExistance = "ActionOnExistance";
  

  
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
  
  private TreeMap<String, Integer> pSubBuilderOffset;
  
  private ArrayList<SetupPass> pFirstLoopPasses;
  private ArrayList<ConstructPass> pSecondLoopPasses;

  private int pCurrentPass;
  
  private ActionOnExistance pActionOnExistance;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  E N U M E R A T I O N S                                                               */
  /*----------------------------------------------------------------------------------------*/

  protected enum JobsState
  {
    Complete, Problem, InProgress
  }
  
  public static enum ActionOnExistance
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

  public class 
  SetupPass
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
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, "Stub validate phase in the " + pName + ".");      
    }
    
    @SuppressWarnings("unused")
    public void 
    gatherPhase() 
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, "Stub gather phase in the " + pName + ".");
    }
    
    @SuppressWarnings("unused")
    public void
    initPhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, "Stub init phase in the " + pName + ".");
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
  
  public class
  ConstructPass
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
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Finest, "Stub preBuild phase in the " + pName + ".");
      return new TreeSet<String>();
    }
    
    @SuppressWarnings("unused")
    public void
    buildPhase()
      throws PipelineException
    {
      sLog.log(Kind.Ops, Level.Finest, "Stub build phase in the " + pName + ".");
    }
    
    public final void
    run()
      throws PipelineException
    {
      TreeSet<String> neededNodes = preBuildPhase();
      sLog.log(Kind.Ops, Level.Finer, "Queuing the following nodes " + neededNodes + ".");
      if (neededNodes.size() > 0) {
	LinkedList<QueueJobGroup> jobs = queueNodes(neededNodes);
	waitForJobs(jobs);
	if (!didTheNodesFinishCorrectly(neededNodes))
	  throw new PipelineException("The jobs did not finish correctly");
      }
      buildPhase();
    }
    
    private static final long serialVersionUID = 2397375949761850587L;
  }
}
