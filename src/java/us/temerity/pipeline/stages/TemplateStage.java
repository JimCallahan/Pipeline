// $Id: TemplateStage.java,v 1.1 2008/09/19 03:30:09 jesse Exp $

package us.temerity.pipeline.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.math.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   S T A G E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Stage for use with the Template Builder, that reads an existing template node network
 * and constructs a new one based upon certain string replacement values passed into the
 * builder and annotations that are assigned to the nodes in the template.
 * <p>
 */
public 
class TemplateStage
  extends StandardStage
  implements FinalizableStage
{
  private
  TemplateStage
  (
    NodeMod sourceMod,
    TreeMap<String, BaseAnnotation> annots,
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    PluginContext editor,
    PluginContext action,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> maps,
    TreeMap<String, TreeMap<String, BaseAnnotation>> annotCache
  ) 
    throws PipelineException
  {
    super("Template", 
          "Stage for use with the Template Builder", 
          stageInfo, context, client, 
          nodeName, range, padding, suffix, editor, action);
    pReplacements = stringReplacements;
    pMaps = maps;
    pAnnotCache = annotCache;
    init(sourceMod, annots);
  }

  private
  TemplateStage
  (
    NodeMod sourceMod,
    TreeMap<String, BaseAnnotation> annots,
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String suffix,
    PluginContext editor,
    PluginContext action,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> maps,
    TreeMap<String, TreeMap<String, BaseAnnotation>> annotCache
  ) 
    throws PipelineException
  {
    super("Template", 
          "Stage for use with the Template Builder", 
          stageInfo, context, client, 
          nodeName, suffix, editor, action);
    pReplacements = stringReplacements;
    pMaps = maps;
    pAnnotCache = annotCache;
    init(sourceMod, annots);
  }
  
  /**
   * Does the rest of the work.
   * <ul>
   * <li>  Adds an secondary sequences that need to be added.
   * <li>  Copy and modify all the links.
   * <li>  Fix the Single params for the Action
   * <li>  Fix the Source params for each source.
   * <li>  Copy the job requirements.
   * <li>  Copy and modify the annotations.
   * </ul>
   * @param sourceMod
   *   The template node we are basing this new node on.
   * @param annots
   *   The annotations on the template node.
   */
  @SuppressWarnings("unchecked")
  private void 
  init
  (
    NodeMod sourceMod,
    TreeMap<String, BaseAnnotation> annots
  ) 
    throws PipelineException
  {
    pSourceMod = sourceMod;
    
    for (String aName : annots.keySet()) {
      BaseAnnotation annot = annots.get(aName); 
      if (aName.matches("TemplateSettings")) {
        pRemoveAction = (Boolean) annot.getParamValue(aPostRemoveAction);
        pDisableAction = (Boolean) annot.getParamValue(aPostDisableAction);
        pEnableAction = (Boolean) annot.getParamValue(aPreEnableAction);
        pCloneFiles = (Boolean) annot.getParamValue(aCloneFiles);
        pVouch = (Boolean) annot.getParamValue(aVouch);
        pUnlinkAll = (Boolean) annot.getParamValue(aUnlinkAll);
      }
    }
    
    for (FileSeq seq : sourceMod.getSecondarySequences()) 
      addSecondarySequence(stringReplaceSeq(seq));
    
    BaseAction act = sourceMod.getAction();
    
    for (LinkMod link : sourceMod.getSources()) {
      TreeSet<String> maps = getMaps(aLinkName);
      if (maps.size() == 0)
        createLink(link, act, pReplacements);
      else
        mapLink(link, act, maps, pReplacements);
    }
    
    pSrcHasDisabledAction = !(sourceMod.isActionEnabled()); 
    
    
    if (act != null) {
      for (ActionParam param : act.getSingleParams()) {
        Comparable value = stringReplaceParamValue(param);
        addSingleParamValue(param.getName(), value);
      }
      
      setJobReqs(sourceMod.getJobRequirements());
      ExecutionMethod exec = sourceMod.getExecutionMethod();
      setExecutionMethod(exec);
      if (exec == ExecutionMethod.Parallel)
        setBatchSize(sourceMod.getBatchSize());
    }
    
    for (String annotName : annots.keySet()) {
      BaseAnnotation annot = annots.get(annotName);
      for (AnnotationParam param : annot.getParams() ) {
        Comparable value = stringReplaceAnnotParamValue(param);
        if (!annot.isParamConstant(param.getName()))
          annot.setParamValue(param.getName(), value);
      }
      addAnnotation(annotName, annot);
    }
  }

  @SuppressWarnings("unchecked")
  private String 
  createLink
  (
    LinkMod link,
    BaseAction act,
    TreeMap<String, String> replacements
  )
    throws PipelineException
  {
    String oldSrc = link.getName();
    
    String newSrc = stringReplace(oldSrc, replacements);
    LinkMod newLink = new LinkMod(newSrc, link.getPolicy(), link.getRelationship(), link.getFrameOffset());
    addLink(newLink);
    
    
    if (act != null) {
      if (act.supportsSourceParams()) {
        Set<String> sourceParams = act.getInitialSourceParams().keySet();
          if (act.hasSourceParams(oldSrc)) {
            for (String srcParam : sourceParams) {
              ActionParam param = act.getSourceParam(oldSrc, srcParam);
              Comparable value = stringReplaceParamValue(param);
              addSourceParamValue(newSrc, srcParam, value);
            }
          }
          for (FilePattern oldPat : act.getSecondarySequences(oldSrc)) {
            FilePattern newPat = stringReplacePat(oldPat);
            for (String srcParam : sourceParams) {
              ActionParam param = act.getSecondarySourceParam(oldSrc, oldPat, srcParam);
              Comparable value = stringReplaceParamValue(param);
              addSecondarySourceParamValue(newSrc, newPat, srcParam, value);
            }
          }
        }
      }
    return newSrc;
  }
  
  private void
  mapLink
  (
    LinkMod link,
    BaseAction act,
    TreeSet<String> mapList,
    TreeMap<String, String> replace
  )
    throws PipelineException
  {
    String currentMap = mapList.pollFirst();
    ArrayList<TreeMap<String, String>> values = pMaps.get(currentMap);
    
    if (values == null || values.isEmpty()) 
      throw new PipelineException
        ("The map (" + currentMap + ") specified on (" + link.getName() + ") has no values " +
         "defined for it.");
    
    for (TreeMap<String, String> mapEntry : values) {
      TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
      newReplace.putAll(mapEntry);
      if (mapList.isEmpty()) {  //bottom of the recursion
        createLink(link, act, newReplace);
      }
      else {
        mapLink(link, act, mapList, newReplace);
      }
    }
  }
  
  /**
   * Construct a new Template Stage.
   * 
   * @param sourceMod
   *   The template node we are basing this new node on.
   * @param annots
   *   The annotations on the template node.
   * @param stageInfo
   *   The stage information for the builder.
   * @param context
   *   The UtilContext we're making the node in.  Note that the toolset setting in this 
   *   context is going to be ignored in favor of the toolset in the source NodeMod.
   * @param client
   *   The instance of the MasterMgr to use when making the node.
   * @param stringReplacements
   *   A list of String replacements to make when creating the node from the template.  The
   *   keys will be searched for in node names, links, param values, and annotations
   *   and replaced with the values the key maps to.
   * @param maps
   *   The list of recursive string substitutions to be performed on nodes tagged with the
   *   TemplateMapAnnotation.
   * @param annotCache
   *   A shared cache of annotations for nodes 
   * @return
   *   A TemplateStage ready to build the new node.
   * @throws PipelineException
   *   If something goes horribly awry. 
   */
  public static TemplateStage
  getTemplateStage
  (
    NodeMod sourceMod,
    TreeMap<String, BaseAnnotation> annots,
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> maps,
    TreeMap<String, TreeMap<String, BaseAnnotation>> annotCache
  ) 
    throws PipelineException
  {
    String nodeName = stringReplace(sourceMod.getName(), stringReplacements);
    FileSeq priSeq = sourceMod.getPrimarySequence();
    FilePattern pat = priSeq.getFilePattern();
    String suffix = pat.getSuffix();
    
    PluginContext editor = null;
    {
      BaseEditor ed = sourceMod.getEditor();
      if (ed != null) {
        VersionID ver = ed.getVersionID();
        editor = new PluginContext(ed.getName(), ed.getVendor(), new Range<VersionID>(ver, ver));
      }
    }
    
    PluginContext action = null;
    {
      BaseAction act = sourceMod.getAction();
      if (act != null) {
        VersionID ver = act.getVersionID();
        action = new PluginContext(act.getName(), act.getVendor(), new Range<VersionID>(ver, ver));
      }
    }
    
    String toolset = sourceMod.getToolset();
    UtilContext newContext = new UtilContext(context.getAuthor(), context.getView(), toolset);
    
    if (priSeq.hasFrameNumbers()) {
      int padding = pat.getPadding();
      FrameRange range = priSeq.getFrameRange();
      return new TemplateStage
        (sourceMod, annots, stageInfo, newContext, client, nodeName, range, padding, suffix, 
         editor, action, stringReplacements, maps, annotCache);
    }
    else 
      return new TemplateStage
        (sourceMod, annots, stageInfo, newContext, client, nodeName, suffix, editor, action, 
         stringReplacements, maps, annotCache);
  }
  
  @Override
  public boolean 
  build()
    throws PipelineException
  {
    boolean build = super.build();
    if (build) {
      if (pSrcHasDisabledAction && !pEnableAction) {
        pRegisteredNodeMod.setActionEnabled(false);
        pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
        pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);
      }
      if (pCloneFiles) {
        NodeID src = new NodeID(getAuthor(), getView(), pSourceMod.getName() );
        NodeID tar = new NodeID(getAuthor(), getView(), pRegisteredNodeName);
        pClient.cloneFiles(src, tar);
      }
    }
    return build;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I N A L I Z A T I O N                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does this stage require a finalize pass to clean up what was built?
   * <p>
   * There are certain settings that can be made in annotations that will cause this to return
   * true. Specifically, any of the following will cause finalization to happen: <ul>
   * <li> Unlink All
   * <li> Post Remove Action
   * <li> Post Disable Action
   * <li> Vouch
   * </ul>
   */
  public boolean
  needsFinalization()
  {
    if (pUnlinkAll || pRemoveAction || pDisableAction || pVouch)
      return true;
    return false;
  }
  
  /**
   * Finishes off the work of the stage after it has been queued.
   * <p>
   * Actions are performed in the following order.
   * <ol>
   * <li> Disable Action
   * <li> Remove Action
   * <li> Unlink All
   * <li> Vouch
   * </ol>
   */
  @Override
  public void 
  finalizeStage()
    throws PipelineException
  {
    if (pDisableAction)
      pRegisteredNodeMod.setActionEnabled(false);
    if (pRemoveAction)
      pRegisteredNodeMod.setAction(null);
    if (pDisableAction || pRemoveAction) {
      pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
      pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);
    }
    
    if (pUnlinkAll) {
      for (String source : pRegisteredNodeMod.getSourceNames()) {
        pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, source);
      }
    }
    
    if (pVouch)
      vouch();
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T R I N G   R E P L A C E M E N T                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Modify the source string, replacing each key with its mapped value.
   * 
   * @param source
   *   The source string to be modified
   * @param stringReplacements
   *   A list of String replacements to make. The keys will be searched for and
   *   replaced with the values the keys map to.
   */
  private static String
  stringReplace
  (
    String source,
    TreeMap<String, String> stringReplacements
  )
  {
    String toReturn = source;
    if (toReturn != null) {
      for (String pattern : stringReplacements.keySet())
        toReturn = toReturn.replaceAll(pattern, stringReplacements.get(pattern));
    }
    return toReturn;
  }
  
  /**
   * Modify the file sequence, replacing each key with its mapped value.
   * 
   * @param seq
   *   The file sequence to be modified;
   */
  private FileSeq
  stringReplaceSeq
  (
    FileSeq seq
  )
  {
    FilePattern pat = seq.getFilePattern();
    String prefix = pat.getPrefix();
    prefix = stringReplace(prefix, pReplacements);
    
    if (seq.hasFrameNumbers()) {
      FrameRange range = seq.getFrameRange();
      FilePattern newPat = new FilePattern(prefix, pat.getPadding(), pat.getSuffix());
      return new FileSeq(newPat, range);
    }
    return new FileSeq(prefix, pat.getSuffix());
  }
  
  /**
   * Modify the file pattern, replacing each key with its mapped value.
   * 
   * @param pat
   *   The file sequence to be modified;
   */
  private FilePattern
  stringReplacePat
  (
    FilePattern pat
  )
  {
    String prefix = pat.getPrefix();
    prefix = stringReplace(prefix, pReplacements);
    
    return new FilePattern(prefix, pat.getPadding(), pat.getSuffix());
  }
  
  /**
   * Modify the Action Param value, replacing each key with its mapped value.
   * 
   * @param param
   *   The Action Param to be modified;
   */
  @SuppressWarnings("unchecked")
  private Comparable
  stringReplaceParamValue
  (
    ActionParam param
  )
  {
    Comparable value = param.getValue();
    if (param instanceof LinkActionParam || 
        param instanceof StringActionParam ||
        param instanceof TextAreaActionParam) 
      value = stringReplace((String) value, pReplacements);
    
    return value;
  }
  
  /**
   * Modify the Annotation Param value, replacing each key with its mapped value.
   * 
   * @param param
   *   The Annotation Param to be modified;
   */
  @SuppressWarnings("unchecked")
  private Comparable
  stringReplaceAnnotParamValue
  (
    AnnotationParam param
  )
  {
    Comparable value = param.getValue();
    if (param instanceof StringAnnotationParam ||
        param instanceof TextAreaAnnotationParam) 
      value = stringReplace((String) value, pReplacements);
    
    return value;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N S                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the Annotations on the given node.  
   *
   * @param name
   *   The name of the node.
   * @return
   *   A TreeMap of Annotations indexed by annotation name or 
   *   <code>null</code> if none exists.
   */
  private TreeMap<String, BaseAnnotation>
  getAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = pAnnotCache.get(name);
    if (annots == null) {
      annots = pClient.getAnnotations(name);
      pAnnotCache.put(name, annots);
    }
   return annots;
  }
  
  private TreeSet<String>
  getMaps
  (
    String name  
  )
    throws PipelineException
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    
    TreeMap<String, BaseAnnotation> allAnnots = getAnnotations(name);
    for (String aName : allAnnots.keySet()) {
      if (aName.startsWith("TemplateMap")) {
        BaseAnnotation annot = allAnnots.get(aName);
        String mapName = (String) annot.getParamValue(aMapName);
        toReturn.add(mapName); 
      }
    }
   return toReturn;   
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1242156409941962795L;
  
  public static final String aCloneFiles = "CloneFiles";
  public static final String aPreEnableAction = "PreEnableAction";
  public static final String aUnlinkAll ="UnlinkAll";
  public static final String aVouch = "Vouch";
  public static final String aPostRemoveAction = "PostRemoveAction";
  public static final String aPostDisableAction = "PostDisableAction";
  public static final String aMapName = "MapName";
  public static final String aLinkName = "LinkName";
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeMap<String, String> pReplacements;
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pMaps;
  private TreeMap<String, TreeMap<String, BaseAnnotation>> pAnnotCache;
  
  private boolean pSrcHasDisabledAction;
  
  private boolean pDisableAction;
  private boolean pRemoveAction;
  private boolean pCloneFiles;
  private boolean pVouch;
  private boolean pUnlinkAll;
  private boolean pEnableAction;
  
  private NodeMod pSourceMod;
}
