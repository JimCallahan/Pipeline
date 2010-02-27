// $Id: MayaRenderAction.java,v 1.1 2009/02/02 22:06:27 jesse Exp $

package us.temerity.pipeline.plugin.MayaPassRenderAction.v2_4_23;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   P A S S   R E N D E R   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Render a group of passes from a particular render layer in a source Maya scene 
 * source node. <P>
 * 
 * In order to use this action, the name of the primary file sequence must be 
 * <i>renderLayer</i>_MasterBeauty. The node must also contain a secondary file sequence for
 * each additional pass, which should be named the same thing as the render pass in Maya.  This
 * action will use which ever renderer is specified in the scene for the particular render 
 * layer.  The passes do not need to be active in the Maya scene; the action will take care 
 * of activating the appropriate passes based on the names of the secondary file sequences.
 *
 * This action defines the following single valued parameters: <BR>
 *
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source Maya scene node.
 *   </DIV> <BR>
 *
 *   Set Project <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to set the Maya project path to the target directory. <BR>
 *   </DIV> <BR>
 *   
 *   Camera Override <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the render camera (if set). <BR>
 *   </DIV> <BR>
 *
 *   Render Layer <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the render layer contain the passes to be rendered.
 *   </DIV> <BR>
 *
 *   Particle Cache <BR>
 *   <DIV style="margin-left: 40px;">
 *     A place to link in a MayaPartCacheGroup node that will set the particle cache
 *     directories.
 *   </DIV> <BR>
 *   <P> 
 *   
 *   Processors <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of processors to use (0 = all available).
 *   </DIV> <BR>
 *   
 *   Verbosity <BR>
 *   <DIV style="margin-left: 40px;">
 *     The level of logging to use when running mental ray renders. <BR>
 *   </DIV> <BR>
 *   <P> 
 *
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR>
 *   </DIV> <BR>
 *   <P> 
 *
 *   Pre Render MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced before rendering begins.
 *   </DIV> <BR>
 *
 *   Post Render MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced after rendering ends.
 *   </DIV> <BR>
 *
 *   Pre Layer MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced before rendering each layer.
 *   </DIV> <BR>
 *
 *   Post Layer MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced after rendering each layer.
 *   </DIV> <BR>
 *
 *   Pre Frame MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced before rendering each frame.
 *   </DIV> <BR>
 *
 *   Post Frame MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced after rendering each frame.
 *     frame. <BR>
 *   </DIV>
 * </DIV> <P>
 */
public
class MayaPassRenderAction
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MayaPassRenderAction()
  {
    super("MayaPassRender", new VersionID("2.4.23"), "Temerity",
          "Renders multiple passes in a render layer from a Maya scene .");

    addMayaSceneParam();
    
    {
      ActionParam param =
        new BooleanActionParam
        (aSetProject,
         "Whether to set the Maya project path to the target directory.",
         false);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
        new StringActionParam
        (aCameraOverride,
         "Overrides the render camera (if set).",
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
        new StringActionParam
        (aRenderLayer,
         "The Render Layer in the maya scene.",
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
        new LinkActionParam
        (aParticleCache,
         "A place to link in a MayaPartCacheGroup node that will set the particle cache " + 
         "directories.",
         null);
      addSingleParam(param);
    }

      
    {
      ActionParam param =
        new IntegerActionParam
        (aProcessors,
         "The number of processors to use (0 = all available).",
         1);
      addSingleParam(param);
    }
      
    {
      String each[] = {"1", "2", "3", "4", "5", "6", "7" };
      ArrayList<String> choices = new ArrayList<String>(Arrays.asList(each));
      ActionParam param =
        new EnumActionParam
        (aVerbosity,
         "The Log verbosity for mental ray renders",
         "3",
         choices);
      addSingleParam(param);
    }     
    

    addExtraOptionsParam();

    
    {
      ActionParam param =
        new LinkActionParam
        (aPreRenderMEL,
         "The MEL script to source before rendering begins.",
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
        new LinkActionParam
        (aPostRenderMEL,
         "The MEL script to source after rendering ends.",
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
        new LinkActionParam
        (aPreLayerMEL,
         "The MEL script to source before rendering each layer.",
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
        new LinkActionParam
        (aPostLayerMEL,
         "The MEL script to source after rendering each layer.",
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
        new LinkActionParam
        (aPreFrameMEL,
         "The MEL script to source before rendering each frame.",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new LinkActionParam
        (aPostFrameMEL,
         "The MEL script to source after rendering each frame.",
         null);
      addSingleParam(param);
    }
    

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aSetProject);
      layout.addEntry(aCameraOverride);
      layout.addEntry(aRenderLayer);
      layout.addEntry(aParticleCache);
      layout.addSeparator();
      layout.addEntry(aProcessors);
      layout.addEntry(aVerbosity);
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout);
      
      {
        LayoutGroup sub =
          new LayoutGroup
          ("MEL Scripts",
           "MEL scripts run at various stages of the rendering process.",
           true);
        sub.addEntry(aPreRenderMEL);
        sub.addEntry(aPostRenderMEL);
        sub.addSeparator();
        sub.addEntry(aPreLayerMEL);
        sub.addEntry(aPostLayerMEL);
        sub.addSeparator();
        sub.addEntry(aPreFrameMEL);
        sub.addEntry(aPostFrameMEL);
        
        layout.addSubGroup(sub);
      }
      
      setSingleLayout(layout);
    }
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will
   * fulfill the given action agenda. <P>
   *
   * @param agenda
   *   The agenda to be accomplished by the action.
   *
   * @param outFile
   *   The file to which all STDOUT output is redirected.
   *
   * @param errFile
   *   The file to which all STDERR output is redirected.
   *
   * @return
   *   The SubProcess which will fulfill the agenda.
   *
   * @throws PipelineException
   *   If unable to prepare a SubProcess due to illegal, missing or incompatible
   *   information in the action agenda or a general failure of the prep method code.
   */
  @Override
  public SubProcessHeavy
  prep
  (
    ActionAgenda agenda,
    File outFile,
    File errFile
  )
    throws PipelineException
  {
    /* the target image sequence */
    FileSeq target = null;
    FilePattern fpat = null;
    FrameRange range = null;
    TreeSet<String> passes = new TreeSet<String>();
    
    String renderLayer = getSingleStringParamValue(aRenderLayer, false);
    {
      String layerPrefix = renderLayer + "_";
      
      target = agenda.getPrimaryTarget();
      fpat = target.getFilePattern();
      range = target.getFrameRange();
      
      String suffix = fpat.getSuffix();
      if(suffix == null)
        throw new PipelineException
          ("The target file sequence (" + target + ") must have a filename suffix!");

      if(!target.hasFrameNumbers())
        throw new PipelineException
          ("The " + getName() + " Action requires that the output images have frame " +
           "numbers.");
      
      if (!fpat.getPrefix().equals(layerPrefix + "MasterBeauty"))
        throw new PipelineException
          ("The MayaPassRenderAction requires that the primary sequence of the render node " +
           "be named (MasterBeauty).");
      
      for (FileSeq secSeq : agenda.getSecondaryTargets()) {
        FilePattern secPat = secSeq.getFilePattern();
        String prefix = secPat.getPrefix();
        prefix = prefix.replaceFirst(layerPrefix, "");
        if (passes.contains(prefix))
          throw new PipelineException
            ("Two render passes are listed as having the same prefix (" + prefix +")");
        String secSuf = secPat.getSuffix();
        if (secSuf == null || !secSuf.equals(suffix))
          throw new PipelineException
            ("The suffix of render pass (" + secSeq +  ") does not match the suffix " +
             "(" + suffix + ") of the primary sequence");
        FrameRange secRange = secSeq.getFrameRange();
        if (!secRange.equals(range))
          throw new PipelineException
            ("The frame range of render pass (" + secSeq + ") does not match the " +
             "frame range (" + range + ") of the primary sequence");
        passes.add(prefix);
      }
      
    }

    /* the source Maya scene */
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    if(sourceScene == null)
      throw new PipelineException
        ("A source MayaScene must be specified!");

    /* MEL script paths */
    Path preRenderMEL  = getMelScriptSourcePath(aPreRenderMEL, agenda);
    Path postRenderMEL = getMelScriptSourcePath(aPostRenderMEL, agenda);
    Path preLayerMEL   = getMelScriptSourcePath(aPreLayerMEL, agenda);
    Path postLayerMEL  = getMelScriptSourcePath(aPostLayerMEL, agenda);
    Path preFrameMEL   = getMelScriptSourcePath(aPreFrameMEL, agenda);
    Path postFrameMEL  = getMelScriptSourcePath(aPostFrameMEL, agenda);
    
    /* toolset environment */
    TreeMap<String,String> env = new TreeMap<String,String>(agenda.getEnvironment());
    
    Path renderPath = agenda.getTargetPath();
    
//    int renderer = getSingleEnumParamIndex(aRenderer); 
    /* renderer command-line arguments */
    ArrayList<String> args = new ArrayList<String>();
    {
      args.add("-renderer");
      args.add("file");

      args.add("-rl");
      args.add(renderLayer);
      
      args.add("-s");
      args.add(String.valueOf(range.getStart()));
      args.add("-e");
      args.add(String.valueOf(range.getEnd()));
      
      args.add("-b");
      args.add(String.valueOf(range.getBy()));
      
      
      NodeID nodeID = agenda.getNodeID();
//      {
//        String topPathDir = nodeID.getWorkingParent().getName();
//        String layerCompare = renderLayer.replace(":", "_");
//        if (!topPathDir.equals(layerCompare))
//          throw new PipelineException
//            ("If render layers are being used, the name of the directory being rendered " +
//             "to must correspond to the name of the render layer.");
//      }
      
      /* the set project option */
      Boolean setProject = getSingleBooleanParamValue(aSetProject);
      if (setProject) {
        args.add("-proj");
        args.add(agenda.getTargetPath().toOsString());
      }
      
      /* cache dir and project stuff */ 
      {
        Path workspaceMel = new Path(getTempPath(agenda), "workspace.mel");
        try {
          FileWriter out = new FileWriter(workspaceMel.toFile());

          String particleSourceName = getSingleStringParamValue(aParticleCache); 
          if (particleSourceName != null) {
            if(PackageInfo.sOsType == OsType.Windows)
              throw new PipelineException
                ("Particle cache setup features not yet supported for Windows!"); 

            ActionInfo info = agenda.getSourceActionInfo(particleSourceName);
            if (!info.getName().equals("MayaPartCacheGroup") || !info.isEnabled()) {
              throw new PipelineException
                ("Only nodes with the MayaPartCacheGroup Action enabled can be hooked into " +
                 "the Particle Cache source param.");
            }
            Path melScript = getMelScriptSourcePath(aParticleCache, agenda);
            BufferedReader in = new BufferedReader(new FileReader(melScript.toFile()));
            String line = in.readLine();
            while (line != null) {
             out.write(line);
             line = in.readLine();
            }
            in.close();
          }
          out.close();
        }
        catch(IOException ex) {
          throw new PipelineException
            ("Unable to write temporary MEL script file (" + workspaceMel + ") for Job " +
             "(" + agenda.getJobID() + ")!\n" + ex.getMessage());
        }
      }

      {
        { 
          File script = createTemp(agenda, "mel");
          try {
            FileWriter out = new FileWriter(script);

            if(preRenderMEL != null)
              out.write("source \"" + preRenderMEL + "\";\n\n");
            
            {
              StringBuffer buf = new StringBuffer();
              buf.append("[");
              boolean first = true;
              for (String s : passes) {
                if (!first)
                  buf.append(", ");
                else
                  first = false;
                buf.append("'" + s + "'");
              }
              buf.append("]");
              
              out.write
                ("{\n" +
                 "string $command = \"import maya.cmds as cmds\\n\" +\n" + 
                 "\"layer = '" + renderLayer + "'\\n\" +\n" + 
                 "\"myPasses = " + buf.toString() + "\\n\" +\n" + 
                 "\"allPasses = cmds.ls(type='renderPass')\\n\" +\n" + 
                 "\"for each in myPasses:\\n\" +\n" + 
                 "\"  if cmds.objExists(each) == 0:\\n\" +\n" + 
                 "\"    raise RuntimeError('The render pass (' + each + ') does not exist')\\n\" +\n" + 
                 "\"  if cmds.nodeType(each) != 'renderPass':\\n\" +\n" + 
                 "\"    raise RuntimeError('The node (' + each + ') is not a valid render pass')\\n\" +\n" + 
                 "\"\\n\" +\n" + 
                 "\"conns = cmds.listConnections(layer + '.renderPass', c=True, p=True)\\n\" +\n" + 
                 "\"print conns\\n\" +\n" + 
                 "\"i = 0\\n\" +\n" +
                 "\"if conns != None:\\n\" +\n" + 
                 "\"  while i < len(conns):\\n\" +\n" + 
                 "\"    source = conns[i]\\n\" +\n" + 
                 "\"    dest = conns[i+1]\\n\" +\n" + 
                 "\"    cmds.disconnectAttr(source, dest)\\n\" +\n" + 
                 "\"    i = i+2\\n\" +\n" + 
                 "\"for each in myPasses:\\n\" +\n" + 
                 "\" cmds.connectAttr(layer + '.renderPass', each + '.owner')\\n\";\n" +
                 "\n" + 
                 "print($command);\n" + 
                 "python($command);\n}\n");
            }
            
            // Set these here because we do not know what renderer we're using.
            Integer procs = (Integer) getSingleParamValue(aProcessors);
            String verb = getSingleStringParamValue(aVerbosity);
            out.write(
              "select defaultRenderGlobals;" +
              "setAttr .numCpusToUse " + procs +";\n" +
              "if(!`about -mac`) {\n" +
              "  threadCount -n " + procs +"; " +
              "}\n\n" +
              "global int $g_mrBatchRenderCmdOption_NumThreadAutoOn = true;\n" +
              "global int $g_mrBatchRenderCmdOption_NumThread = " + procs + ";\n" +
              "global int $g_mrBatchRenderCmdOption_VerbosityOn = true;\n" +
              "global int $g_mrBatchRenderCmdOption_Verbosity = " + verb  + ";\n");

            out.close();
          }
          catch(IOException ex) {
            throw new PipelineException
              ("Unable to write temporary MEL script file (" + script + ") for Job " +
               "(" + agenda.getJobID() + ")!\n" +
               ex.getMessage());
          }
            
          args.add("-preRender");
          args.add("source " + script.getName());
        }

        if(postRenderMEL != null) {
          args.add("-postRender");
          args.add(wrapperMEL(agenda, postRenderMEL));
        }

        if(preLayerMEL != null) {
          args.add("-preLayer");
          args.add(wrapperMEL(agenda, preLayerMEL));
        }

        if(postLayerMEL != null) {
          args.add("-postLayer");
          args.add(wrapperMEL(agenda, postLayerMEL));
        }

        if(preFrameMEL != null) {
          args.add("-preFrame");
          args.add(wrapperMEL(agenda, preFrameMEL));
        }

        {
          File script = createTemp(agenda, "mel");
          try {
            FileWriter out = new FileWriter(script);

            if(postFrameMEL != null)
              out.write("source \"" + postFrameMEL + "\";\n\n");
            
            out.close();
          }
          catch(IOException ex) {
            throw new PipelineException
              ("Unable to write temporary MEL script file (" + script + ") for Job " +
               "(" + agenda.getJobID() + ")!\n" +
               ex.getMessage());
          }
          args.add("-postFrame");
          args.add("source " + script.getName());

        }

        {
          Path tempPath = getTempPath(agenda);
          String ospath = env.get("MAYA_SCRIPT_PATH");
          if(ospath != null) {
            env.put("MAYA_SCRIPT_PATH",
              tempPath.toOsString() + File.pathSeparator + ospath);
          }
          else {
            env.put("MAYA_SCRIPT_PATH", tempPath.toOsString());
          }
        }
        
        args.add("-pad");
        args.add(String.valueOf(fpat.getPadding()));
      }

      args.add("-of");
      args.add(fpat.getSuffix());

      args.add("-fnc");
      args.add("3");

      args.add("-rd");
      args.add(renderPath.toOsString());

      args.add("-im");
      args.add("<RenderLayer>_<RenderPass>");
      
      {
        String camera = getSingleStringParamValue(aCameraOverride);
        if(camera != null) {
          args.add("-cam");
          args.add(camera);
        }
      }

      args.addAll(getExtraOptionsArgs());

      args.add(sourceScene.toOsString());
    }
    
    String program = "Render";
    if (PackageInfo.sOsType == OsType.Windows) {
      program = "Render.exe";
        File pyScript = createTemp(agenda, "py");
        try {
          FileWriter out = new FileWriter(pyScript);
          out.write("import os\n");
          out.write("import shutil\n");
          out.write(getPythonLaunchHeader());
          out.write(createMayaRenderPythonLauncher(args) + "\n");
          out.close();
        } 
        catch (IOException ex) {
          throw new PipelineException
            ("Unable to write temporary MEL script file (" + pyScript + ") for Job " +
             "(" + agenda.getJobID() + ")!\n" +
             ex.getMessage());
        }
        return createPythonSubProcess(agenda, pyScript, null, env, outFile, errFile);
      }
    else {
      /* create the process to run the action */
      return createSubProcess(agenda, program, args, env,
          agenda.getTargetPath().toFile(), outFile, errFile);
    }
  }

  /**
   * Creates a temporary MEL script which simply sources the given script. <P>
   *
   * This allows MEL scripts with full path names and "-" characters to be passed as arguments
   * to the various pre/post MEL script options.  It also means that only the temporary
   * directory for the job needs to be added to the MAYA_SCRIPT_PATH.
   *
   * @param mel
   *   The MEL script to source.
   *
   * @return
   *   The name of the temporary wrapper MEL script.
   */
  private String
  wrapperMEL
  (
   ActionAgenda agenda,
   Path mel 
  )
    throws PipelineException
  {
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script);

      out.write("source \"" + mel + "\";\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary MEL script file (" + script + ") for Job " +
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }

    return ("source " + script.getName());
  }
  
  /** 
   * A convenience method for creating the Python code equivalent of 
   * {@link #createMayaSubProcess createMayaSubProcess} suitable for inclusion in an 
   * temporary Python script.<P> 
   * 
   * The returned OS level process will run Maya in batch mode to optionally load a input
   * Maya scene and then perform some operations specified by a dynamically created MEL 
   * script. <P> 
   * 
   * The Python code generated by this method requires the "launch" method defined by {@link 
   * PythonActionUtils#getPythonLaunchHeader PythonActionUtils.getPythonLaunchHeader}.  You 
   * must first include the code generate by <CODE>getPythonLaunchHeaderget</CODE> before 
   * the code generated by this method.<P> 
   * 
   * @param args
   *   Arguments to the "Render" command.
   */ 
  public static String
  createMayaRenderPythonLauncher
  (
   ArrayList<String> args
  ) 
  {
    StringBuilder buf = new StringBuilder();
    
    String maya = "Render";
    if(PackageInfo.sOsType == OsType.Windows) 
      maya = "Render.exe"; 

    buf.append("launch('" + maya + "', [");
    
    for (int i = 0; i < args.size(); i++) {
      String arg = args.get(i);
      if (arg.contains("\\"))
        arg = escPath(arg);
      buf.append("'" + arg + "'");
      if (i != args.size() -1)
        buf.append(",");
    }

    buf.append("])\n");

    return buf.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3056963679422691286L;
  
  public static final String aCameraOverride = "CameraOverride";
  public static final String aRenderLayer    = "RenderLayer";
  public static final String aParticleCache  = "ParticleCache";
  public static final String aVerbosity      = "Verbosity";
  public static final String aProcessors     = "Processors";
  public static final String aPreRenderMEL   = "PreRenderMEL";
  public static final String aPostRenderMEL  = "PostRenderMEL";
  public static final String aPreLayerMEL    = "PreLayerMEL";
  public static final String aPostLayerMEL   = "PostLayerMEL";
  public static final String aPreFrameMEL    = "PreFrameMEL";
  public static final String aPostFrameMEL   = "PostFrameMEL";
  
  public static final String aSetProject     = "SetProject";

}
