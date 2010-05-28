// $Id: BaseUtil.java,v 1.47 2009/11/03 03:48:00 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import sun.rmi.runtime.*;
import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.math.Range;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   U T I L                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract class that provides the basis for all utility classes in Pipeline.
 * <P>
 * This class contains all the information and helper methods that will be used by utility
 * classes. There are two main types of methods in this class.
 * <p>
 * One group relates to UtilityParameters and are helper methods for adding, removing, and
 * querying parameters. These methods follow the standard format of all other plugins with
 * parameters, with the additional functionality that there are methods to replace parameters.
 * Parameter replacement is intended for situations where a parameter is needed but its values
 * are not known when the utility is initialized. In these cases, the parameter can be
 * declared and placed in a layout as a {@link PlaceholderUtilityParam} and then later
 * replaced with the actual parameter. See the {@link #replaceParam(UtilityParam)} method for
 * more information on this.
 * <p>
 * The other group of methods is for high-level interaction with and querying of node
 * networks. These include methods for searching trees, interacting with Actions, and getting
 * versions of nodes.
 * <p>
 * BaseUtil declares one parameter in its constructor. Any class which inherits from BaseUtil
 * that is using parameters and parameter layouts will need to have a way to account for this
 * parameter.
 * <ul>
 * <li> UtilContext - This parameter represents the context in which the Utility is going to
 * be run.
 * </ul>
 */
public abstract
class BaseUtil
  extends Described
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor that passes in all the information BaseUtil needs to initialize.
   * 
   * @param name
   *   The name of the utility.
   * 
   * @param desc
   *   A description of what the utility should do.
   * 
   * @param mclient
   *   The instance of the Master Manager that the utility will use to execute.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the utility will use to execute.
   * 
   * @param context
   *   The {@link UtilContext} that this utility is going to operate in.
   *   
   * @param logger
   *   The instance of the {@link LogMgr} that the utility should use to write its logs or
   *   <code>null</code> if the default instance should be used.
   */
  protected 
  BaseUtil
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    UtilContext context,
    String logger
  ) 
    throws PipelineException 
  {
    super(name, desc);
    pClient = mclient;
    pQueue = qclient;
    pPlug = PluginMgrClient.getInstance();
    pContext = context;
    
    if (logger == null)
      pLog = LogMgr.getInstance();
    else
      pLog = LogMgr.getInstance(logger);
    
    if (pContext == null)
      pContext = UtilContext.getDefaultUtilContext(mclient);
    
    if (name.contains(" "))
      throw new PipelineException
      ("A class with Builder Parameters cannot have a space in its name, " +
       "due to command-line parsing restrictions.  (" + name + ") is not a valid name.");
    
    if (name.contains("-"))
      throw new PipelineException
      ("Due to command line parsing requirements, you cannot include the '-' character " +
       "in the name of any class that uses Builder Parameters.");
    
    pParams = new TreeMap<String, UtilityParam>();
    pParamMapping = new TreeMap<ParamMapping, ParamMapping>();
    
    {
      UtilityParam param = 
      new UtilContextUtilityParam
      (aUtilContext, 
       "The User, View, and Toolset to perform these actions in.",  
       pContext,
       mclient);
      addParam(param);
    }
  }

  /**
   * Default constructor for BaseUtil. <P>
   * 
   * The {@link UtilContext} for this utility will need to be initialized using
   * {@link #setContext(UtilContext)} before any of the methods in this class are used. 
   * 
   * @param name
   *   The name of the utility.
   * 
   * @param desc
   *   A description of what the utility should do.
   * 
   * @param mclient
   *   The instance of the Master Manager that the utility will use to execute.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the utility will use to execute.
   *   
   * @param logger
   *   The instance of the {@link LogMgr} that the utility should use to write its logs or
   *   <code>null</code> if the default instance should be used.
   */
  protected 
  BaseUtil
  (
    String name,
    String desc,    
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    String logger
  ) 
    throws PipelineException 
  {
    this(name, desc, mclient, qclient, null, logger);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets a modified form of the name of this instance with spaces inserted between 
   * each word. <P> 
   * 
   * This name is used in the UI to label fields and table columns in a more human 
   * friendly manner.
   */ 
  public final String
  getNameUI()
  {
    StringBuilder buf = new StringBuilder();
    char c[] = getName().toCharArray();
    int wk;
    buf.append(c[0]);
    for(wk=1; wk<(c.length-1); wk++) {
      if(Character.isUpperCase(c[wk]) && 
	 (Character.isLowerCase(c[wk-1]) ||
	  Character.isLowerCase(c[wk+1])))
	  buf.append(" ");

      buf.append(c[wk]);
    }
    buf.append(c[wk]);

    return (buf.toString());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L   C O N T E X T S                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the {@link UtilContext} that this utility will operate in. <P>
   * 
   * This should not be used to change the context in the middle of running a tool. Generally,
   * this should be set in the first validatePhase of the first Information Pass in a builder.
   * That will guarantee that all future decisions are made in the right default space. If a
   * change is needed to the working environment in the middle of running a builder, it would
   * be best to just create a new {@link UtilContext} and pass that into the necessary stages,
   * rather than calling this method, as there will be no impact on the rest of the Builder. 
   * 
   * @param context
   *   The {@link UtilContext} for the utility.
   */
  public void 
  setContext
  (
    UtilContext context
  )
  {
    pContext = context;
  }
  
  /**
   * Disables a node's Action. <p>
   * 
   * Takes the name of a node and disables the Action for that node. 
   * 
   * @param name
   *   The full name of the node to have its Action disabled.
   *   
   * @throws PipelineException
   *   If there is a problem disabling the action.
   */
  public void 
  disableAction
  (
    String name
  ) 
    throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), name);
    NodeMod nodeMod = pClient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(false);
    pClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }
  
  /**
   * Enables a node's Action.<p>
   * 
   * Takes the name of a node and enables the Action for that node. 
   * 
   * @param name
   *   The full name of the node to have its Action enabled.
   * 
   * @throws PipelineException
   *   If there is a problem enabling the action.
   */
  public void 
  enableAction
  (
    String name
  ) 
    throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), name);
    NodeMod nodeMod = pClient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(true);
    pClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }

  /**
   * Removes a node's Action.  <p>
   * 
   * Takes the name of a node and removes any Action that the node might have. 
   * 
   * @param name
   *   The full name of the node to have its Action removed.
   *   
   * @throws PipelineException
   *   If there is a problem removing the action.
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
   * Releases a group of nodes. <p>
   * 
   * Takes a {@link TreeSet} of node names and releases all the nodes in the list. 
   * 
   * @param nodes
   *   A list of the full nodes names of everything to be released.
   *  
   * @throws PipelineException
   *   If there is a problem releasing a node.
   */
  public void 
  releaseNodes
  (
    TreeSet<String> nodes
  ) 
    throws PipelineException
  {
    for(String s : nodes) {
      pClient.release(getAuthor(), getView(), s, true);
    }
  }

  /**
   * Returns a boolean that indicates if the name is an existing node in Pipeline.
   * 
   * @param name
   *   The name of the node to search for.
   *        
   * @return 
   *   <code>true</code> if the node exists. <code>false</code> if the node does not exist 
   *   or if the specified path is a Branch.
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
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    State state = treeComps.getState(name);
    if ( state == null || state.equals(State.Branch) )
      return false;
    return true;
  }
  
  /**
   * Returns a boolean that indicates if the node is checked out into the current working 
   * area.
   * 
   * @param name
   *   The name of the node to search for.
   *        
   * @return 
   *   <code>true</code> if the node exists in the current working area.  <code>false</code> 
   *   if the node does not exist or if it is not checked-out.
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
   * Returns all the paths that are located directly underneath a given path.
   * 
   * @param start
   *   The path to start the search underneath.
   *        
   * @return 
   *   An {@link ArrayList} containing all the paths (both directories and nodes) located 
   *   directly under the given path.
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
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    Path p = new Path(start);
    ArrayList<String> parts = p.getComponents();
    for(String comp : parts) {
      if (treeComps == null)
        break;
      treeComps = treeComps.get(comp);
    }
    if (treeComps != null) {
      for(String s : treeComps.keySet()) {
        toReturn.add(s);
      }
    }
    return toReturn;
  }

  /**
   * Returns all the directories that are located directly underneath a given path.
   * 
   * @param start
   *   The path to start the search underneath.
   *   
   * @return 
   *   An {@link ArrayList} containing the names of all the directories located directly 
   *   under the given path.
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
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
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
   *   The path to start the search underneath
   *   
   * @return 
   *   An {@link ArrayList} containing all the node paths located under the given path.
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
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    ArrayList<String> toReturn = new ArrayList<String>();
    for(String s : treeComps.keySet()) {
      findNodes(treeComps.get(s), toReturn, "/", start);
    }
    return toReturn;
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
   * Check-outs the latest version of a node if the working version is older than the latest
   * checked-in version. <p>
   * 
   * Checks the current version ID of a node against the newest checked-in version. If the
   * ID's are the same, it does nothing. If ID is older it checks out the node, using the
   * CheckOutMode and CheckOutMethod passed in.
   * 
   * @param name
   *   The name of the node node to checkout.
   *   
   * @param mode
   *   The {@link CheckOutMode} to use.
   *   
   * @param method
   *   The {@link CheckOutMethod} to use.
   * 
   * @see #checkOutLatest(String, CheckOutMode, CheckOutMethod) checkOutLatest
   * @see #frozenStomp(String) frozenStomp
   * 
   * @throws PipelineException 
   *   If no checked-in versions of the node exist.
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
      mod = pClient.getWorkingVersion(getAuthor(), getView(), name);
    }
    catch(PipelineException ex) {}

    TreeSet<VersionID> versions = null;
    try {
      versions = pClient.getCheckedInVersionIDs(name);
    }
    catch(PipelineException ex) {
      throw new PipelineException(
        "checkOutNewer has aborted since there is no Checked-In Version of the node.\n "
            + ex.getMessage());
    }
    VersionID latestID = versions.last();

    if(mod != null) {
      VersionID currentID = mod.getWorkingID();

      if(currentID.compareTo(latestID) < 0) {
        pClient.checkOut(getAuthor(), getView(), name, latestID, mode, method);
      }
    }
    else {
      pClient.checkOut(getAuthor(), getView(), name, latestID, mode, method);
    }
  }
  
  /**
   * Check-out a version of a node if that version is not currently checked-out in the current
   * working area. <p>
   * 
   * Checks the current version ID of a node against the specified version. If the ID's are 
   * the same, it does nothing. If ID is different it checks out the node, using the
   * CheckOutMode and CheckOutMethod passed in.
   * 
   * @param name
   *   The name of the node to consider forcheckout.
   *   
   * @param verID
   *   The version of the node to consider for checkout.
   * 
   * @param mode
   *   The {@link CheckOutMode} to use.
   *   
   * @param method
   *   The {@link CheckOutMethod} to use.
   *   
   * @throws PipelineException 
   *   If no checked-in versions of the node exist.
   */
  public void
  checkOutVersionIfDifferent
  (
    String name,
    VersionID verID,
    CheckOutMode mode, 
    CheckOutMethod method
  )
    throws PipelineException
  {
    NodeMod mod = null;
    try {
      mod = pClient.getWorkingVersion(getAuthor(), getView(), name);
    }
    catch(PipelineException ex) {}

    TreeSet<VersionID> versions = null;
    try {
      versions = pClient.getCheckedInVersionIDs(name);
    }
    catch(PipelineException ex) {
      throw new PipelineException
        ("checkOutVersionIfDifferent has aborted since there is no Checked-In Version " +
         "of the node.\n " + ex.getMessage());
    }
    
    if (!versions.contains(verID))
      throw new PipelineException
        ("There is no version (" + verID + ") of the node named (" + name + ") that was " +
         "specified for checkout.");
    
    if(mod != null) {
      VersionID currentID = mod.getWorkingID();

      if(currentID.compareTo(verID) < 0) {
        pClient.checkOut(getAuthor(), getView(), name, verID, mode, method);
      }
    }
    else {
      pClient.checkOut(getAuthor(), getView(), name, verID, mode, method);
    }
    
  }

  /**
   * Check-out the latest version of a node.  <p>
   * 
   * Check-out the latest version of the node using the CheckOutMode and CheckOutMethod
   * passed in.
   * 
   * @param name
   *   The name of the node node to checkout.
   *   
   * @param mode
   *   The {@link CheckOutMode} to use.
   *   
   * @param method
   *   The {@link CheckOutMethod} to use.
   *   
   * @see #checkOutNewer(String, CheckOutMode, CheckOutMethod) checkOutNewer
   * @see #frozenStomp(String) frozenStomp
   * 
   * @throws PipelineException 
   *   If no checked-in versions of the node exist.
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
    pClient.checkOut(getAuthor(), getView(), name, null, mode, method);
  }
  
  /**
   * Check-out the latest version of a node using the Overwrite All and All Frozen options.
   * 
   * @param name
   *   The name of the node node to checkout.
   * 
   * @see #checkOutNewer(String, CheckOutMode, CheckOutMethod) checkOutNewer
   * @see #checkOutLatest(String, CheckOutMode, CheckOutMethod) checkOutLatest
   * 
   * @throws PipelineException 
   *   If no checked-in versions of the node exist.
   */
  public void 
  frozenStomp
  (
    String name
  )
    throws PipelineException
  {
    pClient.checkOut(getAuthor(), getView(), name, null, 
                     CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
  }

  /**
   * Lock to the latest version of the node.
   * 
   * @param name
   *   The name of the node node to lock.
   *   
   * @throws PipelineException 
   *   If no checked-in versions of the node exist.
   */
  public void 
  lockLatest
  (
    String name
  ) 
    throws PipelineException
  {
    TreeSet<VersionID> versions = pClient.getCheckedInVersionIDs(name);
    VersionID latestID = versions.last();
    pClient.lock(getAuthor(), getView(), name, latestID);
  }

  /**
   * Determine the latest checked-in version of a node and evolves the current working
   * version of the that node to the latest version.  <p>
   * 
   * Throws a {@link PipelineException} if no checked-in versions of the node exist.
   * 
   * @param name
   *   The name of the node to be evolved.
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
      versions = pClient.getCheckedInVersionIDs(name);
    }
    catch(PipelineException ex) {
      throw new PipelineException(
        "evolveNode has aborted since there is no Checked-In Version of the node.\n "
            + ex.getMessage());
    }
    VersionID latestID = versions.last();
    NodeID id = new NodeID(getAuthor(), getView(), name);
    pClient.evolve(id, latestID);
  }
  
  
  /**
   * Checks in a group of nodes with the specified message and the specified check-in Level.
   * 
   * @param nodes
   *   The list of nodes to be checked in.
   *   
   * @param level
   *   The Level of the check in.
   *   
   * @param message
   *   The message for all the checked in nodes.
   */
  public void 
  checkInNodes
  (
    List<String> nodes, 
    VersionID.Level level, 
    String message
  ) 
    throws PipelineException
  {
    if (nodes != null) 
      for(String node : nodes) {
        pLog.log(Kind.Ops, Level.Finer, 
          "Checking in node (" + node + ").");
	pClient.checkIn(getAuthor(), getView(), node, message, level);
      }
  }

  /**
   * Returns <code>true</code> if the entire tree given has an {@link OverallQueueState} of
   * Finished. <p>
   * 
   * Starts at the node specified in the node status passed in and decends down the tree,
   * checking the {@link OverallQueueState} of each node. If every node it finds is in the
   * Finished state, the method will return <code>true</code>. If any are found in a state
   * which is not Finished, then <code>false</code> is returned.
   * 
   * @param status
   *   The heavyweight status of the root node of the tree to be searched.
   */
  public boolean 
  isTreeFinished
  (
    NodeStatus status
  )
  {
    if(!status.hasHeavyDetails()) 
      throw new IllegalArgumentException
        ("A node status with heavyweight details is required by this method!");
      
    OverallQueueState state = status.getHeavyDetails().getOverallQueueState();
    switch(state){
    case Finished: {
      Collection<NodeStatus> stati = status.getSources();
      if(stati != null) {
	for(NodeStatus stat : stati) {
	  boolean temp = isTreeFinished(stat);
	  if(!temp)
	    return temp;
	}
	return true;
      }
      return true;
    } 
    
    default: 
      {
        pLog.logAndFlush(Kind.Ops, Level.Warning, 
          "The node (" + status.getName() + ") which was expected to be in the Finished " +
          "state " + "was actually in the (" + state + ") state");
        return false;
      }
    }
  }
  
  /**
   * Returns a list of nodes in the given tree that do not have an {@link OverallQueueState} 
   * of Finished. <p>
   * 
   * Starts at the node specified in the node status passed in and descends down the tree,
   * checking the {@link OverallQueueState} of each node.
   * 
   * @param nodeNames
   *   The TreeSet to add the names too.
   * 
   * @param status
   *   The heavyweight status of the root node of the tree to be searched.
   */
  @SuppressWarnings("incomplete-switch")
  public void 
  findBadNodes
  (
   TreeSet<String> nodeNames,
   NodeStatus status
  )
  {
    if(!status.hasHeavyDetails()) 
      throw new IllegalArgumentException
        ("A node status with heavyweight details is required by this method!");

    OverallQueueState state = status.getHeavyDetails().getOverallQueueState();
    Collection<NodeStatus> stati = status.getSources();
    switch (state) {
    case Aborted:
    case Failed:
    case Stale:
      nodeNames.add(status.getName());
      break;
    }
    if(stati != null)
      for (NodeStatus stat : stati)
        findBadNodes(nodeNames, stat);
  }

  /**
   * Static method to return a list of all the selection keys. 
   * 
   * @param client
   *   The instance of the Queue Manager to use to extract the information.
   */
  public static ArrayList<SelectionKey>
  getSelectionKeys
  (
    QueueMgrClient client
  )
    throws PipelineException
  {
    return client.getSelectionKeys();
  }

  /**
   * Static method to return a list of all the license keys.<P> 
   * 
   * @param client
   *   The instance of the Queue Manager to use to extract the information.
   */
  public static ArrayList<LicenseKey>
  getLicenseKeys
  (
    QueueMgrClient client
  )
    throws PipelineException
  {
    return client.getLicenseKeys();
  }
   

  /*----------------------------------------------------------------------------------------*/
  /*   U T I L   H E L P E R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursive function to search for nodes under a given path.  <p>
   * 
   * Starts with the current {@link NodeTreeComp}, travels down the tree, and adds any nodes
   * it finds to an {@link ArrayList} being passed as a parameter. It is important to note
   * when using this method that the {@link ArrayList} is being modified inside the method.
   * The {@link ArrayList} will contain the fully resolved node names for all the nodes.
   * 
   * @param treeComps
   *   A {@link NodeTreeComp} that should contain information about the node name
   *   specified by scene. The most common way to acquire this data structure is with the
   *   <code>updatePaths</code> method in {@link MasterMgrClient}.
   * 
   * @param toReturn
   *   An {@link ArrayList} that will hold every node that is found by this method.
   * 
   * @param path
   *   The full path that leads up to the current {@link NodeTreeComp}. This is needed
   *   to build the full node name being stored in the ArrayList.
   * 
   * @param start
   *   The path we want all of the nodes to be contained under.  This will keep nodes which
   *   live a single level above the searched directory from somehow creeping in from the
   *   NodeTreeComps.
   */
  private void 
  findNodes
  (
    NodeTreeComp treeComps, 
    ArrayList<String> toReturn, 
    String path, 
    String start
  )
  {
    State state = treeComps.getState();
    if(state.equals(State.Branch))
      for(String s : treeComps.keySet())
	findNodes(treeComps.get(s), toReturn, path + treeComps.getName() + "/", start);
    else {
      String node = path + treeComps.getName();
      if (node.startsWith(start))
        toReturn.add(node);
    }
  }

  /**
   * Shortcut method to get the author value of the utility's {@link UtilContext}.
   * 
   * @return 
   *   The author.
   */
  protected final String 
  getAuthor()
  {
    return pContext.getAuthor();
  }

  /**
   * Shortcut method to get the view value of the utility's {@link UtilContext}.
   * 
   * @return 
   *   The working area.
   */
  protected final String 
  getView()
  {
    return pContext.getView();
  }

  /**
   * Shortcut method to get the toolset value of the stage's {@link UtilContext}.
   * 
   * @return 
   *   The toolset.
   */
  protected final String 
  getToolset()
  {
    return pContext.getToolset();
  }
  
  /**
   * Has the utility been locked, meaning no further parameters or other sensitive data
   * structures can be added.
   * <p>
   * Locking occurs upon calling the {@link #setLayout(PassLayoutGroup)} method.
   */
  public boolean
  isLocked()
  {
    return pLocked;
  }
  
  /**
   * Get the instance of {@link MasterMgrClient} being used by this utility.
   */
  public MasterMgrClient
  getMasterMgrClient()
  {
    return pClient;
  }
  
  /**
   * Get the instance of {@link QueueMgrClient} being used by this utility.
   */
  public QueueMgrClient
  getQueueMgrClient()
  {
    return pQueue;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  G L O B A L   P A R A M E T E R S                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the utility have any parameters?
   */
  public boolean 
  hasParams()
  {
      return ( !pParams.isEmpty() );
  }

  /**
   * Add a parameter to this utility. <P>
   * 
   * This method is used by subclasses in their constructors to initialize the set of global
   * parameters that they support.
   * 
   * @param param
   *   The parameter to add.
   *   
   * @throws PipelineException
   *   If a parameter is added with the same name as an existing parameter.
   */
  protected void 
  addParam
  (
    UtilityParam param
  )
  {
    if (param == null)
      throw new IllegalArgumentException("Unable to add a (null) param to a builder.");
    
    if (isLocked())
      throw new IllegalStateException
        ("Illegal attempt to add a parameter (" + param.getName() + ") after " +
         "the Utility has been locked.");
    
    if ( pParams.containsKey(param.getName()) )
      throw new IllegalArgumentException
        ("A parameter named (" + param.getName() + ") already exists!");

    pParams.put(param.getName(), param);
    
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding a parameter named (" + param.getName() + ") to a Builder " +
      "identified by (" + getName() + ")");
  }
  
  /**
   * Replaces an existing parameter with a new parameter. <P>
   * 
   * The conditions under which this will succeed are as follows. The parameter must have the 
   * same name as the parameter it  is replacing. If an attempt is made to replace a parameter 
   * that does not exist, an exception is thrown. Only parameters which have not had a value 
   * inputed into them can be replaced. If an  attempt is made to replace a parameter from a 
   * layout which is not after the current pass an exception will be thrown.
   * 
   * @param param
   *   The parameter to replace.
   *   
   * @throws PipelineException
   *   If a parameter which does not exist or which belongs to a pass that is before the
   *   current pass is replaced.
   */
  protected void 
  replaceParam
  (
    UtilityParam param
  )
    throws PipelineException
  {
    if ( !pParams.containsKey(param.getName()) )
      throw new IllegalArgumentException
        ("No parameter named (" + param.getName() + ") exists to be replaced!");
    
    int paramPass = getParameterPass(new ParamMapping(param.getName()));
    int currentPass = getCurrentPass();
    if (paramPass <= currentPass)
      throw new PipelineException
        ("The attempt to replace parameter ("+ param.getName() +") in pass " + 
         "(" + paramPass + ") failed because the current pass is (" + currentPass + ").");

    pParams.put(param.getName(), param);
    
    pLog.log(Kind.Bld, Level.Finest, 
      "Replaced the parameter named (" + param.getName() + ") in Builder " +
      "identified by (" + getName() + ")");
  }

  /**
   * Get the value of the parameter with the given name.
   * 
   * @param name
   *   The name of the parameter.
   * 
   * @return 
   *   The parameter value.
   *   
   * @throws IllegalArgumentException 
   *   If no parameter with the given name exists or if the  named parameter does not 
   *   implement {@link SimpleParamAccess}.
   */
  @SuppressWarnings("unchecked")
  public Comparable 
  getParamValue
  (
    String name
  ) 
  {
    UtilityParam param = getParam(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("Unable to determine the value of the (" + name + ") parameter!");
    if (! (param instanceof SimpleParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "SimpleParamAccess.");
    return ((SimpleParamAccess) param).getValue();
  }
  
  /**
   * Get the value of the Simple Parameter located inside the named Complex Parameter and
   * identified by the list of keys.
   * 
   * @throws IllegalArgumentException 
   *   If no parameter with the given name exists or if the named parameter does not 
   *   implement {@link SimpleParamAccess}.
   */
  @SuppressWarnings("unchecked")
  public Comparable
  getParamValue
  (
    String name,
    List<String> keys
  )
  {
    UtilityParam param = getParam(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("Unable to determine the value of the (" + name + ") parameter!");
    
    if (keys == null || keys.isEmpty())
      return getParamValue(name);
    
    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    return ((ComplexParamAccess<UtilityParam>) param).getValue(keys); 
  }
  
  /**
   * Get the value of the Simple Parameter described by the {@link ParamMapping}.
   * 
   * @param mapping
   *   The name of the parameter.
   * 
   * @throws IllegalArgumentException 
   *   If no parameter with the given name exists or if the named parameter does not implement 
   *   {@link SimpleParamAccess}.
   */
  @SuppressWarnings("unchecked")
  public Comparable
  getParamValue
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return getParamValue(mapping.getParamName(), mapping.getKeys());
    return getParamValue(mapping.getParamName());
  }

  /**
   * Get the parameter located inside the named Complex Parameter and identified by the list
   * of keys.
   * 
   * @throws IllegalArgumentException
   *   If no parameter with the given name exists.
   */
  @SuppressWarnings("unchecked")
  public UtilityParam 
  getParam
  (
    String name,
    List<String> keys
  )
  {
    UtilityParam param = getParam(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("Unable to determine the value of the (" + name + ") parameter!");
    
    if (keys == null || keys.isEmpty())
      return param;
    
    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    return ((ComplexParamAccess<UtilityParam>) param).getParam(keys); 
  }
  
  /**
   * Get the parameter with the given name.
   * 
   * @param name
   *   The name of the parameter.
   * 
   * @throws IllegalArgumentException
   *   If no parameter with the given name exists.
   */
  public UtilityParam 
  getParam
  (
    String name
  )
  {
    if ( name == null )
      throw new IllegalArgumentException("The parameter name cannot be (null)!");
    return pParams.get(name);
  }
  
  /**
   * Get the parameter described by the {@link ParamMapping}.
   * 
   * @param mapping
   *   The name of the parameter.
   * 
   * @throws IllegalArgumentException
   *   If no parameter with the given name exists.
   */
  public UtilityParam
  getParam
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return getParam(mapping.getParamName(), mapping.getKeys());
    return getParam(mapping.getParamName());
  }

  /**
   * Gets a collection of all the Parameters in this utility.
   */
  public Collection<UtilityParam> 
  getParams()
  {
    return Collections.unmodifiableCollection(pParams.values());
  }

  /**
   * Gets a collection of the names of all the Parameters in this utility.
   */
  public Collection<String> 
  getParamNames()
  {
    return Collections.unmodifiableCollection(pParams.keySet());
  }
  
  /**
   * Gets a sorted Map of all the Parameters in this builder, with the keys being
   * the name of the parameters and the values the actual parameters.
   */
  public SortedMap<String, UtilityParam> 
  getParamMap()
  {
    return Collections.unmodifiableSortedMap(pParams);
  }
  
  /**
   * Set the value of a parameter.
   * 
   * @param name
   *   The name of the parameter.
   * 
   * @param value
   *   The new value of the parameter.
   */
  @SuppressWarnings("unchecked")
  public final boolean 
  setParamValue
  (
    String name, 
    Comparable value
  )
  {
    if ( name == null )
      throw new IllegalArgumentException("The parameter name cannot be (null)!");

    UtilityParam param = pParams.get(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("No parameter named (" + name + ") exists for this extension!");
    if (! (param instanceof SimpleParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "SimpleParamAccess.");

    ((SimpleParamAccess) param).setValue(value);
    return false;
  }

  /**
   * Set the value of a parameter.
   * 
   * @param name
   *        The name of the parameter.
   * @param keys
   *        The list of keys that are an index into the Complex Parameter
   * @param value
   *        The new value of the parameter.
   */
  @SuppressWarnings("unchecked")
  public final boolean
  setParamValue
  (
    String name,
    List<String> keys,
    Comparable value  
  )
  {
    UtilityParam param = getParam(name);
    if ( param == null )
      throw new IllegalArgumentException
        ("Unable to determine the value of the (" + name + ") parameter!");
    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    return ((ComplexParamAccess<UtilityParam>) param).setValue(keys, value);
  }
  
  /**
   * Set the value of a parameter.
   * 
   * @param mapping
   *        The mapping that describes this parameter
   * @param value
   *        The new value of the parameter.
   */
  @SuppressWarnings("unchecked")
  public final boolean
  setParamValue
  (
    ParamMapping mapping,
    Comparable value
  )
  {
    if (mapping.hasKeys())
      return setParamValue(mapping.getParamName(), mapping.getKeys(), value);
    
    return setParamValue(mapping.getParamName(), value);
  }
  
  /**
   *  Does a parameter with this name exist? 
   */
  public boolean
  hasParam
  (
    String name
  )
  {
    UtilityParam param = getParam(name);
    if ( param == null )
      return false;
    return true;
  }
  
  /**
   *  Does a parameter with this name and these keys exist? 
   */
  @SuppressWarnings("unchecked")
  public boolean
  hasParam
  (
    String name,
    List<String> keys
  )
  {
    UtilityParam param = getParam(name);
    if ( param == null )
      return false;
    
    if (keys == null || keys.isEmpty())
      return hasParam(name);
    
    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    
    return ((ComplexParamAccess<UtilityParam>) param).hasParam(keys); 
  }
  
  /**
   *  Does a parameter defined by this mapping exist? 
   */
  public boolean
  hasParam
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return hasParam(mapping.getParamName(), mapping.getKeys());
    return hasParam(mapping.getParamName());
  }
  
  /**
   * Does a parameter with this name that implements {@link SimpleParamAccess} exist?
   */
  public boolean
  hasSimpleParam
  (
    String name
  )
  {
    UtilityParam param = getParam(name);
    if ( param == null )
      return false;
    if (param instanceof SimpleParamAccess)
      return true;
    return false;
  }

  /**
   * Does a parameter with this name and these keys that implements {@link SimpleParamAccess}
   * exist?
   */
  @SuppressWarnings("unchecked")
  public boolean
  hasSimpleParam
  (
    String name,
    List<String> keys
  )
  {
    UtilityParam param = getParam(name);
    
    if ( param == null )
      return false;
    
    if (keys == null || keys.isEmpty())
      return hasSimpleParam(name);

    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    
    return ((ComplexParamAccess<UtilityParam>) param).hasSimpleParam(keys); 
  }
  
  /**
   * Does a parameter defined by this mapping that implements {@link SimpleParamAccess} exist?
   */
  public boolean
  hasSimpleParam
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return hasSimpleParam(mapping.getParamName(), mapping.getKeys());
    return hasSimpleParam(mapping.getParamName());
  }
  
  /**
   * Does a parameter with this name that implements {@link SimpleParamFromString} exist?
   */
  public boolean
  canSetSimpleParamFromString
  (
    String name
  )
  {
    UtilityParam param = getParam(name);
    if ( param == null )
      return false;
    if (param instanceof SimpleParamFromString)
      return true;
    return false;
  }
  
  /**
   * Does a parameter with this name and these keys that implements
   * {@link SimpleParamFromString} exist?
   */
  @SuppressWarnings("unchecked")
  public boolean
  canSetSimpleParamFromString
  (
    String name,
    List<String> keys
  )
  {
    UtilityParam param = getParam(name);
    
    if ( param == null )
      return false;
    
    if (keys == null || keys.isEmpty())
      return hasSimpleParam(name);

    if (! (param instanceof ComplexParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "ComplexParamAccess.");
    
    return ((ComplexParamAccess<UtilityParam>) param).canSetSimpleParamFromString(keys); 
  }
  
  /**
   * Does a parameter defined by this mapping that implements {@link SimpleParamFromString}
   * exist?
   */
  public boolean
  canSetSimpleParamFromString
  (
    ParamMapping mapping
  )
  {
    if (mapping.hasKeys())
      return canSetSimpleParamFromString(mapping.getParamName(), mapping.getKeys());
    return canSetSimpleParamFromString(mapping.getParamName());
  }
  
  @SuppressWarnings("unchecked")
  /**
   * Add all the parameter names and values from this Utility to a MultiMap.
   * <p>
   * The first entry in the MultiMap will be the full prefixed name of the utility, which will 
   * be followed by all the keys representing the parameters, with the current parameter value
   * being stored as the value on the last parameter map node. This data structure is what 
   * would be need to be feed to a BuilderInformation file if a builder was going to be rerun 
   * with the current parameters.
   * <p> 
   * This map will not contain the values of constant parameters or parameters which are 
   * mapped by a parent builder.
   */
  protected final MultiMap<String, String>
  getParamValues
  (
    MultiMap<String, String> toReturn
  )
    throws PipelineException
  {
    LinkedList<String> builderName = new LinkedList<String>();
    if (pPrefixName == null)
      builderName.add(pName);
    else
      builderName.add(pPrefixName.toString());
    
    for (UtilityParam param : getParams()) {
      LinkedList<String> pKeys = new LinkedList<String>(builderName);
      pKeys.add(param.getName());
      if (param instanceof SimpleParam) {
        if (!(param instanceof ConstantStringUtilityParam)) {
          LinkedList<String> mapping = new LinkedList<String>(pKeys);
          mapping.removeFirst();
          ParamMapping pmapping = new ParamMapping(mapping.removeFirst(), mapping);
          
          if (!pParamMapping.keySet().contains(pmapping)) {
            Comparable value = ((SimpleParam) param).getValue();
            String stringValue = null;
            if (value != null)
              stringValue = value.toString();
            toReturn.putValue(pKeys, stringValue, true);
          }
        }
      }
      else if (param instanceof ComplexParam) {
        getParamValuesHelper((ComplexParam) param, pKeys, toReturn);
      }
      else
        throw new PipelineException
          ("Cannot determine the parameter type of the parameter (" + param.getName()  +") " +
           "in builder (" + builderName + ")");
    }
    return toReturn;
  }
  
  @SuppressWarnings("unchecked")
  private void 
  getParamValuesHelper
  (
    ComplexParam param,
    LinkedList<String> keys,
    MultiMap<String, String> toReturn
  )
    throws PipelineException
  {
    for (Object child : param.getParams().values() ) {
      LinkedList<String> pKeys = new LinkedList<String>(keys);
      if (child instanceof SimpleParam) {
        if (!(child instanceof ConstantStringUtilityParam)) {
          LinkedList<String> mapping = new LinkedList<String>(pKeys);
          mapping.removeFirst();
          ParamMapping pmapping = new ParamMapping(mapping.removeFirst(), mapping);

          if (!pParamMapping.keySet().contains(pmapping)) {

            pKeys.add(((SimpleParam) child).getName());
            Comparable value = ((SimpleParam) child).getValue();
            String stringValue = null;
            if (value != null)
              stringValue = value.toString();
            toReturn.putValue(pKeys, stringValue, true);
          }
        }
      }
      else if (child instanceof ComplexParam) {
        getParamValuesHelper((ComplexParam) child, pKeys, toReturn);
      }
      else
        throw new PipelineException
          ("Cannot determine the parameter type of the parameter (" + param.getName()  +") " +
           "in builder (" + keys + ")");
    }
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R    L A Y O U T                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the hierarchical grouping of parameters which determine the layout of UI components
   * in different passes.
   * <P>
   * 
   * The given layouts must contain an entry for all parameters defined for the action exactly
   * once in all the passes. A collapsable drawer component will be created for each layout
   * group which contains a field for each parameter entry in the order specified by the
   * group. All <CODE>null</CODE> entries will cause additional space to be added between
   * the UI fields. Each layout subgroup will be represented by its own drawer nested within
   * the drawer for the parent layout group.
   * <P>
   * 
   * This method should be called by subclasses in their constructor after building the
   * appropriate {@link PassLayoutGroup}.
   */
  protected void
  setLayout
  (
    PassLayoutGroup layout
  ) 
  {
    TreeSet<String> names = new TreeSet<String>();
    for(AdvancedLayoutGroup advanced : layout.getAllLayouts().values()) {
      collectLayoutNames(advanced, names);
    }
    
    for(String name : names) {
      if(!pParams.containsKey(name))
	throw new IllegalArgumentException
	  ("The entry (" + name + ") specified by the builder parameter layout group " + 
	   "does not match any single valued parameter defined for this Builder!");
    }

    for(String name : pParams.keySet()) {
      if(!names.contains(name))
	throw new IllegalArgumentException
	  ("The single valued parameter (" + name + ") defined by this Builder was not " + 
	   "specified by any the parameter layout group!");
    }
    pLayout = layout;
    pLocked = true;
  }
  
  /**
   * Returns a name list for all the parameters that are contained in a particular
   * pass of the layout.
   * 
   * @param pass Which pass the name list is generated for.
   */
  public TreeSet<String> 
  getPassParamNames
  (
    int pass 
  )
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    collectLayoutNames(getLayout().getPassLayout(pass), toReturn);
    return toReturn;
  }
  
  /**
   *  Returns the layout pass that a particular parameter is contained in. 
   */
  public final int
  getParameterPass
  (
    ParamMapping param
  )
  {
    int toReturn = 1;
    PassLayoutGroup layout = getLayout();
    int num = layout.getNumberOfPasses();
    for (int i = 1; i <= num; i++) {
      TreeSet<String> params = new TreeSet<String>();
      collectLayoutNames(layout.getPassLayout(i), params);
      if (params.contains(param.getParamName()))
	toReturn = i;
    }
    return toReturn;
  }
  
  /**
   * Recursively search the parameter groups to collect the parameter names and verify
   * that no parameter is specified more than once.
   */ 
  private void 
  collectLayoutNames
  (
   AdvancedLayoutGroup group, 
   TreeSet<String> names
  ) 
  {
    for (Integer column : group.getAllColumns()) {
      for(String name : group.getEntries(column)) {
	if(name != null) {
	  if(names.contains(name)) 
	    throw new IllegalArgumentException
    	      ("The single valued parameter (" + name + ") was specified more than once " +
  	       "in the given parameter group!");
	  names.add(name);
	}
      }
      for(LayoutGroup sgroup : group.getSubGroups(column)) 
	      collectLayoutNames(sgroup, names);
    }
  }

  /**
   * Get the grouping of parameters used to layout components which represent 
   * the parameters in the user interface. <P> 
   * 
   * If no parameter group has been previously specified, a group will 
   * be created which contains all parameters in alphabetical order, in a single pass.
   */ 
  public PassLayoutGroup
  getLayout()
  {
    if(pLayout == null) {
      AdvancedLayoutGroup layout = 
	new AdvancedLayoutGroup("Pass 1", "The first pass of the params.", "Column 1", true);
      for(String name : pParams.keySet()) 
	layout.addEntry(1, name);
      pLayout = 
	new PassLayoutGroup("BuilderParams", "The BuilderParams", layout.getName(), layout );
    }
    return pLayout; 
  }
  
  /**
   * Get the grouping of parameters used to layout components which represent 
   * the parameters in the user interface. <P> 
   * 
   */ 
  protected PassLayoutGroup
  getActualLayout()
  {
    return pLayout; 
  }
  
  /**
   * Gets the layout for a particular pass.
   * 
   * @param pass
   * 	The number of the pass.
   */
  public AdvancedLayoutGroup
  getPassLayout
  (
    int pass
  )
  {
    return getLayout().getPassLayout(pass);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Defines a mapping between a parameter in this Utility and a parameter in its parent.
   * <p>
   * This setting has no effect on the utility level.  It is up to implementing classes to 
   * decide how to take advantage of this functionality.
   * 
   * @see BaseBuilder#addMappedParam(String, ParamMapping, ParamMapping)
   */
  protected final void
  addParamMapping
  (
    ParamMapping subParam,
    ParamMapping masterParam
  )
  {
    pParamMapping.put(subParam, masterParam);
  }

  /**
   * Gets all the mapped parameters and the parameters that drive them.
   */
  public final SortedMap<ParamMapping, ParamMapping> 
  getMappedParams()
  {
    return Collections.unmodifiableSortedMap(pParamMapping);
  }
  
  /**
   * Gets the names of all the mapped parameters.
   */
  public final Set<ParamMapping> 
  getMappedParamNames()
  {
    return Collections.unmodifiableSet(pParamMapping.keySet());
  }
  
  /**
   * Returns the prefix name for this builder, which includes the full path to this builder as
   * a '-' separate list.
   * 
   * If this method is called on something with Builder Parameters which is used as a
   * Sub-Builder, then it should only be used after
   * {@link BaseBuilder#addSubBuilder(BaseUtil)} is called. The addSubBuilder method
   * is responsible for setting the value returned by this function correctly. If this method
   * is called before the value is correctly set, it will just return the name of the builder.
   * 
   */
  public PrefixedName 
  getPrefixedName()
  {
    if (pPrefixName == null)
      return new PrefixedName(getName());
    return pPrefixName;
  }
  
  public void
  setPrefixedName
  (
    PrefixedName namedPrefix
  )
  {
    pPrefixName = new PrefixedName(namedPrefix);
  }
  
  /**
   * Returns the current pass of the utility.
   * <p>
   * Utilities are responsible for keep track of their own passes, but this method should
   * always return the current pass.
   */
  public abstract int
  getCurrentPass();
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R   L O O K U P                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the selected index of the single valued Enum parameter with the given name.<P> 
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The index value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   * @throws ClassCastException
   *   If the parameter is not an EnumParameter.
   */ 
  public int
  getEnumParamIndex
  (
   ParamMapping mapping
  ) 
    throws PipelineException
  {
    EnumUtilityParam param = (EnumUtilityParam) getParam(mapping);
    if(param == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") does not exist!"); 
      
    return param.getIndex();
  }

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Boolean parameter with the given name.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public boolean
  getBooleanParamValue
  (
    ParamMapping mapping
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getParamValue(mapping);  
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") was not set!"); 

    return value;
  }  

  /** 
   * Get the value of the single valued Boolean parameter with the given name.<P> 
   * 
   * If <CODE>null</CODE> value is treated as <CODE>false</CODE>.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   */ 
  public boolean
  getOptionalBooleanParamValue
  (
    ParamMapping mapping
  ) 
  {
    Boolean value = (Boolean) getParamValue(mapping); 
    return ((value != null) && value);
  } 
  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Long parameter with the given name.<P> 
   * 
   * This method can be used to retrieve ByteSizeUtilityParam values.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public long
  getLongParamValue
  (
    ParamMapping mapping
  ) 
    throws PipelineException
  {
    return getLongParamValue(mapping, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Long parameter with 
   * the given name. <P> 
   * 
   * This method can also be used to retrieve ByteSizeActionParam values.
   * 
   * @param mapping  
   *   The name and keys of the parameter.  
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public long
  getLongParamValue
  (
    ParamMapping mapping,
    Range<Long> range
  ) 
    throws PipelineException
  {
    Long value = (Long) getParamValue(mapping); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") was not set!"); 

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Integer parameter with the given name.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public int
  getIntegerParamValue
  (
    ParamMapping mapping
  ) 
    throws PipelineException
  {
    return getIntegerParamValue(mapping, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Integer parameter with 
   * the given name. <P> 
   * 
   * @param mapping  
   *   The name and keys of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */  
  public int
  getIntegerParamValue
  (
    ParamMapping mapping,
    Range<Integer> range
  ) 
    throws PipelineException
  {
    Integer value = (Integer) getParamValue(mapping); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") was not set!"); 

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Double parameter with the given name.
   * 
   * @param mapping
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public double
  getDoubleParamValue
  (
    ParamMapping mapping
  ) 
    throws PipelineException
  {
    return getDoubleParamValue(mapping, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Double parameter with 
   * the given name. <P> 
   * 
   * @param mapping  
   *   The name and keys of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists, 
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public double
  getDoubleParamValue
  (
    ParamMapping mapping,
    Range<Double> range
  ) 
    throws PipelineException
  {
    Double value = (Double) getParamValue(mapping); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + mapping + ") was not set!"); 

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + mapping + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued String parameter with the given name.
   * 
   * @param mapping  
   *   The name and keys of the parameter. 
   *   
   * @param allowsNull
   *   Whether this parameter can have a null or empty value.
   *   
   * @return 
   *   The action parameter value or (optionally)
   *   <CODE>null</CODE> if the value is null or the empty string. 
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or (optionally)
   *   if the value is null or empty.
   */ 
  public final String
  getStringParamValue
  (
    ParamMapping mapping, 
    boolean allowsNull
  ) 
    throws PipelineException
  { 
    String value = (String) getParamValue(mapping); 
    if((value == null) || (value.length() == 0)) {
      if (!allowsNull)
        throw new PipelineException
          ("Cannot have an empty String value for parameter (" + mapping + ")");
      value = null;
    }
    return value;    
  }
  
  /** 
   * Get the value of the single valued String parameter with the given name.
   * 
   * @param mapping  
   *   The name and keys of the parameter. 
   *
   * @return 
   *   The parameter value or <CODE>null</CODE> if the value is null or the empty string. 
   * 
   */ 
  public String
  getStringParamValue
  (
    ParamMapping mapping   
  ) 
    throws PipelineException
  { 
    return getStringParamValue(mapping, true);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @deprecated 
   *   The static method {@link Exceptions#getFullMessage} should be used instead.
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  @Deprecated
  protected String 
  getFullMessage
  (
   Throwable ex
  ) 
  {
    return Exceptions.getFullMessage(ex); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aUtilContext = "UtilContext";
  
  public final static ParamMapping aUtilContextAuthorParam = 
    new ParamMapping(aUtilContext, UtilContextUtilityParam.aAuthor);

  public final static ParamMapping aUtilContextViewParam = 
    new ParamMapping(aUtilContext, UtilContextUtilityParam.aView);
  
  public final static ParamMapping aUtilContextToolsetParam = 
    new ParamMapping(aUtilContext, UtilContextUtilityParam.aToolset);


  
  private static final long serialVersionUID = -7450635393202917345L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The instance of the Master Manager that is being used to perform all the
   * Pipeline operations in this stage.
   */
  protected final MasterMgrClient pClient;
  
  /**
   * The instance of the Queue Manager that is being used to perform all the
   * Pipeline operations in this stage.
   */
  protected final QueueMgrClient pQueue;

  /**
   * The instance of the Plugin Manager which is used to perform all plugin related stage
   * operations.
   */
  protected final PluginMgrClient pPlug;
  
  /**
   * The context which holds the user, view, and toolset that the stage is operating on.
   */
  protected UtilContext pContext;
  
  /**
   * The table of Builder parameters.
   */
  private  TreeMap<String, UtilityParam> pParams;
  
  /**
   * Specifies the grouping of parameters used to layout components which 
   * represent the parameters in the user interface, broken down by pass. 
   */ 
  private PassLayoutGroup pLayout;
  
  /**
   *  Contains a mapping of the Sub-Builder Parameter name to the parent Parameter name.
   */
  private TreeMap<ParamMapping, ParamMapping> pParamMapping;
  
  /**
   * Is the class that inherits from this class allowed to have children?
   * <p>
   * This is an important consideration, since having child {@link HasBuilderParams} means
   * that those classes have to be able to manage those children.  It also means that 
   * calculations of things like the maximum number of passes necessary to run or the collection
   * of layouts that much more difficult.
   * <p>
   * This value should be set by Abstract classes that inherit from this class and not by
   * the actual implementing classes that use the Abstract classes.  Those Abstract classes need
   * to define methods to deal with the complexities discussed above. 
   */
  //private final boolean pAllowsChildren;
  
  /**
   * The prefixed name of the utility
   */
  private PrefixedName pPrefixName = null;
  
  /**
   * Instance of the log manager for builder logging purposes.
   */
  protected final LogMgr pLog;
  
  /**
   * Boolean which signals that no more params or other volatile parts can be added to the 
   * Util.
   * <p>
   * Locking is triggered by calling setLayout.
   */
  private boolean pLocked;
}