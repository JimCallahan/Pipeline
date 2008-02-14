package us.temerity.pipeline.plugin.NukeThumbnailAction.v2_4_2;

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
 *   Add Alpha<BR>
 *   <DIV style="margin-left: 40px;">
 *    Whether to add an solid alpha channel to the input image before resizing and/or 
 *    compositing over the optional background layer.
 *   </DIV> 
 * 
 *   Over Background<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to composite the thumbnail images over a constant colored background layer.
 *   </DIV> 
 * 
 *   Background Color<BR>
 *   <DIV style="margin-left: 40px;">
 *     The background color to be used to composite the thumbnail image over.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * By default, this Action executes the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Action plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 */
public 
class NukeThumbnailAction
  extends NukeActionUtils
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
    super("NukeThumbnail", new VersionID("2.4.2"), "Temerity", 
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
	new BooleanActionParam
	(aAddAlpha, 
	 "Whether to add an solid alpha channel to the input image before resizing and/or " + 
         "compositing over the optional background layer.", 
         false);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aOverBackground, 
	 "Whether to composite the thumbnail images over a constant colored " + 
         "background layer.", 
         true);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Color3dActionParam
	(aBackgroundColor, 
	 "The thumbnail is composited over a background layer of this constant color.", 
	 new Color3d(0.5, 0.5, 0.5));
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageNumber);   
      layout.addEntry(aThumbnailSize);  
      layout.addSeparator();
      layout.addEntry(aAddAlpha);   
      layout.addEntry(aOverBackground);   
      layout.addEntry(aBackgroundColor);   

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

      Path fpath = null;
      if(fseq.hasFrameNumbers()) {
        int frame = getSingleIntegerParamValue(aImageNumber, new Range<Integer>(0, null));
        if(!range.isValid(frame))
          throw new PipelineException
            ("The specified Image Number (" + frame + ") does not exist in the source " + 
             "file sequence (" + fseq + ")!"); 

        fpath = fseq.getPath(range.frameToIndex(frame));
      }
      else {
        fpath = fseq.getPath(0);
      }

      Path spath = new Path("WORKING" + sname);
      sourcePath = new Path(spath.getParentPath(), fpath); 
    }

    /* the target thumbnail image path */
    Path targetPath = null;
    String targetSuffix = null;
    {
      FileSeq targetSeq = agenda.getPrimaryTarget();
      if(!targetSeq.isSingle()) 
	throw new PipelineException
	  ("The NukeThumbnail Action requires a single target image file!"); 

      targetPath = getPrimaryTargetPath(agenda, "thumbnail image");
      targetSuffix = targetSeq.getFilePattern().getSuffix();
    }

    /* create a temporary Nuke script */ 
    Path script = new Path(createTemp(agenda, "nk"));
    try {    
      int size = getSingleIntegerParamValue(aThumbnailSize, new Range<Integer>(1, null));
      boolean overBg = getSingleBooleanParamValue(aOverBackground);

      Color3d bg = (Color3d) getSingleParamValue(aBackgroundColor); 
      if(bg == null) 
        throw new PipelineException
          ("The BackgroundColor was not specified!");
      
      FileWriter out = new FileWriter(script.toFile()); 

      /* create a constant background? */ 
      if(overBg) 
        out.write("Constant {\n" + 
                  " inputs 0\n" + 
                  " channels rgb\n" + 
                  " color {0.576754 0.719033 0.821674 0}\n" + 
                  " name BG\n" + 
                  "}\n");

      /* read the file in */
      out.write("Read {\n" + 
                " inputs 0\n" + 
                " file " + sourcePath + "\n" +
                " name READ\n" + 
                "}\n");

      /* add an alpha channel to the input image? */
      if(getSingleBooleanParamValue(aAddAlpha)) 
        out.write("add_layer {alpha rgba.alpha}\n" + 
                  "AddChannels {\n" + 
                  " channels alpha\n" + 
                  " color 1\n" + 
                  " name ALPHA\n" + 
                  "}\n");

      /* resize it */
      out.write("Reformat {\n" + 
                " type \"to box\"\n" + 
                " box_width " + size + "\n" + 
                " box_height " + size + "\n" + 
                " name RESIZE\n" + 
                "}\n");

      /* comp resized image over the background? */ 
      if(overBg) 
        out.write("Merge2 {\n" + 
                  " inputs 2\n" + 
                  " operation under\n" + 
                  " bbox B\n" + 
                  " name MERGE\n" + 
                  "}\n");
      
      /* write the thumbnail out */
      out.write("Write {\n" + 
                " file " + targetPath + "\n"); 
      
      if((targetSuffix != null) && 
         (targetSuffix.equals("tif") || targetSuffix.equals("tiff"))) 
        out.write(" file_type tiff\n" + 
                  " compression LZW\n");

      out.write(" name WRITE\n" + 
                "}\n");

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary Nuke script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-nx"); 
      args.add(script.toString()); 
      args.add("1"); 

      return createSubProcess(agenda, getNukeProgram(agenda), args, agenda.getEnvironment(), 
                              agenda.getTargetPath().toFile(), outFile, errFile);
    }
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aImageNumber     = "ImageNumber";
  public static final String aThumbnailSize   = "ThumbnailSize"; 
  public static final String aAddAlpha        = "AddAlpha"; 
  public static final String aOverBackground  = "OverBackground"; 
  public static final String aBackgroundColor = "BackgroundColor"; 
  
  private static final long serialVersionUID = -8783247176363796620L;

}
