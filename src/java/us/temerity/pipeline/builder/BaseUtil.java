// $Id: BaseUtil.java,v 1.16 2007/08/19 00:57:33 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.math.Range;

/**
 * The abstract class that provides the basis for all utility classes in Pipeline.
 * <P>
 * This class contains all the information and helper methods that will be used by
 * utility classes.
 */
public abstract
class BaseUtil
  extends BasePlugin
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
    VersionID vid,
    String vendor,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    UtilContext context
  ) 
    throws PipelineException 
  {
    super(name, vid, vendor, desc);
    pClient = mclient;
    pQueue = qclient;
    pPlug = PluginMgrClient.getInstance();
    pContext = context;
    
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
       context,
       mclient);
      setContext(context);
      addParam(param);
    }
  }

  /**
   * Default constructor for BaseUtil. <P>
   * The {@link UtilContext} for this utility will need to be initialized using
   * {@link #setContext(UtilContext)} before any of the methods in this 
   * class are used. 
   * 
   * @param name
   *        The name of the utility.
   * @param desc
   *        A description of what the utility should do.
   */
  protected 
  BaseUtil
  (
    String name,
    VersionID vid,
    String vendor,
    String desc,    
    MasterMgrClient mclient,
    QueueMgrClient qclient
  ) 
    throws PipelineException 
  {
    this(name, vid, vendor, desc, mclient, qclient, null);
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
   * 
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
   * Sets the {@link UtilContext} that this utility will operate in.
   * <P>
   * This should not be used to change the context in the middle of running a tool.
   * Generally, this should be set in the first validatePhase of the first Information Pass in
   * a builder.  That will guarantee that all future decisions are made in the right default 
   * space.  If a change is needed to the working environment in the middle of running a builder,
   * it would be best to just create a new {@link UtilContext} and pass that into the necessary
   * stages, rather than calling this method, as there will be no impact on the rest of the
   * Builder. 
   * 
   * @param context
   * 	The {@link UtilContext} for the utility. 
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
    NodeMod nodeMod = pClient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(false);
    pClient.modifyProperties(getAuthor(), getView(), nodeMod);
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
    NodeMod nodeMod = pClient.getWorkingVersion(nodeID);
    nodeMod.setActionEnabled(true);
    pClient.modifyProperties(getAuthor(), getView(), nodeMod);
  }

  /**
   * Removes a node's Action.
   * <p>
   * Takes the name of a node and removes any Action that the node
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
    NodeMod nodeMod = pClient.getWorkingVersion(nodeID);
    nodeMod.setAction(null);
    pClient.modifyProperties(getAuthor(), getView(), nodeMod);
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
      pClient.release(getAuthor(), getView(), s, true);
    }
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
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
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
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
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
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
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
    NodeTreeComp treeComps = pClient.updatePaths(getAuthor(), getView(), comps);
    ArrayList<String> toReturn = new ArrayList<String>();
    for(String s : treeComps.keySet()) {
      findNodes(treeComps.get(s), toReturn, "/");
    }
    return toReturn;
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
      mod = pClient.getWorkingVersion(getAuthor(), getView(), name);
    }
    catch(PipelineException ex)
    {}

    TreeSet<VersionID> versions = null;
    try {
      versions = pClient.getCheckedInVersionIDs(name);
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
        pClient.checkOut(getAuthor(), getView(), name, latestID, mode, method);
      }
    }
    else {
      pClient.checkOut(getAuthor(), getView(), name, latestID, mode, method);
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
    pClient.checkOut(getAuthor(), getView(), name, null, mode, method);
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
    pClient.lock(getAuthor(), getView(), name, null);
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
    if (nodes != null) {
      for(String node : nodes) {
	pClient.checkIn(getAuthor(), getView(), node, message, level);
      }
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

  public static ArrayList<SelectionKey>
  getSelectionKeys
  (
    QueueMgrClient client
  )
    throws PipelineException
  {
    return client.getSelectionKeys();
  }
  
  public static ArrayList<LicenseKey>
  getLicenseKeys
  (
    QueueMgrClient client
  )
    throws PipelineException
  {
    return client.getLicenseKeys();
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
  /* G L O B A L   P A R A M E T E R S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the Class have any parameters?
   */
  public boolean 
  hasParams()
  {
      return ( !pParams.isEmpty() );
  }

  /**
   * Add a parameter to this Class.
   * <P>
   * This method is used by subclasses in their constructors to initialize the set of
   * global parameters that they support.
   * 
   * @param param
   *        The parameter to add.
   */
  protected void 
  addParam
  (
    UtilityParam param
  )
  {
    if ( pParams.containsKey(param.getName()) )
      throw new IllegalArgumentException
        ("A parameter named (" + param.getName() + ") already exists!");

    pParams.put(param.getName(), param);
    
    pLog.log(Kind.Bld, Level.Finest, 
      "Adding a parameter named (" + param.getName() + ") to a Builder " +
      "identified by (" + getName() + ")");
  }
  
  /**
   * Replaces an existing parameter with a new parameter.
   * <P>
   * The conditions are as follows. The parameter must have the same name as the parameter it
   * is replacing. If you attempt to replace a parameter that does not exist, an exception is
   * thrown. If you attempt to replace a parameter from a layout with is not after the current
   * pass an exception will be thrown. Basically, only parameters which have not had value
   * inputted into them can be replaced.
   * 
   * @param param
   *        The parameter to add.
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
        ("The attempt to replace parameter ("+ param.getName() +") in pass (" + paramPass + ") " +
         "failed because the current pass is (" + currentPass + ").");

    pParams.put(param.getName(), param);
    
    pLog.log(Kind.Bld, Level.Finest, 
      "Replaced the parameter named (" + param.getName() + ") in Builder " +
      "identified by (" + getName() + ")");
  }

  /**
   * Get the value of the parameter with the given name.
   * 
   * @param name
   *        The name of the parameter.
   * @return The parameter value.
   * @throws IllegalArgumentException if no parameter with the given name exists or if the
   * named parameter does not implement {@link SimpleParamAccess}.
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
   * @throws IllegalArgumentException if no parameter with the given name exists or if the
   * named parameter does not implement {@link SimpleParamAccess}.
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
   *        The name of the parameter.
   * @return The parameter or <CODE>null</CODE> if no parameter with the given name
   *         exists.
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
  
  @SuppressWarnings("unchecked")
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

  public Collection<UtilityParam> 
  getParams()
  {
    return Collections.unmodifiableCollection(pParams.values());
  }
  
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
   *        The name of the parameter.
   * @param value
   *        The new value of the parameter.
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
        ("No parameter named (" + param.getName() + ") exists for this extension!");
    if (! (param instanceof SimpleParamAccess))
      throw new IllegalArgumentException
        ("The parameter (" + name + ") in builder (" + getName() + ") does not implement " +
         "SimpleParamAccess.");

    ((SimpleParamAccess) param).setValue(value);
    return false;
  }

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
  
  @SuppressWarnings("unchecked")
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
  
  @SuppressWarnings("unchecked")
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
  
  @SuppressWarnings("unchecked")
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R    L A Y O U T                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the hierarchical grouping of parameters which determine the layout of 
   * UI components in different passes. <P> 
   * 
   * The given layouts must contain an entry for all parameters 
   * defined for the action exactly once in all the passes.  A collapsible drawer component 
   * will be created for each layout group which contains a field for each parameter
   * entry in the order specified by the group.  All <CODE>null</CODE> entries will cause 
   * additional space to be added between the UI fields. Each layout subgroup will be represented 
   * by its own drawer nested within the drawer for the parent layout group. <P> 
   * 
   * This method should be called by subclasses in their constructor after building the appropriate 
   * {@link PassLayoutGroup}.
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
   * Gets all the mapped parameters and the parameters that drive them.
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aUtilContext = "UtilContext";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The static instance of the Master Manager that is being used to perform all the
   * Pipeline operations in this stage.
   */
  protected final MasterMgrClient pClient;
  
  /**
   * The static instance of the Queue Manager that is being used to perform all the
   * Pipeline operations in this stage.
   */
  protected final QueueMgrClient pQueue;

  /**
   * Static instance of the Plugin Manager which is used to perform all plugin related stage
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
  protected TreeMap<String, UtilityParam> pParams;
  
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
   * Is the class that inherets from this class allowed to have children?
   * <p>
   * This is an important consideration, since having child {@link HasBuilderParams} means
   * that those classes have to be able to maange those children.  It also means that 
   * calculations of things like the maximum number of passes necessary to run or the collection
   * of layouts that much more difficult.
   * <p>
   * This value should be set by Abstract classes that inherit from this class and not by
   * the actual implementing classes that use the Abstract classes.  Those Abstract classes need
   * to define methods to deal with the complexities discussed above. 
   */
  //private final boolean pAllowsChildren;
  
  /**
   * 
   */
  private PrefixedName pPrefixName = null;
  
  /**
   * Instance of the log manager for builder logging purposes.
   */
  protected final LogMgr pLog = LogMgr.getInstance();

  
  
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
  public static 
  enum NodeLocation
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A key into a paramter contained in a Utility.  It consists of a mandatory name, which
   * specifies the parameter name, and an optional list of keys which specify the path
   * to descend if the parameter is in quesiton is a {@link ComplexUtilityParam}.
   * <P>
   * If Complex Parameters are extended to other parts of the Pipeline API, this class will
   * most likely be converted into a standalone class. 
   */
  public static
  class ParamMapping
    implements Comparable<ParamMapping>
  {
    public
    ParamMapping
    (
      ParamMapping mapping
    )
    {
      this(mapping.getParamName(), mapping.getKeys());
    }
    
    public
    ParamMapping
    (
      String paramName
    )
    {
      this(paramName, null);
    }
    
    public
    ParamMapping
    (
      String paramName,
      List<String> keys
    )
    {
      if (paramName == null)
	throw new IllegalArgumentException("Cannot have a null parameter name");
      pParamName = paramName;
      if (keys == null)
	pKeys = null;
      else if (keys.isEmpty())
	pKeys = null;
      else
	pKeys = new LinkedList<String>(keys);
    }
    
    public String
    getParamName()
    {
      return pParamName;
    }
    
    public boolean
    hasKeys()
    {
      if (pKeys == null)
	return false;
      return true;
    }
    
    public List<String>
    getKeys()
    {
      if (pKeys == null)
	return null;
      return Collections.unmodifiableList(pKeys);
    }
    
    public void
    addKey
    (
      String key
    )
    {
      if (pKeys == null)
	pKeys = ComplexParam.listFromObject(key);
      else
	pKeys.add(key);
    }

    public int 
    compareTo
    (
      ParamMapping that
    )
    {
      int compare = this.pParamName.compareTo(that.pParamName);
      if (compare != 0)
	return compare;
      if (this.pKeys == null) {
	if (that.pKeys == null)
	  return 0;
	return -1;
      }
      if (that.pKeys == null)
	return 1;
      int thisSize = this.pKeys.size();
      int thatSize = that.pKeys.size();
      if (thisSize > thatSize)
	return 1;
      else if (thatSize < thisSize)
	return -1;
      for (int i = 0; i < thisSize; i++) {
	String thisKey = this.pKeys.get(i);
	String thatKey = that.pKeys.get(i);
	compare = thisKey.compareTo(thatKey);
	if (compare != 0)
	  return compare;
      }
      return 0;
    }
    
    @Override
    public boolean 
    equals
    (
      Object obj
    )
    {
      if (!(obj instanceof ParamMapping ) )
	return false;
      ParamMapping mapping = (ParamMapping) obj;
      int compare = this.compareTo(mapping);
      if (compare == 0)
	return true;
      return false;
    }

    @Override
    public String
    toString()
    {
      return "Param Name (" + pParamName + ") with Keys: " + pKeys;
    }
    
    private String pParamName;
    private LinkedList<String> pKeys;
  }
  
  public static 
  class PrefixedName
  {
    public
    PrefixedName
    (
      LinkedList<String> prefixes,
      String name
    )
    {
      if (prefixes == null)
	pPrefixes = new LinkedList<String>();
      else
	pPrefixes = new LinkedList<String>(prefixes);
      if (name != null)
	pPrefixes.add(name);
    }
    
    public PrefixedName
    (
      String name
    )
    {
      pPrefixes = ComplexParam.listFromObject(name);
    }
    
    public PrefixedName
    (
      PrefixedName prefixName,
      String name
    )
    {
      if (prefixName.pPrefixes == null)
	pPrefixes = new LinkedList<String>();
      else
	pPrefixes = new LinkedList<String>(prefixName.pPrefixes);
      if (name != null)
	pPrefixes.add(name);
    }
    
    public PrefixedName
    (
      PrefixedName prefixName
    )
    {
      if (prefixName.pPrefixes == null)
	pPrefixes = new LinkedList<String>();
      else
	pPrefixes = new LinkedList<String>(prefixName.pPrefixes);
    }
    
    @Override
    public String toString()
    {
      StringBuilder toReturn = new StringBuilder();
      for (String each : pPrefixes) {
	if (toReturn.length() > 0)
	  toReturn.append(" - ");
	toReturn.append(each);
      }
      return toReturn.toString();
    }
    
    @Override
    public boolean 
    equals
    (
      Object that
    )
    {
      if (!(that instanceof PrefixedName)) 
	return false;
      PrefixedName that1 = (PrefixedName) that;
      if (that1.toString().equals(this.toString()))
	return true;
      return false;
    }

    private LinkedList<String> pPrefixes;
  }
}
