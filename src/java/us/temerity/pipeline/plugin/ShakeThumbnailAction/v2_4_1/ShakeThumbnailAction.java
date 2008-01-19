package us.temerity.pipeline.plugin.ShakeThumbnailAction.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.CommonActionUtils;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   S H A K E   T H U M B N A I L   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts and resizes a single image frame from a source sequence for use as a thumbnail
 * image on web pages. <P> 
 * 
 * This action uses the Shake to perform the image conversion and resize operations.  
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
 */
public 
class ShakeThumbnailAction
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShakeThumbnailAction() 
  {
    super("ShakeThumbnail", new VersionID("2.4.1"), "Temerity", 
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
    /* the source image path */ 
    Path sourcePath = null;
    {
      String sname = agenda.getSourceName(); 
      if(sname == null) 
	throw new PipelineException
	  ("The ShakeThumbnail Action only accepts a single source node.");

      FileSeq fseq = agenda.getPrimarySource(sname);
      if(!fseq.hasFrameNumbers())
        throw new PipelineException
          ("The ShakeThumbnail Action requires that the source file sequence " + 
           "(" + fseq + ") has frame numbers!");

      int frame = getSingleIntegerParamValue(aImageNumber, new Range<Integer>(0, null));

      FrameRange range = fseq.getFrameRange();
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
	  ("The ShakeThumbnail Action requires a single target image file!"); 

      targetPath = getPrimaryTargetPath(agenda, "thumbnail image");
    }

    /* create a temporary Shake script */ 
    Path script = new Path(createTemp(agenda, "shk"));
    try {    
      int size = getSingleIntegerParamValue(aThumbnailSize, new Range<Integer>(1, null));
      boolean overBg = getSingleBooleanParamValue(aOverBackground);

      Color3d bg = (Color3d) getSingleParamValue(aBackgroundColor); 
      if(bg == null) 
        throw new PipelineException
          ("The BackgroundColor was not specified!");
      
      FileWriter out = new FileWriter(script.toFile()); 

      if(overBg) 
        out.write
          ("Bg1 = Color(Fit1.xSize, Fit1.ySize, 1, " + 
           bg.r() + ", " + bg.g() + ", " + bg.b() + ", 1, 0);\n");
      
      out.write
        ("In1 = FileIn(\"" + sourcePath.toOsString() + "\", \"Auto\", 0, 0);\n");

      String fitIn = "In1";
      if(getSingleBooleanParamValue(aAddAlpha)) {
        out.write("SetAlpha1 = SetAlpha(In1, 1);\n");
        fitIn = "SetAlpha1";
      }

      out.write
        ("Fit1 = Fit(" + fitIn + ", " + 
         "In1.width > In1.height ? " + size + " : " + size + "*(In1.width/In1.height), " + 
         "In1.width < In1.height ? " + size + " : " + size + "*(In1.height/In1.width), " + 
         "\"default\", \"default\", 1);\n");

      String outIn = "Fit1";
      if(overBg) {
        out.write("Over1 = Over(Fit1, Bg1, 0, 0, 0);\n"); 
        outIn = "Over1"; 
      }
      
      out.write
        ("Out1 = FileOut(" + outIn + ", \"" + targetPath.toOsString() + "\", \"Auto\");\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary Shake script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */
    {
      ArrayList<String> args = new ArrayList<String>();

      args.add("-exec");
      args.add(script.toOsString());

      return createSubProcess(agenda, "shake", args, outFile, errFile);
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
  
  private static final long serialVersionUID = -4837189460564992093L;

}
