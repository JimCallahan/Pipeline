// $Id: MayaMRayRenderAction.java,v 1.7 2007/04/04 07:33:30 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M R A Y   R E N D E R   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Takes a source Maya scene, exports a monolithic MI file for each frame that contains 
 * everything in the scene and then renders it with the Mental Ray standalone engine. <P>
 * 
 * The Maya scene is opened and the export length is set correctly.  The entire scene is then
 * exported out to per-frame MI files.  The Action then invokes the Mental Ray standalone 
 * renderer to generate the target images. <P>  
 *
 * Both before and after the export, optional MEL scripts may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the PreExportMEL and PostExportMEL single valued parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source Maya scene node.
 *   </DIV> <BR>
 *   
 *   Pre Export MEL<BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to be sourced before MI file export. 
 *   </DIV> <BR>
 *   
 *   Pre Export MEL<BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to be sourced after MI file export. 
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
 *   Include Path<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     A semicolon seperated list of directories which overrides the path used to resolve 
 *     "$include" statements in the MI scene file.  Toolset environmental variable 
 *     substitutions are enabled (see {@link ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Library Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of directories that Mental Ray searches for shader 
 *     libraries containing shader code before the default library paths.  Toolset 
 *     environmental variable substitutions are enabled (see {@link ActionAgenda#evaluate 
 *     evaluate}).
 *   </DIV> <BR>
 * 
 *   Texture Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of directories that Mental Ray searches for texture files.  
 *     Toolset environmental variable substitutions are enabled (see {@link 
 *     ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Extra Opts<BR>
 *   <DIV style="margin-left: 40px;">
 *      Additional command-line arguments for the Mental Ray renderer.
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * If the environmental variable MRAY_BINARY is defined, its value will be used as the 
 * name of the renderer executable instead of the "ray" (Unix/MacOS) or "ray345.exe" 
 * (Windows).  On Windows, the renderer name should include the ".exe" extension. <P> 
 * 
 * By default, the "python" program is used by this action when running on Windows to run
 * Maya and Mental Ray.  An alternative program can be specified by setting PYTHON_BINARY 
 * in the Toolset environment to the name of the Python interpertor this Action should use.  
 * When naming an alternative Python interpretor under Windows, make sure to include the 
 * ".exe" extension in the program name.
 */ 
public class MayaMRayRenderAction
  extends MayaActionUtils
{
  /*---------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                               */
  /*---------------------------------------------------------------------------------------*/

  public 
  MayaMRayRenderAction() 
  {
    super("MayaMRayRender", new VersionID("2.2.1"), "Temerity",
          "Takes a source Maya scene, exports a monolithic MI file for each frame that " + 
          "contains everything in the scene and then renders it with the Mental Ray " + 
          "standalone engine.");

    {
      ActionParam param =
	new LinkActionParam
        (aMayaScene, 
         "The source Maya scene node.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aPreExportMEL,
	 "The MEL script to be sourced before MI file export.", 
	 null);
      addSingleParam(param);
    }
  
    {
      ActionParam param = 
        new LinkActionParam
	(aPostExportMEL,
	 "The MEL script to be sourced after MI file export.", 
	 null);
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
	new StringActionParam
	(aIncludePath,
	 "A semicolon seperated list of directories which overrides the path used to " +
	 "resolve $include statements in the MI scene file.  Toolset environmental " + 
         "variable substitutions are enabled.", 
         "${MI_ROOT+}/include"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aLibraryPath,
	 "A semicolon seperated list of directories that Mental Ray searches for shader " +
	 "libraries containing shader code before the default library paths.  Toolset " + 
         "environmental variable substitutions are enabled.", 
         "${MI_ROOT+}/lib"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aTexturePath,
	 "A semicolon seperated list of directories that Mental Ray searches for texture " +
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
        values.put(aIncludePath, "${MI_ROOT+}/include");
        values.put(aLibraryPath, "${MI_ROOT+}/lib"); 
	
        addPresetValues(aBaseLibraries, "Mental Ray", values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aIncludePath, "${MAYA_LOCATION+}/mentalray/include");
        values.put(aLibraryPath, "${MAYA_LOCATION+}/mentalray/lib"); 
	
        addPresetValues(aBaseLibraries, "Maya", values);
      }
    }

    addExtraOptionsParam(); 
  
    {    
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aPreExportMEL);
      layout.addEntry(aPostExportMEL);
      layout.addSeparator();
      layout.addEntry(aKeepTempFiles);
      layout.addEntry(aRenderVerbosity);
      layout.addSeparator();
      layout.addEntry(aBaseLibraries);
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
    /* the target image sequence */ 
    FileSeq target = null;
    {
      target = agenda.getPrimaryTarget();
      String suffix = target.getFilePattern().getSuffix();

      ArrayList<String> exts = new ArrayList<String>();
      exts.add("bmp");
      exts.add("gif");
      exts.add("jpg");
      exts.add("rgb");
      exts.add("rla");
      exts.add("sgi");
      exts.add("tga");
      exts.add("tif");
      exts.add("tiff");
      exts.add("iff");
      exts.add("eps");
      exts.add("exr");
      exts.add("hdr");
      
      if(!exts.contains(suffix))
        throw new PipelineException
          ("The " + getName() + " Action does not support target images with a suffix " + 
           "of (" + suffix + ").");

      if(!target.hasFrameNumbers())
        throw new PipelineException
          ("The " + getName() + " Action requires that the output images have frame " + 
           "numbers.");
    }

    /* the source Maya scene */ 
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);

    /* MEL script paths */ 
    Path preExportMEL  = getMelScriptSourcePath(aPreExportMEL, agenda);
    Path postExportMEL = getMelScriptSourcePath(aPostExportMEL, agenda);
    
    
    /* create a temporary MEL script used to export MI files */ 
    File exportMEL = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(exportMEL);

      if(preExportMEL != null) 
	out.write("// PRE-EXPORT SCRIPT\n" + 
                  "print \"Pre-Export Script: " + preExportMEL + "\\n\";\n" +
                  "source \"" + preExportMEL + "\";\n\n");

      FilePattern fpat = target.getFilePattern();
      FrameRange range = target.getFrameRange();

      out.write
        ("// SET RENDER GLOBALS\n" + 
         "{\n" +
         "  miCreateImageFormats();\n" +
         "  global string $miImgFormat[];\n" +
         "  global int $miImgExtNum[];\n" +
         "  string $temp = \"" + fpat.getSuffix() + "\";\n" +
         "  int $num = 0;\n" +
         "  string $format;\n" +
         "  for($format in $miImgFormat) {\n" +
         "    if($format == $temp)\n" +
         "      break;\n" +
         "    $num++;\n" +      
         "  }\n" +
         "  setAttr defaultRenderGlobals.imageFormat $miImgExtNum[$num];\n" +
         "  setAttr defaultRenderGlobals.imfkey -type \"string\" $miImgFormat[$num];\n" +
         "}\n\n" +
      
         "setAttr defaultRenderGlobals.an true ;\n" +
         "setAttr defaultRenderGlobals.extensionPadding " + fpat.getPadding() + " ;\n" +
         "setAttr defaultRenderGlobals.pff 1 ;\n" +
         "setAttr defaultRenderGlobals.periodInExt 1;\n" +
         "setAttr mentalrayGlobals.passAlphaThrough 1;\n" +
         "setAttr -type \"string\" " + "defaultRenderGlobals.ifp " + 
                  "\"" + fpat.getPrefix() + "\";\n" +

         "setAttr defaultRenderGlobals.startFrame " + range.getStart() + ";\n" +
         "setAttr defaultRenderGlobals.endFrame " + range.getEnd() + ";\n" +
         "setAttr defaultRenderGlobals.byFrameStep " + range.getBy() + ";\n\n");
      
      Path miPath = new Path(getTempPath(agenda), fpat.getPrefix() + ".mi");
      out.write
        ("// EXPORT MI FILES\n" + 
         "print \"Exporting MI Files: " + miPath + "\\n\";\n" + 
         "Mayatomr -mi -perframe 2 -padframe 4 -binary -xp \"1111333333\"\n" + 
         "         -file \"" + miPath + "\";\n\n");
      
      if(postExportMEL != null)
	out.write("// POST-EXPORT SCRIPT\n" + 
                  "print \"Post-Export Script: " + postExportMEL + "\\n\";\n" +
                  "source \"" + postExportMEL + "\";\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary script file (" + exportMEL + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create a temporary MI file which loads the common shaders */ 
    Path miCommon = new Path(createTemp(agenda, "mi3"));
    try {
      FileWriter out = new FileWriter(miCommon.toFile());
      
      out.write
        ("link \"base.so\"\n" +
         "$include \"base.mi\"\n" +
         "link \"physics.so\"\n" +
         "$include \"physics.mi\"\n" +
         "link \"contour.so\"\n" +
         "$include \"contour.mi\"\n" +
         "link \"subsurface.so\"\n" +
         "$include \"subsurface.mi\"\n" +
         "link \"paint.so\"\n" +
         "$include \"paint.mi\"\n");
      
      out.close();
    } 
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write the temporary MI file (" + miCommon + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }

    /* create a temporary Python script file to export MI files from Maya
         followed by per-frame MentalRay renders */
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);
      
      /* import modules */ 
      out.write("import os\n");

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* export the MI files from Maya */ 
      out.write(createMayaPythonLauncher(sourceScene, exportMEL) + "\n");

      /* RIB file paths */ 
      FileSeq miSeq = null;
      {
        FilePattern fpat = target.getFilePattern();
        FilePattern mpat = new FilePattern(fpat.getPrefix(), fpat.getPadding(), "mi");
        FrameRange range = target.getFrameRange();

        miSeq = new FileSeq(getTempPath(agenda).toString(), new FileSeq(mpat, range));
      }
      
      /* verify that the MI files where actually generated */ 
      out.write(getPythonFileVerify(miSeq, "MI"));

      /* construct to common MentalRay command-line arguments */  
      String common = null;
      {
        StringBuilder buf = new StringBuilder();

        String ray = MRayActionUtils.getMRayProgram(agenda);

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
        for(Path path : miSeq.getPaths()) 
          out.write(common + ", '" + miCommon + "', '" + path + "'])\n");
        out.write("\n");

        if(!getSingleBooleanParamValue(aKeepTempFiles)) {
          for(Path path : miSeq.getPaths()) 
            out.write("os.remove('" + path + "')\n");
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7375186586564455944L;

  public static final String aMayaScene       = "MayaScene";
  public static final String aPreExportMEL    = "PreExportMEL";
  public static final String aPostExportMEL   = "PostExportMEL";
  public static final String aKeepTempFiles   = "KeepTempFiles";
  public static final String aRenderVerbosity = "RenderVerbosity";
  public static final String aBaseLibraries   = "BaseLibraries"; 
  public static final String aLibraryPath     = "LibraryPath";
  public static final String aIncludePath     = "IncludePath";
  public static final String aTexturePath     = "TexturePath";

}
