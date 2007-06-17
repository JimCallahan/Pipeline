// $Id: MaxwellGlobalsAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MaxwellGlobalsAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A X W E L L   G L O B A L S   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a MEL script which when executed by Maya will set many of the most useful global 
 * rendering parameters related to the Maxwell renderer in the Maya scene. <P> 
 * 
 * This generated MEL script can then be used as the PreRenderMEL script of a 
 * {@link us.temerity.pipeline.plugin.v2_0_0.MayaMxsExportAction MayaMxsExport} action to allow 
 * control over Maxwell rendering parameters directly from Pipeline without the need to 
 * reopen the Maya scene. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
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
 *   Pixel Aspect Ratio <BR>
 *   <DIV style="margin-left: 40px;">
 *     Ratio of pixel height to pixel width. <BR>
 *   </DIV> <BR> <BR>
 *   
 *   <I>Render Output Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Output RGB <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output a RGB color image.
 *     </DIV> <BR>
 * 
 *     Output Alpha <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output an Alpha mask image.
 *     </DIV> <BR>
 * 
 *     Output Depth <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output an Z Depth image.
 *     </DIV> <BR>
 * 
 *     Depth Min<BR>
 *     <DIV style="margin-left: 40px;">
 *       The minimum depth value.   ???
 *     </DIV> <BR>
 * 
 *     Depth Max<BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum depth value.   ???
 *     </DIV> <BR>
 *     <BR>
 * 
 *     Output Camera Cosine <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output an image containing the cosine of the angle between surface normals
 *       and the view direction.
 *     </DIV> <BR>
 * 
 *     Output MXI <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output an MXI file. 
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   <I>Render Quality Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Render Time <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum number of minutes to render.<BR> 
 *     </DIV> <BR>
 *   
 *     Sample Level <BR> 
 *     <DIV style="margin-left: 40px;">
 *       The maxmimum sampling level.<BR> 
 *     </DIV> <BR>
 *   
 *     Motion Blur Subframes<BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of motion blur subframes.<BR> 
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Illumination Layers</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Direct Lighting <BR>
 *     <DIV style="margin-left: 40px;">
 *       Calculates light rays from emitters. <BR>
 *     </DIV> <BR>
 *   
 *     Indirect Lighting <BR>
 *     <DIV style="margin-left: 40px;">
 *       Calculates bounced light rays from the surfaces. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   <I>Caustic Layers</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Direct Reflection <BR>
 *     <DIV style="margin-left: 40px;">
 *       Calculates direct reflected caustics. <BR>
 *     </DIV> <BR>
 * 
 *     Indirect Reflection <BR>
 *     <DIV style="margin-left: 40px;">
 *       Calculates bounced reflected caustics. <BR>
 *     </DIV> <BR>
 * 
 *     Direct Refraction <BR>
 *     <DIV style="margin-left: 40px;">
 *       Calculates direct refracted caustics. <BR>
 *     </DIV> <BR>
 * 
 *     Indirect Refraction <BR>
 *     <DIV style="margin-left: 40px;">
 *       Calculates bounced refracted caustics. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   <I>Tone</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Burn<BR>
 *     <DIV style="margin-left: 40px;">
 *       Controls the white saturation of the burnt colors.<BR>
 *     </DIV> <BR>
 * 
 *     Monitor Gamma<BR>
 *     <DIV style="margin-left: 40px;">
 *       The monitor gamma correction factor.<BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * See the Maxwell documentation for more details about the meaning of these parameters. 
 */
public
class MaxwellGlobalsAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MaxwellGlobalsAction() 
  {
    super("MaxwellGlobals", new VersionID("2.0.0"), "Temerity",
	  "Creates a MEL script which sets the Maxwell related render globals of a Maya scene.");

    /* image resolution */ 
    {
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("ImageWidth",
	   "The horizontal resolution of the output image in pixels.", 
	   640);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("ImageHeight",
	   "The vertical resolution of the output image in pixels.", 
	   480);
	addSingleParam(param);
      }
    
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("PixelAspectRatio",
	   "Ratio of pixel height to pixel width.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("320x240");
	choices.add("640x480");
	choices.add("1k Square");
	choices.add("2k Square");
	choices.add("3k Square");
	choices.add("4k Square");
	choices.add("CCIR PAL/Quantel PAL");
	choices.add("CCIR 601/Quantel NTSC");
	choices.add("Full 1024");
	choices.add("Full 1280/Screen");
	choices.add("HD 720");
	choices.add("HD 1080");
	choices.add("NTSC 4d");
	choices.add("PAL 768");
	choices.add("PAL 780");
	choices.add("Targa 486");
	choices.add("Target NTSC");
	choices.add("Targa PAL");
	choices.add("Letter");
	choices.add("Legal");
	choices.add("Tabloid");
	choices.add("A4");
	choices.add("A3");
	choices.add("B5");
	choices.add("B4");
	choices.add("B3");
	choices.add("2x3 Inches");
	choices.add("4x6 Inches");
	choices.add("5x7 Inches");
	choices.add("8x10 Inches");

	addPreset("ImageResolution", choices);

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       320);
	  values.put("ImageHeight",      240);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "320x240", values);
	}
      
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       640);
	  values.put("ImageHeight",      480);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "640x480", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1024);
	  values.put("ImageHeight",      1024);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "1k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       2048);
	  values.put("ImageHeight",      2048);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "2k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       3072);
	  values.put("ImageHeight",      3072);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "3k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       4096);
	  values.put("ImageHeight",      4096);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "4k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       720);
	  values.put("ImageHeight",      576);
	  values.put("PixelAspectRatio", 1.066);
	
	  addPresetValues("ImageResolution", "CCIR PAL/Quantel PAL", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       720);
	  values.put("ImageHeight",      486);
	  values.put("PixelAspectRatio", 0.900);
	
	  addPresetValues("ImageResolution", "CCIR 601/Quantel NTSC", values);
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
	  values.put("PixelAspectRatio", 1.066);
	
	  addPresetValues("ImageResolution", "Full 1280/Screen", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1280);
	  values.put("ImageHeight",      720);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "HD 720", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1920);
	  values.put("ImageHeight",      1080);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "HD 1080", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       646);
	  values.put("ImageHeight",      485);
	  values.put("PixelAspectRatio", 1.001);
	
	  addPresetValues("ImageResolution", "NTSC 4d", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       768);
	  values.put("ImageHeight",      576);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "PAL 768", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       780);
	  values.put("ImageHeight",      576);
	  values.put("PixelAspectRatio", 0.984);
	
	  addPresetValues("ImageResolution", "PAL 780", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       512);
	  values.put("ImageHeight",      486);
	  values.put("PixelAspectRatio", 1.265);
	
	  addPresetValues("ImageResolution", "Targa 486", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       512);
	  values.put("ImageHeight",      482);
	  values.put("PixelAspectRatio", 1.255);
	
	  addPresetValues("ImageResolution", "Target NTSC", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       512);
	  values.put("ImageHeight",      576);
	  values.put("PixelAspectRatio", 1.500);
	
	  addPresetValues("ImageResolution", "Targa PAL", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       2550);
	  values.put("ImageHeight",      3300);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "Letter", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       2550);
	  values.put("ImageHeight",      4200);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "Legal", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       5100);
	  values.put("ImageHeight",      3300);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "Tabloid", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       2480);
	  values.put("ImageHeight",      3508);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "A4", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       3507);
	  values.put("ImageHeight",      4962);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "A3", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       2079);
	  values.put("ImageHeight",      2952);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "B5", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       2952);
	  values.put("ImageHeight",      4170);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "B4", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       4170);
	  values.put("ImageHeight",      5907);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "B3", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       600);
	  values.put("ImageHeight",      900);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "2x3 Inches", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1200);
	  values.put("ImageHeight",      1800);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "4x6 Inches", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       1500);
	  values.put("ImageHeight",      2100);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "5x7 Inches", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("ImageWidth",       2400);
	  values.put("ImageHeight",      3000);
	  values.put("PixelAspectRatio", 1.0);
	
	  addPresetValues("ImageResolution", "8x10 Inches", values);
	}
      }
    }

    /* render output */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputRGB",
	   "Whether to output a RGB color image.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputAlpha",
	   "Whether to output an Alpha mask image.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputDepth",
	   "Whether to output an Z Depth image.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("DepthMin",
	   "The minimum depth value.",
	   -1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("DepthMax",
	   "The maximum depth value.",
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputCameraCosine",
	   "Whether to output an image containing the cosine of the angle between surface " + 
	   "normals and the view direction.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputMXI",
	   "Whether to output an MXI file.",
	   false);
	addSingleParam(param);
      }
    }

    /* render quality */ 
    {
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("RenderTime",
	   "The maximum number of minutes to render.", 
	   10);
	addSingleParam(param);
      }

      {      
	ActionParam param = 
	  new DoubleActionParam
	  ("SampleLevel",
	   "The maxmimum sampling level.",
	   20.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("MotionBlurSubframes",
	   "The number of motion blur subframes.", 
	   1);
	addSingleParam(param);
      }
    }

    /* illumination layers */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("DirectLighting",
	   "Calculates light rays from emitters.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("IndirectLighting",
	   "Calculates bounced light rays from the surfaces.",
	   true);
	addSingleParam(param);
      }
    }

    /* caustic layers */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("DirectReflection",
	   "Calculates direct reflected caustics.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("IndirectReflection",
	   "Calculates bounced reflected caustics.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("DirectRefraction",
	   "Calculates direct refracted caustics.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("IndirectRefraction",
	   "Calculates bounced refracted caustics.",
	   false);
	addSingleParam(param);
      }      
    }

    /* tone */ 
    {
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("Burn",
	   "Controls the white saturation of the burnt colors.",
	   0.8);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("MonitorGamma",
	   "The monitor gamma correction factor.", 
	   2.2);
	addSingleParam(param);
      }
    }

    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ImageResolution");
      layout.addEntry("ImageWidth");
      layout.addEntry("ImageHeight");
      layout.addSeparator(); 
      layout.addEntry("PixelAspectRatio");
      
      {
	LayoutGroup ro = new LayoutGroup
	  ("RenderOutput", "Controls image file output.", false);
	ro.addEntry("OutputRGB");
	ro.addEntry("OutputAlpha");
	ro.addSeparator(); 
	ro.addEntry("OutputDepth");
	ro.addEntry("DepthMin");
	ro.addEntry("DepthMax");
	ro.addSeparator(); 
	ro.addEntry("OutputCameraCosine");
	ro.addEntry("OutputMXI");

	layout.addSubGroup(ro);
      }

      {
	LayoutGroup rq = new LayoutGroup
	  ("RenderQuality", "Overall quality controls.", false);
	rq.addEntry("RenderTime");
	rq.addEntry("SampleLevel");
	rq.addSeparator(); 
	rq.addEntry("MotionBlurSubframes");

	layout.addSubGroup(rq);
      }

      {
	LayoutGroup il = new LayoutGroup
	  ("IlluminationLayers", "Controls lighting calculations.", false);
	il.addEntry("DirectLighting");
	il.addEntry("IndirectLighting");

	layout.addSubGroup(il);
      }

      {
	LayoutGroup cl = new LayoutGroup
	  ("CausticLayers", "Controls caustic calculations.", false);
	cl.addEntry("DirectReflection");
	cl.addEntry("IndirectReflection");
	cl.addSeparator(); 
	cl.addEntry("DirectRefraction");
	cl.addEntry("IndirectRefraction");

	layout.addSubGroup(cl);
      }

      {
	LayoutGroup tone = new LayoutGroup
	  ("Tone", "Controls irradiance to RGB conversion.", false);
	tone.addEntry("Burn");
	tone.addEntry("MonitorGamma");

	layout.addSubGroup(tone);
      }

      setSingleLayout(layout);
    }

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
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  { 
    /* sanity checks */ 
    NodeID nodeID = agenda.getNodeID();
    FileSeq fseq = agenda.getPrimaryTarget();
    if(!fseq.isSingle() || !fseq.getFilePattern().getSuffix().equals("mel"))
      throw new PipelineException
	("The MaxwellGlobals Action requires that primary target file sequence must " + 
	 "be a single MEL script!"); 

    /* create a temporary shell script */ 
    File script = createTemp(agenda, 0644, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      
      File target = new File(PackageInfo.sProdDir, 
			     nodeID.getWorkingParent() + "/" + fseq.getFile(0));

      out.write("cat > " + target + " <<EOF\n" +
		"print \"Applying Render Globals: " + target + "\\n\";\n\n");

      /* image resolution */ 
      {
	Integer width = (Integer) getSingleParamValue("ImageWidth"); 
	if((width == null) || (width <= 0)) 
	  throw new PipelineException
	    ("The value of ImageWidth (" + width + ") was illegal!");

	Integer height = (Integer) getSingleParamValue("ImageHeight"); 
	if((height == null) || (height <= 0)) 
	  throw new PipelineException
	    ("The value of ImageHeight (" + height + ") was illegal!");

	Double ratio = (Double) getSingleParamValue("PixelAspectRatio");
	if((ratio == null) || (ratio <= 0.0)) 
	  throw new PipelineException
	    ("The value of PixelAspectRatio (" + ratio + ") was illegal!");

	double deviceRatio = (width.doubleValue() / height.doubleValue()) * ratio;

	out.write
	  ("// IMAGE RESOLUTION\n" + 
	   "setAttr \"maxwellGlobals.width\" " + width + ";\n" + 
	   "setAttr \"maxwellGlobals.height\" " + height + ";\n" + 
	   "setAttr \"maxwellGlobals.deviceAspectRatio\" " + deviceRatio + ";\n\n");
      }

      /* render output */ 
      {
	Boolean rgb = (Boolean) getSingleParamValue("OutputRGB");  
	if(rgb == null) 
	  throw new PipelineException
	    ("The OutputRGB was (null)!");

	Boolean alpha = (Boolean) getSingleParamValue("OutputAlpha");  
	if(alpha == null) 
	  throw new PipelineException
	    ("The OutputAlpha was (null)!");

	Boolean depth = (Boolean) getSingleParamValue("OutputDepth");  
	if(depth == null) 
	  throw new PipelineException
	    ("The OutputDepth was (null)!");

	Double dmin = (Double) getSingleParamValue("DepthMin");
	if(dmin == null)
	  throw new PipelineException
	    ("The value of DepthMin (" + dmin + ") was illegal!");

	Double dmax = (Double) getSingleParamValue("DepthMax");
	if(dmax == null)
	  throw new PipelineException
	    ("The value of DepthMax (" + dmax + ") was illegal!");

	if(dmin > dmax) 
	  throw new PipelineException
	    ("The value of DepthMin (" + dmin + ") must be less-than DepthMax (" + dmax + ")!");

	Boolean cosine = (Boolean) getSingleParamValue("OutputCameraCosine");  
	if(cosine == null) 
	  throw new PipelineException
	    ("The OutputCameraCosine was (null)!");

	Boolean mxi = (Boolean) getSingleParamValue("OutputMXI");  
	if(mxi == null) 
	  throw new PipelineException
	    ("The OutputMXI was (null)!");

	out.write
	  ("// RENDER OUTPUT\n" + 
	   "setAttr \"maxwellGlobals.renderChannels\" " + rgb + ";\n" + 
	   "setAttr \"maxwellGlobals.alphaChannel\" " + alpha + ";\n" + 
	   "setAttr \"maxwellGlobals.zChannel\" " + depth + ";\n" + 
	   "setAttr \"maxwellGlobals.zChannelMin\" " + dmin + ";\n" + 
	   "setAttr \"maxwellGlobals.zChannelMax\" " + dmax + ";\n" + 
	   "setAttr \"maxwellGlobals.normalChannel\" " + cosine + ";\n" +
	   "setAttr \"maxwellGlobals.mxiWrite\" " + mxi + ";\n\n");
      }

      /* render quality */ 
      {
	Integer time = (Integer) getSingleParamValue("RenderTime"); 
	if((time == null) || (time <= 0)) 
	  throw new PipelineException
	    ("The value of RenderTime (" + time + ") was illegal!");

	Double samples = (Double) getSingleParamValue("SampleLevel");
	if((samples == null) || (samples <= 0.0)) 
	  throw new PipelineException
	    ("The value of SampleLevel (" + samples + ") was illegal!");

	Integer steps = (Integer) getSingleParamValue("MotionBlurSubframes"); 
	if((steps == null) || (steps < 1)) 
	  throw new PipelineException
	    ("The value of MotionBlurSubframes (" + steps + ") was illegal!");
	
	out.write
	  ("// RENDER QUALITY\n" + 
	   "setAttr \"maxwellGlobals.renderTime\" " + time + ";\n" + 
	   "setAttr \"maxwellGlobals.samplingLevel\" " + samples + ";\n" + 
	   "setAttr \"maxwellGlobals.defaultMotionBlur\" " + steps + ";\n\n");
      }

      /* illumination layers */ 
      {
	Boolean direct = (Boolean) getSingleParamValue("DirectLighting");  
	if(direct == null) 
	  throw new PipelineException
	    ("The DirectLighting was (null)!");
	
	Boolean indirect = (Boolean) getSingleParamValue("IndirectLighting");  
	if(indirect == null) 
	  throw new PipelineException
	    ("The IndirectLighting was (null)!");
	
	out.write
	  ("// ILLUMINATION LAYERS\n" + 
	   "setAttr \"maxwellGlobals.layerDirect\" " + direct + ";\n" + 
	   "setAttr \"maxwellGlobals.layerIndirect\" " + indirect + ";\n\n");
      }
      
      /* illumination layers */ 
      {
	Boolean direct = (Boolean) getSingleParamValue("DirectLighting");  
	if(direct == null) 
	  throw new PipelineException
	    ("The DirectLighting was (null)!");
	
	Boolean indirect = (Boolean) getSingleParamValue("IndirectLighting");  
	if(indirect == null) 
	  throw new PipelineException
	    ("The IndirectLighting was (null)!");
	
	out.write
	  ("// ILLUMINATION LAYERS\n" + 
	   "setAttr \"maxwellGlobals.layerDirect\" " + direct + ";\n" + 
	   "setAttr \"maxwellGlobals.layerIndirect\" " + indirect + ";\n\n");
      }
      
      /* caustic layers */ 
      {
	Boolean directReflect = (Boolean) getSingleParamValue("DirectReflection");  
	if(directReflect == null) 
	  throw new PipelineException
	    ("The DirectReflection was (null)!");
	
	Boolean indirectReflect = (Boolean) getSingleParamValue("IndirectReflection");  
	if(indirectReflect == null) 
	  throw new PipelineException
	    ("The IndirectReflection was (null)!");
	
	Boolean directRefract = (Boolean) getSingleParamValue("DirectRefraction");  
	if(directRefract == null) 
	  throw new PipelineException
	    ("The DirectRefraction was (null)!");
	
	Boolean indirectRefract = (Boolean) getSingleParamValue("IndirectRefraction");  
	if(indirectRefract == null) 
	  throw new PipelineException
	    ("The IndirectRefraction was (null)!");
	
	out.write
	  ("// CAUSTIC LAYERS\n" + 
	   "setAttr \"maxwellGlobals.layerDRflCaustic\" " + directReflect + ";\n" + 
	   "setAttr \"maxwellGlobals.layerIRflCaustic\" " + indirectReflect + ";\n" + 
	   "setAttr \"maxwellGlobals.layerDRfrCaustic\" " + directRefract + ";\n" + 
	   "setAttr \"maxwellGlobals.layerIRfrCaustic\" " + indirectRefract + ";\n\n");
      }
      
      /* tone */ 
      {
	Double burn = (Double) getSingleParamValue("Burn");
	if((burn == null) || (burn <= 0.0)) 
	  throw new PipelineException
	    ("The value of Burn (" + burn + ") was illegal!");

	Double gamma = (Double) getSingleParamValue("MonitorGamma");
	if((gamma == null) || (gamma <= 0.0)) 
	  throw new PipelineException
	    ("The value of MonitorGamma (" + gamma + ") was illegal!");

	out.write
	  ("// TONE\n" + 
	   "setAttr \"maxwellGlobals.burn\" " + burn + ";\n" + 
	   "setAttr \"maxwellGlobals.monitorGamma\" " + gamma + ";\n\n");
      }
	
      out.write("EOF\n");
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the target MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.getPath());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -3817055592883558630L;

}

