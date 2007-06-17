// $Id: DLTextureAction.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.DLTextureAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   T E X T U R E   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates optimized 3Delight textures and environment maps from source images. <P> 
 * 
 * Converts the images which make up the primary file sequence of one of the source
 * nodes into the texture maps which make up the primary file sequence of this node. <P>
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
 * 
 *   
 *   Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The format of generated texture map files.
 *     <UL>
 *       <LI> Texture  
 *       <LI> EnvMap   (latitude/longitude environment map)
 *     </UL>
 *   </DIV> <BR>
 * 
 *   S Mode <BR>
 *   T Mode <BR>
 *   <DIV style="margin-left: 40px;">
 *     The method used to lookup texels outside the [0,1] S/T coordinate range: 
 *     <UL>
 *       <LI>Black - Use black for all values outside [0,1] range.
 *       <LI>Clamp - Use border texel color for values outside [0,1] range.
 *       <LI>Periodic - Tiles texture outside [0,1] range.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Flip <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether and how to flip the output texture maps:
 *     <UL>
 *       <LI> None - No flipping is performed.
 *       <LI> S-Only - Flip image in the S direction.
 *       <LI> T-Only - Flip image in the T direction.
 *       <LI> Both - Flip in both the S and T directions. 
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output texture maps.<BR>
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
class DLTextureAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DLTextureAction() 
  {
    super("DLTexture", new VersionID("2.2.1"), "Temerity", 
	  "Generates optimized 3Delight textures and environment maps from source images.");
    
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
      choices.add("Texture");
      choices.add("EnvMap");

      ActionParam param = 
        new EnumActionParam
        (aFormat, 
         "The format of generated texture map files.", 
         "Texture", choices);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Black");
      choices.add("Clamp");
      choices.add("Periodic");

      {
	ActionParam param = 
	  new EnumActionParam
	  (aSMode, 
	   "The method used to lookup texels outside the [0,1] S-coordinate range.",
	   "Black", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new EnumActionParam
	  (aTMode, 
	   "The method used to lookup texels outside the [0,1] T-coordinate range.",
	   "Black", choices);
	addSingleParam(param);
      }
	
      addPreset(aMode, choices);
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aSMode, "Black");
	values.put(aTMode, "Black");
	
	addPresetValues(aMode, "Black", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aSMode, "Clamp");
	values.put(aTMode, "Clamp");
	
	addPresetValues(aMode, "Clamp", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aSMode, "Periodic");
	values.put(aTMode, "Periodic");
	
	addPresetValues(aMode, "Periodic", values);
      }
    }   
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("S-Only");
      choices.add("T-Only");
      choices.add("Both"); 

      ActionParam param = 
	new EnumActionParam
	(aFlip, 
	 "Whether and how to flip the output texture maps.",
	 "None", choices);
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

      {
	LayoutGroup output = new LayoutGroup
	  ("Output", "Texture output and interpretation controls.", true);
        output.addEntry(aFormat);
	output.addSeparator();
	output.addEntry(aMode);
	output.addEntry(aSMode);
	output.addEntry(aTMode);
	output.addSeparator();
	output.addEntry(aFlip);
	output.addEntry(aCompression);
	
	layout.addSubGroup(output);	
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
    FileSeq targetSeq = null;
    {
      {    
	String sname = getSingleStringParamValue(aImageSource); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
	
	sourceSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("tdl"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain 3Delight " + 
	     "textures (.tdl)!");
	
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
      switch(getSingleEnumParamIndex(aFormat)) {
      case 1:
        args.add("-envlatl");
      }

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

      {
        args.add("-smode");
        switch(getSingleEnumParamIndex(aSMode)) {
        case 0:
          args.add("black");
          break;
          
        case 1:
          args.add("clamp");
          break;
          
        case 2:
          args.add("periodic");
          break;
          
        default:
          throw new PipelineException
            ("Illegal S Mode value!");
        }
      }
    
      {
        args.add("-tmode");
        switch(getSingleEnumParamIndex(aTMode)) {
        case 0:
          args.add("black");
          break;
          
        case 1:
          args.add("clamp");
          break;
          
        case 2:
          args.add("periodic");
          break;
          
        default:
          throw new PipelineException
            ("Illegal T Mode value!");
        }
      }
    
      switch(getSingleEnumParamIndex(aFlip)) {
      case 0:
	break;
        
      case 1:
	args.add("-flips");
	break;
        
      case 2:
	args.add("-flipt");
	break;
	
      case 3:
	args.add("-flipst");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Flip value!");
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
    }

    /* texture conversion program */ 
    String program = "tdlmake";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "tdlmake.exe";
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, program, args, sourcePath, sourceSeq, targetSeq, 
                                  null, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3766897490253134992L;

  public static final String aImageSource  = "ImageSource";
  public static final String aQuality      = "Quality";
  public static final String aFilter       = "Filter";
  public static final String aFilterWindow = "FilterWindow";
  public static final String aSFilterWidth = "SFilterWidth";
  public static final String aTFilterWidth = "TFilterWidth";
  public static final String aBlurFactor   = "BlurFactor";
  public static final String aFormat       = "Format"; 
  public static final String aSMode        = "SMode";
  public static final String aTMode        = "TMode";
  public static final String aMode         = "Mode";
  public static final String aFlip         = "Flip";
  public static final String aCompression  = "Compression";

}

