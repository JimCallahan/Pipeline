// $Id: DLEnvCubeAction.java,v 1.2 2007/04/12 15:21:55 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   E N V   C U B E   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generate an optimized 3Delight cube faced environment map from six directional source 
 * images.<P> 
 * 
 * Converts the six images [+x, -x, +y, -y, +z, -z] which make up the primary file 
 * sequence of one of the source nodes into the single cubic environment map which is the 
 * single member of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for 
 * <A href="http://www.3delight.com/ZDoc/3delight_12.html"><B>tdlmake</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images files to convert. <BR> 
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output texture maps.<BR>
 *   </DIV> <BR>
 * 
 * 
 *   Quality <BR>
 *   <DIV style="margin-left: 40px;">
 *     The strategy for selecting the source image when downsampling mipmap levels:
 *     <UL>
 *       <LI> Low - Use previous mipmap level.
 *       <LI> Medium - Use 2nd previous mipmap level.
 *       <LI> High - Use 4th previous mipmap level.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Filter <BR>
 *   <DIV style="margin-left: 40px;">
 *     The filter used to downsample the source images to generate the mipmap levels.
 *     <UL>
 *       <LI> Box 
 *       <LI> Triangle 
 *       <LI> Gaussian  
 *       <LI> Catmul-Rom 
 *       <LI> Bessel 
 *       <LI> Sinc 
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Filter Window <BR>
 *   <DIV style="margin-left: 40px;">
 *     Windowing function used to soften boundry of Bessel or Sinc filters. 
 *     Ignored for all other filters.
 *     <UL>
 *       <LI> Lanczos
 *       <LI> Hamming
 *       <LI> Hann
 *       <LI> Blackman
 *       <LI> None
 *     </UL>
 *   </DIV> <BR>
 * 
 *   S Filter Width <BR>
 *   T Filter Width <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the width (diameter) of the downsizing filter in the S/T directions.
 *   </DIV> <BR>
 * 
 *   Blur Factor <BR>
 *   <DIV style="margin-left: 40px;">
 *     Scale factor applied to the filter function.
 *   </DIV> <BR>
 * </DIV><P> 
 * 
 * By default, the "python" program is used by this action to run the "tdlmake" commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class DLEnvCubeAction
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DLEnvCubeAction() 
  {
    super("DLEnvCube", new VersionID("2.2.1"), "Temerity", 
	  "Generates an optimized 3Delight cube faced environment map from six " + 
          "directional source images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The source node which contains the image files to convert.",
	 null);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Low");
      choices.add("Medium");
      choices.add("High");

      ActionParam param = 
	new EnumActionParam
	(aQuality, 
	 "The strategy for selecting the source image when downsampling mipmap levels.",
	 "Medium", choices);
      addSingleParam(param);
    }       

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Box");
      choices.add("Triangle");
      choices.add("Gaussian");
      choices.add("Catmul-Rom");
      choices.add("Bessel");
      choices.add("Sinc");

      ActionParam param = 
	new EnumActionParam
	(aFilter, 
	 "The filter used to downsample the source images to generate the mipmap levels.",
	 "Sinc", choices);
      addSingleParam(param);
    }   

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Lanczos");
      choices.add("Hamming");
      choices.add("Hann");
      choices.add("Blackman");
      choices.add("None");

      ActionParam param = 
	new EnumActionParam
	(aFilterWindow, 
	 "Windowing function used to soften boundry of Bessel or Sinc filters.<BR>" +
	 "Ignored for all other filters.",
	 "Lanczos", choices);
      addSingleParam(param);
    }   
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aSFilterWidth, 
	 "Overrides the width (diameter) of the downsizing filter in the S direction.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aTFilterWidth, 
	 "Overrides the width (diameter) of the downsizing filter in the T direction.", 
	 null);
      addSingleParam(param);
    }
     
    {
      ActionParam param = 
	new DoubleActionParam
	(aBlurFactor, 
	 "Scale factor applied to the filter function.", 
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
      layout.addEntry(aImageSource);
      layout.addSeparator();      
      layout.addEntry(aCompression);

      {
	LayoutGroup filter = new LayoutGroup
	  ("Filtering", "Mipmap level generation filtering controls.", true);
	filter.addEntry(aQuality);
	filter.addSeparator();      
	filter.addEntry(aFilter);
	filter.addEntry(aFilterWindow);
	filter.addEntry(aBlurFactor);
	filter.addSeparator();      
	filter.addEntry(aSFilterWidth);
	filter.addEntry(aTFilterWidth);

	layout.addSubGroup(filter);
      }

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
    Path targetPath = null;
    {
      {    
	String sname = (String) getSingleParamValue("ImageSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Image Source (" + sname + ") was not one of the source nodes!");
	
	if(fseq.numFrames() != 6)
	  throw new PipelineException
	    ("The source file sequence (" + fseq + ") must contain exactly six " + 
	     "[+x, -x, +y, -y, +z, -z] images!");

	sourceSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("tdl"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a 3Delight " + 
	     "environment map (.tdl)!");
	
	if(fseq.numFrames() != 1)
	  throw new PipelineException
	    ("The target file sequence (" + fseq + ") must be a single environment map!");
	
	targetPath = new Path(agenda.getTargetPath(), fseq.getPath(0));
      }
    }

    /* build command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    {
      args.add("-envcube");
    
      args.add("-quality");
      switch(getSingleEnumParamIndex(aQuality)) {
      case 0:
	args.add("low");
	break;

      case 1:
	args.add("medium");
	break;

      case 2:
	args.add("high");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Quality value!");	
      }

      boolean windowedFilter = false;
      {
        args.add("-filter");
        switch(getSingleEnumParamIndex(aFilter)) {
        case 0:
          args.add("box");
          break;
          
        case 1:
          args.add("triangle");
          break;
          
        case 2:
          args.add("gaussian");
          break;
          
        case 3:
          args.add("catmull-rom");
          break;
          
        case 4:
          args.add("bessel");
          windowedFilter = true;
          break;
          
        case 5:
          args.add("sinc");
          windowedFilter = true;
          break;
          
        default:
          throw new PipelineException
            ("Illegal Filter value!");	
        }
      }

      if(windowedFilter) {
        switch(getSingleEnumParamIndex(aFilterWindow)) {
        case 0:
          args.add("-window");
          args.add("lanczos");
          break;
          
        case 1:
          args.add("-window");
          args.add("hamming");
          break;
          
        case 2:
          args.add("-window");
          args.add("hann");
          break;
	
        case 3:
          args.add("-window");
          args.add("blackman");
          break;
          
        case 5:
          break;	
          
        default:
          throw new PipelineException
            ("Illegal Quality value!");	
        }
      }
    
      Double swidth = (Double) getSingleParamValue(aSFilterWidth);
      if(swidth != null) {
        if(swidth < 0.0) 
          throw new PipelineException
            ("The S Filter Width (" + swidth + ") cannot be negative!");
        
        args.add("-swidth");
        args.add(swidth.toString());
      }

      Double twidth = (Double) getSingleParamValue(aTFilterWidth);
      if(twidth != null) {
        if(twidth < 0.0) 
          throw new PipelineException
            ("The T Filter Width (" + twidth + ") cannot be negative!");
        
        args.add("-twidth");
        args.add(twidth.toString());
      }
      
      Double blur = (Double) getSingleParamValue(aBlurFactor);
      if(blur != null) {
        if(blur < 0.0) 
          throw new PipelineException
            ("The Blur Factor (" + blur + ") cannot be negative!");
        
        args.add("-blur");
        args.add(blur.toString());
      }

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
      
      for(Path path : sourceSeq.getPaths()) {
        Path spath = new Path(sourcePath, path);
	args.add(spath.toOsString()); 
      }

      args.add(targetPath.toOsString());
    }

    /* texture conversion program */ 
    String program = "tdlmake";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "tdlmake.exe";
    
    /* create the process to run the action */ 
    return createSubProcess(agenda, program, args, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6487863347098012249L;

  public static final String aImageSource  = "ImageSource";
  public static final String aQuality      = "Quality";
  public static final String aFilter       = "Filter";
  public static final String aFilterWindow = "FilterWindow";
  public static final String aSFilterWidth = "SFilterWidth";
  public static final String aTFilterWidth = "TFilterWidth";
  public static final String aBlurFactor   = "BlurFactor";
  public static final String aCompression  = "Compression";

}

