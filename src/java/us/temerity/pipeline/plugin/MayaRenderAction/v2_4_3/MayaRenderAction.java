// $Id: MayaRenderAction.java,v 1.3 2008/09/15 17:36:08 jesse Exp $

package us.temerity.pipeline.plugin.MayaRenderAction.v2_4_3;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.MayaActionUtils;
import us.temerity.pipeline.plugin.PythonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a series of images from a source Maya scene source node. <P>
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
 *     If the Maya scene has render layers, you can specify here which render layer you
 *     want to render.  Note that due to the naming restrictions that Maya imposes, the
 *     name of the render layer must match the directory that you are attempting to
 *     render into.  There is no restriction on the actual image name.  If you are
 *     attempting to render a scene with render layers without specifying this flag,
 *     your job will not produce frames in the right location.<BR><BR>
 *     If this is a 3delight render, this represents the name of the 3delight render pass
 *     to render.
 *   </DIV> <BR>
 *
 *   Particle Cache <BR>
 *   <DIV style="margin-left: 40px;">
 *     A place to link in a MayaPartCacheGroup node that will set the particle cache
 *     directories.
 *   </DIV> <BR>
 *   <P> 
 *   
 *   Renderer <BR>
 *   <DIV style="margin-left: 40px;">
 *     The type of renderer used to render the images: Hardware, Software, Mental Ray, 
 *     Vector, or 3Delight<BR>
 *   </DIV> <BR>
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
class MayaRenderAction
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MayaRenderAction()
  {
    super("MayaRender", new VersionID("2.4.3"), "Temerity",
          "Renders a Maya scene.");

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
         "The Render Layer in the maya scene or the 3delight render pass that should be " +
         "rendered.  Leave this blank to ignore renderlayers.",
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
      ArrayList<String> names = new ArrayList<String>();
      names.add("Hardware");
      names.add("Software");
      names.add("Mental Ray");
      names.add("Vector");
      names.add("3delight");
        
      ActionParam param =
        new EnumActionParam
        (aRenderer,
         "The type of renderer to use.",
         "Software", names);
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
         "The Log verbosity for mental ray renders or 3delight renders (0-3 valid only)",
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
      layout.addEntry(aRenderer);
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
    {
      target = agenda.getPrimaryTarget();
      String suffix = target.getFilePattern().getSuffix();
      if(suffix == null)
        throw new PipelineException
          ("The target file sequence (" + target + ") must have a filename suffix!");

      if(!target.hasFrameNumbers())
        throw new PipelineException
          ("The " + getName() + " Action requires that the output images have frame " +
           "numbers.");
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
    
    String renderLayer = getSingleStringParamValue(aRenderLayer);
    boolean hasRenderLayers = false;
    if (renderLayer != null)
      hasRenderLayers = true;
    Path renderPath = agenda.getTargetPath();
    
    int renderer = getSingleEnumParamIndex(aRenderer); 
    /* renderer command-line arguments */
    ArrayList<String> args = new ArrayList<String>();
    {
      args.add("-renderer");

      switch(renderer) {
      case 0: // Hardware
        args.add("hw");
        break;

      case 1: // Software
        args.add("sw");
        break;

      case 2: // Mental Ray
        args.add("mr");
        break;

      case 3: // Vector
        args.add("vr");
        break;
        
      case 4: //3delight
        args.add("3delight");
        break;

      default:
        throw new PipelineException("Unsupported Renderer type!");
      }

      
      if (renderLayer != null ) {
        if (renderer == 4) {
          args.add("-rp");
        }
        else {
          args.add("-rl");
        }
        args.add(renderLayer);
      }
      
      FrameRange range = target.getFrameRange();
      FilePattern fpat = target.getFilePattern();
      
      boolean fixPadding = (renderer == 0 || renderer == 3  ) ? true : false;

      args.add("-s");
      args.add(String.valueOf(range.getStart()));
      args.add("-e");
      args.add(String.valueOf(range.getEnd()));
      
      if (renderer == 4)
        args.add("-inc");
      else
        args.add("-b");
      args.add(String.valueOf(range.getBy()));
      
      if (renderer == 4) {
        args.add("-an");
        args.add("true");
      }

      
      NodeID nodeID = agenda.getNodeID();
      {
        if (renderLayer != null && renderer != 4) {
          String topPathDir = nodeID.getWorkingParent().getName();
          if (!topPathDir.equals(renderLayer))
            throw new PipelineException
              ("If render layers are being used, the name of the directory being rendered " +
               "to must correspond to the name of the render layer.");

          if (PackageInfo.sOsType != OsType.Windows)
            renderPath = renderPath.getParentPath();
        }
      }
      
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

            if (fixPadding)
              out.write("setAttr \"defaultRenderGlobals.extensionPadding\" " + 
                        "" + fpat.getPadding() + ";\n");
            
            out.close();
          }
          catch(IOException ex) {
            throw new PipelineException
              ("Unable to write temporary MEL script file (" + script + ") for Job " +
               "(" + agenda.getJobID() + ")!\n" +
               ex.getMessage());
          }
            
          args.add("-preRender");
          if(PackageInfo.sOsType == OsType.Windows && !hasRenderLayers)
            args.add("\"source " + script.getName() + "\"");
          else
            args.add("source " + script.getName());
        }

        if(postRenderMEL != null) {
          args.add("-postRender");
          args.add(wrapperMEL(agenda, postRenderMEL, hasRenderLayers));
        }

        if(preLayerMEL != null) {
          args.add("-preLayer");
          args.add(wrapperMEL(agenda, preLayerMEL, hasRenderLayers));
        }

        if(postLayerMEL != null) {
          args.add("-postLayer");
          args.add(wrapperMEL(agenda, postLayerMEL, hasRenderLayers));
        }

        if(preFrameMEL != null) {
          args.add("-preFrame");
          args.add(wrapperMEL(agenda, preFrameMEL, hasRenderLayers));
        }

        if (renderLayer != null && PackageInfo.sOsType == OsType.Windows && renderer != 4) {
          File script = createTemp(agenda, "mel");
          try {
            FileWriter out = new FileWriter(script);

            if(preRenderMEL != null)
              out.write("source \"" + preRenderMEL + "\";\n\n");
            
            String targetDir = renderPath.toOsString(OsType.Unix);
            String srcDir = new Path(renderPath, renderLayer).toOsString(OsType.Unix);
            
            out.write
              ("string $command = \"import os;\\n\" + \n" + 
               "\"import shutil;\\n\" + \n" + 
               "\"\\n\" + \n" + 
               "\"parentDir = '" + targetDir + "'\\n\" + \n" + 
               "\"srcDir = '" + srcDir + "'\\n\" + \n" + 
               "\"files = os.listdir(srcDir)\\n\" + \n" + 
               "\"for file in files:\\n\" + \n" + 
               "\"  shutil.move(srcDir + '/' + file, parentDir)\\n\";\n" + 
               "\n" + 
               "print($command);\n" + 
               "python($command);\n\n");

            out.close();
          }
          catch(IOException ex) {
            throw new PipelineException
              ("Unable to write temporary MEL script file (" + script + ") for Job " +
               "(" + agenda.getJobID() + ")!\n" +
               ex.getMessage());
          }
          args.add("-postFrame");
          if(PackageInfo.sOsType == OsType.Windows && !hasRenderLayers)
            args.add("\"source " + script.getName() + "\"");
          else
            args.add("source " + script.getName());

        }
        else if(postFrameMEL != null) {
          args.add("-postFrame");
          args.add(wrapperMEL(agenda, postFrameMEL, hasRenderLayers));
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
        
        if (!fixPadding && renderer != 4) {
          args.add("-pad");
          args.add(String.valueOf(fpat.getPadding()));
        }
      }

      args.add("-of");
      args.add(fpat.getSuffix());

      if (renderer != 4) {
        args.add("-fnc");
        args.add("3");

        args.add("-rd");
        args.add(renderPath.toOsString());
      }

      {
        Path path = new Path(nodeID.getName());
        if (renderer == 4) { //3delight special naming sauce
          if (fpat.getPadding() != 4)
            throw new PipelineException("The 3Delight render only supports frame padding of 4");
          String outputName = path.getName() + ".#." + fpat.getSuffix();
          Path outputPath = new Path(renderPath, outputName);
          
          args.add("-img");
          args.add(outputPath.toOsString());
        }
        else {
          args.add("-im");
          args.add(path.getName());
        }
      }
      
      {
        String camera = getSingleStringParamValue(aCameraOverride);
        if(camera != null) {
          args.add("-cam");
          args.add(camera);
        }
      }

      {
        Integer procs = (Integer) getSingleParamValue(aProcessors);
        if(procs != null) {
          switch(getSingleEnumParamIndex(aRenderer)) {
          case 1: // Software
            {
              if(procs < 0)
                throw new PipelineException
                  ("The Software renderer requires that the Processors parameter is " +
                   "non-negative.");

              args.add("-n");
              args.add(procs.toString());
            }
            break;

          case 2: // Mental Ray
            {
              if((procs < 1) || (procs > 4))
                throw new PipelineException
                  ("The Mental Ray renderer requires that the Processors parameter is " +
                   "in the range (1-4).");

              args.add("-rt");
              args.add(procs.toString());
            }
            break;
          case 4:
            {
              if(procs < 0)
                throw new PipelineException
                  ("The 3delight renderer requires that the Processors parameter is " +
                   "non-negative.");
              args.add("-cpus");
              args.add(procs.toString());
            }
            break;
          }
        }
      }

      if(renderer == 2) { // Mental Ray
        String verb = getSingleStringParamValue(aVerbosity);
        args.add("-v");
        args.add(verb);
      }
      else if (renderer == 4) { // 3delight
        String verb = getSingleStringParamValue(aVerbosity);
        if (Integer.valueOf(verb) > 3)
          throw new PipelineException("3delight only supports verbosity levels up to 3");
        args.add("-statl");
        args.add(verb);
      }

      args.addAll(getExtraOptionsArgs());

      args.add(sourceScene.toOsString());
    }
    
    String program = "Render";
    if (PackageInfo.sOsType == OsType.Windows) {
      program = "Render.exe";
      if (renderLayer == null || renderer == 4)
        /* create the process to run the action */
        return createSubProcess(agenda, program, args, env,
            agenda.getTargetPath().toFile(), outFile, errFile);
      else {
        File pyScript = createTemp(agenda, "py");
        try {
          FileWriter out = new FileWriter(pyScript);
          out.write("import os\n");
          out.write("import shutil\n");
          out.write(getPythonLaunchHeader());
          out.write(createMayaRenderPythonLauncher(args) + "\n");
          String targetDir = escPath(renderPath.toOsString());
          String srcDir = escPath(new Path(renderPath, renderLayer).toOsString());
          out.write("parentDir = \'" + targetDir + "'\n" + 
                    "srcDir = \'" + srcDir + "'\n" + 
                    "\n" + 
                    "files = os.listdir(srcDir)\n" + 
                    "for file in files:\n" + 
                    "  shutil.move(srcDir + \'/\' + file, parentDir)");
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
    }
    else {
      /* create the process to run the action */
      return createSubProcess(agenda, program, args, env,
          agenda.getTargetPath().toFile(), outFile, errFile);
    }
    /* render program */

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
   Path mel, 
   boolean hasRenderLayers
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

    if((PackageInfo.sOsType == OsType.Windows)  && !hasRenderLayers)
      return ("\"source " + script.getName() + "\"");

    return ("source " + script.getName());
  }
  
  /** 
   * A convienence method for creating the Python code equivalent of 
   * {@link #createMayaSubProcess createMayaSubProcess} suitable for inclusion in an 
   * temporary Python script.<P> 
   * 
   * The returned OS level process will run Maya in batch mode to optionally load a input
   * Maya scene and then perform some operations specified by a dynamically creatd MEL 
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

  private static final long serialVersionUID = 472998966492199733L;

  public static final String aCameraOverride = "CameraOverride";
  public static final String aRenderLayer    = "RenderLayer";
  public static final String aParticleCache  = "ParticleCache";
  public static final String aRenderer       = "Renderer";
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

