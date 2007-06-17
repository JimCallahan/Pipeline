// $Id: HdrToTifAction.java,v 1.1 2007/06/17 15:34:40 jim Exp $

package us.temerity.pipeline.plugin.HdrToTifAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 
import us.temerity.pipeline.math.Range; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H D R   T O   T I F   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts high dynamic range (HDR) images into low dynamic range TIFF images suitable
 * for display by common images viewers. <P> 
 * 
 * Converts the HDR images which make up the primary file sequence of one of the source
 * nodes into the TIFF images which make up the primary file sequence of this node. <P>
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for 
 * <A href="http://www.3delight.com/ZDoc/3delight_14.html"><B>hdri2tif</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   HDR Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the HDR files to convert. <BR> 
 *   </DIV> 
 * 
 *   Middle Gray <BR>
 *   <DIV style="margin-left: 40px;">
 *     The middle gray value of the image. <BR> 
 *   </DIV> <BR>
 * 
 *   Scales <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of gaussian convolutions to use when computing luminance information 
 *     for each pixel. <BR> 
 *   </DIV> <BR>
 * 
 *   Sharpness <BR>
 *   <DIV style="margin-left: 40px;">
 *     The gaussian convolution sharpness. <BR>
 *   </DIV> <BR>
 * 
 *   Gamma <BR>
 *   <DIV style="margin-left: 40px;">
 *     The post conversion gamma correction factor. <BR> 
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output TIF images.<BR>
 *   </DIV> <BR>
 * </DIV><P> 
 * 
 * By default, the "python" program is used by this action to run the "hdr2tif" commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class HdrToTifAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HdrToTifAction() 
  {
    super("HdrToTif", new VersionID("2.2.1"), "Temerity", 
	  "Converts high dynamic range (HDR) images into low dynamic range TIF images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aHDRSource,
	 "The source node containing the high dynamic range (HDR) images to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aMiddleGray, 
	 "The middle gray value of the image.", 
	 0.18);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aScales, 
	 "The number of gaussian convolutions to use when computing luminance " + 
	 "information for each pixel.",
	 8);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aSharpness, 
	 "The gaussian convolution sharpness.",
	 8);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aGamma, 
	 "The post conversion gamma correction factor.",
	 1.0);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("LZW");
      choices.add("Deflate");
      choices.add("PackBits");
      choices.add("None");

      ActionParam param = 
	new EnumActionParam
	(aCompression, 
	 "The compression method to use for the output TIF images.",
	 "LZW", choices);
      addSingleParam(param);
    }      
    
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aHDRSource);
      layout.addSeparator();
      layout.addEntry(aMiddleGray);
      layout.addEntry(aScales);
      layout.addEntry(aSharpness);
      layout.addSeparator();
      layout.addEntry(aGamma);
      layout.addEntry(aCompression);   

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
    /* file sequence checks */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    FileSeq targetSeq = null;
    {
      {    
	String sname = (String) getSingleParamValue(aHDRSource); 
	if(sname == null) 
	  throw new PipelineException
	    ("The HDR Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the HDR Source (" + sname + ") was not one of the source nodes!");
	
	sourceSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !(suffix.equals("tiff") || suffix.equals("tif")))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain TIF images!");
	
	targetSeq = fseq;
      }

      if(sourceSeq.numFrames() != targetSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + sourceSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + targetSeq + ")!");
    }


    /* build common command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    {
      args.add("-verbose");

      double gray = getSingleDoubleParamValue(aMiddleGray, new Range(0.0, 1.0));
      args.add("-key");
      args.add(Double.toString(gray));
    
      int scales = getSingleIntegerParamValue(aScales);
      args.add("-nscales");
      args.add(Integer.toString(scales));

      int sharp = getSingleIntegerParamValue(aSharpness);
      args.add("-sharpness");
      args.add(Integer.toString(sharp));
    
      double gamma = getSingleDoubleParamValue("Gamma");
      args.add("-gamma");
      args.add(Double.toString(gamma));

      switch(getSingleEnumParamIndex(aCompression)) {
      case 0:
	args.add("-lzw");
	break;

      case 1:
	args.add("-deflate");
	break;

      case 2:
	args.add("-packbits");
	break;
	
      case 3:
	args.add("-c-");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Compression value!");
      }
    }

    /* DSM conversion program */ 
    String program = "hdri2tif";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "hdri2tif.exe";
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, program, args, sourcePath, sourceSeq, targetSeq, 
                                  null, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9079892286019545643L;

  public static final String aHDRSource   = "HDRSource";
  public static final String aMiddleGray  = "MiddleGray";
  public static final String aScales      = "Scales";
  public static final String aSharpness   = "Sharpness";
  public static final String aGamma       = "Gamma";
  public static final String aCompression = "Compression";
  

}

