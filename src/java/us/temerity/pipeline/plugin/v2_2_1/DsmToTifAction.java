// $Id: DsmToTifAction.java,v 1.2 2007/04/12 15:23:28 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 
import us.temerity.pipeline.math.Range; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D S M   T O   T I F   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts a depth slice from a 3Delight deep shadow map (DSM) into a TIF image suitable for 
 * display. <P>
 * 
 * Converts the DSMs (.shd) which make up the primary file sequence of one of the source
 * nodes into the TIFF images which make up the primary file sequence of this node. <P>
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for 
 * <A href="http://www.3delight.com/ZDoc/3delight_13.html"><B>dsm2tif</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   DSM Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the DSM files to convert. <BR> 
 *   </DIV> 
 * 
 *   Sample Method <BR>
 *   <DIV style="margin-left: 40px;">
 *     The depth sampling method: <BR>
 *     <UL>
 *       <LI>Relative - Each pixel is evaluated at a relative depth between the minimum and 
 *           maximum depth values for the sampled pixel.
 *       <LI>Absolute - Each pixel is evaluated a depth between the minimum and maximim 
 *           depth values of the entire DSM.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Sample Depth <BR>
 *   <DIV style="margin-left: 40px;">
 *     The sample depth [0,1].  The interpretation of this depth depends on the Sample Method.
 *   </DIV> <BR>
 * 
 *   Shadow Bias <BR>
 *   <DIV style="margin-left: 40px;">
 *     The shadow bias used to avoid self occulsion when evaluating the DSM.
 *   </DIV> <BR>
 * 
 *   Mipmap Level <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the mipmap level of the DSM to evaluate.
 *   </DIV> <BR>
 * 
 *   Output Value <BR>
 *   <DIV style="margin-left: 40px;">
 *     The meaning of color values in the output TIF images:<BR>
 *     <UL>
 *       <LI> Visibility - White means unocculded, black means fully occluded.
 *       <LI> Opacity - White means fully occluded, black meand unoccluded.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Pixel Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The pixel data type of the output TIF images.<BR>
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output TIF images.<BR>
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * By default, the "python" program is used by this action to run the "dsm2tif" commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class DsmToTifAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DsmToTifAction() 
  {
    super("DsmToTif", new VersionID("2.2.1"), "Temerity",
	  "Converts a depth slice from a deep shadow map (DSM) into a TIF image " + 
	  "suitable for display.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aDSMSource,
	 "The source node containing the deep shadow maps (DSM) to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Absolute");
      choices.add("Relative");

      ActionParam param = 
	new EnumActionParam
	(aSampleMethod, 
	 "The depth sampling method.", 
	 "Absolute", choices);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	(aSampleDepth, 
	 "The sample depth [0,1].  The interpretation of this depth depends on the " + 
	 "Sample Method.", 
	 1.0);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aShadowBias, 
	 "The shadow bias used to avoid self occulsion when evaluating the DSM.", 
	 0.015); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aMipmapLevel, 
	 "Specifies the mipmap level of the DSM to evaluate.", 
	 0);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Visibility");
      choices.add("Opacity");

      ActionParam param = 
	new EnumActionParam
	(aOutputValue, 
	 "The meaning of color values in the output TIF images.",
	 "Visibility", choices);
      addSingleParam(param);
    }      

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("8-Bit");
      choices.add("16-Bit");
      choices.add("32-Bit");
      choices.add("Float");

      ActionParam param = 
	new EnumActionParam
	(aPixelType,
	 "The pixel data type of the output TIF images.", 
	 "8-Bit", choices);
      addSingleParam(param);
    }      

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("LZW");
      choices.add("Deflate");
      choices.add("PackBits");
      choices.add("LogLuv");

      ActionParam param = 
	new EnumActionParam
	(aCompression, 
	 "The compression method to use for the output TIF images.",
	 "LZW", choices);
      addSingleParam(param);
    }      
    
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aDSMSource);
      layout.addSeparator();
      layout.addEntry(aSampleMethod);
      layout.addEntry(aSampleDepth);
      layout.addSeparator();
      layout.addEntry(aShadowBias);
      layout.addEntry(aMipmapLevel);
      layout.addSeparator();
      layout.addEntry(aOutputValue);
      layout.addEntry(aPixelType);   
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
	String sname = (String) getSingleParamValue(aDSMSource); 
	if(sname == null) 
	  throw new PipelineException
	    ("The DSM Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the DSM Source (" + sname + ") was not one of the source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("shd")) 
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain 3Delight deep " + 
             "shadow maps (.shd)!");

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
      double depth = getSingleDoubleParamValue(aSampleDepth, new Range(0.0, 1.0));

      switch(getSingleEnumParamIndex(aSampleMethod)) {
      case 0:
	args.add("-Z");
	break;
	
      case 1:
	args.add("-z");
	break;

      default:
	throw new PipelineException
	  ("Illegal Sample Method!");
      }
      
      args.add(Double.toString(depth));
    }

    Double bias = (Double) getSingleParamValue(aShadowBias);
    if(bias != null) {
      args.add("-bias");
      args.add(bias.toString());
    }

    Integer level = (Integer) getSingleParamValue(aMipmapLevel);
    if(level != null) {
      args.add("-mipmap");
      args.add(level.toString());
    }

    switch(getSingleEnumParamIndex(aOutputValue)) {
    case 0:
      break;
      
    case 1:
      args.add("-opacity");
      break;
      
    default:
      throw new PipelineException
        ("Illegal Output Value!"); 
    }

    {
      boolean isFloat = false;

      switch(getSingleEnumParamIndex(aPixelType)) {
      case 0:
	args.add("-8");
	break;

      case 1:
	args.add("-16");
	break;

      case 2:
	args.add("-32");
	break;

      case 3:
	args.add("-float"); 
	isFloat = true;
	break;

      default:
	throw new PipelineException
	  ("Illegal Pixel Type!"); 
      }

      switch(getSingleEnumParamIndex(aCompression)) {
      case 0:
	args.add("-lzw");
	break;

      case 1:
	args.add("-deflate");
	break;

      case 2:
	if(!isFloat) 
	  throw new PipelineException
	    ("The LogLuv compression method can only be used with the Float pixel type!");
	args.add("-logluv");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Compression method!");
      }
    }
    
    /* DSM conversion program */ 
    String program = "dsm2tif";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "dsm2tif.exe";
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, program, args, sourcePath, sourceSeq, targetSeq, 
                                  null, outFile, errFile); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8783747900751620513L;

  public static final String aDSMSource    = "DSMSource";
  public static final String aSampleMethod = "SampleMethod";
  public static final String aSampleDepth  = "SampleDepth";
  public static final String aShadowBias   = "ShadowBias";
  public static final String aMipmapLevel  = "MipmapLevel";
  public static final String aOutputValue  = "OutputValue";
  public static final String aPixelType    = "PixelType";
  public static final String aCompression  = "Compression";

}

