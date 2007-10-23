package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

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
   * @param suffix
   *        The suffix for the created node.
   * @param editor
   *        Contains the name and vendor for the Editor plugin. If this is <code>null</code>
   *        then the Maya Editor from Temerity will be used. *
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
    String suffix,
    PluginContext editor, 
    PluginContext action
  ) 
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client);
    pRegisteredNodeName = nodeName;
    pSuffix = suffix;
    if(editor != null) {
      pEditor = getEditor(editor, getToolset());
    }
    else {
      PluginContext defaultEditor = stageInformation.getDefaultEditor(getStageFunction());
      if (defaultEditor != null)
	pEditor = getEditor(defaultEditor, getToolset());
    }

    if(action != null) {
      pAction = getAction(action, getToolset());
    }
    pFrameRange = null;
    pPadding = -1;
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
   * 
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
    super(name, desc, stageInformation, context, client);
    pRegisteredNodeName = nodeName;
    pSuffix = suffix;
    if(editor != null) {
      pEditor = getEditor(editor, getToolset());
    }
    else {
      PluginContext defaultEditor = stageInformation.getDefaultEditor(getStageFunction());
      if (defaultEditor != null)
	pEditor = getEditor(defaultEditor, getToolset());
    }
    
    if(action != null) {
      pAction = getAction(action, getToolset());
    }
    if (range == null)
      throw new PipelineException("You must specify a frame range in a FrameNumStage");
    pFrameRange = range;
    if (padding == null)
      pPadding = 4;
    else if (padding < 0)
      throw new PipelineException("Cannot have a negative padding value");
    else
      pPadding = padding;
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
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client);

    pRegisteredNodeName = mod.getName();

    {
      FileSeq primary = mod.getPrimarySequence();
      FilePattern fpat = primary.getFilePattern();
      pSuffix = fpat.getSuffix();
      pFrameRange = primary.getFrameRange();
      pPadding = fpat.getPadding();
      
      pSecondarySequences.addAll(mod.getSecondarySequences());
    }

    pEditor = mod.getEditor();
    pAction = mod.getAction();

    pExecutionMethod = mod.getExecutionMethod();
    if(pExecutionMethod == ExecutionMethod.Parallel)
      pBatchSize = mod.getBatchSize();

    pLinks.addAll(mod.getSources());
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
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
    LogMgr.getInstance().log
      (Kind.Ops, Level.Fine, "Building the node: " + pRegisteredNodeName );
    ActionOnExistence actionOnExistence = pStageInformation.getActionOnExistence(); 
    if (!checkExistance(pRegisteredNodeName, actionOnExistence))
      return construct();
    else if (actionOnExistence == ActionOnExistence.Conform)
      return conform();
    else 
      pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(),
	      pRegisteredNodeName);
    return false;
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
  public boolean
  construct()
    throws PipelineException
  {
    if (pFrameRange == null)
	pRegisteredNodeMod = registerNode();
    else
	pRegisteredNodeMod = registerSequence();
    if(pRegisteredNodeMod == null)
	return false;
    if(pSecondarySequences != null)
	addSecondarySequences();
    if(pLinks != null)
	createLinks();
    if(pAction != null)
	setAction();
    setKeys();
    if(pAction != null)
	setJobSettings();
    pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
    pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(),
	pRegisteredNodeName);
    if (pStageInformation.doAnnotations())
	doAnnotations();
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
  @Override
  public boolean 
  conform()
    throws PipelineException
  {
    LogMgr.getInstance().log
      (Kind.Ops, Level.Finer, "Conforming the node: " + pRegisteredNodeName );
    NodeID id = new NodeID(getAuthor(), getView(), pRegisteredNodeName);
    NodeStatus status = pClient.status(id, true);
    NodeDetails details = status.getDetails();
    NodeMod oldMod = details.getWorkingVersion(); 
    boolean hasFrameNumbers = oldMod.getPrimarySequence().hasFrameNumbers();
    if (hasFrameNumbers && pFrameRange == null)
      throw new PipelineException
        ("Attempting to conform the node (" + pRegisteredNodeName + ") from one with " +
         "frame numbers to one without frame numbers");
    if (!hasFrameNumbers && pFrameRange != null)
      throw new PipelineException
      ("Attempting to conform the node (" + pRegisteredNodeName + ") from one without " +
       "frame numbers to one with frame numbers");
    NodeVersion latest = details.getLatestVersion(); 
    if ( latest != null) {
      String oldSuffix = latest.getPrimarySequence().getFilePattern().getSuffix();
      String newSuffix = latest.getPrimarySequence().getFilePattern().getSuffix();
      if (!oldSuffix.equals(newSuffix))
	throw new PipelineException
	  ("Attempting to conform the node (" + pRegisteredNodeName + ") from having " +
	   "the suffix (" + oldSuffix + ") to having a suffix (" + newSuffix + ").  " +
	   "Since this node has been checked-in, this is an illegal modification.");
    }
    if (pFrameRange != null)
      pClient.renumber(id, pFrameRange, true);
    pRegisteredNodeMod = pClient.getWorkingVersion(id);
    pRegisteredNodeMod.setEditor(pEditor);
    removeSecondarySequences();
    if(pSecondarySequences != null)
      addSecondarySequences();
    removeLinks();
    if(pLinks != null)
      createLinks();
    if(pAction != null)
      setAction();
    pRegisteredNodeMod.setJobRequirements(new JobReqs());
    setKeys();
    if(pAction != null)
      setJobSettings();
    pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
    pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);
    removeAnnotations();
    if (pStageInformation.doAnnotations())
      doAnnotations();
    return true;
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
  registerNode() 
    throws PipelineException
  {
    if(pRegisteredNodeName == null)
      return null;
    NodeMod toReturn = registerNode(pRegisteredNodeName, pSuffix, pEditor);
    pStageInformation.addNode(pRegisteredNodeName, getAuthor(), getView());
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
    if(pRegisteredNodeName == null)
      return null;
    NodeMod toReturn = 
      registerSequence(pRegisteredNodeName, pPadding, pSuffix, pEditor, 
	               pFrameRange.getStart(), pFrameRange.getEnd(), pFrameRange.getBy()); 
    pStageInformation.addNode(pRegisteredNodeName, getAuthor(), getView());
    return toReturn;
  }
}
