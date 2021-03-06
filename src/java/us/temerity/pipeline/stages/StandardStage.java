package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;

/*------------------------------------------------------------------------------------------*/
/*   S T A N D A R D   S T A G E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A branch stage designed to make building stages easy.
 */
public abstract
class StandardStage 
  extends BaseStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Constructor that allows the specification of the name of an Editor (with vendor) and an
   * Action (with vendor).
   * 
   * @param name
   *  The name of the stage.
   * @param desc
   *  A description of what the stage should do.
   * @param stageInformation
   *  Class containing basic information shared among all stages.
   * @param context
   *  The {@link UtilContext} that this stage acts in.
   * @param client
   *  The instance of Master Manager that the stage performs all its actions in.
   * @param nodeName
   *  The name of the node that is to be created.
   * @param suffix
   *  The suffix for the created node.
   * @param editor
   *  Contains the name and vendor for the Editor plugin. If this is <code>null</code>
   *  then an editor will be selected based on the Stage Function.
   * @param action
   *  Contains the name and vendor for the Action plugin.
   */
  protected 
  StandardStage
  (
    String name, 
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName, 
    String suffix,
    PluginContext editor, 
    PluginContext action
  ) 
    throws PipelineException
  {
    this(name, desc, stageInformation, 
         context, client, nodeName, suffix, 
         editor, action, StageFunction.aNone);
  }
  
  /**
   * Constructor that allows the specification of the name of an Editor (with vendor) and an
   * Action (with vendor).
   * 
   * @param name
   *  The name of the stage.
   * @param desc
   *  A description of what the stage should do.
   * @param stageInformation
   *  Class containing basic information shared among all stages.
   * @param context
   *  The {@link UtilContext} that this stage acts in.
   * @param client
   *  The instance of Master Manager that the stage performs all its actions in.
   * @param nodeName
   *  The name of the node that is to be created.
   * @param suffix
   *  The suffix for the created node.
   * @param editor
   *  Contains the name and vendor for the Editor plugin. If this is <code>null</code>
   *  then an editor will be selected based on the Stage Function.
   * @param action
   *  Contains the name and vendor for the Action plugin.
   * @param stageFunction
   *   A string which describes what sort of node the stage is building.  This is currently
   *   being used to decide which editor to assign to nodes.  This can be set to 
   *   <code>null</code> if a stage does not want to provide a value.
   */
  protected 
  StandardStage
  (
    String name, 
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName, 
    String suffix,
    PluginContext editor, 
    PluginContext action,
    String stageFunction
  ) 
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client, nodeName, suffix, stageFunction);
    if(editor != null) {
      setEditor(lookupEditor(editor, getToolset()));
    }
    else {
      PluginContext defaultEditor = stageInformation.getDefaultEditor(getStageFunction());
      if (defaultEditor != null)
	setEditor(lookupEditor(defaultEditor, getToolset()));
    }

    if(action != null) {
      setAction(lookupAction(action, getToolset()));
    }
    setNodeFrameRange(null);
    setPadding(-1);
    init();
  }

  
  /**
   * Constructor that allows the specification of the name of an Editor (with vendor) and an
   * Action (with vendor).
   * 
   * @param name
   *        The name of the stage.
   * @param desc
   *        A description of what the stage should do.
   * @param stageInformation
   *        Class containing basic information shared among all stages.
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param client
   *        The instance of Master Manager that the stage performs all its actions in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param range
   *        The frame range for the node.
   * @param padding
   *        The padding for the file numbers. If this is set to <code>null</code>, a
   *        padding of 4 will be used.
   * @param suffix
   *        The suffix for the created node.
   * @param editor
   *        Contains the name and vendor for the Editor plugin. If this is <code>null</code>
   *        then the Maya Editor from Temerity will be used. 
   * @param action
   *        Contains the name and vendor for the Action plugin.
   */
  protected 
  StandardStage
  (
    String name, 
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    PluginContext editor, 
    PluginContext action
  )
    throws PipelineException
  {
    this(name, desc, stageInformation, context, client, nodeName, range, padding, suffix,
         editor, action, StageFunction.aNone);
  }
  

  /**
   * Constructor that allows the specification of the name of an Editor (with vendor) and an
   * Action (with vendor).
   * 
   * @param name
   *        The name of the stage.
   * @param desc
   *        A description of what the stage should do.
   * @param stageInformation
   *        Class containing basic information shared among all stages.
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param client
   *        The instance of Master Manager that the stage performs all its actions in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param range
   *        The frame range for the node.
   * @param padding
   *        The padding for the file numbers. If this is set to <code>null</code>, a
   *        padding of 4 will be used.
   * @param suffix
   *        The suffix for the created node.
   * @param editor
   *        Contains the name and vendor for the Editor plugin. If this is <code>null</code>
   *        then the Maya Editor from Temerity will be used. 
   * @param action
   *        Contains the name and vendor for the Action plugin.
   * @param stageFunction
   *   A string which describes what sort of node the stage is building.  This is currently
   *   being used to decide which editor to assign to nodes.  This can be set to 
   *   <code>null</code> if a stage does not want to provide a value. 
   */
  protected 
  StandardStage
  (
    String name, 
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    PluginContext editor, 
    PluginContext action, 
    String stageFunction
  )
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client, nodeName, suffix, stageFunction);
    if(editor != null) {
      setEditor(lookupEditor(editor, getToolset()));
    }
    else {
      PluginContext defaultEditor = stageInformation.getDefaultEditor(getStageFunction());
      if (defaultEditor != null)
	setEditor(lookupEditor(defaultEditor, getToolset()));
    }
    
    if(action != null) {
      setAction(lookupAction(action, getToolset()));
    }
    if (range == null)
      throw new PipelineException("You must specify a frame range in a FrameNumStage");
    setNodeFrameRange(range);
    if (padding == null)
      setPadding(4);
    else if (padding < 0)
      throw new PipelineException("Cannot have a negative padding value");
    else
      setPadding(padding);
    init();
  }
  
  /**
   * Construct a node identical to the given working version.
   * 
   * @param name
   *        The name of the stage.
   * 
   * @param desc
   *        A description of what the stage should do.
   * 
   * @param stageInformation
   *        Class containing basic information shared among all stages.
   * 
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   *        
   * @param client
   *        The instance of Master Manager that the stage performs all its actions in.
   * 
   * @param mod
   *        The original node working version.
   */
  protected 
  StandardStage
  (
    String name, 
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    NodeMod mod
  )
  {
    this(name, desc, stageInformation, context, client, mod, StageFunction.aNone);
  }
  

  /**
   * Construct a node identical to the given working version.
   * 
   * @param name
   *        The name of the stage.
   * @param desc
   *        A description of what the stage should do.
   * @param stageInformation
   *        Class containing basic information shared among all stages.
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param client
   *        The instance of Master Manager that the stage performs all its actions in.
   * @param mod
   *        The original node working version.
   * @param stageFunction
   *   A string which describes what sort of node the stage is building.  This is currently
   *   being used to decide which editor to assign to nodes.  This can be set to 
   *   <code>null</code> if a stage does not want to provide a value.
   */
  protected 
  StandardStage
  (
    String name, 
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    NodeMod mod, 
    String stageFunction
  )
  {
    super(name, desc, stageInformation, context, client, mod.getName(), 
          mod.getPrimarySequence().getFilePattern().getSuffix(),  stageFunction);

    {
      FileSeq primary = mod.getPrimarySequence();
      FilePattern fpat = primary.getFilePattern();
      setNodeFrameRange(primary.getFrameRange());
      setPadding(fpat.getPadding());

      for (FileSeq sSeq : mod.getSecondarySequences())
        addSecondarySequence(sSeq);
    }

    setEditor(mod.getEditor());
    setAction(mod.getAction());

    setExecutionMethod(mod.getExecutionMethod());
    if(getExecutionMethod() == ExecutionMethod.Parallel)
      setBatchSize(mod.getBatchSize());

    for (LinkMod lmod: mod.getSources())
      addLink(lmod);
    init();
  }


  /*----------------------------------------------------------------------------------------*/

  private void
  init()
  {
    pIsIntermediate = false;
    
    pNodeAdded     = false;
    pNodeConformed = false;
    pNodeSkipped   = false;
  }
  

 
  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   O P S                                                                       */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Constructs the node from all the given pieces.
   * <p>
   * This method looks at the ActionOnExistence parameter and decides what to do.  It can
   * either:
   * <ul>
   * <li> Register a node which doesn't exist.
   * <li> Checkout a node that exists.
   * <li> Throw an exception if a node exists.
   * <li> Continue without doing anything if a node exists
   * <li> Conform an existing node 
   * </ul>
   * If you wish to use simply build or conform a node without having to use the logic
   * that is built into this method, simply override this method in an inheriting stage
   * and call either {@link #construct()} or {@link #conform()} as desired.
   * 
   * @see #conform()
   * @see #construct()
   */
  @Override
  public boolean 
  build() 
    throws PipelineException
  {
    String nodeName = getNodeName();
    pLog.log
      (Kind.Ops, Level.Fine, "Building the node: " + nodeName );

    ActionOnExistence actionOnExistence = 
      pStageInformation.getActionOnExistence(nodeName);
    pLog.log
      (Kind.Bld, Level.Finer, "Action on Existence for the node: " + actionOnExistence);
    if (!checkExistance(nodeName, actionOnExistence))
      return construct();
    else if (actionOnExistence == ActionOnExistence.Conform)
      return conform();
    else {
      pStageInformation.addSkippedNode(nodeName); 
      pNodeSkipped = true;
      pRegisteredNodeMod = 
        pClient.getWorkingVersion(getAuthor(), getView(), getNodeName());
      return false;
    }
  }
  
  /**
   * Constructs the node from all the given pieces.
   * <p>
   * This method does the following things.
   * <ul>
   * <li> Registers the node.
   * <li> Adds the secondary sequences.
   * <li> Creates all the links.
   * <li> Sets the Action.
   * <li> Sets all the Job Requirements
   * <li> Performs a {@link MasterMgrClient#modifyProperties(String, String, NodeMod)} to
   * commit all the updates.
   * <li> Sets the pRegisteredNodeMod to the newly modified working version.
   * <li> If doAnnotations is set in the stage information, then it adds all the stored
   * annotations in the stage to the node.
   * 
   * @return <code>true</code> if the node was registered successfully. <code>false</code>
   *         if something happened and the node was not registered.
   * @see BaseStage#build()
   * @see #build()
   */
  @SuppressWarnings("deprecation")
  public boolean
  construct()
    throws PipelineException
  {
    if (getNodeFrameRange() == null)
      pRegisteredNodeMod = registerNode();
    else
      pRegisteredNodeMod = registerSequence();

    if(pRegisteredNodeMod == null)
      return false;

    addSecondarySequences();

    createLinks();

    if (setAction())
      setJobSettings();
      
    setKeys();

    pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);

    pRegisteredNodeMod = 
      pClient.getWorkingVersion(getAuthor(), getView(), getNodeName());

    if (pStageInformation.doAnnotations()) {
	doAnnotations();
	pRegisteredNodeMod = doVersionAnnotations(pRegisteredNodeMod);
    }

    return true;
  }
  
  /**
   * Modifies an existing node to match all the given pieces.
   * <p>
   * This method does the following things.
   * <ul>
   * <li> Renumbers the node if necessary.
   * <li> Removes all existing secondary sequences and adds new ones
   * <li> Unlinks all existing nodes and makes new links
   * <li> Removes the Editor and Action and adds new ones.
   * <li> Sets all the Job Requirements
   * <li> Performs a {@link MasterMgrClient#modifyProperties(String, String, NodeMod)} to
   * commit all the updates.
   * <li> Sets the pRegisteredNodeMod to the newly modified working version.
   * <li> Removes all existing annotations.
   * <li> If doAnnotations is set in the stage information, then it adds all the stored
   * annotations in the stage to the node.
   * 
   * @return <code>true</code> if the node was registered successfully. <code>false</code>
   *         if something happened and the node was not registered.
   * @see BaseStage#build()
   * @see #build()
   */
  @SuppressWarnings("deprecation")
  @Override
  public boolean 
  conform()
    throws PipelineException
  {
    String nodeName = getNodeName();
    pLog.log
      (Kind.Ops, Level.Finer, "Conforming the node: " + nodeName );
    pStageInformation.addConformedNode(nodeName); 
    pNodeConformed = true;

    NodeID id = new NodeID(getAuthor(), getView(), nodeName);
    FrameRange fRange = getNodeFrameRange();
    {
      NodeStatus status = pClient.status(id, true, DownstreamMode.None);
      NodeDetailsLight details = status.getLightDetails();
      NodeMod oldMod = details.getWorkingVersion(); 
      boolean hasFrameNumbers = oldMod.getPrimarySequence().hasFrameNumbers();
      if (hasFrameNumbers && fRange == null)
        throw new PipelineException
          ("Attempting to conform the node (" + nodeName + ") from one with " +
           "frame numbers to one without frame numbers");
      if (!hasFrameNumbers && fRange != null)
        throw new PipelineException
          ("Attempting to conform the node (" + nodeName + ") from one without " +
           "frame numbers to one with frame numbers");
      
      NodeVersion latest = details.getLatestVersion(); 
      if ( latest != null) {
        String oldSuffix = latest.getPrimarySequence().getFilePattern().getSuffix();
        String newSuffix = latest.getPrimarySequence().getFilePattern().getSuffix();
        if (((oldSuffix == null) && (newSuffix != null)) ||
            ((oldSuffix != null) && !oldSuffix.equals(newSuffix)))
          throw new PipelineException
            ("Attempting to conform the node (" + nodeName + ") from having " +
             ((oldSuffix == null) ? "no suffix" : "the suffix (" + oldSuffix + ")") + " " + 
             "to having " + 
             ((newSuffix == null) ? "no suffix" : "the suffix (" + newSuffix + ")") + ".  " + 
             "Since this node has been checked-in, this is an illegal modification.");
      }
    }

    if (fRange != null)
      pClient.renumber(id, fRange, true);

    pRegisteredNodeMod = pClient.getWorkingVersion(id);
    {
      removeSecondarySequences();
      addSecondarySequences();
      
      removeLinks();
      if(getLinks() != null)
        createLinks();

      pRegisteredNodeMod.setToolset(getToolset());
      pRegisteredNodeMod.setEditor(getEditor());

      if(setAction()) {
        pRegisteredNodeMod.setJobRequirements(new JobReqs());
        setJobSettings();
      }
      
      setKeys();
      
      pRegisteredNodeMod.setIntermediate(pIsIntermediate);

      pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
    }
    pRegisteredNodeMod = pClient.getWorkingVersion(id);

    removeAnnotations();
    if (pStageInformation.doAnnotations()) {
      doAnnotations();
      pRegisteredNodeMod = doVersionAnnotations(pRegisteredNodeMod);
    }

    return true;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Registers a node with the given name and extension, using the given Editor.
   * <P>
   * If either the name or the editor is null, the method will not attempt to register a node.
   * Instead it just return <code>null</code>. This method also adds the registered node to
   * the list stored in the {@link StageInformation}, allowing for its removal in case of
   * errors. Throws a {@link PipelineException} if anything goes wrong with the registration
   * process.
   * 
   * @return The {@link NodeMod} that represents the newly created node.
   */
  protected NodeMod 
  registerNode() 
    throws PipelineException
  {
    String nodeName = getNodeName();
    if(nodeName == null)
      return null;
    NodeMod toReturn = registerNode(nodeName, getSuffix(), getEditor(), pIsIntermediate);
    pNodeAdded = true;
    pStageInformation.addNode(nodeName, getAuthor(), getView());
    return toReturn;
  }
  
  /**
   * Registers a node with the given name and extension, using the given Editor.
   * <P>
   * If either the name or the editor is null, the method will not attempt to register a node.
   * Instead it just return <code>null</code>. This method also adds the registered node to
   * the list stored in the {@link StageInformation}, allowing for its removal in case of
   * errors. Throws a {@link PipelineException} if anything goes wrong with the registration
   * process.
   * 
   * @return The {@link NodeMod} that represents the newly created node.
   */
  protected NodeMod 
  registerSequence() 
    throws PipelineException
  {
    String nodeName = getNodeName();
    if(nodeName == null)
      return null;
    
    FrameRange fRange = getNodeFrameRange();
    NodeMod toReturn = 
      registerSequence(nodeName, getPadding(), getSuffix(), getEditor(), pIsIntermediate, 
	               fRange.getStart(), fRange.getEnd(), fRange.getBy());
    pNodeAdded = true;
    pStageInformation.addNode(nodeName, getAuthor(), getView());
    return toReturn;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return the current is intermediate setting of the stage.
   * <p>
   * This does not necessarily correspond to the actual setting on the node that was 
   * constructed with this stage, since it is possible to change this setting after 
   * {@link #build()} has been called.  This represents what setting will be used if the
   * {@link #build()} method is actually called.
   */
  public final boolean
  isIntermediate()
  {
    return pIsIntermediate;
  }
  
  /**
   * Set whether the stage should build an intermediate node or a normal node.
   */
  public final void
  setIntermediate
  (
    boolean isIntermediate  
  )
  {
    pIsIntermediate = isIntermediate;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Was the node conformed in this stage.
   */
  public boolean
  wasNodeConformed()
  {
   return pNodeConformed;
  }
  
  /**
   * Was the node added (registered) in this stage.
   */
  public boolean
  wasNodeAdded()
  {
   return pNodeAdded; 
  }
 
  /**
   * Was the node constructed in this stage.
   */
  public boolean
  wasNodeSkipped() 
  {
    return pNodeSkipped; 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2327486058471612742L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Flags for what actions where taken for the node which was processed by this stage.
   */ 
  private boolean pNodeAdded;
  private boolean pNodeConformed;
  private boolean pNodeSkipped; 
  
  private boolean pIsIntermediate;
}
