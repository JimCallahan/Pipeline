// $Id: MRayRenderGlobalsAction.java,v 1.2 2007/08/21 13:03:51 jesse Exp $

package us.temerity.pipeline.plugin.MRayRenderGlobalsAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 
import us.temerity.pipeline.math.Range; 

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
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayRenderGlobalsAction() 
  {
    super("MRayRenderGlobals", new VersionID("2.2.1"), "Temerity",
	  "Creates a MEL script which sets the Mental Ray render globals of a Maya scene.");

    /* image resolution */ 
    {
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aImageWidth,
	   "The horizontal resolution of the output image in pixels.", 
	   640);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aImageHeight,
	   "The vertical resolution of the output image in pixels.", 
	   480);
	addSingleParam(param);
      }
    
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aPixelAspectRatio,
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

	addPreset(aImageResolution, choices);

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       320);
	  values.put(aImageHeight,      240);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "320x240", values);
	}
      
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       640);
	  values.put(aImageHeight,      480);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "640x480", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      1024);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "1k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       2048);
	  values.put(aImageHeight,      2048);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "2k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       3072);
	  values.put(aImageHeight,      3072);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "3k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      4096);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "4k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       720);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 1.066);
	
	  addPresetValues(aImageResolution, "CCIR PAL/Quantel PAL", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       720);
	  values.put(aImageHeight,      486);
	  values.put(aPixelAspectRatio, 0.900);
	
	  addPresetValues(aImageResolution, "CCIR 601/Quantel NTSC", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      768);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "Full 1024", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1280);
	  values.put(aImageHeight,      1024);
	  values.put(aPixelAspectRatio, 1.066);
	
	  addPresetValues(aImageResolution, "Full 1280/Screen", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1280);
	  values.put(aImageHeight,      720);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "HD 720", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1920);
	  values.put(aImageHeight,      1080);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "HD 1080", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       646);
	  values.put(aImageHeight,      485);
	  values.put(aPixelAspectRatio, 1.001);
	
	  addPresetValues(aImageResolution, "NTSC 4d", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       768);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "PAL 768", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       780);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 0.984);
	
	  addPresetValues(aImageResolution, "PAL 780", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      486);
	  values.put(aPixelAspectRatio, 1.265);
	
	  addPresetValues(aImageResolution, "Targa 486", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      482);
	  values.put(aPixelAspectRatio, 1.255);
	
	  addPresetValues(aImageResolution, "Target NTSC", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 1.500);
	
	  addPresetValues(aImageResolution, "Targa PAL", values);
	}
      }
    }

    /* render quality */ 
    {
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aMinSampleLevel,
	   "The minimum sample rate, each pixel is sampled at least 2^(2*rate).", 
	   -2);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aMaxSampleLevel,
	   "The maximim sample rate, each pixel is sampled at most 2^(2*rate).", 
	   0);
	addSingleParam(param);
      }

      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Box Filter");
	choices.add("Triangle Filter");
	choices.add("Gaussian Filter");
	choices.add("Mitchell Filter");
	choices.add("Lanczos Filter");

	ActionParam param = 
	  new EnumActionParam
	  (aPixelFilterType,
	   "The type of filter used to integrate pixels.", 
	   "Triangle Filter", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aPixelFilterWidthX,
	   "The horizontal pixel filter width.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aPixelFilterWidthY,
	   "The vertical pixel filter width.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aRedThreshold,
	   "The red contrast threshold.", 
	   0.100);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aGreenThreshold,
	   "The green contrast threshold.", 
	   0.100);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aBlueThreshold,
	   "The blue contrast threshold.", 
	   0.100);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aCoverageThreshold,
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
	  (aSampleLock,
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aJitter,
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
	  (aUseRaytracing,
	   "Whether to enable raytracing.",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aReflections,
	   "The reflected ray depth.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aRefractions,
	   "The refracted ray depth.", 
	   1);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aMaxTraceDepth,
	   "The maximum ray depth.", 
	   2);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aShadows,
	   "The shadow ray depth.", 
	   2);
	addSingleParam(param);
      }
      
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Off");
	choices.add("On");
	choices.add("OpenGL");
	choices.add("Rapid");

	ActionParam param = 
	  new EnumActionParam
	  (aScanline,
	   "Controls the use of scanline rendering.", 
	   "On", choices);
	addSingleParam(param);
      }

      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Front");
	choices.add("Back");
	choices.add("Both");

	ActionParam param = 
	  new EnumActionParam
	  (aFaces,
	   "Controls which side(s) of triangles are rendered.", 
	   "Both", choices);
	addSingleParam(param);
      }
    }

    /* motion blur */ 
    {
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Off");
	choices.add("Linear");
	choices.add("Exact");

	ActionParam param = 
	  new EnumActionParam
	  (aMotionBlur,
	   "Controls the motion blur technique.", 
	   "Off", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aMotionBlurBy,
	   "???", 
	   1.0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aShutter,
	   "The time when the shutter closes.", 
	   1.0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aShutterDelay,
	   "The time when the shutter opens.", 
	   0.0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aTimeContrastRed,
	   "The maximum temporal contrast in the red channel.", 
	   0.200);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aTimeContrastGreen,
	   "The maximum temporal contrast in the green channel.", 
	   0.200);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aTimeContrastBlue,
	   "The maximum temporal contrast in the blue channel.", 
	   0.200);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aTimeContrastAlpha,
	   "The maximum temporal contrast in the alpha channel.", 
	   0.200);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aMotionSteps,
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
	  (aUseCaustics,
	   "Whether to enable caustics.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aCausticsAccuracy,
	   "The number of photons used to estimate caustics during rendering.", 
	   64);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aCausticsRadius,
	   "The maximum radius to be used when picking up caustic photons.", 
	   0.0);
	addSingleParam(param);
      }
      
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Box Filter");
	choices.add("Cone Filter");

	ActionParam param = 
	  new EnumActionParam
	  (aCausticFilterType,
	   "The type of filter used by caustics.", 
	   "Box Filter", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aCausticFilterKernel,
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
	  (aUseGlobalIllum,
	   "Whether to enable global illumination.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aGlobalIllumAccuracy,
	   "The number of photons used to estimate global illumination during rendering.", 
	   64);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aGlobalIllumRadius,
	   "The maximum radius to be used when picking up global illumination photons.",
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aPhotonVolumeAccuracy,
	   "The maximum number of photons to examine in participating media.", 
	   64);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aPhotonVolumeRadius,
	   "The maximum radius to search for photons in participating media.", 
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aMaxReflectionPhotons,
	   "The reflected photon depth.", 
	   5);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aMaxRefractionPhotons,
	   "The refracted photon depth.", 
	   5);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aMaxPhotonDepth,
	   "The maximum photon depth.", 
	   5);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new StringActionParam
	  (aPhotonMapFile,
	   "The name of the file to save/reuse photons.", 
	   "");
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aEnableGIMapVisualizer,
	   "Produce a false-color rendering of photon density.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aPhotonMapRebuild,
	   "Ignore and overwrite the photon map file if it exists.",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aDirectIllumShadowEffects,
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
	  (aUseFinalGather,
	   "Whether to enable final gathering.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aPrecompPhotonLookup,
	   "Whether to store final gather irradiance in photon map.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aFinalGatherRays,
	   "The number of final gathering rays to cast.",
	   1000);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aMinRadius,
	   "The minimum distance within which final gather results must be reused.", 
	   0.0);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aMaxRadius,
	   "The maximum distance within which a final gather result can be reused.", 
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aFilter,
	   "The number of neighboring samples to include in the speckle elimination filter.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aFalloffStart,
	   "The distance at which final gather rays start blending with the env color.", 
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aFalloffStop,
	   "The distance at which final gather rays completely blend with the env color.", 
	   0.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aTraceDepth,
	   "The maximum final gather ray depth.", 
	   2);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aTraceReflection,
	   "The reflected final gather ray depth.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aTraceRefraction,
	   "The refracted final gather ray depth.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new StringActionParam
	  (aFinalGatherMapFile,
	   "The name of the file to save/reuse final gather rays.", 
	   "");
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aEnableFGMapVisualizer,
	   "Produce a false-color rendering of final gather ray density.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aFinalGatherMapRebuild,
	   "Ignore and overwrite the final gather map file if it exists.",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aPreviewFinalGather,
	   "Shows diagnostic final gathering points in output image.",
	   true);
	addSingleParam(param);
      }
    }

    /* framebuffer attributes */ 
    {
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("RGB (Byte) 3x8");
	choices.add("RGB (Short) 3x16");
	choices.add("RGB (Half) 3x16");
	choices.add("RGB (Float) 3x32");
	choices.add("RGBA (Byte) 4x8");
	choices.add("RGBE (Byte) 4x8");
	choices.add("RGBA (Short) 4x16");
	choices.add("RGBA (Half) 4x16");
	choices.add("RGBA (Float) 4x32");
	choices.add("Alpha (Byte) 1x8");
	choices.add("Alpha (Short) 1x16");
	choices.add("Alpha (Float) 1x32");
	choices.add("Depth (Float) 1x32");
	choices.add("Coverage (Float) 1x32");
	choices.add("Normal (Float) 3x32");
	choices.add("Motion (Float) 3x32");
	choices.add("Label (Integer) 1x32");
	choices.add("Bit");

	ActionParam param = 
	  new EnumActionParam
	  (aDataType,
	   "The output data format.", 
	   "RGBA (Byte) 4x8", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aGamma,
	   "The gamma correction factor.", 
	   1.0);
	addSingleParam(param);
      }
      
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("RGB");
	choices.add("Alpha");
	choices.add("Raw");
      
	ActionParam param = 
	  new EnumActionParam
	  (aColorClip,
	   "Pre-quantization color clipping method.", 
	   "Raw", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aInterpSamples,
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aDesaturate,
	   "Bleach out clipped colors to maintain percieved brightness.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aPremultiply,
	   "Whether to premultiply alpha into color channels.",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aDither,
	   "Whether to enable dithering.",
	   true);
	addSingleParam(param);
      }
    }

    /* memory and performance */ 
    {
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("BSP");
	choices.add("Grid");
	choices.add("Large BSP");

	ActionParam param = 
	  new EnumActionParam
	  (aAccelerationMethod,
	   "The algorithm used to acceleration ray testing.", 
	   "BSP", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aBspSize,
	   "???", 
	   10);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aBspDepth,
	   "???", 
	   40);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aGridResolution,
	   "???", 
	   2);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aGridMaxSize,
	   "???", 
	   128);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aGridDepth,
	   "???", 
	   2);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aTaskSize,
	   "???", 
	   0);
	addSingleParam(param);
      }

      /* memory limits */ 
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aPhysicalMemory,
	   "???", 
	   800);
	addSingleParam(param);
      }
    }

    /* translation */ 
    {
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("No Messages");
	choices.add("Fatal Messages Only");
	choices.add("Error Messages");
	choices.add("Warning Messages");
	choices.add("Info Messages");
	choices.add("Progress Messages");
	choices.add("Details Messages");

	ActionParam param = 
	  new EnumActionParam
	  (aExportVerbosity,
	   "The verbosity of export messages.", 
	   "Warning Messages", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportExactHierarchy,
	   "???",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportFullDagpath,
	   "???",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportTexturesFirst,
	   "???",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportParticles,
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportParticleInstances,
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportFluids,
	   "???",
	   true);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportPostEffects,
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
      
      addPreset(aQuality, choices);
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     -2);
	values.put(aMaxSampleLevel,     0);
	values.put(aPixelFilterType,    "Box Filter");
	values.put(aPixelFilterWidthX,  1.0);
	values.put(aPixelFilterWidthY,  1.0);
	values.put(aRedThreshold,       0.1);
	values.put(aGreenThreshold,     0.1);
	values.put(aBlueThreshold,      0.1);
	values.put(aJitter,             false);

	values.put(aReflections,        1);
	values.put(aRefractions,        1);
	values.put(aMaxTraceDepth,      2);

	values.put(aMotionBlur,         "Off");
	values.put(aTimeContrastRed,    1.0);
	values.put(aTimeContrastGreen,  1.0);
	values.put(aTimeContrastBlue,   1.0);
	values.put(aTimeContrastAlpha,  1.0);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     false);
	values.put(aUseFinalGather,     false);

	addPresetValues(aQuality, "Draft", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     -2);
	values.put(aMaxSampleLevel,     0);
	values.put(aPixelFilterType,    "Box Filter");
	values.put(aPixelFilterWidthX,  1.0);
	values.put(aPixelFilterWidthY,  1.0);
	values.put(aRedThreshold,       0.1);
	values.put(aGreenThreshold,     0.1);
	values.put(aBlueThreshold,      0.1);
	values.put(aJitter,             false);

	values.put(aReflections,        1);
	values.put(aRefractions,        1);
	values.put(aMaxTraceDepth,      2);

	values.put(aMotionBlur,         "Linear");
	values.put(aTimeContrastRed,    1.0);
	values.put(aTimeContrastGreen,  1.0);
	values.put(aTimeContrastBlue,   1.0);
	values.put(aTimeContrastAlpha,  1.0);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     false);
	values.put(aUseFinalGather,     false);

	addPresetValues(aQuality, "Draft Motion Blur", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     -1);
	values.put(aMaxSampleLevel,     1);
	values.put(aPixelFilterType,    "Triangle Filter");
	values.put(aPixelFilterWidthX,  2.0);
	values.put(aPixelFilterWidthY,  2.0);
	values.put(aRedThreshold,       0.1);
	values.put(aGreenThreshold,     0.1);
	values.put(aBlueThreshold,      0.1);
	values.put(aJitter,             false);

	values.put(aReflections,        2);
	values.put(aRefractions,        2);
	values.put(aMaxTraceDepth,      4);

	values.put(aMotionBlur,         "Off");
	values.put(aTimeContrastRed,    1.0);
	values.put(aTimeContrastGreen,  1.0);
	values.put(aTimeContrastBlue,   1.0);
	values.put(aTimeContrastAlpha,  1.0);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     false);
	values.put(aUseFinalGather,     false);

	addPresetValues(aQuality, "Preview", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     -1);
	values.put(aMaxSampleLevel,     1);
	values.put(aPixelFilterType,    "Triangle Filter");
	values.put(aPixelFilterWidthX,  2.0);
	values.put(aPixelFilterWidthY,  2.0);
	values.put(aJitter,             false);

	values.put(aReflections,        2);
	values.put(aRefractions,        2);
	values.put(aMaxTraceDepth,      4);

	values.put(aMotionBlur,         "Off");
	values.put(aTimeContrastRed,    1.0);
	values.put(aTimeContrastGreen,  1.0);
	values.put(aTimeContrastBlue,   1.0);
	values.put(aTimeContrastAlpha,  1.0);

	values.put(aUseCaustics,        true);
	values.put(aUseGlobalIllum,     false);
	values.put(aUseFinalGather,     false);

	addPresetValues(aQuality, "Preview Caustics", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     -1);
	values.put(aMaxSampleLevel,     1);
	values.put(aPixelFilterType,    "Triangle Filter");
	values.put(aPixelFilterWidthX,  2.0);
	values.put(aPixelFilterWidthY,  2.0);
	values.put(aRedThreshold,       0.1);
	values.put(aGreenThreshold,     0.1);
	values.put(aBlueThreshold,      0.1);
	values.put(aJitter,             false);

	values.put(aReflections,        2);
	values.put(aRefractions,        2);
	values.put(aMaxTraceDepth,      4);

	values.put(aMotionBlur,         "Off");
	values.put(aTimeContrastRed,    1.0);
	values.put(aTimeContrastGreen,  1.0);
	values.put(aTimeContrastBlue,   1.0);
	values.put(aTimeContrastAlpha,  1.0);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     true);
	values.put(aUseFinalGather,     true);

	addPresetValues(aQuality, "Preview Final Gather", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     -1);
	values.put(aMaxSampleLevel,     1);
	values.put(aPixelFilterType,    "Triangle Filter");
	values.put(aPixelFilterWidthX,  2.0);
	values.put(aPixelFilterWidthY,  2.0);
	values.put(aRedThreshold,       0.1);
	values.put(aGreenThreshold,     0.1);
	values.put(aBlueThreshold,      0.1);
	values.put(aJitter,             false);

	values.put(aReflections,        2);
	values.put(aRefractions,        2);
	values.put(aMaxTraceDepth,      4);

	values.put(aMotionBlur,         "Off");
	values.put(aTimeContrastRed,    1.0);
	values.put(aTimeContrastGreen,  1.0);
	values.put(aTimeContrastBlue,   1.0);
	values.put(aTimeContrastAlpha,  1.0);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     true);
	values.put(aUseFinalGather,     false);

	addPresetValues(aQuality, "Preview Global Illum", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     1);
	values.put(aMaxSampleLevel,     2);
	values.put(aPixelFilterType,    "Triangle Filter");
	values.put(aPixelFilterWidthX,  1.5);
	values.put(aPixelFilterWidthY,  1.5); 
	values.put(aRedThreshold,       0.040);
	values.put(aGreenThreshold,     0.030);
	values.put(aBlueThreshold,      0.060);
	values.put(aJitter,             false);

	values.put(aReflections,        2);
	values.put(aRefractions,        2);
	values.put(aMaxTraceDepth,      4);

	values.put(aMotionBlur,         "Off");
	values.put(aTimeContrastRed,    1.0);
	values.put(aTimeContrastGreen,  1.0);
	values.put(aTimeContrastBlue,   1.0);
	values.put(aTimeContrastAlpha,  1.0);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     true);
	values.put(aUseFinalGather,     false);

	addPresetValues(aQuality, "Preview Fur", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     -1);
	values.put(aMaxSampleLevel,     1);
	values.put(aPixelFilterType,    "Triangle Filter");
	values.put(aPixelFilterWidthX,  2.0);
	values.put(aPixelFilterWidthY,  2.0);
	values.put(aRedThreshold,       0.1);
	values.put(aGreenThreshold,     0.1);
	values.put(aBlueThreshold,      0.1);
	values.put(aJitter,             false);

	values.put(aReflections,        2);
	values.put(aRefractions,        2);
	values.put(aMaxTraceDepth,      4);

	values.put(aMotionBlur,         "Linear");
	values.put(aTimeContrastRed,    0.500);
	values.put(aTimeContrastGreen,  0.500);
	values.put(aTimeContrastBlue,   0.500);
	values.put(aTimeContrastAlpha,  0.500);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     false);
	values.put(aUseFinalGather,     false);	

	addPresetValues(aQuality, "Preview Motion Blur", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     0);
	values.put(aMaxSampleLevel,     2);
	values.put(aPixelFilterType,    "Gaussian Filter");
	values.put(aPixelFilterWidthX,  3.0);
	values.put(aPixelFilterWidthY,  3.0);
	values.put(aRedThreshold,       0.1);
	values.put(aGreenThreshold,     0.1);
	values.put(aBlueThreshold,      0.1);
	values.put(aJitter,             false);

	values.put(aReflections,        10);
	values.put(aRefractions,        10);
	values.put(aMaxTraceDepth,      20);

	values.put(aMotionBlur,         "Off");
	values.put(aTimeContrastRed,    0.200);
	values.put(aTimeContrastGreen,  0.200);
	values.put(aTimeContrastBlue,   0.200);
	values.put(aTimeContrastAlpha,  0.200);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     false);
	values.put(aUseFinalGather,     false);	
	
	addPresetValues(aQuality, "Production", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put(aMinSampleLevel,     0);
	values.put(aMaxSampleLevel,     2);
	values.put(aPixelFilterType,    "Gaussian Filter");
	values.put(aPixelFilterWidthX,  3.0);
	values.put(aPixelFilterWidthY,  3.0);
	values.put(aRedThreshold,       0.1);
	values.put(aGreenThreshold,     0.1);
	values.put(aBlueThreshold,      0.1);
	values.put(aJitter,             false);

	values.put(aReflections,        10);
	values.put(aRefractions,        10);
	values.put(aMaxTraceDepth,      20);

	values.put(aMotionBlur,         "Linear");
	values.put(aTimeContrastRed,    0.200);
	values.put(aTimeContrastGreen,  0.200);
	values.put(aTimeContrastBlue,   0.200);
	values.put(aTimeContrastAlpha,  0.200);

	values.put(aUseCaustics,        false);
	values.put(aUseGlobalIllum,     false);
	values.put(aUseFinalGather,     false);	

	addPresetValues(aQuality, "Production Motion Blur", values);
      }
    }

    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageResolution);
      layout.addEntry(aImageWidth);
      layout.addEntry(aImageHeight);
      layout.addEntry(aPixelAspectRatio);
      
      {
	LayoutGroup aaq = new LayoutGroup
	  ("RenderQuality", "The overall rendering quality controls.", false);
	aaq.addEntry(aQuality);
	
	{
	  LayoutGroup nos = new LayoutGroup
	    ("NumberOfSamples", "The ray sampling controls.", true);
	  nos.addEntry(aMinSampleLevel);
	  nos.addEntry(aMaxSampleLevel);

	  aaq.addSubGroup(nos);
	}

	{ 
	  LayoutGroup mpf = new LayoutGroup
	    ("MultiPixelFiltering", "Output pixel filtering controls.", true);
	  mpf.addEntry(aPixelFilterType);
	  mpf.addEntry(aPixelFilterWidthX);
	  mpf.addEntry(aPixelFilterWidthY);

	  aaq.addSubGroup(mpf);
	}
	  
	{ 
	  LayoutGroup ct = new LayoutGroup
	    ("ContrastThreshold", "Controls of oversampling due to sample contrast.", true);
	  ct.addEntry(aRedThreshold);
	  ct.addEntry(aGreenThreshold);
	  ct.addEntry(aBlueThreshold);
	  ct.addSeparator(); 
	  ct.addEntry(aCoverageThreshold);

	  aaq.addSubGroup(ct);
	}

	{ 
	  LayoutGroup so = new LayoutGroup
	    ("SampleOptions", "Overall sampling controls.", true);
	  so.addEntry(aSampleLock);
	  so.addEntry(aJitter);

	  aaq.addSubGroup(so);
	}

	layout.addSubGroup(aaq);
      }

      {
	LayoutGroup rq = new LayoutGroup
	  ("RaytracingQuality", "Overall raytracing quality controls.", false);
	rq.addEntry(aUseRaytracing);
	rq.addSeparator(); 
	rq.addEntry(aReflections);
	rq.addEntry(aRefractions);
	rq.addEntry(aMaxTraceDepth);
	rq.addEntry(aShadows);
	rq.addSeparator(); 
	rq.addEntry(aScanline);
	rq.addEntry(aFaces);
	
	layout.addSubGroup(rq);
      }

      {
	LayoutGroup mb = new LayoutGroup
	  ("MotionBlur", "Motion blur specific quality controls.", false);
	mb.addEntry(aMotionBlur);
	mb.addSeparator(); 
	mb.addEntry(aMotionBlurBy);
	mb.addSeparator(); 
	mb.addEntry(aShutter);
	mb.addEntry(aShutterDelay);
	mb.addSeparator(); 
	mb.addEntry(aTimeContrastRed);
	mb.addEntry(aTimeContrastGreen);
	mb.addEntry(aTimeContrastBlue);
	mb.addEntry(aTimeContrastAlpha);
	mb.addSeparator(); 
	mb.addEntry(aMotionSteps);
	
	layout.addSubGroup(mb);
      }

      {
	LayoutGroup cs = new LayoutGroup
	  ("Caustics", "Caustic specific quality controls.", false);
	cs.addEntry(aUseCaustics);
	cs.addSeparator(); 
	cs.addEntry(aCausticsAccuracy);
	cs.addEntry(aCausticsRadius);
	cs.addEntry(aCausticFilterType);
	cs.addEntry(aCausticFilterKernel);
	
	layout.addSubGroup(cs);
      }

      {
	LayoutGroup gi = new LayoutGroup
	  ("GlobalIllumination", "Global illumination specific quality controls.", false);
	gi.addEntry(aUseGlobalIllum);
	gi.addSeparator(); 
	gi.addEntry(aGlobalIllumAccuracy);
	gi.addEntry(aGlobalIllumRadius);
	gi.addSeparator(); 
	gi.addEntry(aPhotonVolumeAccuracy);
	gi.addEntry(aPhotonVolumeRadius);
	gi.addEntry(aMaxReflectionPhotons);
	gi.addEntry(aMaxRefractionPhotons);
	gi.addEntry(aMaxPhotonDepth);
	gi.addSeparator(); 
	gi.addEntry(aPhotonMapFile);
	gi.addEntry(aEnableGIMapVisualizer);
	gi.addEntry(aPhotonMapRebuild);
	gi.addEntry(aDirectIllumShadowEffects);
	
	layout.addSubGroup(gi);
      }

      {
	LayoutGroup fg = new LayoutGroup
	  ("FinalGather", "Final gathering specific quality controls.", false);
	fg.addEntry(aUseFinalGather);
	fg.addSeparator(); 
	fg.addEntry(aPrecompPhotonLookup);
	fg.addEntry(aFinalGatherRays);
	fg.addEntry(aMinRadius);
	fg.addEntry(aMaxRadius);
	fg.addSeparator(); 
	fg.addEntry(aFilter);
	fg.addEntry(aFalloffStart);
	fg.addEntry(aFalloffStop);
	fg.addEntry(aTraceDepth);
	fg.addEntry(aTraceReflection);
	fg.addEntry(aTraceRefraction);
	fg.addSeparator(); 
	fg.addEntry(aFinalGatherMapFile);
	fg.addEntry(aEnableFGMapVisualizer);
	fg.addEntry(aFinalGatherMapRebuild);
	fg.addEntry(aPreviewFinalGather);
	
	layout.addSubGroup(fg);
      }

      {
	LayoutGroup fb = new LayoutGroup
	  ("FramebufferAttributes", "Output framebuffer controls.", false);
	fb.addEntry(aDataType);
	fb.addEntry(aGamma);
	fb.addEntry(aColorClip);
	fb.addEntry(aInterpSamples);
	fb.addEntry(aDesaturate);
	fb.addEntry(aPremultiply);
	fb.addEntry(aDither);
	fb.addSeparator(); 
	
	layout.addSubGroup(fb);
      }

      {
	LayoutGroup mp = new LayoutGroup
	  ("MemoryAndPerformance", "???", false);
	mp.addEntry(aAccelerationMethod);
	mp.addEntry(aBspSize);
	mp.addEntry(aBspDepth);
	mp.addEntry(aGridResolution);
	mp.addEntry(aGridMaxSize);
	mp.addEntry(aGridDepth);
	mp.addSeparator(); 
	mp.addEntry(aTaskSize);
	
	{
	  LayoutGroup ml = new LayoutGroup
	    ("MemoryLimits", "???", true);
	  ml.addEntry(aPhysicalMemory);

	  mp.addSubGroup(ml);
	}

	layout.addSubGroup(mp);
      }

      {
	LayoutGroup tn = new LayoutGroup
	  ("Translation", "Controls over the Maya to Mental Ray translation.", false);
	tn.addEntry(aExportVerbosity);
	tn.addEntry(aExportExactHierarchy);
	tn.addEntry(aExportFullDagpath);
	tn.addEntry(aExportTexturesFirst);
	tn.addSeparator(); 
	tn.addEntry(aExportParticles);
	tn.addEntry(aExportParticleInstances);
	tn.addEntry(aExportFluids);
	tn.addEntry(aExportPostEffects);
	
	layout.addSubGroup(tn);
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
    /* target MEL script */ 
    Path target = getPrimaryTargetPath(agenda, "mel", "MEL script");

    /* create a temporary file which will be copied to the target */ 
    File temp = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(temp);

      {
        Path npath = new Path(agenda.getNodeID().getName());
        Path wpath = new Path(npath.getParentPath(), agenda.getPrimaryTarget().getPath(0)); 
        out.write("print(\"Applying Render Globals: " + 
                  "\" + `getenv \"WORKING\"` + \"" + wpath + "\\n\");\n\n");
      }

      /* image resolution */ 
      {
	int width    = getSingleIntegerParamValue(aImageWidth,  new Range(1, null)); 
	int height   = getSingleIntegerParamValue(aImageHeight, new Range(1, null)); 
        double ratio = getSingleDoubleParamValue(aPixelAspectRatio, 
                                                 new Range(0.0, null, false));

	double deviceRatio = (((double) width) / ((double) height)) * ratio;

	out.write
	  ("// IMAGE RESOLUTION\n" + 
	   "setAttr \"defaultResolution.aspectLock\" 0;\n" + 
	   "setAttr \"defaultResolution.width\" " + width + ";\n" + 
	   "setAttr \"defaultResolution.height\" " + height + ";\n" + 
	   "setAttr \"defaultResolution.aspectLock\" 0;\n" + 
	   "setAttr \"defaultResolution.deviceAspectRatio\" " + deviceRatio + ";\n\n");
      }

      /* number of samples */ 
      {
        int minSamples = getSingleIntegerParamValue(aMinSampleLevel); 
        int maxSamples = getSingleIntegerParamValue(aMaxSampleLevel); 
	
	out.write
	  ("// NUMBER OF SAMPLES\n" + 
	   "setAttr \"miDefaultOptions.minSamples\" " + minSamples + ";\n" + 
	   "setAttr \"miDefaultOptions.maxSamples\" " + maxSamples + ";\n\n");
      }
      
      /* multi-pixel filtering */ 
      {
        Range range = new Range(0.0, 10.0, false, true); 
	int filter     = getSingleEnumParamIndex(aPixelFilterType);
	double filterX = getSingleDoubleParamValue(aPixelFilterWidthX, range); 
	double filterY = getSingleDoubleParamValue(aPixelFilterWidthY, range); 

	out.write
	  ("// MULTI-PIXEL FILTERING\n" + 
	   "setAttr \"miDefaultOptions.filter\" " + filter + ";\n" + 
	   "setAttr \"miDefaultOptions.filterWidth\" " + filterX + ";\n" + 
	   "setAttr \"miDefaultOptions.filterHeight\" " + filterY + ";\n\n");	   
      }

      /* contrast threshold */ 
      {
        Range range = new Range(0.0, null, false); 
        double red      = getSingleDoubleParamValue(aRedThreshold, range); 
	double green    = getSingleDoubleParamValue(aGreenThreshold, range); 
	double blue     = getSingleDoubleParamValue(aBlueThreshold, range); 
	double coverage = getSingleDoubleParamValue(aCoverageThreshold, range); 
	
	out.write
	  ("// CONTRAST THRESHOLD\n" +
	   "setAttr \"miDefaultOptions.contrastR\" " + red + ";\n" + 
	   "setAttr \"miDefaultOptions.contrastG\" " + green + ";\n" + 
	   "setAttr \"miDefaultOptions.contrastB\" " + blue + ";\n" +
	   "setAttr \"miDefaultOptions.contrastA\" " + coverage + ";\n\n");
      }

      /* sample options */ 
      {
	boolean lock   = getSingleBooleanParamValue(aSampleLock);  
	boolean jitter = getSingleBooleanParamValue(aJitter);  

	out.write
	  ("// SAMPLE OPTIONS \n" +
	   "setAttr \"miDefaultOptions.sampleLock\" " + lock + ";\n" +
	   "setAttr \"miDefaultOptions.jitter\" " + jitter + ";\n\n");
      }

      /* raytracing quality */ 
      {
	boolean useRay = getSingleBooleanParamValue(aUseRaytracing);  
        int reflect    = getSingleIntegerParamValue(aReflections); 
        int refract    = getSingleIntegerParamValue(aRefractions); 
        int depth      = getSingleIntegerParamValue(aMaxTraceDepth); 
        int shadows    = getSingleIntegerParamValue(aShadows); 
	int scanline   = getSingleEnumParamIndex(aScanline);
	int faces      = getSingleEnumParamIndex(aFaces);
	
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
	int blur       = getSingleEnumParamIndex(aMotionBlur);
	double blurBy  = getSingleDoubleParamValue(aMotionBlurBy);
	double shutter = getSingleDoubleParamValue(aShutter);
	double delay   = getSingleDoubleParamValue(aShutterDelay);
	double red     = getSingleDoubleParamValue(aTimeContrastRed);
	double green   = getSingleDoubleParamValue(aTimeContrastGreen);
	double blue    = getSingleDoubleParamValue(aTimeContrastBlue);
	double alpha   = getSingleDoubleParamValue(aTimeContrastAlpha);
	int steps      = getSingleIntegerParamValue(aMotionSteps); 
	
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
	boolean useCaustics = getSingleBooleanParamValue(aUseCaustics);  
        int accuracy  = getSingleIntegerParamValue(aCausticsAccuracy); 
	double radius = getSingleDoubleParamValue(aCausticsRadius); 
	int filter    = getSingleEnumParamIndex(aCausticFilterType);
	double kernel = getSingleDoubleParamValue(aCausticFilterKernel); 
	
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
	boolean useGlobalIllum = getSingleBooleanParamValue(aUseGlobalIllum);  
        int accuracy   = getSingleIntegerParamValue(aGlobalIllumAccuracy); 
	double radius  = getSingleDoubleParamValue(aGlobalIllumRadius); 
	int paccuracy  = getSingleIntegerParamValue(aPhotonVolumeAccuracy); 
	double pradius = getSingleDoubleParamValue(aPhotonVolumeRadius); 
        int reflect    = getSingleIntegerParamValue(aMaxReflectionPhotons); 
	int refract    = getSingleIntegerParamValue(aMaxRefractionPhotons); 
        int depth      = getSingleIntegerParamValue(aMaxPhotonDepth); 

	String file = getSingleStringParamValue(aPhotonMapFile); 
	if(file == null) 
	  file = "";
	
	boolean mapvis  = getSingleBooleanParamValue(aEnableGIMapVisualizer);  
	boolean rebuild = getSingleBooleanParamValue(aPhotonMapRebuild);  
	boolean direct  = getSingleBooleanParamValue(aDirectIllumShadowEffects);  
	
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
	boolean useFinalGather = getSingleBooleanParamValue(aUseFinalGather);  
	boolean precomp = getSingleBooleanParamValue(aPrecompPhotonLookup);  
	int rays = getSingleIntegerParamValue(aFinalGatherRays); 
	double minRadius = getSingleDoubleParamValue(aMinRadius); 
	double maxRadius = getSingleDoubleParamValue(aMaxRadius); 
	int filter = getSingleIntegerParamValue(aFilter); 
	double falloffStart = getSingleDoubleParamValue(aFalloffStart); 
	double falloffStop  = getSingleDoubleParamValue(aFalloffStop); 
	int depth   = getSingleIntegerParamValue(aTraceDepth); 
	int reflect = getSingleIntegerParamValue(aTraceReflection); 
	int refract = getSingleIntegerParamValue(aTraceRefraction); 

	String file = getSingleStringParamValue(aFinalGatherMapFile); 
	if(file == null) 
	  file = "";
	
	boolean mapvis  = getSingleBooleanParamValue(aEnableFGMapVisualizer);  
	boolean rebuild = getSingleBooleanParamValue(aFinalGatherMapRebuild);  
	boolean preview = getSingleBooleanParamValue(aPreviewFinalGather);  

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
	int dtype = getSingleEnumParamIndex(aDataType);
        if((dtype < 0) || (dtype > 17)) 
          throw new PipelineException
            ("The DataType (" + dtype + ") was illegal!"); 
        else {
          int indexMap[] = { 0, 1, 17, 4, 2, 14, 3, 16, 5, 6, 7, 15, 8, 13, 9, 10, 11, 12 };
          dtype = indexMap[dtype];
        }

	double gamma = getSingleDoubleParamValue(aGamma); 
	int clip     = getSingleEnumParamIndex(aColorClip);

	boolean interp     = getSingleBooleanParamValue(aInterpSamples);  
	boolean desaturate = getSingleBooleanParamValue(aDesaturate);  
	boolean premult    = getSingleBooleanParamValue(aPremultiply);  
	boolean dither     = getSingleBooleanParamValue(aDither);  
	
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
        switch(getSingleEnumParamIndex(aAccelerationMethod)) {
        case 0:
          method = 0;
          break;
          
        case 1:
          method = 1;
          break;
          
        case 2:
          method = 3;
          break;

        default:
          throw new PipelineException
            ("The AccelerationMethod was illegal!"); 
	}

	int bspSize   = getSingleIntegerParamValue(aBspSize); 
	int bspDepth  = getSingleIntegerParamValue(aBspDepth); 
	int gridRes   = getSingleIntegerParamValue(aGridResolution); 
	int gridSize  = getSingleIntegerParamValue(aGridMaxSize); 
	int gridDepth = getSingleIntegerParamValue(aGridDepth); 
	int taskSize  = getSingleIntegerParamValue(aTaskSize); 
	int memory    = getSingleIntegerParamValue(aPhysicalMemory); 

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
	int verbose = getSingleEnumParamIndex(aExportVerbosity);

	boolean hier      = getSingleBooleanParamValue(aExportExactHierarchy);  
	boolean dag       = getSingleBooleanParamValue(aExportFullDagpath);  
	boolean textures  = getSingleBooleanParamValue(aExportTexturesFirst);  
	boolean particles = getSingleBooleanParamValue(aExportParticles);  
	boolean pinst     = getSingleBooleanParamValue(aExportParticleInstances);  
	boolean fluids    = getSingleBooleanParamValue(aExportFluids);  
	boolean effects   = getSingleBooleanParamValue(aExportPostEffects);  
	
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

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + temp + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, temp, target, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7534841812204404785L;

  public static final String aImageWidth               = "ImageWidth";
  public static final String aImageHeight              = "ImageHeight";
  public static final String aPixelAspectRatio         = "PixelAspectRatio";
  public static final String aImageResolution          = "ImageResolution";
  public static final String aMinSampleLevel           = "MinSampleLevel";
  public static final String aMaxSampleLevel           = "MaxSampleLevel";
  public static final String aPixelFilterType          = "PixelFilterType";
  public static final String aPixelFilterWidthX        = "PixelFilterWidthX";
  public static final String aPixelFilterWidthY        = "PixelFilterWidthY";
  public static final String aRedThreshold             = "RedThreshold";
  public static final String aGreenThreshold           = "GreenThreshold";
  public static final String aBlueThreshold            = "BlueThreshold";
  public static final String aCoverageThreshold        = "CoverageThreshold";
  public static final String aSampleLock               = "SampleLock";
  public static final String aJitter                   = "Jitter";
  public static final String aUseRaytracing            = "UseRaytracing";
  public static final String aReflections              = "Reflections";
  public static final String aRefractions              = "Refractions";
  public static final String aMaxTraceDepth            = "MaxTraceDepth";
  public static final String aShadows                  = "Shadows";
  public static final String aScanline                 = "Scanline";
  public static final String aFaces                    = "Faces";
  public static final String aMotionBlur               = "MotionBlur";
  public static final String aMotionBlurBy             = "MotionBlurBy";
  public static final String aShutter                  = "Shutter";
  public static final String aShutterDelay             = "ShutterDelay";
  public static final String aTimeContrastRed          = "TimeContrastRed";
  public static final String aTimeContrastGreen        = "TimeContrastGreen";
  public static final String aTimeContrastBlue         = "TimeContrastBlue";
  public static final String aTimeContrastAlpha        = "TimeContrastAlpha";
  public static final String aMotionSteps              = "MotionSteps";
  public static final String aUseCaustics              = "UseCaustics";
  public static final String aCausticsAccuracy         = "CausticsAccuracy";
  public static final String aCausticsRadius           = "CausticsRadius";
  public static final String aCausticFilterType        = "CausticFilterType";
  public static final String aCausticFilterKernel      = "CausticFilterKernel";
  public static final String aUseGlobalIllum           = "UseGlobalIllum";
  public static final String aGlobalIllumAccuracy      = "GlobalIllumAccuracy";
  public static final String aGlobalIllumRadius        = "GlobalIllumRadius";
  public static final String aPhotonVolumeAccuracy     = "PhotonVolumeAccuracy";
  public static final String aPhotonVolumeRadius       = "PhotonVolumeRadius";
  public static final String aMaxReflectionPhotons     = "MaxReflectionPhotons";
  public static final String aMaxRefractionPhotons     = "MaxRefractionPhotons";
  public static final String aMaxPhotonDepth           = "MaxPhotonDepth";
  public static final String aPhotonMapFile            = "PhotonMapFile";
  public static final String aEnableGIMapVisualizer    = "EnableGIMapVisualizer";
  public static final String aPhotonMapRebuild         = "PhotonMapRebuild";
  public static final String aDirectIllumShadowEffects = "DirectIllumShadowEffects";
  public static final String aUseFinalGather           = "UseFinalGather";
  public static final String aPrecompPhotonLookup      = "PrecompPhotonLookup";
  public static final String aFinalGatherRays          = "FinalGatherRays";
  public static final String aMinRadius                = "MinRadius";
  public static final String aMaxRadius                = "MaxRadius";
  public static final String aFilter                   = "Filter";
  public static final String aFalloffStart             = "FalloffStart";
  public static final String aFalloffStop              = "FalloffStop";
  public static final String aTraceDepth               = "TraceDepth";
  public static final String aTraceReflection          = "TraceReflection";
  public static final String aTraceRefraction          = "TraceRefraction";
  public static final String aFinalGatherMapFile       = "FinalGatherMapFile";
  public static final String aEnableFGMapVisualizer    = "EnableFGMapVisualizer";
  public static final String aFinalGatherMapRebuild    = "FinalGatherMapRebuild";
  public static final String aPreviewFinalGather       = "PreviewFinalGather";
  public static final String aDataType                 = "DataType";
  public static final String aGamma                    = "Gamma";
  public static final String aColorClip                = "ColorClip";
  public static final String aInterpSamples            = "InterpSamples";
  public static final String aDesaturate               = "Desaturate";
  public static final String aPremultiply              = "Premultiply";
  public static final String aDither                   = "Dither";
  public static final String aAccelerationMethod       = "AccelerationMethod";
  public static final String aBspSize                  = "BspSize";
  public static final String aBspDepth                 = "BspDepth";
  public static final String aGridResolution           = "GridResolution";
  public static final String aGridMaxSize              = "GridMaxSize";
  public static final String aGridDepth                = "GridDepth";
  public static final String aTaskSize                 = "TaskSize";
  public static final String aPhysicalMemory           = "PhysicalMemory";
  public static final String aExportVerbosity          = "ExportVerbosity";
  public static final String aExportExactHierarchy     = "ExportExactHierarchy";
  public static final String aExportFullDagpath        = "ExportFullDagpath";
  public static final String aExportTexturesFirst      = "ExportTexturesFirst";
  public static final String aExportParticles          = "ExportParticles";
  public static final String aExportParticleInstances  = "ExportParticleInstances";
  public static final String aExportFluids             = "ExportFluids";
  public static final String aExportPostEffects        = "ExportPostEffects";
  public static final String aQuality                  = "Quality";

}

