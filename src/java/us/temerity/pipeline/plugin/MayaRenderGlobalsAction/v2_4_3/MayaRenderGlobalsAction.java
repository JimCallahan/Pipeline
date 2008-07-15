// $Id: MayaRenderGlobalsAction.java,v 1.1 2008/07/15 16:57:56 jim Exp $

package us.temerity.pipeline.plugin.MayaRenderGlobalsAction.v2_4_3;

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
 * This generated MEL script can then be used as the PreRenderMEL script of a MayaRender
 * action to allow control over rendering parameters directly from Pipeline without the 
 * need to reopen the Maya scene. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Resolution Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains MEL code responsible for setting the render 
 *     resolution.  If specified, the ImageWidth and ImageHeight parameters will be ignored.
 *   </DIV> <BR>
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
    super("MayaRenderGlobals", new VersionID("2.4.3"), "Temerity",
    "Creates a MEL script which sets the render globals of a Maya scene.");

    /* image resolution */ 
    {
      {
        ActionParam param = 
          new LinkActionParam
          (aResolutionSource,
            "The source node which contains MEL code responsible for setting the render " + 
            "resolution.  If specified, the ImageWidth and ImageHeight parameters will be " + 
            "ignored.", 
            null);
        addSingleParam(param);
      } 

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
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       320);
          values.put(aImageHeight,      240);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "320x240", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       640);
          values.put(aImageHeight,      480);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "640x480", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       1024);
          values.put(aImageHeight,      1024);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "1k Square", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       2048);
          values.put(aImageHeight,      2048);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "2k Square", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       3072);
          values.put(aImageHeight,      3072);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "3k Square", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       4096);
          values.put(aImageHeight,      4096);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "4k Square", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       720);
          values.put(aImageHeight,      576);
          values.put(aPixelAspectRatio, 1.066);

          addPresetValues(aImageResolution, "CCIR PAL/Quantel PAL", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       720);
          values.put(aImageHeight,      486);
          values.put(aPixelAspectRatio, 0.900);

          addPresetValues(aImageResolution, "CCIR 601/Quantel NTSC", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       1024);
          values.put(aImageHeight,      768);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "Full 1024", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       1280);
          values.put(aImageHeight,      1024);
          values.put(aPixelAspectRatio, 1.066);

          addPresetValues(aImageResolution, "Full 1280/Screen", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       1280);
          values.put(aImageHeight,      720);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "HD 720", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       1920);
          values.put(aImageHeight,      1080);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "HD 1080", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       646);
          values.put(aImageHeight,      485);
          values.put(aPixelAspectRatio, 1.001);

          addPresetValues(aImageResolution, "NTSC 4d", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       768);
          values.put(aImageHeight,      576);
          values.put(aPixelAspectRatio, 1.0);

          addPresetValues(aImageResolution, "PAL 768", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       780);
          values.put(aImageHeight,      576);
          values.put(aPixelAspectRatio, 0.984);

          addPresetValues(aImageResolution, "PAL 780", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       512);
          values.put(aImageHeight,      486);
          values.put(aPixelAspectRatio, 1.265);

          addPresetValues(aImageResolution, "Targa 486", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
          values.put(aImageWidth,       512);
          values.put(aImageHeight,      482);
          values.put(aPixelAspectRatio, 1.255);

          addPresetValues(aImageResolution, "Target NTSC", values);
        }

        {
          TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
          values.put(aResolutionSource, null); 
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
        choices.add("Plug-in Filter");

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

    /* field options */ 
    {
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Frames");
        choices.add("Both fields, interlaced");
        choices.add("Both fields, separate");
        choices.add("Odd fields");
        choices.add("Even fields");

        ActionParam param = 
          new EnumActionParam
          (aRender,
            "The fields to render.", 
            "Frames", choices);
        addSingleParam(param);
      }
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Odd Field (NTSC)");
        choices.add("Even Field (PAL)");

        ActionParam param = 
          new EnumActionParam
          (aFieldDominance,
            "The fields dominance.", 
            "Odd Field (NTSC)", choices);
        addSingleParam(param);
      }
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("At Top");
        choices.add("At Bottom");

        ActionParam param = 
          new EnumActionParam
          (aZerothScanline,
            "The zeroth scanline.", 
            "At Top", choices);
        addSingleParam(param);
      }
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("No Field Extension");
        choices.add("Default Field Extension (O and E)");
        choices.add("Custom Extension");

        ActionParam param = 
          new EnumActionParam
          (aFieldExtension,
            "The field extension.", 
            "No Field Extension", choices);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aOddField,
            "The custom odd field extension.", 
            null);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aEvenField,
            "The custom even field extension.", 
            null);
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

    /* render options */
    {
      // post processing
      {
        ActionParam param = 
          new StringActionParam
          (aEnvironmentFog,
            "The environment fog node.", 
            null);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aApplyFogInPost,
            "Whether to apply fog in post.",
            false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aPostFogBlur,
            "The amount of post fog blur.", 
            1);
        addSingleParam(param);
      }

      // camera
      {
        ActionParam param = 
          new BooleanActionParam
          (aIgnoreFilmGate,
            "Whether to ignore film gate.",
            true);
        addSingleParam(param);
      }

      // lights and shadows
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Shadows Obey Shadow Linking");
        choices.add("Shadows Obey Light Linking");
        choices.add("Shadows Ignore Linking");

        ActionParam param = 
          new EnumActionParam
          (aShadowLinking,
            "The shadow linking behavior.", 
            "Shadows Obey Light Linking", choices);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aEnableDepthMaps,
            "Whether to use depth maps.",
            true);
        addSingleParam(param);
      }

      // color/compositing
      {
        ActionParam param = 
          new DoubleActionParam
          (aGammaCorrection,
            "The amount gamma correction.", 
            1.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aClipFinalShadedColor,
            "Whether to clip final shaded color.",
            true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aJitterFinalColor,
            "Whether to jitter final color.",
            true);
        addSingleParam(param);
      }
      {
        ActionParam param = 
          new BooleanActionParam
          (aPremultiply,
            "Whether to premultiply.",
            true);
        addSingleParam(param);
      }
      {
        //range is 0.0 - 1.0
        ActionParam param = 
          new DoubleActionParam
          (aPremultiplyThreshold,
            "The amount to premultiply.",
            0.0);
        addSingleParam(param);
      }
    }

    /* memory and performance options */
    {
      // tessellation
      {
        ActionParam param = 
          new BooleanActionParam
          (aUseFileCache,
            "Whether to use file cache.",
            true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOptimizeInstances,
            "Whether to optimize instances.",
            true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aReuseTessellations,
            "Whether to reuse tessellations.",
            true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aUseDisplacementBoundingBox,
            "Whether to use displacement bounding box.",
            true);
        addSingleParam(param);
      }

      // ray tracing
      {
        //range is 1 - 10
        ActionParam param = 
          new IntegerActionParam
          (aRecursionDepth,
            "The ray tracing recursion depth.", 
            2);
        addSingleParam(param);
      }

      {
        //range is 50 to 5000
        ActionParam param = 
          new IntegerActionParam
          (aLeafPrimitives,
            "The amount of leaf primitives.", 
            200);
        addSingleParam(param);
      }

      {
        //range is 01 to 1.0
        ActionParam param = 
          new DoubleActionParam
          (aSubdivisionPower,
            "The subdivision power.", 
            0.25);
        addSingleParam(param);
      }

      // multi processing - skipped since maya render action overrides this...
    }
    /* IPR options */
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aRenderShadingLightAndGlow,
            "Whether to render shading, lighting, and glow.",
            true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aRenderShadowMaps,
            "Whether to render shadow maps.",
            true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aRender2dMotionBlur,
            "Whether to render 2D motion blur.",
            true);
        addSingleParam(param);
      }
    }

    /* paint effects rendering options */
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aEnableStrokeRendering,
            "Whether to enable stroke rendering.",
            true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOversample,
            "Whether to oversample.",
            false);
        addSingleParam(param);
      }

      {
        // has no effect if oversample is set to false...
        ActionParam param = 
          new BooleanActionParam
          (aOversamplePostFilter,
            "Whether to use oversample post filter.",
            false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOnlyRenderStrokes,
            "Whether to render only strokes.",
            false);
        addSingleParam(param);
      }
    }

    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageResolution);
      layout.addEntry(aResolutionSource); 
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
        LayoutGroup fo = new LayoutGroup
        ("FieldOptions", "Overall field options.", false);
        fo.addEntry(aRender);
        fo.addEntry(aFieldDominance);
        fo.addEntry(aZerothScanline);
        fo.addSeparator(); 
        fo.addEntry(aFieldExtension);
        fo.addEntry(aOddField);
        fo.addEntry(aEvenField);

        layout.addSubGroup(fo);
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

      {
        LayoutGroup ro = new LayoutGroup
        ("RenderOptions", "Additional render option controls.", false);

        {
          LayoutGroup pp = new LayoutGroup
          ("PostProcessing", "Post processing option controls.", true);
          pp.addEntry(aEnvironmentFog);
          pp.addEntry(aApplyFogInPost);
          pp.addEntry(aPostFogBlur);

          ro.addSubGroup(pp);
        }

        {
          LayoutGroup co = new LayoutGroup
          ("Camera", "Camera option controls.", true);
          co.addEntry(aIgnoreFilmGate);

          ro.addSubGroup(co);
        }

        {
          LayoutGroup ls = new LayoutGroup
          ("LightsAndShadows", "Lights and shadows option controls.", true);
          ls.addEntry(aShadowLinking);
          ls.addEntry(aEnableDepthMaps);

          ro.addSubGroup(ls);
        }

        {
          LayoutGroup cc = new LayoutGroup
          ("ColorAndCompositing", "color and compositing option controls.", true);
          cc.addEntry(aGammaCorrection);
          cc.addEntry(aClipFinalShadedColor);
          cc.addEntry(aJitterFinalColor);
          cc.addEntry(aPremultiply);
          cc.addEntry(aPremultiplyThreshold);

          ro.addSubGroup(cc);
        }

        layout.addSubGroup(ro);
      }

      {
        LayoutGroup mpo = new LayoutGroup
        ("MemoryAndPerformanceOptions", "Memory and performance option controls.", false);

        {
          LayoutGroup pp = new LayoutGroup
          ("Tessellation", "Tessellation option controls.", true);
          pp.addEntry(aUseFileCache);
          pp.addEntry(aOptimizeInstances);
          pp.addEntry(aReuseTessellations);
          pp.addEntry(aUseDisplacementBoundingBox);

          mpo.addSubGroup(pp);
        }

        {
          LayoutGroup rt = new LayoutGroup
          ("Ray Tracing", "Additional ray tracing option controls.", true);
          rt.addEntry(aRecursionDepth);
          rt.addEntry(aLeafPrimitives);
          rt.addEntry(aSubdivisionPower);

          mpo.addSubGroup(rt);
        }

        layout.addSubGroup(mpo);
      }

      {
        LayoutGroup ipro = new LayoutGroup
        ("IPROptions", "Interactive Photorealistic Rendering option controls.", false);

        ipro.addEntry(aRenderShadingLightAndGlow);
        ipro.addEntry(aRenderShadowMaps);
        ipro.addEntry(aRender2dMotionBlur);

        layout.addSubGroup(ipro);
      }

      {
        LayoutGroup pero = new LayoutGroup
        ("PaintEffectsRenderingOptions", "Paint effects rendering option controls.", false);

        pero.addEntry(aEnableStrokeRendering);
        pero.addEntry(aOversample);
        pero.addEntry(aOversamplePostFilter);
        pero.addEntry(aOnlyRenderStrokes);

        layout.addSubGroup(pero);
      }

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

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
    /* target MEL script */ 
    Path target = getPrimaryTargetPath(agenda, "mel", "MEL script");

    /* resolution MEL script */ 
    Path resSourcePath = null;
    {
      String sname = getSingleStringParamValue(aResolutionSource); 
      if(sname != null) {
        FileSeq fseq = agenda.getPrimarySource(sname);
        if(fseq == null) 
          throw new PipelineException
          ("Somehow the " + aResolutionSource + " (" + sname + ") was not one of the " + 
          "source nodes!");

        NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
        resSourcePath = new Path(PackageInfo.sProdPath, 
          new Path(snodeID.getWorkingParent(), fseq.getPath(0)));
      }
    }

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
      if(resSourcePath != null) {
        out.write("// IMAGE RESOLUTION\n" + 
          "source \"" + resSourcePath + "\";\n\n");
      }
      else {
        int width    = getSingleIntegerParamValue(aImageWidth,  new Range(1, null)); 
        int height   = getSingleIntegerParamValue(aImageHeight, new Range(1, null)); 
        double ratio = getSingleDoubleParamValue(aPixelAspectRatio, new Range(0.0, null, false));
        double deviceRatio = (((double) width) / ((double) height)) * ratio;

        out.write
        ("// IMAGE RESOLUTION\n" + 
          "setAttr \"defaultResolution.aspectLock\" 0;\n" + 
          "setAttr \"defaultResolution.width\" " + width + ";\n" + 
          "setAttr \"defaultResolution.height\" " + height + ";\n" + 
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
          filterType = 0;	// Box
          break;
        case 1:
          filterType = 2;	// Triagle
          break;
        case 2:
          filterType = 4;	// Gaussian
          break;
        case 3:
          filterType = 5;	// Quadratic B-Spline
          break;
        case 4:
          filterType = 1000;	// Quadratic B-Spline
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

      /* field options */
      {
        int fieldOption = getSingleEnumParamIndex(aRender);
        int fields = 0;

        switch(fieldOption) {
        case 0:
          fields = 0;
          break;
        case 1:
          fields = 3;
          break;
        case 2:
          fields = 4;
          break;
        case 3:
          fields = 1;
          break;
        case 4:
          fields = 2;
          break;
        default:
          throw new PipelineException("Invalid Field Option!");
        }

        out.write("// FIELD OPTIONS \n" +
          "setAttr \"defaultResolution.fields\" " + fields + ";\n");

        if (fieldOption > 0){
          int oddFieldFirst;
          int fd = getSingleEnumParamIndex(aFieldDominance);

          if (fd == 0)
            oddFieldFirst = 1;
          else
            oddFieldFirst = 0;

          out.write("setAttr \"defaultResolution.oddFieldFirst\" " + oddFieldFirst + ";\n");
        }

        if (fieldOption >= 2) {
          int zerothScanline = getSingleEnumParamIndex(aZerothScanline);

          out.write("setAttr \"defaultResolution.zerothScanline\" " + zerothScanline + ";\n");

          int fieldExtControl = -1;

          switch (getSingleEnumParamIndex(aFieldExtension)) {
          case 0:
            fieldExtControl = 1;
            break;
          case 1:
            fieldExtControl = 0;
            break;
          case 2:
            fieldExtControl = 2;
            break;
          default:
            throw new PipelineException
            ("The Field Extension selection was illegal!"); 
          }

          out.write("setAttr \"defaultRenderGlobals.fieldExtControl\" " + fieldExtControl + ";\n");

          if (fieldExtControl == 2) {
            String ofield = getSingleStringParamValue(aOddField);
            String efield = getSingleStringParamValue(aEvenField);

            if (ofield != null)
              out.write("setAttr -type \"string\" \"defaultRenderGlobals.oddFieldExt\" " +
                ofield.charAt(0) + ";\n");

            if (efield != null )
              out.write("setAttr -type \"string\" \"defaultRenderGlobals.evenFieldExt\" " +
                efield.charAt(0) + ";\n");
          }
        }

        out.write("\n");
      }
      
      /* raytracing quality */
      {
        Range range = new Range(0, null);
        boolean useRay = getSingleBooleanParamValue(aUseRaytracing); 
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
        Range range = new Range(0.0, null);
        boolean useBlur = getSingleBooleanParamValue(aUseMotionBlur); 
        int blurType    = getSingleEnumParamIndex(aMotionBlurType); 
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

      /* render options */
      {
        String envFog = getSingleStringParamValue(aEnvironmentFog);
        boolean applyFog = getSingleBooleanParamValue(aApplyFogInPost);
        int postFogBlur = getSingleIntegerParamValue(aPostFogBlur);
        boolean ignoreFilmGate = getSingleBooleanParamValue(aIgnoreFilmGate);
        boolean enableDepthMaps = getSingleBooleanParamValue(aEnableDepthMaps);
        boolean shadowsObeyShadowLinking = false;
        boolean shadowsObeyLightLinking = false;
        double gamma = getSingleDoubleParamValue(aGammaCorrection); 
        boolean clipFinalShadedColor = getSingleBooleanParamValue(aClipFinalShadedColor);
        boolean jitterFinalColor = getSingleBooleanParamValue(aJitterFinalColor);
        Range compRange = new Range(0.0, 1.0);
        boolean doPreMult = getSingleBooleanParamValue(aPremultiply);
        double compositeThreshold = getSingleDoubleParamValue(aPremultiplyThreshold, compRange);

        switch (getSingleEnumParamIndex(aShadowLinking)) {
        case 0:
          shadowsObeyShadowLinking = true;
          break;
        case 1:
          shadowsObeyLightLinking = true;
          break;
        case 2:	//do nothing...
          break;
        default:
          throw new PipelineException
          ("The Shadow Linking selection was illegal!"); 
        }

        out.write
        ("// RENDER OPTIONS \n" +
         "setAttr \"defaultRenderGlobals.applyFogInPost\" " + applyFog + ";\n" +
         "setAttr \"defaultRenderGlobals.postFogBlur\" " + postFogBlur + ";\n" +
         "setAttr \"defaultRenderGlobals.ignoreFilmGate\" " + ignoreFilmGate + ";\n" +
         "setAttr \"defaultRenderGlobals.enableDepthMaps\" " + enableDepthMaps + ";\n" +
         "setAttr \"defaultRenderGlobals.shadowsObeyShadowLinking\" " + shadowsObeyShadowLinking + ";\n" +
         "setAttr \"defaultRenderGlobals.shadowsObeyLightLinking\" " + shadowsObeyLightLinking + ";\n" +
         "setAttr \"defaultRenderGlobals.gammaCorrection\" " + gamma + ";\n" +
         "setAttr \"defaultRenderGlobals.clipFinalShadedColor\" " + clipFinalShadedColor + ";\n" +
         "setAttr \"defaultRenderGlobals.jitterFinalColor\" " + jitterFinalColor + ";\n");

        if (envFog != null)
          out.write ("if ((`objExists " + envFog + "`) &&\n" + 
            "(`objectType " + envFog + "` == \"environmentFog\") &&\n" +
            "(!`isConnected " + envFog + ".message defaultRenderGlobals.fogGeometry`))\n" +
            "\tconnectAttr -f " + envFog +".message defaultRenderGlobals.fogGeometry;\n");

        if (doPreMult)
          out.write("setAttr \"defaultRenderGlobals.compositeThreshold\" " + compositeThreshold + ";\n");

        out.write("\n");
      }
      
      /* memory and performance options */
      {
        boolean useFileCache = getSingleBooleanParamValue(aUseFileCache);
        boolean optimizeInstances = getSingleBooleanParamValue(aOptimizeInstances);
        boolean reuseTessellations = getSingleBooleanParamValue(aReuseTessellations);
        boolean useDisplacementBoundingBox = getSingleBooleanParamValue(aUseDisplacementBoundingBox);

        Range recurRange = new Range(0, 10);
        Range leafPrimRange = new Range(50, 5000);
        Range subdivRange = new Range(0.01, 1.0);
        int recursionDepth = getSingleIntegerParamValue(aRecursionDepth, recurRange);
        int leafPrimitives = getSingleIntegerParamValue(aLeafPrimitives, leafPrimRange);
        double subdivisionPower = getSingleDoubleParamValue(aSubdivisionPower, subdivRange);

        out.write
        ("// MEMORY AND PERFORMANCE OPTIONS \n" +
         "setAttr \"defaultRenderGlobals.useFileCache\" " + useFileCache + ";\n" +
         "setAttr \"defaultRenderGlobals.optimizeInstances\" " + optimizeInstances + ";\n" +
         "setAttr \"defaultRenderGlobals.reuseTessellations\" " + reuseTessellations + ";\n" +
         "setAttr \"defaultRenderGlobals.useDisplacementBoundingBox\" " + useDisplacementBoundingBox + ";\n" +
         "setAttr \"defaultRenderGlobals.recursionDepth\" " + recursionDepth + ";\n" +
         "setAttr \"defaultRenderGlobals.leafPrimitives\" " + leafPrimitives + ";\n" +
         "setAttr \"defaultRenderGlobals.subdivisionPower\" " + subdivisionPower + ";\n\n");
      }
      
      /* IPR options */
      {
        boolean iprRenderShading = getSingleBooleanParamValue(aRenderShadingLightAndGlow);
        boolean iprRenderShadowMaps = getSingleBooleanParamValue(aRenderShadowMaps);
        boolean iprRenderMotionBlur = getSingleBooleanParamValue(aRender2dMotionBlur);

        out.write
        ("// MEMORY AND PERFORMANCE OPTIONS \n" +
          "setAttr \"defaultRenderGlobals.iprRenderShading\" " + iprRenderShading + ";\n" +
          "setAttr \"defaultRenderGlobals.iprRenderMotionBlur\" " + iprRenderMotionBlur + ";\n");

        if (iprRenderShading)
          out.write("setAttr \"defaultRenderGlobals.iprRenderShadowMaps\" " + iprRenderShadowMaps + ";\n");

        out.write("\n");
      }
      
      /* paint effects and rendering options */
      {
        boolean enableStrokeRender = getSingleBooleanParamValue(aEnableStrokeRendering);
        boolean oversamplePaintEffects = getSingleBooleanParamValue(aOversample);
        boolean oversamplePfxPostFilter = getSingleBooleanParamValue(aOversamplePostFilter);
        boolean onlyRenderStrokes = getSingleBooleanParamValue(aOnlyRenderStrokes);

        out.write
        ("// PAINT EFFECTS RENDERING OPTIONS \n" +
          "setAttr \"defaultRenderGlobals.enableStrokeRender\" " + enableStrokeRender + ";\n");
        if (enableStrokeRender) {
          out.write
          ("setAttr \"defaultRenderGlobals.oversamplePaintEffects\" " + oversamplePaintEffects + ";\n" +
           "setAttr \"defaultRenderGlobals.onlyRenderStrokes\" " + onlyRenderStrokes + ";\n");

          if (oversamplePaintEffects)
            out.write
            ("setAttr \"defaultRenderGlobals.oversamplePfxPostFilter\" " + oversamplePfxPostFilter + ";\n");
        }

        out.write("\n");
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

  private static final long serialVersionUID = 6135126172872209184L;

  public static final String aResolutionSource			= "ResolutionSource";
  public static final String aImageWidth			= "ImageWidth";
  public static final String aImageHeight			= "ImageHeight";
  public static final String aPixelAspectRatio			= "PixelAspectRatio";
  public static final String aImageResolution			= "ImageResolution";
  public static final String aEdgeAntiAliasing			= "EdgeAntiAliasing";
  public static final String aShadingSamples			= "ShadingSamples";
  public static final String aMaxShadingSamples			= "MaxShadingSamples";
  public static final String aParticleSamples			= "ParticleSamples";
  public static final String aUseMultiPixelFiltering		= "UseMultiPixelFiltering";
  public static final String aPixelFilterType			= "PixelFilterType";
  public static final String aPixelFilterWidthX			= "PixelFilterWidthX";
  public static final String aPixelFilterWidthY			= "PixelFilterWidthY";
  public static final String aRedThreshold                      = "RedThreshold";
  public static final String aGreenThreshold                    = "GreenThreshold";
  public static final String aBlueThreshold     		= "BlueThreshold";
  public static final String aCoverageThreshold			= "CoverageThreshold";
  public static final String aQuality				= "Quality";
  public static final String aUseRaytracing			= "UseRaytracing";
  public static final String aReflections                       = "Reflections";
  public static final String aRefractions                       = "Refractions";
  public static final String aShadows                           = "Shadows";
  public static final String aBias                              = "Bias";
  public static final String aUseMotionBlur                     = "UseMotionBlur";
  public static final String aMotionBlurType                    = "MotionBlurType";
  public static final String aBlurByFrame                       = "BlurByFrame";
  public static final String aBlurLength                        = "BlurLength";
  public static final String aBlurSharpness                     = "BlurSharpness";
  public static final String aSmooth                            = "Smooth";
  public static final String aSmoothValue                       = "SmoothValue";
  public static final String aKeepMotionVectors			= "KeepMotionVectors";
  public static final String aUse2dBlurMemoryLimit		= "Use2dBlurMemoryLimit";
  public static final String a2dBlurMemoryLimit			= "2dBlurMemoryLimit";
  public static final String aRender                            = "Render";
  public static final String aFieldDominance                    = "FieldDominance";
  public static final String aZerothScanline                    = "ZerothScanline";
  public static final String aFieldExtension                    = "FieldExtension";
  public static final String aOddField                          = "OddField";
  public static final String aEvenField                         = "EvenField";
  public static final String aEnvironmentFog                    = "EnvironmentFog";
  public static final String aApplyFogInPost                    = "ApplyFogInPost";
  public static final String aPostFogBlur                       = "PostFogBlur";
  public static final String aIgnoreFilmGate                    = "IgnoreFilmGate";
  public static final String aShadowLinking                     = "ShadowLinking";
  public static final String aEnableDepthMaps                   = "EnableDepthMaps";
  public static final String aGammaCorrection                   = "GammaCorrection";
  public static final String aClipFinalShadedColor		= "ClipFinalShadedColor";
  public static final String aJitterFinalColor			= "JitterFinalColor";
  public static final String aPremultiply                       = "Premultiply";
  public static final String aPremultiplyThreshold		= "PremultiplyThreshold";
  public static final String aUseFileCache                      = "UseFileCache";
  public static final String aOptimizeInstances			= "OptimizeInstances";
  public static final String aReuseTessellations                = "ReuseTessellations";
  public static final String aUseDisplacementBoundingBox        = "UseDisplacementBoundingBox";
  public static final String aRecursionDepth                    = "RecursionDepth";
  public static final String aLeafPrimitives                    = "LeafPrimitives";
  public static final String aSubdivisionPower                  = "Subdivision";
  public static final String aRenderShadingLightAndGlow         = "RenderShadingLightingAndGlow";
  public static final String aRenderShadowMaps                  = "RenderShadowMaps";
  public static final String aRender2dMotionBlur                = "Render2DMotionBlur";
  public static final String aEnableStrokeRendering             = "EnableStrokeRendering";
  public static final String aOversample                        = "Oversample";
  public static final String aOversamplePostFilter		= "OversamplePostFilter";
  public static final String aOnlyRenderStrokes			= "OnlyRenderStrokes";
  //public static final String aReadThisDepthFile		= "ReadThisDepthFile";

}

