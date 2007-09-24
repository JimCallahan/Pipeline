package us.temerity.pipeline.plugin.NukeThumbnailAction.v2_3_6;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   N U K E   T H U M B N A I L   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts and resizes a single image frame from a source sequence for use as a thumbnail
 * image on web pages. <P> 
 * 
 * This action uses the Nuke to perform the image conversion and resize operations.  
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Number <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies which image from the source image sequence should be processed.
 *   </DIV> 
 * 
 *   Thubnail Size <BR> 
 *   <DIV style="margin-left: 40px;">
 *    The maximum width/height of the generated thumbnail image.  The image will be scaled 
 *    proportionally so that it fits within a square region of this size.
 *   </DIV> 
 * 
 *   Background Color<BR>
 *   <DIV style="margin-left: 40px;">
 *    If specified, the background color to be used to composite the thumbnail image over.
 *   </DIV> 
 * </DIV> <P> 
 */
public 
class NukeThumbnailAction
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * 
   */
  public
  NukeThumbnailAction() 
  {
    super("NukeThumbnail", new VersionID("2.3.6"), "Temerity", 
	  "Generates a smaller version of a single image from a sequence of images.");
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aImageNumber, 
	 "Specifies the frame number of image from the source sequence to process.", 
	 1);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aThumbnailSize, 
	 "The maximum width/height of the generated thumbnail image.  The image will be " + 
         "scaled proportionally so that it fits within a square region of this size.", 
	 100);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Color3dActionParam
	(aBackgroundColor, 
	 "The thumbnail is composited over a background image of this color.", 
	 new Color3d(0.0, 0.0, 0.0));
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageNumber);   
      layout.addEntry(aThumbnailSize);   
      layout.addEntry(aBackgroundColor);   

      setSingleLayout(layout);  
    }

    underDevelopment();

    addSupport(OsType.MacOS);
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
    String sname = agenda.getSourceName(); 
    FileSeq fseq = agenda.getPrimarySource(sname);
    FrameRange range = fseq.getFrameRange();

    /* the source image path */ 
    Path sourcePath = null;
    {
      if(sname == null) 
	throw new PipelineException
	("The NukeThumbnail Action only accepts a single source node.");

      if(!fseq.hasFrameNumbers())
	throw new PipelineException
	("The NukeThumbnail Action requires that the source file sequence " + 
	  "(" + fseq + ") has frame numbers!");

      int frame = getSingleIntegerParamValue(aImageNumber, new Range<Integer>(0, null));

      if(!range.isValid(frame))
	throw new PipelineException
	("The specified Image Number (" + frame + ") does not exist in the source file " + 
	  "sequence (" + fseq + ")!"); 

      int index = range.frameToIndex(frame);
      sourcePath = getWorkingNodeFilePaths(agenda, sname, fseq).get(index);
    }

    /* the target thumbnail image path */
    Path targetPath = null;
    {
      FileSeq targetSeq = agenda.getPrimaryTarget();
      if(!targetSeq.isSingle()) 
	throw new PipelineException
	  ("The NukeThumbnail Action requires a single target image file!"); 

      targetPath = getPrimaryTargetPath(agenda, "thumbnail image");
    }

    /* create a temporary Nuke script */ 
    Path tclScript = new Path(createTemp(agenda, "tcl"));
    try {    
      int size = getSingleIntegerParamValue(aThumbnailSize, new Range<Integer>(1, null));

      Color3d bg = (Color3d) getSingleParamValue(aBackgroundColor); 
      if(bg == null) 
        throw new PipelineException
          ("The BackgroundColor was not specified!");
      
      FileWriter out = new FileWriter(tclScript.toFile()); 

      /* read the file in */
      out.write("Read -New name READ file " + sourcePath.toOsString() + "\n");

      /* resize it */
      out.write("Reformat -New name RESIZE type \"to box\" " +
      		"box_width " + size + " box_height " + size + "\n");

      /* comp over BG */
      out.write("Merge2 -New name MERGE operation under bbox B\n");
      
      /* output */
      out.write("Write -New name WRITE file " + targetPath.toOsString() + "\n");
      
      /* BG */
      out.write("Constant -New name BG color {"+bg.r()+" "+bg.g()+" "+bg.b()+"}\n");
      out.write("input MERGE 1 BG\n");

      /* do it */
      out.write("execute WRITE 1\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary TCL script file (" + tclScript + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* Create a temporary Python script to run Nuke piping the script to STDIN 
     * NOTE:  we're passing a "-d :0" argument to Nuke, which defines the X Display.
     * This shouldn't be necessary in Terminal Mode, but for some reason, Nuke spits
     * out a 'can't open DISPLAY ""' error if we don't.
     * 
     * TODO:  turns out that the Nuke "save_script" function is trying to open the
     * X display, even in terminal mode.  This causes this action to barf on machines
     * with no X servers (um, like anything in a renderfarm).  Need to figure out why
     * the heck Nuke is doing this, and make it stop.
     */ 
    Map<String, String> env = agenda.getEnvironment();
    File pyScript = createTemp(agenda, "py");
    try {
      FileWriter out = new FileWriter(pyScript); 

      String nukeApp = NukeActionUtils.getNukeProgram(env); 
      out.write
      ("import subprocess\n" + 
	"nukeScript = open('" + tclScript + "', 'r')\n" + 
	"p = subprocess.Popen(['" + nukeApp + "', '-t', '-d :0'], stdin=nukeScript)\n" + 
      "p.communicate()\n");
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
      ("Unable to write temporary Python script file (" + pyScript + ") for Job " + 
	"(" + agenda.getJobID() + ")!\n" +
	ex.getMessage());
    }

    /* create the process to run the action */
    {
      ArrayList<String> args = new ArrayList<String>();
      String python = PythonActionUtils.getPythonProgram(env);
      args.add(pyScript.getPath());

      return createSubProcess(agenda, python, args, outFile, errFile);
    }    
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aImageNumber     = "ImageNumber";
  public static final String aThumbnailSize   = "ThumbnailSize"; 
  public static final String aBackgroundColor = "BackgroundColor"; 
  
  private static final long serialVersionUID = 4550814638289129942L;

}
