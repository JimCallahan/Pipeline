package us.temerity.pipeline.plugin.AERenderImgAction.v2_4_23;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   A E   R E N D E R   I M G   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Render an AfterFX scene using the aerender batch render application. <p>
 * 
 * In order to use this Action, the previous node (or at least a direct upstream dependency) 
 * should be an Intermediate node with the AfterFXFixPaths Action which was run on the same
 * operating system as this render command.  That will guarantee that all the sources being 
 * used in the render are from the current working area.  <p>
 * 
 * The composition to be rendered should already have an entry in the AfterFX 
 * render queue with the correct render settings, since there is limited control over adding
 * comps to the render queue through the API.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Comp Name<BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the composition to be rendered.  There should already be a render queue 
 *     entry for this composition.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public 
class AERenderImgAction
  extends AfterFXActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AERenderImgAction()
  {
    super("AERenderImg", new VersionID("2.4.23"), "Temerity",
          "Renders an image or image sequence from an After Effects scene using aerender.");
    
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
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aAfterFXScene);
      layout.addEntry(aCompName);
      setSingleLayout(layout);
    }
    
    /*
     * Support for OS X should work, but it untested. 
     */
    addSupport(OsType.MacOS);
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
      extensions.add("jpg");
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
    
    String fileName = new Path(targetDir, target.getFilePattern().getPrefix()).toString();
    
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
    int byFrame = range.getBy();
    
    String compName = getSingleStringParamValue(aCompName, false);
    
    Path sourceScene = getAfterFXSceneSourcePath(aAfterFXScene, agenda);
    
    ArrayList<String> args = new ArrayList<String>();
    args.add("-project");
    args.add("\"" + sourceScene.toString() + "\"");
    args.add("-comp");
    args.add("\"" + compName + "\"");
    args.add("-s");
    args.add(String.valueOf(startFrame));
    args.add("-e");
    args.add(String.valueOf(endFrame));
    args.add("-i");
    args.add(String.valueOf(byFrame));
    args.add("-v");
    args.add("ERRORS_AND_PROGRESS");
    args.add("-output");
    args.add("\"" + fileName + "\"");
    
    if (PackageInfo.sOsType == OsType.Windows) {
      File launchScript = createTemp(agenda, "py");
      try {      
        FileWriter out = new FileWriter(launchScript);
        out.write(getAERenderPythonLaunchHeader()); 
        out.write("import os.path\n");
        
        out.write 
          ("launch('aerender.exe', [" + packageArgs(args) + "])\n");
        
        out.close();
      }
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write temporary python script file (" + launchScript + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }
      
      // replace this when stuff descends from PythonActionUtils
      
      String owner = agenda.getSubProcessOwner();
      String title = getName() + "-" + agenda.getJobID(); 

      ArrayList<String> nargs = new ArrayList<String>();
      nargs.add(launchScript.getPath());

      Map<String,String> nenv = agenda.getEnvironment();

      return new SubProcessHeavy(owner, title, getPythonProgram(nenv), nargs, 
                                 nenv, agenda.getTargetPath().toFile(), 
                                 outFile, errFile);
    }
    else
      return createSubProcess(agenda, "aerender", args, outFile, errFile);
  }  
  
  private static final String 
  getAERenderPythonLaunchHeader() 
  {
    /* 
     * Need to include the special case where the render succeeds but it cannot write the log
     * due to permissions. 
     */
    return 
      ("import subprocess;\n" +
       "import sys;\n\n" +
       "def launch(program, args):\n" +
       "  a = [program] + args\n" +
       "  print('RUNNING: ' + ' '.join(a))\n" +
       "  sys.stdout.flush()\n" + 
       "  result = subprocess.call(a)\n" +
       "  if result != 0 and result != 9:\n" +
       "    sys.exit('  FAILED: Exit Code = ' + str(result));\n\n");
  }
  
  private String
  packageArgs
  (
    ArrayList<String> args 
  )
  {
    String toReturn = "";

    if (args.size() > 0) {
      toReturn = "'" + args.remove(0) + "'";
      for (String arg : args) {
        toReturn += ", '" + arg + "'";
      }
    }
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 715540580696665278L;

  public static final String aCompName = "CompName";
}
