// $Id: TemplateBuilder.java,v 1.28 2009/12/18 07:22:16 jim Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.plugin.TemplateRangeAnnotation.v2_4_3.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   B U I L D E R                                                        */
/*------------------------------------------------------------------------------------------*/


/**
 * Builder to read a template node network, and create an instance of it. 
 * 
 */
public 
class TemplateBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor for the basic template builder.
   * <p>
   * This is constructed to be as basic as possible.  All you have to pass it is a list of
   * nodes you want to build (as part of the Template Info) and any string replacements you 
   * want and it will do the rest.
   * <p>  
   * There are additional parameters that you can pass in if they are things that you've 
   * already generated as part of constructing the list of nodes to builder.  For example, any
   * sort of node traversal should generate a list of upstream and downstream connections that
   * can be passed into this builder.  This will save time.  If that data is not available,
   * then the template builder will perform a traversal itself and figure that information 
   * out. 
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
   * @param templateInfo
   *   Information about what exactly this builder should generated.  If any of the 
   *   information is missing, the builder will generate it itself.
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
  TemplateBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    TemplateBuildInfo templateInfo,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    TreeMap<String, FrameRange> frameRanges,
    TreeMap<String, ActionOnExistence> aoeModes,
    TreeMap<String, TemplateExternalData> externals,
    TreeMap<String, Boolean> optionalBranches
  ) 
    throws PipelineException
  {
    super("TemplateBuilder",
          "Builder to read a template node network and create an instance of it for a" +
          "particular set of replacement values.",
          mclient, qclient, builderInformation, null);
    
    TreeSet<String> nodesToBuild = templateInfo.getNodesToBuild();
    if (nodesToBuild == null || nodesToBuild.isEmpty())
      throw new PipelineException("Empty list of nodes to build passed to Template Builder.");
    pNodesToBuild = new TreeSet<String>(nodesToBuild);
    
    
    pLog.log(Kind.Ops, Level.Finer, 
      "The list of nodes being built is " + pNodesToBuild);
    pGenerateDependSets = false;
    
    MappedSet<String, String> nodesDependingOnMe = templateInfo.getNodesDependingOnMe();
    pLog.log(Kind.Ops, Level.Finest, 
      "The nodesDependingOnMe data structure passed in has the following values:\n" + 
      nodesDependingOnMe );
    pNodesDependingOnMe = new MappedSet<String, String>();
    if (nodesDependingOnMe == null || nodesDependingOnMe.isEmpty())
      pGenerateDependSets = true;
    else
      pNodesDependingOnMe.putAll(nodesDependingOnMe);
    
    MappedSet<String, String> nodesIDependedOn = templateInfo.getNodesIDependedOn();
    pLog.log(Kind.Ops, Level.Finest, 
      "The nodesIDependOn data structure passed in has the following values:\n" + 
      nodesIDependedOn );
    pNodesIDependedOn = new MappedSet<String, String>();
    if (nodesIDependedOn == null || nodesIDependedOn.isEmpty())
      pGenerateDependSets = true;
    else
      pNodesIDependedOn.putAll(nodesIDependedOn);
    
    // Is this stringent a condition needed for product nodes?  not sure.
    // Seems to be.  Turning it off for now.
    TreeMap<String, Boolean> products = templateInfo.getProductNodes();
    pLog.log(Kind.Ops, Level.Finest, 
      "The products data structure passed in has the following values:\n" + 
      products );
    pProductNodes = new TreeMap<String, Boolean>();
    if (products == null || products.isEmpty())
      pLog.log(Kind.Ops, Level.Fine, "No product nodes defined in this template");
    //pGenerateDependSets = true;
    else
      pProductNodes.putAll(products);
    
    DoubleMap<String, String, TreeSet<String>> productContexts = templateInfo.getProductContexts();
    pLog.log(Kind.Ops, Level.Finest, 
      "The productContexts data structure passed in has the following values:\n" + 
      productContexts );
    pProductContexts = new DoubleMap<String, String, TreeSet<String>>();
    if (productContexts == null) 
      pGenerateDependSets = true;
    else
      pProductContexts.putAll(productContexts);
    
    DoubleMap<String, String, OptionalBranchType> optionalBranchValues = 
      templateInfo.getOptionalBranches();
    pLog.log(Kind.Ops, Level.Finest, 
      "The optionalBranches data structure passed in has the following values:\n" + 
      optionalBranchValues);
    pOptionalBranchValues = new DoubleMap<String, String, OptionalBranchType>();
    if (optionalBranchValues != null)
      pOptionalBranchValues.putAll(optionalBranchValues);
    
    pTemplateInfo = templateInfo;
    
    pReplacements = new TreeMap<String, String>();
    if (stringReplacements != null)
      pReplacements.putAll(stringReplacements);
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

    pLog.log(Kind.Ops, Level.Finest, 
      "The list of frame ranges to apply to this template: " + pFrameRanges);

    pExternals = new TreeMap<String, TemplateExternalData>();
    if (externals != null)
      pExternals.putAll(externals);

    pLog.log(Kind.Ops, Level.Finest, 
      "The list of external file sequences available to this template: " + pExternals);
    
    pOptionalBranches = new TreeMap<String, Boolean>();
    if (optionalBranches != null)
      pOptionalBranches.putAll(optionalBranches);
    
    pLog.log(Kind.Ops, Level.Finest, 
      "The list of optional branchs to apply to this template: " + pOptionalBranches);
    
    pAnnotCache = new TripleMap<String, String, String, TreeMap<String,BaseAnnotation>>();
    
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
    
    addCheckinWhenDoneParam();
    
    addSetupPass(new InformationPass());
    addConstructPass(new BuildPass());
    addConstructPass(new CheckpointPass());
    addConstructPass(new FinalizePass());
    addConstructPass(new SecondFinalizePass());
    
    for (String mode : aoeModes.keySet()) {
      ActionOnExistence aoe = aoeModes.get(mode);
      addAOEMode(mode, aoe);
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
   */
  protected TreeSet<String>
  getContextLinks
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
   * Check if the src node is ignorable from the perspective of the target node.
   * <p>
   * The node is considered ignorable if the target node has a TemplateIgnoreProductAnnotation
   * with the LinkName parameter set to the value of the src node.
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
  
  /**
   * Get the Task Annotations on the given node.  
   *
   * @param name
   *   The name of the node.
   * @return
   *   A TreeMap of Task Annotations indexed by annotation name or 
   *   <code>null</code> if none exists.
   */
  @Override
  protected TreeMap<String, BaseAnnotation>
  getTaskAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = getAnnotations(name);
    TreeMap<String, BaseAnnotation> toReturn = null;
    for(String aname : annots.keySet()) {
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        if (toReturn == null)
          toReturn = new TreeMap<String, BaseAnnotation>();
        BaseAnnotation tannot = annots.get(aname);
        toReturn.put(aname, tannot);
      }
    }
   return toReturn;
  }
  
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
   * Recursively remove nodes from the build list if they are supposed to be ignored.
   * <p>
   * This will remove the given node and any nodes upstream of it that other parts of the
   * template are not using.
   * 
   * @param node
   *   The name of the node.
   *   
   * @param type
   *   The optional branch type that is causing this node to be cleared.
   *   
   * @return
   *   Return the list of downstream nodes that had the cleared node as a source.
   *   
   * @throws PipelineException
   *   If the type is set to AsProduct but some of the upstream nodes being cleared are 
   *   still being used by other nodes in the network.  This state would potentially 
   *   cause frozen staleness, which is not an acceptable state.
   * 
   */
  private TreeSet<String>
  clearNode
  (
    String parent,
    String node,
    OptionalBranchType type
  )
    throws PipelineException
  {
    TreeSet<String> downstream = pNodesDependingOnMe.remove(node);
    if (downstream != null) {
      for (String each : downstream)
        pNodesIDependedOn.remove(each, node);
    }
    TreeSet<String> upstream = pNodesIDependedOn.remove(node);
    if (upstream != null) {
      for (String each : upstream) {
        TreeSet<String> down = pNodesDependingOnMe.get(each); 
        down.remove(node);
        if (down.size() == 0)
          clearNode(parent, each, type);
        else if (type == OptionalBranchType.AsProduct)
          throw new PipelineException
            ("An error was encounted when trying to clear node (" + each + ") as part of an " +
             "optional branch starting at (" + parent +").  The nodes (" + down + ") still " +
              "depend on the node being cleared, which is not acceptable in a situation " +
              "where the Optional Branch Type is AsProduct due to the possibility of getting " +
              "frozen stale nodes.");
      }
    }
    pSkippedNodes.add(node);
    pNodesToBuild.remove(node);
    return downstream;
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
      
      pSkippedNodes = new TreeSet<String>();
      
      pAllowZeroContexts = getBooleanParamValue(new ParamMapping(aAllowZeroContexts));
      pInhibitCopyFiles = getBooleanParamValue(new ParamMapping(aInhibitFileCopy));
      pCheckInMessage = getStringParamValue(new ParamMapping(aCheckInMessage));
      
      {
        int level = getEnumParamIndex(new ParamMapping(aCheckInLevel));
        pCheckInLevel = us.temerity.pipeline.VersionID.Level.values()[level];
      }
      
      if (pGenerateDependSets) {
        pLog.log(Kind.Ops, Level.Finer, 
          "Generating the product and dependency nodes.");

        pProductNodes = new TreeMap<String, Boolean>();
        pProductContexts = new DoubleMap<String, String, TreeSet<String>>();
        pNodesDependingOnMe = new MappedSet<String, String>();
        pNodesIDependedOn = new MappedSet<String, String>();
        pOptionalBranchValues = new DoubleMap<String, String, OptionalBranchType>();
        
        for (String node : pNodesToBuild) {
          NodeStatus stat = pClient.status(new NodeID(getAuthor(), getView(), node), true, 
            DownstreamMode.WorkingOnly);
          NodeMod mod = stat.getLightDetails().getWorkingVersion();
          
          if (mod == null)
            throw new PipelineException
              ("There is no working version of the node (" + node  + ") that is " +
               "part of the template.");
          
          BaseAnnotation annot = mod.getAnnotation("TemplateOptionalBranch");
          if (annot != null) {
            String optionName = (String) annot.getParamValue(aOptionName); 
            OptionalBranchType type = OptionalBranchType.BuildOnly;
            AnnotationParam aparam = annot.getParam(aOptionType);
            if (aparam != null) {
              String value = (String) aparam.getValue();
              type = OptionalBranchType.valueOf(OptionalBranchType.class, value);
            }
            pOptionalBranchValues.put(optionName, node, type);
            pLog.log(Kind.Bld, Level.Finest, 
              "Found an optional branch (" + optionName+ ") for node (" + node + ").");
          }
          for (String src : stat.getSourceNames()) {
            if (pNodesToBuild.contains(src))
              pNodesIDependedOn.put(node, src);
            else {
              TreeSet<String> contexts = getContextLinks(node, src);
              if (!contexts.isEmpty())
                pProductContexts.put(src, node, contexts);
              boolean ignoreable = getIgnorable(node, src);
              if (!pProductNodes.containsKey(src))
                pProductNodes.put(src, ignoreable);
              else if (pProductNodes.get(src) == true && !ignoreable)
                pProductNodes.put(src, ignoreable);
            }
          }
          for (String trgt : stat.getTargetNames()) {
            if (pNodesToBuild.contains(trgt))
              pNodesDependingOnMe.put(node, trgt);
          }
        }
        pLog.log(Kind.Ops, Level.Finest, 
          "The generated nodesDependingOnMe is:\n" + pNodesDependingOnMe);
        pLog.log(Kind.Ops, Level.Finest, 
          "The generated nodesIDependOn is:\n" + pNodesIDependedOn);
        pLog.log(Kind.Ops, Level.Finest, 
          "The generated product nodes is:\n" + pProductNodes);
        pLog.log(Kind.Ops, Level.Finest, 
          "The generated product contexts is:\n" + pProductContexts);
        
      } //if (pGenerateDependSets) 
      
      boolean cleared = false;
      boolean asProduct = false;
      for (Entry<String, Boolean> entry : pOptionalBranches.entrySet()) {
        if (!entry.getValue()) {
          TreeMap<String, OptionalBranchType> taggedNodes = 
            pOptionalBranchValues.get(entry.getKey());
          if (taggedNodes != null) {
            for (Entry<String, OptionalBranchType> entry2: taggedNodes.entrySet()) { 
              String node = entry2.getKey();
              OptionalBranchType type = entry2.getValue();
              TreeSet<String> downstreamNodes = clearNode(node, node, type);
              cleared = true;
              if (type == OptionalBranchType.AsProduct) {
                pProductNodes.put(node, true);
                pSkippedNodes.remove(node);
                for (String target : downstreamNodes) {
                  TreeSet<String> contexts = getContextLinks(target, node);
                  if (!contexts.isEmpty())
                    pProductContexts.put(node, target, contexts);
                }
                asProduct = true;
              }
            } //for (Entry<String, OptionalBranchType> entry2: taggedNodes.entrySet())
          }
        }
      }
      
      if (cleared) {
        pLog.log(Kind.Ops, Level.Finest,
          "\n\nAfter pruning optional branches:");
        pLog.log(Kind.Ops, Level.Finest, 
          "The pruned nodesToBuild is:\n" + pNodesToBuild);
        pLog.log(Kind.Ops, Level.Finest, 
          "The pruned nodesDependingOnMe is:\n" + pNodesDependingOnMe);
        pLog.log(Kind.Ops, Level.Finest, 
          "The pruned nodesIDependOn is:\n" + pNodesIDependedOn);
      }
      if (asProduct) {
        pLog.log(Kind.Ops, Level.Finest,
        "\n\nAfter adding optional branches as product nodes:");
        pLog.log(Kind.Ops, Level.Finest, 
          "The new product nodes are:\n" + pProductNodes);
        pLog.log(Kind.Ops, Level.Finest, 
          "The new product contexts are:\n" + pProductContexts);
      }
      
      pTemplateInfo.setNodesDependingOnMe(pNodesDependingOnMe);
      pTemplateInfo.setNodesIDependedOn(pNodesIDependedOn);
      pTemplateInfo.setProductNodes(pProductNodes);
      pTemplateInfo.setProductContexts(pProductContexts);
      
      pFinalizableStages = new ArrayList<FinalizableStage>();
      pSecondaryFinalizableStages = new ArrayList<FinalizableStage>();
      pCheckpointStages = new ArrayList<FinalizableStage>();
    }
    

    private static final long serialVersionUID = -9082066996485427448L;
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
      acquireProducts();
      
      pIgnoredNodes = new TreeSet<String>();
      
      ArrayList<String> roots = new ArrayList<String>(); 
      MappedSet<Integer, String> orderedRoots = new MappedSet<Integer, String>();
      
      Set<String> aoeModes = getBuilderInformation().getAOEModes();
      
      while (!pNodesToBuild.isEmpty() ) {
        String toBuild = findNodeToBuild();
        pLog.log(Kind.Ops, Level.Fine, 
          "Template Node: " + toBuild);
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), toBuild);
        TreeMap<String, BaseAnnotation> annots = getAnnotations(toBuild);
        TreeSet<String> contexts = new TreeSet<String>();
        TreeSet<String> ignoreableProducts = new TreeSet<String>();
        TreeMap<String, ActionOnExistence> aoeOverrides = new TreeMap<String, ActionOnExistence>();
        boolean checkpoint = false;
        boolean vouchable = false;
        String external = null;
        for (String aName : annots.keySet()) {
          BaseAnnotation annot = annots.get(aName);
          if (aName.startsWith("TemplateContext") && 
              !aName.startsWith("TemplateContextLink")) {
            String contextName = (String) annot.getParamValue(aContextName);
            pLog.log(Kind.Bld, Level.Finest, 
              "Found the context (" + contextName + ").");
            contexts.add(contextName);
          }
          else if (aName.startsWith("TemplateIgnoreProduct")) {
            String linkName = (String) annot.getParamValue(aLinkName);
            pLog.log(Kind.Bld, Level.Finest, 
              "Found an ignorable product (" + linkName + ").");
            ignoreableProducts.add(linkName);
          }
          else if (aName.startsWith("TemplateAOE")) {
            String mode = (String) annot.getParamValue(aModeName);
            if (aoeModes.contains(mode)) {
              ActionOnExistence aoe = 
                ActionOnExistence.valueFromString(
                  (String) annot.getParamValue(aActionOnExistence));
              pLog.log(Kind.Bld, Level.Finest, 
                "Found an AOE override (" + aoe + ") for mode (" + mode + ").");
              aoeOverrides.put(mode, aoe);
            }
          }
          else if (aName.equals("TemplateCheckpoint")) {
            checkpoint = true;
            pLog.log(Kind.Bld, Level.Finest, 
              "Found a Checkpoint annotation.");
          }
          else if (aName.equals("TemplateVouchable")) {
            vouchable = true;
            pLog.log(Kind.Bld, Level.Finest, 
            "Found a Vouchable annotation.");
          }
          else if (aName.equals("TemplateExternal")) {
            external = (String) annot.getParamValue(aExternalName);
           }
        }
        
        Integer order = null;
        {
          BaseAnnotation annot = annots.get("TemplateOrder");
          if (annot != null) {
            order = (Integer) annot.getParamValue("Order");
            pLog.log(Kind.Bld, Level.Finest, 
              "Found a template order of (" + order + ").");
          }
        }
        
        String conditionalBuild = null;
        {
          BaseAnnotation annot = annots.get("TemplateConditionalBuild");
          if (annot != null) {
            conditionalBuild = (String) annot.getParamValue(aConditionName);
            pLog.log(Kind.Bld, Level.Finest, 
              "Found a conditional build dependency on (" + conditionalBuild + ").");
          }
        }
        
        FrameRange range = null;
        {
          BaseAnnotation annot = annots.get("TemplateRange");
          if (annot != null) {
            String rangeName = (String) annot.getParamValue("RangeName");
            range = pFrameRanges.get(rangeName);
            pLog.log(Kind.Bld, Level.Finest, 
              "Found a template frame range (" + range + ").");
          }
        }
        
        TreeSet<String> nodesMade = new TreeSet<String>();
        TreeSet<String> allNodes  = new TreeSet<String>();
        if (contexts.size() == 0) { //no contexts, just do a straight build
          makeNode(mod, pReplacements, pContexts, range, ignoreableProducts, 
                   conditionalBuild, checkpoint, aoeOverrides, external, nodesMade, allNodes);
        }
        else { //uh-oh, there are contexts!
          contextLoop(toBuild, mod, contexts, pReplacements, pContexts, 
                      range, ignoreableProducts, conditionalBuild, checkpoint, 
                      aoeOverrides, external, nodesMade, allNodes);
        }
        
        if (vouchable) {
          for (String node : allNodes)
            addToVouchableList(node);
        }

        pNodesToBuild.remove(toBuild);
        TreeSet<String> targets = pNodesDependingOnMe.get(toBuild);
        if (targets != null && !targets.isEmpty()) {
          for (String target : targets ) {
            TreeSet<String> temp = pNodesIDependedOn.get(target);
            temp.remove(toBuild);
            if (temp.isEmpty())
              pNodesIDependedOn.remove(target);
            else
              pNodesIDependedOn.put(target, temp);
          }
          if (order != null)
            for (String node : allNodes)
              orderedRoots.put(order, node);
        } 
        else { // we've got a node that nothing depends on, must be a root.
          if (order == null)
            roots.addAll(allNodes);
          else {
            for (String node : allNodes)
              orderedRoots.put(order, node);
          }
        }
      } // while (!pNodesToBuild.isEmpty() )
      for (Integer order : orderedRoots.keySet()) {
        for (String root : orderedRoots.get(order)) {
          pLog.log(Kind.Bld, Level.Finer, 
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
      String toBuild,
      NodeMod mod,
      TreeSet<String> contextList,
      TreeMap<String, String> replace,
      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts, 
      FrameRange range,
      TreeSet<String> ignorableProducts,
      String conditionalBuild, 
      boolean checkpoint, 
      TreeMap<String,ActionOnExistence> aoeOverrides, 
      String external, 
      TreeSet<String> nodesMade, 
      TreeSet<String> allNodes
    )
      throws PipelineException
    {
      String currentContext = contextList.pollFirst();
      ArrayList<TreeMap<String, String>> values = contexts.get(currentContext);
      
      if (values == null || values.isEmpty()) {
        if (pAllowZeroContexts) {
          pLog.logAndFlush
            (Kind.Ops, Level.Warning, 
             "The context (" + currentContext + ") specified on (" + toBuild + ") has no " + 
             "values defined for it.");
          TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
          TreeMap<String, ArrayList<TreeMap<String, String>>> newMaps = 
            new TreeMap<String, ArrayList<TreeMap<String,String>>>(contexts);
          if (contextList.isEmpty()) {  //bottom of the recursion
            makeNode(mod, newReplace, newMaps, range, ignorableProducts, 
                     conditionalBuild, checkpoint, aoeOverrides, external, 
                     nodesMade, allNodes);
          }
          else {
            contextLoop
            (toBuild, mod, new TreeSet<String>(contextList), 
              newReplace, newMaps, range, ignorableProducts, conditionalBuild, 
              checkpoint, aoeOverrides, external, nodesMade, allNodes);
          }
          return;
        }
        else
          throw new PipelineException
            ("The context (" + currentContext + ") specified on (" + toBuild + ") has " +
             "no values defined for it.");
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
          makeNode(mod, newReplace, newMaps, range, ignorableProducts,
                   conditionalBuild, checkpoint, aoeOverrides, external, 
                   nodesMade, allNodes);
        }
        else {
          contextLoop
            (toBuild, mod, new TreeSet<String>(contextList), 
             newReplace, newMaps, range, ignorableProducts, conditionalBuild, 
             checkpoint, aoeOverrides, external, nodesMade, allNodes);
        }
      }
    }

    private void 
    makeNode
    (
      NodeMod mod,
      TreeMap<String, String> replace,
      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
      FrameRange range,
      TreeSet<String> ignorableProducts, 
      String conditionalBuild, 
      boolean checkpoint, 
      TreeMap<String,ActionOnExistence> aoeOverrides, 
      String external, 
      TreeSet<String> nodesMade, 
      TreeSet<String> allNodes
    )
      throws PipelineException
    {
      String nodeName = stringReplace(mod.getName(), replace);
      
      if (nodeName.equals(mod.getName()))
        throw new PipelineException
          ("The name of the node in the template (" + nodeName + ") is the same as the " +
           "instantiated node.  A template cannot build over itself.  All nodes in a template " +
           "must contain at least one string replacement.");
      
      if (conditionalBuild != null) {
        String check = stringReplace(conditionalBuild, replace);
        if (!nodeExists(check)) {
          pLog.logAndFlush
            (Kind.Ops, Level.Fine, 
             "Not building (" + nodeName + ") because conditional node (" + check + ") " +
             "does not exist");
          pIgnoredNodes.add(nodeName);
          return;
        }
      }
      
      for (String mode : aoeOverrides.keySet()) {
        addAOEOverride(mode, nodeName, aoeOverrides.get(mode));
      }
      
       
      TemplateExternalData exD = null;
      if (external != null) {
        String newExternalName = stringReplace(external, replace);
        exD = pExternals.get(newExternalName);
        if (exD == null)
          throw new PipelineException
            ("An external sequence annotation with ExternalName (" + newExternalName + ") " +
             "was found but the Template does not specify a value for that external sequence.");        
      }
      
        
      
      TemplateStage stage = 
        TemplateStage.getTemplateStage
          (mod, getStageInformation(), pContext, pClient, 
           pTemplateInfo, replace, contexts, range,
           pSkippedNodes, pIgnoredNodes, ignorableProducts, 
           pInhibitCopyFiles, pAllowZeroContexts, exD, pAnnotCache);

      allNodes.add(stage.getNodeName());
      
      if (stage.build()) {
        if (stage.needsFinalization()) {
          if (checkpoint)
            pCheckpointStages.add(stage);
          else
            pFinalizableStages.add(stage);
        }
        if (stage.needsSecondFinalization())
          pSecondaryFinalizableStages.add(stage);
        nodesMade.add(stage.getNodeName());
        pLog.logAndFlush
        (Kind.Ops, Level.Fine, 
         "FINISHED Building Node.");
      }
    }
    
    private String
    findNodeToBuild()
      throws PipelineException
    {
      pLog.log(Kind.Ops, Level.Finer, "Search for a node to build");
      for (String node : pNodesToBuild) {
        TreeSet<String> set = pNodesIDependedOn.get(node);
        if ( set == null)
          return node;
        else
          pLog.log
            (Kind.Ops, Level.Finest, 
             "The node (" + node + ") must wait for the following nodes to be built: \n" + 
             set);
      }
      throw new PipelineException
        ("There are no nodes which is is possible to construct.  " +
         "This state should be imposible with any node network I can think of.");
    }
    
    private void
    acquireProducts()
     throws PipelineException
    {
      for (String product : pProductNodes.keySet()) {
        pLog.log
          (Kind.Ops, Level.Fine, 
           "Searching for product nodes based on the template node (" + product + ")");
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), product);
        TreeSet<String> allProducts = new TreeSet<String>();
        if (pProductContexts.containsKey(product)) {
          TreeMap<String,TreeSet<String>> allContexts = pProductContexts.get(product);
          for (String target : allContexts.keySet()) {
            TreeSet<String> contexts = new TreeSet<String>(allContexts.get(target));
            expandContexts(product, contexts, pReplacements, allProducts);
          }
        }
        else {
          String realProduct = stringReplace(product, pReplacements);
          allProducts.add(realProduct);
        }
        
        pLog.log(Kind.Ops, Level.Finer,
          "The following products were found:\n " + allProducts);
        
        boolean ignorable = pProductNodes.get(product);
        
        for (String realProduct : allProducts) {
        
          if (!nodeExists(realProduct)) {
            if (ignorable) {
              pLog.log(Kind.Ops, Level.Finer, 
                "Needed source node (" + realProduct + ") does not exist, " +
              "but is being ignored.");
              continue;
            }
            else
              throw new PipelineException
                ("Needed source node (" + realProduct + ") does not exist.");
          }
          if (mod.isLocked()) {
            lockIfNeeded(realProduct);
          }
          else
            frozenStomp(realProduct);
        }
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

    private void 
    expandContexts
    (
      String product,
      TreeSet<String> contexts,
      TreeMap<String,String> replacements, 
      TreeSet<String> allProducts
    )
      throws PipelineException
    {
      String currentContext = contexts.pollFirst();
      ArrayList<TreeMap<String, String>> values = pContexts.get(currentContext);
      
      if (values == null || values.isEmpty()) {
        if (pAllowZeroContexts) {
          pLog.logAndFlush
          (Kind.Ops, Level.Warning, 
           "The context (" + currentContext + ") specified for the product " + 
           "(" + product + ") has no values defined for it.");
          TreeMap<String, String> newReplace = new TreeMap<String, String>(replacements);
          
          if (contexts.isEmpty()) {  //bottom of the recursion
            allProducts.add(stringReplace(product, newReplace));
          }
          else {
            expandContexts(product, new TreeSet<String>(contexts), newReplace, allProducts);
          }
          return;
        }
        else
          throw new PipelineException
            ("The context (" + currentContext + ") specified for the product " + 
             "(" + product + ") has no values defined for it.");
      }
      
      for (TreeMap<String, String> contextEntry : values) {
        TreeMap<String, String> newReplace = new TreeMap<String, String>(replacements);
        newReplace.putAll(contextEntry);
       
        if (contexts.isEmpty()) {  //bottom of the recursion
          allProducts.add(stringReplace(product, newReplace));
        }
        else {
          expandContexts(product, new TreeSet<String>(contexts), newReplace, allProducts);
        }
      }
    }

    private static final long serialVersionUID = -8757997441040344237L;
  }

  protected
  class CheckpointPass
    extends ConstructPass
  {
    public
    CheckpointPass()
    {
      super("CheckpointPass", "Pass with finalizes the nodes tagged as checkpoints.");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      for (FinalizableStage stage : pCheckpointStages) {
        
        String nodeName = stage.getNodeName();
        LinkedList<String> toQueue = new LinkedList<String>();
        toQueue.add(nodeName);
        LinkedList<QueueJobGroup> jobs = queueNodes(toQueue);
        if (jobs.size() > 0) {
          waitForJobs(jobs);
          /* Sleep for 3 seconds to give nfs caching a chance to catch up */
          try {
            Thread.sleep(3000);
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("The execution thread was interrupted while waiting for jobs to complete.\n" + 
               Exceptions.getFullMessage(ex));
          }
        }
        if (!areAllFinished(toQueue)) {
          TreeSet<Long> jobList = new TreeSet<Long>();
          for (QueueJobGroup group : jobs) {
            for (Long id : group.getAllJobIDs()) {
              jobList.add(id);
            }
          }
          pQueue.killJobs(jobList);
          throw new PipelineException
            ("Errors were encountered when trying to queue the node (" + nodeName + ") " +
             "during the checkpoint finalization.  Execution has been halted and all " +
             "associated jobs have been killed.");
        }
        stage.finalizeStage();
      }
    }
    
    private static final long serialVersionUID = -1044782209387738110L;
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
      for(FinalizableStage stage : pFinalizableStages) 
          stage.finalizeStage();
    }    
    private static final long serialVersionUID = -2948200152727649296L;
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
      for(FinalizableStage stage : pSecondaryFinalizableStages) 
        stage.finalizeStage();
    }    

    private static final long serialVersionUID = 6576648274737148570L;
  }  

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3515581617122035575L;

  public static final String aContextName   = "ContextName";
  public static final String aLinkName      = "LinkName";
  public static final String aConditionName = "ConditionName";
  public static final String aModeName      = "ModeName";
  public static final String aOptionName    = "OptionName";
  public static final String aOptionType    = "OptionType";
  public static final String aExternalName  = "ExternalName";
  
  public static final String aAllowZeroContexts = "AllowZeroContexts";
  public static final String aInhibitFileCopy   = "InhibitFileCopy";
  
  public static final String aCheckInLevel   = "CheckInLevel";
  public static final String aCheckInMessage = "CheckInMessage";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  private boolean pGenerateDependSets;
  
  private boolean pAllowZeroContexts;
  
  private boolean pInhibitCopyFiles;
  
  private TreeMap<String, String> pReplacements;
  
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  
  private TripleMap<String, String, String, TreeMap<String, BaseAnnotation>> pAnnotCache;
  
  private TreeMap<String, Boolean> pOptionalBranches;
  
  private TemplateBuildInfo pTemplateInfo;
  
  private ArrayList<FinalizableStage> pCheckpointStages;
  
  private ArrayList<FinalizableStage> pFinalizableStages;
  private ArrayList<FinalizableStage> pSecondaryFinalizableStages;
  
  private TreeSet<String> pNodesToBuild;
  
  private MappedSet<String, String> pNodesIDependedOn;
  private MappedSet<String, String> pNodesDependingOnMe;
  private DoubleMap<String, String, OptionalBranchType> pOptionalBranchValues;
  private TreeMap<String, Boolean> pProductNodes;
  private DoubleMap<String, String, TreeSet<String>> pProductContexts;
  private TreeMap<String, FrameRange> pFrameRanges;
  private TreeMap<String, TemplateExternalData> pExternals;
  
 
  /**
   * The set of nodes in the template which were skipped, due to conditionals.
   * <p>
   * These node names match the actual template node names, not the names in the 
   * instantiated network. 
   */
  private TreeSet<String> pSkippedNodes;
  
  /**
   * The set of the nodes in the instantiated network which have not been built. due
   * to conditionals.
   * <p>
   * These are node names which have been fully expanded with all context replacements.
   */
  private TreeSet<String> pIgnoredNodes;
  
  private us.temerity.pipeline.VersionID.Level pCheckInLevel;
  private String pCheckInMessage;
}
