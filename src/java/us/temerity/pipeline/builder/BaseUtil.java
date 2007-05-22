package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;

/**
 * The abstract class that provides the basis for all utility classes in Pipeline.
 * <P>
 * This class contains all the information and helper methods that will be used by
 * utility classes.
 */
public 
class BaseUtil
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor that passes in all the information BaseUtil needs to initialize.
   * 
   * @param name
   *        The name of the utility.
   * @param desc
   *        A description of what the utility should do.
   * @param context
   *        The {@link UtilContext} that this utility is going to operate in.
   */
  protected 
  BaseUtil
  (
    String name,
    String desc,
    UtilContext context
  ) 
  {
    pName = name;
    pDesc = desc;
    pContext = context;
  }

  /**
   * Default constructor for BaseUtil. <P>
   * The {@link UtilContext} for this utility will need to be initialized using
   * {@link #setGlobalContext(UtilContext)} before any of the methods in this 
   * class are used. 
   * 
   * @param name
   *        The name of the utility.
   * @param desc
   *        A description of what the utility should do.
   * @param context
   *        The {@link UtilContext} that this utility is going to operate in.
   */
  protected 
  BaseUtil
  (
    String name,
    String desc
  ) 
  {
    pName = name;
    pDesc = desc;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L   C O N T E X T S                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the {@link UtilContext} that this utility will operate in.
   * <P>
   * This should only be used when the {@link #BaseUtil(String, String)} constructor is used.
   * This should not be used to change the context in the middle of running a tool.
   * 
   * @param context
   * 	The {@link UtilContext} for the utility. 
   */
  protected void 
  setGlobalContext
  (
    UtilContext context
  )
  {
    pContext = context;
  }
  
  /**
   * Returns a default {@link UtilContext}
   * 
   * @throws PipelineException
   */
  public static UtilContext 
  getDefaultUtilContext()
    throws PipelineException
  {
    String author = PackageInfo.sUser;
    TreeMap<String, TreeSet<String>> areas = sClient.getWorkingAreas();
    TreeSet<String> userAreas = areas.get(author);
    String view = null;
    if(userAreas.contains("default"))
      view = "default";
    else
      view = userAreas.first();
    String toolset = sClient.getDefaultToolsetName();
    return new UtilContext(author, view, toolset);
  }

  /**
   * Disables a node's Action. Takes the name of a node and disables the Action for that
   * node. Throws a {@link PipelineException} if there is a problem disabling the action.
   * 
   * @param name
   *        The full name of the node to have its Action disabled.
   * @throws PipelineException
   */
  public void 
  disableAction
  (
    String name
  ) 
  throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), name);
    NodeMod nodeMod = sClient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(false);
    sClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }

  /**
   * Enables a node's Action Takes the name of a node and enables the Action for that node.
   * Throws a {@link PipelineException} if there is a problem enabling the action.
   * 
   * @param name
   *        The full name of the node to have its Action enabled.
   * @throws PipelineException
   */
  public void 
  enableAction
  (
    String name
  ) 
  throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), name);
    NodeMod nodeMod = sClient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(true);
    sClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }

  /**
   * Removes a node's Action. Takes the name of a node and removes any Action that the node
   * might have. Throws a {@link PipelineException} if there is a problem removing the
   * action.
   * 
   * @param name
   *        The full name of the node to have its Action removed.
   * @throws PipelineException
   */
  public void 
  removeAction
  (
    String name
  ) 
  throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), name);
    NodeMod nodeMod = sClient.getWorkingVersion(nodeID);
    nodeMod.setAction(null);
    sClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }

  /**
   * Releases a group of nodes. Takes a {@link TreeSet} of node names and releases all the
   * nodes in the list. Throws a PipelineException if there is a problem releasing a node.
   * 
   * @param nodes
   *        A list of the full nodes names of everything to be released.
   * @throws PipelineException
   */
  public void 
  releaseNodes
  (
    TreeSet<String> nodes
  ) 
  throws PipelineException
  {
    for(String s : nodes) {
      sClient.release(getAuthor(), getView(), s, true);
    }
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
   * @throws PipelineException
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
    sClient.register(getAuthor(), getView(), nodeMod);
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
   * @throws PipelineException
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
    sClient.register(getAuthor(), getView(), nodeMod);
    return nodeMod;
  }

  /**
   * Creates a new node that matches the old node and then (optionally) copies the old node's files to 
   * the new node. 
   * <P>
   * This does not allow for any of the fine grained control that the GUI version of clone node does. 
   * Throws a {@link PipelineException} if anything goes wrong.
   * 
   * @param oldName
   *        The node to be cloned.
   * @param newName
   *        The new node to be created.
   * @param cloneLinks
   * 		Should the links of the old node be copied to the new node.
   * @param cloneAction
   *  		Should the action of the old node be copied to the new node.
   * @return a {@link NodeMod} representing the newly created node.
   * @throws PipelineException
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

    NodeMod oldMod = sClient.getWorkingVersion(getAuthor(), getView(), oldName);
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
    sClient.register(getAuthor(), getView(), newMod);
    if (cloneLinks)
    {
      for (LinkMod link : oldMod.getSources())
      {
	sClient.link(getAuthor(), getView(), newName, link.getName(), 
	  link.getPolicy(), link.getRelationship(), link.getFrameOffset());
      }
    }
    if (cloneAction)
    {
      BaseAction act = new BaseAction(oldMod.getAction());
      newMod.setAction(act);
      sClient.modifyProperties(getAuthor(), getView(), newMod);
    }
    if (copyFiles){
      NodeID source = new NodeID(getAuthor(), getView(), oldName);
      NodeID target = new NodeID(getAuthor(), getView(), newName);
      sClient.cloneFiles(source, target);
    }
    return newMod;
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
  public boolean 
  nodeExists
  (
    String name
  ) 
  throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(name, false);
    NodeTreeComp treeComps = sClient.updatePaths(getAuthor(), getView(), comps);
    State state = getState(treeComps, name);
    if ( state == null || state.equals(State.Branch) )
      return false;
    return true;
  }
  
  /**
   * Returns a enum which indicates where a node lives.
   * <p>
   * If a version of the node exists in the current working area, then
   * {@link NodeLocation#LOCAL} is returned. If the node has been checked in, but is not
   * checked out into the current working area, then {@link NodeLocation#REP} is returned. If
   * the name represents a directory, <code>null</code> is returned. Otherwise,
   * {@link NodeLocation#OTHER} is returned, indicating that the node exists in some other
   * working area, but was never checked in.
   * <p>
   * Note that this method assumes that the node actually exists. If existance is not assured,
   * then the {@link #nodeExists(String)} method should be called first.
   * 
   * @param name
   *        The node name.
   * @return The location of the node.
   */
  public NodeLocation 
  getNodeLocation
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(name, false);
    NodeTreeComp treeComps = sClient.updatePaths(getAuthor(), getView(), comps);
    Path p = new Path(name);
    ArrayList<String> parts = p.getComponents();
    for (String comp : parts)
    {
      treeComps = treeComps.get(comp);
    }
    NodeTreeComp.State state = treeComps.getState();
    NodeLocation toReturn = null;
    switch (state)
    {
      case Branch:
	toReturn = null;
	break;
      case WorkingCurrentCheckedInNone:
	toReturn = NodeLocation.LOCALONLY;
	break;
      case WorkingCurrentCheckedInSome:
	toReturn = NodeLocation.LOCAL;
	break;
      case WorkingNoneCheckedInSome:
      case WorkingOtherCheckedInSome:
	toReturn = NodeLocation.REP;
	break;
      case WorkingOtherCheckedInNone:
	toReturn = NodeLocation.OTHER;
	break;
      default:
	assert ( false );
    }
    return toReturn;
  }

  /**
   * Returns all the paths that are located directly underneath a given path.
   * 
   * @param start
   *        The path to start the search underneath
   * @return An {@link ArrayList} containing all the paths (both directories and nodes)
   *         located directly under the given path.
   * @throws PipelineException
   */
  public ArrayList<String> 
  findChildNodeNames
  (
    Path start
  ) 
  throws PipelineException
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start.toString(), false);
    NodeTreeComp treeComps = sClient.updatePaths(getAuthor(), getView(), comps);
    Path p = new Path(start);
    ArrayList<String> parts = p.getComponents();
    for(String comp : parts) {
      treeComps = treeComps.get(comp);
    }
    for(String s : treeComps.keySet()) {
      toReturn.add(s);
    }
    return toReturn;
  }

  /**
   * Returns all the directories that are located directly underneath a given path.
   * 
   * @param start
   *        The path to start the search underneath
   * @return An {@link ArrayList} containing the names of all the directories located
   *         directly under the given path.
   * @throws PipelineException
   */
  public ArrayList<String> 
  findChildBranchNames
  (
    Path start
  ) 
  throws PipelineException
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start.toString(), false);
    NodeTreeComp treeComps = sClient.updatePaths(getAuthor(), getView(), comps);
    Path p = new Path(start);
    ArrayList<String> parts = p.getComponents();
    for(String comp : parts) {
      if ( treeComps == null )
        break;
      treeComps = treeComps.get(comp);
    }
    if(treeComps != null) {
      for(String s : treeComps.keySet()) {
        NodeTreeComp comp = treeComps.get(s);
        if ( comp.getState() == NodeTreeComp.State.Branch )
          toReturn.add(s);
      }
    }
    return toReturn;
  }

  /**
   * Returns all the fully resolved node names that are located underneath a given path.
   * 
   * @param start
   *        The path to start the search underneath
   * @return An {@link ArrayList} containing all the node paths located under the given
   *         path.
   * @throws PipelineException
   */
  public ArrayList<String> 
  findAllChildNodeNames
  (
    String start
  ) 
  throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start, true);
    NodeTreeComp treeComps = sClient.updatePaths(getAuthor(), getView(), comps);
    ArrayList<String> toReturn = new ArrayList<String>();
    for(String s : treeComps.keySet()) {
      findNodes(treeComps.get(s), toReturn, "/");
    }
    return toReturn;
  }

  /**
   * Shortcut method for linking nodes and setting the correct namespace in the target
   * node's parameters. Links the target node to the source node using the given Link
   * Policy. If the action on the target node is a MayaReference, MayaBuild, or a MayaImport
   * it will also set the "Prefix Name" SourceParam on the action. In these cases, this
   * method changes the action that is passed in.
   * 
   * @param target
   *        The node that will be the target of the link.
   * @param source
   *        The node that will be the source of the link.
   * @param action
   *        The action associated with the target node. This will be modified.
   * @param policy
   *        What sort of link should be made.
   * @param nameSpace
   *        The namespace that will be used if the action is a MayaReference, MayaBuild, or
   *        MayaImport.
   * @throws PipelineException
   */
  public void 
  referenceNode
  (
    String target, 
    String source, 
    BaseAction action,
    LinkPolicy policy, 
    String nameSpace
  ) 
  throws PipelineException
  {
    boolean reference = false;

    String actionType = action.getName();
    if ( actionType.equals("MayaReference") || actionType.equals("MayaImport")
        || actionType.equals("MayaBuild") )
      reference = true;

    sClient
      .link(getAuthor(), getView(), target, source, policy, LinkRelationship.All, null);
    if(reference) {
      action.initSourceParams(source);
      action.setSourceParamValue(source, "PrefixName", nameSpace);
    }
  }

  /**
   * Shortcut for assigning preset values to an Action. Sets the Single Param Values of the
   * Action to the values in the SortedMap. Modifies the Action which is passed in.
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
   * Check-outs the latest version of a node if the working version is older than the latest
   * checked-in version. Checks the current version ID of a node against the newest
   * checked-in version. If the ID's are the same, it does nothing. If ID is older it checks
   * out the node, using the CheckOutMode and CheckOutMethod passed in. Throws a
   * {@link PipelineException} if no checked-in versions of the node exist.
   * 
   * @param name
   *        The name of the node node to checkout.
   * @param mode
   *        The {@link CheckOutMode} to use.
   * @param method
   *        The {@link CheckOutMethod} to use.
   * @throws PipelineException
   * @see #checkOutLatest(String, CheckOutMode, CheckOutMethod)
   */
  public void 
  checkOutNewer
  (
    String name, 
    CheckOutMode mode, 
    CheckOutMethod method
  )
    throws PipelineException
  {
    NodeMod mod = null;
    try {
      mod = sClient.getWorkingVersion(getAuthor(), getView(), name);
    }
    catch(PipelineException ex)
    {}

    TreeSet<VersionID> versions = null;
    try {
      versions = sClient.getCheckedInVersionIDs(name);
    }
    catch(PipelineException ex)
    {
      throw new PipelineException(
        "getNewest has aborted since there is no Checked-In Version of the node.\n "
            + ex.getMessage());
    }
    VersionID latestID = versions.last();

    if(mod != null) {
      VersionID currentID = mod.getWorkingID();

      if(currentID.compareTo(latestID) < 0) {
        sClient.checkOut(getAuthor(), getView(), name, latestID, mode, method);
      }
    }
    else {
      sClient.checkOut(getAuthor(), getView(), name, latestID, mode, method);
    }
  }

  /**
   * Check-outs the latest version of a node. Check-outs the latest version of the node
   * using the CheckOutMode and CheckOutMethod passed in. Throws a {@link PipelineException}
   * if the check-out fails.
   * 
   * @param name
   *        The name of the node node to checkout.
   * @param mode
   *        The {@link CheckOutMode} to use.
   * @param method
   *        The {@link CheckOutMethod} to use.
   * @throws PipelineException
   * @see #checkOutNewer(String, CheckOutMode, CheckOutMethod)
   */
  public void 
  checkOutLatest
  (
    String name, 
    CheckOutMode mode, 
    CheckOutMethod method
  )
    throws PipelineException
  {
    sClient.checkOut(getAuthor(), getView(), name, null, mode, method);
  }

  /**
   * Locks the latest version of the node. Throws a {@link PipelineException} if no
   * checked-in versions of the node exist.
   * 
   * @param name
   *        The name of the node node to checkout.
   * @throws PipelineException
   */
  public void 
  lockLatest
  (
    String name
  ) 
    throws PipelineException
  {
    sClient.lock(getAuthor(), getView(), name, null);
  }

  /**
   * Determines the latest checked-in version of a node and evolves the current working
   * version of the that node to the latest version. Throws a {@link PipelineException} if
   * no checked-in versions of the node exist.
   * 
   * @param name
   *        The name of the node to be evolved.
   * @throws PipelineException
   */
  public void 
  evolveNode
  (
    String name
  ) 
    throws PipelineException
  {
    TreeSet<VersionID> versions = null;
    try {
      versions = sClient.getCheckedInVersionIDs(name);
    }
    catch(PipelineException ex) {
      throw new PipelineException(
        "evolveNode has aborted since there is no Checked-In Version of the node.\n "
            + ex.getMessage());
    }
    VersionID latestID = versions.last();
    NodeID id = new NodeID(getAuthor(), getView(), name);
    sClient.evolve(id, latestID);
  }
  
  
  /**
   * Checks in a group of nodes with the specified message and the specified check-in Level.
   * 
   * @param nodes
   * 	The list of nodes to be checked in.
   * @param level
   * 	The Level of the check in.
   * @param message
   * 	The message for all the checked in nodes.
   * @throws PipelineException
   */
  public void 
  checkInNodes
  (
    TreeSet<String> nodes, 
    VersionID.Level level, 
    String message
  ) 
    throws PipelineException
  {
    for(String node : nodes) {
      sClient.checkIn(getAuthor(), getView(), node, message, level);
    }
  }

  /**
   * Returns <code>true</code> if the entire tree given has an {@link OverallQueueState}
   * of Finished. Starts at the node specified in the node status passed in and decends down
   * the tree, checking the {@link OverallQueueState} of each node. If every node it finds
   * is in the Finished state, the method will return <code>true</code>. If any are found
   * in a state which is not Finished, then <code>false</code> sis returned.
   * 
   * @param status
   *        The status of the root node of the tree to be searched.
   */
  public boolean 
  getTreeState
  (
    NodeStatus status
  )
  {
    OverallQueueState state = status.getDetails().getOverallQueueState();
    switch(state){
    case Finished: {
      Collection<NodeStatus> stati = status.getSources();
      if(stati != null) {
	for(NodeStatus stat : stati) {
	  boolean temp = getTreeState(stat);
	  if(!temp)
	    return temp;
	}
	return true;
      }
      return true;
    }
      default:
        return false;
    }
  }

  /**
   * Returns a {@link BaseAction}.
   * <p>
   * Finds the latest version of a plugin from a specified vendor in the given toolset.
   * 
   * @param pluginUtil
   *        Contains the name and the vendor the the plugin *
   * @param toolset
   *        The toolset from which the version of the Action will be extracted.
   * @throws PipelineException
   */
  public static BaseAction 
  getAction
  (
    PluginContext pluginUtil, 
    String toolset
  )
    throws PipelineException
  {
    if(sPlug == null)
      sPlug = PluginMgrClient.getInstance();
    
    DoubleMap<String, String, TreeSet<VersionID>> plugs = sClient
      .getToolsetActionPlugins(toolset);
    
    TreeSet<VersionID> pluginSet = 
      plugs.get(pluginUtil.getPluginVendor(), pluginUtil.getPluginName());
    if (pluginSet == null)
      throw new PipelineException
        ("No Action Exists that matches the Plugin Context (" + pluginUtil + ") " +
         "in toolset (" + toolset + ")");
    VersionID ver = pluginSet.last();

    return sPlug.newAction(pluginUtil.getPluginName(), ver, pluginUtil.getPluginVendor());
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
   * @throws PipelineException
   */
  public static BaseEditor 
  getEditor
  (
    PluginContext pluginUtil, 
    String toolset
  )
    throws PipelineException
  {
    if(sPlug == null)
      sPlug = PluginMgrClient.getInstance();
    
    DoubleMap<String, String, TreeSet<VersionID>> plugs = sClient
      .getToolsetEditorPlugins(toolset);
    VersionID ver = plugs.get(pluginUtil.getPluginVendor(), pluginUtil.getPluginName())
      .last();

    return sPlug.newEditor(pluginUtil.getPluginName(), ver, pluginUtil.getPluginVendor());
  }
  
  public static TreeMap<String, TreeSet<String>>
  getWorkingAreas()
    throws PipelineException
  {
    return sClient.getWorkingAreas();
  }
  
  public static ArrayList<String> 
  getViews
  (
    String user
  ) 
    throws PipelineException
  {
    return new ArrayList<String>(sClient.getWorkingAreas().get(user));
  }
  
  public static ArrayList<String> 
  getUsers() 
    throws PipelineException
  {
    return new ArrayList<String>(sClient.getWorkingAreas().keySet());
  }
  
  public static ArrayList<String>
  getActiveToolsets()
    throws PipelineException
  {
    return new ArrayList<String>(sClient.getActiveToolsetNames());
  }
  
  public static String
  getDefaultToolset()
    throws PipelineException
  {
    return sClient.getDefaultToolsetName();
  }
  
  public static ArrayList<SelectionKey>
  getSelectionKeys()
    throws PipelineException
  {
    return sQueue.getSelectionKeys();
  }
  
  public static ArrayList<LicenseKey>
  getLicenseKeys()
    throws PipelineException
  {
    return sQueue.getLicenseKeys();
  }
  
  /**
   * @return the name
   */
  public String getName()
  {
    return pName;
  }

  /**
   * @return the desc
   */
  public String getDesc()
  {
    return pDesc;
  }
  
  
  // Utility methods for this class.

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
   * Recursive function to search for nodes under a given path. A recursive function that
   * starts with the current {@link NodeTreeComp}, travels down the tree, and adds any
   * nodes it finds to an {@link ArrayList} being passed as a parameter. It is important to
   * note when using this method that the {@link ArrayList} is being modified inside the
   * method. The {@link ArrayList} will contain the fully resolved node names for all the
   * nodes.
   * 
   * @param treeComps
   *        A {@link NodeTreeComp} that should contain information about the node name
   *        specified by scene. The most common way to acquire this data structure is with
   *        the <code>updatePaths</code> method in {@link MasterMgrClient}.
   * @param toReturn
   *        An {@link ArrayList} that will hold every node that is found by this method.
   * @param path
   *        The full path that leads up to the current {@link NodeTreeComp}. This is needed
   *        to build the full node name being stored in the ArrayList.
   */
  private void 
  findNodes
  (
    NodeTreeComp treeComps, 
    ArrayList<String> toReturn, 
    String path
  )
  {
    State state = treeComps.getState();
    if(state.equals(State.Branch))
      for(String s : treeComps.keySet())
	findNodes(treeComps.get(s), toReturn, path + treeComps.getName() + "/");
    else
      toReturn.add(path + treeComps.getName());
  }

  /**
   * Shortcut method to get the author value of the stage's {@link UtilContext}.
   * 
   * @return The Author.
   */
  protected String 
  getAuthor()
  {
    return pContext.getAuthor();
  }

  /**
   * Shortcut method to get the view value of the stage's {@link UtilContext}.
   * 
   * @return The View.
   */
  protected String 
  getView()
  {
    return pContext.getView();
  }

  /**
   * Shortcut method to get the toolset value of the stage's {@link UtilContext}.
   * 
   * @return The Toolset.
   */
  protected String 
  getToolset()
  {
    return pContext.getToolset();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   S H U T D O W N                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  public static void
  disconnectClients()
  {
    sClient.disconnect();
    sQueue.disconnect();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The static instance of the Master Manager that is being used to perform all the
   * Pipeline operations in this stage.
   */
  protected static final MasterMgrClient sClient = new MasterMgrClient();
  
  /**
   * The static instance of the Queue Manager that is being used to perform all the
   * Pipeline operations in this stage.
   */
  protected static final QueueMgrClient sQueue = new QueueMgrClient();

  /**
   * Static instance of the Plugin Manager which is used to perform all plugin related stage
   * operations.
   */
  protected static PluginMgrClient sPlug = PluginMgrClient.getInstance();
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The context which holds the user, view, and toolset that the stage is operating on.
   */
  protected UtilContext pContext;
  
  /**
   * The name of the utility.
   */
  private String pName;
  
  /**
   * The description of the utility.
   */
  private String pDesc;
  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   E N U M S                                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Has one of three values representing where a node lives.
   * <p>
   * LOCAL means the node exists in the current working area.
   * REP means the node exists in the repository, but not the current working area.
   * OTHER means the node only exists in the another working area.
   *
   */
  public static enum NodeLocation
  {
    /**
     * The node exists in the current working area.
     */
    LOCAL,
    /**
     * The node exists in the current working area, but does not exist in the respoistory. 
     */
    LOCALONLY,
    /**
     * The node exists in the repository, but not the current working area.
     */
    REP,
    /**
     * The node only exists in the another working area.
     */
    OTHER;
  }
}