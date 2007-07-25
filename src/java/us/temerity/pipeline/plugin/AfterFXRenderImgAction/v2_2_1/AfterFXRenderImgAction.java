package us.temerity.pipeline.plugin.AfterFXRenderImgAction.v2_2_1;

import java.io.*;
import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.AfterFXActionUtils;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   R E N D E R   I M G   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders an After Effects composition out to a single image or image sequence.
 * <p>
 * Due to certain limitation in the After Effects scripting API there are some restrictions
 * that apply to this Action. The composition needs to already contain a Render Queue entry
 * for the Composition that you want to render. This Render Queue entry must have only one
 * Output Module associated with it and that Output Module must have an output file name
 * whose suffix matches the suffix of the node. The filename and padding set in the Output
 * Module are ignored, but the suffix must be correct.
 * <p>
 * If there are multiple Render Queue entries for the named Composition, this Action will
 * abort with an error code of 44. If the suffix of the Output Module does not match, this
 * Action will abort with an error code of 45. If there is no Render Queue Item for the
 * named Composition, this Action will abort with an error code of 46.  But apparantly After
 * Effects doesn't report the exit code correctly, so I don't think any of this really works.
 * So you're just going to have to guess why it isn't rendering until I figure out how to
 * make it work in a somewhat rational way.  joy.
 */
public 
class AfterFXRenderImgAction
  extends AfterFXActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AfterFXRenderImgAction()
  {
    super("AfterFXRenderImg", new VersionID("2.2.1"), "Temerity",
	  "Renders an image or image sequence from an After Effects scene.");
    
    {
      ActionParam param = 
  	new StringActionParam
  	(aCompName,
  	 "The name of the After Effects Comp to create.",
  	 "Comp1");
      addSingleParam(param);
    }
    
    {
      ActionParam param =
	new LinkActionParam
        (aAfterFXScene, 
         "The source After Effects scene node.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
	new LinkActionParam
        (aPreRenderScript, 
         "A JavaScript script to run before the images are rendered.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
	new LinkActionParam
        (aPostRenderScript, 
            "A JavaScript script to run after the images are rendered.", 
         null);
      addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aAfterFXScene);
      layout.addEntry(aCompName);
      {
	LayoutGroup sub = new LayoutGroup("Javascripts", "Pre and Post Render Scripts", true);
	sub.addEntry(aPreRenderScript);
	sub.addEntry(aPostRenderScript);
	layout.addSubGroup(sub);
      }
      setSingleLayout(layout);
    }
    
    /* 
     * Support for OsX can be added when After Effects CS3 launches, making it possible
     * to run AfterFX as an AppleScript app.  I have not been able to get the Mac version
     * of 7.0 to run scripts from the command line.
     */
    //addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
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
    String suffix = null;
    {
      target = agenda.getPrimaryTarget();
      suffix = target.getFilePattern().getSuffix();
      if(suffix == null) 
	throw new PipelineException
	  ("The target file sequence (" + target + ") must have a filename suffix!");
    }
    
    ArrayList<String> extensions = new ArrayList<String>();
    // This probably needs to be extended
    {
      extensions.add("bmp");
      extensions.add("iff");
      extensions.add("jpeg");
      extensions.add("exr");
      extensions.add("png");
      extensions.add("psd");
      extensions.add("sgi");
      extensions.add("tga");
      extensions.add("tif");
    }
    
    Path targetDir = 
      getPrimaryTargetPaths
        (agenda, extensions, "The images Files to Render").get(0).getParentPath();
    
    String fileName = new Path(targetDir, target.getFilePattern().getPrefix()).toOsString();
    
    if (target.hasFrameNumbers()) {
      int padding = target.getFilePattern().getPadding();
      if (padding == 0 || padding == 1)
	fileName += ".[#]";
      else {
	fileName += ".[";
	for (int i = 0; i < padding ; i++) {
	  fileName += "#";
	}
	fileName += "]";
      }
    }
    
    fileName += "." + suffix;
    FrameRange range = target.getFrameRange(); 
    int startFrame = range.getStart();
    int endFrame = range.getEnd();
    int skipFrame = range.getBy() - 1;
    
    String compName = getSingleStringParamValue(aCompName);
    
    Path sourceScene = getAfterFXSceneSourcePath(aAfterFXScene, agenda);
    
    Path preRenderScript = getJavaScriptSourcePath(aPreRenderScript, agenda);
    Path postRenderScript = getJavaScriptSourcePath(aPostRenderScript, agenda);
    
    File script = createTemp(agenda, "jsx");
    try {      
      BufferedWriter out = new BufferedWriter(new FileWriter(script));
      
      out.write("app.exitAfterLaunchAndEval = true;\n" + 
      		"app.beginSuppressDialogs();\n" + 
      		"app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
      		"var f = new File(\"" + CommonActionUtils.escPath(sourceScene) + "\");\n" +
		"app.open(f);\n\n");
      
      writeSourceRelinkingScript(out, agenda);
      
      if (preRenderScript != null)
	writeScriptToFile(out, preRenderScript);
      
      out.write("{\n");
      
      
      out.write(" var compName = \"" + compName + "\";\n" + 
      		" var startFrame = " + startFrame + ";\n" + 
      		" var endFrame = " + endFrame + ";\n" + 
      		" var skipFrame = " + skipFrame + ";\n" + 
      		" \n" + 
      		" var extension = \"" + suffix + "\";\n" + 
      		" \n" + 
      		" var outputName = \"" + CommonActionUtils.escPath(fileName) + "\";\n");
      
      out.write(" var proj = app.project;\n" + 
      		" var renderQueue = proj.renderQueue;\n" + 
      		" var items = renderQueue.items;\n" + 
      		" \n" + 
      		" var renderItem = null;\n" + 
      		" var frameDur = null;\n" + 
      		" var found = false;\n");
      
      out.write(" for (var i = 1; i <= items.length ; i++) {\n" + 
      		" 	var item = items[i];\n" + 
      		" 	var compItem = item.comp;\n" + 
      		" 	var cName = compItem.name;\n" + 
      		" 	if (cName == compName) {\n" + 
      		" 	  if (found == true) {\n" + 
      		"               app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
      		" 	  	app.exitCode = -44;\n" +
      		" 	  	app.quit();\n" + 
      		" 	  }\n" + 
      		" 	  else {\n" + 
      		" 	    found = true;\n" + 
      		" 	    renderItem = item;\n" + 
      		" 	    item.render = true;\n" + 
      		" 	    frameDur = compItem.frameDuration;\n" + 
      		" 	  }\n" + 
      		" 	} \n" + 
      		" 	else {\n" + 
      		" 	  item.render = false;\n" + 
      		" 	}\n" + 
      		" }\n" + 
      		" \n" +
      		"if (!renderItem) {\n" + 
      		"  app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
      		"  app.exitCode = -46;\n" +
      		"  app.quit();\n" + 
      		"}\n" + 
      		"var module = renderItem.outputModules[1];\n" + 
      		"var file = module.file;\n" + 
      		"var split = file.fullName.split(\".\");\n" + 
      		"var length = split.length;\n" + 
      		"var ext = split[length - 1];\n" + 
      		"if (ext != extension) {\n" + 
      		"  app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
      		"  app.exitCode = -45;\n" +
      		"  app.quit();\n" + 
      		"}\n" + 
      		"module.file = new File(outputName);\n" + 
      		"renderItem.timeSpanStart = frameDur * startFrame ;\n" + 
      		"renderItem.timeSpanDuration = frameDur * (endFrame - startFrame + 1) ;\n" + 
      		"renderItem.skipFrames = skipFrame;\n" + 
      		"\n" + 
      		"renderQueue.render();\n");
      
      out.write("}\n");
      
      if (postRenderScript != null)
	writeScriptToFile(out, postRenderScript);
      
      out.write("app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
      		"app.quit();\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary JSX script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-m");
      args.add("-r");
      args.add(script.getAbsolutePath());
      
      return createSubProcess(agenda, "AfterFX.exe", args, outFile, errFile);
    }    
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private void
  writeScriptToFile
  (
    BufferedWriter out,
    Path script
  ) 
    throws IOException
  {
    BufferedReader in = new BufferedReader(new FileReader(script.toFile()));
    
    out.write("\\ Sourcing JavaScript " + script.toString() + "\n");
    out.write("{\n");
    
    String line = in.readLine();
    while (line != null) {
      out.write(line + "\n");
      line = in.readLine();
    }
    out.write("}\n");
    out.write("\\ Done Sourcing JavaScript " + script.toString() + "\n");
  }
  
  protected void
  writeSourceRelinkingScript
  (
    BufferedWriter out,
    ActionAgenda agenda
  ) 
    throws PipelineException, IOException
  {
    
    String workingStart = PackageInfo.sWorkPath.toOsString().replaceAll("\\\\", "/");
    String currentWorking = agenda.getEnvironment().get("WORKING").replaceAll("\\\\", "/");
    
    out.write("{\n" + 
	"var workingStart = \"" + workingStart + "/\";\n" +
	"var currentWorking = \"" + currentWorking + "/\";\n" +
	"var proj = app.project;\n" +
	"var list = proj.items;\n" +
	"var regExp = new RegExp(workingStart, \"g\");\n" + 
	"for (j=1; j <= list.length; j++)\n" + 
	"{\n" + 
	"  var item = list[j];\n" +
	"  if (item instanceof FootageItem)\n" + 
	"  {\n" + 
	"     var file = item.file;\n" + 
	"     if (regExp.test(file))\n" + 
	"     {\n" + 
	"	var fileName = file.fullName;\n" + 
	"	var endName = fileName.replace(workingStart, \"\");\n" + 
	"	var split = endName.split(\"/\");\n" + 
	"	var newEnd = \"\";\n" + 
	"	for (i=2; i < split.length; i++)\n" + 
	"	{\n" + 
	"	  newEnd += split[i];\n" + 
	"	  if (i != split.length -1)\n" + 
	"	    newEnd += \"/\";\n" + 
	"	}\n" + 
	"	var newFileName = currentWorking + newEnd;\n" + 
	"	var newFile = new File(newFileName);\n" + 
	"	item.replaceWithSequence(newFile, false);\n" + 
	"    }\n" + 
	"  }\n" + 
	"}\n" +
	"}\n");
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6912006079342155094L;
  
  public static final String aCompName = "CompName";
  
}
