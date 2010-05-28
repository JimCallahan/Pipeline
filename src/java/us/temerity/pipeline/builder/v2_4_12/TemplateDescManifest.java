package us.temerity.pipeline.builder.v2_4_12;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   D E S C   M A N I F E S T                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A record of the nodes that made up a template which can be combined with a 
 * {@link TemplateParamManifest} to rerun a template.
 */
public 
class TemplateDescManifest
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  TemplateDescManifest()
  {
    super();
  }
  
  /**
   * This constructor takes a single node which is a grouping node for all the root nodes of
   * the template.  <p>
   * 
   * Templates using this constructor must be task networks.
   * 
   * @param startNode
   *   The start node of the template
   *   
   * @param verID
   *   The versionID of the start node.
   *   
   * @throws IllegalArgumentException
   *   If startNode or verID is <code>null</code>.
   */
  public 
  TemplateDescManifest
  (
    String startNode,
    VersionID verID
  )
  {
    pTemplateType = TemplateType.TaskSingle;
    
    if (startNode == null)
      throw new IllegalArgumentException("The startNode parameter cannot be null");
    pStartNode = startNode;
    
    if (verID == null)
      throw new IllegalArgumentException("The verID parameter cannot be null");
    pStartNodeVersion = verID;
  }
  
  /**
   * This constructor takes a map of nodes, which are the root nodes of the template 
   * network and their version numbers. <p>  
   * 
   * Templates using this constructor must be task networks.
   * 
   * @param rootNodes
   *   The root nodes of the template.  This should never be <code>null</code> or empty.
   *   
   * @throws IllegalArgumentException
   *   If rootNodes is <code>null</code> or empty.
   */
  public 
  TemplateDescManifest
  (
    SortedMap<String, VersionID> rootNodes  
  )
  {
    pTemplateType = TemplateType.TaskList;
    if (rootNodes == null)
      throw new IllegalArgumentException("The rootNodes parameter cannot be null");
    if (rootNodes.isEmpty())
      throw new IllegalArgumentException("The rootNodes parameter cannot be an empty set.");
    pRootNodes = new TreeMap<String, VersionID>(rootNodes); 
  }

  /**
   * This constructor takes a map of nodes, which are the root nodes and versions of the 
   * template network and a second set which contains all the nodes in the template.<p>
   * 
   * Templates using this constructor need not be task networks.
   * 
   * @param rootNodes
   *   The root nodes of the template.
   * 
   * @param allNodes
   *   All the nodes in the template.
   */
  public
  TemplateDescManifest
  (
    SortedMap<String, VersionID> rootNodes,
    Set<String> allNodes
  )
  {
    pTemplateType = TemplateType.NonTask;
    
    if (rootNodes == null)
      throw new IllegalArgumentException("The rootNodes parameter cannot be null");
    if (rootNodes.isEmpty())
      throw new IllegalArgumentException("The rootNodes parameter cannot be an empty set.");
    pRootNodes = new TreeMap<String, VersionID>(rootNodes);
    
    if (allNodes == null)
      throw new IllegalArgumentException("The allNodes parameter cannot be null");
    if (allNodes.isEmpty())
      throw new IllegalArgumentException("The allNodes parameter cannot be an empty set.");
    pAllNodes = new TreeSet<String>(allNodes); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the template type.
   */
  public final TemplateType
  getTemplateType()
  {
    return pTemplateType;
  }
  
  /**
   * Get the start node name of the template.
   * 
   * @throws IllegalStateException
   *   If this method is called when the {@link TemplateType} is not TaskSingle.
   */
  public final String
  getStartNode()
  {
    if (pTemplateType != TemplateType.TaskSingle)
      throw new IllegalStateException
        ("The getStartNode method is not valid when the task type is " +
        "(" + pTemplateType + ")");
    return pStartNode;
  }
  
  /**
   * Get the start node version of the template.
   * 
   * @throws IllegalStateException
   *   If this method is called when the {@link TemplateType} is not TaskSingle.
   */
  public final VersionID
  getStartNodeVersion()
  {
    if (pTemplateType != TemplateType.TaskSingle)
      throw new IllegalStateException
        ("The getStartNodeVersion method is not valid when the task type is " +
        "(" + pTemplateType + ")");
    return pStartNodeVersion;
  }

  /**
   * Get the root node names and versions.
   * 
   * @return
   *   An unmodifiable map containing the root nodes as keys and their versions as values.
   *   
   * @throws IllegalStateException
   *   If this method is called when the {@link TemplateType} is not TaskList or NonTask.
   */
  public final SortedMap<String, VersionID>
  getRootNodes()
  {
    if (pTemplateType == TemplateType.TaskSingle)
      throw new IllegalStateException
        ("The getRootNodes method is not valid when the task type is " +
         "(" + pTemplateType + ")");
    
    return Collections.unmodifiableSortedMap(pRootNodes);
  }
  
  /**
   * Get all the template node names.
   * 
   * @return
   *   An unmodifiable set containing the template nodes.
   *   
   * @throws IllegalStateException
   *   If this method is called when the {@link TemplateType} is not NonTask.
   */
  public final Set<String>
  getAllNodes()
  {
    if (pTemplateType != TemplateType.NonTask)
      throw new IllegalStateException
        ("The getAllNodes method is not valid when the task type is " +
         "(" + pTemplateType + ")");
    
    return Collections.unmodifiableSet(pAllNodes);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  G L U E A B L E                                                                       */
  /*----------------------------------------------------------------------------------------*/

  @SuppressWarnings({ "unchecked" })
  @Override
  public void 
  fromGlue
  (
    GlueDecoder decoder
  )
    throws GlueException
  {
    pTemplateType = (TemplateType) decoder.decode(aTemplateType);
    if (pTemplateType == null)
      throw new GlueException("TemplateType cannot be null");
    
    switch (pTemplateType) {
    case TaskSingle:
      {
        Object o = decoder.decode(aStartNode);
        if (o == null)
          throw new GlueException("StartNode cannot be (null)");
        pStartNode = (String) o;
      }
      {
        Object o = decoder.decode(aStartNodeVersion);
        if (o == null)
          throw new GlueException("StartNodeVersion cannot be (null)");
        pStartNodeVersion = (VersionID) o;
      }
      break;
    case NonTask:
      {
        Object o = decoder.decode(aAllNodes);
        if (o == null)
          throw new GlueException("AllNodes cannot be (null)");
        pAllNodes = (TreeSet<String>) o;
      }
      //$FALL-THROUGH$
    case TaskList:
      {
        Object o = decoder.decode(aRootNodes);
        if (o == null)
          throw new GlueException("RootNodes cannot be (null)");
        pRootNodes = (TreeMap<String, VersionID>) o;
      }
      break;
    }
  }
  
  @Override
  public void 
  toGlue
  (
    GlueEncoder encoder
  )
    throws GlueException
  {
    encoder.encode(aTemplateType, pTemplateType);
    switch (pTemplateType) {
    case TaskSingle:
      encoder.encode(aStartNode, pStartNode);
      encoder.encode(aStartNodeVersion, pStartNodeVersion);
      break;
    case NonTask:
      encoder.encode(aAllNodes, pAllNodes);
      //$FALL-THROUGH$
    case TaskList:
      encoder.encode(aRootNodes, pRootNodes);
      break;
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final String aTemplateType     = "TemplateType";
  private static final String aStartNode        = "StartNode";
  private static final String aStartNodeVersion = "StartNodeVersion";
  private static final String aRootNodes        = "RootNodes";
  private static final String aAllNodes         = "AllNodes";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * How the template is defining its members.
   */
  private TemplateType pTemplateType;
  
  private String pStartNode;
  private VersionID pStartNodeVersion;
  private TreeMap <String, VersionID> pRootNodes;
  private TreeSet<String> pAllNodes;
}
