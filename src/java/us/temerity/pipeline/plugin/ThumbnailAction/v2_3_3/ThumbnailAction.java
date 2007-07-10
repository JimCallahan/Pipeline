package us.temerity.pipeline.plugin.ThumbnailAction.v2_3_3;

import java.io.File;
import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   T H U M B N A I L   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

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
    super("Thumbnail", new VersionID("2.3.3"), "SCEA", 
	  "Generates a smaller version of a single image from a sequence of images.");
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aImageNumber, 
	 "Which image from the sequence should be made into the thumbnail.", 
	 1);
      addSingleParam(param);
    }
    {
      ActionParam param = 
	new IntegerActionParam
	(aNewWidth, 
	 "What should the new width of the image be.  " +
	 "The height will be scaled to keep the image proportional.", 
	 512);
      addSingleParam(param);
    }
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
    Path targetPath;
    Path sourcePath = null;
    int frameNumber = getSingleIntegerParamValue(aImageNumber, new Range<Integer>(0, null));
    {
      int size = agenda.getSourceNames().size();
      if (size != 0)
	throw new PipelineException
	  ("The Thumbnail Action only accepts a single source node.");
      for (String sname : agenda.getSourceNames()) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if (!fseq.isSingle())
	  throw new PipelineException
	    ("The Thumbnail Action requires that its source node is a file sequence");
	FrameRange range = fseq.getFrameRange();
	if(range.isValid(frameNumber))
	  throw new PipelineException
	    ("The specified frame number (" + frameNumber + ") is not a valid frame in the source sequence.");
	int index = range.frameToIndex(frameNumber);
	sourcePath = getWorkingNodeFilePaths(agenda, sname, fseq).get(index);
      }
    }
    {
      FileSeq targetSeq = agenda.getPrimaryTarget();
      if (!targetSeq.isSingle()) {
	throw new PipelineException
	  ("The Thumbnail Action only works on target nodes that have a single file.");
      }
      targetPath = getPrimaryTargetPath(agenda, "The thumbnail image");
    }
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("--resize");
      int newSize = getSingleIntegerParamValue(aNewWidth, new Range<Integer>(1, null));
      args.add(newSize + "x");
      args.add(sourcePath.toOsString());
      args.add(targetPath.toOsString());
      return createSubProcess(agenda, "convert", args, outFile, errFile);
    }
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aImageNumber = "ImageNumber";
  public static final String aNewWidth = "NewWidth";
  
  private static final long serialVersionUID = -4058999495124325725L;
}
