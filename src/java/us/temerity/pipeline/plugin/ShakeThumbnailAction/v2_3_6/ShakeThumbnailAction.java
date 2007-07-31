package us.temerity.pipeline.plugin.ShakeThumbnailAction.v2_3_6;

import java.io.File;
import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.CommonActionUtils;

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
 *   Background Color<BR>
 *   <DIV style="margin-left: 40px;">
 *    If specified, the background color to be used to composite the thumbnail image over.
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
    super("ShakeThumbnail", new VersionID("2.3.6"), "Temerity", 
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

    /* create the process to run the action */
    {
      ArrayList<String> args = new ArrayList<String>();

      int size = getSingleIntegerParamValue(aThumbnailSize, new Range<Integer>(1, null));
      String sizeStr = Integer.toString(size); 
      
      Color3d bg = (Color3d) getSingleParamValue(aBackgroundColor); 
      if(bg == null) 
        throw new PipelineException
          ("The BackgroundColor was not specified!");

      args.add("-color");      
      args.add(sizeStr);     
      args.add(sizeStr);      
      args.add("1");      
      args.add(Double.toString(bg.r()));
      args.add(Double.toString(bg.g()));
      args.add(Double.toString(bg.b()));
      
      args.add("-label");      
      args.add("bg");      

      args.add("-fi"); 
      args.add(sourcePath.toOsString());
     
      args.add("-fit");      
      args.add(sizeStr);      
      args.add(sizeStr);      

      args.add("-over");      
      args.add("bg");      

      args.add("-fo");
      args.add(targetPath.toOsString());

      return createSubProcess(agenda, "shake", args, outFile, errFile);
    }
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aImageNumber     = "ImageNumber";
  public static final String aThumbnailSize   = "ThumbnailSize"; 
  public static final String aBackgroundColor = "BackgroundColor"; 
  
  private static final long serialVersionUID = 6801919495288012115L;

}
