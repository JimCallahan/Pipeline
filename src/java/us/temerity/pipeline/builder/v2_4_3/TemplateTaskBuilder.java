// $Id: TemplateTaskBuilder.java,v 1.14 2009/06/11 05:14:06 jesse Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.plugin.TemplateIgnoreProductAnnotation.v2_4_3.*;
import us.temerity.pipeline.plugin.TemplateRangeAnnotation.v2_4_3.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   T A S K   B U I L D E R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Builder that will traverse a task network and then use the template builder to instantiate
 * a copy of that task.
 */
public 
class TemplateTaskBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor.
   * 
   * @param mclient
   *   The instance of MasterMgrClient the builder will use.
   *   
   * @param qclient
   *   The instance of QueueMgrClient the builder will use.
   *   
   * @param builderInformation
   *   The shared information class for builders.
   *   
   * @param startNode
   *   The node which the builder will use to begin search for task nodes to build. This node
   *   can be one of two different sorts of nodes.  It can be either the Submit or Approve
   *   node of the task, in which case the builder will traverse the network up to an Edit node,
   *   trace its way back down to the Submit and Approve nodes and then scan the network for 
   *   all nodes that should be built.  Alternatively, it can a grouping node that has all the 
   *   root nodes of the task underneath it.  The builder will then search upstream from all of 
   *   these root nodes to find all nodes in the task.  This method should be a lot quicker, 
   *   but requires there to be an extra node in existence.  If this builder is going to be
   *   used with the TemplateInfoBuilder, then the glue node that exists for the info builder
   *   can fill this role. 
   * 
   * @param stringReplacements
   *   A list of all the string replacements to be made for all nodes in the template and on
   *   all product nodes.
   * 
   * @param contexts
   *   A set of the replacements to be made, each indexed by the context name that will 
   *   trigger those replacements to be used.
   *   
   * @param frameRanges
   *   The list of frame ranges to use, each indexed by the name of the template Range value
   *   that should be set on the {@link TemplateRangeAnnotation}.
   *   
   * @param aoeModes
   *   The list of AoE modes and the default value for each mode.
   *   
   * @param externals
   *   The list of external file sequences that may be used in the template, keyed by the name
   *   of the external annotation.
   *   
   * @param optionalBranches
   *   The list of optional branches with a boolean value which represents whether the branch 
   *   should be built.
   *   
   * @throws PipelineException
   */
  public
  TemplateTaskBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    String startNode,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    TreeMap<String, FrameRange> frameRanges,
    TreeMap<String, ActionOnExistence> aoeModes,
    TreeMap<String, TemplateExternalData> externals,
    TreeMap<String, Boolean> optionalBranches
  ) 
    throws PipelineException
  {
    super("TaskTemplateBuilder",
      "Builder to read a template node network, built using the v2.4.1 Task system, " +
      "and create an instance of it for particular project.",
      mclient, qclient, builderInformation, null);
    
    noDefaultConstructPasses();
    
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
    
    pAOEModes = new TreeMap<String, ActionOnExistence>();
    if (aoeModes != null)
      pAOEModes.putAll(aoeModes);
    
    pExternals = new TreeMap<String, TemplateExternalData>();
    if (externals != null)
      pExternals.putAll(externals);
    
    pOptionalBranches = new TreeMap<String, Boolean>();
    if (optionalBranches != null)
      pOptionalBranches.putAll(optionalBranches);
    
    addCheckinWhenDoneParam();
    
    addSetupPass(new InformationPass());
    
    for (String mode : pAOEModes.keySet()) {
      ActionOnExistence aoe = pAOEModes.get(mode);
      addAOEMode(mode, aoe);
    }
    
    PassLayoutGroup rootLayout = new PassLayoutGroup("Root", "Root Layout");
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
          (aAllowZeroContexts,
           "Allow contexts to have no replacements.",
           false);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
          (aInhibitFileCopy,
           "Inhibit the CopyFile flag on all nodes in the template.",
           false);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new StringUtilityParam
          (aCheckInMessage,
           "The check-in message to use.",
           null);
      addParam(param);
    }
    
    {
      ArrayList<String> values = new ArrayList<String>();
      Collections.addAll(values, "Major", "Minor", "Micro");
      UtilityParam param = 
        new EnumUtilityParam
          (aCheckInLevel,
           "The check-in levelto use.",
           "Minor",
           values);
      addParam(param);
    }
    
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
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckInLevel);
    layout.addEntry(1, aCheckInMessage);
    layout.addEntry(1, null);
    layout.addEntry(1, aAllowZeroContexts);
    layout.addEntry(1, aInhibitFileCopy);
    
    rootLayout.addPass(layout.getName(), layout);
    setLayout(rootLayout);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R   M E T H O D S                                                         */
  /*----------------------------------------------------------------------------------------*/
  
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
   * Recursively finds all the nodes downstream of the current one which are a member
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
   * Recursively finds all the nodes upstream of the the current one which are a member
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
      if (parent != null) {
        pNodesIDependedOn.put(parent, nodeName);
        pNodesDependingOnMe.put(nodeName, parent);
      }
      return;
    }
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
        TreeMap<String, BaseAnnotation> annots = getAnnotations(nodeName); 
        BaseAnnotation annot = annots.get("TemplateOptionalBranch");
        if (annot != null) {
          String optionName = (String) annot.getParamValue(aOptionName);
          pOptionalBranchValues.put(optionName, nodeName);
          pLog.log(Kind.Bld, Level.Finest, 
            "Found an optional branch (" + optionName+ ") for node (" + nodeName + ").");
        }
      }
      else { //if the task doesn't match, it better be a Product node.
        if (!purposes.contains(NodePurpose.Product.toString()))
          throw new PipelineException
            ("The node ("+ nodeName + ") connected to this network belongs to a different " +
             "task, but is not a Product node.");
        checkProductNode(parent, nodeName);
        searchForContexts(parent, nodeName);
      }
    }//if (annots != null && !annots.isEmpty()) {
    else { //If there are no task annotations/ {
      checkProductNode(parent, nodeName);
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

  /**
   * Check if a product node is ignorable from the perspective of a given parent node.
   * 
   * @param parent
   *   The downstream node.
   * 
   * @param src
   *   The upstream node
   *   
   * @throws PipelineException
   */
  private void 
  checkProductNode
  (
    String parent,
    String src
  )
    throws PipelineException
  {
    pLog.logAndFlush(Kind.Bld, Level.Finest, 
      "Checking for product node (" + src + ") ignorability with regard to " +
      "(" + parent + ")");
    boolean ignoreable = getIgnorable(parent, src);
    if (!pProductNodes.containsKey(src)) {
      pProductNodes.put(src, ignoreable);
      pLog.logAndFlush(Kind.Bld, Level.Finest,
        "Ignorability has been set to (" + ignoreable + ")");
    }
    else if (pProductNodes.get(src) == true && !ignoreable) {
      pProductNodes.put(src, ignoreable);
      pLog.logAndFlush(Kind.Bld, Level.Finest,
        "Ignorability has been overriden to (" + ignoreable + ")");
    }
  }


  /**
   * Check if the src node is ignorable from the perspective of the target node.
   * <p>
   * The node is considered ignorable if the target node has a 
   * {@link TemplateIgnoreProductAnnotation} with the LinkName parameter set to the
   * value of the src node.
   * 
   * @param target
   *   The target node, which should be part of the template.
   * 
   * @param src
   *   The source node, which should not be part of the template
   * 
   * @return
   *   <code>true</code> if the node is ignorable, <code>false</code> otherwise.
   * 
   * @throws PipelineException
   */
  protected boolean 
  getIgnorable
  (
    String target,
    String src
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = getAnnotations(target);
    for (String aName : annots.keySet()) {
      if (aName.startsWith("TemplateIgnoreProduct")) {
        BaseAnnotation annot = annots.get(aName);
        String aSrc = (String) annot.getParamValue(aLinkName);
        if (aSrc.equals(src))
          return true;
      }
    }
    return false;
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
      
      pEditNodes           = new TreeSet<String>();
      pProductNodes        = new TreeMap<String, Boolean>();
      pProductNodeContexts = new DoubleMap<String, String, TreeSet<String>>();
      pNodesIDependedOn    = new MappedSet<String, String>();
      pNodesDependingOnMe  = new MappedSet<String, String>();
      pOptionalBranchValues    = new MappedSet<String, String>();
      
      TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(pStartNode);

      // This must be a grouping node case. 
      if (annots == null || annots.isEmpty()) {
        NodeStatus status = pClient.status(new NodeID(getAuthor(), getView(), pStartNode), 
                                           true, DownstreamMode.WorkingOnly);
        Set<String> sources = status.getSourceNames();
        if (sources.size() == 0)
          throw new PipelineException
            ("There were no source nodes of (" + pStartNode + ") which was specified as the " +
             "Template Task start node.");
        
        pProjectName = null;
        pTaskName = null;
        pTaskType = null;
        for (String source : sources) {
          TreeMap<String, BaseAnnotation> sAnnots = getTaskAnnotations(source);
          if (sAnnots == null || sAnnots.isEmpty()) 
            throw new PipelineException
              ("The node (" + source + ") that is a source of the start node " +
               "(" + pStartNode + ") has no task annotations on it");
          String aName = sAnnots.firstKey();
          BaseAnnotation annot = sAnnots.get(aName);
          if (pProjectName == null) {
            pProjectName = lookupProjectName(source, aName, annot);
            pTaskName = lookupTaskName(source, aName, annot);
            pTaskType = lookupTaskType(source, aName, annot);
          }
          else {
            if (!doesTaskMatch(source, aName, annot)) {
              throw new PipelineException
               ("The node ("+ source + ") connected to the start node belongs to a " +
               	"different task.");
            }
          }
          String purpose = lookupPurpose(source, aName, annot);
          if (purpose.equals(NodePurpose.Approve.toString())) 
            pApprovalNode = source;
        } // finish task check for all attached nodes
        
        if (pApprovalNode == null)
          throw new PipelineException
            ("Unable to find the approve node for the task!  The task approve node must be " +
             "connected to the template state node."); 
        
        for (String source : sources) {
          getLeafNodes(source);
        }
        
      }
      else {
        String aName = annots.firstKey();
        BaseAnnotation annot = annots.get(aName);
        pProjectName = lookupProjectName(pStartNode, aName, annot);
        pTaskName = lookupTaskName(pStartNode, aName, annot);
        pTaskType = lookupTaskType(pStartNode, aName, annot);

        getLeafNodes(pStartNode);

        pLog.log(Kind.Ops, Level.Finest, 
          "The following edit nodes were found:\n" + pEditNodes);

        if (pEditNodes.isEmpty())
          throw new PipelineException("There were no edit nodes found in the task network.");
        getSubmitAndApproveNodes(pEditNodes.first());
        getLeafNodes(pSubmitNode);
        getLeafNodes(pApprovalNode);
      }
      
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
         pProductNodes, pProductNodeContexts, pOptionalBranchValues);
      TemplateBuilder builder = 
        new TemplateBuilder
          (pClient, pQueue, getBuilderInformation(), info, pReplacements, pContexts, 
           pFrameRanges, pAOEModes, pExternals, pOptionalBranches);
      addSubBuilder(builder);
      addMappedParam(builder.getName(), aCheckinWhenDone, aCheckinWhenDone);
      addMappedParam(builder.getName(), aAllowZeroContexts, aAllowZeroContexts);
      addMappedParam(builder.getName(), aInhibitFileCopy, aInhibitFileCopy);
      addMappedParam(builder.getName(), aCheckInLevel, aCheckInLevel);
      addMappedParam(builder.getName(), aCheckInMessage, aCheckInMessage);
    }
    
    private TreeSet<String> pNodesToBuild;
    
    private static final long serialVersionUID = 372864091539779290L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1416838665331059152L;

  public static final String aContextName = "ContextName";
  public static final String aLinkName    = "LinkName";
  
  public static final String aAllowZeroContexts = "AllowZeroContexts";
  public static final String aInhibitFileCopy   = "InhibitFileCopy";
  public static final String aOptionName        = "OptionName";
  
  public static final String aCheckInLevel   = "CheckInLevel";
  public static final String aCheckInMessage = "CheckInMessage";
  
  
  
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
  private TreeMap<String, Boolean> pProductNodes;
  private DoubleMap<String, String, TreeSet<String>> pProductNodeContexts;
  
  private String pStartNode;
  
  private TreeMap<String, String> pReplacements;
  
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  
  private TreeMap<String, FrameRange> pFrameRanges;
  
  private TreeMap<String, ActionOnExistence> pAOEModes;
  
  private TreeMap<String, TemplateExternalData> pExternals;
  private TreeMap<String, Boolean> pOptionalBranches;
  
  private MappedSet<String, String> pNodesIDependedOn;
  private MappedSet<String, String> pNodesDependingOnMe;
  
  private MappedSet<String, String> pOptionalBranchValues;
}
