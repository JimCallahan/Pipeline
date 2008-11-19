// $Id: TemplateTaskBuilder.java,v 1.4 2008/11/19 04:34:48 jesse Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.execution.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   T A S K   B U I L D E R                                              */
/*------------------------------------------------------------------------------------------*/


public 
class TemplateTaskBuilder
  extends TaskBuilder
{
  public
  TemplateTaskBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    String startNode,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    TreeMap<String, FrameRange> frameRanges
  ) 
    throws PipelineException
  {
    super("TaskTemplateBuilder",
      "Builder to read a template node network, built using the v2.4.1 Task system, " +
      "and create an instance of it for particular project.",
      mclient, qclient, builderInformation, null);
    
    pStartNode = startNode;
    pReplacements = new TreeMap<String, String>();
    if (stringReplacements != null)
      pReplacements.putAll(stringReplacements);
    
    pContexts = new TreeMap<String, ArrayList<TreeMap<String,String>>>();
    if (contexts != null)
      pContexts.putAll(contexts);
    
    pFrameRanges = new TreeMap<String, FrameRange>();
    if (frameRanges != null)
      pFrameRanges.putAll(frameRanges);
    
    addCheckinWhenDoneParam();
    
    addSetupPass(new InformationPass());
    
    PassLayoutGroup rootLayout = new PassLayoutGroup("Root", "Root Layout");
    
    AdvancedLayoutGroup layout = 
      new AdvancedLayoutGroup
      ("Builder Information", 
       "The pass where all the basic information about the asset is collected " +
       "from the user.", 
       "BuilderSettings", 
       true);
    layout.addEntry(1, aUtilContext);
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckinWhenDone);
    layout.addEntry(1, aActionOnExistence);
    layout.addEntry(1, aReleaseOnError);
    
    rootLayout.addPass(layout.getName(), layout);
    setLayout(rootLayout);
  }
  
  
  /**
   * Search upstream from the specified node until all leaf Task nodes are found.
   */
  private void
  getLeafNodes
  (
    String startNode
  )
    throws PipelineException
  {
    NodeStatus status = pClient.status(new NodeID(getAuthor(), getView(), startNode), true, 
      DownstreamMode.WorkingOnly);
    
    findUpstreamNodes(null, status);
  }
  
  /**
   * Search downstream from an edit node until the Submit and Approve nodes are found.
   *
   * @param editNode
   *   The edit node of the Task.  This can be found with the 
   *   {@link #getEditNode(String, MasterMgrClient)} method.
   * @return A String Array containing two values. The first entry is the Submit node and the
   *   second is the Approve Node.
   * @throws PipelineException If either the Submit or the Approve node associated with 
   *    the task cannot be found.
   */
  private String[]
  getSubmitAndApproveNodes
  (
    String editNode
  )
    throws PipelineException
  {
    pSubmitNode = null;
    pApprovalNode = null;
    
    NodeStatus status = pClient.status(new NodeID(getAuthor(), getView(), editNode), true, 
      DownstreamMode.WorkingOnly);

    findDownstreamNodes(status);
    
    if (pSubmitNode == null)
      throw new PipelineException
        ("Unable to find the submit node for the task annotation on the node " +
        "(" + editNode + ")!"); 
    
    if (pApprovalNode == null)
      throw new PipelineException
        ("Unable to find the approve node for the task annotation on the node " +
        "(" + editNode + ")!"); 
    
    String toReturn[] = new String[2];
    toReturn[0] = pSubmitNode;
    toReturn[1] = pApprovalNode;
    return toReturn;
  }
  
  /**
   * Recursively finds all the nodes upstream of the current one which are a member
   * of the same task as the original node.
   * 
   * @param status
   *   The status of the current node.
   */
  private void
  findDownstreamNodes
  (
    NodeStatus status
  )
    throws PipelineException
  {
    if (pSubmitNode != null && pApprovalNode != null)
      return;
    String nodeName = status.getName();
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(nodeName);
    if (annots != null && !annots.isEmpty()) {
      boolean taskMatch = false;
      for ( String aName : annots.keySet()) {
        BaseAnnotation annot = annots.get(aName);
        String purpose = lookupPurpose(nodeName, aName, annot);
        taskMatch = doesTaskMatch(nodeName, aName, annot);
        if (taskMatch) {
          if (purpose.equals(NodePurpose.Submit.toString())) {
            pSubmitNode = nodeName;
            return;
          }
          if (purpose.equals(NodePurpose.Approve.toString())) {
            pApprovalNode = nodeName;
            return;
          }
        }
      }
      if (taskMatch) {
        Collection<NodeStatus> stati = status.getTargets();
        for (NodeStatus parentStatus : stati) {
          findDownstreamNodes(parentStatus);
          if (pSubmitNode != null && pApprovalNode != null)
            return;
        }
      }
    }
  }
  
  /**
   * Recursively finds all the nodes underneath the current one which are a member
   * of the same task as the original node.
   * 
   * @param status
   *   The status of the current node.
   */
  private void
  findUpstreamNodes
  (
    String parent,
    NodeStatus status
  )
    throws PipelineException
  {
    String nodeName = status.getName();
    // Easy out if we have already found this node and established that it belongs.
    if (pNodesDependingOnMe.containsKey(nodeName)) {
      pNodesIDependedOn.put(parent, nodeName);
      pNodesDependingOnMe.put(nodeName, parent);
      return;
    }
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(nodeName);
    TreeSet<String> purposes = new TreeSet<String>();
    if (annots != null && !annots.isEmpty()) {
      boolean taskMatch = false;
      for ( String aName : annots.keySet()) {
        BaseAnnotation annot = annots.get(aName);
        taskMatch = doesTaskMatch(nodeName, aName, annot);
        purposes.add(lookupPurpose(nodeName, aName, annot));
      }
      if (taskMatch) {
        if (parent != null) {
          pNodesIDependedOn.put(parent, nodeName);
          pNodesDependingOnMe.put(nodeName, parent);
        }
        if (purposes.contains(NodePurpose.Edit.toString()))
          pEditNodes.add(nodeName);
        Collection<NodeStatus> stati = status.getSources();
        for (NodeStatus childStatus : stati) {
          findUpstreamNodes(nodeName, childStatus);
        }
      }
      else { //if the task doesn't match, it better be a Product node.
        if (!purposes.contains(NodePurpose.Product.toString()))
          throw new PipelineException
            ("The node ("+ nodeName + ") connected to this network belongs to a different " +
             "task, but is not a Product node.");
        pProductNodes.add(nodeName);
        searchForContexts(parent, nodeName);
      }
    }//if (annots != null && !annots.isEmpty()) {
    else { //If there are no task annotations/ {
      pProductNodes.add(nodeName);
      searchForContexts(parent, nodeName);
    }
  }


  /**
   * Searches for contexts defined on the parent node and adds them to the data structure 
   * keeping track of those values.
   * <p>
   * @param target
   *   The name of the target node.
   * @param source
   *   The name of the product node (should not be part of the template).
   * @throws PipelineException
   */
  private void 
  searchForContexts
  (
    String target,
    String source
  )
    throws PipelineException
  {
    TreeSet<String> contexts = getContextualLinks(target, source);
    if (!contexts.isEmpty())
      pProductNodeContexts.put(source, target, contexts);
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
  
  /**
   * Return a list of all the contexts that the source node is identified as belonging
   * to on the target node.
   * @param target
   *   The node that is part of the template that contains the TemplateContextLink
   *   Annotations.
   * @param src
   *   The node outside the template that may be identified in the TemplateContextLink
   *   Annotations.
   * @return
   *   The set of contexts the src node is mentioned as belonging to. 
   * @throws PipelineException 
   */
  protected TreeSet<String>
  getContextualLinks
  (
    String target,
    String src
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = getAnnotations(target);
    TreeSet<String> toReturn = new TreeSet<String>();
    for (String aName : annots.keySet()) {
      if (aName.startsWith("TemplateContextLink")) {
        BaseAnnotation annot = annots.get(aName);
        String aSrc = (String) annot.getParamValue(aLinkName);
        if (aSrc.equals(src))
          toReturn.add((String) annot.getParamValue(aContextName));
      }
    }
    return toReturn;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class InformationPass
    extends SetupPass
  {
    public 
    InformationPass()
    {
      super("Information Pass", 
            "Information pass for the TemplateBuilder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      
      //grr, can't forget this call or stuff don't work.
      getStageInformation().setDoAnnotations(true);
      
      TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(pStartNode);
      if (annots == null || annots.isEmpty())
        throw new PipelineException
          ("There were no Task Annotations on the node (" + pStartNode + ")");
      String aName = annots.firstKey();
      BaseAnnotation annot = annots.get(aName);
      pProjectName = lookupProjectName(pStartNode, aName, annot);
      pTaskName = lookupTaskName(pStartNode, aName, annot);
      pTaskType = lookupTaskType(pStartNode, aName, annot);
      
      pEditNodes = new TreeSet<String>();
      pProductNodes = new TreeSet<String>();
      pProductNodeContexts = new DoubleMap<String, String, TreeSet<String>>();
      
      pNodesIDependedOn = new MappedSet<String, String>();
      pNodesDependingOnMe = new MappedSet<String, String>();
      
      getLeafNodes(pStartNode);
      
      pLog.log(Kind.Ops, Level.Finest, 
        "The following edit nodes were found:\n" + pEditNodes);
      
      if (pEditNodes.isEmpty())
        throw new PipelineException("There were no edit nodes found in the task network.");
      getSubmitAndApproveNodes(pEditNodes.first());
      getLeafNodes(pSubmitNode);
      getLeafNodes(pApprovalNode);
      
      pNodesToBuild = new TreeSet<String>();
      pNodesToBuild.addAll(pNodesDependingOnMe.keySet());
      pNodesToBuild.addAll(pNodesIDependedOn.keySet());

    }
    
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      TemplateBuildInfo info = new TemplateBuildInfo
        (pNodesToBuild, pNodesDependingOnMe, pNodesIDependedOn, 
         pProductNodes, pProductNodeContexts);
      TemplateBuilder builder = 
        new TemplateBuilder
          (pClient, pQueue, getBuilderInformation(), info, pReplacements, pContexts, pFrameRanges);
      addSubBuilder(builder);
      addMappedParam(builder.getName(), aCheckinWhenDone, aCheckinWhenDone);
    }
    
    private TreeSet<String> pNodesToBuild;
    
    private static final long serialVersionUID = 372864091539779290L;
  }
  
  
  
  public static void 
  main(
    String[] args
  ) 
    throws PipelineException
  {
    PluginMgrClient.init();
    
    LogMgr.getInstance().setLevel(Kind.Ops, Level.Finest);
    
    TreeMap<String, String> subs = new TreeMap<String, String>();
    subs.put("TEMPLATE", "templ");
    subs.put("SEQ1", "s001");
    subs.put("SHOT1", "s002");
    
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts = 
      new TreeMap<String, ArrayList<TreeMap<String,String>>>();
    
    {
      ArrayList<TreeMap<String,String>> context = new ArrayList<TreeMap<String,String>>(); 
      {
        TreeMap<String, String> sub = new TreeMap<String, String>();
        sub.put("ASSET", "horse");
        sub.put("TYPE", "prop");
        context.add(sub);
      }
      {
        TreeMap<String, String> sub = new TreeMap<String, String>();
        sub.put("ASSET", "bob");
        sub.put("TYPE", "char");
        context.add(sub);
      }
      {
        TreeMap<String, String> sub = new TreeMap<String, String>();
        sub.put("ASSET", "jimmy");
        sub.put("TYPE", "char");
        context.add(sub);
      }
      contexts.put("assets", context);
    }

    {
      ArrayList<TreeMap<String,String>> context = new ArrayList<TreeMap<String,String>>(); 
      {
        TreeMap<String, String> sub = new TreeMap<String, String>();
        sub.put("ATYPE", "facial");
        context.add(sub);
      }
      {
        TreeMap<String, String> sub = new TreeMap<String, String>();
        sub.put("ATYPE", "body");
        context.add(sub);
      }
      {
        TreeMap<String, String> sub = new TreeMap<String, String>();
        sub.put("ATYPE", "hands");
        context.add(sub);
      }
      contexts.put("atype", context);
    }
    
    
    //  subs.put("ASSET", "carriage");
    //  subs.put("TYPE", "prop");
    
    
    TemplateTaskBuilder builder = new TemplateTaskBuilder
      (new MasterMgrClient(), new QueueMgrClient(), 
       new BuilderInformation(false, true, true, new MultiMap<String, String>()), 
       "/projects/TEMPLATE/prod/SEQ1/SHOT1/anim/submit/SEQ1_SHOT1_submit", subs, contexts, null);
    
    GUIExecution exec = new GUIExecution(builder);
    exec.run();
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1416838665331059152L;

  public static final String aContextName = "ContextName";
  public static final String aLinkName = "LinkName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The task identifiers.
   */ 
  private String pProjectName;
  private String pTaskName;
  private String pTaskType;
  
  private TreeSet<String> pEditNodes;
  private String pSubmitNode;
  private String pApprovalNode;
  private TreeSet<String> pProductNodes;
  private DoubleMap<String, String, TreeSet<String>> pProductNodeContexts;
  
  private String pStartNode;
  
  private TreeMap<String, String> pReplacements;
  
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  
  private TreeMap<String, FrameRange> pFrameRanges;
  
  private MappedSet<String, String> pNodesIDependedOn;
  private MappedSet<String, String> pNodesDependingOnMe;
}
