// $Id: TemplateBuilder.java,v 1.2 2010/01/07 22:30:47 jesse Exp $

package us.temerity.pipeline.plugin.TemplateCollection.v2_4_12;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.plugin.TemplateRangeAnnotation.v2_4_3.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   B U I L D E R                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TemplateBuilder
  extends TemplateTraversalUtil
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
   
  /**
   * Constructor which exists to permit installation of the template builder as a plugin. <p>
   * 
   * This should never be called from user code.  The template builder will not function 
   * correctly if it is invoked.
   * 
   * @param mclient
   *   The instance of the Master Manager that the Builder is going to use.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the Builder is going to use
   * 
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   */
  public
  TemplateBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super("Template", "Instantiate a node network from an existing network", 
          mclient, qclient, builderInformation);
    
    pInvokedCorrectly = false;
    
    init(null, null, null, null, null, null, null);
  }
  
  /**
   * Constructor for the basic template builder.
   * <p>
   * This constructor takes a single node which is a grouping node for all the root nodes of
   * the template.  Templates using this constructor must be task networks.
   *
   * @param mclient
   *   The instance of the Master Manager that the Builder is going to use.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the Builder is going to use
   * 
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   * 
   * @param startNode
   *   The start node of the template. 
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
   * @param offsets
   *   The list of frame offsets with the integer value that should be assigned to each 
   *   offset.
   *   
   * @throws PipelineException
   */
  public
  TemplateBuilder
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
    TreeMap<String, Boolean> optionalBranches,
    TreeMap<String, Integer> offsets
  ) 
    throws PipelineException
  {
    super("Template", "Instantiate a node network from an existing network", 
          mclient, qclient, builderInformation);
    
    pLog.log(Kind.Ops, Level.Info, "Running in TaskSingle mode");
    
    pInvokedCorrectly = true;
    
    pStartNode = startNode;
    pTemplateType = TemplateType.TaskSingle;
    
    init(stringReplacements, contexts, frameRanges, aoeModes, externals, 
         optionalBranches, offsets);
  }
  
  /**
   * Constructor for the basic template builder.
   * <p>
   * This constructor takes a set of nodes, which are the root nodes of the template network.  
   * Templates using this constructor must be task networks.
   *
   * @param mclient
   *   The instance of the Master Manager that the Builder is going to use.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the Builder is going to use
   * 
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   * 
   * @param rootNodes
   *   The root nodes of the template. 
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
   * @param offsets
   *   The list of frame offsets with the integer value that should be assigned to each 
   *   offset.
   *   
   * @throws PipelineException
   */
  public
  TemplateBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    Set<String> rootNodes,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    TreeMap<String, FrameRange> frameRanges,
    TreeMap<String, ActionOnExistence> aoeModes,
    TreeMap<String, TemplateExternalData> externals,
    TreeMap<String, Boolean> optionalBranches,
    TreeMap<String, Integer> offsets
  ) 
    throws PipelineException
  {
    super("Template", "Instantiate a node network from an existing network", 
          mclient, qclient, builderInformation);
    
    pLog.log(Kind.Ops, Level.Info, "Running in TaskList mode");
    
    pInvokedCorrectly = true;
    
    pRootNodes = rootNodes;
    pTemplateType = TemplateType.TaskList;
    
    init(stringReplacements, contexts, frameRanges, aoeModes, externals, 
         optionalBranches, offsets);
  }
  
  /**
   * Constructor for the basic template builder.
   * <p>
   * This constructor takes a set of nodes, which are the root nodes of the template network 
   * and a second set of nodes, which contains all the nodes in the template. Templates 
   * using this constructor need not be task networks.
   *
   * @param mclient
   *   The instance of the Master Manager that the Builder is going to use.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the Builder is going to use
   * 
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   * 
   * @param rootNodes
   *   The root nodes of the template.
   *   
   * @param allNodes
   *   All of the nodes that are in the template.
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
   * @param offsets
   *   The list of frame offsets with the integer value that should be assigned to each 
   *   offset.
   *   
   * @throws PipelineException
   */
  public
  TemplateBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    Set<String> rootNodes,
    Set<String> allNodes,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    TreeMap<String, FrameRange> frameRanges,
    TreeMap<String, ActionOnExistence> aoeModes,
    TreeMap<String, TemplateExternalData> externals,
    TreeMap<String, Boolean> optionalBranches,
    TreeMap<String, Integer> offsets
  ) 
    throws PipelineException
  {
    super("Template", "Instantiate a node network from an existing network", 
          mclient, qclient, builderInformation);
    
    pLog.log(Kind.Ops, Level.Info, "Running in NonTask mode");
    
    pInvokedCorrectly = true;

    pTemplateType = TemplateType.NonTask;
    pRootNodes = rootNodes;
    pAllNodes = allNodes;
    
    init(stringReplacements, contexts, frameRanges, aoeModes, externals, 
         optionalBranches, offsets);
  }
  
  private void
  init
  (
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    TreeMap<String, FrameRange> frameRanges,
    TreeMap<String, ActionOnExistence> aoeModes,
    TreeMap<String, TemplateExternalData> externals,
    TreeMap<String, Boolean> optionalBranches,
    TreeMap<String, Integer> offsets
  )
    throws PipelineException
  {
    pReplacements = new TreeMap<String, String>();
    if (stringReplacements != null)
      pReplacements.putAll(stringReplacements);
    for (Entry<String, String> entry : pReplacements.entrySet()) {
      if (entry.getValue() == null)
        throw new PipelineException
          ("The replacement value for (" + entry.getKey() + ") was set to null.  The " +
           "template builder cannot accept a null replacement value.");
    }
    pLog.log(Kind.Ops, Level.Finest, 
      "The list of top-level string replacements: " + pReplacements);
    
    pContexts = new TreeMap<String, ArrayList<TreeMap<String,String>>>();
    if (contexts != null)
      pContexts.putAll(contexts);
    pLog.log(Kind.Ops, Level.Finest, 
      "The list of contexts to apply to this template: " + pContexts);
    
    pFrameRanges = new TreeMap<String, FrameRange>();
    if (frameRanges != null)
      pFrameRanges.putAll(frameRanges);
    for (Entry<String, FrameRange> entry : pFrameRanges.entrySet()) {
      if (entry.getValue() == null)
        throw new PipelineException
          ("The frame range (" + entry.getKey() + ") was set to null.  The template builder " +
           "cannot accept a null frame range.");
    }
    pLog.log(Kind.Ops, Level.Finest, 
      "The list of frame ranges to apply to this template: " + pFrameRanges);
    
    pExternals = new TreeMap<String, TemplateExternalData>();
    if (externals != null)
      pExternals.putAll(externals);
    for (Entry<String, TemplateExternalData> entry : pExternals.entrySet()) {
      if (entry.getValue() == null)
        throw new PipelineException
          ("The external sequence (" + entry.getKey() + ") was set to null.  The template " +
           "builder cannot accept a null external sequence.");
    }
    pLog.log(Kind.Ops, Level.Finest, 
      "The list of external file sequences available to this template: " + pExternals);

    pOptionalBranches = new TreeMap<String, Boolean>();
    if (optionalBranches != null)
      pOptionalBranches.putAll(optionalBranches);
    for (Entry<String, Boolean> entry : pOptionalBranches.entrySet()) {
      if (entry.getValue() == null)
        throw new PipelineException
          ("The optional branch (" + entry.getKey() + ") was set to null.  The template " +
           "builder cannot accept a null optional branch.");
    }
    pLog.log(Kind.Ops, Level.Finest, 
      "The list of optional branches to apply to this template: " + pOptionalBranches);
    
    pOffsets = new TreeMap<String, Integer>();
    if (offsets != null)
      pOffsets.putAll(offsets);
    pLog.log(Kind.Ops, Level.Finest, 
      "The list of offsets available to this template: " + pOffsets);
    
    pParamManifest = new TemplateParamManifest();
    pParamManifest.setReplacements(pReplacements);
    pParamManifest.setContexts(pContexts);
    pParamManifest.setFrameRanges(pFrameRanges);
    pParamManifest.setExternals(pExternals);
    pParamManifest.setOffsets(pOffsets);
    pParamManifest.setOptionalBranches(pOptionalBranches);
    pParamManifest.setAOEModes(aoeModes);
    
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
         "The check-in level to use.",
         "Minor",
         values);
      addParam(param);
    }
    
    addCheckinWhenDoneParam();
    
    addSetupPass(new InformationPass());
    addConstructPass(new BuildPass());
    addConstructPass(new FinalizePass());
    addConstructPass(new SecondFinalizePass());
    
    if (aoeModes != null) {
      for (String mode : aoeModes.keySet()) {
        ActionOnExistence aoe = aoeModes.get(mode);
        addAOEMode(mode, aoe);
      }
    }
    
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
  /*   B U I L D E R   O V E R R I D E S                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public us.temerity.pipeline.VersionID.Level 
  getCheckinLevel()
  {
    return pCheckInLevel;
  }
  
  @Override
  public String
  getCheckInMessage()
  {
    if (pCheckInMessage != null)
      return pCheckInMessage;

    return "Node tree created using the (" + getName() + ") Builder.\n";
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   U T I L S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Replace each key occurrence with the its value in the source string.
   * 
   * @param source
   *   The string which will be replaced.
   * 
   * @param stringReplacements
   *   A map with the keys being the strings to be replaced and the values being what to 
   *   replace the  
   */
  protected String
  stringReplace
  (
    String source,
    TreeMap<String, String> stringReplacements
  )
  {
    String toReturn = source;
    for (String pattern : stringReplacements.keySet())
      toReturn = toReturn.replaceAll(pattern, stringReplacements.get(pattern));
    return toReturn;
  }
  
  /**
   * Get all the sets of replacements generated from the list of contexts.
   * <p>
   * This method does not do any error-checking for zero-value contexts.  Zero-value contexts
   * are treated as if the builder was set to allow zero-value contexts.
   * 
   * @param contexts
   *   The list of contexts.   
   * 
   * @return
   *   A list of all the maps of string replacements.
   */
  protected LinkedList<TreeMap<String, String>>
  allContextReplacements
  (
    Set<String> contexts  
  )
  {
    LinkedList<TreeMap<String, String>> toReturn = new LinkedList<TreeMap<String,String>>();
    if (contexts.isEmpty()) {
      toReturn.add(new TreeMap<String, String>(pReplacements));
      return toReturn;
    }
    allContextReplacementsHelper(new TreeSet<String>(contexts), pReplacements, toReturn);
    return toReturn;
  }
  
  private void
  allContextReplacementsHelper
  (
    TreeSet<String> contexts,
    TreeMap<String,String> replacements, 
    LinkedList<TreeMap<String, String>> data
  )
  {
    String currentContext = contexts.pollFirst();
    ArrayList<TreeMap<String, String>> values = pContexts.get(currentContext);
    
    if (values == null || values.isEmpty()) {
      TreeMap<String, String> newReplace = new TreeMap<String, String>(replacements);

      if (contexts.isEmpty()) {  //bottom of the recursion
        data.add(newReplace);
      }
      else {
        allContextReplacementsHelper(new TreeSet<String>(contexts), newReplace, data);
      }
      return;
    }
    
    for (TreeMap<String, String> contextEntry : values) {
      TreeMap<String, String> newReplace = new TreeMap<String, String>(replacements);
      newReplace.putAll(contextEntry);
     
      if (contexts.isEmpty()) {  //bottom of the recursion
        data.add(newReplace);
      }
      else {
        allContextReplacementsHelper(new TreeSet<String>(contexts), newReplace, data);
      }
    }
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
      if (!pInvokedCorrectly)
         throw new PipelineException
           ("The template builder was not invoked with the correct constructor.  The " +
            "generic constructor exists only to allow installation as a plugin, but should " +
            "never be used to run this builder;");
      
      validateBuiltInParams();
      //grr, can't forget this call or stuff don't work.
      getStageInformation().setDoAnnotations(true);
      
      switch(pTemplateType) {
      case TaskSingle:
        {
          NodeMod mod = getWorkingVersion(pStartNode);
          pDescManifest = new TemplateDescManifest(pStartNode, mod.getWorkingID());
          traverseTaskNetwork(pStartNode);
        }
        break;
      case TaskList:
        {
          TreeMap<String, VersionID> roots = new TreeMap<String, VersionID>();
          for (String root : pRootNodes) {
            NodeMod mod = getWorkingVersion(root);
            roots.put(root, mod.getWorkingID());
          }
          pDescManifest = new TemplateDescManifest(roots);
        }
        traverseTaskNetwork(pRootNodes);
        break;
      case NonTask:
        {
          TreeMap<String, VersionID> roots = new TreeMap<String, VersionID>();
          for (String root : pRootNodes) {
            NodeMod mod = getWorkingVersion(root);
            roots.put(root, mod.getWorkingID());
          }
          pDescManifest = new TemplateDescManifest(roots, pAllNodes);
        }
        traverseNetwork(pRootNodes, pAllNodes);
        break;
      }
      
      pAllowZeroContexts = getBooleanParamValue(new ParamMapping(aAllowZeroContexts));
      pInhibitCopyFiles = getBooleanParamValue(new ParamMapping(aInhibitFileCopy));
      pCheckInMessage = getStringParamValue(new ParamMapping(aCheckInMessage));
      
      {
        int level = getEnumParamIndex(new ParamMapping(aCheckInLevel));
        pCheckInLevel = us.temerity.pipeline.VersionID.Level.values()[level];
      }
      
      pBuildList = new TreeSet<String>();
      pOptionalCheckoutList = new TreeSet<String>();
      
      pFoundContexts = new TreeSet<String>();
      
      for (String root : getRootNodes()) {
        checkUpstreamNodes(getTemplateNode(root), null, null);
      }
      
      for (String context : pFoundContexts) {
        ArrayList<TreeMap<String, String>> replacements = pContexts.get(context);
        if (replacements == null || replacements.isEmpty()) {
          if (pAllowZeroContexts)
            pLog.logAndFlush(Kind.Ops, Level.Warning, 
              "The context (" + context + ") contains no replacements.");
          else
            throw new PipelineException 
              ("The context (" + context + ") contains no replacements.");
        }
      }
      
      pFinalizableStages = new ArrayList<FinalizableStage>();
      pSecondaryFinalizableStages = new ArrayList<TemplateStage>();
    }
    
    private void
    checkUpstreamNodes
    (
      TemplateNode node,
      String checkOutBranchName,
      String optionalCheckoutRoot
    )
      throws PipelineException
    {
      String nodeName = node.getNodeName();
      
      if (node.isInTemplate()) {
        
        if (node.getNodeMod().isLocked())
          throw new PipelineException
            ("The node (" + nodeName + ") which is identified as part of the template is " +
             "locked.  Locked nodes are not valid in templates, except as products.");
        
        if (node.modifyFiles() && !node.cloneFiles())
          throw new PipelineException
            ("The node (" + nodeName + ") has the modify files flag set on it, but does not " +
             "have the clone files flag set as well.");
        
        String optionalBranch = node.getOptionalBranch();
        OptionalBranchType optionalType = node.getOptionalBranchType();
        OptionalProductType productType = node.getOptionalProductType();
        
        if(optionalBranch != null && !pOptionalBranches.containsKey(optionalBranch))
          throw new PipelineException
            ("The node (" + nodeName  + ") has an optional branch of " +
             "(" + optionalBranch + ") on it, but no value was provided for that branch " +
             "when the template was invoked.");

        boolean isOptional = false;
        if (optionalBranch != null && !pOptionalBranches.get(optionalBranch))
          isOptional = true;
        
        if (productType == OptionalProductType.LockCurrent &&
            optionalType != OptionalBranchType.AsProduct)
          throw new PipelineException
            ("The node (" + nodeName +") has the product type of (" + productType + ") but " +
             "an option type of (" + optionalType + ").  That product type is only valid " +
             "with an option type of (AsProduct)");
        
        /* 
         * If we are directly downstream from an optional branch that is being checked-out,
         * then we need to do some additional error checking.  
         */
        if (optionalCheckoutRoot != null ) {
          /*
           * If this node is not part of optional branch or is part of an optional branch, but 
           * that optional branch is not active, then we have a problem.
           */
          if (optionalBranch == null || !isOptional)
            throw new PipelineException
              ("The node (" + nodeName + ") is upstream from an activated optional " +
               "branch checkout rooted at (" + optionalCheckoutRoot + ") with the branch name " +
               "(" + checkOutBranchName + "), but it is not part of the optional branch.  " +
               "Due to this, modifications to this node would cause frozen staleness and the " +
               "template would fail.  The template cannot complete as currently configured.  " +
               "If the Optional Branch Checkout option is going to be used than everything " +
               "upstream of it must also be part of the Optional Branch");

          /* For everything after this, we can assume that this optional branch is active */
          if (optionalType == OptionalBranchType.CheckOut)
            throw new PipelineException
              ("The node (" + nodeName +") is tagged as the Checkout node of the optional " +
               "branch (" + optionalBranch + ").  However it is upstream of the node " +
               "(" + optionalCheckoutRoot + ") which is the checkout root of activated " +
               "branch (" + checkOutBranchName + ").  Having two different optional branches " +
               "attempt to checkout the same nodes could result in frozen staleness and " +
               "template failure.");
          if (!optionalBranch.equals(checkOutBranchName)) {
            pLog.logAndFlush(Kind.Ops, Level.Warning, 
              "The node (" + nodeName + ") is from the optional branch " +
              "(" + optionalBranch + ").  In the current template invocation it is being " +
              "checked-out by the root node (" + optionalCheckoutRoot + ") from the optional " +
              "branch (" + checkOutBranchName + ").  Depending on how these networks are " +
              "configured, this may result in frozen staleness and template failure.  " +
              "While this will not terminate the template, it is probably an indication of " +
              "some problem in the template design.");
          }
        } // if (optionalCheckoutRoot != null )
        
        node.setIsOptional(isOptional);
        
        if (isOptional && optionalType == OptionalBranchType.CheckOut) {
          optionalCheckoutRoot = nodeName;
          checkOutBranchName = optionalBranch;
          pOptionalCheckoutList.add(nodeName);
        }
        else if (isOptional && optionalType == OptionalBranchType.BuildOnly) {
          node.setSkipped(true);
        }
        
        if (!isOptional)
          pBuildList.add(nodeName);
  
        Set<String> contexts = node.getContexts();
        
        pFoundContexts.addAll(contexts);
        pFoundContexts.addAll(node.getSecondaryContexts());
        
        validateSecContexts(node);
        
        LinkedList<TreeMap<String, String>> replacements = null;
        if (!contexts.isEmpty())
          replacements = allContextReplacements(contexts);
        else {
          replacements = new LinkedList<TreeMap<String,String>>();
          replacements.add(new TreeMap<String, String>());
        }
        
        {
          String frameRange = node.getFrameRangeName();
          if (frameRange != null) {
            for (TreeMap<String, String> replace : replacements ) {
              String actualRange = stringReplace(frameRange, replace);
              if (!pFrameRanges.containsKey(actualRange))
                throw new PipelineException
                  ("The node (" + nodeName +") has a frame range (" + actualRange +") that " +
                   "was generated from (" + frameRange + ") on it, but no value was " +
                   "provided for that frame range when the template was invoked.");
            }
          }
        }
        
        {
          String external = node.getExternalName();
          if (external != null) {
            BaseAction act = node.getNodeMod().getAction();
            if (act != null)
              throw new PipelineException
                ("The node (" + nodeName + ") has an external annotation but also has an " +
                 "Action assigned to it.  This is not allowable in the Template Builder.");
            
            if (node.cloneFiles())
              throw new PipelineException
                ("The node (" + nodeName + ") has an external annotation but also has the " +
                 "clone files setting.  This is not allowable in the Template Builder.");
            
            if (node.touchFiles())
              throw new PipelineException
                ("The node (" + nodeName + ") has an external annotation but also has the " +
                 "touch files setting.  This is not allowable in the Template Builder.");

            if (node.isIntermediate())
              throw new PipelineException
                ("The node (" + nodeName + ") has an external annotation but also has the " +
                 "intermediate setting.  This is not allowable in the Template Builder.");
            
            if (node.getManifestType() != null)
              throw new PipelineException
                ("The node (" + nodeName + ") has an external annotation but also has a " +
                 "manifest annotation.  This is not allowable in the Template Builder.");
            
            for (TreeMap<String, String> replace : replacements ) {
              String actualExternal = stringReplace(external, replace);
              if (!pExternals.containsKey(actualExternal))
                throw new PipelineException
                  ("The node (" + nodeName +") has a frame range (" + actualExternal+") " +
                   "that was generated from (" + external + ") on it, but no value was " +
                   "provided for that frame range when the template was invoked.");

            }
          }
        }
        {
          String manifest = node.getManifestType();
          if (manifest != null) {
            NodeMod mod = node.getNodeMod();
            
            if (mod.getAction() != null)
              throw new PipelineException
                ("The node (" + nodeName + ") has a manifest annotation but also has an " +
                 "Action assigned to it.  This is not allowable in the Template Builder.");
            
            if (mod.getPrimarySequence().hasFrameNumbers())
              throw new PipelineException
                ("The node (" + nodeName + ") has a manifest annotation but also has frame " +
                 "numbers in its FileSeq.  This is not allowable in the Template Builder.");

            if (!mod.getSecondarySequences().isEmpty())
              throw new PipelineException
                ("The node (" + nodeName + ") has a manifest annotation but also has " +
                 "secondary sequences.  This is not allowable in the Template Builder.");
            
            if (node.touchFiles())
              throw new PipelineException
                ("The node (" + nodeName + ") has a manifest annotation but also has the " +
                 "touch files setting.  This is not allowable in the Template Builder.");

            if (node.isIntermediate())
              throw new PipelineException
                ("The node (" + nodeName + ") has a manifest annotation but also has the " +
                 "intermediate setting.  This is not allowable in the Template Builder.");
            
            if (node.getFrameRangeName() != null)
              throw new PipelineException
                ("The node (" + nodeName + ") has a manifest annotation but also has a " +
                 "frame range annotation.  This is not allowable in the Template Builder.");
          }
        }

        for (Entry<String, TemplateLink> entry : node.getSources().entrySet()) {
          String childName = entry.getKey();
          TemplateLink link = entry.getValue();
          TemplateNode childNode = getTemplateNode(childName);
          boolean inTemplate = childNode.isInTemplate();
          String offset = link.getOffset();
          
          if (inTemplate && link.hasContexts())
            throw new PipelineException
              ("The node (" + nodeName + ") has context links pointing to (" + childName +"), " +
               "but the upstream node is part of the template.  Context links should only " +
               "point to nodes outside the template.");
          if (inTemplate && link.isIgnorable())
            throw new PipelineException
              ("The node (" + nodeName + ") has an ignore product annotation pointing to " +
               "(" + childName +"), but the upstream node is part of the template.  Only " +
               "nodes not in the current template can be ignored.");
          
          if (offset != null) {
            LinkedList<TreeMap<String, String>> secReplacments;
            if (inTemplate) {
              secReplacments = allContextReplacements(childNode.getContexts());
            }
            else {
              secReplacments = allContextReplacements(link.getContexts());
            }
            
            for (TreeMap<String, String> replace : secReplacments ) {
             String newOffset = stringReplace(offset, replace);
             if (!pOffsets.containsKey(newOffset))
               throw new PipelineException
                 ("The node (" + nodeName +") has a frame offset pointing to link " +
                  "(" + childName + ") whose name is (" + newOffset + ") after all context " +
                  "replacements have been applied to it, yet no offset with that name was " +
                  "passed into the template builder.");
            }
          }
          
          if (!inTemplate) {
            pFoundContexts.addAll(link.getContexts());
            pFoundContexts.addAll(childNode.getSecondaryContexts());
          }
          else {
            checkUpstreamNodes(childNode, checkOutBranchName, optionalCheckoutRoot);
          }
        }
      } //if (node.isInTemplate())
      else {
        validateSecContexts(node);
      }
    }
    
    private void 
    validateSecContexts
    (
      TemplateNode node
    )
      throws PipelineException
    {
      TreeSet<String> secPatterns = new TreeSet<String>();
      SortedSet<FileSeq> secs = node.getNodeMod().getSecondarySequences();
      if (!node.getSecondaryContexts().isEmpty() && secs.isEmpty())
        throw new PipelineException
          ("The node (" + node.getNodeName() +") has secondary contexts on it, but does " +
           "not have an secondary sequences");
      for (FileSeq sec : secs) 
        secPatterns.add(sec.getFilePattern().toString());
      for (String sec : node.getSecSeqsWithContexts()) {
        if (!secPatterns.contains(sec))
          throw new PipelineException
            ("The secondary sequence file pattern (" + sec  +") specified as part a " +
             "secondary context on the node (" + node.getNodeName() + ") is not a valid " +
             "secondary sequence on that node.");
      }
    }

    private static final long serialVersionUID = 2185606398523410267L;
    
    
    private TreeSet<String> pFoundContexts;
  }
 
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected
  class BuildPass
    extends ConstructPass
  {
    public
    BuildPass()
    {
      super("BuildPass", "Pass with makes all the nodes.");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      ArrayList<String> roots = new ArrayList<String>(); 
      MappedSet<Integer, String> orderedRoots = new MappedSet<Integer, String>();
      
      for (String nodeName : pOptionalCheckoutList) {
        pLog.log
          (Kind.Ops, Level.Fine, 
           "Finding Optional Branch(s) for check out based on template node " +
           "(" + nodeName +")");
        TemplateNode node = getTemplateNode(nodeName);
        
        Set<String> contexts = node.getContexts();
        TreeSet<String> allNames = null;
        if (contexts.isEmpty()) {
          String realName = stringReplace(nodeName, pReplacements);
          allNames = new TreeSet<String>();
          allNames.add(realName);
        }
        else {
          allNames = expandContexts(nodeName, contexts);
        }
        for (String realName : allNames) {
          if (nodeExists(realName)) {
            checkOutNewer(realName, CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
            pLog.log
              (Kind.Ops, Level.Finer, 
               "Checked out the Optional Branch rooted at (" + realName +")");
          }
          else {
            pLog.log
              (Kind.Ops, Level.Finer, 
               "Skipped the Optional Branch rooted at (" + realName +"), since the node " +
               "does not exist.");
          }
        }
      }
      
      for (String root : getRootNodes()) {
        validateOptionalBranches(root);
      }
      
      
      while(!pBuildList.isEmpty()) {
        String nodeName = getNodeToBuild();
        if (nodeName == null && !pBuildList.isEmpty())
          throw new PipelineException
            ("There are no nodes which are ready to be constructed.  " +
             "This state should be impossible with any node network I can think of." +
             "The nodes which are left are " + pBuildList);
    
        pLog.log(Kind.Ops, Level.Fine, "Template Node: " + nodeName);
        
        TemplateNode node = getTemplateNode(nodeName);
        
        TreeSet<String> nodesMade = new TreeSet<String>();
        TreeSet<String> allNodes  = new TreeSet<String>();
        
        TreeSet<String> contexts = new TreeSet<String>(node.getContexts());
        if (contexts.size() == 0) {
          makeNode(node, pReplacements, pContexts, nodesMade, allNodes);
        }
        else {
          contextLoop(node, contexts, pReplacements, pContexts, nodesMade, allNodes);
        }
        
        if (node.isVouchable()) {
          for (String each : allNodes)
            addToVouchableList(each);
        }
        
        if (node.isRoot()) {
          Integer order = node.getOrder();
          if (order == null) {
            roots.addAll(allNodes);
          }
          else {
            for (String each : allNodes)
              orderedRoots.put(order, each);
          }
        }
        
        node.setBuilt(true);
        pBuildList.remove(nodeName);
        
        pLog.logAndFlush(Kind.Ops, Level.Fine, "FINISHED with template node.\n");
      } //while(!pBuildList.isEmpty())
     
      for (Integer order : orderedRoots.keySet()) {
        for (String root : orderedRoots.get(order)) {
          pLog.log
            (Kind.Bld, Level.Finer, 
             "Order (" + order + ").  Adding root node (" + root + ") to " +
             "queue and check-in list.");
          addToQueueList(root);
          addToCheckInList(root);  
        }
      }
      for (String root : roots) {
        pLog.log(Kind.Bld, Level.Finer, 
          "Order (none).  Adding root node (" + root + ") to " +
          "queue and check-in list.");
        addToQueueList(root);
        addToCheckInList(root);  
      }
    }
    
    private void 
    contextLoop
    (
      TemplateNode node,
      TreeSet<String> contextList,
      TreeMap<String, String> replace,
      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts, 
      TreeSet<String> nodesMade, 
      TreeSet<String> allNodes
    )
      throws PipelineException
    {
      String currentContext = contextList.pollFirst();
      ArrayList<TreeMap<String, String>> values = contexts.get(currentContext);
      
      if (values == null || values.isEmpty()) {
        TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
        TreeMap<String, ArrayList<TreeMap<String, String>>> newMaps = 
          new TreeMap<String, ArrayList<TreeMap<String,String>>>(contexts);
        if (contextList.isEmpty()) {  //bottom of the recursion
          makeNode(node, newReplace, newMaps, nodesMade, allNodes);
        }
        else {
          contextLoop
          (node, new TreeSet<String>(contextList), 
            newReplace, newMaps, nodesMade, allNodes);
        }
        return;
      }
      
      for (TreeMap<String, String> contextEntry : values) {
        TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
        newReplace.putAll(contextEntry);
        TreeMap<String, ArrayList<TreeMap<String, String>>> newMaps = 
          new TreeMap<String, ArrayList<TreeMap<String,String>>>(contexts);
        ArrayList<TreeMap<String, String>> newStuff = new ArrayList<TreeMap<String,String>>();
        newStuff.add(new TreeMap<String, String>(contextEntry));
        newMaps.put(currentContext, newStuff);
        
        if (contextList.isEmpty()) {  //bottom of the recursion
          makeNode(node, newReplace, newMaps, nodesMade, allNodes);
        }
        else {
          contextLoop
            (node, new TreeSet<String>(contextList), 
             newReplace, newMaps, nodesMade, allNodes);
        }
      }
    }
    
    private void 
    makeNode
    (
      TemplateNode node,
      TreeMap<String, String> replace,
      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
      TreeSet<String> nodesMade, 
      TreeSet<String> allNodes
    )
      throws PipelineException
    {
      String nodeName = stringReplace(node.getNodeName(), replace);
      
      pLog.logAndFlush (Kind.Ops, Level.Fine, "Instantiated node: " + nodeName);
      
      if (nodeName.equals(node.getNodeName()))
        throw new PipelineException
          ("The name of the node in the template (" + nodeName + ") is the same as the " +
           "instantiated node.  A template cannot build over itself.  All nodes in a template " +
           "must contain at least one string replacement.");
      
      String conditionalBuild = node.getConditionalBuild();
      if (conditionalBuild != null) {
        String check = stringReplace(conditionalBuild, replace);
        TemplateNode cbNode = pNodeDatabase.get(conditionalBuild);
        if (cbNode != null) {
          if (cbNode.isSkippedNode(check)) {
            pLog.logAndFlush
            (Kind.Ops, Level.Fine, 
             "Not building (" + nodeName + ") because conditional node (" + check + ") " +
             "was skipped earlier in the template process");
            node.addSkippedNode(nodeName);
            return;
          }
        }
        
        if (!nodeExists(check)) {
          pLog.logAndFlush
            (Kind.Ops, Level.Fine, 
             "Not building (" + nodeName + ") because conditional node (" + check + ") " +
             "does not exist");
          node.addSkippedNode(nodeName);
          return;
        }
      }
      
      for (Entry<String, ActionOnExistence> mode : node.getAOEModes().entrySet()) {
        addAOEOverride(mode.getKey(), nodeName, mode.getValue());
      }
       
      TemplateExternalData exD = null;
      String external = node.getExternalName();
      if (external != null) {
        String newExternalName = stringReplace(external, replace);
        exD = pExternals.get(newExternalName);
      }
      
      FrameRange range = null;
      String frameRangeName = node.getFrameRangeName(); 
      if (frameRangeName != null) {
        frameRangeName = stringReplace(frameRangeName, replace);
        range = pFrameRanges.get(frameRangeName);
      }
      
      TemplateStage stage = 
        TemplateStage.getTemplateStage
          (node, getStageInformation(), pContext, pClient, 
           replace, contexts, range,
           pInhibitCopyFiles, exD, pOffsets, 
           pParamManifest, pDescManifest, pNodeDatabase);

      allNodes.add(nodeName);
      
      if (stage.build()) {
        if (stage.needsFinalization()) {
          pLog.logAndFlush
            (Kind.Bld, Level.Finer, 
             "Node added to finalize list.");
          pFinalizableStages.add(stage);
        }
        if (stage.needsSecondFinalization()) {
          pLog.logAndFlush
            (Kind.Bld, Level.Finer, 
             "Node added to second finalize list.");
          pSecondaryFinalizableStages.add(stage);
        }
        nodesMade.add(nodeName);
        node.addBuiltNode(nodeName);
        pLog.logAndFlush
          (Kind.Ops, Level.Fine, 
           "Node Instantiated.");
      }
    }
    
    private String
    getNodeToBuild()
      throws PipelineException
    {
      String toBuild = null;
      for (String root : getRootNodes() ) {
        toBuild = checkNode(root);
        if (toBuild != null)
          break;
      }
      return toBuild;
    }
    
    private String
    checkNode
    (
      String nodeName
    )
      throws PipelineException
    {
      TemplateNode node = getTemplateNode(nodeName);
      if (node.isDone())
        return null;
      
      Map<String, TemplateLink> links = node.getSources();
      if (links.isEmpty() && node.isInTemplate())
        return nodeName;
        
      boolean allDone = true;
      for (Entry<String, TemplateLink> entry: links.entrySet() ) {
        TemplateNode child = getTemplateNode(entry.getKey());
        if (!child.isInTemplate()) {
          if (!child.wasProductAcquired(nodeName)) {
            acquireProduct(child, nodeName);
            child.addProductTarget(nodeName);
          }
        }
        else if (!child.isDone()) {
          allDone = false;
          String temp = checkNode(entry.getKey());
          if (temp != null)
            return temp;
        }
      }
      if (allDone)
        return nodeName;
      return null;
    }
    
    private void 
    acquireProduct
    (
      TemplateNode node,
      String parent
    )
      throws PipelineException
    {
      String nodeName = node.getNodeName();
      pLog.log
        (Kind.Ops, Level.Fine, 
         "Searching for product nodes based on the template node (" + nodeName + ") " +
         "as it is used by template node (" + parent +")");
      
      TemplateLink link = getTemplateNode(parent).getSource(nodeName);
      boolean ignoreable = link.isIgnorable();
      Set<String> contexts = link.getContexts();
      
      TreeSet<String> allProducts = new TreeSet<String>();
      if (!contexts.isEmpty()) {
        allProducts = expandContexts(nodeName, contexts);
      }
      else {
        String realProduct = stringReplace(nodeName, pReplacements);
        allProducts.add(realProduct);
      }
      
      pLog.log
        (Kind.Ops, Level.Finer,
         "The following products were found:\n " + allProducts);

      for (String realProduct : allProducts) {
        if (node.isAcquiredNode(realProduct) || node.isSkippedNode(realProduct)) {
          pLog.log(Kind.Ops, Level.Finer, 
            "Needed source node (" + realProduct + ") was already dealt with.");
          continue;
        }
        if (!nodeExists(realProduct)) {
          if (ignoreable) {
            pLog.log(Kind.Ops, Level.Finer, 
              "Needed source node (" + realProduct + ") does not exist, " +
              "but is being ignored.");
            node.addSkippedNode(realProduct);
            continue;
          }
          else
            throw new PipelineException
              ("Needed source node (" + realProduct + ") does not exist.");
        }
        NodeMod mod = node.getNodeMod();
        if (mod.isLocked()) {
          lockIfNeeded(realProduct);
        }
        else
          checkOutNewer(realProduct, CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
        node.addAcquiredNode(realProduct);
      }
    }
  
    private void
    lockIfNeeded
    (
      String nodeName  
    )
      throws PipelineException
    {
      NodeMod mod = null;
      try {
        mod = pClient.getWorkingVersion(getAuthor(), getView(), nodeName);
      }
      catch (PipelineException ex) {
        //fail silently
      }
      if (mod == null)
        lockLatest(nodeName);
      else {
        VersionID latest = pClient.getCheckedInVersionIDs(nodeName).last();
        VersionID cur = mod.getWorkingID();
        if (cur != null && cur.equals(latest) && mod.isLocked() ) {
          pLog.log(Kind.Bld, Level.Finest, 
            "Needed source node (" + nodeName + ") is not being locked since it is already " +
            "locked to the latest version in the current working area.");
        }
        else
          lockLatest(nodeName);
      }
    }
    
    private TreeSet<String>
    expandContexts
    (
      String product,
      Set<String> contexts
    )
    {
      TreeSet<String> toReturn = new TreeSet<String>();
      
      LinkedList<TreeMap<String, String>> allReplacements = allContextReplacements(contexts);
      for (TreeMap<String, String> replace : allReplacements) {
        toReturn.add(stringReplace(product, replace));
      }

      return toReturn;
    }
    
    
    private void
    validateOptionalBranches
    (
      String nodeName  
    )
      throws PipelineException
    {
      TemplateNode node = getTemplateNode(nodeName);
      if (node.isInTemplate()) {
        /* Only need to deal wpBuildListith optional nodes we haven't hit yet. */
        if (node.isOptional() && !(node.wasAcquired() || node.wasSkipped())) {
          OptionalBranchType type = node.getOptionalBranchType();
          
          /* 
           * Only need to deal if this is optionally a product.  I think this check is 
           * actually unnecessary, since all the BuildOnly nodes should already have 
           * been marked as Ignorable.  But it doesn't hurt to be sure when the test
           * is cheap.
           */
          if (type == OptionalBranchType.AsProduct ||
              type == OptionalBranchType.CheckOut) {
            Set<String> contexts = node.getContexts();
            LinkedList<TreeMap<String, String>> replacements = 
              allContextReplacements(contexts);
            boolean acquired = false;
            for (TreeMap<String, String> replace : replacements) {
              String realNode = stringReplace(node.getNodeName(), replace);
              if (workingVersionExists(realNode)) {
                if (type == OptionalBranchType.AsProduct && 
                    node.getOptionalProductType() == OptionalProductType.LockCurrent) {
                  NodeMod mod = getWorkingVersion(realNode);
                  VersionID id = mod.getWorkingID();
                  pClient.lock(getAuthor(), getView(), realNode, id);
                  pLog.log(Kind.Bld, Level.Fine, 
                    "The optional branch node (" + realNode + ") is available and has been" +
                    "locked to version (" + id +") for use as a product node..");
                }
                else
                  pLog.log(Kind.Bld, Level.Fine, 
                           "The optional branch node (" + realNode + ") is available and " +
                           "can be used as a product.");

                node.addAcquiredNode(realNode);
                acquired = true;
              }
              else {
                pLog.log(Kind.Bld, Level.Fine, 
                  "The optional branch node (" + realNode + ") is not in the working area " +
                  "and will be considered ignorable for this builder invocation.");
                node.addSkippedNode(realNode);
              }
            }
            
            /* 
             * Optimization if the entire node is missing.  Means we won't have to expand 
             * contexts while building downstream nodes.
             */
            if (!acquired)
              node.setSkipped(true);
            else
              node.setAcquired(true);
          }
        }
        
        for (String linkName : node.getSourceName() ) {
          validateOptionalBranches(linkName);
        }
      }
    }

    private static final long serialVersionUID = 8557453695981723176L;
  }
  
  protected
  class FinalizePass
    extends ConstructPass
  {
    public
    FinalizePass()
    {
      super("FinalizePass", "Pass which finalizes all the nodes.");
    }
    
    @Override
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> regenerate = new LinkedList<String>();

      for(FinalizableStage stage : pFinalizableStages) 
        regenerate.add(stage.getNodeName());

      return regenerate;
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log
        (Kind.Ops, Level.Fine, 
         "Running the first finalizing for nodes.");
      for(FinalizableStage stage : pFinalizableStages) 
          stage.finalizeStage();
    }

    private static final long serialVersionUID = -1694813769421240174L;
  }
  
  protected
  class SecondFinalizePass
    extends ConstructPass
  {
    public
    SecondFinalizePass()
    {
      super("SecondFinalizePass", "Pass which touches all the Post Touch nodes.");
    }
    
    @Override
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> regenerate = new LinkedList<String>();

      for(FinalizableStage stage : pSecondaryFinalizableStages) 
        regenerate.add(stage.getNodeName());

      return regenerate;
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log
        (Kind.Ops, Level.Fine, 
         "Running the secondary finalizing for nodes.");
      for(TemplateStage stage : pSecondaryFinalizableStages) 
        stage.secondFinalizeStage();
    }    

    private static final long serialVersionUID = 6576648274737148570L;
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -7290927829922516604L;
 
  public static final String aAllowZeroContexts = "AllowZeroContexts";
  public static final String aInhibitFileCopy   = "InhibitFileCopy";
  public static final String aCheckInLevel      = "CheckInLevel";
  public static final String aCheckInMessage    = "CheckInMessage";
 
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private boolean pInvokedCorrectly;
  
  private boolean pAllowZeroContexts;
  
  private boolean pInhibitCopyFiles;
  
  /*
   * The information that will drive what is being made.
   */
  private TreeMap<String, String> pReplacements;
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  private TreeMap<String, FrameRange> pFrameRanges;
  private TreeMap<String, TemplateExternalData> pExternals;
  private TreeMap<String, Boolean> pOptionalBranches;
  private TreeMap<String, Integer> pOffsets;
  
  private us.temerity.pipeline.VersionID.Level pCheckInLevel;
  private String pCheckInMessage;
  
  /**
   * A list of stages to be finalized during the first finalize pass.
   */
  private ArrayList<FinalizableStage> pFinalizableStages;
  
  /**
   * A list of stages to be finalized during the second finalize pass.
   */
  private ArrayList<TemplateStage> pSecondaryFinalizableStages;
  
  /**
   * The list of template nodes to be instantiated. <p>
   * 
   * Node names are removed from this list during template instantiated as they are 
   * instantiated.
   */
  private TreeSet<String> pBuildList;

  /**
   * The set of root nodes for optional branches that are being checked out during template
   * invocation.
   */
  private TreeSet<String> pOptionalCheckoutList;
 
  /*
   * Variables that define the sort of the template this is and the nodes that define it.
   */
  private String pStartNode;
  private Set<String> pAllNodes;
  private Set<String> pRootNodes;
  private TemplateType pTemplateType;
  
  /*
   * Template manifests.  
   */
  private TemplateDescManifest pDescManifest;
  private TemplateParamManifest pParamManifest;
}
