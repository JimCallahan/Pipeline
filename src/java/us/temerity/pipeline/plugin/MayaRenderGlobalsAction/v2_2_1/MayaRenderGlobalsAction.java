// $Id: MayaRenderGlobalsAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaRenderGlobalsAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 
import us.temerity.pipeline.math.Range; 

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
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaRenderGlobalsAction() 
  {
    super("MayaRenderGlobals", new VersionID("2.2.1"), "Temerity",
	  "Creates a MEL script which sets the render globals of a Maya scene.");

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
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Low Quality");
	choices.add("Medium Quality");
	choices.add("High Quality");
	choices.add("Highest Quality");

	ActionParam param = 
	  new EnumActionParam
	  (aEdgeAntiAliasing,
	   "The quality of edge anti-aliasing.", 
	   "Low Quality", choices);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aShadingSamples,
	   "The minimum number of shading samples.", 
	   1);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aMaxShadingSamples,
	   "The maximum number of shading samples.", 
	   1);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  (aParticleSamples,
	   "The number of particle shading samples.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aUseMultiPixelFiltering,
	   "Whether to enable multi-pixel filtering.",
	   false);
	addSingleParam(param);
      }
      
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Box Filter");
	choices.add("Triangle Filter");
	choices.add("Gaussian Filter");
	choices.add("Quadratic B-Spline Filter");

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
	   2.200);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aPixelFilterWidthY,
	   "The vertical pixel filter width.", 
	   2.200);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aRedThreshold,
	   "The red contrast threshold.", 
	   0.400);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aGreenThreshold,
	   "The green contrast threshold.", 
	   0.300);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aBlueThreshold,
	   "The blue contrast threshold.", 
	   0.600);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aCoverageThreshold,
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

	addPreset(aQuality, choices);

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aEdgeAntiAliasing,  "Low Quality");
	  values.put(aShadingSamples,    1);
	  values.put(aMaxShadingSamples, 1);
	  values.put(aParticleSamples,   1);

	  values.put(aUseMultiPixelFiltering, false);
	  values.put(aPixelFilterType,        "Triangle Filter");	
	  values.put(aPixelFilterWidthX,      2.200);	
	  values.put(aPixelFilterWidthY,      2.200);

	  values.put(aRedThreshold,   0.400); 
	  values.put(aGreenThreshold, 0.300);     
	  values.put(aBlueThreshold,  0.600);    

	  addPresetValues(aQuality, "Preview Quality", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aEdgeAntiAliasing,  "Highest Quality");
	  values.put(aShadingSamples,    1);
	  values.put(aMaxShadingSamples, 8);
	  values.put(aParticleSamples,   1);

	  values.put(aUseMultiPixelFiltering, false);
	  values.put(aPixelFilterType,        "Triangle Filter");	
	  values.put(aPixelFilterWidthX,      2.200);	
	  values.put(aPixelFilterWidthY,      2.200);

	  values.put(aRedThreshold,   0.400); 
	  values.put(aGreenThreshold, 0.300);     
	  values.put(aBlueThreshold,  0.600);    

	  addPresetValues(aQuality, "Intermediate Quality", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aEdgeAntiAliasing,  "Highest Quality");
	  values.put(aShadingSamples,    2);
	  values.put(aMaxShadingSamples, 8);
	  values.put(aParticleSamples,   1);

	  values.put(aUseMultiPixelFiltering, true);
	  values.put(aPixelFilterType,        "Triangle Filter");	
	  values.put(aPixelFilterWidthX,      2.200);	
	  values.put(aPixelFilterWidthY,      2.200);

	  values.put(aRedThreshold,   0.400); 
	  values.put(aGreenThreshold, 0.300);     
	  values.put(aBlueThreshold,  0.600);    

	  addPresetValues(aQuality, "Production Quality", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aEdgeAntiAliasing,  "Highest Quality");
	  values.put(aShadingSamples,    2);
	  values.put(aMaxShadingSamples, 8);
	  values.put(aParticleSamples,   1);

	  values.put(aUseMultiPixelFiltering, true);
	  values.put(aPixelFilterType,        "Triangle Filter");	
	  values.put(aPixelFilterWidthX,      2.200);	
	  values.put(aPixelFilterWidthY,      2.200);

	  values.put(aRedThreshold,   0.200); 
	  values.put(aGreenThreshold, 0.150);     
	  values.put(aBlueThreshold,  0.300);    

	  addPresetValues(aQuality, "Contrast Sensitive Production", values);
	}
      }
    }

    /* raytracing quality */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aUseRaytracing,
	   "Whether to enable raytracing.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aReflections,
	   "The reflection ray depth.", 
	   1);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aRefractions,
	   "The refraction ray depth.", 
	   6);
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
	ActionParam param = 
	  new DoubleActionParam
	  (aBias,
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
	  (aUseMotionBlur,
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
	  (aMotionBlurType,
	   "The motion blur technique.", 
	   "2D", quality);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aBlurByFrame,
	   "The amount moving objects are blurred.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aBlurLength,
	   "Scales the amount that moving objects are blurred.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  (aBlurSharpness,
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
	  (aSmooth,
	   "Anti-aliasing hack.", 
	   "Alpha", quality);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aSmoothValue,
	   "The amount Maya blurs motion blur edges.", 
	   2);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aKeepMotionVectors,
	   "Whether to save the motion vectors for external 2D blur.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aUse2dBlurMemoryLimit,
	   "Whether to limit 2D blur memory.",
	   false);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new DoubleActionParam
	  (a2dBlurMemoryLimit,
	   "The maximum amount of memory used by the 2d blur operation.", 
	   200.0);
	addSingleParam(param);
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
	  ("AntiAliasingQuality", "Overall anti-aliasing quality controls.", false);
	aaq.addEntry(aQuality);
	aaq.addEntry(aEdgeAntiAliasing);
	
	{
	  LayoutGroup nos = new LayoutGroup
	    ("NumberOfSamples", "Sampling controls.", true);
	  nos.addEntry(aShadingSamples);
	  nos.addEntry(aMaxShadingSamples);
	  nos.addSeparator(); 
	  nos.addEntry(aParticleSamples);

	  aaq.addSubGroup(nos);
	}

	{ 
	  LayoutGroup mpf = new LayoutGroup
	    ("MultiPixelFiltering", "Output pixel filtering controls.", true);
	  mpf.addEntry(aUseMultiPixelFiltering);
	  mpf.addSeparator(); 
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

	layout.addSubGroup(aaq);
      }

      {
	LayoutGroup rq = new LayoutGroup
	  ("RaytracingQuality", "Overall raytracing quality controls.", false);
	rq.addEntry(aUseRaytracing);
	rq.addSeparator(); 
	rq.addEntry(aReflections);
	rq.addEntry(aRefractions);
	rq.addEntry(aShadows);
	rq.addEntry(aBias);
	
	layout.addSubGroup(rq);
      }

      {
	LayoutGroup mb = new LayoutGroup
	  ("MotionBlur", "Motion blur specific quality controls.", false);
	mb.addEntry(aUseMotionBlur);
	mb.addSeparator(); 
	mb.addEntry(aMotionBlurType);
	mb.addEntry(aBlurByFrame);
	mb.addEntry(aBlurLength);
	mb.addEntry(aBlurSharpness);
	mb.addSeparator(); 
	mb.addEntry(aSmooth);
	mb.addEntry(aSmoothValue);
	mb.addSeparator(); 
	mb.addEntry(aKeepMotionVectors);
	mb.addSeparator(); 
	mb.addEntry(aUse2dBlurMemoryLimit);
	mb.addEntry(a2dBlurMemoryLimit);
	
	layout.addSubGroup(mb);
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

      /* render quality */ 
      {
        int edgeAliasing = 3 - getSingleEnumParamIndex(aEdgeAntiAliasing);

	out.write
	  ("// RENDER QUALITY\n" + 
	   "setAttr \"defaultRenderQuality.edgeAntiAliasing\" " + edgeAliasing + ";\n\n");
      }

      /* number of samples */ 
      { 
        Range range = new Range(1, null);
        int samples  = getSingleIntegerParamValue(aShadingSamples, range); 
        int msamples = getSingleIntegerParamValue(aMaxShadingSamples, range); 
        int psamples = getSingleIntegerParamValue(aParticleSamples, range); 

	out.write
	  ("// NUMBER OF SAMPLES\n" + 
	   "setAttr \"defaultRenderQuality.shadingSamples\" " + samples + ";\n" + 
	   "setAttr \"defaultRenderQuality.maxShadingSamples\" " + msamples + ";\n" + 
	   "setAttr \"defaultRenderQuality.particleSamples\" " + psamples + ";\n\n");
      }
      
      /* pixel filtering */ 
      {
        boolean useFilter = getSingleBooleanParamValue(aUseMultiPixelFiltering); 
	
	int filterType = -1;
        switch(getSingleEnumParamIndex(aPixelFilterType)) {
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
          break;

        default:
          throw new PipelineException
            ("The PixelFilterType was illegal!"); 
	}
	 
	double filterX = getSingleDoubleParamValue(aPixelFilterWidthX);
        double filterY = getSingleDoubleParamValue(aPixelFilterWidthY);

	out.write
	  ("// PIXEL FILTERING\n" +
	   "setAttr \"defaultRenderQuality.useMultiPixelFilter\" " + useFilter + ";\n" +
	   "setAttr \"defaultRenderQuality.pixelFilterType\" " + filterType + ";\n" + 
	   "setAttr \"defaultRenderQuality.pixelFilterWidthX\" " + filterX + ";\n" + 
	   "setAttr \"defaultRenderQuality.pixelFilterWidthY\" " + filterY + ";\n\n");
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
	   "setAttr \"defaultRenderQuality.redThreshold\" " + red + ";\n" + 
	   "setAttr \"defaultRenderQuality.greenThreshold\" " + green + ";\n" + 
	   "setAttr \"defaultRenderQuality.blueThreshold\" " + blue + ";\n" +
	   "setAttr \"defaultRenderQuality.coverageThreshold\" " + coverage + ";\n\n");
      }

      /* raytracing quality */
      {
        boolean useRay = getSingleBooleanParamValue(aUseRaytracing); 

        Range range = new Range(0, null);
        int reflect = getSingleIntegerParamValue(aReflections, range); 
        int refract = getSingleIntegerParamValue(aRefractions, range); 
        int shadow  = getSingleIntegerParamValue(aShadows, range); 
        double bias = getSingleDoubleParamValue(aBias, new Range(0.0, null));
	
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
        boolean useBlur = getSingleBooleanParamValue(aUseMotionBlur); 
	int blurType    = getSingleEnumParamIndex(aMotionBlurType); 

        Range range = new Range(0.0, null);
        double byFrame   = getSingleDoubleParamValue(aBlurByFrame, range); 
        double length    = getSingleDoubleParamValue(aBlurLength, range); 
        double sharpness = getSingleDoubleParamValue(aBlurSharpness, range); 

	int smooth          = getSingleEnumParamIndex(aSmooth); 
	int smoothValue     = getSingleIntegerParamValue(aSmoothValue, new Range(0, null));
        boolean keepVectors = getSingleBooleanParamValue(aKeepMotionVectors); 
        boolean useMemLimit = getSingleBooleanParamValue(aUse2dBlurMemoryLimit); 
        double memLimit     = getSingleDoubleParamValue(a2dBlurMemoryLimit, range);
        
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
	
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the target MEL script file (" + temp + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, temp, target, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6554405729266353868L;

  public static final String aImageWidth             = "ImageWidth";
  public static final String aImageHeight            = "ImageHeight";
  public static final String aPixelAspectRatio       = "PixelAspectRatio";
  public static final String aImageResolution        = "ImageResolution";
  public static final String aEdgeAntiAliasing       = "EdgeAntiAliasing";
  public static final String aShadingSamples         = "ShadingSamples";
  public static final String aMaxShadingSamples      = "MaxShadingSamples";
  public static final String aParticleSamples        = "ParticleSamples";
  public static final String aUseMultiPixelFiltering = "UseMultiPixelFiltering";
  public static final String aPixelFilterType        = "PixelFilterType";
  public static final String aPixelFilterWidthX      = "PixelFilterWidthX";
  public static final String aPixelFilterWidthY      = "PixelFilterWidthY";
  public static final String aRedThreshold           = "RedThreshold";
  public static final String aGreenThreshold         = "GreenThreshold";
  public static final String aBlueThreshold          = "BlueThreshold";
  public static final String aCoverageThreshold      = "CoverageThreshold";
  public static final String aQuality                = "Quality";
  public static final String aUseRaytracing          = "UseRaytracing";
  public static final String aReflections            = "Reflections";
  public static final String aRefractions            = "Refractions";
  public static final String aShadows                = "Shadows";
  public static final String aBias                   = "Bias";
  public static final String aUseMotionBlur          = "UseMotionBlur";
  public static final String aMotionBlurType         = "MotionBlurType";
  public static final String aBlurByFrame            = "BlurByFrame";
  public static final String aBlurLength             = "BlurLength";
  public static final String aBlurSharpness          = "BlurSharpness";
  public static final String aSmooth                 = "Smooth";
  public static final String aSmoothValue            = "SmoothValue";
  public static final String aKeepMotionVectors      = "KeepMotionVectors";
  public static final String aUse2dBlurMemoryLimit   = "Use2dBlurMemoryLimit";
  public static final String a2dBlurMemoryLimit      = "2dBlurMemoryLimit";

}

