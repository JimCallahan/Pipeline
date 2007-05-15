package us.temerity.pipeline.builder.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseUtil;
import us.temerity.pipeline.builder.UtilContext;

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
  extends BaseUtil
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
   * @param context
   *        The context the stage operates in.
   */
  protected 
  BaseStage
  (
    String name,
    String desc,
    UtilContext context
  ) 
  {
    super(name, desc, context);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   A C C E S S                                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Clears all added nodes that are currently being kept track of.
   */
  public static void 
  initializeAddedNodes()
  {
    sAddedNodes = new TreeSet<String>();
    sAddedNodesUserMap = new TreeMap<String, String>();
    sAddedNodesViewMap = new TreeMap<String, String>();
  }

  /**
   * Gets a list that contains the names of all the nodes that have been built by stages.
   * 
   * @return The {@link TreeSet} containing the node names.
   * @see #getAddedNodesUserMap()
   * @see #getAddedNodesViewMap()
   */
  public static TreeSet<String> 
  getAddedNodes()
  {
    return sAddedNodes;
  }

  /**
   * Gets a mapping of each added node to the user in whose working area the node was
   * added.
   * 
   * @return The {@link TreeMap} containing the user names.
   * @see #getAddedNodes()
   * @see #getAddedNodesViewMap()
   */
  public static TreeMap<String, String> 
  getAddedNodesUserMap()
  {
    return sAddedNodesUserMap;
  }

  /**
   * Gets a mapping of each added node to the working area where the node was added.
   * 
   * @return The {@link TreeMap} containing the working area names.
   * @see #getAddedNodes()
   * @see #getAddedNodesUserMap()
   */
  public static TreeMap<String, String> 
  getAddedNodesViewMap()
  {
    return sAddedNodesViewMap;
  }

  /**
   * Attempts to release all the nodes that have been added so far.
   * <P>
   * This method is intended to be used to clean-up a builder that did not succesfully
   * complete. It uses the added node information to go through and attempt to release
   * each node. To accomplish this, the method uses the class's static instance of
   * {@link MasterMgrClient}. If an exception is encountered while releasing a node, it
   * is caught and the method continues to execute. Once the method has attempted to
   * remove all the nodes in the added nodes list, then a {@link PipelineException} will
   * be thrown (if an error had occured during execution) that contains the exception
   * messages for all the exceptions that had been thrown.
   * 
   * @throws PipelineException
   */
  public static void 
  cleanUpAddedNodes() 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder();
    boolean exception = false;
    for(String s : BaseStage.sAddedNodes) {
      String user = sAddedNodesUserMap.get(s);
      String view = sAddedNodesViewMap.get(s);
      NodeID id = new NodeID(user, view, s);
      try {
	sClient.release(id, true);
      }
      catch(PipelineException ex) {
	exception = true;
	buf.append(ex.getMessage() + "\n");
      }
    }
    if(exception)
      throw new PipelineException(buf.toString());
  }
  
  public static void
  setDefaultSelectionKeys
  (
    Set<String> keys
  )
  {
    if (keys == null)
      sDefaultSelectionKeys = new TreeSet<String>();
    else
      sDefaultSelectionKeys = new TreeSet<String>(keys);
  }
  
  public static void
  setDefaultLicenseKeys
  (
    Set<String> keys
  )
  {
    if (keys == null)
      sDefaultLicenseKeys = new TreeSet<String>();
    else
      sDefaultLicenseKeys = new TreeSet<String>(keys);
  }
  
  public static TreeSet<String> 
  getDefaultSelectionKeys()
  {
    return sDefaultSelectionKeys;
  }
  
  public static TreeSet<String> 
  getDefaultLicenseKeys()
  {
    return sDefaultLicenseKeys;
  }
  
  public static void 
  useDefaultSelectionKeys
  (
    boolean value
  )
  {
    sUseDefaultSelectionKeys = value;
  }
  
  public static boolean 
  useDefaultSelectionKeys()
  {
   return sUseDefaultSelectionKeys; 
  }

  public static void 
  useDefaultLicenseKeys
  (
    boolean value
  )
  {
    sUseDefaultLicenseKeys = value;
  }
  
  public static boolean 
  useDefaultLicenseKeys()
  {
   return sUseDefaultLicenseKeys; 
  }

  /**
   * Adds a node name to the list of nodes created duing the session.
   * <P>
   * The method will return a boolean based on whether the node already existed in the
   * current list. A return value of <code>false</code> indicates that the name was not
   * added to the list since it already existed then. A return value of <code>true</code>
   * indicates that the add was succesful. A PipelineException is thrown if the
   * <code>initializeAddedNodes</code> method was not called before calling this method.
   * 
   * @param name
   * @return
   * @throws PipelineException
   * @see #initializeAddedNodes()
   */
  protected final boolean 
  addNode
  (
    String name
  ) 
    throws PipelineException
  {
    if(sAddedNodes == null)
      throw new PipelineException(
      "It appears that initializeAddedNodes() was never called, leading to an error");
    if(sAddedNodes.contains(name))
      return false;
    sAddedNodes.add(name);
    sAddedNodesUserMap.put(name, getAuthor());
    sAddedNodesViewMap.put(name, getView());
    return true;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L   H E L P E R S                                                       */
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
      sClient.link(getAuthor(), getView(), pRegisteredNodeName, link.getName(), link
	.getPolicy(), link.getRelationship(), link.getFrameOffset());
    }
    return true;
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
  
  protected final void
  setKeys() 
    throws PipelineException
  {
    JobReqs reqs = pRegisteredNodeMod.getJobRequirements();
    if (pSelectionKeys != null) {
      reqs.addSelectionKeys(pSelectionKeys);
    }
    if (sUseDefaultSelectionKeys) {
      reqs.addSelectionKeys(sDefaultSelectionKeys);
    }
    if (pLicenseKeys != null) {
      reqs.addLicenseKeys(pSelectionKeys);
    }
    if (sUseDefaultLicenseKeys) {
      reqs.addLicenseKeys(sDefaultLicenseKeys);
    }
    pRegisteredNodeMod.setJobRequirements(reqs);
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
   * @throws PipelineException
   */
  @SuppressWarnings("unchecked")
  public void 
  addSingleParam
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
   * @throws PipelineException
   */
  @SuppressWarnings("unchecked")
  public void 
  addSourceParam
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
   * @throws PipelineException
   */
  @SuppressWarnings("unchecked")
  public void 
  addSecondarySourceParam
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

  public void addSelectionKeys
  (
    TreeSet<String> selectionKeys
  )
  {
    if (pSelectionKeys == null)
      pSelectionKeys = new TreeSet<String>();
    pSelectionKeys.addAll(selectionKeys);
  }
  
  public void 
  setSelectionKeys
  (
    TreeSet<String> selectionKeys
  )
  {
    pSelectionKeys = selectionKeys;
  }

  public void addLicenseKeys
  (
    TreeSet<String> licenseKeys
  )
  {
    if (pLicenseKeys == null)
      pLicenseKeys = new TreeSet<String>();
    pLicenseKeys.addAll(licenseKeys);
  }

  public void setLicenseKeys
  (
    TreeSet<String> licenseKeys
  )
  {
    pLicenseKeys = licenseKeys;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the node that is to be registered by the stage.
   */
  protected String pRegisteredNodeName = null;

  /**
   * The suffix of the node that is going to be registered.
   */
  protected String pSuffix = null;

  /**
   * The Editor for the node that is going to be registered
   */
  protected BaseEditor pEditor = null;

  /**
   * The Action for the node that is going to be registered.
   * <p>
   * This also stores all the paramter (single, source, and secondary source) information.
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


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A list containing all the nodes that have been added by stages. All stages are
   * responsible for ensuring that all created nodes end up in this data structure.
   */
  private static TreeSet<String> sAddedNodes;

  /**
   * A mapping of added nodes to the user who added them.
   */
  private static TreeMap<String, String> sAddedNodesUserMap;

  /**
   * A mapping of added nodes to the working area they were added in.
   */
  private static TreeMap<String, String> sAddedNodesViewMap;
  
  private static TreeSet<String> sDefaultSelectionKeys = new TreeSet<String>();
  
  private static TreeSet<String> sDefaultLicenseKeys = new TreeSet<String>();
  
  private static boolean sUseDefaultSelectionKeys;
  
  private static boolean sUseDefaultLicenseKeys;

}
