// $Id: MayaRenderGlobalsAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaRenderGlobalsAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E N D E R   G L O B A L S   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a MEL script which when executed by Maya will set many of the most useful global 
 * rendering parameters of the Maya scene. <P> 
 * 
 * This generated MEL script can then be used as the PreRenderMEL script of a 
 * {@link us.temerity.pipeline.plugin.v1_2_0.MayaRenderAction MayaRender} action to allow 
 * control over rendering parameters directly from Pipeline without the need to reopen 
 * the Maya scene. <P> 
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
 *   <I>Render Quality Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Edge Anti Aliasing <BR>
 *     <DIV style="margin-left: 40px;">
 *       The quality of edge anti-aliasing. <BR>
 *     </DIV> <BR>
 *   
 *     Shading Samples <BR>
 *     <DIV style="margin-left: 40px;">
 *       The minimum number of shading samples. <BR>
 *     </DIV> <BR>
 *   
 *     Max Shading Samples <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum number of shading samples. <BR>
 *     </DIV> <BR>
 *   
 *     Particle Samples <BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of particle shading samples. <BR>
 *     </DIV> <BR>
 *   
 *     Use Multi Pixel Filtering <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable multi-pixel filtering. <BR>
 *     </DIV> <BR>
 *   
 *     Pixel Filter Type <BR>
 *     <DIV style="margin-left: 40px;">
 *       The type of filter used to integrate pixels. <BR>
 *     </DIV> <BR>
 *   
 *     Pixel Filter Width X <BR>
 *     <DIV style="margin-left: 40px;">
 *       The horizontal pixel filter width. <BR>
 *     </DIV> <BR>
 *   
 *     Pixel Filter Width Y <BR>
 *     <DIV style="margin-left: 40px;">
 *       The vertical pixel filter width. <BR>
 *     </DIV> <BR>
 *   
 *     Red Threshold <BR>
 *     <DIV style="margin-left: 40px;">
 *       The red contrast threshold. <BR>
 *     </DIV> <BR>
 *   
 *     Green Threshold <BR>
 *     <DIV style="margin-left: 40px;">
 *       The green contrast threshold. <BR>
 *     </DIV> <BR>
 *   
 *     Blue Threshold <BR>
 *     <DIV style="margin-left: 40px;">
 *       The blue contrast threshold. <BR>
 *     </DIV> <BR>
 *   
 *     Coverage Threshold <BR>
 *     <DIV style="margin-left: 40px;">
 *       The alpha coverage threshold. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Raytracing Quality Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Use Raytracing <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable raytracing. <BR>
 *     </DIV> <BR>
 *   
 *     Reflections <BR>
 *     <DIV style="margin-left: 40px;">
 *       The reflection ray depth. <BR>
 *     </DIV> <BR>
 *   
 *     Refractions <BR>
 *     <DIV style="margin-left: 40px;">
 *       The refraction ray depth. <BR>
 *     </DIV> <BR>
 *   
 *     Shadows <BR>
 *     <DIV style="margin-left: 40px;">
 *       The shadow ray depth. <BR>
 *     </DIV> <BR>
 *   
 *     Bias <BR>
 *     <DIV style="margin-left: 40px;">
 *       Distance a shadow ray travels before testing for intersections. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Motion Blur Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Use Motion Blur <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable motion blur. <BR>
 *     </DIV> <BR>
 *   
 *     Motion Blur Type<BR>
 *     <DIV style="margin-left: 40px;">
 *       The motion blur technique (2D or 3D). <BR>
 *     </DIV> <BR>
 *   
 *     Blur By Frame <BR>
 *     <DIV style="margin-left: 40px;">
 *       The amount moving objects are blurred. <BR>
 *     </DIV> <BR>
 *   
 *     Blur Length <BR>
 *     <DIV style="margin-left: 40px;">
 *       Scales the amount that moving objects are blurred. <BR>
 *     </DIV> <BR>
 *   
 *     Blur Sharpness <BR>
 *     <DIV style="margin-left: 40px;">
 *       The sharpness of motion blurred objects. <BR>
 *     </DIV> <BR>
 *   
 *     Smooth <BR>
 *     <DIV style="margin-left: 40px;">
 *       Anti-aliasing hack (Alpha or Color). <BR>
 *     </DIV> <BR>
 *   
 *     Smooth Value <BR>
 *     <DIV style="margin-left: 40px;">
 *       The amount Maya blurs motion blur edges. <BR>
 *     </DIV> <BR>
 *   
 *     Keep Motion Vectors <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whehter to save the motion vectors for external 2D blur. <BR>
 *     </DIV> <BR>
 *   
 *     Use 2d Blur Memory Limit <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to limit 2D blur memory. <BR>
 *     </DIV> <BR>
 *   
 *     2d Blur Memory Limit<BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum amount of memory used by the 2d blur operation. <BR>
 *     </DIV> 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * See the Maya documentation for more details about the meaning of these Render Globals 
 * parameters. 
 */
public
class MayaRenderGlobalsAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaRenderGlobalsAction() 
  {
    super("MayaRenderGlobals", new VersionID("2.0.0"), "Temerity",
	  "Creates a MEL script which sets the render globals of a Maya scene.");

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
      }
    }

    /* render quality */ 
    {
      {
	ArrayList<String> quality = new ArrayList<String>();
	quality.add("Low Quality");
	quality.add("Medium Quality");
	quality.add("High Quality");
	quality.add("Highest Quality");

	ActionParam param = 
	  new EnumActionParam
	  ("EdgeAntiAliasing",
	   "The quality of edge anti-aliasing.", 
	   "Low Quality", quality);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("ShadingSamples",
	   "The minimum number of shading samples.", 
	   1);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("MaxShadingSamples",
	   "The maximum number of shading samples.", 
	   1);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("ParticleSamples",
	   "The number of particle shading samples.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("UseMultiPixelFiltering",
	   "Whether to enable multi-pixel filtering.",
	   false);
	addSingleParam(param);
      }
      
      {
	ArrayList<String> quality = new ArrayList<String>();
	quality.add("Box Filter");
	quality.add("Triangle Filter");
	quality.add("Gaussian Filter");
	quality.add("Quadratic B-Spline Filter");

	ActionParam param = 
	  new EnumActionParam
	  ("PixelFilterType",
	   "The type of filter used to integrate pixels.", 
	   "Triangle Filter", quality);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("PixelFilterWidthX",
	   "The horizontal pixel filter width.", 
	   2.200);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("PixelFilterWidthY",
	   "The vertical pixel filter width.", 
	   2.200);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("RedThreshold",
	   "The red contrast threshold.", 
	   0.400);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("GreenThreshold",
	   "The green contrast threshold.", 
	   0.300);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("BlueThreshold",
	   "The blue contrast threshold.", 
	   0.600);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("CoverageThreshold",
	   "The alpha coverage threshold.", 
	   0.125);
	addSingleParam(param);
      }

      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Preview Quality");
	choices.add("Intermediate Quality");
	choices.add("Production Quality");
	choices.add("Contrast Sensitive Production");

	addPreset("Quality", choices);

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("EdgeAntiAliasing",  "Low Quality");
	  values.put("ShadingSamples",    1);
	  values.put("MaxShadingSamples", 1);
	  values.put("ParticleSamples",   1);

	  values.put("UseMultiPixelFiltering", false);
	  values.put("PixelFilterType",        "Triangle Filter");	
	  values.put("PixelFilterWidthX",      2.200);	
	  values.put("PixelFilterWidthY",      2.200);

	  values.put("RedThreshold",   0.400); 
	  values.put("GreenThreshold", 0.300);     
	  values.put("BlueThreshold",  0.600);    

	  addPresetValues("Quality", "Preview Quality", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("EdgeAntiAliasing",  "Highest Quality");
	  values.put("ShadingSamples",    1);
	  values.put("MaxShadingSamples", 8);
	  values.put("ParticleSamples",   1);

	  values.put("UseMultiPixelFiltering", false);
	  values.put("PixelFilterType",        "Triangle Filter");	
	  values.put("PixelFilterWidthX",      2.200);	
	  values.put("PixelFilterWidthY",      2.200);

	  values.put("RedThreshold",   0.400); 
	  values.put("GreenThreshold", 0.300);     
	  values.put("BlueThreshold",  0.600);    

	  addPresetValues("Quality", "Intermediate Quality", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("EdgeAntiAliasing",  "Highest Quality");
	  values.put("ShadingSamples",    2);
	  values.put("MaxShadingSamples", 8);
	  values.put("ParticleSamples",   1);

	  values.put("UseMultiPixelFiltering", true);
	  values.put("PixelFilterType",        "Triangle Filter");	
	  values.put("PixelFilterWidthX",      2.200);	
	  values.put("PixelFilterWidthY",      2.200);

	  values.put("RedThreshold",   0.400); 
	  values.put("GreenThreshold", 0.300);     
	  values.put("BlueThreshold",  0.600);    

	  addPresetValues("Quality", "Production Quality", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put("EdgeAntiAliasing",  "Highest Quality");
	  values.put("ShadingSamples",    2);
	  values.put("MaxShadingSamples", 8);
	  values.put("ParticleSamples",   1);

	  values.put("UseMultiPixelFiltering", true);
	  values.put("PixelFilterType",        "Triangle Filter");	
	  values.put("PixelFilterWidthX",      2.200);	
	  values.put("PixelFilterWidthY",      2.200);

	  values.put("RedThreshold",   0.200); 
	  values.put("GreenThreshold", 0.150);     
	  values.put("BlueThreshold",  0.300);    

	  addPresetValues("Quality", "Contrast Sensitive Production", values);
	}
      }
    }

    /* raytracing quality */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("UseRaytracing",
	   "Whether to enable raytracing.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("Reflections",
	   "The reflection ray depth.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("Refractions",
	   "The refraction ray depth.", 
	   6);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("Shadows",
	   "The shadow ray depth.", 
	   2);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("Bias",
	   "Distance a shadow ray travels before testing for intersections.", 
	   0.0);
	addSingleParam(param);
      }
    }

    /* motion blur */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("UseMotionBlur",
	   "Whether to enable motion blur.",
	   false);
	addSingleParam(param);
      }

      {
	ArrayList<String> quality = new ArrayList<String>();
	quality.add("2D");
	quality.add("3D");

	ActionParam param = 
	  new EnumActionParam
	  ("MotionBlurType",
	   "The motion blur technique.", 
	   "2D", quality);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("BlurByFrame",
	   "The amount moving objects are blurred.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("BlurLength",
	   "Scales the amount that moving objects are blurred.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("BlurSharpness",
	   "The sharpness of motion blurred objects.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ArrayList<String> quality = new ArrayList<String>();
	quality.add("Alpha");
	quality.add("Color");

	ActionParam param = 
	  new EnumActionParam
	  ("Smooth",
	   "Anti-aliasing hack.", 
	   "Alpha", quality);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("SmoothValue",
	   "The amount Maya blurs motion blur edges.", 
	   2);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("KeepMotionVectors",
	   "Whehter to save the motion vectors for external 2D blur.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Use2dBlurMemoryLimit",
	   "Whether to limit 2D blur memory.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("2dBlurMemoryLimit",
	   "The maximum amount of memory used by the 2d blur operation.", 
	   200.0);
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
	LayoutGroup aaq = new LayoutGroup
	  ("AntiAliasingQuality", "Overall anti-aliasing quality controls.", false);
	aaq.addEntry("Quality");
	aaq.addEntry("EdgeAntiAliasing");
	
	{
	  LayoutGroup nos = new LayoutGroup
	    ("NumberOfSamples", "Sampling controls.", true);
	  nos.addEntry("ShadingSamples");
	  nos.addEntry("MaxShadingSamples");
	  nos.addSeparator(); 
	  nos.addEntry("ParticleSamples");

	  aaq.addSubGroup(nos);
	}

	{ 
	  LayoutGroup mpf = new LayoutGroup
	    ("MultiPixelFiltering", "Output pixel filtering controls.", true);
	  mpf.addEntry("UseMultiPixelFiltering");
	  mpf.addSeparator(); 
	  mpf.addEntry("PixelFilterType");
	  mpf.addEntry("PixelFilterWidthX");
	  mpf.addEntry("PixelFilterWidthY");

	  aaq.addSubGroup(mpf);
	}
	  
	{ 
	  LayoutGroup ct = new LayoutGroup
	    ("ContrastThreshold", "Controls of oversampling due to sample contrast.", true);
	  ct.addEntry("RedThreshold");
	  ct.addEntry("GreenThreshold");
	  ct.addEntry("BlueThreshold");
	  ct.addSeparator(); 
	  ct.addEntry("CoverageThreshold");

	  aaq.addSubGroup(ct);
	}

	layout.addSubGroup(aaq);
      }

      {
	LayoutGroup rq = new LayoutGroup
	  ("RaytracingQuality", "Overall raytracing quality controls.", false);
	rq.addEntry("UseRaytracing");
	rq.addSeparator(); 
	rq.addEntry("Reflections");
	rq.addEntry("Refractions");
	rq.addEntry("Shadows");
	rq.addEntry("Bias");
	
	layout.addSubGroup(rq);
      }

      {
	LayoutGroup mb = new LayoutGroup
	  ("MotionBlur", "Motion blur specific quality controls.", false);
	mb.addEntry("UseMotionBlur");
	mb.addSeparator(); 
	mb.addEntry("MotionBlurType");
	mb.addEntry("BlurByFrame");
	mb.addEntry("BlurLength");
	mb.addEntry("BlurSharpness");
	mb.addSeparator(); 
	mb.addEntry("Smooth");
	mb.addEntry("SmoothValue");
	mb.addSeparator(); 
	mb.addEntry("KeepMotionVectors");
	mb.addSeparator(); 
	mb.addEntry("Use2dBlurMemoryLimit");
	mb.addEntry("2dBlurMemoryLimit");
	
	layout.addSubGroup(mb);
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
    /* sanity checks */ 
    NodeID nodeID = agenda.getNodeID();
    FileSeq fseq = agenda.getPrimaryTarget();
    if(!fseq.isSingle() || !fseq.getFilePattern().getSuffix().equals("mel"))
      throw new PipelineException
	("The MayaRenderGlobals Action requires that primary target file sequence must " + 
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
	   "setAttr \"defaultResolution.aspectLock\" 0;\n" + 
	   "setAttr \"defaultResolution.width\" " + width + ";\n" + 
	   "setAttr \"defaultResolution.height\" " + height + ";\n" + 
	   "setAttr \"defaultResolution.aspectLock\" 0;\n" + 
	   "setAttr \"defaultResolution.deviceAspectRatio\" " + deviceRatio + ";\n\n");
      }

      /* render quality */ 
      {
	EnumActionParam param = (EnumActionParam) getSingleParam("EdgeAntiAliasing");
	int edgeAliasing = -1;
	if(param != null) 
	  edgeAliasing = 3 - param.getIndex();
	if((edgeAliasing < 0) || (edgeAliasing > 3)) 
	  throw new PipelineException
	    ("The value of EdgeAntiAliasing (" + param.getValue() + ") was illegal!"); 

	out.write
	  ("// RENDER QUALITY\n" + 
	   "setAttr \"defaultRenderQuality.edgeAntiAliasing\" " + edgeAliasing + ";\n\n");
      }

      /* number of samples */ 
      {
	Integer samples = (Integer) getSingleParamValue("ShadingSamples"); 
	if((samples == null) || (samples <= 0)) 
	  throw new PipelineException
	    ("The value of ShadingSamples (" + samples + ") was illegal!");
	
	Integer msamples = (Integer) getSingleParamValue("MaxShadingSamples"); 
	if((msamples == null) || (msamples <= 0)) 
	  throw new PipelineException
	    ("The value of MaxShadingSamples (" + msamples + ") was illegal!");
	
	Integer psamples = (Integer) getSingleParamValue("ParticleSamples"); 
	if((psamples == null) || (psamples <= 0)) 
	  throw new PipelineException
	    ("The value of ParticleSamples (" + psamples + ") was illegal!");
	
	out.write
	  ("// NUMBER OF SAMPLES\n" + 
	   "setAttr \"defaultRenderQuality.shadingSamples\" " + samples + ";\n" + 
	   "setAttr \"defaultRenderQuality.maxShadingSamples\" " + msamples + ";\n" + 
	   "setAttr \"defaultRenderQuality.particleSamples\" " + psamples + ";\n\n");
      }
      
      /* pixel filtering */ 
      {
	Boolean useFilter = (Boolean) getSingleParamValue("UseMultiPixelFiltering");  
	if(useFilter == null) 
	  throw new PipelineException
	    ("The UseMultiPixelFiltering was (null)!");
	
	int filterType = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("PixelFilterType");
	  if(param != null) {
	    switch(param.getIndex()) {
	    case 0:
	      filterType = 0;  // Box
	      break;
	      
	    case 1:
	      filterType = 2;  // Triagle
	      break;
	      
	    case 2:
	      filterType = 4;  // Gaussian
	      break;

	    case 3:
	      filterType = 5;  // Quadratic B-Spline
	    }
	  }

	  if(filterType == -1) 
	    throw new PipelineException
	      ("The PixelFilterType (" + param.getValue() + ") was illegal!"); 
	}
	 
	Double filterX = (Double) getSingleParamValue("PixelFilterWidthX");
	if((filterX == null) || (filterX <= 0.0)) 
	  throw new PipelineException
	    ("The value of PixelFilterWidthX (" + filterX + ") was illegal!");
	
	Double filterY = (Double) getSingleParamValue("PixelFilterWidthY");
	if((filterY == null) || (filterY <= 0.0)) 
	  throw new PipelineException
	    ("The value of PixelFilterWidthY (" + filterY + ") was illegal!");

	out.write
	  ("// PIXEL FILTERING\n" +
	   "setAttr \"defaultRenderQuality.useMultiPixelFilter\" " + useFilter + ";\n" +
	   "setAttr \"defaultRenderQuality.pixelFilterType\" " + filterType + ";\n" + 
	   "setAttr \"defaultRenderQuality.pixelFilterWidthX\" " + filterX + ";\n" + 
	   "setAttr \"defaultRenderQuality.pixelFilterWidthY\" " + filterY + ";\n\n");
      }

      /* contrast threshold */ 
      {
	Double red = (Double) getSingleParamValue("RedThreshold");
	if((red == null) || (red < 0.0)) 
	  throw new PipelineException
	    ("The value of RedThreshold (" + red + ") was illegal!");
	
	Double green = (Double) getSingleParamValue("GreenThreshold");
	if((green == null) || (green < 0.0)) 
	  throw new PipelineException
	    ("The value of GreenThreshold (" + green + ") was illegal!");

	Double blue = (Double) getSingleParamValue("BlueThreshold");
	if((blue == null) || (blue < 0.0)) 
	  throw new PipelineException
	    ("The value of BlueThreshold (" + blue + ") was illegal!");
	
	Double coverage = (Double) getSingleParamValue("CoverageThreshold");
	if((coverage == null) || (coverage < 0.0)) 
	  throw new PipelineException
	    ("The value of CoverageThreshold (" + coverage + ") was illegal!");
	
	out.write
	  ("// CONTRAST THRESHOLD\n" +
	   "setAttr \"defaultRenderQuality.redThreshold\" " + red + ";\n" + 
	   "setAttr \"defaultRenderQuality.greenThreshold\" " + green + ";\n" + 
	   "setAttr \"defaultRenderQuality.blueThreshold\" " + blue + ";\n" +
	   "setAttr \"defaultRenderQuality.coverageThreshold\" " + coverage + ";\n\n");
      }

      /* raytracing quality */
      {
	Boolean useRay = (Boolean) getSingleParamValue("UseRaytracing");  
	if(useRay == null) 
	  throw new PipelineException
	    ("The UseRaytracing was (null)!");

	Integer reflect = (Integer) getSingleParamValue("Reflections"); 
	if((reflect == null) || (reflect < 0)) 
	  throw new PipelineException
	    ("The value of Reflections (" + reflect + ") was illegal!");
	
	Integer refract = (Integer) getSingleParamValue("Refractions"); 
	if((refract == null) || (refract < 0)) 
	  throw new PipelineException
	    ("The value of Refractions (" + refract + ") was illegal!");
	
	Integer shadow = (Integer) getSingleParamValue("Shadows"); 
	if((shadow == null) || (shadow < 0)) 
	  throw new PipelineException
	    ("The value of Shadows (" + shadow + ") was illegal!");
	
	Double bias = (Double) getSingleParamValue("Bias");
	if((bias == null) || (bias < 0.0)) 
	  throw new PipelineException
	    ("The value of Bias (" + bias + ") was illegal!");
	
	out.write
	  ("// RAYTRACING QUALITY \n" +
	   "setAttr \"defaultRenderQuality.enableRaytracing\" " + useRay + ";\n" +
	   "setAttr \"defaultRenderQuality.reflections\" " + reflect + ";\n" + 
	   "setAttr \"defaultRenderQuality.refractions\" " + refract + ";\n" +
	   "setAttr \"defaultRenderQuality.shadows\" " + shadow + ";\n" +
	   "setAttr \"defaultRenderQuality.rayTraceBias\" " + bias + ";\n\n");
      }

      /* motion blur */ 
      { 
	Boolean useBlur = (Boolean) getSingleParamValue("UseMotionBlur");  
	if(useBlur == null) 
	  throw new PipelineException
	    ("The UseMotionBlur was (null)!");
	
	int blurType = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("MotionBlurType");
	  if(param != null)
	    blurType = param.getIndex();
	  if(blurType == -1) 
	  throw new PipelineException
	    ("The MotionBlurType (" + param.getValue() + ") was illegal!"); 
	}
	
	Double byFrame = (Double) getSingleParamValue("BlurByFrame");
	if((byFrame == null) || (byFrame < 0.0)) 
	  throw new PipelineException
	    ("The value of BlurByFrame (" + byFrame + ") was illegal!");
	
	Double length = (Double) getSingleParamValue("BlurLength");
	if((length == null) || (length < 0.0)) 
	  throw new PipelineException
	    ("The value of BlurLength (" + length + ") was illegal!");

	Double sharpness = (Double) getSingleParamValue("BlurSharpness");
	if((sharpness == null) || (sharpness < 0.0)) 
	  throw new PipelineException
	    ("The value of BlurSharpness (" + sharpness + ") was illegal!");

	int smooth = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("Smooth");
	  if(param != null)
	    smooth = param.getIndex();
	  if(smooth == -1) 
	    throw new PipelineException
	      ("The Smooth (" + param.getValue() + ") was illegal!"); 
	}

	Integer smoothValue = (Integer) getSingleParamValue("SmoothValue"); 
	if((smoothValue == null) || (smoothValue < 0)) 
	  throw new PipelineException
	    ("The value of SmoothValue (" + smoothValue + ") was illegal!");
	
	Boolean keepVectors = (Boolean) getSingleParamValue("KeepMotionVectors");  
	if(keepVectors == null) 
	  throw new PipelineException
	    ("The KeepMotionVectors was (null)!");
	
	Boolean useMemLimit = (Boolean) getSingleParamValue("Use2dBlurMemoryLimit");  
	if(useMemLimit == null) 
	  throw new PipelineException
	    ("The Use2dBlurMemoryLimit was (null)!");
	
	Double memLimit = (Double) getSingleParamValue("2dBlurMemoryLimit");
	if((memLimit == null) || (memLimit < 0.0)) 
	  throw new PipelineException
	    ("The value of 2dBlurMemoryLimit (" + memLimit + ") was illegal!");

	out.write
	  ("// MOTION BLUR \n" +
	   "setAttr \"defaultRenderGlobals.motionBlur\" " + useBlur + ";\n" +
	   "setAttr \"defaultRenderGlobals.motionBlurType\" " + blurType + ";\n" + 
	   "setAttr \"defaultRenderGlobals.motionBlurByFrame\" " + byFrame + ";\n" +
	   "setAttr \"defaultRenderGlobals.blurLength\" " + length + ";\n" +
	   "setAttr \"defaultRenderGlobals.blurSharpness\" " + sharpness + ";\n" +
	   "setAttr \"defaultRenderGlobals.smoothColor\" " + smooth + ";\n" +
	   "setAttr \"defaultRenderGlobals.smoothValue\" " + smoothValue + ";\n" +
	   "setAttr \"defaultRenderGlobals.keepMotionVector\" " + keepVectors + ";\n" +
	   "setAttr \"defaultRenderGlobals.useBlur2DMemoryCap\" " + useMemLimit + ";\n" +
	   "setAttr \"defaultRenderGlobals.blur2DMemoryCap\" " + memLimit + ";\n\n");
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

  private static final long serialVersionUID = -8736308079810185513L;

}

