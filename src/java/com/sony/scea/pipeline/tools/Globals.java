package com.sony.scea.pipeline.tools;

import java.io.File;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;

/**
 * A class of static methods that can be used in standalone tools. 
 * <p> 
 * Most of this relies upon the {@link Wrapper} class, to pass around important
 * information.
 * @author Jesse Clemens
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
/**
 * @author jesse
 *
 */
public class Globals
{
/**
 * Clones a node.
 * @param w
 * 	wrapper
 * @param newName
 * 	The name of the node to be created
 * @param oldName
 * 	The node that it is going to be based on
 * @return
 * 	The {@link NodeMod} that represents the newly created node.
 * @throws PipelineException
 */
  public static NodeMod cloneNode(Wrapper w, String newName, String oldName)
    throws PipelineException
  {
    Path p = new Path(newName);
    String name = p.getName();

    NodeMod oldMod = w.mclient.getWorkingVersion(w.user, w.view, oldName);
    FileSeq oldSeq = oldMod.getPrimarySequence();
    FilePattern oldPat = oldSeq.getFilePattern();

    FrameRange range = null;
    FilePattern pat = null;

    if ( oldSeq.hasFrameNumbers() )
    {
      range = oldSeq.getFrameRange();
      pat = new FilePattern(name, oldPat.getPadding(), oldPat.getSuffix());
    } else
    {
      range = null;
      pat = new FilePattern(name, oldPat.getSuffix());
    }
    FileSeq newSeq = new FileSeq(pat, range);
    NodeMod newMod = new NodeMod(newName, newSeq, oldMod.getSecondarySequences(), oldMod
      .getToolset(), oldMod.getEditor());
    w.mclient.register(w.user, w.view, newMod);
    NodeID source = new NodeID(w.user, w.view, oldName);
    NodeID target = new NodeID(w.user, w.view, newName);
    w.mclient.cloneFiles(source, target);
    return newMod;
  }

  /**
   * Takes a {@link NodeStatus} and recurses through it to determine the dominate
   * queue state. <p>
   * It is primarily useful in determining if an entire tree of nodes is in the 
   * Finished state.  The only way that this function will return Finished is if
   * all the nodes included in the status are in the finished state.
   * @param status
   * 	The status to be evaluated.
   * @return The derived queue state.
   */
  public static OverallQueueState getTreeState(NodeStatus status)
  {
    OverallQueueState state = status.getDetails().getOverallQueueState();
    System.err.println(status.getName() + "\t" + state);
    switch (state)
    {
      case Undefined:
	return OverallQueueState.Undefined;
      case Aborted:
	return OverallQueueState.Aborted;
      case Failed:
	return OverallQueueState.Failed;
      case Running:
	return OverallQueueState.Running;
      case Paused:
	return OverallQueueState.Paused;
      case Queued:
	return OverallQueueState.Queued;
      case Stale:
	return OverallQueueState.Stale;
      case Finished:
      {
	TreeSet<OverallQueueState> set = new TreeSet<OverallQueueState>();
	Collection<NodeStatus> stati = status.getSources();
	if ( stati != null )
	{
	  for (NodeStatus stat : stati)
	  {
	    set.add(getTreeState(stat));
	  }
	  return findReturnState(set);
	}
	return OverallQueueState.Finished;
      }
    }
    return null;
  }

  private static OverallQueueState findReturnState(TreeSet<OverallQueueState> set)
  {
    if ( set.contains(OverallQueueState.Undefined) )
      return OverallQueueState.Undefined;
    if ( set.contains(OverallQueueState.Aborted) )
      return OverallQueueState.Aborted;
    if ( set.contains(OverallQueueState.Failed) )
      return OverallQueueState.Failed;
    if ( set.contains(OverallQueueState.Running) )
      return OverallQueueState.Running;
    if ( set.contains(OverallQueueState.Paused) )
      return OverallQueueState.Paused;
    if ( set.contains(OverallQueueState.Queued) )
      return OverallQueueState.Queued;
    if ( set.contains(OverallQueueState.Stale) )
      return OverallQueueState.Stale;
    return OverallQueueState.Finished;
  }

  /**
   * Disables the action for a node
   * @param w wrapper
   * @param name The name of the node
   * @throws PipelineException
   */
  public static void disableAction(Wrapper w, String name) throws PipelineException
  {
    NodeID nodeID = new NodeID(w.user, w.view, name);
    NodeMod nodeMod = w.mclient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(false);
    w.mclient.modifyProperties(w.user, w.view, nodeMod);
  }

  /**
   * Enables the action for the given node name.
   * @param w wrapper
   * @param name The name of the node
   * @throws PipelineException
   */
  public static void enableAction(Wrapper w, String name) throws PipelineException
  {
    NodeID nodeID = new NodeID(w.user, w.view, name);
    NodeMod nodeMod = w.mclient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(true);
    w.mclient.modifyProperties(w.user, w.view, nodeMod);
  }

  /**
   * Removes the action for the given node name.
   * @param w wrapper
   * @param name The name of the node
   * @throws PipelineException
   */
  public static void removeAction(Wrapper w, String name) throws PipelineException
  {
    NodeID nodeID = new NodeID(w.user, w.view, name);
    NodeMod nodeMod = w.mclient.getWorkingVersion(nodeID);
    nodeMod.setAction(null);
    w.mclient.modifyProperties(w.user, w.view, nodeMod);
  }
  
  /**
   * Releases an entire list of nodes
   * @param w wrapper
   * @param nodes The list of nodes to release
   * @throws PipelineException
   */
  public static void releaseNodes(Wrapper w, TreeSet<String> nodes)
    throws PipelineException
  {
    for (String s : nodes)
    {
      w.mclient.release(w.user, w.view, s, true);
    }
  }

  /**
   * Registers a node with frame numbers
   * @param w wrapper
   * @param name node name
   * @param pad frame padding
   * @param extention file extention 
   * @param editor the Editor for the node
   * @param startF starting frame
   * @param endf ending frame
   * @param byF step
   * @return the {@link NodeMod} representing the new node
   * @throws PipelineException
   */
  public static NodeMod registerSequence(Wrapper w, String name, int pad, String extention,
      BaseEditor editor, int startF, int endf, int byF) throws PipelineException
  {
    Path p = new Path(name);
    FilePattern pat = new FilePattern(p.getName(), pad, extention);
    FrameRange range = new FrameRange(startF, endf, byF);
    FileSeq animSeq = new FileSeq(pat, range);
    NodeMod animNode = new NodeMod(name, animSeq, null, w.toolset, editor);
    w.mclient.register(w.user, w.view, animNode);
    return animNode;
  }

  /**
   * Registers a node without frame numbers
   * @param w wrapper
   * @param name node name
   * @param extention file extention
   * @param editor the {@link BaseEditor} for the node
   * @return the {@link NodeMod} representing the new node
   * @throws PipelineException
   */
  public static NodeMod registerNode(Wrapper w, String name, String extention,
      BaseEditor editor) throws PipelineException
  {
    File f = new File(name);
    FileSeq animSeq = new FileSeq(f.getName(), extention);
    NodeMod animNode = new NodeMod(name, animSeq, null, w.toolset, editor);
    w.mclient.register(w.user, w.view, animNode);
    return animNode;
  }

  /**
   * Returns true if the name is a registered node in pipeline.
   * @param w wrapper
   * @param name the node name
   * @return <code>true</code> if the node exists.  Else, <code>false</code>.
   * @throws PipelineException
   */
  public static boolean doesNodeExists(Wrapper w, String name) throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(name, false);
    NodeTreeComp treeComps = w.mclient.updatePaths(w.user, w.view, comps);
    State state = getState(treeComps, name);
    if ( state == null || state.equals(State.Branch) )
      return false;
    return true;
  }

  private static State getState(NodeTreeComp treeComps, String scene)
  {
    State toReturn = null;
    Path p = new Path(scene);
    NodeTreeComp dest = null;
    for (String s : p.getComponents())
    {
      if ( dest == null )
	dest = treeComps.get(s);
      else
	dest = dest.get(s);

      if ( dest == null )
	break;
    }
    if ( dest != null )
      toReturn = dest.getState();
    return toReturn;
  }

  /**
   * Returns a enum which indicates where a node lives.<p>
   * If a version of the node exists in the current working area, then 
   * {@link NodeLocation#LOCAL} is returned.  If the node has been checked in,
   * but is not checked out into the current working area, then {@link NodeLocation#REP}
   * is returned.  If the name represents a directory, <code>null</code> is returned.
   * Otherwise, {@link NodeLocation#OTHER} is returned, indicating that the node exists
   * in some other working area, but was never checked in.
   * @param w wrapper
   * @param name the node name
   * @return The location of the node.
   * @throws PipelineException
   */
  public static NodeLocation getNodeLocation(Wrapper w, String name)
    throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(name, false);
    NodeTreeComp treeComps = w.mclient.updatePaths(w.user, w.view, comps);
    Path p = new Path(name);
    ArrayList<String> parts = p.getComponents();
    for (String comp : parts)
    {
      treeComps = treeComps.get(comp);
    }
    NodeTreeComp.State state = treeComps.getState();
    NodeLocation toReturn = null;
    //System.err.println(state);
    switch (state)
    {
      case Branch:
	toReturn = null;
	break;
      case WorkingCurrentCheckedInNone:
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
   * @param w
   * 	Wrapper class.
   * @param start
   * 	The path to start the search underneath
   * @return a list of all the paths
   * @throws PipelineException
   */
  public static ArrayList<String> getChildrenNodes(Wrapper w, String start)
    throws PipelineException
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start, false);
    NodeTreeComp treeComps = w.mclient.updatePaths(w.user, w.view, comps);
    Path p = new Path(start);
    ArrayList<String> parts = p.getComponents();
    for (String comp : parts)
    {
      treeComps = treeComps.get(comp);
    }
    for (String s : treeComps.keySet())
    {
      toReturn.add(s);
    }
    return toReturn;
  }

  /**
   * Returns all the directories that are located directly underneath a given path.
   * 
   * @param w
   * 	Wrapper class.
   * @param start
   * 	The path to start the search underneath
   * @return a list of all the directories
   * @throws PipelineException
   */
  public static ArrayList<String> getChildrenDirs(Wrapper w, String start)
    throws PipelineException
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start, false);
    NodeTreeComp treeComps = w.mclient.updatePaths(w.user, w.view, comps);
    Path p = new Path(start);
    ArrayList<String> parts = p.getComponents();
    for (String comp : parts)
    {
      if ( treeComps == null )
	break;
      treeComps = treeComps.get(comp);
    }
    if ( treeComps != null )
    {
      for (String s : treeComps.keySet())
      {
	NodeTreeComp comp = treeComps.get(s);
	if ( comp.getState() == NodeTreeComp.State.Branch )
	  toReturn.add(s);
      }
    }
    return toReturn;
  }

  /**
   * Returns all the nodes that are located underneath a given path.
   * 
   * @param w
   * 	Wrapper class.
   * @param start
   * 	The path to start the search underneath
   * @return a list of all the nodes
   * @throws PipelineException
   */
  public static ArrayList<String> getAllNodes(Wrapper w, String start)
    throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start, true);
    NodeTreeComp treeComps = w.mclient.updatePaths(w.user, w.view, comps);
    ArrayList<String> toReturn = new ArrayList<String>();
    for (String s : treeComps.keySet())
    {
      findNodes(treeComps.get(s), toReturn, "/");
    }
    return toReturn;
  }

  private static void findNodes(NodeTreeComp treeComps, ArrayList<String> toReturn,
      String path)
  {
    State state = treeComps.getState();
    if ( state.equals(State.Branch) )
      for (String s : treeComps.keySet())
	findNodes(treeComps.get(s), toReturn, path + treeComps.getName() + "/");
    else
      toReturn.add(path + treeComps.getName());
  }

  /**
   * Links the target node to the source node using the given Link Policy.
   * If the action on the target node is a MayaReference or a MayaImport
   * it will also set the "Prefix Name" SourceParam on the action.  In these
   * cases, this method changes the action that is passed in. 
   * 
   * @param w
   * 	Wrapper class.
   * @param target
   * 	The node that will be the target of the link.
   * @param source
   * 	The node that will be the source of the link.
   * @param action
   * 	The action associated with the target node.  This will be modified.
   * @param policy
   * 	What sort of link should be made.
   * @param nameSpace
   * 	The namespace that will be used if the action is a 
   * 	MayaReference or a MayaImport.
   * @throws PipelineException
   */
  public static void referenceNode(Wrapper w, String target, String source,
      BaseAction action, LinkPolicy policy, String nameSpace) throws PipelineException
  {
    boolean reference = false;

    String actionType = action.getName();
    if ( actionType.equals("MayaReference") || actionType.equals("MayaImport") || actionType.equals("MayaBuild") )
      reference = true;

    w.mclient.link(w.user, w.view, target, source, policy, LINKALL, null);
    if ( reference )
    {
      action.initSourceParams(source);
      action.setSourceParamValue(source, "PrefixName", nameSpace);
    }
  }

  /**
   * Applies a group of presets to an Action.  <p>
   * Changes the Action. Be careful!
   * 
   * @param act
   * 	The Action to apply the preset to.
   * @param preset
   * 	The map of presets.
   */
  @SuppressWarnings("unchecked")
  public static void setPresets(BaseAction act, SortedMap<String, Comparable> preset)
  {
    for (String name : preset.keySet())
    {
      act.setSingleParamValue(name, preset.get(name));
    }
  }

  /**
   * Checks the current version ID of a node against the newest checked-in version.
   * If the ID's are the same, it does nothing.  If ID is older it checks out 
   * the node, using the CheckOutMode and CheckOutMethod passed in.
   *
   * @param w
   * @param name
   * @param mode
   * @param method
   * @throws PipelineException
   */
  public static void getNewest(Wrapper w, String name, CheckOutMode mode,
      CheckOutMethod method) throws PipelineException
  {
    NodeMod mod;
    try
    {
      mod = w.mclient.getWorkingVersion(w.user, w.view, name);
    } catch ( PipelineException ex )
    {
      mod = null;
    }

    TreeSet<VersionID> versions = null;
    try
    {
      versions = w.mclient.getCheckedInVersionIDs(name);
    } catch ( PipelineException ex )
    {
      System.err.println("Aborting getNewest since no checked-in versions of " + name
	  + " exist");
      return;
    }
    VersionID latestID = versions.last();

    if ( mod != null )
    {
      VersionID currentID = mod.getWorkingID();

      if ( currentID.compareTo(latestID) < 0 )
      {
	w.mclient.checkOut(w.user, w.view, name, latestID, mode, method);
      }
    } else
    {
      w.mclient.checkOut(w.user, w.view, name, latestID, mode, method);
    }
  }

  /**
   * Checkouts the latest version of the node using the CheckOutMode and 
   * CheckOutMethod passed in.
   * 
   * @param w
   * @param name
   * @param mode
   * @param method
   * @throws PipelineException
   */
  public static void getLatest(Wrapper w, String name, CheckOutMode mode,
      CheckOutMethod method) throws PipelineException
  {
     TreeSet<VersionID> versions = null;
     try
     {
       versions = w.mclient.getCheckedInVersionIDs(name);
     } catch ( PipelineException ex )
     {
       System.err.println("Aborting getLatest since no checked-in versions of " + name
 	  + " exist");
       return;
     }
    VersionID latestID = versions.last();

    w.mclient.checkOut(w.user, w.view, name, latestID, mode, method);
  }

  /**
   * Locks the given node to the latest version.
   * @param w wrapper
   * @param name the node name
   * @throws PipelineException
   */
  public static void lockLatest(Wrapper w, String name) throws PipelineException
  {
    TreeSet<VersionID> versions = w.mclient.getCheckedInVersionIDs(name);
    VersionID latestID = versions.last();
    NodeID nodeID = new NodeID(w.user, w.view, name);

    w.mclient.lock(nodeID, latestID);
  }

  /**
   * Prints all the check-in messages for a node to standard out.
   * @param w wrapper
   * @param name the node name
   * @throws PipelineException
   */
  public static void printNodeHistory(Wrapper w, String name) throws PipelineException
  {
    TreeMap<VersionID, LogMessage> history = w.mclient.getHistory(name);
    for (VersionID ver : history.keySet())
    {
      LogMessage log = history.get(ver);
      String message = log.getMessage();
      System.out.println(ver.toString() + "\t" + message);
    }
  }

  /**
   * Evolves a node to the latest version
   * @param w wrapper
   * @param name the node name
   * @throws PipelineException
   */
  public static void evolveNode(Wrapper w, String name) throws PipelineException
  {
    VersionID latestID = w.mclient.getCheckedInVersionIDs(name).last();
    NodeID id = new NodeID(w.user, w.view, name);
    w.mclient.evolve(id, latestID);
  }

  public static final LinkPolicy REF = LinkPolicy.Reference;
  public static final LinkPolicy DEP = LinkPolicy.Dependency;
  public static final LinkRelationship LINKALL = LinkRelationship.All;
  public static final LinkRelationship LINKONE = LinkRelationship.OneToOne;
  public static final CheckOutMode over = CheckOutMode.OverwriteAll;
  public static final CheckOutMode keep = CheckOutMode.KeepModified;
  public static final CheckOutMethod modi = CheckOutMethod.Modifiable;
  public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
  public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
  public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;

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
     * The node exists in the repository, but not the current working area.
     */
    REP,
    /**
     * The node only exists in the another working area.
     */
    OTHER;
  }
}
