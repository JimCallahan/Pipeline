// $Id: TemplateStage.java,v 1.12 2009/05/22 19:16:29 jesse Exp $

package us.temerity.pipeline.stages;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_3.*;
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
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    PluginContext editor,
    PluginContext action,
    boolean inhibitCopy,
    boolean allowZeroContexts,
    TemplateBuildInfo templateInfo,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    FrameRange templateRange,
    TreeSet<String> ignoredNodes,
    TreeSet<String> ignorableProducts,
    TripleMap<String, String, String, TreeMap<String, BaseAnnotation>> annotCache
  ) 
    throws PipelineException
  {
    super("Template", 
          "Stage for use with the Template Builder", 
          stageInfo, context, client, 
          nodeName, (templateRange != null) ? templateRange : range, padding, suffix, editor, action);
    pReplacements = stringReplacements;
    pContexts = contexts;
    pAnnotCache = annotCache;
    pTemplateInfo = templateInfo;
    pTemplateRange = templateRange;
    pIgnoredNodes = ignoredNodes;
    pIgnorableProducts = ignorableProducts;
    pInhibitCopyFiles = inhibitCopy;
    pAllowZeroContexts = allowZeroContexts;
    init(sourceMod);
  }

  private
  TemplateStage
  (
    NodeMod sourceMod,
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String suffix,
    PluginContext editor,
    PluginContext action,
    boolean inhibitCopy,
    boolean allowZeroContexts,
    TemplateBuildInfo templateInfo,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> maps,
    FrameRange templateRange,
    TreeSet<String> ignoredNodes,
    TreeSet<String> ignorableProducts,
    TripleMap<String, String, String, TreeMap<String, BaseAnnotation>> annotCache
  ) 
    throws PipelineException
  {
    super("Template", 
          "Stage for use with the Template Builder", 
          stageInfo, context, client, 
          nodeName, suffix, editor, action);
    pReplacements = stringReplacements;
    pContexts = maps;
    pAnnotCache = annotCache;
    pTemplateInfo = templateInfo;
    pTemplateRange = templateRange;
    pIgnoredNodes = ignoredNodes;
    pIgnorableProducts = ignorableProducts;
    pInhibitCopyFiles = inhibitCopy;
    pAllowZeroContexts = allowZeroContexts;
    init(sourceMod);
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
    NodeMod sourceMod
  ) 
    throws PipelineException
  {
    pSourceMod = sourceMod;
    
    pUnlinkNodes = new MappedSet<String, String>();
    pTemplateNodesToUnlink = new TreeSet<String>();
    pSecSeqs = new TreeMap<FileSeq, FileSeq>();
    
    TreeMap<String, BaseAnnotation> annots = getAnnotations(pSourceMod.getName());
    
    for (String aName : annots.keySet()) {
      BaseAnnotation annot = annots.get(aName); 
      if (aName.matches("TemplateSettings")) {
        LogMgr.getInstance().log(Kind.Bld, Level.Finest, 
          "Found a template settings annotation.");
        pRemoveAction = (Boolean) annot.getParamValue(aPostRemoveAction);
        pDisableAction = (Boolean) annot.getParamValue(aPostDisableAction);
        pEnableAction = (Boolean) annot.getParamValue(aPreEnableAction);
        pCloneFiles = (Boolean) annot.getParamValue(aCloneFiles);
        pVouch = (Boolean) annot.getParamValue(aVouch);
        pUnlinkAll = (Boolean) annot.getParamValue(aUnlinkAll);
        pTouchFiles = (Boolean) annot.getParamValue(aTouchFiles);
      }
      if (aName.startsWith("TemplateUnlink" )) {
        String unlink = (String) annot.getParamValue(aLinkName);
        LogMgr.getInstance().log(Kind.Bld, Level.Finest, 
          "Found a Template Unlink annotation with the value (" + unlink + ").");
        pTemplateNodesToUnlink.add(unlink);
      }
    }
    
    for (FileSeq seq : sourceMod.getSecondarySequences()) {
      FileSeq targetSeq = stringReplaceSeq(seq);
      LogMgr.getInstance().log(Kind.Bld, Level.Finest,
        "Adding the secondary sequence: " + targetSeq);
      if (pTemplateRange != null) {
        targetSeq = new FileSeq(targetSeq.getFilePattern(), pTemplateRange);
      }
      addSecondarySequence(targetSeq);
      pSecSeqs.put(seq, targetSeq);
    }
    
    BaseAction act = sourceMod.getAction();
    
    for (LinkMod link : sourceMod.getSources()) {
      String linkName = link.getName();
      
      LogMgr.getInstance().log(Kind.Bld, Level.Finest, 
        "Checking the link: " + linkName);
      TreeSet<String> contexts = getContexts(linkName);
      
      boolean ignoreable = false;
      
      //If it is a product node (not being built)
      if (!pTemplateInfo.getNodesToBuild().contains(linkName)) { 
        if (pIgnorableProducts.contains(linkName)) {
          ignoreable = true;
          LogMgr.getInstance().log(Kind.Bld, Level.Finest,
            "The link is in the ignorable product list.");
        }
      }
      
      if (contexts.size() == 0)
        createLink(link, act, pReplacements, ignoreable);
      else
        contextLink(link, act, new TreeSet<String>(contexts), pReplacements, ignoreable);
    }
    
    pSrcHasDisabledAction = !(sourceMod.isActionEnabled()); 
    
    
    if (act != null) {
      for (ActionParam param : act.getSingleParams()) {
        Comparable value = stringReplaceParamValue(param, pReplacements);
        addSingleParamValue(param.getName(), value);
      }
      
      if (pTemplateRange != null) {
        if (act.getSingleParam(aStartFrame) != null)
          addSingleParamValue(aStartFrame, pTemplateRange.getStart());
        if (act.getSingleParam(aEndFrame) != null)
          addSingleParamValue(aEndFrame, pTemplateRange.getEnd());
        if (act.getSingleParam(aByFrame) != null)
          addSingleParamValue(aByFrame, pTemplateRange.getBy());
      }
      
      setJobReqs(sourceMod.getJobRequirements());
      ExecutionMethod exec = sourceMod.getExecutionMethod();
      setExecutionMethod(exec);
      if (exec == ExecutionMethod.Parallel)
        setBatchSize(sourceMod.getBatchSize());
    }
    
    {
      TreeMap<String, BaseAnnotation> perNode = 
        pClient.getAnnotations(sourceMod.getName()); 
      for (String annotName : perNode.keySet()) {
        BaseAnnotation annot = perNode.get(annotName);
        for (AnnotationParam param : annot.getParams() ) {
          Comparable value = stringReplaceAnnotParamValue(param);
          if (!annot.isParamConstant(param.getName()))
            annot.setParamValue(param.getName(), value);
        }
        addAnnotation(annotName, annot);
      }
    }
    
    {
      TreeMap<String, BaseAnnotation> perVersion = 
        sourceMod.getAnnotations();
      for (String annotName : perVersion.keySet()) {
        BaseAnnotation annot = perVersion.get(annotName);
        for (AnnotationParam param : annot.getParams() ) {
          Comparable value = stringReplaceAnnotParamValue(param);
          if (!annot.isParamConstant(param.getName()))
            annot.setParamValue(param.getName(), value);
        }
        addVersionAnnotation(annotName, annot);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private String 
  createLink
  (
    LinkMod link,
    BaseAction act,
    TreeMap<String, String> replacements, 
    boolean ignoreable
  )
    throws PipelineException
  {
    String oldSrc = link.getName();
    
    String newSrc = stringReplace(oldSrc, replacements);
    if (pIgnoredNodes.contains(newSrc)) {
      LogMgr.getInstance().log(Kind.Bld, Level.Fine, 
        "Not linking source node (" + newSrc + ") because the template skipped building it.");
      return null;
    }
    
    if (!nodeExists(newSrc) && ignoreable) {
      LogMgr.getInstance().log(Kind.Bld, Level.Fine, 
        "Not linking source node (" + newSrc + ") because it doesn't exist");
      return null;
    }
    LinkMod newLink = new LinkMod(newSrc, link.getPolicy(), link.getRelationship(), link.getFrameOffset());
    LogMgr.getInstance().log(Kind.Bld, Level.Fine, 
      "Linking source node (" + newSrc + ").");
    addLink(newLink);
    
    if (pTemplateNodesToUnlink.contains(oldSrc))
      pUnlinkNodes.put(oldSrc, newSrc);
    
    if (act != null) {
      if (act.supportsSourceParams()) {
        Set<String> sourceParams = act.getInitialSourceParams().keySet();
          if (act.hasSourceParams(oldSrc)) {
            for (String srcParam : sourceParams) {
              ActionParam param = act.getSourceParam(oldSrc, srcParam);
              Comparable value = stringReplaceParamValue(param, replacements);
              addSourceParamValue(newSrc, srcParam, value);
            }
          }
          for (FilePattern oldPat : act.getSecondarySequences(oldSrc)) {
            FilePattern newPat = stringReplacePat(oldPat);
            for (String srcParam : sourceParams) {
              ActionParam param = act.getSecondarySourceParam(oldSrc, oldPat, srcParam);
              Comparable value = stringReplaceParamValue(param, replacements);
              addSecondarySourceParamValue(newSrc, newPat, srcParam, value);
            }
          }
        }
      }
    return newSrc;
  }
  
  private void
  contextLink
  (
    LinkMod link,
    BaseAction act,
    TreeSet<String> contextList,
    TreeMap<String, String> replace, boolean ignoreable
  )
    throws PipelineException
  {
    String currentContext = contextList.pollFirst();
    ArrayList<TreeMap<String, String>> values = pContexts.get(currentContext);
    
    if (values == null || values.isEmpty()) {
      if (pAllowZeroContexts) {
        LogMgr.getInstance().logAndFlush(Kind.Bld, Level.Warning, 
          "The context (" + currentContext + ") specified on (" + link.getName() + ") has " +
          "no values defined for it.");
        TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
        if (contextList.isEmpty()) {  //bottom of the recursion
          createLink(link, act, newReplace, ignoreable);
        }
        else {
          contextLink(link, act, new TreeSet<String>(contextList), newReplace, ignoreable);
        }
        return;
      }
      else {
        throw new PipelineException
          ("The context (" + currentContext + ") specified on (" + link.getName() + ") has no values " +
          "defined for it.");
      }
    }
    
    for (TreeMap<String, String> contextEntry : values) {
      TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
      newReplace.putAll(contextEntry);
      if (contextList.isEmpty()) {  //bottom of the recursion
        createLink(link, act, newReplace, ignoreable);
      }
      else {
        contextLink(link, act, new TreeSet<String>(contextList), newReplace, ignoreable);
      }
    }
  }
  
  /**
   * Construct a new Template Stage.
   * 
   * @param sourceMod
   *   The template node we are basing this new node on.
   * 
   * @param stageInfo
   *   The stage information for the builder.
   * 
   * @param context
   *   The UtilContext we're making the node in.  Note that the toolset setting in this 
   *   context is going to be ignored in favor of the toolset in the source NodeMod.
   * 
   * @param client
   *   The instance of the MasterMgr to use when making the node.
   * 
   * @param templateInfo
   *   Information about what the template is making.  Used to pull out context information
   *   for product nodes.
   * 
   * @param stringReplacements
   *   A list of String replacements to make when creating the node from the template.  The
   *   keys will be searched for in node names, links, param values, and annotations
   *   and replaced with the values the key maps to.
   * 
   * @param contexts
   *   The list of recursive string substitutions to be performed on nodes tagged with the
   *   TemplateContextAnnotation.
   * 
   * @param range
   *   The frame range to be used for this node or <code>null</code> if there is no 
   *   special template frame range.
   * 
   * @param ignoreableProducts
   *   A list of products which can be ignored if they are not found.
   *   
   * @param ignoredNodes
   *   A list of nodes in the template which were not built because of a Conditional Build
   *   annotation
   *   
   * @param inhibitCopy
   *   Whether the CloneFiles template setting should be ignored.
   *   
   * @param allowZeroContexts
   *   Whether a zero context is acceptable in the sources of the node.
   * 
   * @param annotCache
   *   A shared cache of annotations for nodes 
   * 
   * @return
   *   A TemplateStage ready to build the new node.
   * 
   * @throws PipelineException
   *   If something goes horribly awry. 
   */
  public static TemplateStage
  getTemplateStage
  (
    NodeMod sourceMod,
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    TemplateBuildInfo templateInfo,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    FrameRange range, 
    TreeSet<String> ignoredNodes,
    TreeSet<String> ignoreableProducts,
    boolean inhibitCopy,
    boolean allowZeroContexts,
    TripleMap<String, String, String, TreeMap<String, BaseAnnotation>> annotCache
  ) 
    throws PipelineException
  {
    String nodeName = stringReplace(sourceMod.getName(), stringReplacements);
    FileSeq priSeq = sourceMod.getPrimarySequence();
    FilePattern pat = priSeq.getFilePattern();
    String suffix = pat.getSuffix();
    if (suffix != null)
      suffix = stringReplace(suffix, stringReplacements);
    
    
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
      FrameRange oldRange = priSeq.getFrameRange();
      return new TemplateStage
        (sourceMod, stageInfo, newContext, client, nodeName, oldRange, padding, suffix, 
         editor, action, inhibitCopy, allowZeroContexts, templateInfo, stringReplacements, contexts, range, 
         ignoredNodes, ignoreableProducts, annotCache);
    }
    else 
      return new TemplateStage
        (sourceMod, stageInfo, newContext, client, nodeName, suffix, editor, action, 
         inhibitCopy, allowZeroContexts, templateInfo, stringReplacements, contexts, range, ignoredNodes, 
         ignoreableProducts, annotCache);
  }
  
  @Override
  public boolean 
  build()
    throws PipelineException
  {
    boolean build = super.build();
    if (build) {
      if (pSrcHasDisabledAction && !pEnableAction) {
        LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
          "Disabling the action after building the node.");
        pRegisteredNodeMod.setActionEnabled(false);
        pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
        pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);
      }
      if (pCloneFiles  && !pInhibitCopyFiles) {
        LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
          "Cloning the files after building the node.");
        NodeID src = new NodeID(getAuthor(), getView(), pSourceMod.getName() );
        NodeID tar = new NodeID(getAuthor(), getView(), pRegisteredNodeName);
        pClient.cloneFiles(src, tar, pSecSeqs);
      }
      if (pTouchFiles) {
        LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
          "Backing up the Action and replacing it with the Touch action.");
        pBackedUpAction = pRegisteredNodeMod.getAction();
        pRegisteredNodeMod.setAction(getAction(new PluginContext("Touch"), getToolset()));
        pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
        pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);
      }
    }
    return build;
  }
  
  /**
   * Return a subprocess which, when run, will touch all the files for the node.
   */
  private SubProcessLight
  touchFilesProcess()
    throws PipelineException
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), pRegisteredNodeName);
    if(PackageInfo.sOsType == OsType.Windows) { 
      try {
        File script = File.createTempFile("BuilderTouchFiles", ".bat");
        FileWriter out = new FileWriter(script);
        
        Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());
        
        wpath.toFile().mkdirs();
        
        for(Path target : pRegisteredNodeMod.getPrimarySequence().getPaths()) {
          Path path = new Path(wpath, target);
          out.write("@echo off > " + path.toOsString() + "\n"); 
        }
        
        for(FileSeq fseq : pRegisteredNodeMod.getSecondarySequences()) {
          for(Path target : fseq.getPaths()) {
            Path path = new Path(wpath, target);
            out.write("@echo off > " + path.toOsString() + "\n"); 
          }
        }
        
        out.close();
        
        TreeMap<String, String> toolset = 
          pClient.getToolsetEnvironment
            (getAuthor(), getView(), getToolset(), PackageInfo.sOsType);
        SubProcessLight light = 
          new SubProcessLight("BuilderTouch", script.getPath(), new ArrayList<String>(), 
                              toolset, PackageInfo.sTempPath.toFile());
        return light;
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary BAT file to touch the files for node " +
           "(" + pRegisteredNodeName + ")\n" + ex.getMessage());
      }
    }
    else {
      ArrayList<String> args = new ArrayList<String>();
      for(File file : pRegisteredNodeMod.getPrimarySequence().getFiles()) 
        args.add(file.toString());
      
      for(FileSeq fseq : pRegisteredNodeMod.getSecondarySequences()) {
        for(File file : fseq.getFiles())
          args.add(file.toString());
      }
      
      Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent()); 
      wpath.toFile().mkdirs();
      
      TreeMap<String, String> toolset = 
        pClient.getToolsetEnvironment
          (getAuthor(), getView(), getToolset(), PackageInfo.sOsType);
      SubProcessLight light = 
        new SubProcessLight("BuilderTouch", "touch", args, toolset, wpath.toFile());
      return light;
    }
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
   * <li> Unlink
   * <li> Post Touch Files
   * </ul>
   * 
   * @return
   *   If the stage needs finalization.
   */
  public boolean
  needsFinalization()
  {
    if (pUnlinkAll || pRemoveAction || pDisableAction || pVouch || 
        !pUnlinkNodes.isEmpty())
      return true;
    return false;
  }

  public boolean
  needsSecondFinalization()
  {
    if (pTouchFiles)
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
    if (pDisableAction) {
      LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
        "Disabling the action on (" + pRegisteredNodeName + ") as part of finalization.");
      pRegisteredNodeMod.setActionEnabled(false);
    }
    if (pRemoveAction) {
      LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
        "Removing the action on (" + pRegisteredNodeName + ") as part of finalization.");
      pRegisteredNodeMod.setAction(null);
    }
    if (pDisableAction || pRemoveAction) {
      pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
      pRegisteredNodeMod = 
        pClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);
    }

    if (!pUnlinkNodes.isEmpty()) {
      for (String oldSrc : pUnlinkNodes.keySet()) {
        for (String newSrc : pUnlinkNodes.get(oldSrc)) {
          LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
            "Unlinking the node (" + newSrc + ") from (" + pRegisteredNodeName + ") due to " +
            "an unlink annotation.");
          pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, newSrc);
        }
      }
      pRegisteredNodeMod = 
        pClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);
    }
    
    if (pUnlinkAll) {
      LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
        "Unlinking all sources from (" + pRegisteredNodeName + ") due to TemplateSettings " +
        "Annotation.");
      for (String source : pRegisteredNodeMod.getSourceNames()) {
        pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, source);
      }
    }

    if (pTouchFiles) {
      LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
        "Restoring backed-up action to (" + pRegisteredNodeName + ") as part " +
        "of finalization.");
      pRegisteredNodeMod.setAction(pBackedUpAction);
      pRegisteredNodeMod.setActionEnabled(true);
      pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
      pRegisteredNodeMod = 
        pClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);

      LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
        "Touching the files on (" + pRegisteredNodeName + ") to remove staleness as part of " +
        "finalization.");
      SubProcessLight process = touchFilesProcess();
      process.run();
      try {
        process.join();
      }
      catch (InterruptedException ex) 
      {
        String message = "The touch subprocess for node () failed.\n";
        throw new PipelineException(Exceptions.getFullMessage(message, ex));
      }
      Integer exit = process.getExitCode();
      if (exit == null || exit != 0)
        throw new PipelineException
        ("The touch subprocess did not finish correctly\n" +
          process.getStdOut() + "\n" + process.getStdErr());
    }

    if (pVouch) {
      LogMgr.getInstance().log(Kind.Bld, Level.Finer, 
        "Vouching for the node (" + pRegisteredNodeName + ") as part of finalization.");
      vouch();
    }
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
    String suffix = pat.getSuffix();
    if (suffix != null)
      suffix = stringReplace(suffix, pReplacements);
    
    if (seq.hasFrameNumbers()) {
      FrameRange range = seq.getFrameRange();
      FilePattern newPat = new FilePattern(prefix, pat.getPadding(), suffix);
      return new FileSeq(newPat, range);
    }
    return new FileSeq(prefix, suffix);
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
    String suffix = pat.getSuffix();
    if (suffix != null)
      suffix = stringReplace(suffix, pReplacements);
    
    return new FilePattern(prefix, pat.getPadding(), suffix);
  }
  
  /**
   * Modify the Action Param value, replacing each key with its mapped value.
   * 
   * @param param
   *   The Action Param to be modified;
   * @param replacements 
   *   The list of replacements to use when fixing the String.
   */
  @SuppressWarnings("unchecked")
  private Comparable
  stringReplaceParamValue
  (
    ActionParam param, 
    TreeMap<String,String> replacements
  )
  {
    Comparable value = param.getValue();
    if (param instanceof LinkActionParam || 
        param instanceof StringActionParam ||
        param instanceof TextAreaActionParam) 
      value = stringReplace((String) value, replacements);
    
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
    TreeMap<String, BaseAnnotation> annots = pAnnotCache.get(getAuthor(), getView(), name);
    if (annots == null) {
      annots = pClient.getAnnotations(getAuthor(), getView(), name);
      pAnnotCache.put(getAuthor(), getView(), name, annots);
    }
   return annots;
  }
  
  private TreeSet<String>
  getContexts
  (
    String name  
  )
    throws PipelineException
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    
    // if the node is part of the template.
    if (pTemplateInfo.getNodesToBuild().contains(name)) {
      TreeMap<String, BaseAnnotation> allAnnots = getAnnotations(name);
      for (String aName : allAnnots.keySet()) {
        if (aName.startsWith("TemplateContext")) {
          BaseAnnotation annot = allAnnots.get(aName);
          String mapName = (String) annot.getParamValue(aContextName);
          toReturn.add(mapName); 
        }
      }
    }
    /* else this is a product node and we need to pull context info from
     * the template info.
     */
    else {
      TreeSet<String> contexts = 
        pTemplateInfo.getProductContexts().get(name, pSourceMod.getName());
      if (contexts != null)
        toReturn.addAll(contexts);       
    }
    
   return toReturn;   
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1242156409941962795L;
  
  public static final String aCloneFiles        = "CloneFiles";
  public static final String aPreEnableAction   = "PreEnableAction";
  public static final String aUnlinkAll         = "UnlinkAll";
  public static final String aVouch             = "Vouch";
  public static final String aTouchFiles        = "TouchFiles";
  public static final String aPostRemoveAction  = "PostRemoveAction";
  public static final String aPostDisableAction = "PostDisableAction";
  public static final String aContextName       = "ContextName";
  public static final String aLinkName          = "LinkName";
  
  public static final String aStartFrame = "StartFrame";
  public static final String aEndFrame   = "EndFrame";
  public static final String aByFrame    = "ByFrame";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeMap<String, String> pReplacements;
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  private TripleMap<String, String, String, TreeMap<String, BaseAnnotation>> pAnnotCache;
  
  private MappedSet<String, String> pUnlinkNodes;
  private TreeSet<String> pTemplateNodesToUnlink;
  
  private TemplateBuildInfo pTemplateInfo;
  
  private boolean pSrcHasDisabledAction;
  
  private boolean pDisableAction;
  private boolean pRemoveAction;
  private boolean pCloneFiles;
  private boolean pTouchFiles;
  private boolean pVouch;
  private boolean pUnlinkAll;
  private boolean pEnableAction;
  
  private boolean pInhibitCopyFiles;
  private boolean pAllowZeroContexts;
  
  private NodeMod pSourceMod;
  
  private FrameRange pTemplateRange;
  
  private TreeMap<FileSeq, FileSeq> pSecSeqs;
  
  private TreeSet<String> pIgnorableProducts;
  
  private TreeSet<String> pIgnoredNodes;
  
  private BaseAction pBackedUpAction;
}
