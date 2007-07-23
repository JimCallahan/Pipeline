package us.temerity.pipeline.plugin.ThumbnailAction.v2_3_3;

import java.io.File;
import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   T H U M B N A I L   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts and resizes a single image frame from a source sequence for use as a thumbnail
 * image on web pages. <P> 
 * 
 * This action uses the ImageMagick convert(1) command line utility to perform the image 
 * conversion and resize operation.  See the man pages for convert(1) and ImageMagick(1) 
 * for details of the supported image formats. <P> 
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
 * </DIV> <P> 
 */
public 
class ThumbnailAction
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ThumbnailAction() 
  {
    super("Thumbnail", new VersionID("2.3.3"), "Temerity", 
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
	  ("The Thumbnail Action only accepts a single source node.");

      FileSeq fseq = agenda.getPrimarySource(sname);
      if(!fseq.hasFrameNumbers())
        throw new PipelineException
          ("The Thumbnail Action requires that the source file sequence (" + fseq + " has " + 
           "frame numbers!");

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
	  ("The Thumbnail Action requires a single target image file!"); 

      targetPath = getPrimaryTargetPath(agenda, "thumbnail image");
    }

    /* create the process to run the action */
    {
      ArrayList<String> args = new ArrayList<String>();

      int size = getSingleIntegerParamValue(aThumbnailSize, new Range<Integer>(1, null));
      args.add("-resize");
      args.add(size + "x" + size);

      args.add(sourcePath.toOsString());
      args.add(targetPath.toOsString());

      String program = "convert";
      if(PackageInfo.sOsType == OsType.Windows) 
        program = "convert.exe";

      return createSubProcess(agenda, program, args, outFile, errFile);
    }
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aImageNumber   = "ImageNumber";
  public static final String aThumbnailSize = "ThumbnailSize"; 
  
  private static final long serialVersionUID = -4058999495124325725L;
}
