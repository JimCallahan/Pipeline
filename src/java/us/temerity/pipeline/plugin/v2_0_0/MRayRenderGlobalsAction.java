// $Id: MRayRenderGlobalsAction.java,v 1.4 2006/01/17 20:41:47 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   R E N D E R   G L O B A L S   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a MEL script which when executed by Maya will set many of the most useful global 
 * rendering parameters of the Maya scene specific to the Mental Ray renderer. <P> 
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
 *     Min Sample Level <BR>
 *     <DIV style="margin-left: 40px;">
 *       The minimum sample rate, each pixel is sampled at least 2^(2*rate). <BR>
 *     </DIV> <BR>
 *   
 *     Max Sample Level <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximim sample rate, each pixel is sampled at most 2^(2*rate). <BR>
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
 * 
 *     Sample Lock <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   
 *     Jitter <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable ray jittering. <BR>
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
 *       The reflected ray depth. <BR>
 *     </DIV> <BR>
 *   
 *      Refractions<BR>
 *     <DIV style="margin-left: 40px;">
 *       The refracted ray depth. <BR>
 *     </DIV> <BR>
 *   
 *     Max Trace Depth <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum ray depth. <BR>
 *     </DIV> <BR>
 *   
 *     Shadows <BR>
 *     <DIV style="margin-left: 40px;">
 *       The shadow ray depth. <BR>
 *     </DIV> <BR>
 *   
 *     Scanline <BR>
 *     <DIV style="margin-left: 40px;">
 *       Controls the use of scanline rendering. <BR>
 *     </DIV> <BR>
 *   
 *     Faces <BR>
 *     <DIV style="margin-left: 40px;">
 *       Controls which side(s) of triangles are rendered. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Motion Blur Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Motion Blur <BR>
 *     <DIV style="margin-left: 40px;">
 *       Controls the motion blur technique. <BR>
 *     </DIV> <BR>
 *   
 *     Motion Blur By <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   
 *     Shutter <BR>
 *     <DIV style="margin-left: 40px;">
 *        The time when the shutter closes. <BR>
 *     </DIV> <BR>
 *   
 *     Shutter Delay <BR>
 *     <DIV style="margin-left: 40px;">
 *       The time when the shutter opens. <BR>
 *     </DIV> <BR>
 *   
 *     Time Contrast Red <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum temporal contrast in the red channel. <BR>
 *     </DIV> <BR>
 *   
 *     Time Contrast Green <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum temporal contrast in the green channel. <BR>
 *     </DIV> <BR>
 *   
 *     Time Contrast Blue <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum temporal contrast in the blue channel. <BR>
 *     </DIV> <BR>
 *   
 *     Time Contrast Alpha <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum temporal contrast in the alpha channel. <BR>
 *     </DIV> <BR>
 *   
 *     Motion Steps <BR>
 *     <DIV style="margin-left: 40px;">
 *       Approximate instance motion transformations with steps segments. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Caustic Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Use Caustics <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable caustics. <BR>
 *     </DIV> <BR>
 *   
 *     Caustics Accuracy <BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of photons used to estimate caustics during rendering. <BR>
 *     </DIV> <BR>
 *   
 *      <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum radius to be used when picking up caustic photons. <BR>
 *     </DIV> <BR>
 *   
 *     Caustics Radius <BR>
 *     <DIV style="margin-left: 40px;">
 *        <BR>
 *     </DIV> <BR>
 *   
 *     Caustic Filter Type <BR>
 *     <DIV style="margin-left: 40px;">
 *       The type of filter used by caustics. <BR>
 *     </DIV> <BR>
 *   
 *     Caustic Filter Kernel <BR>
 *     <DIV style="margin-left: 40px;">
 *        The size of the caustics filter kernel. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Global Illumination Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Use Global Illum <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable global illumination. <BR>
 *     </DIV> <BR>
 *   
 *     Global Illum Accuracy<BR>
 *     <DIV style="margin-left: 40px;">
 *        The number of photons used to estimate global illumination during rendering. <BR>
 *     </DIV> <BR>
 *   
 *     Global Illum Radius <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum radius to be used when reading global illumination photons. <BR>
 *     </DIV> <BR>
 *   
 *     Photon Volume Accuracy <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum number of photons to examine in participating media. <BR>
 *     </DIV> <BR>
 *   
 *     Photon Volume Radius <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum radius to search for photons in participating media. <BR>
 *     </DIV> <BR>
 *   
 *     Max Reflection Photons <BR>
 *     <DIV style="margin-left: 40px;">
 *       The reflected photon depth. <BR>
 *     </DIV> <BR>
 *   
 *     Max Refraction Photons <BR>
 *     <DIV style="margin-left: 40px;">
 *       The refracted photon depth. <BR>
 *     </DIV> <BR>
 *   
 *     Max Photon Depth <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum photon depth. <BR>
 *     </DIV> <BR>
 *   
 *     Photon Map File <BR>
 *     <DIV style="margin-left: 40px;">
 *       The name of the file to save/reuse photons. <BR>
 *     </DIV> <BR>
 *   
 *     Enable GI Map Visualizer <BR>
 *     <DIV style="margin-left: 40px;">
 *       Produce a false-color rendering of photon density. <BR>
 *     </DIV> <BR>
 *   
 *     Photon Map Rebuild  <BR>
 *     <DIV style="margin-left: 40px;">
 *       Ignore and overwrite the photon map file if it exists. <BR>
 *     </DIV> <BR>
 *   
 *     Direct Illum Shadow Effects <BR>
 *     <DIV style="margin-left: 40px;">
 *        ??? <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Final Gathering Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Use Final Gather <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable final gathering. <BR>
 *     </DIV> <BR>
 *   
 *     Precomp Photon Lookup <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to store final gather irradiance in photon map. <BR>
 *     </DIV> <BR>
 *   
 *     Final Gather Rays <BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of final gathering rays to cast. <BR>
 *     </DIV> <BR>
 *   
 *     Min Radius <BR>
 *     <DIV style="margin-left: 40px;">
 *       The minimum distance within which final gather results must be reused. <BR>
 *     </DIV> <BR>
 *   
 *     Max Radius <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum distance within which a final gather result can be reused. <BR>
 *     </DIV> <BR>
 *   
 *     Filter <BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of neighboring samples to include in the speckle elimination filter. <BR>
 *     </DIV> <BR>
 *   
 *     Falloff Start <BR>
 *     <DIV style="margin-left: 40px;">
 *       The distance at which final gather rays start blending with the env color. <BR>
 *     </DIV> <BR>
 *   
 *     Falloff Stop <BR>
 *     <DIV style="margin-left: 40px;">
 *        The distance at which final gather rays completely blend with the env color. <BR>
 *     </DIV> <BR>
 *   
 *     Trace Depth <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum final gather ray depth. <BR>
 *     </DIV> <BR>
 *   
 *     Trace Reflection <BR>
 *     <DIV style="margin-left: 40px;">
 *        The reflected final gather ray depth.<BR>
 *     </DIV> <BR>
 *   
 *     Trace Refraction<BR>
 *     <DIV style="margin-left: 40px;">
 *       The refracted final gather ray depth. <BR>
 *     </DIV> <BR>
 *   
 *     Final Gather Map File <BR>
 *     <DIV style="margin-left: 40px;">
 *       The name of the file to save/reuse final gather rays.  <BR>
 *     </DIV> <BR>
 *   
 *     Preview Final Gather <BR>
 *     <DIV style="margin-left: 40px;">
 *       Shows diagnostic final gathering points in output image <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Framebuffer Attribute Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Data Type <BR>
 *     <DIV style="margin-left: 40px;">
 *       The output data format. <BR>
 *     </DIV> <BR>
 *   
 *     Gamma <BR>
 *     <DIV style="margin-left: 40px;">
 *       The gamma correction factor. <BR>
 *     </DIV> <BR>
 *   
 *     Color Clip <BR>
 *     <DIV style="margin-left: 40px;">
 *       Pre-quantization color clipping method. <BR>
 *     </DIV> <BR>
 *   
 *     Interp Samples<BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   
 *      Desaturate <BR>
 *     <DIV style="margin-left: 40px;">
 *       Bleach out clipped colors to maintain percieved brightness. <BR>
 *     </DIV> <BR>
 *   
 *     Premultiply <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to premultiply alpha into color channels. <BR>
 *     </DIV> <BR>
 *   
 *     Dither <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable dithering. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Translation Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Export Verbosity <BR>
 *     <DIV style="margin-left: 40px;">
 *       The verbosity of export messages. <BR>
 *     </DIV> <BR>
 *   
 *     Export Exact Hierarchy <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   
 *     Export Full Dagpath <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   
 *     Export Textures First <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   
 *     Export Particles <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   
 *     Export Particle Instances <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   
 *     Export Fluids <BR>
 *     <DIV style="margin-left: 40px;">
 *        <BR>
 *     </DIV> <BR>
 *   
 *     Export Post Effects <BR>
 *     <DIV style="margin-left: 40px;">
 *        <BR>
 *     </DIV> 
 *   </DIV> 
 * </DIV><P> 
 * 
 * See the Maya and Mental Ray documentation for more details about the meaning of these 
 * Render Globals parameters.
 */
public
class MRayRenderGlobalsAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayRenderGlobalsAction() 
  {
    super("MRayRenderGlobals", new VersionID("2.0.0"), "Temerity",
	  "Creates a MEL script which sets the Mental Ray render globals of a Maya scene.");

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
	ActionParam param = 
	  new IntegerActionParam
	  ("MinSampleLevel",
	   "The minimum sample rate, each pixel is sampled at least 2^(2*rate).", 
	   -2);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("MaxSampleLevel",
	   "The maximim sample rate, each pixel is sampled at most 2^(2*rate).", 
	   0);
	addSingleParam(param);
      }

      {
	ArrayList<String> filter = new ArrayList<String>();
	filter.add("Box Filter");
	filter.add("Triangle Filter");
	filter.add("Gaussian Filter");
	filter.add("Mitchell Filter");
	filter.add("Lanczos Filter");

	ActionParam param = 
	  new EnumActionParam
	  ("PixelFilterType",
	   "The type of filter used to integrate pixels.", 
	   "Triangle Filter", filter);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("PixelFilterWidthX",
	   "The horizontal pixel filter width.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("PixelFilterWidthY",
	   "The vertical pixel filter width.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("RedThreshold",
	   "The red contrast threshold.", 
	   0.100);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("GreenThreshold",
	   "The green contrast threshold.", 
	   0.100);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("BlueThreshold",
	   "The blue contrast threshold.", 
	   0.100);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("CoverageThreshold",
	   "The alpha coverage threshold.", 
	   0.100);
	addSingleParam(param);
      }
    }

    /* sample options */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("SampleLock",
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Jitter",
	   "Whether to enable ray jittering.",
	   false);
	addSingleParam(param);
      }
    }

    /* raytracing quality */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("UseRaytracing",
	   "Whether to enable raytracing.",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("Reflections",
	   "The reflected ray depth.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("Refractions",
	   "The refracted ray depth.", 
	   1);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("MaxTraceDepth",
	   "The maximum ray depth.", 
	   2);
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
	ArrayList<String> scanline = new ArrayList<String>();
	scanline.add("Off");
	scanline.add("On");
	scanline.add("OpenGL");
	scanline.add("Rapid");

	ActionParam param = 
	  new EnumActionParam
	  ("Scanline",
	   "Controls the use of scanline rendering.", 
	   "On", scanline);
	addSingleParam(param);
      }

      {
	ArrayList<String> faces = new ArrayList<String>();
	faces.add("Front");
	faces.add("Back");
	faces.add("Both");

	ActionParam param = 
	  new EnumActionParam
	  ("Faces",
	   "Controls which side(s) of triangles are rendered.", 
	   "Both", faces);
	addSingleParam(param);
      }
    }

    /* motion blur */ 
    {
      {
	ArrayList<String> blur = new ArrayList<String>();
	blur.add("Off");
	blur.add("Linear");
	blur.add("Exact");

	ActionParam param = 
	  new EnumActionParam
	  ("MotionBlur",
	   "Controls the motion blur technique.", 
	   "Off", blur);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("MotionBlurBy",
	   "???", 
	   1.0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("Shutter",
	   "The time when the shutter closes.", 
	   1.0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("ShutterDelay",
	   "The time when the shutter opens.", 
	   0.0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("TimeContrastRed",
	   "The maximum temporal contrast in the red channel.", 
	   0.200);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("TimeContrastGreen",
	   "The maximum temporal contrast in the green channel.", 
	   0.200);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("TimeContrastBlue",
	   "The maximum temporal contrast in the blue channel.", 
	   0.200);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("TimeContrastAlpha",
	   "The maximum temporal contrast in the alpha channel.", 
	   0.200);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("MotionSteps",
	   "Approximate instance motion transformations with steps segments.", 
	   1);
	addSingleParam(param);
      }
    }

    /* caustics */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("UseCaustics",
	   "Whether to enable caustics.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("CausticsAccuracy",
	   "The number of photons used to estimate caustics during rendering.", 
	   64);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("CausticsRadius",
	   "The maximum radius to be used when picking up caustic photons.", 
	   0.0);
	addSingleParam(param);
      }
      
      {
	ArrayList<String> filter = new ArrayList<String>();
	filter.add("Box Filter");
	filter.add("Cone Filter");

	ActionParam param = 
	  new EnumActionParam
	  ("CausticFilterType",
	   "The type of filter used by caustics.", 
	   "Box Filter", filter);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("CausticFilterKernel",
	   "The size of the caustics filter kernel.", 
	   1.100);
	addSingleParam(param);
      }
    }

    /* global illumination */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("UseGlobalIllum",
	   "Whether to enable global illumination.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("GlobalIllumAccuracy",
	   "The number of photons used to estimate global illumination during rendering.", 
	   64);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("GlobalIllumRadius",
	   "The maximum radius to be used when picking up global illumination photons.",
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("PhotonVolumeAccuracy",
	   "The maximum number of photons to examine in participating media.", 
	   64);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("PhotonVolumeRadius",
	   "The maximum radius to search for photons in participating media.", 
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("MaxReflectionPhotons",
	   "The reflected photon depth.", 
	   5);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("MaxRefractionPhotons",
	   "The refracted photon depth.", 
	   5);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("MaxPhotonDepth",
	   "The maximum photon depth.", 
	   5);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new StringActionParam
	  ("PhotonMapFile",
	   "The name of the file to save/reuse photons.", 
	   "");
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("EnableGIMapVisualizer",
	   "Produce a false-color rendering of photon density.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("PhotonMapRebuild",
	   "Ignore and overwrite the photon map file if it exists.",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("DirectIllumShadowEffects",
	   "???",
	   false);
	addSingleParam(param);
      }
    }

    /* final gather */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("UseFinalGather",
	   "Whether to enable final gathering.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("PrecompPhotonLookup",
	   "Whether to store final gather irradiance in photon map.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("FinalGatherRays",
	   "The number of final gathering rays to cast.",
	   1000);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("MinRadius",
	   "The minimum distance within which final gather results must be reused.", 
	   0.0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("MaxRadius",
	   "The maximum distance within which a final gather result can be reused.", 
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("Filter",
	   "The number of neighboring samples to include in the speckle elimination filter.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  ("FalloffStart",
	   "The distance at which final gather rays start blending with the env color.", 
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("FalloffStop",
	   "The distance at which final gather rays completely blend with the env color.", 
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("TraceDepth",
	   "The maximum final gather ray depth.", 
	   2);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("TraceReflection",
	   "The reflected final gather ray depth.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("TraceRefraction",
	   "The refracted final gather ray depth.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new StringActionParam
	  ("FinalGatherMapFile",
	   "The name of the file to save/reuse final gather rays.", 
	   "");
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("EnableFGMapVisualizer",
	   "Produce a false-color rendering of final gather ray density.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("FinalGatherMapRebuild",
	   "Ignore and overwrite the final gather map file if it exists.",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("PreviewFinalGather",
	   "Shows diagnostic final gathering points in output image.",
	   true);
	addSingleParam(param);
      }
    }

    /* framebuffer attributes */ 
    {
      {
	ArrayList<String> dtype = new ArrayList<String>();
	dtype.add("RGB (Byte) 3x8");
	dtype.add("RGB (Short) 3x16");
	dtype.add("RGB (Half) 3x16");
	dtype.add("RGB (Float) 3x32");
	dtype.add("RGBA (Byte) 4x8");
	dtype.add("RGBE (Byte) 4x8");
	dtype.add("RGBA (Short) 4x16");
	dtype.add("RGBA (Half) 4x16");
	dtype.add("RGBA (Float) 4x32");
	dtype.add("Alpha (Byte) 1x8");
	dtype.add("Alpha (Short) 1x16");
	dtype.add("Alpha (Float) 1x32");
	dtype.add("Depth (Float) 1x32");
	dtype.add("Coverage (Float) 1x32");
	dtype.add("Normal (Float) 3x32");
	dtype.add("Motion (Float) 3x32");
	dtype.add("Label (Integer) 1x32");
	dtype.add("Bit");

	ActionParam param = 
	  new EnumActionParam
	  ("DataType",
	   "The output data format.", 
	   "RGBA (Byte) 4x8", dtype);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("Gamma",
	   "The gamma correction factor.", 
	   1.0);
	addSingleParam(param);
      }
      
      {
	ArrayList<String> clip = new ArrayList<String>();
	clip.add("RGB");
	clip.add("Alpha");
	clip.add("Raw");
      
	ActionParam param = 
	  new EnumActionParam
	  ("ColorClip",
	   "Pre-quantization color clipping method.", 
	   "Raw", clip);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("InterpSamples",
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Desaturate",
	   "Bleach out clipped colors to maintain percieved brightness.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Premultiply",
	   "Whether to premultiply alpha into color channels.",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Dither",
	   "Whether to enable dithering.",
	   true);
	addSingleParam(param);
      }
    }

    /* memory and performance */ 
    {
      {
	ArrayList<String> method = new ArrayList<String>();
	method.add("BSP");
	method.add("Grid");
	method.add("Large BSP");

	ActionParam param = 
	  new EnumActionParam
	  ("AccelerationMethod",
	   "The algorithm used to acceleration ray testing.", 
	   "BSP", method);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("BspSize",
	   "???", 
	   10);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("BspDepth",
	   "???", 
	   40);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("GridResolution",
	   "???", 
	   0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("GridMaxSize",
	   "???", 
	   128);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("GridDepth",
	   "???", 
	   2);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("TaskSize",
	   "???", 
	   0);
	addSingleParam(param);
      }

      /* memory limits */ 
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("PhysicalMemory",
	   "???", 
	   800);
	addSingleParam(param);
      }
    }

    /* translation */ 
    {
      {
	ArrayList<String> verbose = new ArrayList<String>();
	verbose.add("No Messages");
	verbose.add("Fatal Messages Only");
	verbose.add("Error Messages");
	verbose.add("Warning Messages");
	verbose.add("Info Messages");
	verbose.add("Progress Messages");
	verbose.add("Details Messages");

	ActionParam param = 
	  new EnumActionParam
	  ("ExportVerbosity",
	   "The verbosity of export messages.", 
	   "Warning Messages", verbose);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportExactHierarchy",
	   "???",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportFullDagpath",
	   "???",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportTexturesFirst",
	   "???",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportParticles",
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportParticleInstances",
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportFluids",
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportPostEffects",
	   "???",
	   true);
	addSingleParam(param);
      }
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Draft");
      choices.add("Draft Motion Blur");
      choices.add("Preview");
      choices.add("Preview Caustics");
      choices.add("Preview Final Gather");
      choices.add("Preview Global Illum");
      choices.add("Preview Fur");
      choices.add("Preview Motion Blur");
      choices.add("Production");
      choices.add("Production Motion Blur");
      
      addPreset("Quality", choices);
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     -2);
	values.put("MaxSampleLevel",     0);
	values.put("PixelFilterType",    "Box Filter");
	values.put("PixelFilterWidthX",  1.0);
	values.put("PixelFilterWidthY",  1.0);
	values.put("RedThreshold",       0.1);
	values.put("GreenThreshold",     0.1);
	values.put("BlueThreshold",      0.1);
	values.put("Jitter",             false);

	values.put("Reflections",        1);
	values.put("Refractions",        1);
	values.put("MaxTraceDepth",      2);

	values.put("MotionBlur",         "Off");
	values.put("TimeContrastRed",    1.0);
	values.put("TimeContrastGreen",  1.0);
	values.put("TimeContrastBlue",   1.0);
	values.put("TimeContrastAlpha",  1.0);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     false);
	values.put("UseFinalGather",     false);

	addPresetValues("Quality", "Draft", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     -2);
	values.put("MaxSampleLevel",     0);
	values.put("PixelFilterType",    "Box Filter");
	values.put("PixelFilterWidthX",  1.0);
	values.put("PixelFilterWidthY",  1.0);
	values.put("RedThreshold",       0.1);
	values.put("GreenThreshold",     0.1);
	values.put("BlueThreshold",      0.1);
	values.put("Jitter",             false);

	values.put("Reflections",        1);
	values.put("Refractions",        1);
	values.put("MaxTraceDepth",      2);

	values.put("MotionBlur",         "Linear");
	values.put("TimeContrastRed",    1.0);
	values.put("TimeContrastGreen",  1.0);
	values.put("TimeContrastBlue",   1.0);
	values.put("TimeContrastAlpha",  1.0);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     false);
	values.put("UseFinalGather",     false);

	addPresetValues("Quality", "Draft Motion Blur", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     -1);
	values.put("MaxSampleLevel",     1);
	values.put("PixelFilterType",    "Triangle Filter");
	values.put("PixelFilterWidthX",  2.0);
	values.put("PixelFilterWidthY",  2.0);
	values.put("RedThreshold",       0.1);
	values.put("GreenThreshold",     0.1);
	values.put("BlueThreshold",      0.1);
	values.put("Jitter",             false);

	values.put("Reflections",        2);
	values.put("Refractions",        2);
	values.put("MaxTraceDepth",      4);

	values.put("MotionBlur",         "Off");
	values.put("TimeContrastRed",    1.0);
	values.put("TimeContrastGreen",  1.0);
	values.put("TimeContrastBlue",   1.0);
	values.put("TimeContrastAlpha",  1.0);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     false);
	values.put("UseFinalGather",     false);
	

	addPresetValues("Quality", "Preview", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     -1);
	values.put("MaxSampleLevel",     1);
	values.put("PixelFilterType",    "Triangle Filter");
	values.put("PixelFilterWidthX",  2.0);
	values.put("PixelFilterWidthY",  2.0);
	values.put("Jitter",             false);

	values.put("Reflections",        2);
	values.put("Refractions",        2);
	values.put("MaxTraceDepth",      4);

	values.put("MotionBlur",         "Off");
	values.put("TimeContrastRed",    1.0);
	values.put("TimeContrastGreen",  1.0);
	values.put("TimeContrastBlue",   1.0);
	values.put("TimeContrastAlpha",  1.0);

	values.put("UseCaustics",        true);
	values.put("UseGlobalIllum",     false);
	values.put("UseFinalGather",     false);

	addPresetValues("Quality", "Preview Caustics", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     -1);
	values.put("MaxSampleLevel",     1);
	values.put("PixelFilterType",    "Triangle Filter");
	values.put("PixelFilterWidthX",  2.0);
	values.put("PixelFilterWidthY",  2.0);
	values.put("RedThreshold",       0.1);
	values.put("GreenThreshold",     0.1);
	values.put("BlueThreshold",      0.1);
	values.put("Jitter",             false);

	values.put("Reflections",        2);
	values.put("Refractions",        2);
	values.put("MaxTraceDepth",      4);

	values.put("MotionBlur",         "Off");
	values.put("TimeContrastRed",    1.0);
	values.put("TimeContrastGreen",  1.0);
	values.put("TimeContrastBlue",   1.0);
	values.put("TimeContrastAlpha",  1.0);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     true);
	values.put("UseFinalGather",     true);

	addPresetValues("Quality", "Preview Final Gather", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     -1);
	values.put("MaxSampleLevel",     1);
	values.put("PixelFilterType",    "Triangle Filter");
	values.put("PixelFilterWidthX",  2.0);
	values.put("PixelFilterWidthY",  2.0);
	values.put("RedThreshold",       0.1);
	values.put("GreenThreshold",     0.1);
	values.put("BlueThreshold",      0.1);
	values.put("Jitter",             false);

	values.put("Reflections",        2);
	values.put("Refractions",        2);
	values.put("MaxTraceDepth",      4);

	values.put("MotionBlur",         "Off");
	values.put("TimeContrastRed",    1.0);
	values.put("TimeContrastGreen",  1.0);
	values.put("TimeContrastBlue",   1.0);
	values.put("TimeContrastAlpha",  1.0);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     true);
	values.put("UseFinalGather",     false);

	addPresetValues("Quality", "Preview Global Illum", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     1);
	values.put("MaxSampleLevel",     2);
	values.put("PixelFilterType",    "Triangle Filter");
	values.put("PixelFilterWidthX",  1.5);
	values.put("PixelFilterWidthY",  1.5); 
	values.put("RedThreshold",       0.040);
	values.put("GreenThreshold",     0.030);
	values.put("BlueThreshold",      0.060);
	values.put("Jitter",             false);

	values.put("Reflections",        2);
	values.put("Refractions",        2);
	values.put("MaxTraceDepth",      4);

	values.put("MotionBlur",         "Off");
	values.put("TimeContrastRed",    1.0);
	values.put("TimeContrastGreen",  1.0);
	values.put("TimeContrastBlue",   1.0);
	values.put("TimeContrastAlpha",  1.0);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     true);
	values.put("UseFinalGather",     false);

	addPresetValues("Quality", "Preview Fur", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     -1);
	values.put("MaxSampleLevel",     1);
	values.put("PixelFilterType",    "Triangle Filter");
	values.put("PixelFilterWidthX",  2.0);
	values.put("PixelFilterWidthY",  2.0);
	values.put("RedThreshold",       0.1);
	values.put("GreenThreshold",     0.1);
	values.put("BlueThreshold",      0.1);
	values.put("Jitter",             false);

	values.put("Reflections",        2);
	values.put("Refractions",        2);
	values.put("MaxTraceDepth",      4);

	values.put("MotionBlur",         "Linear");
	values.put("TimeContrastRed",    0.500);
	values.put("TimeContrastGreen",  0.500);
	values.put("TimeContrastBlue",   0.500);
	values.put("TimeContrastAlpha",  0.500);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     false);
	values.put("UseFinalGather",     false);	

	addPresetValues("Quality", "Preview Motion Blur", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     0);
	values.put("MaxSampleLevel",     2);
	values.put("PixelFilterType",    "Gaussian Filter");
	values.put("PixelFilterWidthX",  3.0);
	values.put("PixelFilterWidthY",  3.0);
	values.put("RedThreshold",       0.1);
	values.put("GreenThreshold",     0.1);
	values.put("BlueThreshold",      0.1);
	values.put("Jitter",             false);

	values.put("Reflections",        10);
	values.put("Refractions",        10);
	values.put("MaxTraceDepth",      20);

	values.put("MotionBlur",         "Off");
	values.put("TimeContrastRed",    0.200);
	values.put("TimeContrastGreen",  0.200);
	values.put("TimeContrastBlue",   0.200);
	values.put("TimeContrastAlpha",  0.200);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     false);
	values.put("UseFinalGather",     false);	
	

	addPresetValues("Quality", "Production", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("MinSampleLevel",     0);
	values.put("MaxSampleLevel",     2);
	values.put("PixelFilterType",    "Gaussian Filter");
	values.put("PixelFilterWidthX",  3.0);
	values.put("PixelFilterWidthY",  3.0);
	values.put("RedThreshold",       0.1);
	values.put("GreenThreshold",     0.1);
	values.put("BlueThreshold",      0.1);
	values.put("Jitter",             false);

	values.put("Reflections",        10);
	values.put("Refractions",        10);
	values.put("MaxTraceDepth",      20);

	values.put("MotionBlur",         "Linear");
	values.put("TimeContrastRed",    0.200);
	values.put("TimeContrastGreen",  0.200);
	values.put("TimeContrastBlue",   0.200);
	values.put("TimeContrastAlpha",  0.200);

	values.put("UseCaustics",        false);
	values.put("UseGlobalIllum",     false);
	values.put("UseFinalGather",     false);	

	addPresetValues("Quality", "Production Motion Blur", values);
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
	  ("RenderQuality", "The overall rendering quality controls.", false);
	aaq.addEntry("Quality");
	
	{
	  LayoutGroup nos = new LayoutGroup
	    ("NumberOfSamples", "The ray sampling controls.", true);
	  nos.addEntry("MinSampleLevel");
	  nos.addEntry("MaxSampleLevel");

	  aaq.addSubGroup(nos);
	}

	{ 
	  LayoutGroup mpf = new LayoutGroup
	    ("MultiPixelFiltering", "Output pixel filtering controls.", true);
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

	{ 
	  LayoutGroup so = new LayoutGroup
	    ("SampleOptions", "Overall sampling controls.", true);
	  so.addEntry("SampleLock");
	  so.addEntry("Jitter");

	  aaq.addSubGroup(so);
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
	rq.addEntry("MaxTraceDepth");
	rq.addEntry("Shadows");
	rq.addSeparator(); 
	rq.addEntry("Scanline");
	rq.addEntry("Faces");
	
	layout.addSubGroup(rq);
      }

      {
	LayoutGroup mb = new LayoutGroup
	  ("MotionBlur", "Motion blur specific quality controls.", false);
	mb.addEntry("MotionBlur");
	mb.addSeparator(); 
	mb.addEntry("MotionBlurBy");
	mb.addSeparator(); 
	mb.addEntry("Shutter");
	mb.addEntry("ShutterDelay");
	mb.addSeparator(); 
	mb.addEntry("TimeContrastRed");
	mb.addEntry("TimeContrastGreen");
	mb.addEntry("TimeContrastBlue");
	mb.addEntry("TimeContrastAlpha");
	mb.addSeparator(); 
	mb.addEntry("MotionSteps");
	
	layout.addSubGroup(mb);
      }

      {
	LayoutGroup cs = new LayoutGroup
	  ("Caustics", "Caustic specific quality controls.", false);
	cs.addEntry("UseCaustics");
	cs.addSeparator(); 
	cs.addEntry("CausticsAccuracy");
	cs.addEntry("CausticsRadius");
	cs.addEntry("CausticFilterType");
	cs.addEntry("CausticFilterKernel");
	
	layout.addSubGroup(cs);
      }

      {
	LayoutGroup gi = new LayoutGroup
	  ("GlobalIllumination", "Global illumination specific quality controls.", false);
	gi.addEntry("UseGlobalIllum");
	gi.addSeparator(); 
	gi.addEntry("GlobalIllumAccuracy");
	gi.addEntry("GlobalIllumRadius");
	gi.addSeparator(); 
	gi.addEntry("PhotonVolumeAccuracy");
	gi.addEntry("PhotonVolumeRadius");
	gi.addEntry("MaxReflectionPhotons");
	gi.addEntry("MaxRefractionPhotons");
	gi.addEntry("MaxPhotonDepth");
	gi.addSeparator(); 
	gi.addEntry("PhotonMapFile");
	gi.addEntry("EnableGIMapVisualizer");
	gi.addEntry("PhotonMapRebuild");
	gi.addEntry("DirectIllumShadowEffects");
	
	layout.addSubGroup(gi);
      }

      {
	LayoutGroup fg = new LayoutGroup
	  ("FinalGather", "Final gathering specific quality controls.", false);
	fg.addEntry("UseFinalGather");
	fg.addSeparator(); 
	fg.addEntry("PrecompPhotonLookup");
	fg.addEntry("FinalGatherRays");
	fg.addEntry("MinRadius");
	fg.addEntry("MaxRadius");
	fg.addSeparator(); 
	fg.addEntry("Filter");
	fg.addEntry("FalloffStart");
	fg.addEntry("FalloffStop");
	fg.addEntry("TraceDepth");
	fg.addEntry("TraceReflection");
	fg.addEntry("TraceRefraction");
	fg.addSeparator(); 
	fg.addEntry("FinalGatherMapFile");
	fg.addEntry("EnableFGMapVisualizer");
	fg.addEntry("FinalGatherMapRebuild");
	fg.addEntry("PreviewFinalGather");
	
	layout.addSubGroup(fg);
      }

      {
	LayoutGroup fb = new LayoutGroup
	  ("FramebufferAttributes", "Output framebuffer controls.", false);
	fb.addEntry("DataType");
	fb.addEntry("Gamma");
	fb.addEntry("ColorClip");
	fb.addEntry("InterpSamples");
	fb.addEntry("Desaturate");
	fb.addEntry("Premultiply");
	fb.addEntry("Dither");
	fb.addSeparator(); 
	
	layout.addSubGroup(fb);
      }

      {
	LayoutGroup mp = new LayoutGroup
	  ("MemoryAndPerformance", "???", false);
	mp.addEntry("AccelerationMethod");
	mp.addEntry("BspSize");
	mp.addEntry("BspDepth");
	mp.addEntry("GridResolution");
	mp.addEntry("GridMaxSize");
	mp.addEntry("GridDepth");
	mp.addSeparator(); 
	mp.addEntry("TaskSize");
	
	{
	  LayoutGroup ml = new LayoutGroup
	    ("MemoryLimits", "???", true);
	  ml.addEntry("PhysicalMemory");

	  mp.addSubGroup(ml);
	}

	layout.addSubGroup(mp);
      }

      {
	LayoutGroup tn = new LayoutGroup
	  ("Translation", "Controls over the Maya to Mental Ray translation.", false);
	tn.addEntry("ExportVerbosity");
	tn.addEntry("ExportExactHierarchy");
	tn.addEntry("ExportFullDagpath");
	tn.addEntry("ExportTexturesFirst");
	tn.addSeparator(); 
	tn.addEntry("ExportParticles");
	tn.addEntry("ExportParticleInstances");
	tn.addEntry("ExportFluids");
	tn.addEntry("ExportPostEffects");
	
	layout.addSubGroup(tn);
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
	("The MRayRenderGlobals Action requires that primary target file sequence must " + 
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
	   "setAttr \"miDefaultFramebuffer.width\" " + width + ";\n" + 
	   "setAttr \"miDefaultFramebuffer.height\" " + height + ";\n" + 
	   "setAttr \"miDefaultFramebuffer.deviceAspectRatio\" " + deviceRatio + ";\n\n");
      }

      /* number of samples */ 
      {
	Integer minSamples = (Integer) getSingleParamValue("MinSampleLevel"); 
	if(minSamples == null) 
	  throw new PipelineException
	    ("The value of MinSampleLevel (" + minSamples + ") was illegal!");
	
	Integer maxSamples = (Integer) getSingleParamValue("MaxSampleLevel"); 
	if(maxSamples == null)
	  throw new PipelineException
	    ("The value of MaxSampleLevel (" + maxSamples + ") was illegal!");
	
	out.write
	  ("// NUMBER OF SAMPLES\n" + 
	   "setAttr \"miDefaultOptions.minSamples\" " + minSamples + ";\n" + 
	   "setAttr \"miDefaultOptions.maxSamples\" " + maxSamples + ";\n\n");
      }
      
      /* multi-pixel filtering */ 
      {
	int filter = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("PixelFilterType");
	  if(param != null) 
	    filter = param.getIndex();

	  if(filter == -1) 
	    throw new PipelineException
	      ("The PixelFilterType (" + param.getValue() + ") was illegal!"); 
	}

	Double filterX = (Double) getSingleParamValue("PixelFilterWidthX");
	if((filterX == null) || (filterX < 0.01) || (filterX > 5.0)) 
	  throw new PipelineException
	    ("The value of PixelFilterWidthX (" + filterX + ") was illegal!");
	
	Double filterY = (Double) getSingleParamValue("PixelFilterWidthY");
	if((filterY == null) || (filterY < 0.01) || (filterY > 5.0)) 
	  throw new PipelineException
	    ("The value of PixelFilterWidthY (" + filterY + ") was illegal!");
	
	out.write
	  ("// MULTI-PIXEL FILTERING\n" + 
	   "setAttr \"miDefaultOptions.filter\" " + filter + ";\n" + 
	   "setAttr \"miDefaultOptions.filterWidth\" " + filterX + ";\n" + 
	   "setAttr \"miDefaultOptions.filterHeight\" " + filterY + ";\n\n");	   
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
	   "setAttr \"miDefaultOptions.contrastR\" " + red + ";\n" + 
	   "setAttr \"miDefaultOptions.contrastG\" " + green + ";\n" + 
	   "setAttr \"miDefaultOptions.contrastB\" " + blue + ";\n" +
	   "setAttr \"miDefaultOptions.contrastA\" " + coverage + ";\n\n");
      }

      /* sample options */ 
      {
	Boolean lock = (Boolean) getSingleParamValue("SampleLock");  
	if(lock == null) 
	  throw new PipelineException
	    ("The SampleLock parameter was (null)!");

	Boolean jitter = (Boolean) getSingleParamValue("Jitter");  
	if(jitter == null) 
	  throw new PipelineException
	    ("The Jitter parameter was (null)!");

	out.write
	  ("// SAMPLE OPTIONS \n" +
	   "setAttr \"miDefaultOptions.sampleLock\" " + lock + ";\n" +
	   "setAttr \"miDefaultOptions.jitter\" " + jitter + ";\n\n");
      }

      /* raytracing quality */ 
      {
	Boolean useRay = (Boolean) getSingleParamValue("UseRaytracing");  
	if(useRay == null) 
	  throw new PipelineException
	    ("The UseRaytracing  parameter was (null)!");
	
	Integer reflect = (Integer) getSingleParamValue("Reflections"); 
	if(reflect == null) 
	  throw new PipelineException
	    ("The value of Reflections (" + reflect + ") was illegal!");
	
	Integer refract = (Integer) getSingleParamValue("Refractions"); 
	if(refract == null) 
	  throw new PipelineException
	    ("The value of Refractions (" + refract + ") was illegal!");
	
	Integer depth = (Integer) getSingleParamValue("MaxTraceDepth"); 
	if(depth == null) 
	  throw new PipelineException
	    ("The value of MaxTraceDepth (" + depth + ") was illegal!");
	
	Integer shadows = (Integer) getSingleParamValue("Shadows"); 
	if(shadows == null) 
	  throw new PipelineException
	    ("The value of Shadows (" + shadows + ") was illegal!");
	
	int scanline = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("Scanline");
	  if(param != null) 
	    scanline = param.getIndex();

	  if(scanline == -1) 
	    throw new PipelineException
	      ("The Scanline (" + param.getValue() + ") was illegal!"); 
	}

	int faces = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("Faces");
	  if(param != null) 
	    faces = param.getIndex();

	  if(faces == -1) 
	    throw new PipelineException
	      ("The Faces (" + param.getValue() + ") was illegal!"); 
	}
	
	out.write
	  ("// RAYTRACING QUALITY\n" +
	   "setAttr \"miDefaultOptions.rayTracing\" " + useRay + ";\n" +
	   "setAttr \"miDefaultOptions.maxReflectionRays\" " + reflect + ";\n" +
	   "setAttr \"miDefaultOptions.maxRefractionRays\"  " + refract + ";\n" +
	   "setAttr \"miDefaultOptions.maxRayDepth\"  " + depth + ";\n" +
	   "setAttr \"miDefaultOptions.maxShadowRayDepth\" " + shadows + ";\n" +
	   "setAttr \"miDefaultOptions.scanline\" " + scanline + ";\n" +
	   "setAttr \"miDefaultOptions.faces\" " + faces + ";\n\n");
      }

      /* motion blur */ 
      {
	int blur = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("MotionBlur");
	  if(param != null) 
	    blur = param.getIndex();

	  if(blur == -1) 
	    throw new PipelineException
	      ("The MotionBlur (" + param.getValue() + ") was illegal!"); 
	}
	
	Double blurBy = (Double) getSingleParamValue("MotionBlurBy");
	if(blurBy == null) 
	  throw new PipelineException
	    ("The value of MotionBlurBy (" + blurBy + ") was illegal!");

	Double shutter = (Double) getSingleParamValue("Shutter");
	if(shutter == null) 
	  throw new PipelineException
	    ("The value of Shutter (" + shutter + ") was illegal!");

	Double delay = (Double) getSingleParamValue("ShutterDelay");
	if(delay == null) 
	  throw new PipelineException
	    ("The value of ShutterDelay (" + delay + ") was illegal!");

	Double red = (Double) getSingleParamValue("TimeContrastRed");
	if(red == null) 
	  throw new PipelineException
	    ("The value of TimeContrastRed (" + red + ") was illegal!");

	Double green = (Double) getSingleParamValue("TimeContrastGreen");
	if(green == null) 
	  throw new PipelineException
	    ("The value of TimeContrastGreen (" + green + ") was illegal!");

	Double blue = (Double) getSingleParamValue("TimeContrastBlue");
	if(blue == null) 
	  throw new PipelineException
	    ("The value of TimeContrastBlue (" + blue + ") was illegal!");

	Double alpha = (Double) getSingleParamValue("TimeContrastAlpha");
	if(alpha == null) 
	  throw new PipelineException
	    ("The value of TimeContrastAlpha (" + alpha + ") was illegal!");

	Integer steps = (Integer) getSingleParamValue("MotionSteps"); 
	if(steps == null) 
	  throw new PipelineException
	    ("The value of MotionSteps (" + steps + ") was illegal!");
	
	out.write
	  ("// MOTION BLUR\n" +
	   "setAttr \"miDefaultOptions.motionBlur\" " + blur + ";\n" +
	   "setAttr \"miDefaultOptions.motionBlurBy\" " + blurBy + ";\n" +
	   "setAttr \"miDefaultOptions.shutter\" " + shutter + ";\n" +
	   "setAttr \"miDefaultOptions.shutterDelay\" " + delay + ";\n" +
	   "setAttr \"miDefaultOptions.timeContrastR\" " + red + ";\n" +
	   "setAttr \"miDefaultOptions.timeContrastG\" " + green + ";\n" +
	   "setAttr \"miDefaultOptions.timeContrastB\" " + blue + ";\n" +
	   "setAttr \"miDefaultOptions.timeContrastA\" " + alpha + ";\n" +
	   "setAttr \"miDefaultOptions.motionSteps\" " + steps + ";\n\n");
      }
      
      /* caustics */ 
      {
	Boolean useCaustics = (Boolean) getSingleParamValue("UseCaustics");  
	if(useCaustics == null) 
	  throw new PipelineException
	    ("The UseCaustics parameter was (null)!");
	
	Integer accuracy = (Integer) getSingleParamValue("CausticsAccuracy"); 
	if(accuracy == null) 
	  throw new PipelineException
	    ("The value of CausticsAccuracy (" + accuracy + ") was illegal!");
	
	Double radius = (Double) getSingleParamValue("CausticsRadius"); 
	if(radius == null) 
	  throw new PipelineException
	    ("The value of CausticsRadius (" + radius + ") was illegal!");
	
	int filter = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("CausticFilterType");
	  if(param != null) 
	    filter = param.getIndex();

	  if(filter == -1) 
	    throw new PipelineException
	      ("The CausticFilterType (" + param.getValue() + ") was illegal!"); 
	}

	Double kernel = (Double) getSingleParamValue("CausticFilterKernel"); 
	if(kernel == null) 
	  throw new PipelineException
	    ("The value of CausticFilterKernel (" + kernel + ") was illegal!");
	
	out.write
	  ("// CAUSTICS\n" +
	   "setAttr \"miDefaultOptions.caustics\" " + useCaustics + ";\n" +
	   "setAttr \"miDefaultOptions.causticAccuracy\" " + accuracy + ";\n" +
	   "setAttr \"miDefaultOptions.causticRadius\" " + radius + ";\n" +
	   "setAttr \"miDefaultOptions.causticFilterType\" " + filter + ";\n" +
	   "setAttr \"miDefaultOptions.causticFilterKernel\" " + kernel + ";\n\n");
      }

      /* global illumination */ 
      {
	Boolean useGlobalIllum = (Boolean) getSingleParamValue("UseGlobalIllum");  
	if(useGlobalIllum == null) 
	  throw new PipelineException
	    ("The UseGlobalIllum parameter was (null)!");
	
	Integer accuracy = (Integer) getSingleParamValue("GlobalIllumAccuracy"); 
	if(accuracy == null) 
	  throw new PipelineException
	    ("The value of GlobalIllumAccuracy (" + accuracy + ") was illegal!");
	
	Double radius = (Double) getSingleParamValue("GlobalIllumRadius"); 
	if(radius == null) 
	  throw new PipelineException
	    ("The value of GlobalIllumRadius (" + radius + ") was illegal!");
	
	Integer paccuracy = (Integer) getSingleParamValue("PhotonVolumeAccuracy"); 
	if(paccuracy == null) 
	  throw new PipelineException
	    ("The value of PhotonVolumeAccuracy (" + paccuracy + ") was illegal!");

	Double pradius = (Double) getSingleParamValue("PhotonVolumeRadius"); 
	if(pradius == null) 
	  throw new PipelineException
	    ("The value of PhotonVolumeRadius (" + pradius + ") was illegal!");

	Integer reflect = (Integer) getSingleParamValue("MaxReflectionPhotons"); 
	if(reflect == null) 
	  throw new PipelineException
	    ("The value of MaxReflectionPhotons (" + reflect + ") was illegal!");

	Integer refract = (Integer) getSingleParamValue("MaxRefractionPhotons"); 
	if(refract == null) 
	  throw new PipelineException
	    ("The value of MaxRefractionPhotons (" + refract + ") was illegal!");

	Integer depth = (Integer) getSingleParamValue("MaxPhotonDepth"); 
	if(depth == null) 
	  throw new PipelineException
	    ("The value of MaxPhotonDepth (" + depth + ") was illegal!");

	String file = (String) getSingleParamValue("PhotonMapFile"); 
	if(file == null) 
	  file = "";
	
	Boolean mapvis = (Boolean) getSingleParamValue("EnableGIMapVisualizer");  
	if(mapvis == null) 
	  throw new PipelineException
	    ("The EnableGIMapVisualizer parameter was (null)!");
	
	Boolean rebuild = (Boolean) getSingleParamValue("PhotonMapRebuild");  
	if(rebuild == null) 
	  throw new PipelineException
	    ("The PhotonMapRebuild parameter was (null)!");

	Boolean direct = (Boolean) getSingleParamValue("DirectIllumShadowEffects");  
	if(direct == null) 
	  throw new PipelineException
	    ("The DirectIllumShadowEffects parameter was (null)!");
	
	out.write
	  ("// GLOBAL ILLUMINATION\n" +
	   "setAttr \"miDefaultOptions.globalIllum\" " + useGlobalIllum + ";\n" +
	   "setAttr \"miDefaultOptions.globalIllumAccuracy\" " + accuracy + ";\n" +
	   "setAttr \"miDefaultOptions.globalIllumRadius\" " + radius + ";\n" +
	   "setAttr \"miDefaultOptions.photonVolumeAccuracy\" " + paccuracy + ";\n" +
	   "setAttr \"miDefaultOptions.photonVolumeRadius\" " + pradius + ";\n" +
	   "setAttr \"miDefaultOptions.maxReflectionPhotons\" " + reflect + ";\n" +
	   "setAttr \"miDefaultOptions.maxRefractionPhotons\" " + refract + ";\n" +
	   "setAttr \"miDefaultOptions.maxPhotonDepth\" " + depth + ";\n" +
	   "setAttr -type \"string\" \"miDefaultOptions.photonMapFilename\" " + 
	   "\"" + file + "\";\n" +
	   "setAttr \"miDefaultOptions.photonMapVisualizer\" " + mapvis + ";\n" +
	   "setAttr \"miDefaultOptions.photonMapRebuild\" " + rebuild + ";\n" + 
	   "setAttr \"mentalrayGlobals.shadowEffectsWithPhotons\" " + direct + ";\n\n");
      }

      /* final gather */ 
      {
	Boolean useFinalGather = (Boolean) getSingleParamValue("UseFinalGather");  
	if(useFinalGather == null) 
	  throw new PipelineException
	    ("The UseFinalGather parameter was (null)!");

	Boolean precomp = (Boolean) getSingleParamValue("PrecompPhotonLookup");  
	if(precomp == null) 
	  throw new PipelineException
	    ("The PrecompPhotonLookup parameter was (null)!");

	Integer rays = (Integer) getSingleParamValue("FinalGatherRays"); 
	if(rays == null) 
	  throw new PipelineException
	    ("The value of FinalGatherRays (" + rays + ") was illegal!");
	
	Double minRadius = (Double) getSingleParamValue("MinRadius"); 
	if(minRadius == null) 
	  throw new PipelineException
	    ("The value of MinRadius (" + minRadius + ") was illegal!");

	Double maxRadius = (Double) getSingleParamValue("MaxRadius"); 
	if(maxRadius == null) 
	  throw new PipelineException
	    ("The value of MaxRadius (" + maxRadius + ") was illegal!");

	Integer filter = (Integer) getSingleParamValue("Filter"); 
	if(filter == null) 
	  throw new PipelineException
	    ("The value of Filter (" + filter + ") was illegal!");
	
	Double falloffStart = (Double) getSingleParamValue("FalloffStart"); 
	if(falloffStart == null) 
	  throw new PipelineException
	    ("The value of FalloffStart (" + falloffStart + ") was illegal!");

	Double falloffStop = (Double) getSingleParamValue("FalloffStop"); 
	if(falloffStop == null) 
	  throw new PipelineException
	    ("The value of FalloffStop (" + falloffStop + ") was illegal!");
	
	Integer depth = (Integer) getSingleParamValue("TraceDepth"); 
	if(depth == null) 
	  throw new PipelineException
	    ("The value of TraceDepth (" + depth + ") was illegal!");
	
	Integer reflect = (Integer) getSingleParamValue("TraceReflection"); 
	if(reflect == null) 
	  throw new PipelineException
	    ("The value of TraceReflection (" + reflect + ") was illegal!");

	Integer refract = (Integer) getSingleParamValue("TraceRefraction"); 
	if(refract == null) 
	  throw new PipelineException
	    ("The value of TraceRefraction (" + refract + ") was illegal!");
	
	String file = (String) getSingleParamValue("FinalGatherMapFile"); 
	if(file == null) 
	  file = "";
	
	Boolean mapvis = (Boolean) getSingleParamValue("EnableFGMapVisualizer");  
	if(mapvis == null) 
	  throw new PipelineException
	    ("The EnableFGMapVisualizer parameter was (null)!");
	
	Boolean rebuild = (Boolean) getSingleParamValue("FinalGatherMapRebuild");  
	if(rebuild == null) 
	  throw new PipelineException
	    ("The FinalGatherMapRebuild parameter was (null)!");

	Boolean preview = (Boolean) getSingleParamValue("PreviewFinalGather");  
	if(preview == null) 
	  throw new PipelineException
	    ("The PreviewFinalGather parameter was (null)!");
	
	out.write
	  ("// FINAL GATHER\n" +
	   "setAttr \"miDefaultOptions.finalGather\" " + useFinalGather + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherFast\" " + precomp + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherRays\" " + rays + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherMinRadius\" " + minRadius + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherMaxRadius\" " + maxRadius + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherFilter\" " + filter + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherFalloffStart\" " + falloffStart + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherFalloffStop\" " + falloffStop + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherTraceDepth\" " + depth + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherTraceReflection\" " + reflect + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherTraceRefraction\" " + refract + ";\n" +
	   "setAttr -type \"string\" \"miDefaultOptions.finalGatherFilename\" " + 
	   "\"" + file + "\";\n" +
	   "setAttr \"miDefaultOptions.finalGatherMapVisualizer\" " + mapvis + ";\n" +
	   "setAttr \"miDefaultOptions.finalGatherRebuild\" " + rebuild + ";\n" +
	   "setAttr \"mentalrayGlobals.previewFinalGatherTiles\" " + preview + ";\n\n");
      }

      /* framebuffer attributes */ 
      {
	int dtype = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("DataType");
	  if(param != null) 
	    dtype = param.getIndex();

	  if((dtype < 0) || (dtype > 17)) 
	    throw new PipelineException
	      ("The DataType (" + param.getValue() + ") was illegal!"); 
	  else {
	    int indexMap[] = { 0, 1, 17, 4, 2, 14, 3, 16, 5, 6, 7, 15, 8, 13, 9, 10, 11, 12 };
	    dtype = indexMap[dtype];
	  }
	}

	Double gamma = (Double) getSingleParamValue("Gamma"); 
	if(gamma == null) 
	  throw new PipelineException
	    ("The value of Gamma (" + gamma + ") was illegal!");
	
	int clip = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("ColorClip");
	  if(param != null) 
	    clip = param.getIndex();

	  if(clip == -1) 
	    throw new PipelineException
	      ("The ColorClip (" + param.getValue() + ") was illegal!"); 
	}

	Boolean interp = (Boolean) getSingleParamValue("InterpSamples");  
	if(interp == null) 
	  throw new PipelineException
	    ("The InterpSamples parameter was (null)!");
	
	Boolean desaturate = (Boolean) getSingleParamValue("Desaturate");  
	if(desaturate == null) 
	  throw new PipelineException
	    ("The Desaturate parameter was (null)!");

	Boolean premult = (Boolean) getSingleParamValue("Premultiply");  
	if(premult == null) 
	  throw new PipelineException
	    ("The Premultiply parameter was (null)!");
	
	Boolean dither = (Boolean) getSingleParamValue("Dither");  
	if(dither == null) 
	  throw new PipelineException
	    ("The Dither parameter was (null)!");
	
	out.write
	  ("// FRAMEBUFFER ATTRIBUTES\n" + 
	   "setAttr \"miDefaultFramebuffer.datatype\" " + dtype + ";\n" +
	   "setAttr \"miDefaultFramebuffer.gamma\" " + gamma + ";\n" +
	   "setAttr \"miDefaultFramebuffer.colorclip\" " + clip + ";\n" +
	   "setAttr \"miDefaultFramebuffer.interpolateSamples\" " + interp + ";\n" +
	   "setAttr \"miDefaultFramebuffer.desaturate\" " + desaturate + ";\n" +
	   "setAttr \"miDefaultFramebuffer.premultiply\" " + premult + ";\n" +
	   "setAttr \"miDefaultFramebuffer.dither\" " + dither + ";\n\n");
      }

      /* memory and performance */ 
      {
	int method = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("AccelerationMethod");
	  if(param != null) {
	    switch(param.getIndex()) {
	    case 0:
	      method = 0;
	      break;

	    case 1:
	      method = 1;
	      break;

	    case 2:
	      method = 3;
	    }
	  }

	  if(method == -1) 
	    throw new PipelineException
	      ("The AccelerationMethod (" + param.getValue() + ") was illegal!"); 
	}

	Integer bspSize = (Integer) getSingleParamValue("BspSize"); 
	if(bspSize == null) 
	  throw new PipelineException
	    ("The value of BspSize (" + bspSize + ") was illegal!");
	
	Integer bspDepth = (Integer) getSingleParamValue("BspDepth"); 
	if(bspDepth == null) 
	  throw new PipelineException
	    ("The value of BspDepth (" + bspDepth + ") was illegal!");
	
	Integer gridRes = (Integer) getSingleParamValue("GridResolution"); 
	if(gridRes == null) 
	  throw new PipelineException
	    ("The value of GridResolution (" + gridRes + ") was illegal!");
	
	Integer gridSize = (Integer) getSingleParamValue("GridMaxSize"); 
	if(gridSize == null) 
	  throw new PipelineException
	    ("The value of GridMaxSize (" + gridSize + ") was illegal!");
	
	Integer gridDepth = (Integer) getSingleParamValue("GridDepth"); 
	if(gridDepth == null) 
	  throw new PipelineException
	    ("The value of GridDepth (" + gridDepth + ") was illegal!");
	
	Integer taskSize = (Integer) getSingleParamValue("TaskSize"); 
	if(taskSize == null) 
	  throw new PipelineException
	    ("The value of TaskSize (" + taskSize + ") was illegal!");
	
	Integer memory = (Integer) getSingleParamValue("PhysicalMemory"); 
	if(memory == null) 
	  throw new PipelineException
	    ("The value of PhysicalMemory (" + memory + ") was illegal!");

	out.write
	  ("// MEMORY AND PERFORMANCE ATTRIBUTES\n" + 
	   "setAttr \"mentalrayGlobals.accelerationMethod\" " + method + ";\n" +
	   "setAttr \"mentalrayGlobals.bspSize\" " + bspSize + ";\n" +
	   "setAttr \"mentalrayGlobals.bspDepth\" " + bspDepth + ";\n" +
	   "setAttr \"mentalrayGlobals.gridResolution\" " + gridRes + ";\n" +
	   "setAttr \"mentalrayGlobals.gridMaxSize\" " + gridSize + ";\n" +
	   "setAttr \"mentalrayGlobals.gridDepth\" " + gridDepth + ";\n" +
	   "setAttr \"mentalrayGlobals.taskSize\" " + taskSize + ";\n" +
	   "setAttr \"mentalrayGlobals.jlpm\" " + memory + ";\n\n");
      }

      /* translation */ 
      {
	int verbose = -1;
	{
	  EnumActionParam param = (EnumActionParam) getSingleParam("ExportVerbosity");
	  if(param != null) 
	    verbose = param.getIndex();

	  if(verbose == -1) 
	    throw new PipelineException
	      ("The ExportVerbosity was illegal!"); 
	}

	Boolean hier = (Boolean) getSingleParamValue("ExportExactHierarchy");  
	if(hier == null) 
	  throw new PipelineException
	    ("The ExportExactHierarchy parameter was (null)!");

	Boolean dag = (Boolean) getSingleParamValue("ExportFullDagpath");  
	if(dag == null) 
	  throw new PipelineException
	    ("The ExportFullDagpath parameter was (null)!");
	
	Boolean textures = (Boolean) getSingleParamValue("ExportTexturesFirst");  
	if(textures == null) 
	  throw new PipelineException
	    ("The ExportTexturesFirst parameter was (null)!");
	
	Boolean particles = (Boolean) getSingleParamValue("ExportParticles");  
	if(particles == null) 
	  throw new PipelineException
	    ("The ExportParticles parameter was (null)!");
	
	Boolean pinst = (Boolean) getSingleParamValue("ExportParticleInstances");  
	if(pinst == null) 
	  throw new PipelineException
	    ("The ExportParticleInstances parameter was (null)!");
	
	Boolean fluids = (Boolean) getSingleParamValue("ExportFluids");  
	if(fluids == null) 
	  throw new PipelineException
	    ("The ExportFluids parameter was (null)!");
	
	Boolean effects = (Boolean) getSingleParamValue("ExportPostEffects");  
	if(effects == null) 
	  throw new PipelineException
	    ("The ExportPostEffects parameter was (null)!");
	
	out.write
	  ("// TRANSLATION\n" + 
	   "setAttr \"mentalrayGlobals.exportVerbosity\" " + verbose + ";\n" +
	   "setAttr \"mentalrayGlobals.exportExactHierarchy\" " + hier + ";\n" +
	   "setAttr \"mentalrayGlobals.exportFullDagpath\" " + dag + ";\n" +
	   "setAttr \"mentalrayGlobals.exportTexturesFirst\" " + textures + ";\n" +
	   "setAttr \"mentalrayGlobals.exportParticles\" " + particles + ";\n" +
	   "setAttr \"mentalrayGlobals.exportParticleInstances\" " + pinst + ";\n" +
	   "setAttr \"mentalrayGlobals.exportFluids\" " + fluids + ";\n" +
	   "setAttr \"mentalrayGlobals.exportPostEffects\" " + effects + ";\n\n");
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

  private static final long serialVersionUID = -539818250460223624L;

}

