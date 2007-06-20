// $Id: MRayRenderAction.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.MRayRenderAction.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.*;
import java.util.regex.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a collection of MI files using Mental Ray Standalone.<P> 
 * 
 * All of the MI file dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed. The frame range rendered will be limited by frame 
 * numbers of the target images. The Action invokes a new instance of the Mental Ray renderer
 * for each MI file. This is due to how Mental Ray keeps library information loaded when 
 * rendering multiple MIs. Since each MI file may potentially have a different set of library 
 * files and includes, a separate invocation of 'ray' is necessary to ensure that each one 
 * runs in the correct environment.<P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Camera Name <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Name of the "camera" object in the source MI files used to render the target 
 *     images.
 *   </DIV> <BR>
 * 
 *   Framebuffer Type<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The data format of the target rendered images.
 *   </DIV> <BR>
 * 
 *   Keep Temp Files<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should the Action preserve the temporary MI files it generates for rendering. 
 *   </DIV> <BR>
 *   
 *   Render Verbosity<BR>
 *   <DIV style="margin-left: 40px;">
 *     The verbosity of render progress, warning and error messages.
 *   </DIV> <BR>
 *   
 *   Maya Shaders <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to generate "link" and "$include" statements for Mental Ray shaders 
 *     provided as part of the Maya application.
 *   </DIV> <BR>
 * 
 *   Include Path<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     A semicolon seperated list of directories which overrides the path used to resolve 
 *     "$include" statements in the MI scene file.  Toolset environmental variable 
 *     substitutions are enabled (see {@link ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Library Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of directories that mental ray searches for shader 
 *     libraries containing shader code before the default library paths.  Toolset 
 *     environmental variable substitutions are enabled (see {@link ActionAgenda#evaluate 
 *     evaluate}).
 *   </DIV> <BR>
 * 
 *   Texture Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of directories that mental ray searches for texture files.  
 *     Toolset environmental variable substitutions are enabled (see {@link 
 *     ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Extra Opts<BR>
 *   <DIV style="margin-left: 40px;">
 *      Additional command-line arguments.
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Contains <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source sequence which sets this parameter should contain input MI files to render.
 *     This parameter specifies the type of MI data the Action should expect the files to 
 *     contain and controls how the Action processes these files.  The allowable types are:
 *     <DIV style="margin-left: 40px;">
 *       ShaderIncs - Contains "link" and "$include" statements for custom shaders. Typically
 *       generated by the MRayShaderInc Action.<P> 
 * 
 *       Lights - Contains light shaders, objects and instances used to illuminate the 
 *       geometry being rendered and possibly required by shaders.<P> 
 * 
 *       Shaders - Contains "material" and "shader" statements required by the geometry
 *       to be rendered. <P> 
 * 
 *       Geometry - Contains "instance" statements for the geometry to be rendered.<P> 
 * 
 *       InstGroups - Contains "instgroup" statements for instances found in pre-processed 
 *       geometry MI files and "$include" statements for those files.  Typically generated 
 *       by the MRayInstGroup Action. <P> 
 * 
 *       Cameras - Contains the camera object and instance used to render the images.  May
 *       contain additional cameras as well, but they will be ignored.<P>
 * 
 *       CamOverride - Contains MI statements which should be used to override settings 
 *       for the specified render camera as an increment.  Typically generated by the 
 *       MRayCamOverride Action.<P> 
 * 
 *       Options - Contains an "option" statement specifying render globals.
 *     </DIV>
 *   </DIV>
 * </DIV> <P>
 * 
 * If the environmental variable MRAY_BINARY is defined, its value will be used as the 
 * name of the renderer executable instead of the "ray" (Unix/MacOS) or "ray345.exe" 
 * (Windows).  On Windows, the renderer name should include the ".exe" extension. <P> 
 * 
 * By default, the "python" program is used by this action when running on Windows to 
 * process the MI files.  An alternative program can be specified by setting PYTHON_BINARY 
 * in the Toolset environment to the name of the Python interpertor this Action should use.  
 * When naming an alternative Python interpretor under Windows, make sure to include the
 * ".exe" extension in the program name.
 */ 
public class MRayRenderAction
  extends MRayActionUtils
{
  /*---------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                               */
  /*---------------------------------------------------------------------------------------*/

  public 
  MRayRenderAction() 
  {
    super("MRayRender", new VersionID("2.2.1"), "Temerity",
          "Renders a collection of MI files using Mental Ray Standalone.");

    { 
      ActionParam param = 
	new StringActionParam
	(aCameraName,
         "Name of the \"camera\" object in the source MI files used to render the " + 
         "target images.",
         null); 
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("+rgba");
      choices.add("+rgba_16");
      choices.add("+rgba_fp");
      choices.add("+rgb");
      choices.add("+rgb_16");
      choices.add("+rgb_fp");
      choices.add("+rgbe");
      choices.add("+a");
      choices.add("+a_16");
      choices.add("+a_fp");
      choices.add("+s");
      choices.add("+s_16");
      choices.add("+s_fp");
      choices.add("+z");
      choices.add("+n");
      choices.add("+m");
      choices.add("+tag");
      choices.add("+vta");
      choices.add("+vts");
      choices.add("+bit");
      choices.add("+coverage");
      choices.add("-rgba");
      choices.add("-rgba_16");
      choices.add("-rgba_fp");
      choices.add("-rgb");
      choices.add("-rgb_16");
      choices.add("-rgb_fp");
      choices.add("-rgbe");
      choices.add("-a");
      choices.add("-a_16");
      choices.add("-a_fp");
      choices.add("-s");
      choices.add("-s_16");
      choices.add("-s_fp");
      choices.add("-z");
      choices.add("-n");
      choices.add("-m");
      choices.add("-tag");
      choices.add("-vta");
      choices.add("-vts");
      choices.add("-bit");
      choices.add("-coverage");

      ActionParam param = 
	new EnumActionParam
	(aFramebufferType, 
	 "The data format of the target rendered images.", 
	 "+rgba", choices);
      addSingleParam(param);      
    }

    {
      ActionParam param =
	new BooleanActionParam
	(aKeepTempFiles, 
	 "Should the Action preserve the temporary MI files it generates for rendering.", 
	 false);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("No Messages");
      choices.add("Fatal Messages Only");
      choices.add("Error Messages");
      choices.add("Warning Messages");
      choices.add("Info Messages");
      choices.add("Progress Messages");
      choices.add("Details Messages");

      ActionParam param = 
	new EnumActionParam
	(aRenderVerbosity,
	 "The verbosity of render progress, warning and error messages.",
	 "Warning Messages", choices);
      addSingleParam(param);
    }

    { 
      ActionParam param = 
	new BooleanActionParam
	(aMayaShaders,
	 "Whether to generate \"link\" and \"$include\" statements for Mental Ray shaders " + 
         "provided as part of the Maya application.", 
         false); 
      addSingleParam(param);
    }

    { 
      ActionParam param = 
	new StringActionParam
	(aIncludePath,
	 "A semicolon seperated list of directories which overrides the path used to " +
	 "resolve \"$include\" statements in the MI scene file.  Toolset environmental " + 
         "variable substitutions are enabled.", 
         "${MI_ROOT+}/include"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aLibraryPath,
	 "A semicolon seperated list of directories that mental ray searches for shader " +
	 "libraries containing shader code before the default library paths.  Toolset " + 
         "environmental variable substitutions are enabled.", 
         "${MI_ROOT+}/lib"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aTexturePath,
	 "A semicolon seperated list of directories that mental ray searches for texture " +
	 "files.  Toolset environmental variable substitutions are enabled.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Mental Ray");
      choices.add("Maya");
      
      addPreset(aBaseLibraries, choices);

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMayaShaders, false); 
        values.put(aIncludePath, "${MI_ROOT+}/include");
        values.put(aLibraryPath, "${MI_ROOT+}/lib"); 
	
        addPresetValues(aBaseLibraries, "Mental Ray", values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMayaShaders, true); 
        values.put(aIncludePath, "${MAYA_LOCATION+}/mentalray/include");
        values.put(aLibraryPath, "${MAYA_LOCATION+}/mentalray/lib"); 
	
        addPresetValues(aBaseLibraries, "Maya", values);
      }
    }

    addExtraOptionsParam(); 
  
    {    
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aCameraName); 
      layout.addEntry(aFramebufferType); 
      layout.addSeparator();
      layout.addEntry(aKeepTempFiles);
      layout.addEntry(aRenderVerbosity);
      layout.addSeparator();
      layout.addEntry(aBaseLibraries);
      layout.addEntry(aMayaShaders);
      layout.addEntry(aIncludePath);
      layout.addEntry(aLibraryPath);
      layout.addEntry(aTexturePath);
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does this action support per-source parameters?
   */
  public boolean 
  supportsSourceParams()
  {
    return true;
  }

  /**
   * Get an initial set of action parameters associated with an upstream node.
   */
  public TreeMap<String, ActionParam> 
  getInitialSourceParams()
  {
    TreeMap<String, ActionParam> params = new TreeMap<String, ActionParam>();

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aShaderIncs);
      choices.add(aLights);
      choices.add(aShaders);
      choices.add(aGeometry);
      choices.add(aInstGroups);
      choices.add(aCameras);
      choices.add(aCamOverride);
      choices.add(aOptions);   

      ActionParam param = 
	new EnumActionParam
	(aContains,
         "Each source sequence which sets this parameter should contain input MI files to " +
         "render. This parameter specifies the type of MI data the Action should expect " + 
         "the files to contain and controls how the Action processes these files.", 
         "InstGroups", choices);
      params.put(param.getName(), param);
    }

    return params;
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
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy 
  prep
  (
   ActionAgenda agenda, 
   File outFile, 
   File errFile
  )
    throws PipelineException
  {
    /* name of the render camera object */ 
    String cameraName = getSingleStringParamValue(aCameraName);
    if(cameraName == null) 
      throw new PipelineException
        ("The required CameraName parameter was not specified!"); 
    
    /* the paths to the target images and temporary MI files */ 
    String suffix = null; 
    ArrayList<Path> targetPaths = new ArrayList<Path>();
    ArrayList<Path> tempMiPaths = new ArrayList<Path>();
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      suffix = fseq.getFilePattern().getSuffix();

      ArrayList<String> exts = new ArrayList<String>();
      exts.add("bmp");
      exts.add("gif");
      exts.add("jpg");
      exts.add("rgb");
      exts.add("rla");
      exts.add("sgi");
      exts.add("tga");
      exts.add("tif");
      exts.add("iff");
      exts.add("eps");
      exts.add("exr");
      exts.add("hdr");
      
      if(!exts.contains(suffix))
        throw new PipelineException
          ("The " + getName() + " Action does not support target images with a suffix " + 
           "of (" + suffix + ").");

      if(!fseq.hasFrameNumbers())
        throw new PipelineException
          ("The " + getName() + " Action requires that the output images have frame " + 
           "numbers.");

      targetPaths.addAll(fseq.getPaths());

      {
        FilePattern fpat = fseq.getFilePattern();
        FilePattern mpat = new FilePattern(fpat.getPrefix(), fpat.getPadding(), "mi");
        FrameRange range = fseq.getFrameRange();
        
        FileSeq miSeq = new FileSeq(getTempPath(agenda).toString(), new FileSeq(mpat, range));
        tempMiPaths.addAll(miSeq.getPaths());
      }
    }

    /* get the abstract filesytem paths of all source MI files to process 
         indexed by the value of the "Contains" parameter */ 
    MappedLinkedList<String,ArrayList<Path>> miPaths = 
      new MappedLinkedList<String,ArrayList<Path>>(); 
    {
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
          if(!fseq.isSingle() && (fseq.numFrames() != targetPaths.size())) 
            throw new PipelineException
              ("The selected MI file sequence (" + fseq + ") of source node " + 
               "(" + sname + ") must have either the same number of frames as the " + 
               "target rendered images or be a single file sequence!"); 

          String contains = (String) getSourceParamValue(sname, aContains); 
          if(contains == null) 
            throw new PipelineException
              ("Somehow the Contains per-source parameter for file sequence " + 
               "(" + fseq + ") of source node (" + sname + ") was unset!");

          miPaths.put(contains, getWorkingNodeFilePaths(agenda, sname, fseq));
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
          if(!fseq.isSingle() && (fseq.numFrames() != targetPaths.size())) 
            throw new PipelineException
              ("The selected MI file sequence (" + fseq + ") of source node " + 
               "(" + sname + ") must have either the same number of frames as the " + 
               "target rendered images or be a single file sequence!"); 

	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
            String contains = (String) getSecondarySourceParamValue(sname, fpat, aContains);
            if(contains == null) 
              throw new PipelineException
                ("Somehow the Contains per-source parameter for file sequence " + 
                 "(" + fseq + ") of source node (" + sname + ") was unset!");
            
            miPaths.put(contains, getWorkingNodeFilePaths(agenda, sname, fseq));
	  }
	}
      }
    }

    /* create temporary MI files ready to render */  
    {
      int idx = 0;
      for(Path targetPath : targetPaths) {
        Path tempMiPath = tempMiPaths.get(idx);

        try {
          FileWriter out = new FileWriter(tempMiPath.toFile());

          /* load the common shaders */ 
          out.write
            ("# SHADER LIBS\n" + 
             "link \"base.so\"\n" +
             "$include \"base.mi\"\n" +
             "link \"physics.so\"\n" +
             "$include \"physics.mi\"\n" +
             "link \"contour.so\"\n" +
             "$include \"contour.mi\"\n" +
             "link \"subsurface.so\"\n" +
             "$include \"subsurface.mi\"\n" +
             "link \"paint.so\"\n" +
             "$include \"paint.mi\"\n\n");
          
          /* if using Maya shaders, load them too */ 
          if(getSingleBooleanParamValue(aMayaShaders)) 
            out.write
              ("# MAYA SHADER LIBS\n" + 
               "link \"mayabase.so\"\n" +
               "$include \"mayabase.mi\"\n" +
               "link \"mayahair.so\"\n" +
               "$include \"mayahair.mi\"\n\n");
          
          /* load custom shaders */ 
          {
            LinkedList<ArrayList<Path>> shaderIncs = miPaths.get(aShaderIncs);
            if(shaderIncs != null) {
              out.write("# CUSTOM SHADERS\n"); 
              for(ArrayList<Path> spaths : shaderIncs) 
                writeInclude(out, spaths, idx); 
              out.write("\n");
            }
          }

          /* global render options */ 
          String optionsName = null;
          {
            out.write("# OPTIONS\n"); 
            ArrayList<Path> spaths = getUniqueSourceMiPaths(aOptions, miPaths, false);
            Path path = writeInclude(out, spaths, idx); 
            optionsName = findOptionsName(path);
            out.write("\n");
          }

          /* names of all instances to render: lights and geometry */ 
          TreeSet<String> instanceNames = new TreeSet<String>();

          /* light shaders, objects and instances */ 
          {
            LinkedList<ArrayList<Path>> lights = miPaths.get(aLights);
            if(lights != null) {
              out.write("# LIGHTS\n"); 
              for(ArrayList<Path> spaths : lights) {
                Path path = writeInclude(out, spaths, idx); 
                findInstanceNames(path, instanceNames); 
              }
              out.write("\n");
            }
          }

          /* shader and material statements */  
          {
            LinkedList<ArrayList<Path>> shaders = miPaths.get(aShaders);
            if(shaders != null) {
              out.write("# SHADERS & MATERIALS\n"); 
              for(ArrayList<Path> spaths : shaders) 
                writeInclude(out, spaths, idx); 
              out.write("\n");
            }
          }
          
          /* geometry instances */  
          {
            LinkedList<ArrayList<Path>> geometry = miPaths.get(aGeometry);
            if(geometry != null) {
              out.write("# GEOMETRY\n"); 
              for(ArrayList<Path> spaths : geometry) {
                Path path = writeInclude(out, spaths, idx); 
                findInstanceNames(path, instanceNames); 
              }
              out.write("\n");
            }
          }

          /* instgroups */  
          TreeSet<String> instGroupNames = new TreeSet<String>();
          {
            LinkedList<ArrayList<Path>> instgroups = miPaths.get(aInstGroups);
            if(instgroups != null) {
              out.write("# INSTGROUPS\n"); 
              for(ArrayList<Path> spaths : instgroups) {
                Path path = writeInclude(out, spaths, idx); 
                findInstGroupNames(path, instGroupNames); 
              }
              out.write("\n");
            }
          }

          /* camera */ 
          String cameraInstName = null;
          {
            out.write("# CAMERA\n"); 
            ArrayList<Path> spaths = getUniqueSourceMiPaths(aCameras, miPaths, false);
            Path path = writeInclude(out, spaths, idx); 
            cameraInstName = findCameraInstance(cameraName, path); 
            out.write("\n");
          }

          /* instantiate instgroups */ 
          if(!instGroupNames.isEmpty()) {
            out.write("# INSTANCE INSTGROUPS\n"); 
            for(String gname : instGroupNames) 
              out.write("instance \"" + gname + "_instance\" \"" + gname + "\"\n" + 
                        "end instance\n");
            out.write("\n");
          }

          /* camera override */ 
          {
            out.write("# CAMERA OVERRIDE\n" + 
                      "incremental camera \"" + cameraName + "\"\n"); 

            String format = getSingleStringParamValue(aFramebufferType);
            out.write("  output \"" + format + "\" \"" + suffix + "\" " + 
                               "\"" + targetPath + "\"\n");

            ArrayList<Path> spaths = getUniqueSourceMiPaths(aCamOverride, miPaths, true);
            if(spaths != null) {
              int i = (spaths.size() == 1) ? 0 : idx;
              writeCameraIncremental(out, cameraName, spaths.get(i)); 
            }

            out.write("end camera\n\n"); 
          }

          /* render everything... */ 
          {
            out.write("# RENDER\n" +
                      "instgroup \"TheFinalRenderGroup\"\n");

            for(String gname : instGroupNames) 
              out.write("  \"" + gname + "_instance\"\n");

            for(String gname : instanceNames) 
              out.write("  \"" + gname + "\"\n");

            out.write("  \"" + cameraInstName + "\"\n" + 
                      "end instgroup\n\n" + 
                      "render \"TheFinalRenderGroup\" \"" + cameraInstName + "\" " + 
                                                     "\"" + optionsName + "\"\n");
          }

          out.close();
        } 
        catch(IOException ex) {
          throw new PipelineException
            ("Unable to write the temporary MI file (" + tempMiPath + ") for Job " + 
             "(" + agenda.getJobID() + ")!\n" +
             ex.getMessage());
        }

        idx++;
      }
    }

    /* create a temporary Python script file to run the per-frame MentalRay renders */
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);
      
      /* import modules */ 
      out.write("import os\n");

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* construct to common MentalRay command-line arguments */  
      String common = null;
      {
        StringBuilder buf = new StringBuilder();

        String ray = getMRayProgram(agenda);

        buf.append
          ("launch('" + ray + "', " + 
           "['-verbose', '" + getSingleEnumParamIndex(aRenderVerbosity) + "'");
        
        String inc = getSingleStringParamValue(aIncludePath);
        if(inc != null)
          buf.append(", '-I', '" + agenda.evaluate(inc) + "'");

        String lib = getSingleStringParamValue(aLibraryPath);
        if(lib != null)
          buf.append(", '-L', '" + agenda.evaluate(lib) + "'");
        
        String tex = getSingleStringParamValue(aTexturePath);
        if(tex != null)
          buf.append(", '-T', '" + agenda.evaluate(tex) + "'");

        for(String extra : getExtraOptionsArgs()) 
          buf.append(", '" + extra + "'");

        common = buf.toString();
      }

      /* render the MI files and cleanup */ 
      {
        for(Path miPath : tempMiPaths) 
          out.write(common + ", '" + miPath + "'])\n");
        out.write("\n");

        if(!getSingleBooleanParamValue(aKeepTempFiles)) {
          for(Path miPath : tempMiPaths) 
            out.write("os.remove('" + miPath + "')\n");
        }
      }

      out.write("\n" + 
                "print 'ALL DONE.'\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary Python script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }   



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the "$include" line for the correct source MI file.
   * 
   * @return 
   *   The selected path.
   */ 
  private Path 
  writeInclude
  (
   FileWriter out,
   ArrayList<Path> spaths, 
   int idx
  ) 
    throws IOException
  {
    int i = (spaths.size() == 1) ? 0 : idx;
    Path path = spaths.get(i);
    out.write("$include \"" + path + "\"\n");
    return path;
  }

  /**
   * Write the "incremental" camera statement. 
   */ 
  private void
  writeCameraIncremental
  (
   FileWriter out, 
   String cameraName, 
   Path path
  )
    throws PipelineException
  { 
    BufferedReader in = makeBufferedReader(path); 
    try { 
      while(true) {
        String line = in.readLine();
        if(line == null) 
          break;
        
        out.write("  " + line + "\n");
      }

      in.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Problems reading source MI file (" + path + "):\n  " + 
         ex.getMessage());         
    }
  }

  /**
   * Create a new buffered file reader for the given file.
   */ 
  private BufferedReader 
  makeBufferedReader
  (
   Path path
  )
    throws PipelineException
  {
    File file = path.toFile();
    if(!file.exists())
      throw new PipelineException
        ("The source MI file (" + path + ") does not exist!");

    try {
      return new BufferedReader(new FileReader(file));
    }
    catch(IOException ex) {
      throw new PipelineException
        ("The source MI file (" + path + ") could not be opened for reading:\n  " + 
         ex.getMessage());
    }
  }

  /**
   * Strip one leading and trailing character from the given string.
   */ 
  private String
  strip
  (
   String str
  ) 
    throws PipelineException
  {
    if(str == null) 
      throw new PipelineException("Missing string value!");

    if(str.length() < 3)
      throw new PipelineException("Empty or malformed string value!"); 

    return str.substring(1, str.length()-1);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Lookup the MI file paths for the given Contains parameter value which only one file 
   * sequence should have set. 
   */ 
  private ArrayList<Path>
  getUniqueSourceMiPaths
  (
   String contains, 
   MappedLinkedList<String,ArrayList<Path>> miPaths, 
   boolean optional
  )
    throws PipelineException
  {
    LinkedList<ArrayList<Path>> options = miPaths.get(contains);
    if((options == null) && optional) 
      return null;

    if((options == null) || (options.size() != 1)) 
      throw new PipelineException
        ("One and only one \"" + contains + "\" source MI file sequence must be specified!");

    return options.get(0);
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Scan the given MI file for an "options" statement and return its name.
   */ 
  private String 
  findOptionsName
  (
   Path path
  ) 
    throws PipelineException
  {
    String optionsName = null;

    BufferedReader in = makeBufferedReader(path); 
    int lnum = 1;
    try { 
      while(true) {
        String line = in.readLine();
        if(line == null) 
          break;
        
        if(sOptionsPattern.matcher(line).matches()) {
          String parts[] = line.split(" ");
          if(parts.length != 2) 
            throw new PipelineException
              ("Bad \"options\" statement!\n");
            
          optionsName = strip(parts[1]); 
          break;
        }
        
        lnum++;
      }

      in.close();
    }
    catch(PipelineException ex) {
      throw new PipelineException
        ("Syntax Error on line (" + lnum + ") of source MI file (" + path + "):\n  " + 
         ex.getMessage());
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Problems reading source MI file (" + path + "):\n  " + 
         ex.getMessage());         
    }

    if(optionsName == null) 
      throw new PipelineException
        ("Unable to find any \"options\" statements in source MI file (" + path + ")!");

    return optionsName;
  }

  /**
   * Scan the given MI file for an "options" statement and return its name.
   */ 
  private String
  findCameraInstance
  (
   String cameraName, 
   Path path
  ) 
    throws PipelineException
  {
    boolean cameraFound = false;
    String cameraInstName = null;

    BufferedReader in = makeBufferedReader(path); 
    int lnum = 1;
    try { 
      while(true) {
        String line = in.readLine();
        if(line == null) 
          break;
        
        if(sCameraPattern.matcher(line).matches()) {
          String parts[] = line.split(" ");
          if(parts.length != 2) 
            throw new PipelineException
              ("Bad \"camera\" statement!\n");

          if(cameraName.equals(strip(parts[1])))
            cameraFound = true;
        }
        else if(sInstancePattern.matcher(line).matches()) {
          String parts[] = line.split(" ");
          if(parts.length != 3) 
            throw new PipelineException
              ("Bad \"instance\" statement!\n");
            
          if(cameraName.equals(strip(parts[2]))) {
            if(!cameraFound) 
              throw new PipelineException
                ("Found the \"instance\" statement for the render camera " + 
                 "(" + cameraName + ") but the camera object instanced had not been  " + 
                 "defined yet in the MI file!"); 

            cameraInstName = strip(parts[1]);
            break;
          }
        }
        
        lnum++;
      }

      in.close();
    }
    catch(PipelineException ex) {
      throw new PipelineException
        ("Syntax Error on line (" + lnum + ") of source MI file (" + path + "):\n  " + 
         ex.getMessage());
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Problems reading source MI file (" + path + "):\n  " + 
         ex.getMessage());         
    }

    if(!cameraFound) 
      throw new PipelineException
        ("Unable to find the render camera (" + cameraName + ") object definition in " + 
         "source MI file (" + path + ")!");

    if(cameraInstName == null) 
      throw new PipelineException
        ("Unable to find any \"instance\" statement for the render camera " + 
         "(" + cameraName + ") in source MI file (" + path + ")!");

    return cameraInstName;
  }

  /**
   * Scan the given MI file for "instgroup" statements. 
   * 
   * @param path
   *   The abstract path to the source MI file.
   * 
   * @param names
   *   The found instgroup names.
   */ 
  private void 
  findInstGroupNames
  (
   Path path, 
   TreeSet<String> names
  ) 
    throws PipelineException
  {
    findStatementNames("instgroup", sInstGroupPattern, 2, 1, path, names);    
  }

  /**
   * Scan the given MI file for "instance" statements. 
   * 
   * @param path
   *   The abstract path to the source MI file.
   * 
   * @param names
   *   The found instance names.
   */ 
  private void 
  findInstanceNames
  (
   Path path, 
   TreeSet<String> names
  ) 
    throws PipelineException
  {
    findStatementNames("instance", sInstancePattern, 3, 1, path, names);    
  }

  /**
   * Scan the given MI file for statements. 
   * 
   * @param statement
   *   The MI statement name. 
   * 
   * @param pattern
   *   The line matching regular expression pattern.
   * 
   * @param comps
   *   The expected number of components on the matching line.
   * 
   * @param idx
   *   The index of the component containing the name to find.
   * 
   * @param path
   *   The abstract path to the source MI file.
   * 
   * @param names
   *   The found name components of the matching statements.
   */ 
  private void 
  findStatementNames
  (
   String statement, 
   Pattern pattern, 
   int comps, 
   int idx, 
   Path path, 
   TreeSet<String> names
  ) 
    throws PipelineException
  {
    BufferedReader in = makeBufferedReader(path); 
    int lnum = 1;
    try { 
      while(true) {
        String line = in.readLine();
        if(line == null) 
          break;
        
        if(pattern.matcher(line).matches()) {
          String parts[] = line.split(" ");
          if(parts.length != comps) 
            throw new PipelineException
              ("Bad \"" + statement + "\" statement!\n");
            
          names.add(strip(parts[idx])); 
        }
        
        lnum++;
      }

      in.close();
    }
    catch(PipelineException ex) {
      throw new PipelineException
        ("Syntax Error on line (" + lnum + ") of source MI file (" + path + "):\n  " + 
         ex.getMessage());
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Problems reading source MI file (" + path + "):\n  " + 
         ex.getMessage());         
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7437865149118913353L;

  private final Pattern sOptionsPattern   = Pattern.compile("^options.*"); 
  private final Pattern sInstGroupPattern = Pattern.compile("^instgroup.*"); 
  private final Pattern sInstancePattern  = Pattern.compile("^instance.*"); 
  private final Pattern sCameraPattern    = Pattern.compile("^camera.*"); 
  
  public static final String aCameraName      = "CameraName"; 
  public static final String aFramebufferType = "FramebufferType";
  public static final String aKeepTempFiles   = "KeepTempFiles";
  public static final String aRenderVerbosity = "RenderVerbosity";
  public static final String aBaseLibraries   = "BaseLibraries";
  public static final String aMayaShaders     = "MayaShaders"; 
  public static final String aIncludePath     = "IncludePath"; 
  public static final String aLibraryPath     = "LibraryPath";
  public static final String aTexturePath     = "TexturePath";
  public static final String aContains        = "Contains";
  public static final String aShaderIncs      = "ShaderIncs";
  public static final String aLights          = "Lights";
  public static final String aShaders         = "Shaders";
  public static final String aGeometry        = "Geometry";
  public static final String aInstGroups      = "InstGroups";
  public static final String aCameras         = "Cameras";
  public static final String aCamOverride     = "CamOverride";
  public static final String aOptions         = "Options";

}








