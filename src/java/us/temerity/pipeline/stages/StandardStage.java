package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

/**
 * A branch stage designed to make building stages without frame numbers easy.
 */
public abstract
class StandardStage 
  extends BaseStage
{
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
    pPadding = 0;
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
   *        then the Maya Editor from Temerity will be used. *
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
   * Constructs the node from all the given pieces.
   * <p>
   * This method does the following things.
   * <ul>
   * <li> Registers the node.
   * <li> Adds the secondary sequences.
   * <li> Creates all the links.
   * <li> Sets the Action.
   * <li> Performs a {@link MasterMgrClient#modifyProperties(String, String, NodeMod)} to
   * commit all the updates.
   * <li> Sets the pRegisteredNodeMod to the newly modified working version.
   * <li> If doAnnotations is set in the stage information, then it adds all the stored
   * annotations in the stage to the node.
   * 
   * @return <code>true</code> if the node was registered successfully. <code>false</code>
   *         if something happened and the node was not registered.
   * @see BaseStage#build()
   */
  @Override
  public boolean 
  build() 
    throws PipelineException
  {
    LogMgr.getInstance().log
      (Kind.Ops, Level.Fine, "Building the node: " + pRegisteredNodeName );
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
  
  public void
  doAnnotations()
    throws PipelineException
  {
    for (String name : pAnnotations.keySet()) {
      BaseAnnotation annot = pAnnotations.get(name);
      pClient.addAnnotation(pRegisteredNodeName, name, annot);
    }
  }
  

  /**
   * Registers a node with the given name and extention, using the given Editor.
   * <P>
   * If either the name or the editor is null, the method will not attempt to register a
   * node. Instead it just return <code>null</code>. This method also adds the
   * registered node to the list in BaseStage, allowing for its removal in case of errors.
   * Throws a {@link PipelineException} if anything goes wrong with the registration
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
   * Registers a node with the given name and extention, using the given Editor.
   * <P>
   * If either the name or the editor is null, the method will not attempt to register a
   * node. Instead it just return <code>null</code>. This method also adds the
   * registered node to the list in BaseStage, allowing for its removal in case of errors.
   * Throws a {@link PipelineException} if anything goes wrong with the registration
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
