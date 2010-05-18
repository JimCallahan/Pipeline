// $Id: TemplateStage.java,v 1.21 2009/10/27 05:24:46 jesse Exp $

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
    TreeSet<String> skippedNodes,
    TreeSet<String> ignoredNodes,
    TreeSet<String> ignorableProducts,
    TemplateExternalData external, 
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
    pActualRange = (templateRange != null) ? templateRange : range;
    pIgnoredNodes = ignoredNodes;
    pIgnorableProducts = ignorableProducts;
    pSkippedNodes = skippedNodes;
    pInhibitCopyFiles = inhibitCopy;
    pAllowZeroContexts = allowZeroContexts;
    pExternal = external;
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
    TreeSet<String> skippedNodes,
    TreeSet<String> ignoredNodes,
    TreeSet<String> ignorableProducts,
    TemplateExternalData external,
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
    pActualRange = null;
    pSkippedNodes = skippedNodes;
    pIgnoredNodes = ignoredNodes;
    pIgnorableProducts = ignorableProducts;
    pInhibitCopyFiles = inhibitCopy;
    pAllowZeroContexts = allowZeroContexts;
    pExternal = external;
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
    BaseAction act = sourceMod.getAction();
    
    String nodeName = getNodeName();
    if (pExternal != null && act != null)
      throw new PipelineException
        ("The node (" + nodeName + ") has an Action assigned to it, " +
         "but also has a TemplateExternal annotation associated with it.  Nodes with " +
         "TemplateExternal annotations cannot have Actions.");
    
    if (pExternal != null && PackageInfo.sOsType != OsType.Unix)
      throw new PipelineException
        ("The node (" + nodeName + ") has a TemplateExternal annotation associated " +
         "with it, but this builder is not being run on a Unix/Linux machine.  " +
         "External Sequence support only exists on Unix-derivative operating systems" );
    
    setIntermediate(pSourceMod.isIntermediate());
    
    pUnlinkNodes = new MappedSet<String, String>();
    pTemplateNodesToUnlink = new TreeSet<String>();
    pSecSeqs = new MappedSet<FileSeq, FileSeq>();
    
    TreeMap<String, BaseAnnotation> annots = getAnnotations(pSourceMod.getName());
    
    for (String aName : annots.keySet()) {
      BaseAnnotation annot = annots.get(aName); 
      if (aName.matches("TemplateSettings")) {
        pLog.log(Kind.Bld, Level.Finest, 
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
        pLog.log(Kind.Bld, Level.Finest, 
          "Found a Template Unlink annotation with the value (" + unlink + ").");
        pTemplateNodesToUnlink.add(unlink);
      }
    }
    
    for (FileSeq seq : sourceMod.getSecondarySequences()) {
      FileSeq targetSeq = stringReplaceSeq(seq);
      if (pActualRange != null) {
        targetSeq = new FileSeq(targetSeq.getFilePattern(), pActualRange);
      }
      pLog.log(Kind.Bld, Level.Finest,
        "Adding the secondary sequence: " + targetSeq);
      addSecondarySequence(targetSeq);
      pSecSeqs.put(seq, targetSeq);
    }
    
    for (LinkMod link : sourceMod.getSources()) {
      String linkName = link.getName();
      
      pLog.log(Kind.Bld, Level.Finest, 
        "Checking the link: " + linkName);
      
      if (pSkippedNodes.contains(linkName)) {
        pLog.log(Kind.Bld, Level.Fine,
          "The linked node was skipped during construction and the link is being ignored.");
        continue;
      }
      
      TreeSet<String> contexts = getContexts(linkName);
      
      boolean ignoreable = false;
      
      //If it is a product node (not being built)
      if (!pTemplateInfo.getNodesToBuild().contains(linkName)) { 
        if (pIgnorableProducts.contains(linkName)) {
          ignoreable = true;
          pLog.log(Kind.Bld, Level.Finest,
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
      pLog.log(Kind.Bld, Level.Fine, 
        "Not linking source node (" + newSrc + ") because the template skipped building it.");
      return null;
    }
    
    if (!nodeExists(newSrc) && ignoreable) {
      pLog.log(Kind.Bld, Level.Fine, 
        "Not linking source node (" + newSrc + ") because it doesn't exist");
      return null;
    }
    LinkMod newLink = new LinkMod(newSrc, link.getPolicy(), link.getRelationship(), link.getFrameOffset());
    pLog.log(Kind.Bld, Level.Fine, 
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
        pLog.logAndFlush(Kind.Bld, Level.Warning, 
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
   * @param skippedNodes
   *   A list of nodes in the template which were never considered for instantiating due to
   *   an Optional Branch Annotation. 
   * 
   * @param ignoreableProducts
   *   A list of products which can be ignored if they are not found.
   *   
   * @param ignoredNodes
   *   A list of nodes in the instantiated network which were not built because of a 
   *   Conditional Build annotation
   *   
   * @param inhibitCopy
   *   Whether the CloneFiles template setting should be ignored.
   *   
   * @param allowZeroContexts
   *   Whether a zero context is acceptable in the sources of the node.
   *   
   * @param external
   *   The external file sequence to link this node to or <code>null</code> if there is no
   *   external sequence for this node.
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
    TreeSet<String> skippedNodes,
    TreeSet<String> ignoredNodes,
    TreeSet<String> ignoreableProducts,
    boolean inhibitCopy,
    boolean allowZeroContexts,
    TemplateExternalData external,
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
         skippedNodes, ignoredNodes, ignoreableProducts, external, annotCache);
    }
    else 
      return new TemplateStage
        (sourceMod, stageInfo, newContext, client, nodeName, suffix, editor, action, 
         inhibitCopy, allowZeroContexts, templateInfo, stringReplacements, contexts, range, 
         skippedNodes, ignoredNodes, ignoreableProducts, external, annotCache);
  }
  
  @Override
  public boolean 
  build()
    throws PipelineException
  {
    boolean build = super.build();
    if (build) {
      String nodeName = getNodeName();
      if (pSrcHasDisabledAction && !pEnableAction) {
        pLog.log(Kind.Bld, Level.Finer, 
          "Disabling the action after building the node.");
        pRegisteredNodeMod.setActionEnabled(false);
        pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
        pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(), nodeName);
      }
      if (pCloneFiles  && !pInhibitCopyFiles) {
        pLog.log(Kind.Bld, Level.Finer, 
          "Cloning the files after building the node.");
        NodeID src = new NodeID(getAuthor(), getView(), pSourceMod.getName() );
        NodeID tar = getNodeID();
        
        FrameRange srcRange = pSourceMod.getPrimarySequence().getFrameRange();
        FrameRange tgtRange = pRegisteredNodeMod.getPrimarySequence().getFrameRange();
        
        if (tgtRange != null && srcRange != null) {
          for (int frame : tgtRange.getFrameNumbers()) {
            if (!srcRange.isValid(frame))
              throw new PipelineException
              ("The target frame range (" + tgtRange + ") contains frames that are not in " +
                "the source frame range (" + srcRange + ").  Clone files cannot continue.");
          }

          pClient.cloneFiles(src, tar, pSecSeqs, tgtRange, tgtRange);
        }
        else {
          pClient.cloneFiles(src, tar, pSecSeqs);
        }
      }
      if (pTouchFiles) {
        pLog.log(Kind.Bld, Level.Finer, 
          "Backing up the Action and replacing it with the Touch action.");
        pBackedUpAction = pRegisteredNodeMod.getAction();
        pRegisteredNodeMod.setAction(lookupAction(new PluginContext("Touch"), getToolset()));
        pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
        pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(), nodeName);
      }
      if (pExternal != null) {
        FileSeq exSeq = pExternal.getFileSeq();
        FrameRange exRange = exSeq.getFrameRange();
        
        FrameRange nodeRange = pRegisteredNodeMod.getPrimarySequence().getFrameRange();
        
        if (nodeRange == null && exRange != null)
          throw new PipelineException
            ("Cannot link an external sequence with a frame range to a node that " +
             "does not have a frame range.");
        
        FileSeq newExSeq = null;
        if (nodeRange != null) {
          if (exRange == null)
            throw new PipelineException
              ("Cannot link an external sequence without a frame range to a node that " +
               "has a frame range.");
          
          Integer startFrame = pExternal.getStartFrame();
          if (startFrame == null)
            startFrame = exRange.indexToFrame(0);
          
          int nodeSize = nodeRange.numFrames();
          
          int lastIdx = exRange.numFrames() -1;
          TreeSet<Integer> frames = new TreeSet<Integer>();
          int[] frameNumbers = exRange.getFrameNumbers();
          for (int i = exRange.frameToIndex(startFrame); i <= lastIdx; i++) {
            frames.add(frameNumbers[i]);
            if (frames.size() == nodeSize)
              break;
          }
          if (frames.size() < nodeSize) {
            throw new PipelineException
              ("There were not enough frames in the external sequence (" + exSeq + ") " +
               "when starting at (" + startFrame + ") to satisfy the requirements of " +
               "the current sequence (" + pRegisteredNodeMod.getSequences() + ")");
          }
         FrameRange newExRange = new FrameRange(frames);
         newExSeq = new FileSeq(exSeq.getFilePattern(), newExRange);
        } //if (nodeRange != null) {
        else {
          newExSeq = exSeq;
        }
        
        pLog.log(Kind.Bld, Level.Finer, 
          "Creating the symbolic links to (" + newExSeq+ ").");
        SubProcessLight process = softLinkProcess(newExSeq);
        process.run();
        try {
          process.join();
        }
        catch (InterruptedException ex) 
        {
          String message = 
            "The link subprocess for node (" + nodeName + ") failed.";
          throw new PipelineException(Exceptions.getFullMessage(message, ex));
        }
        Integer exit = process.getExitCode();
        if (exit == null || exit != 0)
          throw new PipelineException
            ("The link subprocess did not finish correctly\n" +
              process.getStdOut() + "\n" + process.getStdErr());


        try {
          vouch(); 
        }
        catch (PipelineException ex ) {
          String message =
            "An error occurred while attempting to vouch for the node " +
            "(" + nodeName + ") which was linked to external files.  This is " +
            "most likely caused by the user running the template not having write " +
            "permissions for the external files (which it needs to touch the files).\n" + 
            ex.getMessage();
          throw new PipelineException(message);
        }
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
    NodeID nodeID = new NodeID(getAuthor(), getView(), getNodeName());
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
           "(" + getNodeName() + ")\n" + ex.getMessage());
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
  
  private SubProcessLight
  softLinkProcess
  (
    FileSeq exSeq  
  )
    throws PipelineException
  {
    NodeID nodeID = getNodeID();
    
    Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent()); 
    wpath.toFile().mkdirs();
    
    FileSeq seq = pRegisteredNodeMod.getPrimarySequence();
    
    int size = seq.numFrames();
    
    ArrayList<String> args = new ArrayList<String>();
    try
    {
      File f = File.createTempFile("TemplateExternal.", ".bash", 
        PackageInfo.sTempPath.toFile());
      BufferedWriter out = new BufferedWriter(new FileWriter(f));
      for (int idx = 0; idx < size; idx++) {
        Path target = seq.getPath(idx);
        Path source = exSeq.getPath(idx);
        out.write("ln -sf " + source + " " + target + "\n");
      }
      out.close();
      args.add(f.getAbsolutePath());
    } 
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to create the temporary bash script used to create" + 
         "the symbolic links!\n" + ex.getMessage());
    }
    
    TreeMap<String, String> toolset = 
      pClient.getToolsetEnvironment
        (getAuthor(), getView(), getToolset(), PackageInfo.sOsType);
    SubProcessLight light = 
      new SubProcessLight("LinkExternal", "bash", args, toolset, wpath.toFile());
    return light;
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
    String nodeName = getNodeName();
    if (pDisableAction) {
      pLog.log(Kind.Bld, Level.Finer, 
        "Disabling the action on (" + nodeName + ") as part of finalization.");
      pRegisteredNodeMod.setActionEnabled(false);
    }
    if (pRemoveAction) {
      pLog.log(Kind.Bld, Level.Finer, 
        "Removing the action on (" + nodeName + ") as part of finalization.");
      pRegisteredNodeMod.setAction(null);
    }
    if (pDisableAction || pRemoveAction) {
      pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
      pRegisteredNodeMod = 
        pClient.getWorkingVersion(getAuthor(), getView(), nodeName);
    }

    if (!pUnlinkNodes.isEmpty()) {
      for (String oldSrc : pUnlinkNodes.keySet()) {
        for (String newSrc : pUnlinkNodes.get(oldSrc)) {
          pLog.log(Kind.Bld, Level.Finer, 
            "Unlinking the node (" + newSrc + ") from (" + nodeName + ") due to " +
            "an unlink annotation.");
          pClient.unlink(getAuthor(), getView(), nodeName, newSrc);
        }
      }
      pRegisteredNodeMod = 
        pClient.getWorkingVersion(getAuthor(), getView(), nodeName);
    }
    
    if (pUnlinkAll) {
      pLog.log(Kind.Bld, Level.Finer, 
        "Unlinking all sources from (" + nodeName + ") due to TemplateSettings " +
        "Annotation.");
      for (String source : pRegisteredNodeMod.getSourceNames()) {
        pClient.unlink(getAuthor(), getView(), nodeName, source);
      }
    }

    if (pTouchFiles) {
      pLog.log(Kind.Bld, Level.Finer, 
        "Restoring backed-up action to (" + nodeName + ") as part " +
        "of finalization.");
      pRegisteredNodeMod.setAction(pBackedUpAction);
      pRegisteredNodeMod.setActionEnabled(true);
      pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
      pRegisteredNodeMod = 
        pClient.getWorkingVersion(getAuthor(), getView(), nodeName);

      pLog.log(Kind.Bld, Level.Finer, 
        "Touching the files on (" + nodeName + ") to remove staleness as part of " +
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
      pLog.log(Kind.Bld, Level.Finer, 
        "Vouching for the node (" + nodeName + ") as part of finalization.");
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
  
  private TemplateExternalData pExternal;
  
  private NodeMod pSourceMod;
  
  private FrameRange pTemplateRange;
  private FrameRange pActualRange;
  
  private MappedSet<FileSeq, FileSeq> pSecSeqs;
  
  private TreeSet<String> pIgnorableProducts;
  private TreeSet<String> pIgnoredNodes;
  private TreeSet<String> pSkippedNodes;
  
  private BaseAction pBackedUpAction;
}
