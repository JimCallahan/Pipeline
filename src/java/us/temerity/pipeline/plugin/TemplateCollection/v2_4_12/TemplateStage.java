package us.temerity.pipeline.plugin.TemplateCollection.v2_4_12;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   S T A G E                                                            */
/*------------------------------------------------------------------------------------------*/

public 
class TemplateStage
  extends StandardStage
  implements FinalizableStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  private
  TemplateStage
  (
    TemplateNode sourceNode,
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
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    FrameRange templateRange,
    TemplateExternalData external,
    TreeMap<String, Integer> offsets,
    TemplateParamManifest paramManifest,
    TemplateDescManifest descManifest,
    TreeMap<String, TemplateNode> nodeDatabase
  ) 
    throws PipelineException
  {
    super("Template", 
          "Stage for use with the Template Builder", 
          stageInfo, context, client, 
          nodeName, (templateRange != null) ? templateRange : range, padding, suffix, 
          editor, action);
    
    pTemplateNode = sourceNode;
    pSourceMod = sourceNode.getNodeMod();
    pReplacements = stringReplacements;
    pContexts = contexts;
    pNodeDatabase = nodeDatabase;
    pTemplateRange = templateRange;
    pActualRange = (templateRange != null) ? templateRange : range;
    pInhibitCopyFiles = inhibitCopy;
    pExternal = external;
    pOffsets = offsets;
    pParamManifest = paramManifest;
    pDescManifest = descManifest;
    
    
    init();
  }
  
  private
  TemplateStage
  (
    TemplateNode sourceNode,
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String suffix,
    PluginContext editor,
    PluginContext action,
    boolean inhibitCopy,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    FrameRange templateRange,
    TemplateExternalData external,
    TreeMap<String, Integer> offsets,
    TemplateParamManifest paramManifest,
    TemplateDescManifest descManifest,
    TreeMap<String, TemplateNode> nodeDatabase
  ) 
    throws PipelineException
  {
    super("Template", 
          "Stage for use with the Template Builder", 
          stageInfo, context, client, 
          nodeName, suffix, editor, action);
    
    pTemplateNode = sourceNode;
    pSourceMod = sourceNode.getNodeMod();
    pReplacements = stringReplacements;
    pContexts = contexts;
    pNodeDatabase = nodeDatabase;
    pTemplateRange = templateRange;
    pActualRange = null;
    pInhibitCopyFiles = inhibitCopy;
    pExternal = external;
    pOffsets = offsets;
    pParamManifest = paramManifest;
    pDescManifest = descManifest;
    
    init();
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
   */
  @SuppressWarnings({ "unchecked", "deprecation" })
  private void 
  init() 
    throws PipelineException
  {
    BaseAction act = pSourceMod.getAction();
    
    String nodeName = getNodeName();
    if (pExternal != null && act != null)
      throw new PipelineException
        ("The node (" + nodeName + ") has an Action assigned to it, " +
         "but also has a TemplateExternal annotation associated with it.  Nodes with " +
         "TemplateExternal annotations cannot have Actions.");
    
    if (pExternal != null && PackageInfo.sOsType != OsType.Unix)
      throw new PipelineException
        ("The node (" + nodeName + ") has a TemplateExternal annotation " +
         "associated with it, but this builder is not being run on a Unix/Linux machine. " +
         "External Sequence support only exists on Unix-derivative operating systems" );
    
    String manifest = pTemplateNode.getManifestType();
    if (manifest != null) {
      if (pSourceMod.getPrimarySequence().hasFrameNumbers())
        throw new PipelineException
          ("The node (" + nodeName + ") has a TemplateManifest annotation on it, " +
           "but also has frame numbers.  This situation is not supported by the template " +
           "builder.");
      if (act != null) 
        throw new PipelineException
          ("The node (" + nodeName + ") has a TemplateManifest annotation on it, " +
           "but also has an action.  This situation is not supported by the template " +
           "builder.");
    }
    
    setIntermediate(pTemplateNode.isIntermediate());
    
    pUnlinkNodes = new MappedSet<String, String>();
    pTemplateNodesToUnlink = pTemplateNode.getNodesToUnlink();
    pSecSeqs = new MappedSet<FileSeq, FileSeq>();
    pAllLinkNames = new TreeSet<String>();
    
    pLateFileCopy = false;
    if (pTemplateNode.cloneFiles() && pTemplateNode.touchFiles())
      pLateFileCopy = true;
   
    for (FileSeq seq : pSourceMod.getSecondarySequences()) {
      String filePat = seq.getFilePattern().toString();
      Set<String> secContexts = pTemplateNode.getSecondaryContextsForSequence(filePat);
      LinkedList<TreeMap<String, String>> secReplacements =
        allContextReplacements(secContexts);
      
      for (TreeMap<String, String> replacements : secReplacements) {
        FileSeq targetSeq = stringReplaceSeq(seq, replacements);
        if (pActualRange != null) {
          targetSeq = new FileSeq(targetSeq.getFilePattern(), pActualRange);
        }
        LogMgr.getInstance().log(Kind.Bld, Level.Finest,
          "Adding the secondary sequence: " + targetSeq);
        addSecondarySequence(targetSeq);
        pSecSeqs.put(seq, targetSeq);  
      }
    }
    
    for (Entry<String, TemplateLink> entry : pTemplateNode.getSources().entrySet()) {
      String linkName = entry.getKey();
      TemplateNode linkNode = pNodeDatabase.get(linkName);
      TemplateLink link = entry.getValue();
      
      pLog.log(Kind.Bld, Level.Finest, 
        "Checking the link: " + linkName);
      
      if (linkNode.wasSkipped()) {
        pLog.log(Kind.Bld, Level.Fine,
          "The linked node was skipped during construction and the link is being ignored.");
        continue;
      }
      
      LinkMod linkMod = pTemplateNode.getNodeMod().getSource(linkName); 
      
      TreeSet<String> contexts = null;
      boolean ignoreable = false;
      if (linkNode.isInTemplate())
        contexts = new TreeSet<String>(linkNode.getContexts());
      else {
        contexts = new TreeSet<String>(link.getContexts());
        ignoreable = link.isIgnorable();
      }
      if (contexts.size() == 0)
        createLink(linkNode, linkMod, act, pReplacements, ignoreable);
      else
        contextLink(linkNode, linkMod, act, new TreeSet<String>(contexts), 
                    pReplacements, ignoreable);
    }
    
    pSrcHasDisabledAction = !(pSourceMod.isActionEnabled()); 
    
    if (act != null) {
      for (ActionParam param : act.getSingleParams()) {
        Comparable value = stringReplaceParamValue(param, pReplacements);
        if (param instanceof LinkActionParam && value != null) {
          if (!pAllLinkNames.contains(value))
            continue;
        }
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
      
      setJobReqs(pSourceMod.getJobRequirements());
      ExecutionMethod exec = pSourceMod.getExecutionMethod();
      setExecutionMethod(exec);
      if (exec == ExecutionMethod.Parallel)
        setBatchSize(pSourceMod.getBatchSize());
    }
    
    {
      TreeMap<String, BaseAnnotation> perNode = 
        pClient.getAnnotations(pSourceMod.getName()); 
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
      TreeMap<String, BaseAnnotation> perVersion = pSourceMod.getAnnotations();
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
    
    {
      JobReqs reqs = pSourceMod.getJobRequirements();
      if (reqs != null) {
        addSelectionKeys(new TreeSet<String>(reqs.getSelectionKeys()));
        addLicenseKeys(new TreeSet<String>(reqs.getLicenseKeys()));
        addHardwareKeys(new TreeSet<String>(reqs.getHardwareKeys()));
      }
    }
  }
  
  /**
   * Construct a new Template Stage.
   * 
   * @param sourceNode
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
   * @param inhibitCopy
   *   Whether the CloneFiles template setting should be ignored.
   *   
   * @param external
   *   The external file sequence to link this node to or <code>null</code> if there is no
   *   external sequence for this node.
   *   
   * @param offsets
   *   The frame offset values indexed by the name of the frame offset.
   *   
   * @param paramManifest
   *   The parameter manifest for this template.
   *   
   * @param descManifest
   *   The description manifest for this template.
   *   
   * @param nodeDirectory
   *   The directory of all the template nodes in the project.
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
    TemplateNode sourceNode,
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
    FrameRange range,
    boolean inhibitCopy,
    TemplateExternalData external,
    TreeMap<String, Integer> offsets,
    TemplateParamManifest paramManifest,
    TemplateDescManifest descManifest,
    TreeMap<String, TemplateNode> nodeDirectory
  ) 
    throws PipelineException
  {
    String nodeName = stringReplace(sourceNode.getNodeName(), stringReplacements);
    NodeMod sourceMod = sourceNode.getNodeMod();
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
        editor = 
          new PluginContext(ed.getName(), ed.getVendor(), new Range<VersionID>(ver, ver));
      }
    }
    
    PluginContext action = null;
    {
      BaseAction act = sourceMod.getAction();
      if (act != null) {
        VersionID ver = act.getVersionID();
        action = 
          new PluginContext(act.getName(), act.getVendor(), new Range<VersionID>(ver, ver));
      }
    }
    
    String toolset = sourceMod.getToolset();
    UtilContext newContext = new UtilContext(context.getAuthor(), context.getView(), toolset);
    
    if (priSeq.hasFrameNumbers()) {
      int padding = pat.getPadding();
      FrameRange oldRange = priSeq.getFrameRange();
      return new TemplateStage
        (sourceNode, stageInfo, newContext, client, nodeName, oldRange, padding, suffix, 
         editor, action, inhibitCopy, stringReplacements, contexts, range, external, offsets, 
         paramManifest, descManifest, nodeDirectory);
    }
    else 
      return new TemplateStage
        (sourceNode, stageInfo, newContext, client, nodeName, suffix, editor, action, 
         inhibitCopy, stringReplacements, contexts, range, external, offsets, 
         paramManifest, descManifest, nodeDirectory);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   B U I L D                                                                            */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public boolean 
  build()
    throws PipelineException
  {
    boolean build = super.build();
    if (build) {
      String nodeName = getNodeName();
      if (pSrcHasDisabledAction && !pTemplateNode.preEnableAction()) {
        pLog.log(Kind.Bld, Level.Finer, 
          "Disabling the action after building the node.");
        pRegisteredNodeMod.setActionEnabled(false);
        pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
        pRegisteredNodeMod = pClient.getWorkingVersion(getAuthor(), getView(), nodeName);
      }
      if (pTemplateNode.cloneFiles()  && !pInhibitCopyFiles) {
        NodeID src = new NodeID(getAuthor(), getView(), pSourceMod.getName());
        NodeID tar = getNodeID();
        
        FrameRange srcRange = pSourceMod.getPrimarySequence().getFrameRange();
        pTgtRange = pRegisteredNodeMod.getPrimarySequence().getFrameRange();
        
        if (pTgtRange != null && srcRange != null) {
          for (int frame : pTgtRange.getFrameNumbers()) {
            if (!srcRange.isValid(frame))
              throw new PipelineException
              ("The target frame range (" + pTgtRange + ") contains frames that are not in " +
                "the source frame range (" + srcRange + ").  Clone files cannot continue.");
          }

          if (!pLateFileCopy) {
            pLog.log(Kind.Bld, Level.Finer, 
              "Cloning the files after building the node.");
            pClient.cloneFiles(src, tar, pSecSeqs, pTgtRange, pTgtRange);
          }
        }
        else {
          if (!pLateFileCopy) {
            pLog.log(Kind.Bld, Level.Finer, 
              "Cloning the files after building the node.");
            pClient.cloneFiles(src, tar, pSecSeqs);
          }
        }
        
        if (pTemplateNode.modifyFiles() && !pLateFileCopy) {
          stringReplaceFiles();
        }
      }
      
      if (pTemplateNode.touchFiles()) {
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
      
      String manifest = pTemplateNode.getManifestType();
      if (manifest != null) {
        Path wpath = new Path(PackageInfo.sProdPath, getNodeID().getWorkingParent());
        
        wpath.toFile().mkdirs();
        
        Path path = new Path(wpath, pRegisteredNodeMod.getPrimarySequence().getPath(0));
        File file = path.toFile();
        
        if (manifest.equals("Param")) {
          try {
            GlueEncoderImpl.encodeFile("ParamManifest", pParamManifest, file);
          }
          catch (GlueException ex) {
            throw new PipelineException
              ("Error while writing the param manifest glue file.", ex);
          }
        }
        else if (manifest.equals("Desc")) {
          try {
            GlueEncoderImpl.encodeFile("DescManifest", pDescManifest, file);
          }
          catch (GlueException ex) {
            throw new PipelineException
              ("Error while writing the desc manifest glue file.", ex);
          }
        }
        else
          throw new PipelineException("Invalid manifest type (" + manifest + ")");
        
      }
      
    } //if (build)
    return build;
  }
  
  
  @SuppressWarnings("unchecked")
  private String 
  createLink
  (
    TemplateNode linkNode,
    LinkMod link,
    BaseAction act,
    TreeMap<String, String> replacements, 
    boolean ignoreable
  )
    throws PipelineException
  {
    String oldSrc = link.getName();
    
    String newSrc = stringReplace(oldSrc, replacements);
    if (linkNode.getSkippedNodes().contains(newSrc)) {
      pLog.log(Kind.Bld, Level.Fine, 
        "Not linking source node (" + newSrc + ") because the template skipped building it.");
      return null;
    }
    
    if (!nodeExists(newSrc) && ignoreable) {
      pLog.log(Kind.Bld, Level.Fine, 
        "Not linking source node (" + newSrc + ") because it doesn't exist");
      return null;
    }
    
    TemplateLink tlink = pTemplateNode.getSource(oldSrc);
    
    Integer offset = null;
    if (link.getRelationship() == LinkRelationship.OneToOne) {
      offset = link.getFrameOffset();
      String oName = tlink.getOffset();
      if (oName != null ) {
        oName = stringReplace(oName, replacements);
        Integer newOffset = pOffsets.get(oName);
        if (newOffset != null)
          offset = newOffset;
      }
    }
    
    
    LinkMod newLink = 
      new LinkMod(newSrc, link.getPolicy(), 
                  link.getRelationship(), offset);
    pLog.log(Kind.Bld, Level.Fine, 
      "Linking source node (" + newSrc + ").");
    addLink(newLink);
    pAllLinkNames.add(newSrc);
    
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
            Set<String> secContexts = linkNode.getSecondaryContexts();
            LinkedList<TreeMap<String, String>> secReplacements = 
              allContextReplacements(secContexts);
            for (TreeMap<String, String> secReplace : secReplacements) {
              FilePattern newPat = stringReplacePat(oldPat, secReplace);
              for (String srcParam : sourceParams) {
                ActionParam param = act.getSecondarySourceParam(oldSrc, oldPat, srcParam);
                Comparable value = stringReplaceParamValue(param, replacements);
                addSecondarySourceParamValue(newSrc, newPat, srcParam, value);
              }  
            }
          }
        }
      }
    return newSrc;
  }
  
  private void
  contextLink
  (
    TemplateNode linkNode,
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
      pLog.logAndFlush(Kind.Bld, Level.Warning, 
        "The context (" + currentContext + ") specified on (" + link.getName() + ") has " +
        "no values defined for it.");
      TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
      if (contextList.isEmpty()) {  //bottom of the recursion
        createLink(linkNode, link, act, newReplace, ignoreable);
      }
      else {
        contextLink(linkNode, link, act, new TreeSet<String>(contextList), 
          newReplace, ignoreable);
      }
      return;
    }
    
    for (TreeMap<String, String> contextEntry : values) {
      TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
      newReplace.putAll(contextEntry);
      if (contextList.isEmpty()) {  //bottom of the recursion
        createLink(linkNode, link, act, newReplace, ignoreable);
      }
      else {
        contextLink(linkNode, link, act, new TreeSet<String>(contextList), 
                    newReplace, ignoreable);
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   B U I L D   S U B   P R O C E S S E S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return a subprocess which, when run, will touch all the files for the node.
   */
  private SubProcessLight
  touchFilesProcess()
    throws PipelineException
  {
    NodeID nodeID = getNodeID();
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
  
  private void
  stringReplaceFiles()
    throws PipelineException
  {
    pLog.log
      (Kind.Bld, Level.Fine, "String replacing inside the files.");
    
    NodeID nodeID = getNodeID();
    
    Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());
    
    for(Path fpath : pRegisteredNodeMod.getPrimarySequence().getPaths()) {
      stringReplaceFile(wpath, fpath);
    }
    SortedSet<FileSeq> secSeqs = pRegisteredNodeMod.getSecondarySequences();
    for (FileSeq sSeq : secSeqs) {
      for(Path fpath : sSeq.getPaths()) {
        stringReplaceFile(wpath, fpath);
      }
    }
  }
    
  private void
  stringReplaceFile
  (
    Path wpath,
    Path fpath
  )
    throws PipelineException
  {
    File sFile = new Path(wpath, fpath.toString() + ".temp").toFile();
    File f = new Path(wpath, fpath).toFile();
    boolean success = f.renameTo(sFile);
    if (!success)
      throw new PipelineException
        ("Error copying the source file (" + f + ") to the backup location " +
         "(" + sFile +") before string editing.");
    try {
      BufferedReader in = new BufferedReader(new FileReader(sFile));
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
      String line = in.readLine();
      while (line != null) {
        out.println(stringReplace(line, pReplacements));
        line = in.readLine();
      }
      out.close();
    }
    catch (FileNotFoundException ex) {
      throw new PipelineException
        ("Somehow the backup file we just copied does not exist!  " + ex.getMessage());
    }
    catch (IOException ex) {
      throw new PipelineException
        ("An error occurred while processing the file (" + f + ").\n" + ex.getMessage());
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
    if (pTemplateNode.unlinkAll() || pTemplateNode.postRemoveAction()|| 
        pTemplateNode.postDisableAction() || !pUnlinkNodes.isEmpty() ||
        pTemplateNode.getLinkSync() != null)
      return true;
    
    return false;
  }

  public boolean
  needsSecondFinalization()
  {
    if (pTemplateNode.touchFiles())
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
    boolean changed = false;
    String nodeName = getNodeName();
    if (pTemplateNode.postDisableAction()) {
      pLog.log(Kind.Bld, Level.Finer, 
        "Disabling the action on (" + nodeName + ") as part of finalization.");
      pRegisteredNodeMod.setActionEnabled(false);
      changed = true;
    }
    if (pTemplateNode.postRemoveAction()) {
      pLog.log(Kind.Bld, Level.Finer, 
        "Removing the action on (" + nodeName + ") as part of finalization.");
      pRegisteredNodeMod.setAction(null);
      changed = true;
    }
    if (changed) {
      pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
      pRegisteredNodeMod = getWorkingVersion(nodeName); 
    }

    if (pTemplateNode.unlinkAll()) {
      pLog.log(Kind.Bld, Level.Finer, 
        "Unlinking all sources from (" + nodeName + ") due to TemplateSettings " +
        "Annotation.");
      for (String source : pRegisteredNodeMod.getSourceNames()) {
        pClient.unlink(getAuthor(), getView(), nodeName, source);
      }
      pRegisteredNodeMod = getWorkingVersion(nodeName); 
    }
    else if (!pUnlinkNodes.isEmpty()) {
      for (String oldSrc : pUnlinkNodes.keySet()) {
        for (String newSrc : pUnlinkNodes.get(oldSrc)) {
          pLog.log(Kind.Bld, Level.Finer, 
            "Unlinking the node (" + newSrc + ") from (" + nodeName + ") due to " +
            "an unlink annotation.");
          pClient.unlink(getAuthor(), getView(), nodeName, newSrc);
        }
      }
      pRegisteredNodeMod = getWorkingVersion(nodeName);
    }
    
    String linkSync = pTemplateNode.getLinkSync();
    linkSync = stringReplace(linkSync, pReplacements);
    if (linkSync != null) {
      pLog.log(Kind.Bld, Level.Finer, 
        "Syncing the links with (" + linkSync +")");
      if (!workingVersionExists(linkSync)) {
        pLog.log(Kind.Bld, Level.Warning,
          "No working version of the node (" + linkSync +") which was supposed to be used " +
          "to sync the links of (" + nodeName +") exists.");
      }
      else {
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), linkSync);
        for (LinkMod link : mod.getSources()) {
          pClient.link(getAuthor(), getView(), nodeName, link);
        }
        pRegisteredNodeMod = getWorkingVersion(nodeName);
      }
    }
  }
  
  public void
  secondFinalizeStage()
    throws PipelineException
  {
    if (pTemplateNode.touchFiles()) {
      String nodeName = getNodeName();
      pLog.log(Kind.Bld, Level.Finer, 
        "Restoring backed-up action to (" + nodeName + ") as part " +
        "of finalization.");
      pRegisteredNodeMod.setAction(pBackedUpAction);
      pRegisteredNodeMod.setActionEnabled(true);
      pClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
      pRegisteredNodeMod = 
        pClient.getWorkingVersion(getAuthor(), getView(), nodeName);
      
      try {
        Thread.sleep(5000l);
      }
      catch (InterruptedException ex1) {
        throw new PipelineException
          ("Interrupted while pausing before touching files.\n" + ex1.getMessage());
      }

      if (!pLateFileCopy) {
        pLog.log(Kind.Bld, Level.Finer, 
          "Touching the files on (" + nodeName + ") to remove staleness as part of " +
          "finalization.");
        if (PackageInfo.sOsType != OsType.Windows) {
          SubProcessLight process = touchFilesProcess();
          process.run();
          try {
            process.join();
          }
          catch (InterruptedException ex) {
            String message = "The touch subprocess for node () failed.\n";
            throw new PipelineException(Exceptions.getFullMessage(message, ex));
          }
          Integer exit = process.getExitCode();
          if (exit == null || exit != 0)
            throw new PipelineException
            ("The touch subprocess did not finish correctly\n" +
              process.getStdOut() + "\n" + process.getStdErr());
        }
        else {
          NodeID nodeID = getNodeID();
          long currentTime = System.currentTimeMillis();
          Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());
          
          wpath.toFile().mkdirs();
          
          for(Path target : pRegisteredNodeMod.getPrimarySequence().getPaths()) {
            Path path = new Path(wpath, target);
            File file = path.toFile();
            file.setWritable(true, true);
            file.setLastModified(currentTime);
            file.setWritable(false);
          }

          for(FileSeq fseq : pRegisteredNodeMod.getSecondarySequences()) {
            for(Path target : fseq.getPaths()) {
              Path path = new Path(wpath, target);
              File file = path.toFile();
              file.setWritable(true, true);
              file.setLastModified(currentTime);
              file.setWritable(false);

            }
          }
        }
      }
      else {
        pLog.log(Kind.Bld, Level.Finer, 
          "Cloning the files for(" + nodeName + ") to remove staleness as part " +
          "of finalization.");

        NodeID src = new NodeID(getAuthor(), getView(), pSourceMod.getName() );
        NodeID tar = getNodeID();

        if (pTgtRange != null)
          pClient.cloneFiles(src, tar, pSecSeqs, pTgtRange, pTgtRange);
        else
          pClient.cloneFiles(src, tar, pSecSeqs);
        
        if (pTemplateNode.modifyFiles())
          stringReplaceFiles();
      }
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
   *   
   * @param replacements
   *   The list of replacements to use.
   */
  private FileSeq
  stringReplaceSeq
  (
    FileSeq seq,
    TreeMap<String, String> replacements
  )
  {
    FilePattern pat = seq.getFilePattern();
    String prefix = pat.getPrefix();
    prefix = stringReplace(prefix, replacements);
    String suffix = pat.getSuffix();
    if (suffix != null)
      suffix = stringReplace(suffix, replacements);
    
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
   *   
   * @param replacements
   *   The list of replacements to use.
   */
  private FilePattern
  stringReplacePat
  (
    FilePattern pat,
    TreeMap<String, String> replacements
  )
  {
    String prefix = pat.getPrefix();
    prefix = stringReplace(prefix, replacements);
    String suffix = pat.getSuffix();
    if (suffix != null)
      suffix = stringReplace(suffix, replacements);
    
    return new FilePattern(prefix, pat.getPadding(), suffix);
  }
  
  /**
   * Modify the Action Param value, replacing each key with its mapped value.
   * 
   * @param param
   *   The Action Param to be modified;
   * 
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
        param instanceof TextAreaAnnotationParam ||
        param instanceof ParamNameAnnotationParam) 
      value = stringReplace((String) value, pReplacements);
    
    return value;
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2958888238062054760L;

  public static final String aStartFrame = "StartFrame";
  public static final String aEndFrame   = "EndFrame";
  public static final String aByFrame    = "ByFrame";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeMap<String, TemplateNode> pNodeDatabase;
  
  private TreeMap<String, String> pReplacements;
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  private TreeMap<String, Integer> pOffsets;
  
  private boolean pInhibitCopyFiles;
  
  private boolean pSrcHasDisabledAction;
  
  private TemplateExternalData pExternal;
  private TemplateNode pTemplateNode;
  private NodeMod pSourceMod;
  private FrameRange pTemplateRange;
  private FrameRange pActualRange;
  
  private MappedSet<FileSeq, FileSeq> pSecSeqs;
  private MappedSet<String, String> pUnlinkNodes;
  private Set<String> pTemplateNodesToUnlink;
  private TreeSet<String> pAllLinkNames;
  
  private boolean pLateFileCopy;
  
  private FrameRange pTgtRange;

  private BaseAction pBackedUpAction;
  
  /*
   * Template manifests.  
   */
  private TemplateDescManifest pDescManifest;
  private TemplateParamManifest pParamManifest;
}
