// $Id: TemplateNode.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateCollection.v2_4_12;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   N O D E                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A single node in a template with all the flags that control template execution included.
 */
public 
class TemplateNode
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor.
   * 
   * @param mod
   *   The working version of the template node.
   *   
   * @param inTemplate
   *   Whether the node is part of the template or a product node from outside the template.
   */
  public
  TemplateNode
  (
    NodeMod mod,
    boolean inTemplate,
    LogMgr log
  )
    throws PipelineException
  {
    pNodeMod = mod;
    pNodeName = mod.getName();
    pInTemplate = inTemplate;
    
    pUpstreamLinks = new TreeMap<String, TemplateLink>();
    pDownstreamLinks = new TreeSet<String>();
    
    pAOEModes = new TreeMap<String, ActionOnExistence>();
    pContexts = new TreeSet<String>();
    pSecContexts = new TreeSet<String>();
    pSecContextBySeq = new MappedSet<String, String>();
    pNodesToUnlink = new TreeSet<String>();
    
    pBuiltNodes = new TreeSet<String>();
    pAcquiredNodes = new TreeSet<String>();
    pSkippedNodes = new TreeSet<String>();
    pProductTargets = new TreeSet<String>();
    
    pOrder = null;
    pConditionalBuild = null;
    pLinkSync = null;
    
    MappedSet<String, String> contextLinkValues = new MappedSet<String, String>();
    TreeSet<String> ignorableProductValues = new TreeSet<String>();
    
    Set<String> sourceNames = mod.getSourceNames();
    
    log.log(Kind.Bld, Level.Finest, 
            "Acquiring template information for node (" + pNodeName + ")");
    
    if (inTemplate) {
      TreeMap<String, BaseAnnotation> annots = mod.getAnnotations();
      for (Entry<String, BaseAnnotation> entry : annots.entrySet()) {
        String aName = entry.getKey();
        BaseAnnotation annot = entry.getValue();
        if (aName.equals("TemplateSettings")) {
          pPostRemoveAction = (Boolean) annot.getParamValue(aPostRemoveAction);
          if (pPostRemoveAction)
            log.log(Kind.Bld, Level.Finest, 
                   "Node has a PostRemoveAction setting.");
          
          pPostDisableAction = (Boolean) annot.getParamValue(aPostDisableAction);
          if (pPostDisableAction)
            log.log(Kind.Bld, Level.Finest, 
                   "Node has a PostDisableAction setting.");
          
          pPreEnableAction = (Boolean) annot.getParamValue(aPreEnableAction);
          if (pPreEnableAction)
            log.log(Kind.Bld, Level.Finest, 
                   "Node has a PreEnableAction setting.");
          
          pCloneFiles = (Boolean) annot.getParamValue(aCloneFiles);
          if (pCloneFiles)
            log.log(Kind.Bld, Level.Finest, 
                   "Node has a Clone files setting.");
          
          pUnlinkAll = (Boolean) annot.getParamValue(aUnlinkAll);
          if (pUnlinkAll)
            log.log(Kind.Bld, Level.Finest, 
                   "Node has an UnlinkAll setting.");
          
          pTouchFiles = (Boolean) annot.getParamValue(aTouchFiles);
          if (pTouchFiles)
            log.log(Kind.Bld, Level.Finest, 
                   "Node has a TouchFile setting.");
    
          if (annot.hasParam(aVouchable)) {
            pVouchable = (Boolean) annot.getParamValue(aVouchable);
            if (pVouchable)
              log.log(Kind.Bld, Level.Finest, "Node has a Vouchable setting.");
          }
          else
            log.log(Kind.Bld, Level.Warning, 
                   "Template Setting node is missing a Vouchable param. (" + pNodeName +")");

          if (annot.hasParam(aIntermediate)) {
            pIntermediate = (Boolean) annot.getParamValue(aIntermediate);
            if (pIntermediate)
              log.log(Kind.Bld, Level.Finest, "Node has an Intermediate setting.");
          }
          else
            log.log(Kind.Bld, Level.Warning, 
                   "Template Setting node is missing an Intermediate param. " +
                   "(" + pNodeName + ")");
          
          if (annot.hasParam(aModifyFiles)) {
            pModifyFiles = (Boolean) annot.getParamValue(aModifyFiles);
            if (pModifyFiles)
              log.log(Kind.Bld, Level.Finest, "Node has a Modify Files setting.");
          }
          else
            log.log(Kind.Bld, Level.Warning, 
                   "Template Setting node is missing a Modify Files param. " +
                   "(" + pNodeName + ")");

        }
        else if (aName.startsWith("TemplateUnlink" )) {
          String unlink = (String) annot.getParamValue(aLinkName);
          checkLink(unlink, sourceNames, "TemplateUnlink");
          pNodesToUnlink.add(unlink);
          log.log(Kind.Bld, Level.Finest, 
                  "Node has an Unlink annotation pointing to (" + unlink +").");
        }
        else if (aName.equals("TemplateOptionalBranch")) {
          pOptionalBranch = (String) annot.getParamValue(aOptionName); 
          pOptionalBranchType = OptionalBranchType.BuildOnly;
          pOptionalProductType = OptionalProductType.UseProduct;
          
          if (annot.hasParam(aOptionType)) {
            String value = (String) annot.getParamValue(aOptionType);
            pOptionalBranchType = 
              OptionalBranchType.valueOf(OptionalBranchType.class, value);
          }
          
          if (annot.hasParam(aProductType)) {
            String value = (String) annot.getParamValue(aProductType);
            pOptionalProductType = 
              OptionalProductType.valueOf(OptionalProductType.class, value);
          }
          
          
          log.log(Kind.Bld, Level.Finest, 
                 "Node is part of an Optional Branch named (" + pOptionalBranch +") with a " +
                 "type of (" + pOptionalBranchType +") and a product type of " +
                 "(" + pOptionalProductType +").");
        }
        else if (aName.startsWith("TemplateContextLink")) {
          String aSrc = (String) annot.getParamValue(aLinkName);
          String context = (String) annot.getParamValue(aContextName);
          checkLink(aSrc, sourceNames, "TemplateContextLink");
          contextLinkValues.put(aSrc, context);
          log.log(Kind.Bld, Level.Finest, 
            "Node has a context link for (" + aSrc + ") with a value of (" + context + ")");
        }
        else if (aName.startsWith("TemplateContext")) {
          String context = (String) annot.getParamValue(aContextName); 
          pContexts.add(context);
          log.log(Kind.Bld, Level.Finest, 
            "Node has a context with a value of (" + context + ")");
        }
        else if (aName.startsWith("TemplateSecContext")) {
          handleSecContext(annot, log);
        }
        else if (aName.startsWith("TemplateIgnoreProduct")) {
          String ignore = (String) annot.getParamValue(aLinkName);
          checkLink(ignore, sourceNames, "TemplateIgnoreProduct");
          ignorableProductValues.add(ignore);
          log.log(Kind.Bld, Level.Finest, 
            "Node can ignore the product (" + ignore + ")");
        }
        else if (aName.startsWith("TemplateAOE")) {
          String mode = (String) annot.getParamValue(aModeName);
          ActionOnExistence aoe = 
            ActionOnExistence.valueFromString(
              (String) annot.getParamValue(aActionOnExistence));
          pAOEModes.put(mode, aoe);
          log.log(Kind.Bld, Level.Finest, 
            "Node has an AOE mode override named (" + mode + ") with value (" + aoe +")");
        }
        else if (aName.equals("TemplateVouchable")) {
          log.log(Kind.Bld, Level.Severe, 
                  "The template vouchable annotation is no longer used in templates " +
                  "v2.4.10.  Use the vouchable setting on the Template Settings node " +
                  "instead. (" + pNodeName +")");
        }
        else if (aName.equals("TemplateCheckpoint")) {
          log.log(Kind.Bld, Level.Severe, 
                  "The template checkpoint annotation is no longer used in templates " +
                  "v2.4.10.  Use the vouchable setting on the Template Settings node " +
                  "instead. (" + pNodeName +")");
        }
        else if (aName.equals("TemplateExternal")) {
          pExternalName = (String) annot.getParamValue(aExternalName);
          log.log(Kind.Bld, Level.Finest, 
            "Node has an external sequence named (" + pExternalName + ")");
        }
        else if (aName.equals("TemplateOrder")) {
          pOrder = (Integer) annot.getParamValue("Order");
          log.log(Kind.Bld, Level.Finest, 
            "Node has an order of (" + pOrder + ")");
        }
        else if (aName.equals("TemplateConditionalBuild")) {
          pConditionalBuild = (String) annot.getParamValue(aConditionName);
          log.log(Kind.Bld, Level.Finest, 
            "Node is conditional on (" + pConditionalBuild +")");
        }
        else if (aName.equals("TemplateLinkSync")) {
          pLinkSync = (String) annot.getParamValue(aLinkName);
          log.log(Kind.Bld, Level.Finest, 
            "Node is set to sync links off (" + pLinkSync +")");
        }
        else if (aName.equals("TemplateRange")){
          pFrameRangeName = (String) annot.getParamValue("RangeName");
          log.log(Kind.Bld, Level.Finest, 
            "Node has a frame range named (" + pFrameRangeName + ")");
        }
      } //for (Entry<String, BaseAnnotation> entry : annots.entrySet()) 
      
      for (String linkName : sourceNames) {
        boolean ignorable = ignorableProductValues.contains(linkName);
        TreeSet<String> contextLinks = contextLinkValues.get(linkName);
        TemplateLink link = new TemplateLink(linkName, contextLinks, ignorable);
        pUpstreamLinks.put(linkName, link);
      }
    }
    else {
      log.log(Kind.Bld, Level.Finest, 
        "Node is a product node.");
      TreeMap<String, BaseAnnotation> annots = mod.getAnnotations();
      for (Entry<String, BaseAnnotation> entry : annots.entrySet()) {
        String aName = entry.getKey();
        BaseAnnotation annot = entry.getValue();
        if (aName.startsWith("TemplateSecContext")) {
          handleSecContext(annot, log);
        }
      }
    }

    log.log(Kind.Bld, Level.Finest, 
            "FINISHED acquiring node info\n.");
    
    pIsOptional = false;
  }


  /**
   * Helper method to handle secondary sequence contexts
   * <p>
   * In a separate method because this is done in two places, for products and template 
   * nodes. 
   * 
   * @param annot
   *   The secondary sequence context annotation.
   *    
   * @param log
   *   The instance of the log manager to write to the log with. 
   *   
   * @throws PipelineException
   */
  private void 
  handleSecContext
  (
    BaseAnnotation annot,
    LogMgr log
  )
    throws PipelineException
  {
    String context = (String) annot.getParamValue(aContextName);
    String seq = (String) annot.getParamValue(aSeqName);
    pSecContexts.add(context);
    if (seq == null || seq.equals(""))
      seq = sALL;
    pSecContextBySeq.put(seq, context);
    log.log(Kind.Bld, Level.Finest, 
      "Node has a secondary context with a value of (" + context + ") that is valid " +
      "on (" + seq + ")");
  }
  
  private void
  checkLink
  (
    String linkName,
    Set<String> sources,
    String annotType
  )
    throws PipelineException
  {
    if (!sources.contains(linkName))
      throw new PipelineException
        ("The node has a " + annotType +" annotation pointing to the node " +
         "(" + linkName + ").  This node does not have a source node with that name");
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T E                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Has this node already been handled by template execution.
   * <p>
   * This could be a node in the template that was built, skipped, or checked-out.
   */
  public boolean
  isDone()
  {
    if (pInTemplate) {
     if (pBuilt || pSkipped || pAcquired)
       return true;
    }
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Is this node a root?
   */
  public final boolean 
  isRoot()
  {
    if (pOrder != null)
      return true;
    return pIsRoot;
  }

  /**
   * Set whether this node is a root.
   */
  public final void 
  setRoot
  (
    boolean isRoot
  )
  {
    pIsRoot = isRoot;
  }

  /**
   * Was this node built as part of template instantiation.
   */
  public final boolean 
  wasBuilt()
  {
    return pBuilt;
  }

  /**
   * Set whether the node was built as part of template instantiation.
   */
  public final void 
  setBuilt
  (
    boolean built
  )
  {
    pBuilt = built;
  }

  /**
   * Was the node skipped during template instantiation.
   */
  public final boolean 
  wasSkipped()
  {
    return pSkipped;
  }
  
  /**
   * Set whether the node was skipped during template instantiation.
   */
  public final void 
  setSkipped
  (
    boolean skipped
  )
  {
    pSkipped = skipped;
  }
  
  /**
   * Was the node checked-out during template instantiation.
   */
  public final boolean 
  wasAcquired()
  {
    return pAcquired;
  }
  
  /**
   * Set whether the node was checked-out during template instantiation.
   */
  public final void 
  setAcquired
  (
    boolean acquired
  )
  {
    pAcquired = acquired;
  }

  /**
   * Get the node name.
   */
  public final String 
  getNodeName()
  {
    return pNodeName;
  }

  /**
   * Get the working version of the template node.
   */
  public final NodeMod 
  getNodeMod()
  {
    return pNodeMod;
  }

  /**
   * Get the contexts on this node.
   */
  public final Set<String> 
  getContexts()
  {
    return Collections.unmodifiableSet(pContexts);
  }
  
  /**
   * Get a list of all the secondary contexts on this node.
   */
  public final Set<String> 
  getSecondaryContexts()
  {
    return Collections.unmodifiableSet(pSecContexts);
  }
  
  /**
   * Get a set of the string representations of the file patterns of the secondary
   * sequences which have contexts associated with them. 
   */
  public final Set<String>
  getSecSeqsWithContexts()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    for (String s : pSecContextBySeq.keySet()) {
      if (!s.equals(sALL))
        toReturn.add(s);
    }
    return toReturn;
  }
  
  /**
   * Get the set of contexts that are associated with the given string representation of 
   * a secondary file pattern.
   * 
   * @param fpat
   *   The String representation of the file pattern of the secondary sequence.
   */
  public final Set<String>
  getSecondaryContextsForSequence
  (
    String fpat  
  )
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    {
      TreeSet<String> part = pSecContextBySeq.get(fpat);
      if (part != null)
        toReturn.addAll(part);
    }
    {
      TreeSet<String> part = pSecContextBySeq.get(sALL);
      if (part != null)
        toReturn.addAll(part);
    }
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I N K   A C C E S S                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the upstream links of this node.
   */
  public final Map<String, TemplateLink> 
  getSources()
  {
    return Collections.unmodifiableMap(pUpstreamLinks);
  }
  
  public final TemplateLink
  getSource
  (
    String linkName  
  )
  {
    return pUpstreamLinks.get(linkName);
  }
  
  public final Set<String> 
  getSourceName()
  {
    return Collections.unmodifiableSet(pUpstreamLinks.keySet());
  }
  
  public final boolean
  hasUpstreamLinks()
  {
    return !pUpstreamLinks.isEmpty();
  }

  /**
   * Get the downstream links of this node.
   */
  public final Set<String> 
  getDownstreamLinks()
  {
    return Collections.unmodifiableSet(pDownstreamLinks);
  }

  public final void
  addDownstreamLink
  (
    String downstream  
  )
  {
    pDownstreamLinks.add(downstream);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N   S E T T I N G S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the custom ActionOnExistence modes.
   */
  public final Map<String, ActionOnExistence> 
  getAOEModes()
  {
    return Collections.unmodifiableMap(pAOEModes);
  }
  
  /**
   * Is the node part of the template.
   */
  public final boolean 
  isInTemplate()
  {
    return pInTemplate;
  }

  /**
   * Get the list of nodes which should be unlinked after being built. 
   */
  public final Set<String> 
  getNodesToUnlink()
  {
    return Collections.unmodifiableSet(pNodesToUnlink);
  }
  
  /**
   * Is the clone files option set. 
   */
  public final boolean 
  cloneFiles()
  {
    return pCloneFiles;
  }
  
  /**
   * Is the touch files option set.
   */
  public final boolean 
  touchFiles()
  {
    return pTouchFiles;
  }
  
  /**
   * Is the modify files option set.
   */
  public final boolean
  modifyFiles()
  {
    return pModifyFiles;
  }
  
  /**
   * Is this node vouchable during queue stages.
   */
  public final boolean 
  isVouchable()
  {
    return pVouchable;
  }
  
  /**
   * Should this node be made an intermediate node.
   */
  public final boolean 
  isIntermediate()
  {
    return pIntermediate;
  }
  
  /**
   * Is the pre-enable action option set.
   */
  public final boolean 
  preEnableAction()
  {
    return pPreEnableAction;
  }

  /**
   *  Is the unlink all option set. 
   */
  public final boolean 
  unlinkAll()
  {
    return pUnlinkAll;
  }
  
  /**
   * Is the post remove action option set.
   */
  public final boolean 
  postRemoveAction()
  {
    return pPostRemoveAction;
  }
  
  /**
   * Is the post disable action option set.
   */
  public final boolean 
  postDisableAction()
  {
    return pPostDisableAction;
  }

  /**
   * Get the name of this optional branch or <code>null</code> if this is 
   * not an optional branch. 
   */
  public final String 
  getOptionalBranch()
  {
    return pOptionalBranch;
  }

  /**
   * Get the type of the optional branch.
   */
  public final OptionalBranchType 
  getOptionalBranchType()
  {
    return pOptionalBranchType;
  }
  
  /**
   * Get the product type of the optional branch.
   */
  public final OptionalProductType 
  getOptionalProductType()
  {
    return pOptionalProductType;
  }
  
  /**
   * Get the name of the external or <code>null</code> if there is no external.
   */
  public final String 
  getExternalName()
  {
    return pExternalName;
  }
  
  /**
   * Get the order or <code>null</code> if the node does not have an order.
   */
  public final Integer 
  getOrder()
  {
    return pOrder;
  }
  
  /**
   * Get the name of the node that this node is conditional on or <code>null</code> if there 
   * is no conditional build.
   */
  public final String 
  getConditionalBuild()
  {
    return pConditionalBuild;
  }
  
  /**
   * Get the name of the node that this node will sync its links off of or <code>null</code> 
   * if there isn't a link sync. 
   */
  public final String
  getLinkSync()
  {
    return pLinkSync;
  }
  
  /**
   * Get the name of the frame range or <code>null</code> if there is no frame range.
   */
  public final String 
  getFrameRangeName()
  {
    return pFrameRangeName;
  }
  
  /**
   * Set whether the optional branch in this template is activated during this template run.
   */
  public final void
  setIsOptional
  (
    boolean setting
  )
  {
    pIsOptional = setting; 
  }

  /**
   * Get the optional branch setting in the template as it applies to this node's state.
   * <p>
   * This means that this value is actually going to be opposite of the optional branch 
   * setting in the builder itself.  If an optional branch is set to <code>true</code> it 
   * means that the optional part of the network is being built, which would cause this  
   * function to return <code>false</code>.  If the optional branch was set to 
   * <code>false</code>, then this part of the network would not be considered for 
   * construction, so this would return <code>true</code>. 
   * 
   * @return
   *   Whether this node is optional in the current template invocation. 
   */
  public final boolean
  isOptional()
  {
    return pIsOptional;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   I N F O R M A T I O N                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of actual nodes that were built when this node was instantiated.
   */
  public final Set<String> 
  getBuiltNodes()
  {
    return Collections.unmodifiableSet(pBuiltNodes);
  }

  /**
   * Add a node that was built during template instantiation.  
   */
  public final void
  addBuiltNode
  (
    String node
  )
  {
    pBuiltNodes.add(node);
  }

  /**
   * Get the list of actual nodes that were built acquired this node was instantiated.
   */
  public final Set<String> 
  getAcquiredNodes()
  {
    return Collections.unmodifiableSet(pAcquiredNodes);
  }
  
  /**
   * Was the node acquired already?
   * 
   * @param nodeName
   *   The name of a node in the instantiated network.
   */
  public final boolean
  isAcquiredNode
  (
    String nodeName  
  )
  {
    return pAcquiredNodes.contains(nodeName);
  }
  

  /**
   * Add a node that was acquired during template instantiation.  
   */
  public final void
  addAcquiredNode
  (
    String node
  )
  {
    pAcquiredNodes.add(node);
  }

  /**
   * Get the list of actual nodes that were skipped when this node was instantiated.
   * <p>
   * Nodes can be skipped for several reasons:
   * <ol>
   * <li> Products which do not exist, but which are ignorable.
   * <li> Nodes which are skipped as part of conditional builds.
   * <li> Nodes which are flagged as optional and are not in the current working area after 
   *      all the optional checkouts.
   * </ol>
   */
  public final Set<String> 
  getSkippedNodes()
  {
    return Collections.unmodifiableSet(pSkippedNodes);
  }
  
  /**
   * Was the node skipped already?
   * <p>
   * Nodes can be skipped for several reasons:
   * <ol>
   * <li> Products which do not exist, but which are ignorable.
   * <li> Nodes which are skipped as part of conditional builds.
   * <li> Nodes which are flagged as optional and are not in the current working area after 
   *      all the optional checkouts.
   * </ol>
   * 
   * @param nodeName
   *   The name of a node in the instantiated network.
   */
  public final boolean
  isSkippedNode
  (
    String nodeName  
  )
  {
    return pSkippedNodes.contains(nodeName);
  }

  /**
   * Add a node that was skipped during template instantiation.  
   */
  public final void
  addSkippedNode
  (
    String node
  )
  {
    pSkippedNodes.add(node);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P R O D U C T   I N F O                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a node to the list of targets this product was acquired from.
   */
  public final void
  addProductTarget
  (
    String nodeName  
  )
  {
    pProductTargets.add(nodeName);
  }
  
  /**
   * Was this product node acquired from the particular target. 
   */
  public final boolean
  wasProductAcquired
  (
    String nodeName  
  )
  {
    return pProductTargets.contains(nodeName);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public static final String sALL = "[[ALL]]";
  
  public static final String aCloneFiles        = "CloneFiles";
  public static final String aPreEnableAction   = "PreEnableAction";
  public static final String aUnlinkAll         = "UnlinkAll";
  public static final String aVouch             = "Vouch";
  public static final String aTouchFiles        = "TouchFiles";
  public static final String aPostRemoveAction  = "PostRemoveAction";
  public static final String aPostDisableAction = "PostDisableAction";
  public static final String aContextName       = "ContextName";
  public static final String aLinkName          = "LinkName";
  public static final String aConditionName     = "ConditionName";
  public static final String aModeName          = "ModeName";
  public static final String aOptionName        = "OptionName";
  public static final String aOptionType        = "OptionType";
  public static final String aProductType       = "ProductType";
  public static final String aExternalName      = "ExternalName";
  public static final String aActionOnExistence = "ActionOnExistence";
  public static final String aVouchable         = "Vouchable";
  public static final String aIntermediate      = "Intermediate";
  public static final String aModifyFiles       = "ModifyFile";
  public static final String aSeqName           = "SeqName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the node.
   */
  private String pNodeName;
  
  /**
   * The template node mod that contains all the info about building the node.
   */
  private NodeMod pNodeMod;
  
  /**
   * The contexts that apply to this node.
   */
  private TreeSet<String> pContexts;
  
  /**
   * The secondary sequence contexts that apply to this node.
   */
  private TreeSet<String> pSecContexts;

  /**
   * A map of the secondary contexts that apply to a particular secondary sequences.
   * <p>
   * The contexts that apply to all the sequences are included under [[ALL]] in this data
   * structure.
   */
  private MappedSet<String, String> pSecContextBySeq;
  
  /**
   * Links to the upstream nodes.
   */
  private TreeMap<String, TemplateLink> pUpstreamLinks;
  
  /**
   * The names of downstream nodes.
   */
  private TreeSet<String> pDownstreamLinks;
  
  /**
   * The map of custom Action on Existence modes.
   */
  private TreeMap<String, ActionOnExistence> pAOEModes;
  
  /**
   * Is this node a root node in the template.
   */
  private boolean pIsRoot;
  
  /**
   * Is this node a part of the template.
   * <p>
   * If this is false, then the node is a product node.
   */
  private boolean pInTemplate;
  
  /**
   * Has this node already been instantiated during template execution.
   */
  private boolean pBuilt;
  
  /**
   * Has this node been skipped instead of built during template execution.
   */
  private boolean pSkipped;

  /**
   * Has this node been acquired (via checkout) as an optional branch being used as a product.
   */
  private boolean pAcquired;
  
  /**
   * The list of all the nodes that were generated by this node during execution.
   */
  private TreeSet<String> pBuiltNodes;
  
  /**
   * The list of all the nodes that were acquired by this node during execution.
   */
  private TreeSet<String> pAcquiredNodes;
  
  /**
   * The list of all the nodes that were ignored by this node during execution.
   */
  private TreeSet<String> pSkippedNodes;
  
  /**
   * The list of nodes which should be unlinked after the node is built and queued.
   */
  private TreeSet<String> pNodesToUnlink;
 
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  P R O D U C T   N O D E   S E T T I N G S                                             */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The list of targets that the product has been acquired from.
   */
  private TreeSet<String> pProductTargets;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  T E M P L A T E   S E T T I N G S                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private boolean pCloneFiles;
  private boolean pTouchFiles;
  private boolean pModifyFiles;
  private boolean pVouchable;
  private boolean pIntermediate;
  private boolean pPreEnableAction;
  private boolean pUnlinkAll;
  private boolean pPostRemoveAction;
  private boolean pPostDisableAction;
  
  private String pOptionalBranch;
  private OptionalBranchType pOptionalBranchType;
  private OptionalProductType pOptionalProductType;
  private String pExternalName;
  private Integer pOrder;
  private String pConditionalBuild;
  private String pFrameRangeName;
  private String pLinkSync;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N S T A N T I A T I O N   S E T T I N G S                                           */
  /*----------------------------------------------------------------------------------------*/

  private boolean pIsOptional;
}
