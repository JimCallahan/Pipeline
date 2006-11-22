// $Id: HfsMantraAction.java,v 1.2 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.plugin.v2_0_4;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*    H F S   R E N D E R   A C T I O N                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a sequence of images using standalone Mantra from IFD files. <P> 
 * 
 * The most commonly used command line options of mantra(1) are supported through parameters 
 * of this action.
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details about
 * mantra(1).<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Input Files <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the IFD files to be rendered. <BR>
 *   </DIV> <BR>
 * 
 *   Processors <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of processors to use. <BR> 
 *   </DIV> 
 * 
 *   Image Width <BR>
 *   <DIV style="margin-left: 40px;">
 *     The horizontal resolution of the output image in pixels. <BR>
 *   </DIV> <BR>
 *  
 *   Image Height <BR>
 *   <DIV style="margin-left: 40px;">
 *     The vertical resolution of the output image in pixels. <BR>
 *   </DIV> <BR>
 * 
 * 
 *   Output Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the type of files generated:<BR>
 *     <DIV style="margin-left: 40px;">
 *       Color Image - Renders standard image formats. <BR> 
 *       Z-Depth - Renders z-depth maps. <BR> 
 *       Average Z-Depth - Integer 16-bits per channel. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   Color Depth <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the bit-depth of pixels in the output image (color only):<BR>
 *     <DIV style="margin-left: 40px;">
 *       Natural - Use the natural bit depth of the target image format. <BR>
 *       8-Bit (byte) - Integer 8-bits per channel. <BR>
 *       16-Bit (short) - Integer 16-bits per channel. <BR>
 *       16-Bit (half) - Half precision floating point. <BR> 
 *       16-Bit (half) Rounded - Rounded half precision floating point.<BR>
 *       32-Bit (float) - Full precision floating point. <BR>
 *       32-Bit (float) Rounded - Rounded full precision floating point. <BR> 
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   Render Fields <BR>
 *   <DIV style="margin-left: 40px;">
 *     Render field mode:<BR>
 *     <DIV style="margin-left: 40px;">
 *       Full - Renders as full field. <BR> 
 *       Odd - Renders as odd field. <BR> 
 *       Even - Render as even field. <BR> 
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * 
 *   <I>Rendering Methods</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Rendering Mode <BR>
 *     <DIV style="margin-left: 40px;">
 *       The fundamental rendering technique: <BR> 
 *       <DIV style="margin-left: 40px;">
 *         MicroPolygon - Use micro-polygon rendering only. <BR>
 *         RayTraced - Use pure ray-tracing only. <BR>
 *         Mixed - Use mixed ray tracing and micro-polygon rendering. <BR>
 *       </DIV> <BR>
 *     </DIV> <BR>
 * 
 *     AntiAliasing <BR> 
 *     <DIV style="margin-left: 40px;">
 *       Whether to turn on anti-aliasing. <BR>
 *     </DIV> <BR>
 * 
 *     Motion Blur <BR>
 *     <DIV style="margin-left: 40px;">
 *       The technique used for motion blur: <BR> 
 *       <DIV style="margin-left: 40px;">
 *         None - No motion blur. <BR> 
 *         No RayTraced - Turn off ray-traced motion blur. <BR>
 *         All - Use all motion blur techniques. <BR>
 *       </DIV> <BR>
 *     </DIV> <BR>
 *     
 *     Depth Of Field <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to render depth-of-field. <BR>
 *     </DIV> <BR>
 * 
 *     Global Illumination <BR> 
 *     <DIV style="margin-left: 40px;">
 *       Whether to compute irradiance and occulsion. <BR> 
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   <I>Render Quality</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Shading Quality <BR>
 *     <DIV style="margin-left: 40px;">
 *       The shading quality multiplier.<BR> 
 *     </DIV> <BR>
 * 
 *     AntiAliasing Threshold <BR>
 *     <DIV style="margin-left: 40px;">
 *       The variance anti-aliasing threshold.<BR> 
 *     </DIV> <BR>
 * 
 *     Jitter Scale <BR>
 *     <DIV style="margin-left: 40px;">
 *       The jitter scale.<BR> 
 *     </DIV> <BR>
 * 
 *     Coving Method <BR>
 *     <DIV style="margin-left: 40px;">
 *       Methods for dealing with patch cracks:<BR> 
 *       <DIV style="margin-left: 40px;">
 *         None - No coving of patch cracks. <BR> 
 *         Default - Cove displaced and sub-division surfaces. <BR>
 *         All - Forced coving of all primitives. <BR>
 *       </DIV> <BR>
 *     </DIV> <BR>
 * 
 *     <I>MicroPolygon Rendering</I> <BR>
 *     <DIV style="margin-left: 40px;">
 *       MicroPolygon Cache Size <BR>
 *       <DIV style="margin-left: 40px;">
 *         The size of the micro-polygon cache.<BR> 
 *       </DIV> <BR>
 * 
 *       MicroPolygon Max Splits <BR>
 *       <DIV style="margin-left: 40px;">
 *         The maxmum number of micro-polygon splits.<BR> 
 *       </DIV> <BR>
 *     </DIV> <BR>
 * 
 *     <I>Ray Tracing</I> <BR>
 *     <DIV style="margin-left: 40px;">
 *       Ray Mesh Cache Size <BR>
 *       <DIV style="margin-left: 40px;">
 *         The size of the ray mesh cache.<BR> 
 *       </DIV> <BR>
 * 
 *       Ray Shading Rate <BR>
 *       <DIV style="margin-left: 40px;">
 *         Global ray-tracing level of detail factor.<BR> 
 *       </DIV> <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   <I>Messages</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Verbosity <BR>
 *     <DIV style="margin-left: 40px;">
 *       The level of verbosity of rendering statistics. <BR> 
 *     </DIV> <BR>
 * 
 *     Profiling <BR>
 *     <DIV style="margin-left: 40px;">
 *       The fundamental rendering technique: <BR> 
 *       <DIV style="margin-left: 40px;">
 *         None - No profiling. <BR>
 *         VEX - VEX profiling only. <BR>
 *         VEX and NaN - VEX profiling and NaN detection. <BR> 
 *       </DIV> <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 * </DIV>
 */
public
class HfsMantraAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsMantraAction() 
  {
    super("HfsMantra", new VersionID("2.0.4"), "Temerity",
	  "Renders a sequence of images using standalone Mantra from IFD files.");

    {
      ActionParam param = 
	new LinkActionParam
	("InputFiles",
	 "The source node which contains the IFD files to be rendered.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("Processors", 
	 "The number of processors to use.", 
	 1);
      addSingleParam(param);
    }

    
    /* image resolution */ 
    {
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("ImageWidth",
	   "The horizontal resolution of the output image in pixels.", 
	   null);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("ImageHeight",
	   "The vertical resolution of the output image in pixels.", 
	   null);
	addSingleParam(param);
      }

      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Default");
	choices.add("320x240");
	choices.add("640x480");
	choices.add("1k Square");
	choices.add("2k Square");
	choices.add("3k Square");
	choices.add("4k Square");
	choices.add("Full 1024");
	choices.add("Full 1280");
	choices.add("HD 720");
	choices.add("HD 1080");

	addPreset("ImageResolution", choices);

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       null);
	  values.put("ImageHeight",      null);
	
	  addPresetValues("ImageResolution", "Default", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       320);
	  values.put("ImageHeight",      240);
	
	  addPresetValues("ImageResolution", "320x240", values);
	}
      
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       640);
	  values.put("ImageHeight",      480);
	
	  addPresetValues("ImageResolution", "640x480", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1024);
	  values.put("ImageHeight",      1024);
	
	  addPresetValues("ImageResolution", "1k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       2048);
	  values.put("ImageHeight",      2048);
	
	  addPresetValues("ImageResolution", "2k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       3072);
	  values.put("ImageHeight",      3072);
	
	  addPresetValues("ImageResolution", "3k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       4096);
	  values.put("ImageHeight",      4096);
	
	  addPresetValues("ImageResolution", "4k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1024);
	  values.put("ImageHeight",      768);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "Full 1024", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1280);
	  values.put("ImageHeight",      1024);
	
	  addPresetValues("ImageResolution", "Full 1280", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1280);
	  values.put("ImageHeight",      720);
	
	  addPresetValues("ImageResolution", "HD 720", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1920);
	  values.put("ImageHeight",      1080);
	
	  addPresetValues("ImageResolution", "HD 1080", values);
	}
      }
    }

    /* output format */ 
    {
      ArrayList<String> format = new ArrayList<String>();
      format.add("Color Image");
      format.add("Z-Depth");
      format.add("Average Z-Depth");
      
      ActionParam param = 
	new EnumActionParam
	("OutputFormat",
	 "Specifies the type of files generated.",
	 "Color Image", format);
      addSingleParam(param);
    }

    /* color depth */ 
    {
      ArrayList<String> depth = new ArrayList<String>();
      depth.add("Natural");
      depth.add("8-Bit (byte)");
      depth.add("16-Bit (short)");
      depth.add("16-Bit (half)");
      depth.add("16-Bit (half) Rounded");
      depth.add("32-Bit (float)");
      depth.add("32-Bit (float) Rounded");
      
      ActionParam param = 
	new EnumActionParam
	("ColorDepth",
	 "Specifies the bit-depth of pixels in the output image (color only).", 
	 "Natural", depth);
      addSingleParam(param);
    }

    /* render fields */ 
    {
      ArrayList<String> fields = new ArrayList<String>();
      fields.add("Full");
      fields.add("Odd");
      fields.add("Even");
      
      ActionParam param = 
	new EnumActionParam
	("RenderFields", 
	 "Render field mode.",
	 "Full", fields);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("ExtraOptions",
	 "Additional command-line arguments.", 
	 null);
      addSingleParam(param);
    }

    /* render methods */ 
    {
      /* render mode */ 
      {
	ArrayList<String> modes = new ArrayList<String>();
	modes.add("MicroPolygon");
	modes.add("RayTraced");
	modes.add("Mixed");

	ActionParam param = 
	  new EnumActionParam
	  ("RenderMode", 
	   "The fundamental rendering technique.", 
	   "Mixed", modes);
	addSingleParam(param);
      }

      /* anti-aliasing */ 
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("AntiAliasing", 
	   "Whether to turn on anti-aliasing.",
	   true);
	addSingleParam(param);
      }

      /* motion blur */ 
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("None"); 
	choices.add("No RayTraced"); 
	choices.add("All"); 

	ActionParam param = 
	  new EnumActionParam
	  ("MotionBlur", 
	   "The technique used for motion blur.",
	   "All", choices);
	addSingleParam(param);
      }
      
      /* depth of field */ 
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("DepthOfField", 
	   "Whether to render depth-of-field.", 
	   true);
	addSingleParam(param);
      }

      /* global illumination */ 
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("GlobalIllumination", 
	   "Whether to compute irradiance and occulsion.", 
	   true);
	addSingleParam(param);
      }
    }

    /* render quality */ 
    {
      /* shading quality */ 
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("ShadingQuality",
	   "The shading quality multiplier.", 
	   1.0);
	addSingleParam(param);
      }
      
      /* anti-aliasing threshold */ 
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("AntiAliasingThreshold",
	   "The variance anti-aliasing threshold.",
	   null);
	addSingleParam(param);
      }

      /* jitter scale */ 
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("JitterScale",
	   "The jitter scale.", 
	   1.0);
	addSingleParam(param);
      }
      
      /* coving method */ 
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("None");
	choices.add("Default");
	choices.add("All");
	
	ActionParam param = 
	  new EnumActionParam
	  ("CovingMethod", 
	   "Methods for dealing with patch cracks.",
	   "Default", choices);
	addSingleParam(param);
      }
      
      /* micro-polygon rendering */ 
      {
	/* micro-polygon cache size */ 
	{
	  ActionParam param = 
	    new IntegerActionParam
	    ("MicroPolygonCacheSize",
	     "The size of the micro-polygon cache.", 
	     4096);
	  addSingleParam(param);
	}

	/* micro-polygon max splits */ 
	{
	  ActionParam param = 
	    new IntegerActionParam
	    ("MicroPolygonMaxSplits",
	     "The maxmum number of micro-polygon splits.",
	     10);
	  addSingleParam(param);
	}
      }

      /* micro-polygon rendering */ 
      {
	/* micro-polygon cache size */ 
	{
	  ActionParam param = 
	    new IntegerActionParam
	    ("RayMeshCacheSize",
	     "The size of the ray mesh cache.", 
	     1024);
	  addSingleParam(param);
	}

	/* micro-polygon max splits */ 
	{
	  ActionParam param = 
	    new DoubleActionParam
	    ("RayShadingRate",
	     "Global ray-tracing level of detail factor.", 
	     1.0);
	  addSingleParam(param);
	}
      }      
    }

    /* messages */ 
    {
      /* verbosity */ 
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("0 (None)");
	choices.add("1");
	choices.add("2");
	choices.add("3");
	choices.add("4");
	choices.add("5");
	choices.add("6");
	choices.add("7");
	choices.add("8");
	choices.add("9 (High)");
	
	ActionParam param = 
	  new EnumActionParam
	  ("Verbosity", 
	   "The level of verbosity of rendering statistics.", 
	   "0 (None)", choices);
	addSingleParam(param);
      }

      /* profiling */ 
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("None");
	choices.add("VEX");
	choices.add("VEX and NaN");
	
	ActionParam param = 
	  new EnumActionParam
	  ("Profiling", 
	   "Level of profiling of VEX shaders.",
	   "None", choices);
	addSingleParam(param);
      }
    }


    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("InputFiles");
      layout.addEntry("Processors");
      layout.addSeparator(); 
      layout.addEntry("ImageResolution");
      layout.addEntry("ImageWidth");
      layout.addEntry("ImageHeight");
      layout.addSeparator(); 
      layout.addEntry("OutputFormat");
      layout.addEntry("ColorDepth");
      layout.addEntry("RenderFields");  
      layout.addSeparator(); 
      layout.addEntry("ExtraOptions");

      {
	LayoutGroup rm = new LayoutGroup
	  ("RenderingMethods", "Rendering method controls.", false);
	
	rm.addEntry("RenderMode");
	rm.addEntry("AntiAliasing");
	rm.addEntry("MotionBlur");
	rm.addEntry("DepthOfField");
	rm.addEntry("GlobalIllumination");

	layout.addSubGroup(rm);
      }

      {
	LayoutGroup rq = new LayoutGroup
	  ("RenderingQuality", "Rendering quality controls.", false);
	
	rq.addEntry("ShadingQuality");
	rq.addEntry("AntiAliasingThreshold");
	rq.addEntry("JitterScale");
	rq.addEntry("CovingMethod");

	{
	  LayoutGroup micro = new LayoutGroup
	    ("MicroPolygonRendering", "Micro-polygon rendering quality controls.", true);
	  
	  micro.addEntry("MicroPolygonCacheSize");
	  micro.addEntry("MicroPolygonMaxSplits");

	  rq.addSubGroup(micro);
	}

	{
	  LayoutGroup ray = new LayoutGroup
	    ("RayTracing", "Ray-tracing quality controls.", true);

	  ray.addEntry("RayMeshCacheSize");
	  ray.addEntry("RayShadingRate");

	  rq.addSubGroup(ray);
	}

	layout.addSubGroup(rq);
      }

      {
	LayoutGroup msg = new LayoutGroup
	  ("Messages", "Controls for statistics and profiling output.", false);
	
	msg.addEntry("Verbosity");
	msg.addEntry("Profiling");

	layout.addSubGroup(msg);	
      }

      setSingleLayout(layout);
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
    NodeID nodeID = agenda.getNodeID();

    TreeSet<File> ifdFiles = new TreeSet<File>();
    {
      String sname = (String) getSingleParamValue("InputFiles"); 
      if(sname == null) 
	throw new PipelineException
	  ("No Input Files where specified!");
	
      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq == null) 
	throw new PipelineException
	  ("Somehow the Input Files node (" + sname + ") was not one of the source " + 
	   "nodes!");
      
      String suffix = fseq.getFilePattern().getSuffix();
      if((suffix == null) || !suffix.equals("ifd")) 
	throw new PipelineException
	  ("The HfsMantra Action requires that the source node specified by the " + 
	   "Input Files parameter (" + sname + ") must have IFD files (.ifd) as its " + 
	   "primary file sequence!");
      
      NodeID snodeID = new NodeID(nodeID, sname);
      for(File file : fseq.getFiles()) {
	ifdFiles.add(new File(PackageInfo.sProdDir,
			      snodeID.getWorkingParent() + "/" + file));
      }
    }

    String options = null;
    {
      StringBuilder buf = new StringBuilder();

      {
	Integer procs = (Integer) getSingleParamValue("Processors"); 
	if(procs != null) {
	  if(procs < 1)
	    throw new PipelineException
	      ("The number of processors (" + procs + ") must be positive!");
	  buf.append(" -n " + procs);
	}
      }

      {
	Integer width = (Integer) getSingleParamValue("ImageWidth"); 
	if(width != null) {
	  if(width < 1) 
	    throw new PipelineException
	      ("The image width (" + width + ") must be positive!");
	  buf.append(" -w " + width);
	}
      }

      {
	Integer height = (Integer) getSingleParamValue("ImageHeight"); 
	if(height != null) {
	  if(height < 1) 
	    throw new PipelineException
	      ("The image height (" + height + ") must be positive!");
	  buf.append(" -h " + height);
	}
      }

      {
	EnumActionParam param = (EnumActionParam) getSingleParam("OutputFormat");
	switch(param.getIndex()) {
	case 0:
	  break;

	case 1:
	  buf.append(" -Z");
	  break;

	case 2:
	  buf.append(" -z");
	  break;

	default:
	throw new PipelineException
	  ("Illegal OutputFormat value!");
	}
      }

      {
	EnumActionParam param = (EnumActionParam) getSingleParam("ColorDepth");
	switch(param.getIndex()) {
	case 0:
	  break;

	case 1:
	  buf.append(" -b byte");
	  break;

	case 2:
	  buf.append(" -b short");
	  break;

	case 3:
	  buf.append(" -b half");
	  break;

	case 4:
	  buf.append(" -b HALF");
	  break;

	case 5:
	  buf.append(" -b float");
	  break;

	case 6:
	  buf.append(" -b FLOAT");
	  break;

	default:
	throw new PipelineException
	  ("Illegal ColorDepth value!");
	}
      }

      {
	EnumActionParam param = (EnumActionParam) getSingleParam("RenderFields");
	switch(param.getIndex()) {
	case 0:
	  break;
	  
	case 1:
	  buf.append(" -O");
	  break;
	  
	case 2:
	  buf.append(" -E");
	  break;

	default:
	  throw new PipelineException
	    ("Illegal RenderFields value!");
	}
      }

      {
	ArrayList<String> qopts = new ArrayList<String>();
	
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("RenderMode");
	  switch(param.getIndex()) {
	  case 0: 
	    qopts.add("r");
	    break;
	    
	  case 1: 
	    buf.append(" -r");
	    break;
	    
	  case 2: 
	    break;
	    
	  default:
	    throw new PipelineException
	      ("Illegal RenderMode value!");
	  }
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("AntiAliasing"); 
	  if((tf != null) && !tf) 
	     buf.append(" -A");
	}
	
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("MotionBlur");
	  switch(param.getIndex()) {
	  case 0: 
	    qopts.add("b");
	    break;
	    
	  case 1: 
	    qopts.add("B");
	    break;
	    
	  case 2: 
	    break;
	    
	  default:
	    throw new PipelineException
	      ("Illegal MotionBlur value!");
	  }
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("DepthOfField"); 
	  if((tf != null) && !tf) 
	    qopts.add("d");
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("GlobalIllumination"); 
	  if((tf != null) && !tf) 
	    qopts.add("i");
	}

	if(!qopts.isEmpty()) {
	  buf.append(" -Q ");
	  for(String opt : qopts) 
	    buf.append(opt);
	}
      }
      
      {
	Double factor = (Double) getSingleParamValue("ShadingQuality"); 
	if(factor != null) 
	  buf.append(" -s " + factor);
      }

      {
	Double threshold = (Double) getSingleParamValue("AntiAliasingThreshold"); 
	if(threshold != null) 
	  buf.append(" -v " + threshold);
      }
      
      {
	Double scale = (Double) getSingleParamValue("JitterScale"); 
	if(scale != null) 
	  buf.append(" -J " + scale);
      }
      
      {
	EnumActionParam param = (EnumActionParam) getSingleParam("CovingMethod");
	switch(param.getIndex()) {
	case 0: 
	case 1: 
	case 2: 
	  buf.append(" -c " + param.getIndex());
	  break;
	  
	default:
	  throw new PipelineException
	    ("Illegal CovingMethod value!");
	}
      }
      
      {
	Integer size = (Integer) getSingleParamValue("MicroPolygonCacheSize"); 
	if(size != null) 
	  buf.append(" -G " + size);
      }
      
      {
	Integer splits = (Integer) getSingleParamValue("MicroPolygonMaxSplits"); 
	if(splits != null) 
	  buf.append(" -S " + splits);
      }
      
      {
	Integer size = (Integer) getSingleParamValue("RayMeshCacheSize"); 
	if(size != null) 
	  buf.append(" -M " + size);
      }

      {
	Double rate = (Double) getSingleParamValue("RayShadingRate"); 
	if(rate != null) 
	  buf.append(" -L " + rate);
      }

      {
	EnumActionParam param = (EnumActionParam) getSingleParam("Verbosity");
	buf.append(" -V " + param.getIndex());
      }
      
      {
	EnumActionParam param = (EnumActionParam) getSingleParam("Profiling");
      	switch(param.getIndex()) {
	case 0: 
	  break;

	case 1: 
	  buf.append("p");
	  break;
	  
	case 2: 
	  buf.append("P");
	  break;
	  
	default:
	  throw new PipelineException
	    ("Illegal CovingMethod value!");
	}
      }

      {
	String extra = (String) getSingleParamValue("ExtraOptions");
	if(extra != null) 
	  buf.append(" " + extra);
      }

      options = buf.toString();
    }

    /* create the wrapper shell script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n");
      
      for(File file : ifdFiles) 
	out.write("mantra" + options + " -f " + file + "\n");      

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    try {
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.getPath(), new ArrayList<String>(), 
	 agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile); 
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7644898711969430929L;

}

