// $Id: TemplateTraversalUtil.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateCollection.v2_4_12;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   T R A V E R S A L   U T I L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Helper functions for writing template builders.
 */
public 
class TemplateTraversalUtil
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor.
   * 
   * @param name
   *   The name of the builder
   *   
   * @param desc
   *   A description of what the builder is.
   *   
   * @param mclient
   *   The instance of the Master Manager that the Builder is going to use.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the Builder is going to use
   * 
   * @param info
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   */
  protected
  TemplateTraversalUtil
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, info, null);
    
    pNodeDatabase = new TreeMap<String, TemplateNode>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   T E M P L A T E    T R A V E R S A L                                                 */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Traverse a non-task template network.
   * 
   * @param rootNodes
   *   The root node of the template. 
   *   
   * @param allNodes
   *   All the nodes in the template.
   */
  protected void
  traverseNetwork
  (
    Set<String> rootNodes,
    TreeSet<String> allNodes
  )
    throws PipelineException
  {
    pTaskNetwork = false;
    pAllNodes = new TreeSet<String>(allNodes);
    pRootNodes = new TreeSet<String>(rootNodes);
    
    TreeMap<String, NodeStatus> stati = 
      pClient.status(getAuthor(), getView(), pRootNodes, new TreeSet<String>(), 
                     DownstreamMode.None);
    for (String node : rootNodes) {
      NodeStatus status = stati.get(node);
      findUpstreamNodes(null, status);
    }
    
    TreeSet<String> missing = new TreeSet<String>();
    for (String node : pAllNodes) {
      if (!pNodeDatabase.containsKey(node)) 
        missing.add(node);
    }
    if (!missing.isEmpty())
      throw new PipelineException
        ("The nodes " + missing + " were listed as being in the template, but were not " +
         "upstream of any of the root nodes.");
  }
  
  /**
   * Traverse a task network.
   * 
   * @param rootNodes
   *   The root task nodes.
   */
  protected void
  traverseTaskNetwork
  (
    Set<String> rootNodes  
  )
    throws PipelineException
  {
    pTaskNetwork = true;
    pRootNodes = new TreeSet<String>(rootNodes);
    
    {
      String firstRoot = pRootNodes.first();
      TreeMap<String, BaseAnnotation> taskAnnots = getTaskAnnotations(firstRoot);
      if (taskAnnots == null || taskAnnots.isEmpty())
        throw new PipelineException
          ("There were no task annotations on the node (" + firstRoot + ").  " +
           "The current template requires task annotations on all the root nodes.");
      String key = new TreeSet<String>(taskAnnots.keySet()).first();
      BaseAnnotation taskA = taskAnnots.get(key);
      pProjectName = lookupProjectName(firstRoot, key, taskA);
      pTaskName = lookupTaskName(firstRoot, key, taskA);
      pTaskType = lookupTaskType(firstRoot, key, taskA);
    }
    
    TreeMap<String, NodeStatus> stati = 
      pClient.status(getAuthor(), getView(), pRootNodes, new TreeSet<String>(), 
                     DownstreamMode.None);
    for (NodeStatus child : stati.values()) {
      String nodeName = child.getName();
      TreeMap<String, BaseAnnotation> taskAnnots = getTaskAnnotations(nodeName);
      if (taskAnnots == null || taskAnnots.isEmpty())
        throw new PipelineException
          ("There were no task annotations on the node (" + nodeName + ").  " +
           "The current template requires task annotations on all the root nodes.");
      for (Entry<String, BaseAnnotation> entry : taskAnnots.entrySet()) {
        if (!doesTaskMatch(nodeName, entry.getKey(), entry.getValue()))
          throw new PipelineException
            ("Root node (" + nodeName  + ") is not a member of the task.");
      }
      findUpstreamNodes(null, child);
    }
  }
  
  /**
   * Traverse a task network.
   * 
   * @param rootNode
   *   A non-task node that acts as a grouping node for the root task nodes.
   */
  protected void
  traverseTaskNetwork
  (
    String rootNode  
  )
    throws PipelineException
  {
    NodeMod mod = getWorkingVersion(rootNode);
    TreeSet<String> nodes = new TreeSet<String>(mod.getSourceNames());
    traverseTaskNetwork(nodes);
  }
  
  private void
  findUpstreamNodes
  (
    String parent,
    NodeStatus status
  )
    throws PipelineException
  {
    String nodeName = status.getName();
    if (pNodeDatabase.containsKey(nodeName)) {
      if (parent != null) {
        pNodeDatabase.get(nodeName).addDownstreamLink(parent);
      }
      return;
    }
    
    NodeMod mod = getWorkingVersion(status);
    
    if (inTemplate(status)) {
      TemplateNode node = new TemplateNode(mod, true);
      if (parent == null)
        node.setRoot(true);
      pNodeDatabase.put(nodeName, node);
      for (NodeStatus child : status.getSources()) {
        findUpstreamNodes(nodeName, child);
      }
    }
    else {
      TemplateNode node = new TemplateNode(mod, false);
      pNodeDatabase.put(nodeName, node);
    }
  }
  
  protected NodeMod
  getWorkingVersion
  (
    NodeStatus status
  )
    throws PipelineException
  {
    NodeMod mod = status.getLightDetails().getWorkingVersion();
    if (mod == null)
      throw new PipelineException
        ("There is no working version of the node (" + status.getName()  + ") that is " +
         "part of the template."); 
    return mod;
  }
  
  /**
   * Check if the node is part of the template.
   */
  private boolean
  inTemplate
  (
    NodeStatus status  
  )
    throws PipelineException
  {
    String nodeName = status.getName();
    if (!pTaskNetwork) {
      return pAllNodes.contains(nodeName);
    }
    else {
      TreeMap<String, BaseAnnotation> taskAnnots = getTaskAnnotations(nodeName);
      TreeSet<String> purposes = new TreeSet<String>();
      if (taskAnnots != null && !taskAnnots.isEmpty()) {
        boolean taskMatch = false;
        for ( String aName : taskAnnots.keySet()) {
          BaseAnnotation annot = taskAnnots.get(aName);
          taskMatch = doesTaskMatch(nodeName, aName, annot);
          purposes.add(lookupPurpose(nodeName, aName, annot));
        }
        if (taskMatch) { // Part of the template.
          return true;
        }
        else { //if the task doesn't match, it better be a Product node.
          if (!purposes.contains(NodePurpose.Product.toString()))
            throw new PipelineException
              ("The node ("+ nodeName + ") connected to this network belongs to a different " +
               "task, but is not a Product node.");
          return false;
        }
      }
      else { //If there are no task annotations.
        return false;
      }
    }
  }
  
  private boolean
  doesTaskMatch
  (
    String nodeName,
    String aName,
    BaseAnnotation annot
  )
    throws PipelineException
  {
    String projectName = lookupProjectName(nodeName, aName, annot);
    String taskName = lookupTaskName(nodeName, aName, annot);
    String taskType = lookupTaskType(nodeName, aName, annot);
    if (pTaskName.equals(taskName) && 
        pProjectName.equals(projectName) &&
        pTaskType.equals(taskType)) 
      return true;
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get a node in the template.
   */
  protected TemplateNode
  getTemplateNode
  (
    String name  
  )
    throws PipelineException
  {
    TemplateNode toReturn = pNodeDatabase.get(name);
    if (toReturn == null)
      throw new PipelineException
        ("No node named (" + name + ") exists in the template, yet a request was made for " +
         "its template node information.");
    return toReturn;
  }
  
  protected Set<String>
  getRootNodes()
  {
    return Collections.unmodifiableSet(pRootNodes);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7732743312567683496L;
 
 
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  protected TreeMap<String, TemplateNode> pNodeDatabase;
  
  private boolean pTaskNetwork;
  
  /** 
   * The task identifiers.
   */ 
  private String pProjectName;
  private String pTaskName;
  private String pTaskType;
  
  /**
   * The list of all the nodes in a non-task template. 
   */
  private TreeSet<String> pAllNodes;
  
  /**
   * The root nodes of the template. 
   */
  private TreeSet<String> pRootNodes;
}
