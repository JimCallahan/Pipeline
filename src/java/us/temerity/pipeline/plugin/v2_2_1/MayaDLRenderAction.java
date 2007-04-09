// $Id: MayaDLRenderAction.java,v 1.1 2007/04/09 17:55:47 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   D L   R E N D E R   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Takes a source Maya scene, exports a monolithic RIB file for each frame that contains 
 * everything in the scene and then renders it with the 3Delight renderer. <P>
 * 
 * The Maya scene is opened and the export length is set correctly.  The entire scene is then
 * exported out to per-frame RIB files.  The Action then invokes the 3Delight renderer to 
 * generate the target images. <P>  
 *
 * Both before and after the export, optional MEL scripts may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the PreExportMEL and PostExportMEL single valued parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Render Pass<BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the 3Delight for Maya render pass to render.
 *   </DIV> <BR>
 * 
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source Maya scene node.
 *   </DIV> <BR>
 * 
 *   Pre Export MEL<BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to be sourced before RIB file export. 
 *   </DIV> <BR>
 *   
 *   Pre Export MEL<BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to be sourced after RIB file export. 
 *   </DIV> <BR>
 * 
 *   Keep Temp Files<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should the Action preserve the temporary RIB files it generates for rendering. 
 *   </DIV> <BR>
 *   
 *   Render Verbosity<BR>
 *   <DIV style="margin-left: 40px;">
 *     The verbosity of render progress, warning and error messages.
 *   </DIV> <BR>
 *   
 *   Shader Path<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     A colon seperated list of directories which overrides the path used by 3Delight to 
 *     find shaders.  Toolset environmental variable substitutions are enabled (see {@link 
 *     ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Procedural Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of directories that 3Delight searches for plugins.  Toolset 
 *     environmental variable substitutions are enabled (see {@link ActionAgenda#evaluate 
 *     evaluate}).
 *   </DIV> <BR>
 * 
 *   Texture Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of directories that 3Delight searches for texture files.  
 *     Toolset environmental variable substitutions are enabled (see {@link 
 *     ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Extra Opts<BR>
 *   <DIV style="margin-left: 40px;">
 *      Additional command-line arguments for the 3Delight renderer.
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * By default, the "python" program is used by this action when running on Windows to run
 * Maya and Mental Ray.  An alternative program can be specified by setting PYTHON_BINARY 
 * in the Toolset environment to the name of the Python interpertor this Action should use.  
 * When naming an alternative Python interpretor under Windows, make sure to include the 
 * ".exe" extension in the program name.
 */ 
public class MayaDLRenderAction
  extends MayaActionUtils
{
  /*---------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                               */
  /*---------------------------------------------------------------------------------------*/

  public 
  MayaDLRenderAction() 
  {
    super("MayaDLRender", new VersionID("2.2.1"), "Temerity",
          "Takes a source Maya scene, exports a monolithic RIB file for each frame that " + 
          "contains everything in the scene and then renders it with the 3Delight renderer.");
    
    {
      ActionParam param =
	new StringActionParam
        (aRenderPass, 
         "The name of the 3Delight for Maya render pass being rendered.", 
         null);
      addSingleParam(param);
    }
    
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
	 "The MEL script to be sourced before RIB file export.", 
	 null);
      addSingleParam(param);
    }
  
    {
      ActionParam param = 
        new LinkActionParam
	(aPostExportMEL,
	 "The MEL script to be sourced after RIB file export.", 
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
	new BooleanActionParam
	(aKeepTempFiles, 
	 "Should the Action preserve the temporary RIB files it generates for rendering.", 
	 false);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Low");
      choices.add("Medium");
      choices.add("High");

      ActionParam param = 
	new EnumActionParam
	(aRenderVerbosity,
	 "The verbosity of rendering statistics.",
	 "High", choices);
      addSingleParam(param);
    }

    { 
      ActionParam param = 
	new StringActionParam
	(aShaderPath,
         "A colon seperated list of directories which overrides the path used by 3Delight " + 
         "to find shaders.  Toolset environmental variable substitutions are enabled.", 
         "@:$WORKING"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aProceduralPath,
         "A colon seperated list of directories that 3Delight searches for plugins.  " + 
         "Toolset environmental variable substitutions are enabled.", 
         "@:$WORKING"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aTexturePath,
         "A colon seperated list of directories that 3Delight searches for texture files.  " +
         "Toolset environmental variable substitutions are enabled.", 
	 "@:$WORKING"); 
      addSingleParam(param);
    }

    addExtraOptionsParam(); 
  
    {    
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aRenderPass);
      layout.addSeparator();
      layout.addEntry(aMayaScene);
      layout.addEntry(aPreExportMEL);
      layout.addEntry(aPostExportMEL);
      layout.addSeparator();
      layout.addEntry(aKeepTempFiles);
      layout.addEntry(aRenderVerbosity);
      layout.addSeparator();
      layout.addEntry(aShaderPath);
      layout.addEntry(aProceduralPath);
      layout.addEntry(aTexturePath);
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment(); 
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
    /* the name of the render pass */ 
    String pass = getSingleStringParamValue(aRenderPass);
    if(pass == null) 
      throw new PipelineException("The RenderPass was not specified!");

    /* the target image sequence */ 
    FileSeq target = null;
    FileSeq fullTarget = null; 
    {
      target = agenda.getPrimaryTarget();
      if(!target.hasFrameNumbers())
        throw new PipelineException
          ("The " + getName() + " Action requires that the primary target file sequence " +
           "(" + target +") has frame numbers.");

      FilePattern fpat = target.getFilePattern(); 
      String suffix = fpat.getSuffix();
      if(suffix == null) 
        throw new PipelineException
          ("The " + getName() + " Action requires the primary target file sequence " + 
           "(" + target +") to have a filename suffix!"); 

      FrameRange range = target.getFrameRange();
      if(range.getBy() != 1) 
        throw new PipelineException
          ("The " + getName() + " Action requires a frame increment step of (1)!");       

      switch(fpat.getPadding()) {
      case 0:
      case 1:
      case 4:
        break;
        
      default:
        throw new PipelineException
          ("The " + getName() + " Action requires the primary target file sequence " + 
           "(" + target +") has frame number padding of either 1 or 4 digits!"); 
      }

      fullTarget = new FileSeq(agenda.getTargetPath().toString(), target); 
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

      out.write
        ("// 3DELIGHT INIT\n" + 
         "source \"DL_startup\";\n\n");

      if(preExportMEL != null) 
	out.write("// PRE-EXPORT SCRIPT\n" + 
                  "print \"Pre-Export Script: " + preExportMEL + "\\n\";\n" +
                  "source \"" + preExportMEL + "\";\n\n");

      out.write
        ("// VALIDATE RENDER PASS\n" + 
         "{\n" + 
         "  string $matches[] = `ls -type \"delightRenderPass\" \"" + pass + "\"`;\n" + 
         "  if(size($matches) != 1) {\n" +  
         "    print \"Unable to find 3Delight RenderPass (" + pass + ")!\";\n" + 
         "    quite -exitCode 1;\n" +
         "  }\n" + 
         "}\n\n");

      FrameRange range = target.getFrameRange();

      out.write 
        ("// SET RENDER PASS ATTRIBUTES\n" + 
         "{\n" +
         "  select -r \"" + pass + "\";\n" + 
         "  setAttr .animation 1;\n" +
         "  setAttr .startFrame " + range.getStart() + ";\n" +
         "  setAttr .endFrame " + range.getEnd() + ";\n\n");

      Path ribs = new Path(getTempPath(agenda), 
                           target.getFilePattern().getPrefix() + ".#.rib");
      out.write
        ("  setAttr .renderMode 1;\n" + 
         "  setAttr -type \"string\" .ribFilename \"" + ribs + "\";\n\n");

      FilePattern fpat = fullTarget.getFilePattern(); 
      int statsLevel = getSingleEnumParamIndex(aRenderVerbosity) + 1;

      out.write
        ("  setAttr .renderPrimaryDisplay 1;\n" + 
         "  setAttr -type \"string\" .imageFilename \"" + fpat + "\";\n" + 
         "  setAttr -type \"string\" .statisticsFile \"\";\n" + 
         "  setAttr .statisticsLevel " + statsLevel + ";\n\n");
      
      SortedSet<FileSeq> secondary = agenda.getSecondaryTargets();
      if(!secondary.isEmpty()) {
        out.write
          ("  setAttr .renderSecondaryDisplays 1;\n" + 
           "  if(`attributeQuery -exists -multi " + 
                    "-node \"" + pass + "\" \"secondaryFilenames\"`) {\n\n");

        for(FileSeq sfseq : secondary) {
          FilePattern sfpat = sfseq.getFilePattern();

          FileSeq tfseq = new FileSeq(agenda.getTargetPath().toString(), sfseq); 
          FilePattern tfpat = tfseq.getFilePattern();

          out.write
            ("    {\n" + 
             "      $found = 0;\n" + 
             "      int $size = `getAttr -s .secondaryFilenames`;\n" +
             "      int $wk;\n" + 
             "      for($wk=0; $wk<$size; $wk++) {\n" + 
             "        string $s = `getAttr (\".secondaryFilenames[\" + $wk + \"]\")`;\n" + 
             "        if((size($s) > 0) && endsWith($s, \"" + sfpat + "\")) {\n" + 
             "          setAttr -type \"string\" (\".secondaryFilenames[\" + $wk + \"]\")\n" +
             "                  \"" + tfpat + "\";\n" + 
             "          $found = 1;\n" + 
             "          break;\n" + 
             "        }\n" + 
             "      }\n" + 
             "\n" + 
             "      if(!$found) {\n" + 
             "        error \"Unable to find a secondary display which matches the " + 
             "secondary file sequence (" + sfpat + ")!\";\n" + 
             "        quit -exitCode 1;\n" + 
             "      }\n" + 
             "    }\n");
        }

        out.write("  }\n\n");
      }

      String shaderPath = getSingleStringParamValue(aShaderPath); 
      if(shaderPath != null)
        out.write("  setAttr -type \"string\" .shaderPath " + 
                  "\"" + agenda.evaluate(shaderPath) + "\";\n");

      String procPath = getSingleStringParamValue(aProceduralPath); 
      if(procPath != null)
        out.write("  setAttr -type \"string\" .proceduralPath " + 
                  "\"" + agenda.evaluate(procPath) + "\";\n");

      String texturePath = getSingleStringParamValue(aTexturePath); 
      if(texturePath != null)
        out.write("  setAttr -type \"string\" .texturePath " + 
                  "\"" + agenda.evaluate(texturePath) + "\";\n");

      out.write("}\n\n");

      if(postExportMEL != null)
	out.write("// POST-EXPORT SCRIPT\n" + 
                  "print \"Post-Export Script: " + postExportMEL + "\\n\";\n" +
                  "source \"" + postExportMEL + "\";\n\n");
      
      out.write
        ("// EXPORT RIBS\n" + 
         "delightRender \"" + pass + "\";\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary script file (" + exportMEL + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }


    /* create a temporary Python script file to export RIB files from Maya
         followed by per-frame MentalRay renders */
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);
      
      /* import modules */ 
      out.write("import os\n");

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* export the RIB files from Maya */ 
      out.write(createMayaPythonLauncher(sourceScene, exportMEL) + "\n");

      /* RIB file paths */ 
      FileSeq ribSeq = null;
      {
        FilePattern fpat = target.getFilePattern();
        FilePattern mpat = new FilePattern(fpat.getPrefix(), fpat.getPadding(), "rib");
        FrameRange range = target.getFrameRange();

        ribSeq = new FileSeq(getTempPath(agenda).toString(), new FileSeq(mpat, range));
      }

      /* verify that the RIBs where actually generated */ 
      out.write(getPythonFileVerify(ribSeq, "RIB"));

      /* construct to common 3Delight command-line arguments */  
      String common = null;
      {
        StringBuilder buf = new StringBuilder();

        String renderdl = "renderdl"; 
        if(PackageInfo.sOsType == OsType.Windows) 
          renderdl = "renderdl.exe"; 

        buf.append("launch('" + renderdl + "', ['-noinit', ");

        for(String extra : getExtraOptionsArgs()) 
          buf.append("'" + extra + "', ");

        common = buf.toString();
      }

      /* render the RIB files and cleanup */ 
      {
        for(Path path : ribSeq.getPaths()) {
          out.write(common + "'" + path + "'])\n");
        }
        out.write("\n");

        if(!getSingleBooleanParamValue(aKeepTempFiles)) {
          for(Path path : ribSeq.getPaths()) 
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

  private static final long serialVersionUID = 237923057690974328L;

  public static final String aRenderPass      = "RenderPass";
  public static final String aMayaScene       = "MayaScene";
  public static final String aPreExportMEL    = "PreExportMEL";
  public static final String aPostExportMEL   = "PostExportMEL";
  public static final String aKeepTempFiles   = "KeepTempFiles";
  public static final String aRenderVerbosity = "RenderVerbosity";
  public static final String aShaderPath      = "ShaderPath";
  public static final String aProceduralPath  = "ProceduralPath";
  public static final String aTexturePath     = "TexturePath";

}
