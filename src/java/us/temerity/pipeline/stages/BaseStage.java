// $Id: BaseStage.java,v 1.19 2007/10/30 20:40:55 jesse Exp $

package us.temerity.pipeline.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.math.Range;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   S T A G E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The class that provides the basis for all the stage builders in Pipeline
 * <P>
 * This class contains all the information and helper methods that will be used by stage
 * builders.
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
   *        The name of the stage.
   * @param desc
   *        A description of what the stage should do.
   * @param stageInformation
   * Contains information about stage execution that is global for all stages.
   * @param context
   *        The context the stage operates in.
   */
  protected 
  BaseStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client
  ) 
  {
    super(name, desc);
    pUtilContext = context;
    pStageInformation = stageInformation;
    pClient = client;
    pPlug = PluginMgrClient.getInstance();
    pAnnotations = new ListMap<String, BaseAnnotation>();
    
    pExecutionMethod = ExecutionMethod.Serial;
    pBatchSize = 0;
    pJobReqs = JobReqs.defaultJobReqs();
    
    pNodeCheckedOut = false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   A C C E S S                                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Attempts to release all the nodes that have been added so far.
   * <P>
   * This method is intended to be used to clean-up a builder that did not succesfully
   * complete. It uses the added node information to go through and attempt to release each
   * node. If an exception is encountered while releasing a node, it is caught and the method
   * continues to execute. Once the method has attempted to remove all the nodes in the added
   * nodes list, then a {@link PipelineException} will be thrown (if an error had occured
   * during execution) that contains the exception messages for all the exceptions that had
   * been thrown.
   * 
   * @throws PipelineException
   */
  public static void 
  cleanUpAddedNodes
  (
    MasterMgrClient mclient,
    StageState info
  ) 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder();
    boolean exception = false;
    TreeMap<String, String> users = info.getAddedNodesUserMap();
    TreeMap<String, String> views = info.getAddedNodesViewMap();
    for(String s : info.getAddedNodes()) {
      String user = users.get(s);
      String view = views.get(s);
      NodeID id = new NodeID(user, view, s);
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
   *        The instance of the Master Manager used to perform the lookup.
   * 
   * @param suffix
   *        The suffix to find the editor for.
   */
  public static PluginContext
  getDefaultEditor
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
   * 
   * @return A boolean representing whether the build process completed successfully.
   * @throws PipelineException
   */
  public abstract boolean 
  build() 
    throws PipelineException;
  
  /**
   * Method that every stage needs to override to perform its function.
   * 
   * @return A boolean representing whether the build process completed successfully.
   * @throws PipelineException
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
      pClient.link(getAuthor(), getView(), pRegisteredNodeName, link.getName(), link
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
      pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, source);
    }
  }

  /**
   * Takes the {@link BaseAction} stored in the pAction variable and adds it to the node
   * being constructed. For use in the {@link #build()} method.
   * 
   * @return <code>true</code> if the method completed correctly.
   * @throws PipelineException
   */
  protected final boolean 
  setAction() 
    throws PipelineException
  {
    pRegisteredNodeMod.setAction(pAction);
    return true;
  }
  
  /**
   *  Takes all the selection and license keys and applies them to the Job Requirements
   *  for the registered node.
   */
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
      pRegisteredNodeMod.setJobRequirements(reqs);
    }
  }
  
  /**
   * Takes all the Job Requirements (not counting the Selection and License keys) and applies
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
   * Adds all the annotations to the node.
   */
  protected final void
  doAnnotations()
    throws PipelineException
  {
    for (String name : pAnnotations.keySet()) {
      BaseAnnotation annot = pAnnotations.get(name);
      pClient.addAnnotation(pRegisteredNodeName, name, annot);
    }
  }

  /**
   * Removes all the annotations from the node.
   */
  protected final void
  removeAnnotations()
    throws PipelineException
  {
    pClient.removeAnnotations(pRegisteredNodeName);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   C O N S T R U C T I O N                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Removes the registered node's Action.
   * <p>
   * Takes the name of a node and removes any Action that the node
   * might have. Throws a {@link PipelineException} if there is a problem removing the
   * action.
   * 
   * @param name
   *        The full name of the node to have its Action removed.
   */
  public void 
  removeAction
  (
    String name
  ) 
    throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), name);
    NodeMod nodeMod = pClient.getWorkingVersion(nodeID);
    nodeMod.setAction(null);
    pClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }
  
  /**
   * Creates a new node in Pipeline.
   * <P>
   * Registers a node in Pipeline and returns a {@link NodeMod} representing the newly
   * created node. The node will be a single file without frame numbering. Use the
   * registerSequence method if you need a single file with a frame number. Throws a
   * {@link PipelineException} if it cannot register the node successfully.
   * 
   * @param name
   *        The full node name of the new node.
   * @param suffix
   *        The filename suffix for the new node.
   * @param editor
   *        The Editor for the new node.
   * @return The {@link NodeMod} representing the newly registered node.
   */
  public NodeMod 
  registerNode
  (
    String name, 
    String suffix, 
    BaseEditor editor
  )
    throws PipelineException
  {
    Path p = new Path(name);
    FileSeq fSeq = new FileSeq(p.getName(), suffix);
    NodeMod nodeMod = new NodeMod(name, fSeq, null, getToolset(), editor);
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
   *        The full node name of the new node.
   * @param pad
   *        The amount of padding for the frame numbering
   * @param suffix
   *        The filename extention for the new node.
   * @param editor
   *        The Editor for the new node.
   * @param startFrame
   *        The starting frame for the sequence.
   * @param endFrame
   *        The ending frame for the sequence.
   * @param step
   *        The step for the sequence.
   * @return The {@link NodeMod} representing the newly registered node.
   */
  public NodeMod 
  registerSequence
  (
    String name, 
    int pad, 
    String suffix, 
    BaseEditor editor,
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
    NodeMod nodeMod = new NodeMod(name, fSeq, null, getToolset(), editor);
    pClient.register(getAuthor(), getView(), nodeMod);
    return nodeMod;
  }
  
  /**
   * Creates a new node that matches the old node and then (optionally) copies the old node's
   * files to the new node.
   * <P>
   * This does not allow for any of the fine grained control that the GUI version of clone
   * node does. Throws a {@link PipelineException} if anything goes wrong.
   * 
   * @param oldName
   *        The node to be cloned.
   * @param newName
   *        The new node to be created.
   * @param cloneLinks
   *        Should the links of the old node be copied to the new node.
   * @param cloneAction
   *        Should the action of the old node be copied to the new node.
   * @return a {@link NodeMod} representing the newly created node.
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
      range = null;
      pat = new FilePattern(name, oldPat.getSuffix());
    }
    FileSeq newSeq = new FileSeq(pat, range);
    NodeMod newMod = new NodeMod(newName, newSeq, oldMod.getSecondarySequences(), 
      oldMod.getToolset(), oldMod.getEditor());
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
   * Returns a {@link BaseAction}.
   * <p>
   * Finds the latest version of a plugin from a specified vendor in the given toolset.
   * 
   * @param pluginUtil
   *        Contains the name and the vendor the the plugin 
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
   * Returns a {@link BaseEditor}.
   * <p>
   * Finds the latest version of a plugin from a specified vendor in the given toolset.
   * 
   * @param pluginUtil
   *        Contains the name and the vendor the the plugin
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
  
  /**
   * Getter for the name of the created node.
   * 
   * @return The created node's name.
   */
  public String 
  getNodeName()
  {
    return pRegisteredNodeName;
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
   */
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
   */
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
   */
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
   */
  public void 
  setLicenseKeys
  (
    TreeSet<String> licenseKeys
  )
  {
    pLicenseKeys = new TreeSet<String>(licenseKeys);
  }

  /**
   * Adds an annotation with the given name to the registered node.
   * <p>
   * The annotations will not actually be assigned until the
   * {@link #doAnnotations()} method is called.
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
   * Gets the list of all the annotations that are going to be added to the registered node.
   */
  public Map<String, BaseAnnotation>
  getAnnotations()
  {
    return Collections.unmodifiableMap(pAnnotations);
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
   * Note that all Selection and License key information will be ignored. To set these values
   * please use the other helper methods in BaseStage meant to deal with License and Selection
   * Keys.
   * 
   * @see #addSelectionKeys(TreeSet)
   * @see #setSelectionKeys(TreeSet)
   * @see #addLicenseKeys(TreeSet) 
   * @see #setLicenseKeys(TreeSet)
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
   * defined in the {@link BaseBuilder.StageFunction} enumeration which can be used as well. 
   */
  public String
  getStageFunction()
  {
    return StageFunction.aNone;
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
  private boolean 
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
   * Checks for the existence of node and takes action based on what the value of the 
   * actionOnExistence parameter.
   */
  protected final boolean
  checkExistance
  (
    String nodeName,
    ActionOnExistence actionOnExistence
  ) 
    throws PipelineException
  {
    LogMgr pLog = LogMgr.getInstance();
    if (nodeName == null)
      return false;
    pLog.log(Kind.Ops, Level.Finest, "Checking for existance of the node (" + nodeName + ")");
    boolean exists = nodeExists(nodeName);
    if (!exists) 
      return false;
    pLog.log(Kind.Ops, Level.Finest, "The node exists.");
    if (actionOnExistence == ActionOnExistence.Abort)
      throw new PipelineException
        ("The node (" + nodeName + ") exists.  Aborting Builder operation as per " +
         "the setting of the ActionOnExistence parameter in the builder.");
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(nodeName, false);
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    State state = treeComps.getState(nodeName);
    switch(state) {
    case WorkingOtherCheckedInNone:
      throw new PipelineException
        ("The node (" + nodeName + ") exists, but in a different working area and was " +
         "never checked in.  The Builder is aborting due to this problem.");
    case WorkingCurrentCheckedInNone:
      return true;
    case WorkingCurrentCheckedInSome:
      switch(actionOnExistence) {
      case CheckOut:
      case Conform:
	 pClient.checkOut(getAuthor(), getView(), nodeName, null, 
	   CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	 checkOut(CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	 pLog.log(Kind.Ops, Level.Finest, "Checking out the node.");
	return true;
      case Continue:
	return true;
      }
    case WorkingNoneCheckedInSome:
    case WorkingOtherCheckedInSome:
      switch(actionOnExistence) {
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
    pClient.checkOut(getAuthor(), getView(), pRegisteredNodeName, version, 
      mode, method);
    pStageInformation.addCheckedOutNode(pRegisteredNodeName);
    pNodeCheckedOut = true;
  }


  
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
  protected String pRegisteredNodeName = null;

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
   * The list of Selection Keys to assign to the built node.
   */
  protected TreeSet<String> pLicenseKeys;
  
  protected ListMap<String, BaseAnnotation> pAnnotations;
  
  protected ExecutionMethod pExecutionMethod;
  
  protected int pBatchSize;
  
  protected JobReqs pJobReqs;
  
  /**
   * Instance of {@link MasterMgrClient} to perform the stage's operations with.
   */
  protected MasterMgrClient pClient;
  
  protected PluginMgrClient pPlug;
  
  /**
   * Was the node Checked Out by the {@link #checkExistance(String, ActionOnExistence)} 
   * method.
   */
  private boolean pNodeCheckedOut;

}
