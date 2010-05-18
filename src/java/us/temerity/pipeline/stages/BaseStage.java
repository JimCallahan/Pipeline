package us.temerity.pipeline.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.math.Range;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   S T A G E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class for all stages used in Builders and other plugins.
 * <p>
 * A stage is a class that makes or edits a node.  The most common use of stages is in 
 * Builders, which create and invoke stages during the buildPhase of their Construct Passes.
 * All stages follow the same basic form.  Information about the node is set, usually 
 * entirely by the constructor.  Then, the {@link #build()} method is caused, which causes all
 * the information that has been set to be turned into an actuality as the node is created.
 * <p>
 * It is completely possible for Builders to do much of the same information setting that
 * stages do in their constructors, however that is usually not a preferable way to work.  As
 * long as stages remain a black box to the Builder that is instantiating them, it becomes easy
 * to shader functionality between many different Builders.  A need to change how a certain
 * type of node functions is as simple as changing the stage that is being used to make the
 * node regardless of how many Builders are using it.  However, if information about what the
 * stage is building is being controlled at the Builder level and not in the stage, then 
 * making changes to functionality necessitates making changes to similar code in multiple 
 * files.
 * <p>
 * Along the same lines, it is considered better form to extend an existing stage to add
 * functionality to it, rather than placing that extended functionality into the class that is
 * invoking the stage.  By making the changes at the stage level, a single point of entry for
 * editing is retained and a new stage has been created which now has the potential to be 
 * reused. 
 * <p>
 * BaseStage does not contain any actual functionality in terms of actually creating a node.
 * It does contain all the information that will be needed to create a node as well as helper
 * methods which can put together that information, but there are no implementations of
 * {@link #build()} and {@link #conform()}.  A generic implementation of these methods is 
 * provided in the {@link StandardStage} class, which should be adequate in almost all cases.
 * In cases where it is not, it is possible to subclass BaseStage directly.
 */
public abstract 
class BaseStage
  extends Described
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor that passes in all the information BaseState needs to initialize.
   * 
   * @param name
   *  The name of the stage.
   *  
   * @param desc
   *  A description of what the stage should do.
   *  
   * @param stageInformation
   *  Contains information about stage execution that is global for all stages.
   *  
   * @param context
   *   The context the stage operates in.
   *   
   * @param client
   *   The instance of Master Manager that the stage performs all its actions in.
   *   
   * @param nodeName
   *   The name of the node that this stage is going to construct.
   *   
   * @param stageFunction
   *   A string which describes what sort of node the stage is building.  This is currently
   *   being used to decide which editor to assign to nodes.  This can be set to 
   *   <code>null</code> if a stage does not want to provide a value.
   */
  protected 
  BaseStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String stageFunction
  ) 
  {
    super(name, desc);
    pUtilContext = context;
    pStageInformation = stageInformation;
    pClient = client;
    pPlug = PluginMgrClient.getInstance();
    pNodeName = nodeName;
    pNodeID = new NodeID(context.getAuthor(), context.getView(), nodeName);
    
    pAnnotations = new ListMap<String, BaseAnnotation>();
    pVersionAnnotations = new ListMap<String, BaseAnnotation>();
    
    String logname = pStageInformation.getLoggerName();
    if (logname == null)
      pLog = LogMgr.getInstance();
    else
      pLog = LogMgr.getInstance(logname);
    
    pExecutionMethod = ExecutionMethod.Serial;
    pBatchSize = 0;
    pJobReqs = JobReqs.defaultJobReqs();
   
    if (stageFunction != null)
      pStageFunction = stageFunction;
    
    pNodeCheckedOut = false;
    pNodeLocked = false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   A C C E S S                                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Attempts to release all the nodes that have been added so far.
   * <P>
   * This method is intended to be used to clean-up a builder that did not successfully
   * complete. It uses the added node information to go through and attempt to release each
   * node. If an exception is encountered while releasing a node, it is caught and the method
   * continues to execute. Once the method has attempted to remove all the nodes in the added
   * nodes list, then a {@link PipelineException} will be thrown (if an error had occurred
   * during execution) that contains the exception messages for all the exceptions that had
   * been thrown.
   * 
   * @param mclient
   *   The instance of the Master Manager to use to release the nodes.
   * 
   * @param info
   *   The Stage Information class that contains the information about which nodes have
   *   been added.
   * 
   * @throws PipelineException
   *   After all nodes have been released, if any of the release operations failed.
   */
  public static void 
  cleanUpAddedNodes
  (
    MasterMgrClient mclient,
    StageInformation info
  ) 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder();
    boolean exception = false;
    TreeMap<String, NodeID> addedNodes = info.getAddedNodes(); 
    for(String s : addedNodes.keySet()) {
      NodeID id = addedNodes.get(s);
      try {
	mclient.release(id, true);
      }
      catch(Exception ex) {
	exception = true;
	buf.append(ex.getMessage() + "\n");
      }
    }
    if(exception)
      throw new PipelineException(buf.toString());
  }
  
  /**
   * Returns the default editor for the given suffix.
   * 
   * @param client
   *   The instance of the Master Manager used to perform the lookup.
   * 
   * @param suffix
   *   The suffix to find the editor for.
   *        
   * @return
   *   The Plugin Context that is associated with a given suffix.
   */
  public static PluginContext
  getDefaultSuffixEditor
  (
    MasterMgrClient client,
    String suffix
  )
    throws PipelineException
  {
    TreeSet<SuffixEditor> suffixes =  client.getSuffixEditors();
    for (SuffixEditor editor : suffixes) {
      if (editor.getSuffix().equals(suffix)) {
	BaseEditor edit = editor.getEditor();
	return new PluginContext(edit.getName(), edit.getVendor());
      }
    }
    return null;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   O P S                                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Method that every stage needs to override to perform its function.
   * <p>
   * The build() method is what outside programs call in order to get a stage to make 
   * the node it has been setup to make.  There is a default implementation of this 
   * functionality provided in the {@link StandardStage} class which should be adequate
   * for almost all situations.  An example of how this might be extended is present in the
   * {@link FileWriterStage}.
   * 
   * @return 
   *   A boolean representing whether the build process completed successfully.
   * 
   * @throws PipelineException
   *   If node creation fails for any reason.
   */
  public abstract boolean 
  build() 
    throws PipelineException;
  
  /**
   * The method that is called when a stage needs to edit an existing node rather than 
   * construct a new one.
   * <p>
   * This method should be amenable to being called directly.  However, in the
   * {@link StandardStage} implementation, it is actually most commonly called from the
   * build() method, depending on the value of the Action On Existence parameter.  If that
   * parameter value is set to Conform and the node exists, the build() method will call the
   * conform() method itself.  This behavior is not required and does not have to be
   * implemented in this fashion in classes that extend {@link BaseStage} directly.  This
   * implementation makes it easy for Builders to use stages without having to worry about node
   * existence.
   * 
   * @return 
   *   A boolean representing whether the conform process completed successfully.
   *   
   * @throws PipelineException
   *   If node editing fails for any reason
   */
  public abstract boolean 
  conform() 
    throws PipelineException;
 


  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L   H E L P E R S                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Takes all the {@link FileSeq} stored in the pSecondarySequences variable and adds
   * them to the node being constructed. For use in the {@link #build()} method.
   * 
   * @return <code>true</code> if the method completed correctly.
   * @throws PipelineException
   */
  protected final boolean 
  addSecondarySequences() 
    throws PipelineException
  {
    for(FileSeq seq : pSecondarySequences) {
      pRegisteredNodeMod.addSecondarySequence(seq);
    }
    return true;
  }
  
  /**
   * Removes all of the secondary sequences from the registered node.
   * <p>
   * This method does not apply the changes. It is necessary to call modifyProperties on the
   * pRegisteredNodeMod variable to make this permanent.
   */
  protected final void
  removeSecondarySequences()
  {
    pRegisteredNodeMod.removeAllSecondarySequences();
  }

  /**
   * Takes all the {@link LinkMod} stored in the pLinks variable and turns them into
   * actual Pipeline links. For use in the {@link #build()} method.
   * 
   * @return <code>true</code> if the method completed correctly.
   * @throws PipelineException
   */
  protected final boolean 
  createLinks() 
    throws PipelineException
  {
    for(LinkMod link : pLinks) {
      pClient.link(getAuthor(), getView(), pNodeName, link.getName(), link
	.getPolicy(), link.getRelationship(), link.getFrameOffset());
    }
    return true;
  }
  
  /**
   * Removes all the links from the registered node.
   */
  protected final void
  removeLinks()
    throws PipelineException
  {
    for (String source : pRegisteredNodeMod.getSourceNames()) {
      pClient.unlink(getAuthor(), getView(), pNodeName, source);
    }
  }

  /**
   * Takes the {@link BaseAction} stored in the pAction variable and adds it to the node
   * being constructed. For use in the {@link #build()} method.
   * 
   * @throws PipelineException
   */
  protected final void 
  setAction() 
    throws PipelineException
  {
    pRegisteredNodeMod.setAction(pAction);
    if(pAction != null) 
      pRegisteredNodeMod.setActionEnabled(true);
  }
  
  /**
   *  Takes all the selection, hardware, and license keys and applies them to the 
   *  Job Requirements of the registered node.
   *  
   *  @deprecated
   *    Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  protected final void
  setKeys() 
    throws PipelineException
  {
    JobReqs reqs = pRegisteredNodeMod.getJobRequirements();
    if (reqs != null) {
      if (pSelectionKeys != null) {
	reqs.addSelectionKeys(pSelectionKeys);
      }
      if (pStageInformation.useDefaultSelectionKeys()) {
	reqs.addSelectionKeys(pStageInformation.getDefaultSelectionKeys());
      }
      Set<String> stageSKeys = pStageInformation.getStageFunctionSelectionKeys(getStageFunction());
      if (stageSKeys != null)
	reqs.addSelectionKeys(stageSKeys);
      if (pLicenseKeys != null) {
	reqs.addLicenseKeys(pLicenseKeys);
      }
      if (pStageInformation.useDefaultLicenseKeys()) {
	reqs.addLicenseKeys(pStageInformation.getDefaultLicenseKeys());
      }
      Set<String> stageLKeys = pStageInformation.getStageFunctionLicenseKeys(getStageFunction());
      if (stageSKeys != null)
	reqs.addLicenseKeys(stageLKeys);
      if (pHardwareKeys != null) {
        reqs.addHardwareKeys(pHardwareKeys);
      }
      if (pStageInformation.useDefaultHardwareKeys()) {
        reqs.addHardwareKeys(pStageInformation.getDefaultHardwareKeys());
      }
      Set<String> stageHKeys = pStageInformation.getStageFunctionHardwareKeys(getStageFunction());
      if (stageHKeys != null)
        reqs.addHardwareKeys(stageHKeys);
      pRegisteredNodeMod.setJobRequirements(reqs);
    }
  }
  
  /**
   * Takes all the Job Requirements (not counting the keys) and applies
   * them to the registered node.
   */
  protected void
  setJobSettings()
    throws PipelineException
  {
    pRegisteredNodeMod.setExecutionMethod(pExecutionMethod);
    if (pExecutionMethod == ExecutionMethod.Parallel)
      pRegisteredNodeMod.setBatchSize(pBatchSize);
    JobReqs reqs = pRegisteredNodeMod.getJobRequirements();
    reqs.setMaxLoad(pJobReqs.getMaxLoad());
    reqs.setMinDisk(pJobReqs.getMinDisk());
    reqs.setMinMemory(pJobReqs.getMinMemory());
    reqs.setPriority(pJobReqs.getPriority());
    reqs.setRampUp(pJobReqs.getRampUp());
    pRegisteredNodeMod.setJobRequirements(reqs);
  }
  
  /**
   * Add all the per-node annotations to the node.
   */
  protected final void
  doAnnotations()
    throws PipelineException
  {
    for (String name : pAnnotations.keySet()) {
      BaseAnnotation annot = pAnnotations.get(name);
      pClient.addAnnotation(pNodeName, name, annot);
    }
  }
  
  /**
   * Add all the per-version annotations to the node.
   * 
   * @param mod
   *   The working version to add the annotations to.
   * 
   * @return
   *   The working version with all the annotations added.
   *   
   * @throws PipelineException
   */
  protected final NodeMod
  doVersionAnnotations
  (
    NodeMod mod  
  )
    throws PipelineException
  {
    for (String name : pVersionAnnotations.keySet()) {
      BaseAnnotation annot = pVersionAnnotations.get(name);
      mod.addAnnotation(name, annot);
    }
    pClient.modifyProperties(getAuthor(), getView(), mod);
    return pClient.getWorkingVersion(getAuthor(), getView(), pNodeName);
  }

  /**
   * Removes all the annotations from the node.
   */
  protected final void
  removeAnnotations()
    throws PipelineException
  {
    pClient.removeAnnotations(pNodeName);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   C O N S T R U C T I O N                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Removes the registered node's Action.
   * <p>
   * Takes the name of a node and removes any Action that the node might have.  This method
   * calls modifyProperties() to immediately apply its changes. 
   * 
   * @throws PipelineException 
   *   If there is a problem removing the action.
   */
  public void 
  removeAction() 
    throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), pNodeName);
    NodeMod nodeMod = pClient.getWorkingVersion(nodeID);
    nodeMod.setAction(null);
    pClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }
  
  /**
   * Disables the registered node's Action.
   * <p>
   * Takes the name of a node and disables any Action that the node might have.  This method
   * calls modifyProperties() to immediately apply its changes. 
   * 
   * @throws PipelineException
   *   If there is a problem disabling the node's action.
   */
  public void 
  disableAction
  () 
    throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), pNodeName);
    NodeMod nodeMod = pClient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(false);
    pClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }

  /**
   * Vouches for the registered node.
   * 
   * @throws PipelineException
   *   If there is a problem vouching for the node.
   */
  public void 
  vouch
  () 
    throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), pNodeName);
    pClient.vouch(nodeID);
  }
  
  /**
   * Creates a new node in Pipeline.
   * <P>
   * Registers a node in Pipeline and returns a {@link NodeMod} representing the newly
   * created node. The node will be a single file without frame numbering. Use the
   * registerSequence method if you need a single file with a frame number. 
   * 
   * @param name
   *  The full node name of the new node.
   * 
   * @param suffix
   *   The filename suffix for the new node.
   * 
   * @param editor
   *  The Editor for the new node.
   *  
   * @param isIntermediate
   *   Is the node an intermediate node.
   * 
   * @return 
   *   The {@link NodeMod} representing the newly registered node.
   *   
   * @throws PipelineException
   *   If it cannot register the node successfully.
   */
  public NodeMod 
  registerNode
  (
    String name, 
    String suffix, 
    BaseEditor editor,
    boolean isIntermediate
  )
    throws PipelineException
  {
    Path p = new Path(name);
    FileSeq fSeq = new FileSeq(p.getName(), suffix);
    NodeMod nodeMod = new NodeMod(name, fSeq, null, isIntermediate, getToolset(), editor);  
    pClient.register(getAuthor(), getView(), nodeMod);
    return nodeMod;
  }
  
  /**
   * Create a new node representing a sequence in Pipeline. Registers a node in Pipeline and
   * returns a {@link NodeMod} representing the newly created node. The node will be a file
   * sequence with frame numbers, starting at <code>startFrame</code>, ending at
   * <code>endFrame</code>, with a step of <code>step</code>. If you want to register
   * a node without frame numbers, use the registerNode method.
   * 
   * @param name
   *   The full node name of the new node.
   * 
   * @param pad
   *   The amount of padding for the frame numbering
   * 
   * @param suffix
   *   The filename extension for the new node.
   * 
   * @param editor
   *   The Editor for the new node.
   *   
   * @param isIntermediate
   *   Is the node an intermediate node.
   * 
   * @param startFrame
   *   The starting frame for the sequence.
   * 
   * @param endFrame
   *   The ending frame for the sequence.
   * 
   * @param step
   *   The step for the sequence.
   * 
   * @return 
   *   The {@link NodeMod} representing the newly registered node.
   * 
   * @throws PipelineException
   *   If it cannot register the node successfully.
   */
  public NodeMod 
  registerSequence
  (
    String name, 
    int pad, 
    String suffix, 
    BaseEditor editor,
    boolean isIntermediate,
    int startFrame, 
    int endFrame, 
    int step
  ) 
    throws PipelineException
  {
    Path p = new Path(name);
    FilePattern pat = new FilePattern(p.getName(), pad, suffix);
    FrameRange range = new FrameRange(startFrame, endFrame, step);
    FileSeq fSeq = new FileSeq(pat, range);
    NodeMod nodeMod = new NodeMod(name, fSeq, null, isIntermediate, getToolset(), editor);
    pClient.register(getAuthor(), getView(), nodeMod);
    return nodeMod;
  }
  
  /**
   * Creates a new node that matches the old node and then (optionally) copies the old node's
   * files to the new node.
   * <P>
   * This does not allow for any of the fine grained control that the GUI version of clone
   * node does.
   * 
   * @param oldName
   *   The node to be cloned.
   * 
   * @param newName
   *   The new node to be created.
   * 
   * @param cloneLinks
   *   Should the links of the old node be copied to the new node.
   * 
   * @param cloneAction
   *   Should the action of the old node be copied to the new node.
   *   
   * @param copyFiles
   *   Should the files of the old node be copied to the new node.
   * 
   * @return 
   *   The {@link NodeMod} representing the newly created node.
   */
  public NodeMod 
  cloneNode
  (
    String oldName, 
    String newName,
    boolean cloneLinks,
    boolean cloneAction, 
    boolean copyFiles
  ) 
    throws PipelineException
  {
    Path p = new Path(newName);
    String name = p.getName();

    NodeMod oldMod = pClient.getWorkingVersion(getAuthor(), getView(), oldName);
    FileSeq oldSeq = oldMod.getPrimarySequence();
    FilePattern oldPat = oldSeq.getFilePattern();

    FrameRange range = null;
    FilePattern pat = null;

    if(oldSeq.hasFrameNumbers()) {
      range = oldSeq.getFrameRange();
      pat = new FilePattern(name, oldPat.getPadding(), oldPat.getSuffix());
    }
    else {
      pat = new FilePattern(name, oldPat.getSuffix());
    }
    FileSeq newSeq = new FileSeq(pat, range);
    NodeMod newMod = new NodeMod(newName, newSeq, oldMod.getSecondarySequences(), 
                                 oldMod.isIntermediate(), oldMod.getToolset(), 
                                 oldMod.getEditor());
    pClient.register(getAuthor(), getView(), newMod);
    if (cloneLinks)
    {
      for (LinkMod link : oldMod.getSources())
      {
	pClient.link(getAuthor(), getView(), newName, link.getName(), 
	  link.getPolicy(), link.getRelationship(), link.getFrameOffset());
      }
    }
    if (cloneAction)
    {
      BaseAction act = new BaseAction(oldMod.getAction());
      newMod.setAction(act);
      pClient.modifyProperties(getAuthor(), getView(), newMod);
    }
    if (copyFiles){
      NodeID source = new NodeID(getAuthor(), getView(), oldName);
      NodeID target = new NodeID(getAuthor(), getView(), newName);
      pClient.cloneFiles(source, target);
    }
    return newMod;
  }

  /**
   * Shortcut for assigning preset values to an Action.
   * <P>
   * Sets the Single Param Values of the Action to the values in the SortedMap. Modifies the
   * Action which is passed in.
   * 
   * @param act
   *        The Action to set the presets on.
   * @param preset
   *        The {@link SortedMap} that holds the preset names and values.
   */
  @SuppressWarnings("unchecked")
  public void 
  setPresets
  (
    BaseAction act, 
    SortedMap<String, Comparable> preset
  )
  {
    for(String name : preset.keySet()) {
      act.setSingleParamValue(name, preset.get(name));
    }
  }
  
  /**
   * Finds a version of an Action plugin from a specified vendor in the given toolset.
   * <p>
   * 
   * @param pluginUtil
   *        Contains the name, vendor, and version number of the plugin 
   * 
   * @param toolset
   *        The toolset from which the version of the Action will be extracted.
   */
  public BaseAction 
  getAction
  (
    PluginContext pluginUtil, 
    String toolset
  )
    throws PipelineException
  {
    DoubleMap<String, String, TreeSet<VersionID>> plugs = pClient
      .getToolsetActionPlugins(toolset);
    
    if (plugs == null)
      throw new PipelineException
        ("There are no plugins associated with the toolset (" + toolset + ")");
    
    TreeSet<VersionID> temp = plugs.get(pluginUtil.getPluginVendor(), pluginUtil.getPluginName());
    if (temp == null)
      throw new PipelineException
        ("No Action Exists that matches the Plugin Context (" + pluginUtil + ") " +
         "in toolset (" + toolset + ")");

    TreeSet<VersionID> pluginSet = new TreeSet<VersionID>(Collections.reverseOrder());
    pluginSet.addAll(temp);
    

    Range<VersionID> contextRange = pluginUtil.getRange();
    
    VersionID goodVersion = null;
    
    for (VersionID each : pluginSet) {
      if (contextRange.isInside(each)) {
	goodVersion = each;
	break;
      }
    }
    
    if (goodVersion == null)
      throw new PipelineException
        ("No Action Exists that matches the Plugin Context (" + pluginUtil + ") " +
         "in toolset (" + toolset + ")");

    return pPlug.newAction(pluginUtil.getPluginName(), goodVersion, pluginUtil.getPluginVendor());
  }

  /**
   * Finds a version of an Editor plugin from a specified vendor in the given toolset.

   * @param pluginUtil
   *        Contains the name, vendor and version number of the plugin
   * 
   * @param toolset
   *        The toolset from which the version of the Editor will be extracted.
   */
  public BaseEditor 
  getEditor
  (
    PluginContext pluginUtil, 
    String toolset
  )
    throws PipelineException
  {
    DoubleMap<String, String, TreeSet<VersionID>> plugs = pClient
      .getToolsetEditorPlugins(toolset);
    
    if (plugs == null)
      throw new PipelineException
        ("There are no Editor plugins associated with the toolset (" + toolset + ")");
    
    TreeSet<VersionID> temp = plugs.get(pluginUtil.getPluginVendor(), pluginUtil.getPluginName());
    if (temp == null)
      return null;

    TreeSet<VersionID> pluginSet = new TreeSet<VersionID>(Collections.reverseOrder());
    pluginSet.addAll(temp);
    
    Range<VersionID> contextRange = pluginUtil.getRange();
    
    VersionID goodVersion = null;
    
    for (VersionID each : pluginSet) {
      if (contextRange.isInside(each)) {
	goodVersion = each;
	break;
      }
    }
    
    if (goodVersion == null)
      throw new PipelineException
        ("No Action Exists that matches the Plugin Context (" + pluginUtil + ") " +
         "in toolset (" + toolset + ")");
    
    return pPlug.newEditor(pluginUtil.getPluginName(), goodVersion, pluginUtil.getPluginVendor());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  public String
  getNodeName()
  {
    return pNodeName;
  }

  /**
   * Getter for the {@link NodeMod} of the created node. This will be <code>null</code>
   * until the <code>build</code> method has been run.
   * 
   * @return The created {@link NodeMod} or <code>null</code> if it has not been created
   *         yet or if creation failed.
   */
  public NodeMod 
  getNodeMod()
  {
    return pRegisteredNodeMod;
  }
  
  /**
   * Get the {@link NodeID} of the node being registered by this stage.
   */
  public NodeID
  getNodeID()
  {
    return pNodeID;
  }
  
  /**
   * Shortcut method to get the author value of the stage's {@link UtilContext}.
   * 
   * @return The Author.
   */
  protected String 
  getAuthor()
  {
    return pUtilContext.getAuthor();
  }

  /**
   * Shortcut method to get the view value of the stage's {@link UtilContext}.
   * 
   * @return The View.
   */
  protected String 
  getView()
  {
    return pUtilContext.getView();
  }

  /**
   * Shortcut method to get the toolset value of the stage's {@link UtilContext}.
   * 
   * @return The Toolset.
   */
  protected String 
  getToolset()
  {
    return pUtilContext.getToolset();
  }
  
  /**
   * Was the node Checked Out by the {@link #checkExistance(String, ActionOnExistence)} 
   * method.
   */
  public boolean
  wasNodeCheckedOut()
  {
   return pNodeCheckedOut; 
  }

  /**
   * Was the node Locked by the {@link #checkExistance(String, ActionOnExistence)} method.
   */
  public boolean
  wasNodeLocked()
  {
   return pNodeLocked; 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  N O D E    C O N S T U C T I O N                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the pEditor variable.
   * <p>
   * This is the {@link BaseEditor} which the {@link #build()} method should assign to the
   * created node.
   * 
   */
  public void 
  setEditor
  (
    BaseEditor ed
  )
  {
    pEditor = ed;
  }

  /**
   * Sets the pAction variable.
   * <p>
   * This is the {@link BaseAction} which the {@link #build()} method should assign to the
   * created node.
   * 
   */
  public void 
  setAction
  (
    BaseAction act
  )
  {
    pAction = act;
  }

  /**
   * Adds a link to the stage.
   * <p>
   * If pLinks is null, this method will initialize it. Note that this method does not
   * actually create the link in Pipeline, merely adds the link information to the stage.
   * It is the responsibility of the stage, in its {@link #build()} method to actually
   * create the link.
   * 
   * @param link
   *            The link to add.
   */
  public void 
  addLink
  (
    LinkMod link
  )
  {
    if(pLinks == null)
      pLinks = new LinkedList<LinkMod>();
    pLinks.add(link);
  }

  /**
   * Adds a Single Parameter to the node's Action..
   * <p>
   * If pAction has not been initialized before calling this method, it will throw a
   * {@link PipelineException}.
   * 
   * @param name
   *            The name of the single parameter to add.
   * @param value
   *            The value the named single parameter should have.
   */
  @SuppressWarnings("unchecked")
  public void 
  addSingleParamValue
  (
    String name, 
    Comparable value
  ) 
    throws PipelineException
  {
    if(pAction != null)
      pAction.setSingleParamValue(name, value);
    else
      throw new PipelineException("The Action must be initialized before "
	+ "attempting to set a Single Parameter.");
  }

  /**
   * Adds a Source Parameter to the node's Action..
   * <p>
   * If pAction has not been initialized before calling this method, it will throw a
   * {@link PipelineException}.
   * 
   * @param source
   *            The name of the source to add the parameter to.
   * @param name
   *            The name of the source parameter to add.
   * @param value
   *            The value the named source parameter should have.
   */
  @SuppressWarnings("unchecked")
  public void 
  addSourceParamValue
  (
    String source, 
    String name, 
    Comparable value
  )
  throws PipelineException
  {
    if(pAction != null) {
      if(!pAction.hasSourceParams(source))
	pAction.initSourceParams(source);
      pAction.setSourceParamValue(source, name, value);
    }
    else
      throw new PipelineException("The Action must be initialized before "
	+ "attempting to set a Source Parameter.");
  }

  /**
   * Adds a Secondary Source Parameter to the node's Action..
   * <p>
   * If pAction has not been initialized before calling this method, it will throw a
   * {@link PipelineException}.
   * 
   * @param source
   *            The name of the source that has the secondary source.
   * @param fpat
   *            The FilePattern that represents the secondary source to add the parameter
   *            to.
   * @param name
   *            The name of the secondary source parameter to add.
   * @param value
   *            The value the named secondary source parameter should have.
   */
  @SuppressWarnings("unchecked")
  public void 
  addSecondarySourceParamValue
  (
    String source, 
    FilePattern fpat, 
    String name,
    Comparable value
  ) 
    throws PipelineException
  {
    if(pAction != null) {
      if(!pAction.hasSecondarySourceParams(source, fpat))
	pAction.initSecondarySourceParams(source, fpat);
      pAction.setSecondarySourceParamValue(source, fpat, name, value);
    }
    else
      throw new PipelineException("The Action must be initialized before "
	+ "attempting to set a Secondary Source Parameter.");
  }

  /**
   * Adds a secondary sequence to the stage.
   * <p>
   * If pSecondarySequences is null, this method will initialize it. Note that this method
   * does not actually create the secondary sequence in Pipeline, merely adds the
   * information to the stage. It is the responsibility of the stage, in its
   * {@link #build()} method to actually add the secondary sequence.
   * 
   * @param seq
   */
  public void 
  addSecondarySequence
  (
    FileSeq seq
  )
  {
    if(pSecondarySequences == null)
      pSecondarySequences = new LinkedList<FileSeq>();
    pSecondarySequences.add(seq);
  }

  /**
   * Adds the set of Selection Keys to the Selection Keys that will be assigned to the
   * registered node.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  public void 
  addSelectionKeys
  (
    TreeSet<String> selectionKeys
  )
  {
    if (pSelectionKeys == null)
      pSelectionKeys = new TreeSet<String>();
    pSelectionKeys.addAll(selectionKeys);
  }

  /**
   * Replaces the existing of Selection Keys with the new set.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  public void 
  setSelectionKeys
  (
    TreeSet<String> selectionKeys
  )
  {
    pSelectionKeys = new TreeSet<String>(selectionKeys);
  }

  /**
   * Adds the set of License Keys to the License Keys that will be assigned to the
   * registered node.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  public void 
  addLicenseKeys
  (
    TreeSet<String> licenseKeys
  )
  {
    if (pLicenseKeys == null)
      pLicenseKeys = new TreeSet<String>();
    pLicenseKeys.addAll(licenseKeys);
  }

  /**
   * Replaces the existing of License Keys with the new set.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  public void 
  setLicenseKeys
  (
    TreeSet<String> licenseKeys
  )
  {
    pLicenseKeys = new TreeSet<String>(licenseKeys);
  }
  
  /**
   * Adds the set of Hardware Keys to the Hardware Keys that will be assigned to the
   * registered node.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  public void 
  addHardwareKeys
  (
    TreeSet<String> hardwareKeys
  )
  {
    if (pHardwareKeys == null)
      pHardwareKeys = new TreeSet<String>();
    pHardwareKeys.addAll(hardwareKeys);
  }

  /**
   * Replaces the existing of Hardware Keys with the new set.
   * 
   * @deprecated
   *   Due to the addition of key choosers, it is not recommended to hard code keys on nodes.
   */
  @Deprecated
  public void 
  setHardwareKeys
  (
    TreeSet<String> hardwareKeys
  )
  {
    pHardwareKeys = new TreeSet<String>(hardwareKeys);
  }

  /**
   * Add a per-node annotation with the given name to the registered node.
   * <p>
   * The annotations will not actually be assigned until the {@link #doAnnotations()} method
   * is called.
   */
  public void
  addAnnotation
  (
    String name,
    BaseAnnotation annotation  
  )
  {
    pAnnotations.put(name, annotation);
  }

  /**
   * Add a per-version annotation with the given name to the registered node.
   * <p>
   * The annotations will not actually be assigned until the
   * {@link #doVersionAnnotations(NodeMod)} method is called.
   */
  public void
  addVersionAnnotation
  (
    String name,
    BaseAnnotation annotation  
  )
  {
    pVersionAnnotations.put(name, annotation);
  }
  
  /**
   * Get the list of all the per-node annotations that are going to be added 
   * to the registered node.
   */
  public Map<String, BaseAnnotation>
  getAnnotations()
  {
    return Collections.unmodifiableMap(pAnnotations);
  }
  
  /**
   * Get the list of all the per-version annotations that are going to be added 
   * to the registered node.
   */
  public Map<String, BaseAnnotation>
  getVersionAnnotations()
  {
    return Collections.unmodifiableMap(pVersionAnnotations);
  }
  
  /**
   * Adds an {@link ExecutionMethod} that will be assigned to the registered node.
   */
  public void
  setExecutionMethod
  (
    ExecutionMethod method  
  )
  {
    if (method != null)
      pExecutionMethod = method;
  }

  /**
   * Adds a batch size that will be assigned to the registered node, assuming it has the
   * Parallel ExecutionMethod.
   */
  public void
  setBatchSize
  (
    int batchSize  
  )
  {
    pBatchSize = batchSize;
  }
  
  /**
   * Sets any special job requirements, excluding keys for this job.
   * <p>
   * Note that all Selection, License, and Hardware key information will be ignored. To set 
   * these values please use the other helper methods in BaseStage meant to deal with 
   * License, Hardware, and Selection Keys.  
   * 
   * @see #addSelectionKeys(TreeSet)
   * @see #setSelectionKeys(TreeSet)
   * @see #addLicenseKeys(TreeSet) 
   * @see #setLicenseKeys(TreeSet)
   * @see #addHardwareKeys(TreeSet)
   * @see #setHardwareKeys(TreeSet)
   */
  public void
  setJobReqs
  (
    JobReqs reqs
  )
  {
    pJobReqs = reqs;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   F U N C T I O N                                                             */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns a String that identifies the function of this stage in a builder.
   * <p>
   * This is used to select an appropriate editor for the node.  Stages should override
   * this method to set a value that makes sense.  There are a number of standard values
   * defined in {@link StageFunction} which can be used as well. 
   */
  public String
  getStageFunction()
  {
    return pStageFunction;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N F O R M A T I O N                                                                 */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does the Action that is going to be assigned to the registered node have a
   * SingleParameter with the given name?
   */
  public boolean
  doesActionHaveParam
  (
    String paramName
  )
  {
    if (pAction == null)
     return false;
    for (ActionParam param : pAction.getSingleParams() ) {
      String name = param.getName();
      if (name.equals(paramName))
	return true;
    }
    return false;
  }
  
  /**
   * Returns the {@link State} of a node. Takes a {@link NodeTreeComp} and a node name. It
   * traces it way down the tree until it finds the place specified by the node name and
   * returns the State of that place. It will return <code>null</code> if the specified
   * path does not exist in the tree defined by the {@link NodeTreeComp}.
   * 
   * @param treeComps
   *        A {@link NodeTreeComp} that should contain information about the node name
   *        specified by scene. The most common way to acquire this data structure is with
   *        the <code>updatePaths</code> method in {@link MasterMgrClient}.
   * @param scene
   *        The name of the path to search for the {@link State}.
   * @return The {@link State} of the given name or null if the name is not valid in the
   *         given {@link NodeTreeComp}.
   */
  private State 
  getState
  (
    NodeTreeComp treeComps, 
    String scene
  )
  {
    State toReturn = null;
    Path p = new Path(scene);
    NodeTreeComp dest = null;
    for(String s : p.getComponents()) {
      if(dest == null)
	dest = treeComps.get(s);
      else
	dest = dest.get(s);

      if(dest == null)
	break;
    }
    if(dest != null)
      toReturn = dest.getState();
    return toReturn;
  }
  
  /**
   * Returns a boolean that indicates if the name is an existing node in Pipeline.
   * 
   * @param name
   *        The name of the node to search for.
   * @return <code>true</code> if the node exists. <code>false</code> if the node does
   *         not exist or if the specified path is a Branch.
   * @throws PipelineException
   */
  protected boolean 
  nodeExists
  (
    String name
  ) 
  throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(name, false);
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    State state = getState(treeComps, name);
    if ( state == null || state.equals(State.Branch) )
      return false;
    return true;
  }
  
  /**
   * Returns a boolean that indicates if the node is checked out into the current working 
   * area.
   * 
   * @param name
   *        The name of the node to search for.
   *        
   * @return <code>true</code> if the node exists in the current working area. 
   *         <code>false</code> if the node does not exist or if it is not checked-out.
   */
  public boolean 
  workingVersionExists
  (
    String name
  ) 
    throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(name, false);
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    State state = treeComps.getState(name);
    if ( state == State.WorkingCurrentCheckedInNone || 
         state == State.WorkingCurrentCheckedInSome )
      return true;
    return false;
  }
  
  /**
   * Get the working version of a node in the current working area. 
   * 
   * @param nodeName
   *   The name of the node.
   *   
   * @return
   *   The working version of the node
   *   
   * @throws PipelineException
   *   If there is no working version of the node.
   */
  public NodeMod
  getWorkingVersion
  (
    String nodeName  
  )
    throws PipelineException
  {
    return pClient.getWorkingVersion(getAuthor(), getView(), nodeName);
  }
  
  /**
   * Checks for the existence of node and takes action based on what the value of the 
   * actionOnExistence parameter.
   */
  @SuppressWarnings({ "incomplete-switch", "fallthrough" })
  protected final boolean
  checkExistance
  (
    String nodeName,
    ActionOnExistence actionOnExistence
  ) 
    throws PipelineException
  {
    if (nodeName == null)
      return false;
    pLog.log(Kind.Ops, Level.Finest, "Checking for existence of the node (" + nodeName + ")");
  
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(nodeName, false);
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    State state = treeComps.getState(nodeName);
    
    if ( state == null || state.equals(State.Branch) )
      return false;
    pLog.log(Kind.Ops, Level.Finest, "The node exists.");
   
    if (actionOnExistence == ActionOnExistence.Abort) {
      String moreMessage = "";
      if (state == State.WorkingOtherCheckedInNone) {
        TreeMap<String, TreeSet<String>> views = pClient.getWorkingAreasContaining(nodeName);
        moreMessage = 
          "The node has never been checked in and only exists in the " + views + 
          " working area.";
      }
      throw new PipelineException
        ("The node (" + nodeName + ") exists.  Aborting Builder operation as per " +
         "the setting of the ActionOnExistence parameter in the builder.\n" + moreMessage );
    }
    
    
    switch(state) {
    case WorkingOtherCheckedInNone: 
      {
        TreeMap<String, TreeSet<String>> views = pClient.getWorkingAreasContaining(nodeName);
        throw new PipelineException
          ("The node (" + nodeName + ") exists, but only in the working area " + views + 
           " and was never checked in.  The Builder is aborting due to this problem.");
      }
    case WorkingCurrentCheckedInNone:
      switch (actionOnExistence) {
      case Lock:
        throw new PipelineException
          ("The node (" + nodeName + ") does not have any checked-in versions, but the " +
           "Action On Existence was set to Lock.  The Builder is aborting due to " +
           "this problem.");
      }
      return true;
    case WorkingCurrentCheckedInSome:
      switch(actionOnExistence) {
      case Lock:
        pLog.log(Kind.Ops, Level.Finest, "Locking the node.");
        lock();
        return true;
      case CheckOut:
	checkOut(CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	pLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
	return true;
      case Conform:
        NodeID id = new NodeID(getAuthor(), getView(), pNodeName);
        NodeStatus status = pClient.status(id, true, DownstreamMode.None);
        NodeDetailsLight details = status.getLightDetails();
        VersionID baseID = details.getBaseVersion().getVersionID();
        VersionID latestID = details.getLatestVersion().getVersionID();
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), pNodeName);
        if (baseID.equals(latestID) && !mod.isFrozen()) {
          pLog.log(Kind.Bld, Level.Finest, 
            "Conform is not checking out the node, since it is already based on the " +
            "latest version.");
        }
        else {
          checkOut(CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
          pLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
        }
        return true;
      case Continue:
	return true;
      }
    case WorkingNoneCheckedInSome:
    case WorkingOtherCheckedInSome:
      switch(actionOnExistence) {
      case Lock:
        pLog.log(Kind.Ops, Level.Finest, "Locking the node.");
        lock();
        return true;
      case CheckOut:
      case Conform:
	checkOut(CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	pLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
	return true;
      case Continue:
	throw new PipelineException
          ("The node (" + nodeName + ") exists, but is not checked out in the current " +
           "working area.  Since ActionOnExistence was set to Continue, " +
           "the Builder is unable to procede.");
      }
    }
    return false;
  }
  
  /**
   * Checks out the latest version of the node associated with this stage, adding it to the
   * list of nodes that has been checked out.
   * <p>
   * This is the preferred method for stages and builders to cause a node to be checked out,
   * since it keeps track of what has been checked out.
   * 
   * @param mode
   *   The CheckoutMode for the checkout
   * 
   * @param method
   *   The CheckoutMethod for the checkout
   */
  public final void
  checkOut
  (
    CheckOutMode mode,
    CheckOutMethod method
  )
    throws PipelineException
  {
    checkOut(null, mode, method);
  }
  
  /**
   * Checks out the given version of the node associated with this stage, adding it to the
   * list of nodes that has been checked out.
   * <p>
   * This is the preferred method for stages and builders to cause a node to be checked out,
   * since it keeps track of what has been checked out.
   * 
   * @param version
   *   The version of the node to check out.
   * 
   * @param mode
   *   The CheckoutMode for the check out
   * 
   * @param method
   *   The CheckoutMethod for the check out
   *   
   * 
   */
  public final void
  checkOut
  (
    VersionID version,
    CheckOutMode mode,
    CheckOutMethod method
  )
    throws PipelineException
  {
    pClient.checkOut(getAuthor(), getView(), pNodeName, version, 
      mode, method);
    pStageInformation.addCheckedOutNode(pNodeName);
    pNodeCheckedOut = true;
  }
  
  public final void
  lock()
    throws PipelineException
  {
    VersionID latest = pClient.getCheckedInVersionIDs(pNodeName).last();
    pClient.lock(getAuthor(), getView(), pNodeName, latest);
    pStageInformation.addLockedNode(pNodeName);
    pNodeLocked = true;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1921312494746168530L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The UtilContext that the stage operates in.
   */
  protected UtilContext pUtilContext;
  
  /**
   * Contains shared information between all stages.
   */
  protected StageInformation pStageInformation;
  
  /**
   * The name of the node that is to be registered by the stage.
   */
  private String pNodeName;
  
  /**
   * The nodeID for the node that is being registered by the stage.
   */
  private NodeID pNodeID;
  
  /**
   * The suffix of the node that is going to be registered.
   */
  protected String pSuffix = null;
  
  protected FrameRange pFrameRange;
  
  protected int pPadding;

  /**
   * The Editor for the node that is going to be registered
   */
  protected BaseEditor pEditor = null;

  /**
   * The Action for the node that is going to be registered.
   * <p>
   * This also stores all the parameter (single, source, and secondary source) information.
   */
  protected BaseAction pAction = null;

  /**
   * A list of links for the registered node to have.
   */
  protected LinkedList<LinkMod> pLinks = null;

  /**
   * A list of secondary sequences for the registered node to have.
   */
  protected LinkedList<FileSeq> pSecondarySequences = null;

  /**
   * The working version of the registered node, once it has been built. The
   * {@link #build()} method needs to ensure that this field correct once it finishes
   * execution.
   */
  protected NodeMod pRegisteredNodeMod = null;
  
  /**
   * The list of Selection Keys to assign to the built node.
   */
  protected TreeSet<String> pSelectionKeys;
  
  /**
   * The list of License Keys to assign to the built node.
   */
  protected TreeSet<String> pLicenseKeys;
  
  /**
   * The list of Hardware Keys to assign to the built node.
   */
  protected TreeSet<String> pHardwareKeys;
  
  protected ListMap<String, BaseAnnotation> pAnnotations;
  
  protected ListMap<String, BaseAnnotation> pVersionAnnotations;
  
  protected ExecutionMethod pExecutionMethod;
  
  protected int pBatchSize;
  
  protected JobReqs pJobReqs;
  
  /**
   * Instance of {@link MasterMgrClient} to perform the stage's operations with.
   */
  protected MasterMgrClient pClient;
  
  protected PluginMgrClient pPlug;
  
  protected LogMgr pLog;
  
  /**
   * Was the node Checked Out by the {@link #checkExistance(String, ActionOnExistence)} 
   * method?
   */
  private boolean pNodeCheckedOut;
  
  /**
   * Was the node locked by the {@link #checkExistance(String, ActionOnExistence)} method?
   */
  private boolean pNodeLocked;

  private String pStageFunction = StageFunction.aNone;
}
