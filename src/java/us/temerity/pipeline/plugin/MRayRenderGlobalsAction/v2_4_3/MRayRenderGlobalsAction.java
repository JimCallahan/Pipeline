// $Id: MRayRenderGlobalsAction.java,v 1.2 2008/07/21 23:28:06 jim Exp $

package us.temerity.pipeline.plugin.MRayRenderGlobalsAction.v2_4_3;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.math.Range; 
import us.temerity.pipeline.math.Tuple2d;
import us.temerity.pipeline.math.Tuple3d;

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
 * This generated MEL script can then be used as the PreRenderMEL script of a MayaRender
 * action to allow control over rendering parameters directly from Pipeline without the need 
 * to reopen the Maya scene. <P> 
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
 *   </DIV> <BR>
 *   
 *   Alpha Mask Channel <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to render images with alpha mask channel. 
 *   </DIV> <BR>
 *   
 *   Z Depth Channel <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to render images with z depth channel.
 *   </DIV> <BR>
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
 *     Threshold <BR>
 *     <DIV style="margin-left: 40px;">
 *       The contrast threshold. <BR>
 *     </DIV> <BR>
 *   
 *     Coverage Threshold <BR>
 *     <DIV style="margin-left: 40px;">
 *       The alpha coverage threshold. <BR>
 *     </DIV> <BR>
 * 
 *     Sample Lock <BR>
 *     <DIV style="margin-left: 40px;">
 *       Maintain sample patterns between frames to reduce flickering. <BR>
 *     </DIV> <BR>
 *   
 *     Jitter <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable ray jittering. <BR>
 *     </DIV> <BR>
 *     
 *     Visibility Samples <BR>
 *     <DIV style="margin-left: 40px;">
 *       The rapid samples collect. <BR>
 *     </DIV> <BR>
 *     
 *     Shading Quality <BR>
 *     <DIV style="margin-left: 40px;">
 *       The rapid samples shading. <BR>
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
 *     Refractions<BR>
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
 *     Reflection Blur Limit <BR>
 *     <DIV style="margin-left: 40px;">
 *       The reflection blur trace depth limit. <BR>
 *     </DIV> <BR>
 *     
 *     Refraction Blur Limit <BR>
 *     <DIV style="margin-left: 40px;">
 *       The refraction blur trace depth limit. <BR>
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
 *   <I>Acceleration Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Acceleration Method <BR>
 *     <DIV style="margin-left: 40px;">
 *       The algorithm used to acceleration ray testing. <BR>
 *     </DIV> <BR>
 *     
 *     BSP Size <BR>
 *     <DIV style="margin-left: 40px;">
 *       Maximum number of primitives per BSP tree leaf. <BR>
 *     </DIV> <BR>
 *     
 *     BSP Depth <BR>
 *     <DIV style="margin-left: 40px;">
 *       MMaximum levels of the BSP tree. <BR>
 *     </DIV> <BR>
 *   
 *     BSP Shadow <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Grid Resolution <BR>
 *     <DIV style="margin-left: 40px;">
 *       The resolution of the 3d hierarchical grid. <BR>
 *     </DIV> <BR>
 *     
 *     Grid Max Size <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum number of polygons per voxel. <BR>
 *     </DIV> <BR>
 *     
 *     Grid Depth <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum hiarachy depth of the grid. <BR>
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
 *     Motion Quality Factor <BR>
 *     <DIV style="margin-left: 40px;">
 *       Motion quality factor applies to only rasterizer renderer. <BR>
 *     </DIV> <BR>
 *   
 *     Motion Blur By <BR>
 *     <DIV style="margin-left: 40px;">
 *       Scale factor of the motion blur time interval. <BR>
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
 *     Time Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum temporal contrast. <BR>
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
 *     
 *     Custom Motion Offset <BR>
 *     <DIV style="margin-left: 40px;">
 *       The custom motion back offset. <BR>
 *     </DIV> <BR>
 *     
 *     Static Object Offset <BR>
 *     <DIV style="margin-left: 40px;">
 *       The custom static offset. <BR>
 *     </DIV> <BR>
 *     
 *     Time Samples <BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of temporal shading samples per spatial sample. <BR>
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
 *     Caustics Radius <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum radius to be used when picking up caustic photons. <BR>
 *     </DIV> <BR>
 *     
 *     Caustic Scale <BR>
 *     <DIV style="margin-left: 40px;">
 *       The caustic contribution scale. <BR>
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
 *     GlobalIllumScale <BR>
 *     <DIV style="margin-left: 40px;">
 *       The global illumination contribution scale. <BR>
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
 *        Enable advanced shadow effects from direct lighting. <BR>
 *     </DIV> <BR>
 *     
 *     Photon Auto Volume <BR>
 *     <DIV style="margin-left: 40px;">
 *       enable support for overlapping photon volumes. <BR>
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
 *     Enable Final Gather Map Visualizer <BR>
 *     <DIV style="margin-left: 40px;">
 *       Produce a false-color rendering of final gather ray density. <BR>
 *     </DIV> <BR>
 *     
 *     Final Gather Map Rebuild <BR>
 *     <DIV style="margin-left: 40px;">
 *       Ignore and overwrite the final gather map file if it exists. <BR>
 *     </DIV> <BR>
 *   
 *     Preview Final Gather <BR>
 *     <DIV style="margin-left: 40px;">
 *       Shows diagnostic final gathering points in output image <BR>
 *     </DIV> <BR>
 *     
 *     Point Density <BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of final gather points when performing final gather tracing. <BR>
 *     </DIV> <BR>
 *     
 *     Point Interpolation <BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of final gather points to be considered for interpolation. <BR>
 *     </DIV> <BR>
 *     
 *     Final Gather Scale <BR>
 *     <DIV style="margin-left: 40px;">
 *       The final gather contribution scale. <BR>
 *     </DIV> <BR>
 *     
 *     Final Gather View <BR>
 *     <DIV style="margin-left: 40px;">
 *       Enable radius values to be given in screen space as pixel diameter. <BR>
 *     </DIV> <BR>
 *     
 *     Final Gather Trace Diffuse <BR>
 *     <DIV style="margin-left: 40px;">
 *       Enable final gather secondary diffuse bounces. <BR>
 *     </DIV> <BR>
 *     
 *     Final Gather Bounce Scale <BR>
 *     <DIV style="margin-left: 40px;">
 *       The secondary bounce final gather contribution scale. <BR>
 *     </DIV> <BR>
 *     
 *     Optimize for Animation <BR>
 *     <DIV style="margin-left: 40px;">
 *       Enable multi-frame final gather mode to reduce flickering. <BR>
 *     </DIV> <BR>
 *     
 *     Use Radius Quality Control <BR>
 *     <DIV style="margin-left: 40px;">
 *       Use max and min radius to define final gather quality (backwards compatible). <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I>Diagnostics Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;"> <BR>
 *     Diagnose Samples <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to diagnose samples. <BR>
 *     </DIV> <BR>
 *     
 *     Diagnose BSP <BR>
 *     <DIV style="margin-left: 40px;">
 *       BSP diagnostic mode options. <BR>
 *     </DIV> <BR>
 *     
 *     Diagnose Grid Size <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Diagnose Photon <BR>
 *     <DIV style="margin-left: 40px;">
 *       Photon diagnostic mode options. <BR>
 *     </DIV> <BR>
 *     
 *     Diagnose Photon Density <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Diagnose Final Gather <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to diagnose final gather. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 *   
 *   <I> Contours Parameters </I> <BR>
 *   <DIV style="margin-left: 40px;"> <BR>
 *     Enable Contour Rendering <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable contour rendering. <BR>
 *     </DIV> <BR>
 *     
 *     Hide Source <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to hide sources. <BR>
 *     </DIV> <BR>
 *     
 *     Flood Color <BR>
 *     <DIV style="margin-left: 40px;">
 *       The flood color. <BR>
 *     </DIV> <BR>
 *     
 *     Over Sample <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Contour Filter Type <BR>
 *     <DIV style="margin-left: 40px;">
 *       The contour filter type. <BR>
 *     </DIV> <BR>
 *     
 *     Contour Filter Support <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Around Silhouette <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Around All Polyfaces <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Around Coplanar Faces <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Between Different Instances <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Between Different Materials <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Between Different Labels <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Around Render Tessellation <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Front Vs Back Face Contours <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Enable Color Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Color Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Enable Depth Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Depth Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Enable Distance Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Distance Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Enable Normal Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Normal Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Enable UV Contours <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     UV Contours <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Contrast Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       The name of the custom contrast shader for contour rendering. <BR>
 *     </DIV> <BR>
 *     
 *     Distance Contrast <BR>
 *     <DIV style="margin-left: 40px;">
 *       The name of the custom store shader for contour rendering. <BR>
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
 *   <I>Render Options Parameters>/I> <BR>
 *   <DIV style="margin-left: 40px;">
 *   Export Volume Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Geometry Shaders <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Displacement Shaders <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Output Shaders <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Auto Volume <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Displace Presample <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Merge Surfaces <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Render Fur/Hair <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Render Passes <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Faces <BR>
 *     <DIV style="margin-left: 40px;">
 *       Controls which side(s) of triangles are rendered. <BR>
 *     </DIV> <BR>
 *     
 *     Volume Samples <BR>
 *     <DIV style="margin-left: 40px;">
 *       Default volume rendering quality in shaders for objects without override settings. <BR>
 *     </DIV> <BR>
 *     
 *     Shadow Map Bias <BR>
 *     <DIV style="margin-left: 40px;">
 *       The shadow map bias override. <BR>
 *     </DIV> <BR>
 *     
 *     Surface Approx <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Caustics Generating <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Caustics Receiving <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Global Illum Generating <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Global Illum Receiving <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
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
 *     
 *     Export Vertex Color <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export Hair <BR>
 *     <DIV style="margin-left: 40px;">
 *       Hair translation mode <BR>
 *     </DIV> <BR>
 *     
 *     Prune Object W/O Material <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Opt Anim Detection <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Opt Vert Sharing <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Opt Raytrace Shadows <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export Motion Segments <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export Triangulated Poly <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export Shape Deformation <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export Polygon Derivatives <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Maya Derivatives <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Smooth Polygon Derivatives <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export Objects On Demand <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Performance Threshold <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Render Shaders With Filter <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export State Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export Light Linker <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Export Maya Options <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Custom Colors <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Custom Texts <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Custom Data <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Custom Vectors <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   </DIV>
 *   
 *   <I>Preview Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Preview Animation <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Preview Motion Blur <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Preview Render Tiles <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Preview Convert Tiles <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Preview Tonemap Tiles <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Preview Tonemap Scale <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *   </DIV>
 *   
 *   <I>Custom Globals Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Pass Custom Alpha Channel <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Pass Custom Depth Channel <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Pass Custom Label Channel <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Custom Versions <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Custom Links <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
 *     
 *     Custom Includes <BR>
 *     <DIV style="margin-left: 40px;">
 *       ??? <BR>
 *     </DIV> <BR>
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
    super("MRayRenderGlobals", new VersionID("2.4.3"), "Temerity",
          "Creates a MEL script which sets the Mental Ray render globals of a Maya scene.");

    /* create default options node */
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aCreateDefaultNodes,
            "Whether to check for existence of miDefaultOptions in maya, and " +
            "create one in the scene if it is missing before applying settings.",
            false);
        addSingleParam(param);
      }
    }

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

    /* primary renderer */
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Raytracing");
      choices.add("Scanline");
      choices.add("OpenGL");
      choices.add("Rasterizer");

      ActionParam param = 
        new EnumActionParam
        (aScanline,
         "Controls the use of scanline rendering.", 
         "Scanline", choices);
      addSingleParam(param);
    }

    /* additional output channel */ 
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aAlphaMaskChannel,
           "Whether to render images with alpha mask channel.",
           true);
        addSingleParam(param);
      }
      
      {
        ActionParam param = 
          new BooleanActionParam
          (aZDepthChannel,
           "Whether to render images with z depth channel.",
           false);
        addSingleParam(param);
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
          new Tuple3dActionParam
          (aThreshold,
           "The contrast threshold.", 
           new Tuple3d(0.100, 0.100, 0.100));
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

    /* rasterizer quality */
    {
      {
        //range is 0 - 30
        ActionParam param = 
          new IntegerActionParam
          (aVisibilitySamples,
           "The rapid samples collect.", 
           0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aShadingQuality,
           "The rapid samples shading.", 
           1.0);
        addSingleParam(param);
      }
    }

    /* sample options */ 
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aSampleLock,
           "Maintain sample patterns between frames to reduce flickering.",
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
        ActionParam param = 
          new IntegerActionParam
          (aReflectionBlurLimit,
           "The reflection blur limit.", 
           1);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aRefractionBlurLimit,
           "The refraction blur limit.", 
           1);
        addSingleParam(param);
      }
    }

    /* shadows */
    {
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Disabled");
        choices.add("Simple");
        choices.add("Sorted");
        choices.add("Segment");

        ActionParam param = 
          new EnumActionParam
          (aShadowMethod,
           "Controls the method of shadow detecting and shader calling.", 
           "Simple", choices);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("On");
        choices.add("Obeys Light Linking");
        choices.add("Off");

        ActionParam param = 
          new EnumActionParam
          (aShadowLinking,
           "Controls the method of shadow linking.", 
           "Obeys Light Linking", choices);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Disabled");
        choices.add("Regular");
        choices.add("Regular (OpenGL)");
        choices.add("Detail");

        ActionParam param = 
          new EnumActionParam
          (aShadowMapsFormat,
           "Controls the format of the shadow maps.", 
           "Regular", choices);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Reuse Existing Maps");
        choices.add("Rebuild All & Overwrite");
        choices.add("Rebuild All & Merge");

        ActionParam param = 
          new EnumActionParam
          (aShadowMapsRebuildMode,
           "Controls the caching and rebuild mode of the shadow maps.", 
           "Reuse Existing Maps", choices);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aMotionBlurShadowMaps,
           "Whether to do motion blur for shadow maps.",
           true);
        addSingleParam(param);
      }
    }

    /* motion blur */ 
    {
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Off");
        choices.add("NoDeformation");
        choices.add("Full");

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
          (aMotionQualityFactor,
           "Motion Quality Factor applies to rasterizer renderer only.", 
           1.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aMotionBlurBy,
           "Scale factor of the motion blur time interval.", 
           1.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aShutterClose,
           "The time when the shutter closes.", 
           1.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aShutterOpen,
           "The time when the shutter opens.", 
           0.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new Tuple3dActionParam
          (aTimeContrast,
           "The maximum temporal contrast.", 
           new Tuple3d(0.2, 0.2, 0.2));
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

      {
        ActionParam param = 
          new BooleanActionParam
          (aCustomMotionOffsets,
           "Whether to use custom motion offsets.",
           false);
        addSingleParam(param);
      }

      //range is 0.0 to 1.0
      {
        ActionParam param = 
          new DoubleActionParam
          (aMotionBackOffset,
           "The custom motion back offset.", 
           0.500);
        addSingleParam(param);
      }

      //range is 0.0 to 1.0
      {
        ActionParam param = 
          new DoubleActionParam
          (aStaticObjectOffset,
           "The custom static object offset.", 
           0.500);
        addSingleParam(param);
      }

      //range is 1 to 100
      {
        ActionParam param = 
          new IntegerActionParam
          (aTimeSamples,
           "Number of temporal shading samples per spatial sample.", 
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
           100);
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
        ActionParam param = 
          new Tuple3dActionParam
          (aCausticScale,
           "The caustic contribution scale.", 
           new Tuple3d(1.0, 1.0, 1.0));
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Box");
        choices.add("Cone");
        choices.add("Gauss");

        ActionParam param = 
          new EnumActionParam
          (aCausticFilterType,
           "The type of filter used by caustics.", 
           "Box", choices);
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
           500);
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
          new Tuple3dActionParam
          (aGlobalIllumScale,
           "The global illumination contribution scale.", 
           new Tuple3d(1.0, 1.0, 1.0));
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aPhotonVolumeAccuracy,
           "The maximum number of photons to examine in participating media.", 
           30);
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
           "Enable advanced shadow effects from direct lighting.",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPhotonAutoVolume,
           "Enable support for overlapping photon volumes.",
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
           100);
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
           null);
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
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Off");
        choices.add("On");
        choices.add("Freeze");

        ActionParam param = 
          new EnumActionParam
          (aFinalGatherMapRebuild,
           "Ignore and overwrite the final gather map file if it exists.",
           "On", choices);
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

      {
        ActionParam param = 
          new DoubleActionParam
          (aPointDensity,
           "The number of final gather points when performing final gather tracing.", 
           1.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOptimizeForAnim,
            "Enable multi-frame final gather mode to reduce flickering.",
            false);
        addSingleParam(param);
      }
      
      {
        ActionParam param = 
          new BooleanActionParam
          (aUseRadiusQualityControl,
            "Use max and min radius to define final gather quality (backwards compatible).",
            false);
        addSingleParam(param);
      }
      
      //range is 16-bit integer...
      {
        ActionParam param = 
          new IntegerActionParam
          (aPointInterpolation,
           "The number of final gather points to be considered for interpolation at a shading sample.", 
           10);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new Tuple3dActionParam
          (aFinalGatherScale,
           "The final gather contribution scale.", 
           new Tuple3d(1.0, 1.0, 1.0));
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aFinalGatherView,
           "Enable radius values to be given in screen space as pixel diameter.",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aFinalGatherTraceDiffuse,
           "Enable final gather secondary diffuse bounces.",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new Tuple3dActionParam
          (aFGBounceScale,
           "The secondary bounce final gather contribution scale.", 
           new Tuple3d(1.0, 1.0, 1.0));
        addSingleParam(param);
      }
      
    }

    /* diagnostics */
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aDiagnoseSamples,
           "Whether to diagnose samples.",
           false);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Off");
        choices.add("Depth");
        choices.add("Size");

        ActionParam param = 
          new EnumActionParam
          (aDiagnoseBsp,
           "BSP diagnostic mode options.", 
           "Off", choices);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Off");
        choices.add("Object");
        choices.add("World");
        choices.add("Camera");

        ActionParam param = 
          new EnumActionParam
          (aDiagnoseGrid,
           "Grid diagnostic mode options.", 
           "Off", choices);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aDiagnoseGridSize,
           "???", 
           1.0);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Off");
        choices.add("Density");
        choices.add("Irradiance");

        ActionParam param = 
          new EnumActionParam
          (aDiagnosePhoton,
           "Photon diagnostic mode options.", 
           "Off", choices);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aDiagnosePhotonDensity,
           "???", 
           0.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aDiagnoseFinalGather,
           "Whether to diagnose final gather.",
           false);
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

    /* contours */
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aEnableContourRendering,
           "Whether to enable contour rendering.",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aHideSource,
           "Whether to hide sources.",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new Tuple3dActionParam
          (aFloodColor,
           "The flood color.", 
           new Tuple3d(1.0, 1.0, 1.0));
        addSingleParam(param);
      }

      //range is 0.0 to 1.0
      //      {
      //        ActionParam param = 
      //          new DoubleActionParam
      //          (aAlphaFloodColor,
      //            "The alpha component of the flood color.", 
      //            1.0);
      //        addSingleParam(param);
      //      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aOverSample,
           "???", 
           1);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Box Filter");
        choices.add("Triangle Filter");
        choices.add("Gaussian Filter");

        ActionParam param = 
          new EnumActionParam
          (aContourFilterType,
           "The contour filter type.", 
           "Box Filter", choices);
        addSingleParam(param);
      }

      //range is 0.0 to 2.0
      {
        ActionParam param = 
          new DoubleActionParam
          (aContourFilterSupport,
           "???", 
           1.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aAroundSilhouette,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aAroundAllPolyFaces,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aAroundCoplanarFaces,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aBetweenDiffInstances,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aBetweenDiffMaterials,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aBetweenDiffLabels,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aAroundRenderTessellation,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aFrontVsBackFaceContours,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aEnableColorContrast,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new Tuple3dActionParam
          (aColorContrast,
           "The color contrast.", 
           new Tuple3d(1.0, 1.0, 1.0));
        addSingleParam(param);
      }

      //range is 0.0 to 1.0
      //      {
      //        ActionParam param = 
      //          new DoubleActionParam
      //          (aAlphaColorContrast,
      //            "The alpha component of the color contrast.", 
      //            1.0);
      //        addSingleParam(param);
      //      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aEnableDepthContrast,
           "Whether to enable depth contrast",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aDepthContrast,
           "The depth contrast value.", 
           0.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aEnableDistanceContrast,
           "Whether to enable distance contrast",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aDistanceContrast,
           "The distance contrast value.", 
           0.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aEnableNormalContrast,
           "Whether to enable normal contrast",
           true);
        addSingleParam(param);
      }

      //range is 0.0 - 180.0
      {
        ActionParam param = 
          new DoubleActionParam
          (aNormalContrast,
           "The normal contrast value.", 
           0.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aEnableUVContours,
           "Whether to enable UV Contours",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new Tuple2dActionParam
          (aUVContours,
           "The UV value of the contours.", 
           new Tuple2d(0.0, 0.0));
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aContrastShader,
           "The name of the custom contrast shader to use for contour rendering.", 
           null);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aStoreShader,
           "The name of the custom store shader to use for contour rendering.", 
           null);
        addSingleParam(param);
      }
    }

    /* memory and performance */ 
    {
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Regular BSP");
        choices.add("Large BSP");
        choices.add("Hierarchical Grid");

        ActionParam param = 
          new EnumActionParam
          (aAccelerationMethod,
           "The algorithm used to acceleration ray testing.", 
           "Regular BSP", choices);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aBspSize,
           "Maximum number of primitives per BSP tree leaf.", 
           10);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aBspDepth,
           "Maximum levels of the BSP tree.", 
           40);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aBspShadow,
           "???", 
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aGridResolution,
           "The resolution of the 3d hierarchical grid", 
           2);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aGridMaxSize,
           "The maximum number of polygons per voxel", 
           128);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aGridDepth,
           "The maximum hierarchy depth of the grid", 
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

    /* render options */
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aVolumeShaders,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aGeometryShaders,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aDisplacementShaders,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOutputShaders,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aAutoVolume,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aDisplacePresample,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aMergeSurfaces,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aRenderFurHair,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aRenderPasses,
           "???",
           true);
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

      {
        ActionParam param = 
          new IntegerActionParam
          (aVolumeSamples,
           "Default volume rendering quality in shaders for objects without override settings.", 
           1);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aMaxDisplace,
           "The maximum displacement override.", 
           0.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aShadowMapBias,
           "The shadow map bias override.", 
           0.0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aSurfaceApprox,
           "???", 
           null);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aDisplaceApprox,
           "???", 
           null);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("None");
        choices.add("On");
        choices.add("Off");

        ActionParam param = 
          new EnumActionParam
          (aCausticsGenerating,
           "???", 
           "None", choices);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("None");
        choices.add("On");
        choices.add("Off");

        ActionParam param = 
          new EnumActionParam
          (aCausticsReceiving,
           "???", 
           "None", choices);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("None");
        choices.add("On");
        choices.add("Off");

        ActionParam param = 
          new EnumActionParam
          (aGlobalIllumGenerating,
           "???", 
           "None", choices);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("None");
        choices.add("On");
        choices.add("Off");

        ActionParam param = 
          new EnumActionParam
          (aGlobalIllumReceiving,
           "???", 
           "None", choices);
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

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportVertexColors,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Off");
        choices.add("Hair Geometry Shader");
        choices.add("Hair Primitive");

        ActionParam param = 
          new EnumActionParam
          (aExportHair,
           "Hair translation mode.", 
           "Hair Primitive", choices);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPruneObjectsWOMaterial,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOptNonAnimDisplayVis,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOptAnimDetection	,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOptVertSharing		,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aOptRaytraceShadows	,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportMotionSegments,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportTriangulatedPoly,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportShapeDeformation,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportPolygonDerivatives,
           "???",
           true);
        addSingleParam(param);
      }
      {
        ActionParam param = 
          new BooleanActionParam
          (aMayaDerivatives,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aSmoothPolygonDerivatives,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportNurbsDerivatives,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportObjectsOnDemand,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new IntegerActionParam
          (aPerformanceThreshold,
           "???",
           0);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aRenderShadersWithFilter,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportStateShader	,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExportLightLinker	,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aExprtMayaOptions	,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aCustomColors		,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aCustomTexts			,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aCustomData			,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aCustomVectors		,
           "???",
           true);
        addSingleParam(param);
      }
    }

    /* preview */
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aPreviewAnimation		,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPreviewMotionBlur		,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPreviewRenderTiles		,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPreviewConvertTiles		,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPreviewTonemapTiles		,
           "???",
           true);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new DoubleActionParam
          (aTonemapScale,
           "???",
           1.0);
        addSingleParam(param);
      }
    }

    /* custom globals */
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aPassCustomAlphaChannel,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPassCustomDepthChannel,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPassCustomLabelChannel,
           "???",
           false);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aCustomVersions,
           "???",
           null);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aCustomLinks,
           "???",
           null);
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new StringActionParam
          (aCustomIncludes,
           "???",
           null);
        addSingleParam(param);
      }
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Draft");
      choices.add("Draft Motion Blur");
      choices.add("Draft Rapid Motion");
      choices.add("Preview");
      choices.add("Preview Caustics");
      choices.add("Preview Final Gather");
      choices.add("Preview Global Illum");
      choices.add("Preview Motion Blur");
      choices.add("Preview Rapid Motion");
      choices.add("Production");
      choices.add("Production Motion Blur");
      choices.add("Production Rapid Fur");
      choices.add("Production Rapid Hair");
      choices.add("Production Rapid Motion");
      choices.add("Production Fine Trace");

      addPreset(aQuality, choices);

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -2);
        values.put(aMaxSampleLevel,        0);
        values.put(aPixelFilterType,       "Box Filter");
        values.put(aPixelFilterWidthX,     1.0);
        values.put(aPixelFilterWidthY,     1.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           1);
        values.put(aRefractions,           1);
        values.put(aMaxTraceDepth,         2);

        values.put(aMotionBlur,            "Off");
        values.put(aTimeContrast,          new Tuple3d(0.2, 0.2, 0.2));
        values.put(aTimeContrastAlpha,     0.2);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Draft", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -2);
        values.put(aMaxSampleLevel,         0);
        values.put(aPixelFilterType,       "Box Filter");
        values.put(aPixelFilterWidthX,     1.0);
        values.put(aPixelFilterWidthY,     1.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           1);
        values.put(aRefractions,           1);
        values.put(aMaxTraceDepth,         2);

        values.put(aMotionBlur,            "NoDeformation");
        values.put(aTimeContrast,          new Tuple3d(1.0, 1.0, 1.0));
        values.put(aTimeContrastAlpha,     1.0);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Draft Motion Blur", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -2);
        values.put(aMaxSampleLevel,        0);
        values.put(aPixelFilterType,       "Box Filter");
        values.put(aPixelFilterWidthX,     1.0);
        values.put(aPixelFilterWidthY,     1.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           1);
        values.put(aRefractions,           1);
        values.put(aMaxTraceDepth,         2);

        values.put(aMotionBlur,            "NoDeformation");
        values.put(aTimeContrast,          new Tuple3d(1.0, 1.0, 1.0));
        values.put(aTimeContrastAlpha,     1.0);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Rasterizer");
        values.put(aVisibilitySamples,	   1);
        values.put(aShadingQuality, 	   0.25);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Draft Rapid Motion", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -1);
        values.put(aMaxSampleLevel,        1);
        values.put(aPixelFilterType,       "Triangle Filter");
        values.put(aPixelFilterWidthX,     2.0);
        values.put(aPixelFilterWidthY,     2.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           2);
        values.put(aRefractions,           2);
        values.put(aMaxTraceDepth,         4);

        values.put(aTimeContrast,          new Tuple3d(1.0, 1.0, 1.0));
        values.put(aTimeContrastAlpha,     1.0);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Preview", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -1);
        values.put(aMaxSampleLevel,        1);
        values.put(aPixelFilterType,       "Triangle Filter");
        values.put(aPixelFilterWidthX,     2.0);
        values.put(aPixelFilterWidthY,     2.0);
        values.put(aJitter,                false);

        values.put(aReflections,           2);
        values.put(aRefractions,           2);
        values.put(aMaxTraceDepth,         4);

        values.put(aMotionBlur,            "Off");
        values.put(aTimeContrast,          new Tuple3d(1.0, 1.0, 1.0));
        values.put(aTimeContrastAlpha,     1.0);

        values.put(aUseCaustics,           true);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "On");
        values.put(aCausticsReceiving, 	   "On");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Preview Caustics", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -1);
        values.put(aMaxSampleLevel,        1);
        values.put(aPixelFilterType,       "Triangle Filter");
        values.put(aPixelFilterWidthX,     2.0);
        values.put(aPixelFilterWidthY,     2.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           2);
        values.put(aRefractions,           2);
        values.put(aMaxTraceDepth,         4);

        values.put(aMotionBlur,            "Off");
        values.put(aTimeContrast,          new Tuple3d(1.0, 1.0, 1.0));
        values.put(aTimeContrastAlpha,     1.0);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        true);
        values.put(aUseFinalGather,        true);

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Preview Final Gather", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -1);
        values.put(aMaxSampleLevel,        1);
        values.put(aPixelFilterType,       "Triangle Filter");
        values.put(aPixelFilterWidthX,     2.0);
        values.put(aPixelFilterWidthY,     2.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           2);
        values.put(aRefractions,           2);
        values.put(aMaxTraceDepth,         4);

        values.put(aMotionBlur,            "Off");
        values.put(aTimeContrast,          new Tuple3d(1.0, 1.0, 1.0));
        values.put(aTimeContrastAlpha,     1.0);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        true);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "On");
        values.put(aGlobalIllumReceiving,  "On");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Preview Global Illum", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -1);
        values.put(aMaxSampleLevel,        1);
        values.put(aPixelFilterType,       "Triangle Filter");
        values.put(aPixelFilterWidthX,     2.0);
        values.put(aPixelFilterWidthY,     2.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           2);
        values.put(aRefractions,           2);
        values.put(aMaxTraceDepth,         4);

        values.put(aMotionBlur,            "NoDeformation");
        values.put(aTimeContrast,          new Tuple3d(0.500, 0.500, 0.500));
        values.put(aTimeContrastAlpha,     0.500);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);	

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Preview Motion Blur", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        -1);
        values.put(aMaxSampleLevel,        1);
        values.put(aPixelFilterType,       "Triangle Filter");
        values.put(aPixelFilterWidthX,     2.0);
        values.put(aPixelFilterWidthY,     2.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           2);
        values.put(aRefractions,           2);
        values.put(aMaxTraceDepth,         4);

        values.put(aMotionBlur,            "NoDeformation");
        values.put(aTimeContrast,          new Tuple3d(0.500, 0.500, 0.500));
        values.put(aTimeContrastAlpha,     0.500);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);	

        values.put(aScanline,	           "Rasterizer");
        values.put(aVisibilitySamples,	   3);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Preview Rapid Motion", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        0);
        values.put(aMaxSampleLevel,        2);
        values.put(aPixelFilterType,       "Gaussian Filter");
        values.put(aPixelFilterWidthX,     3.0);
        values.put(aPixelFilterWidthY,     3.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           10);
        values.put(aRefractions,           10);
        values.put(aMaxTraceDepth,         20);

        values.put(aMotionBlur,            "Off");
        values.put(aTimeContrast,          new Tuple3d(0.200, 0.200, 0.200));
        values.put(aTimeContrastAlpha,     0.200);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Production", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        0);
        values.put(aMaxSampleLevel,        2);
        values.put(aPixelFilterType,       "Gaussian Filter");
        values.put(aPixelFilterWidthX,     3.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           10);
        values.put(aRefractions,           10);
        values.put(aMaxTraceDepth,         20);

        values.put(aMotionBlur,            "Full");
        values.put(aTimeContrast,          new Tuple3d(0.200, 0.200, 0.200));
        values.put(aTimeContrastAlpha,     0.200);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);	

        values.put(aScanline,	           "Scanline");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,    "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Production Motion Blur", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        0);
        values.put(aMaxSampleLevel,        2);
        values.put(aPixelFilterType,       "Triangle Filter");
        values.put(aPixelFilterWidthX,     2.29);
        values.put(aPixelFilterWidthY,     2.29);
        values.put(aThreshold,             new Tuple3d(0.4, 0.3, 0.7));
        values.put(aJitter,                true);

        values.put(aReflections,           1);
        values.put(aRefractions,           1);
        values.put(aMaxTraceDepth,         1);

        values.put(aMotionBlur,            "Off");
        values.put(aTimeContrast,          new Tuple3d(0.200, 0.200, 0.200));
        values.put(aTimeContrastAlpha,     0.200);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);	

        values.put(aScanline,	           "Rasterizer");
        values.put(aVisibilitySamples,   	3);
        values.put(aShadingQuality, 	   0.25);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Segment");
        values.put(aShadowMapsFormat,      "Detail");
        values.put(aShadowMapsRebuildMode, "Rebuild All & Overwrite");
        values.put(aMotionBlurShadowMaps,  false);
        values.put(aShadowMapBias,         0.02);

        addPresetValues(aQuality, "Production Rapid Fur", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        0);
        values.put(aMaxSampleLevel,        2);
        values.put(aPixelFilterType,       "Triangle Filter");
        values.put(aPixelFilterWidthX,     2.29);
        values.put(aPixelFilterWidthY,     2.29);
        values.put(aThreshold,             new Tuple3d(0.4, 0.3, 0.7));
        values.put(aJitter,                true);

        values.put(aReflections,           1);
        values.put(aRefractions,           1);
        values.put(aMaxTraceDepth,         1);

        values.put(aMotionBlur,            "Off");
        values.put(aTimeContrast,          new Tuple3d(0.200, 0.200, 0.200));
        values.put(aTimeContrastAlpha,     0.200);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);	

        values.put(aScanline,	           "Rasterizer");
        values.put(aVisibilitySamples,	   6);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Segment");
        values.put(aShadowMapsFormat,      "Detail");
        values.put(aShadowMapsRebuildMode, "Rebuild All & Overwrite");
        values.put(aMotionBlurShadowMaps,  false);
        values.put(aShadowMapBias,         0.02);

        addPresetValues(aQuality, "Production Rapid Fur", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        0);
        values.put(aMaxSampleLevel,        2);
        values.put(aPixelFilterType,       "Gaussian Filter");
        values.put(aPixelFilterWidthX,     3.0);
        values.put(aPixelFilterWidthY,     3.0);
        values.put(aThreshold,             new Tuple3d(0.1, 0.1, 0.1));
        values.put(aJitter,                false);

        values.put(aReflections,           10);
        values.put(aRefractions,           10);
        values.put(aMaxTraceDepth,         20);

        values.put(aMotionBlur,            "Full");
        values.put(aTimeContrast,          new Tuple3d(0.200, 0.200, 0.200));
        values.put(aTimeContrastAlpha,     0.200);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Rasterizer");
        values.put(aVisibilitySamples,     8);
        values.put(aShadingQuality, 	   2.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Production Rapid Motion", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aMinSampleLevel,        1);
        values.put(aMaxSampleLevel,        2);
        values.put(aPixelFilterType,       "Box Filter");
        values.put(aPixelFilterWidthX,     0.75);
        values.put(aPixelFilterWidthY,     0.75);
        values.put(aThreshold,             new Tuple3d(0.02, 0.02, 0.02));
        values.put(aJitter,                true);

        values.put(aReflections,           1);
        values.put(aRefractions,           1);
        values.put(aMaxTraceDepth,         1);

        values.put(aMotionBlur,            "Off");
        values.put(aTimeContrast,          new Tuple3d(0.200, 0.200, 0.200));
        values.put(aTimeContrastAlpha,     0.200);

        values.put(aUseCaustics,           false);
        values.put(aUseGlobalIllum,        false);
        values.put(aUseFinalGather,        false);

        values.put(aScanline,	           "Raytracing");
        values.put(aVisibilitySamples,	   0);
        values.put(aShadingQuality, 	   1.0);
        values.put(aCausticsGenerating,	   "None");
        values.put(aCausticsReceiving, 	   "None");
        values.put(aGlobalIllumGenerating, "None");
        values.put(aGlobalIllumReceiving,  "None");

        values.put(aShadowMethod,          "Simple");
        values.put(aShadowMapsFormat,      "Regular");
        values.put(aShadowMapsRebuildMode, "Reuse Existing Maps");
        values.put(aMotionBlurShadowMaps,  true);
        values.put(aShadowMapBias,         0.0);

        addPresetValues(aQuality, "Production Fine Trace", values);
      }
    }

    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aCreateDefaultNodes);
      layout.addSeparator();
      layout.addEntry(aImageResolution);
      layout.addEntry(aResolutionSource); 
      layout.addEntry(aImageWidth);
      layout.addEntry(aImageHeight);
      layout.addEntry(aPixelAspectRatio);
      layout.addSeparator();
      layout.addEntry(aAlphaMaskChannel);
      layout.addEntry(aZDepthChannel);
      layout.addSeparator();
      layout.addEntry(aQuality);
      layout.addEntry(aScanline);

      {
        LayoutGroup aaq = new LayoutGroup
        ("Anti-aliasingQuality", "The overall rendering quality controls.", true);
        {
          LayoutGroup nos = new LayoutGroup
          ("RaytraceScanlineQuality", "The ray sampling controls.", true);
          nos.addEntry(aMinSampleLevel);
          nos.addEntry(aMaxSampleLevel);

          aaq.addSubGroup(nos);
        }

        { 
          LayoutGroup ct = new LayoutGroup
          ("ContrastThreshold", "Controls of oversampling due to sample contrast.", true);
          //ct.addEntry(aRedThreshold);
          //ct.addEntry(aGreenThreshold);
          //ct.addEntry(aBlueThreshold);
          ct.addEntry(aThreshold);
          ct.addEntry(aCoverageThreshold);

          aaq.addSubGroup(ct);
        }

        { 
          LayoutGroup rq = new LayoutGroup
          ("RasterizerQuality", "Controls of Rasterizer options.", true);
          rq.addEntry(aVisibilitySamples);
          rq.addEntry(aShadingQuality);

          aaq.addSubGroup(rq);
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
          LayoutGroup so = new LayoutGroup
          ("SampleOptions", "Overall sampling controls.", true);
          so.addEntry(aJitter);
          so.addEntry(aSampleLock);

          aaq.addSubGroup(so);
        }

        layout.addSubGroup(aaq);
      }

      {
        LayoutGroup rq = new LayoutGroup
        ("Raytracing", "Overall raytracing quality controls.", false);
        rq.addEntry(aUseRaytracing);
        rq.addSeparator(); 
        rq.addEntry(aReflections);
        rq.addEntry(aRefractions);
        rq.addEntry(aMaxTraceDepth);
        rq.addSeparator();
        rq.addEntry(aShadows);
        rq.addEntry(aReflectionBlurLimit);
        rq.addEntry(aRefractionBlurLimit);

        layout.addSubGroup(rq);
      }

      {
        LayoutGroup mp = new LayoutGroup
        ("Acceleration", "???", false);
        mp.addEntry(aAccelerationMethod);
        mp.addSeparator(); 
        mp.addEntry(aBspSize);
        mp.addEntry(aBspDepth);
        mp.addEntry(aBspShadow);
        mp.addSeparator(); 
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
        LayoutGroup shdw = new LayoutGroup
        ("Shadows", "Shadow quality and rebuild option controls", false);
        shdw.addEntry(aShadowMethod);
        shdw.addEntry(aShadowLinking);
        shdw.addSeparator();
        shdw.addEntry(aShadowMapsFormat);
        shdw.addEntry(aShadowMapsRebuildMode);
        shdw.addEntry(aMotionBlurShadowMaps);

        layout.addSubGroup(shdw);
      }

      {
        LayoutGroup mb = new LayoutGroup
        ("MotionBlur", "Motion blur specific quality controls.", false);
        mb.addEntry(aMotionBlur);
        mb.addEntry(aMotionQualityFactor);
        mb.addSeparator(); 
        mb.addEntry(aMotionBlurBy);
        mb.addEntry(aMotionSteps);
        mb.addSeparator(); 
        mb.addEntry(aShutterOpen);
        mb.addEntry(aShutterClose);
        mb.addEntry(aTimeSamples);
        mb.addSeparator();
        mb.addEntry(aTimeContrast);
        mb.addEntry(aTimeContrastAlpha);

        {
          LayoutGroup cmb = new LayoutGroup
          ("Custom", "Custom motion blur option controls", false);
          cmb.addEntry(aCustomMotionOffsets);
          cmb.addEntry(aMotionBackOffset);
          cmb.addEntry(aStaticObjectOffset);
          mb.addSubGroup(cmb);
        }

        layout.addSubGroup(mb);
      }

      {
        LayoutGroup cs = new LayoutGroup
        ("Caustics", "Caustic specific quality controls.", false);
        cs.addEntry(aUseCaustics);
        cs.addSeparator();
        cs.addEntry(aCausticsAccuracy);
        cs.addEntry(aCausticScale);
        cs.addSeparator();
        cs.addEntry(aCausticsRadius);
        //cs.addEntry(aAlphaCausticScale);
        cs.addSeparator();
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
        gi.addEntry(aGlobalIllumScale);
        gi.addSeparator();
        gi.addEntry(aGlobalIllumRadius);
        gi.addSeparator(); 
        gi.addEntry(aMaxReflectionPhotons);
        gi.addEntry(aMaxRefractionPhotons);
        gi.addEntry(aMaxPhotonDepth);
        gi.addSeparator();
        gi.addEntry(aPhotonMapRebuild);
        gi.addEntry(aPhotonMapFile);
        gi.addEntry(aDirectIllumShadowEffects);
        gi.addEntry(aEnableGIMapVisualizer);
        gi.addSeparator();
        gi.addEntry(aPhotonAutoVolume);
        gi.addEntry(aPhotonVolumeAccuracy);
        gi.addEntry(aPhotonVolumeRadius);

        layout.addSubGroup(gi);
      }

      {
        LayoutGroup fg = new LayoutGroup
        ("FinalGather", "Final gathering specific quality controls.", false);
        fg.addEntry(aUseFinalGather);
        fg.addEntry(aFinalGatherRays);
        fg.addEntry(aPointDensity);
        fg.addEntry(aPointInterpolation);
        fg.addEntry(aFinalGatherScale);
        //fg.addEntry(aAlphaFinalGatherScale);
        fg.addSeparator(); 
        fg.addEntry(aFinalGatherMapRebuild);
        fg.addEntry(aFinalGatherMapFile);
        fg.addEntry(aEnableFGMapVisualizer);
        fg.addEntry(aPreviewFinalGather);
        fg.addSeparator();
        fg.addEntry(aOptimizeForAnim);
        fg.addEntry(aUseRadiusQualityControl);
        fg.addEntry(aMaxRadius);
        fg.addEntry(aMinRadius);
        fg.addEntry(aFinalGatherView);
        fg.addSeparator();
        fg.addEntry(aPrecompPhotonLookup);
        fg.addEntry(aFilter);
        fg.addEntry(aFalloffStart);
        fg.addEntry(aFalloffStop);
        fg.addSeparator();
        fg.addEntry(aTraceReflection);
        fg.addEntry(aTraceRefraction);
        fg.addEntry(aTraceDepth);
        fg.addEntry(aFinalGatherTraceDiffuse);
        fg.addEntry(aFGBounceScale);
        //fg.addEntry(aAlphaFGBounceScale);
        
        layout.addSubGroup(fg);
      }

      {
        LayoutGroup diag = new LayoutGroup
        ("Diagnostics", "Diagnostic options controls.", false);
        diag.addEntry(aDiagnoseSamples);
        diag.addEntry(aDiagnoseBsp);
        diag.addEntry(aDiagnoseGrid);
        diag.addEntry(aDiagnoseGridSize);
        diag.addEntry(aDiagnosePhoton);
        diag.addEntry(aDiagnosePhotonDensity);
        diag.addEntry(aDiagnoseFinalGather);

        layout.addSubGroup(diag);
      }
      
      {
        LayoutGroup ro = new LayoutGroup
        ("RenderOptions", "Controls for additional render features and defaults.", false);

        {
          LayoutGroup feat = new LayoutGroup
          ("Features", "Controls for additional render features.", false);
          feat.addEntry(aVolumeShaders);
          feat.addEntry(aGeometryShaders);
          feat.addEntry(aDisplacementShaders);
          feat.addEntry(aOutputShaders);
          feat.addSeparator();
          feat.addEntry(aAutoVolume);
          feat.addEntry(aDisplacePresample);
          feat.addEntry(aMergeSurfaces);
          feat.addEntry(aRenderFurHair);
          feat.addEntry(aRenderPasses);

          ro.addSubGroup(feat);
        }

        {
          LayoutGroup def = new LayoutGroup
          ("Defaults", "Controls for additional render defaults.", false);
          def.addEntry(aFaces);
          def.addSeparator();
          def.addEntry(aVolumeSamples);

          ro.addSubGroup(def);
        }

        {
          LayoutGroup or = new LayoutGroup
          ("Overrides", "Controls for additional render overrides.", false);
          or.addEntry(aMaxDisplace);
          or.addSeparator();
          or.addEntry(aShadowMapBias);
          or.addSeparator();
          or.addEntry(aSurfaceApprox);
          or.addEntry(aDisplaceApprox);
          or.addSeparator();
          or.addEntry(aCausticsGenerating);
          or.addEntry(aCausticsReceiving);
          or.addEntry(aGlobalIllumGenerating);
          or.addEntry(aGlobalIllumReceiving);

          ro.addSubGroup(or);
        }

        layout.addSubGroup(ro);
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

        layout.addSubGroup(fb);
      }

      {
        LayoutGroup cntr = new LayoutGroup
        ("Contours", "Contour rendering controls.", false);
        cntr.addEntry(aEnableContourRendering);
        cntr.addEntry(aHideSource);
        cntr.addEntry(aFloodColor);
        //cntr.addEntry(aAlphaFloodColor);

        {
          LayoutGroup ct = new LayoutGroup
          ("Contours", "Contours option controls", false);
          ct.addEntry(aOverSample);
          ct.addEntry(aContourFilterType);
          ct.addEntry(aContourFilterSupport);

          cntr.addSubGroup(ct);
        }

        {
          LayoutGroup dc = new LayoutGroup
          ("Draw Contours", "Draw contours option controls", false);
          dc.addEntry(aAroundSilhouette);
          dc.addEntry(aAroundAllPolyFaces);
          dc.addEntry(aAroundCoplanarFaces);
          dc.addEntry(aBetweenDiffInstances);
          dc.addEntry(aBetweenDiffMaterials);
          dc.addEntry(aBetweenDiffLabels);
          dc.addEntry(aAroundRenderTessellation);
          dc.addEntry(aFrontVsBackFaceContours);
          dc.addSeparator();
          dc.addEntry(aEnableColorContrast);
          dc.addEntry(aColorContrast);
          //dc.addEntry(aAlphaColorContrast);
          dc.addEntry(aEnableDepthContrast);
          dc.addEntry(aDepthContrast);
          dc.addEntry(aEnableDistanceContrast);
          dc.addEntry(aDistanceContrast);
          dc.addEntry(aEnableNormalContrast);
          dc.addEntry(aNormalContrast);
          dc.addEntry(aEnableUVContours);
          dc.addEntry(aUVContours);
          cntr.addSubGroup(dc);
        }

        {
          LayoutGroup cs = new LayoutGroup
          ("Custom Shaders", "Contours custom shaders option controls", false);
          cs.addEntry(aContrastShader);
          cs.addEntry(aStoreShader);

          cntr.addSubGroup(cs);
        }

        layout.addSubGroup(cntr);
      }

      {
        LayoutGroup tn = new LayoutGroup
        ("Translation", "Controls over the Maya to Mental Ray translation.", false);
        tn.addEntry(aExportVerbosity);
        tn.addSeparator();
        tn.addEntry(aExportExactHierarchy);
        tn.addEntry(aExportFullDagpath);
        tn.addEntry(aExportTexturesFirst);
        tn.addSeparator(); 
        tn.addEntry(aExportParticles);
        tn.addEntry(aExportParticleInstances);
        tn.addEntry(aExportFluids);
        tn.addEntry(aExportHair);
        tn.addEntry(aExportPostEffects);
        tn.addEntry(aExportVertexColors);

        {
          LayoutGroup pf = new LayoutGroup
          ("Performance", "Controls for translation performance.", false);
          pf.addEntry(aPruneObjectsWOMaterial);
          pf.addEntry(aOptNonAnimDisplayVis);
          pf.addEntry(aOptAnimDetection);
          pf.addEntry(aOptVertSharing);
          pf.addEntry(aOptRaytraceShadows);
          pf.addSeparator();
          pf.addEntry(aExportMotionSegments);
          pf.addEntry(aExportTriangulatedPoly);
          pf.addEntry(aExportShapeDeformation);
          pf.addEntry(aExportPolygonDerivatives);
          pf.addEntry(aMayaDerivatives);
          pf.addEntry(aSmoothPolygonDerivatives);
          pf.addEntry(aExportNurbsDerivatives);
          pf.addEntry(aExportObjectsOnDemand);
          pf.addEntry(aPerformanceThreshold);

          tn.addSubGroup(pf);
        }
        
        {
          LayoutGroup cstm = new LayoutGroup
          ("Customization", "Controls for translation customization.", false);

          cstm.addEntry(aRenderShadersWithFilter);
          cstm.addSeparator();
          cstm.addEntry(aExportStateShader);
          cstm.addEntry(aExportLightLinker);
          cstm.addEntry(aExprtMayaOptions);
          cstm.addEntry(aCustomColors);
          cstm.addEntry(aCustomTexts);
          cstm.addEntry(aCustomData);
          cstm.addEntry(aCustomVectors);

          tn.addSubGroup(cstm);
        }
        
        layout.addSubGroup(tn);
      }

      {
        LayoutGroup pv = new LayoutGroup
        ("Preview", "Preview option controls.", false);
        pv.addEntry(aPreviewAnimation);
        pv.addEntry(aPreviewMotionBlur);
        pv.addEntry(aPreviewRenderTiles);
        pv.addEntry(aPreviewConvertTiles);
        pv.addEntry(aPreviewTonemapTiles);
        pv.addEntry(aTonemapScale);

        layout.addSubGroup(pv);
      }

      {
        LayoutGroup ce = new LayoutGroup
        ("CustomEntities", "Custom entities options.", false);
        ce.addEntry(aPassCustomAlphaChannel);
        ce.addEntry(aPassCustomDepthChannel);
        ce.addEntry(aPassCustomLabelChannel);
        ce.addSeparator();
        ce.addEntry(aCustomVersions);
        ce.addEntry(aCustomLinks);
        ce.addEntry(aCustomIncludes);

        layout.addSubGroup(ce);
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

      boolean createDefaults = getSingleBooleanParamValue(aCreateDefaultNodes);

      if (createDefaults)
        out.write
          ("// TEST FOR EXISTENCE OF REQUIRED GLOBALS NODES\n\n" +
           "if ((!`objExists miDefaultOptions`) || (!`objExists mentalrayGlobals`)) {\n" +
           "\tif (!`pluginInfo -query -loaded \"Mayatomr\"`)\n" +
           "\t\tloadPlugin -quiet \"Mayatomr\";\n" +
           "\tmiCreateDefaultNodes;\n" +
           "}\n\n");

      {
        Path npath = new Path(agenda.getNodeID().getName());
        Path wpath = new Path(npath.getParentPath(), agenda.getPrimaryTarget().getPath(0)); 
        out.write
          ("print(\"Applying Render Globals: " + 
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

      /* alpha & depth channels */
      {
        boolean alpha = getSingleBooleanParamValue(aAlphaMaskChannel);
        boolean depth = getSingleBooleanParamValue(aZDepthChannel);
        
        out.write
        ("// ALPHA & DEPTH CHANNEL OVERRIDE\n" +
         "string $cameras[] = `ls -type \"camera\"`;\n" +
         "string $cam;\n" +
         "for ($cam in $cameras) {\n" +
         "\tsetAttr ($cam + \".mask\") " + alpha + ";\n" +
         "\tsetAttr ($cam + \".depth\") " + depth + ";\n" +
         "}\n\n");
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

        //maya filter values are scaled according to the filter type
        if (filter < 4) {
          filterX = filterX / (filter + 1);
          filterY = filterY / (filter + 1);
        }
        else if (filter == 4) {
          filterX = filterX / filter;
          filterY = filterY / filter;
        }
        else {
          throw new PipelineException("Illegal filter type!");
        }

        out.write
          ("// MULTI-PIXEL FILTERING\n" + 
           "setAttr \"miDefaultOptions.filter\" " + filter + ";\n" + 
           "setAttr \"miDefaultOptions.filterWidth\" " + filterX + ";\n" + 
           "setAttr \"miDefaultOptions.filterHeight\" " + filterY + ";\n\n");	   
      }

      /* contrast threshold */ 
      {
        Range range3d = new Range(new Tuple3d(0.0, 0.0, 0.0), null, false); 
        Range range = new Range(0.0, null, false);
        Tuple3d contrast = getSingleTuple3dParamValue(aThreshold, range3d, false);
        double alpha 	= getSingleDoubleParamValue(aCoverageThreshold, range);
        
        out.write
          ("// CONTRAST THRESHOLD\n" +
           "setAttr \"miDefaultOptions.contrastR\" " + contrast.x() + ";\n" + 
           "setAttr \"miDefaultOptions.contrastG\" " + contrast.y() + ";\n" + 
           "setAttr \"miDefaultOptions.contrastB\" " + contrast.z()+ ";\n" +
           "setAttr \"miDefaultOptions.contrastA\" " + alpha + ";\n\n");
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
        int maxReflectionBlur = getSingleIntegerParamValue(aReflectionBlurLimit); 
        int maxRefractionBlur = getSingleIntegerParamValue(aRefractionBlurLimit); 
        Range sampleCollectRange = new Range (0, 30);
        Range sampleShadingRange = new Range (0.1, null);
        int rapidSamplesCollect = getSingleIntegerParamValue(aVisibilitySamples, sampleCollectRange);
        double rapidSamplesShading = getSingleDoubleParamValue(aShadingQuality, sampleShadingRange);

        out.write
          ("// RAYTRACING QUALITY\n" +
           "setAttr \"miDefaultOptions.rayTracing\" " + useRay + ";\n" +
           "setAttr \"miDefaultOptions.scanline\" " + scanline + ";\n" +
           "setAttr \"miDefaultOptions.maxReflectionRays\" " + reflect + ";\n" +
           "setAttr \"miDefaultOptions.maxRefractionRays\"  " + refract + ";\n" +
           "setAttr \"miDefaultOptions.maxRayDepth\"  " + depth + ";\n" +
           "setAttr \"miDefaultOptions.maxShadowRayDepth\" " + shadows + ";\n" +
           "setAttr \"miDefaultOptions.maxReflectionBlur\"  " + maxReflectionBlur + ";\n" +
           "setAttr \"miDefaultOptions.maxRefractionBlur\" " + maxRefractionBlur + ";\n" +
           "setAttr \"miDefaultOptions.rapidSamplesCollect\"  " + rapidSamplesCollect + ";\n" +
           "setAttr \"miDefaultOptions.rapidSamplesShading\" " + rapidSamplesShading + ";\n\n");
      }

      /* motion blur */ 
      {
        Range range = new Range(0.0, 1.0);
        Range range3d = new Range(new Tuple3d(0.0, 0.0, 0.0), new Tuple3d(1.0, 1.0, 1.0));
        Range timeSampleRange = new Range(1, 100);
        int blur       = getSingleEnumParamIndex(aMotionBlur);
        double blurBy  = getSingleDoubleParamValue(aMotionBlurBy);
        double shutter = getSingleDoubleParamValue(aShutterClose);
        double delay   = getSingleDoubleParamValue(aShutterOpen);
        Tuple3d timeContrast = getSingleTuple3dParamValue(aTimeContrast, range3d, false);
        double alpha   = getSingleDoubleParamValue(aTimeContrastAlpha, range);
        int rapidSamplesMotion = getSingleIntegerParamValue(aTimeSamples, timeSampleRange);
        int steps      = getSingleIntegerParamValue(aMotionSteps);
        double motionQualityFactor = getSingleDoubleParamValue(aMotionQualityFactor);
        boolean exportCustomMotion = getSingleBooleanParamValue(aCustomMotionOffsets);
        double exportMotionOffset  = getSingleDoubleParamValue(aMotionBackOffset, range);
        double exportMotionOutput  = getSingleDoubleParamValue(aStaticObjectOffset, range);
        int scanline   = getSingleEnumParamIndex(aScanline);

        out.write
          ("// MOTION BLUR\n" +
           "setAttr \"miDefaultOptions.motionBlur\" " + blur + ";\n" +
           "setAttr \"miDefaultOptions.motionBlurBy\" " + blurBy + ";\n" +
           "setAttr \"miDefaultOptions.shutter\" " + shutter + ";\n" +
           "setAttr \"miDefaultOptions.shutterDelay\" " + delay + ";\n" +
           "setAttr \"miDefaultOptions.timeContrastR\" " + timeContrast.x() + ";\n" +
           "setAttr \"miDefaultOptions.timeContrastG\" " + timeContrast.y() + ";\n" +
           "setAttr \"miDefaultOptions.timeContrastB\" " + timeContrast.z() + ";\n" +
           "setAttr \"miDefaultOptions.timeContrastA\" " + alpha + ";\n" +
           "setAttr \"mentalrayGlobals.exportCustomMotion\" " + exportCustomMotion + ";\n" +
           "setAttr \"mentalrayGlobals.exportMotionOffset\" " + exportMotionOffset + ";\n" +
           "setAttr \"mentalrayGlobals.exportMotionOutput\" " + exportMotionOutput + ";\n" +
           "setAttr \"miDefaultOptions.motionSteps\" " + steps + ";\n\n");

        if (blur > 0 && scanline == 3)
          out.write
            ("// MOTION BLUR QUALITY FACTOR\n" + 
              "proc int tm_miFindMotionFactorIndex() {\n" +
              "\tint $index = -1;\n" +
              "\tstring $stringOptions[] = `listAttr -string \"stringOptions\" -multi \"miDefaultOptions\"`;\n" +
              "\tint $stringOptionCount = size( $stringOptions );\n" +
              "\tfor( $i = 0; $i < $stringOptionCount; $i++ ) {\n" +
              "\t\tif(\"rast motion factor\" == `getAttr (\"miDefaultOptions.stringOptions[\" + $i + \"].name\")`) {\n" +
              "\t\t\t$index = $i;\n" +
              "\t\t\tbreak;\n" +
              "\t\t}\n" +
              "\t}\n\n" +
              "\tif($index == -1) {\n" +
              "\t\t$index = $stringOptionCount;\n" +
              "\t\tsetAttr -type \"string\" miDefaultOptions.stringOptions[$index].name \"rast motion factor\";\n" +
              "\t\tsetAttr -type \"string\" miDefaultOptions.stringOptions[$index].type \"scalar\";\n" +
              "\t\tsetAttr -type \"string\" miDefaultOptions.stringOptions[$index].value \"1.0\";\n" +
              "\t}\n\n" +
              "\treturn $index;\n" +
              "}\n\n" +
              "int $index = tm_miFindMotionFactorIndex();\n" +
              "setAttr -type \"string\" (\"miDefaultOptions.stringOptions[\" + $index + \"].value\") " +
              motionQualityFactor + ";\n");
        out.write("\n");
      }

      /* caustics */ 
      {
        Range range = new Range(0.0, 1.0);
        Range cfkRange = new Range(0.0, 4.0);
        Range range3d = new Range(new Tuple3d(0.0, 0.0, 0.0), new Tuple3d(1.0, 1.0, 1.0));
        boolean useCaustics = getSingleBooleanParamValue(aUseCaustics);  
        int accuracy  = getSingleIntegerParamValue(aCausticsAccuracy); 
        double radius = getSingleDoubleParamValue(aCausticsRadius); 
        int filter    = getSingleEnumParamIndex(aCausticFilterType);
        double kernel = getSingleDoubleParamValue(aCausticFilterKernel, cfkRange);
        Tuple3d causticScale = getSingleTuple3dParamValue(aCausticScale, range3d, false);
        
        out.write
          ("// CAUSTICS\n" +
           "setAttr \"miDefaultOptions.caustics\" " + useCaustics + ";\n" +
           "setAttr \"miDefaultOptions.causticAccuracy\" " + accuracy + ";\n" +
           "setAttr \"miDefaultOptions.causticScaleR\" " + causticScale.x() + ";\n" +
           "setAttr \"miDefaultOptions.causticScaleG\" " + causticScale.y() + ";\n" +
           "setAttr \"miDefaultOptions.causticScaleB\" " + causticScale.z() + ";\n" +
           "setAttr \"miDefaultOptions.causticRadius\" " + radius + ";\n" +
           "setAttr \"miDefaultOptions.causticFilterType\" " + filter + ";\n" +
           "setAttr \"miDefaultOptions.causticFilterKernel\" " + kernel + ";\n\n");
      }

      /* global illumination */ 
      {
        Range range = new Range(0.0, 1.0);
        Range range3d = new Range(new Tuple3d(0.0, 0.0, 0.0), new Tuple3d(1.0, 1.0, 1.0));
        boolean useGlobalIllum = getSingleBooleanParamValue(aUseGlobalIllum);  
        int accuracy   = getSingleIntegerParamValue(aGlobalIllumAccuracy); 
        double radius  = getSingleDoubleParamValue(aGlobalIllumRadius); 
        int paccuracy  = getSingleIntegerParamValue(aPhotonVolumeAccuracy); 
        double pradius = getSingleDoubleParamValue(aPhotonVolumeRadius); 
        int reflect    = getSingleIntegerParamValue(aMaxReflectionPhotons); 
        int refract    = getSingleIntegerParamValue(aMaxRefractionPhotons); 
        int depth      = getSingleIntegerParamValue(aMaxPhotonDepth); 
        Tuple3d globalIllumScale = getSingleTuple3dParamValue(aGlobalIllumScale, range3d, false);
        String file = getSingleStringParamValue(aPhotonMapFile); 
        boolean mapvis  = getSingleBooleanParamValue(aEnableGIMapVisualizer);  
        boolean rebuild = getSingleBooleanParamValue(aPhotonMapRebuild);
        boolean photonAutoVolume = getSingleBooleanParamValue(aPhotonAutoVolume);  
        boolean direct  = getSingleBooleanParamValue(aDirectIllumShadowEffects);  

        if(file == null) 
          file = "";
        
        out.write
          ("// GLOBAL ILLUMINATION\n" +
           "setAttr \"miDefaultOptions.globalIllum\" " + useGlobalIllum + ";\n" +
           "setAttr \"miDefaultOptions.globalIllumAccuracy\" " + accuracy + ";\n" +
           "setAttr \"miDefaultOptions.globalIllumScaleR\" " + globalIllumScale.x() + ";\n" +
           "setAttr \"miDefaultOptions.globalIllumScaleG\" " + globalIllumScale.y() + ";\n" +
           "setAttr \"miDefaultOptions.globalIllumScaleB\" " + globalIllumScale.z() + ";\n" +
           "setAttr \"miDefaultOptions.globalIllumRadius\" " + radius + ";\n" +
           "setAttr \"miDefaultOptions.photonAutoVolume\" " + photonAutoVolume + ";\n" +
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
        Range range = new Range(0.0, 1.0);
        Range range3d = new Range(new Tuple3d(0.0, 0.0, 0.0), new Tuple3d(1.0, 1.0, 1.0));
        Range range16Bit = new Range(-32768, 32767);
        boolean useFinalGather = getSingleBooleanParamValue(aUseFinalGather);  
        boolean precomp = getSingleBooleanParamValue(aPrecompPhotonLookup);  
        int rays = getSingleIntegerParamValue(aFinalGatherRays); 
        double pointDensity = getSingleDoubleParamValue(aPointDensity);
        int finalGatherPoints = getSingleIntegerParamValue(aPointInterpolation, range16Bit);
        Tuple3d finalGatherScale = getSingleTuple3dParamValue(aFinalGatherScale, range3d, false);
        double minRadius = getSingleDoubleParamValue(aMinRadius); 
        double maxRadius = getSingleDoubleParamValue(aMaxRadius); 
        int filter = getSingleIntegerParamValue(aFilter); 
        double falloffStart = getSingleDoubleParamValue(aFalloffStart); 
        double falloffStop  = getSingleDoubleParamValue(aFalloffStop); 
        int depth   = getSingleIntegerParamValue(aTraceDepth); 
        int reflect = getSingleIntegerParamValue(aTraceReflection); 
        int refract = getSingleIntegerParamValue(aTraceRefraction);
        boolean finalGatherView = getSingleBooleanParamValue(aFinalGatherView);
        boolean optimizeForAnim = getSingleBooleanParamValue(aOptimizeForAnim);
        boolean useRadiusQualityControl = getSingleBooleanParamValue(aUseRadiusQualityControl);
        boolean finalGatherTraceDiffuse = getSingleBooleanParamValue(aFinalGatherTraceDiffuse);
        Tuple3d finalGatherBounceScale = getSingleTuple3dParamValue(aFGBounceScale, range3d, false);
        String file = getSingleStringParamValue(aFinalGatherMapFile); 
        boolean mapvis  = getSingleBooleanParamValue(aEnableFGMapVisualizer);  
        int rebuild = getSingleEnumParamIndex(aFinalGatherMapRebuild);  
        boolean preview = getSingleBooleanParamValue(aPreviewFinalGather);
        int finalGatherMode = 0;
        
        if(file == null) 
          file = "";

        if (optimizeForAnim) {
          finalGatherMode = 3;
        }
        else {
          if (useRadiusQualityControl)
            finalGatherMode = 1;
          else
            finalGatherMode = 2;
        }
        
        out.write
          ("// FINAL GATHER\n" +
           "setAttr \"miDefaultOptions.finalGather\" " + useFinalGather + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherFast\" " + precomp + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherRays\" " + rays + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherPresampleDensity\" " + pointDensity + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherPoints\" " + finalGatherPoints + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherScaleR\" " + finalGatherScale.x() + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherScaleG\" " + finalGatherScale.y() + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherScaleB\" " + finalGatherScale.z() + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherMode\" " + finalGatherMode + ";\n");
        
        if (optimizeForAnim || useRadiusQualityControl)
          out.write
            ("setAttr \"miDefaultOptions.finalGatherMinRadius\" " + minRadius + ";\n" +
             "setAttr \"miDefaultOptions.finalGatherMaxRadius\" " + maxRadius + ";\n" +
             "setAttr \"miDefaultOptions.finalGatherView\" " + finalGatherView + ";\n");
        
        out.write
          ("setAttr \"miDefaultOptions.finalGatherFilter\" " + filter + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherFalloffStart\" " + falloffStart + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherFalloffStop\" " + falloffStop + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherTraceDepth\" " + depth + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherTraceReflection\" " + reflect + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherTraceRefraction\" " + refract + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherTraceDiffuse\" " + finalGatherTraceDiffuse + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherBounceScaleR\" " + finalGatherBounceScale.x() + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherBounceScaleG\" " + finalGatherBounceScale.y() + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherBounceScaleB\" " + finalGatherBounceScale.z() + ";\n" +
           "setAttr -type \"string\" \"miDefaultOptions.finalGatherFilename\" " + 
           "\"" + file + "\";\n" +
           "setAttr \"miDefaultOptions.finalGatherMapVisualizer\" " + mapvis + ";\n" +
           "setAttr \"miDefaultOptions.finalGatherRebuild\" " + rebuild + ";\n" +
           "setAttr \"mentalrayGlobals.previewFinalGatherTiles\" " + preview + ";\n\n");
      }

      /* diagnostics */
      {
        boolean diagnoseSamples = getSingleBooleanParamValue(aDiagnoseSamples); 
        int diagnoseBsp = getSingleEnumParamIndex(aDiagnoseBsp);
        int diagnoseGrid = getSingleEnumParamIndex(aDiagnoseGrid);
        double diagnoseGridSize = getSingleDoubleParamValue(aDiagnoseGridSize);
        int diagnosePhoton = getSingleEnumParamIndex(aDiagnosePhoton);
        double diagnosePhotonDensity = getSingleDoubleParamValue(aDiagnosePhotonDensity);
        boolean diagnoseFinalg = getSingleBooleanParamValue(aDiagnoseFinalGather);

        out.write
          ("// DIAGNOSTICS\n" +
           "setAttr \"miDefaultOptions.diagnoseSamples\" " + diagnoseSamples + ";\n" +
           "setAttr \"miDefaultOptions.diagnoseBsp\" " + diagnoseBsp + ";\n" +
           "setAttr \"miDefaultOptions.diagnoseGrid\" " + diagnoseGrid + ";\n" +
           "setAttr \"miDefaultOptions.diagnoseGridSize\" " + diagnoseGridSize + ";\n" +
           "setAttr \"miDefaultOptions.diagnosePhoton\" " + diagnosePhoton + ";\n" +
           "setAttr \"miDefaultOptions.diagnosePhotonDensity\" " + diagnosePhotonDensity + ";\n" +
           "setAttr \"miDefaultOptions.diagnoseFinalg\" " + diagnoseFinalg + ";\n\n");
      }

      /* render options */
      {
        boolean volumeShaders = getSingleBooleanParamValue(aVolumeShaders);
        boolean geometryShaders = getSingleBooleanParamValue(aGeometryShaders);
        boolean displacementShaders = getSingleBooleanParamValue(aDisplacementShaders);
        boolean outputShaders = getSingleBooleanParamValue(aOutputShaders);
        boolean autoVolume = getSingleBooleanParamValue(aAutoVolume);
        boolean displacePresample = getSingleBooleanParamValue(aDisplacePresample);
        boolean mergeSurfaces = getSingleBooleanParamValue(aMergeSurfaces);
        boolean renderHair = getSingleBooleanParamValue(aRenderFurHair);
        boolean renderPasses = getSingleBooleanParamValue(aRenderPasses);
        int faces = getSingleEnumParamIndex(aFaces);
        int volumeSamples = getSingleIntegerParamValue(aVolumeSamples);
        double maxDisplace = getSingleDoubleParamValue(aMaxDisplace);
        double biasShadowMaps = getSingleDoubleParamValue(aShadowMapBias);
        int causticsGenerating = getSingleEnumParamIndex(aCausticsGenerating);
        int causticsReceiving = getSingleEnumParamIndex(aCausticsReceiving);
        int globalIllumGenerating= getSingleEnumParamIndex(aGlobalIllumGenerating);
        int globalIllumReceiving = getSingleEnumParamIndex(aGlobalIllumReceiving);
        String approx = getSingleStringParamValue(aSurfaceApprox);
        String displaceApprox = getSingleStringParamValue(aDisplaceApprox);

        if (approx != null)
          approx = approx.trim();
        if (displaceApprox != null)
          displaceApprox = displaceApprox.trim();

        out.write
          ("// DIAGNOSTICS\n" +
           "setAttr \"miDefaultOptions.volumeShaders\" " + volumeShaders + ";\n" +
           "setAttr \"miDefaultOptions.geometryShaders\" " + geometryShaders + ";\n" +
           "setAttr \"miDefaultOptions.displacementShaders\" " + displacementShaders + ";\n" +
           "setAttr \"miDefaultOptions.outputShaders\" " + outputShaders + ";\n" +
           "setAttr \"miDefaultOptions.autoVolume\" " + autoVolume + ";\n" +
           "setAttr \"miDefaultOptions.displacePresample\" " + displacePresample + ";\n" +
           "setAttr \"miDefaultOptions.mergeSurfaces\" " + mergeSurfaces + ";\n" +
           "setAttr \"miDefaultOptions.renderHair\" " + renderHair + ";\n" +
           "setAttr \"miDefaultOptions.renderPasses\" " + renderPasses + ";\n" +
           "setAttr \"miDefaultOptions.faces\" " + faces + ";\n" + 
           "setAttr \"miDefaultOptions.volumeSamples\" " + volumeSamples + ";\n" +
           "setAttr \"miDefaultOptions.maxDisplace\" " + maxDisplace + ";\n" +
           "setAttr \"miDefaultOptions.biasShadowMaps\" " + biasShadowMaps + ";\n" +
           "setAttr \"miDefaultOptions.causticsGenerating\" " + causticsGenerating + ";\n" + 
           "setAttr \"miDefaultOptions.causticsReceiving\" " + causticsReceiving + ";\n" +
           "setAttr \"miDefaultOptions.globalIllumGenerating\" " + globalIllumGenerating + ";\n" +
           "setAttr \"miDefaultOptions.globalIllumReceiving\" " + globalIllumReceiving + ";\n");

        if (approx != null && !approx.isEmpty() && !approx.contains(" "))
          out.write
            ("if ((`objExists " + approx + "`) && " +
             "(`objectType " + approx + "` == \"mentalraySurfaceApprox\") && " +
             "(!`isConnected " + approx +".message miDefaultOptions.approx`))\n" + 
             "\tconnectAttr -f " + approx + ".message miDefaultOptions.approx;\n");

        if (displaceApprox != null && !displaceApprox.isEmpty() && !displaceApprox.contains(" "))
          out.write
            ("if ((`objExists " + displaceApprox + "`) && " +
             "(`objectType " + displaceApprox + "` == \"mentalrayDisplaceApprox\") && " +
             "(!`isConnected " + displaceApprox +".message miDefaultOptions.displaceApprox`))\n" + 
             "\tconnectAttr -f " + displaceApprox + ".message miDefaultOptions.displaceApprox;\n");

        out.write("\n");
      }

      /* framebuffer attributes */ 
      {
        int dtype = getSingleEnumParamIndex(aDataType);
        if((dtype < 0) || (dtype > 17)) 
        {
          throw new PipelineException
          ("The DataType (" + dtype + ") was illegal!");
        }
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

      /* contours */
      {
        Range range = new Range(0.0, 1.0);
        Range range3d = new Range(new Tuple3d(0.0, 0.0, 0.0), new Tuple3d(1.0, 1.0, 1.0));
        Range cfsRange = new Range(0.0, 2.0);
        Range angleRange = new Range(0.0, 180.0);
        boolean contourEnable = getSingleBooleanParamValue(aEnableContourRendering);
        boolean contourClearImage = getSingleBooleanParamValue(aHideSource);
        Tuple3d contourClearColor = getSingleTuple3dParamValue(aFloodColor, range3d, false);
        int contourSamples = getSingleIntegerParamValue(aOverSample);
        int contourFilter = getSingleEnumParamIndex(aContourFilterType);
        double contourFilterSupport = getSingleDoubleParamValue(aContourFilterSupport, cfsRange);
        boolean contourBackground = getSingleBooleanParamValue(aAroundSilhouette);
        boolean contourPriData = getSingleBooleanParamValue(aAroundAllPolyFaces);
        boolean contourNormalGeom = getSingleBooleanParamValue(aAroundCoplanarFaces);
        boolean contourInstance = getSingleBooleanParamValue(aBetweenDiffInstances);
        boolean contourMaterial = getSingleBooleanParamValue(aBetweenDiffMaterials);
        boolean contourLabel = getSingleBooleanParamValue(aBetweenDiffLabels);
        boolean contourPriIdx = getSingleBooleanParamValue(aAroundRenderTessellation);
        boolean contourInvNormal = getSingleBooleanParamValue(aFrontVsBackFaceContours);
        boolean enableContourColor = getSingleBooleanParamValue(aEnableColorContrast);
        Tuple3d contourColor = getSingleTuple3dParamValue(aColorContrast, range3d, false);
        boolean enableContourDepth = getSingleBooleanParamValue(aEnableDepthContrast);
        double contourDepth = getSingleDoubleParamValue(aDepthContrast);
        boolean enableContourDist = getSingleBooleanParamValue(aEnableDistanceContrast);
        double contourDist = getSingleDoubleParamValue(aDistanceContrast);
        boolean enableContourNormal = getSingleBooleanParamValue(aEnableNormalContrast);
        double contourNormal = getSingleDoubleParamValue(aNormalContrast, angleRange);
        boolean enableContourTexUV = getSingleBooleanParamValue(aEnableUVContours);
        Tuple2d contourTex = getSingleTuple2dParamValue(aUVContours);
        String contrastShader = getSingleStringParamValue(aContrastShader);
        String storeShader = getSingleStringParamValue(aStoreShader);

        if (contrastShader != null)
          contrastShader = contrastShader.trim();
        if (storeShader != null)
          storeShader = storeShader.trim();

        out.write
          ("// CONTOURS\n" + 
           "setAttr \"miDefaultFramebuffer.contourEnable\" " + contourEnable + ";\n" +
           "setAttr \"miDefaultFramebuffer.contourClearImage\" " + contourClearImage + ";\n" +
           "setAttr \"miDefaultFramebuffer.contourClearColorR\" " + contourClearColor.x() + ";\n" +
           "setAttr \"miDefaultFramebuffer.contourClearColorG\" " + contourClearColor.y() + ";\n" +
           "setAttr \"miDefaultFramebuffer.contourClearColorB\" " + contourClearColor.z() + ";\n" +
           "setAttr \"miDefaultFramebuffer.contourSamples\" " + contourSamples + ";\n" +	
           "setAttr \"miDefaultFramebuffer.contourFilter\" " + contourFilter + ";\n" +
           "setAttr \"miDefaultFramebuffer.contourFilterSupport\" " + contourFilterSupport + ";\n" +
           "setAttr \"miDefaultOptions.contourBackground\" " + contourBackground + ";\n" +
           "setAttr \"miDefaultOptions.contourPriData\" " + contourPriData + ";\n" +
           "setAttr \"miDefaultOptions.contourNormalGeom\" " + contourNormalGeom + ";\n" +
           "setAttr \"miDefaultOptions.contourInstance\" " + contourInstance + ";\n" +
           "setAttr \"miDefaultOptions.contourMaterial\" " + contourMaterial + ";\n" +
           "setAttr \"miDefaultOptions.contourLabel\" " + contourLabel + ";\n" +
           "setAttr \"miDefaultOptions.contourPriIdx\" " + contourPriIdx + ";\n" +
           "setAttr \"miDefaultOptions.contourInvNormal\" " + contourInvNormal + ";\n" +
           "setAttr \"miDefaultOptions.enableContourColor\" " + enableContourColor + ";\n" +
           "setAttr \"miDefaultOptions.contourColorR\" " + contourColor.x() + ";\n" +
           "setAttr \"miDefaultOptions.contourColorG\" " + contourColor.y() + ";\n" +
           "setAttr \"miDefaultOptions.contourColorB\" " + contourColor.z() + ";\n" +
           "setAttr \"miDefaultOptions.enableContourDepth\" " + enableContourDepth + ";\n" +
           "setAttr \"miDefaultOptions.contourDepth\" " + contourDepth + ";\n" +
           "setAttr \"miDefaultOptions.enableContourDist\" " + enableContourDist + ";\n" +
           "setAttr \"miDefaultOptions.contourDist\" " + contourDist + ";\n" +
           "setAttr \"miDefaultOptions.enableContourNormal\" " + enableContourNormal + ";\n" +
           "setAttr \"miDefaultOptions.contourNormal\" " + contourNormal + ";\n" +
           "setAttr \"miDefaultOptions.enableContourTexUV\" " + enableContourTexUV + ";\n" +
           "setAttr \"miDefaultOptions.contourTexU\" " + contourTex.x() + ";\n" +
           "setAttr \"miDefaultOptions.contourTexV\" " + contourTex.y() + ";\n");

        if (contrastShader != null && !contrastShader.isEmpty() && !contrastShader.contains(" "))
          out.write
            ("if ((`objExists " + contrastShader + "`) && " +
             "(!`isConnected " + contrastShader +".message miDefaultOptions.contourContrast`))\n" + 
             "\tconnectAttr -f " + contrastShader + ".message miDefaultOptions.contourContrast;\n");


        if (storeShader != null && !storeShader.isEmpty() && !storeShader.contains(" "))
          out.write
            ("if ((`objExists " + storeShader + "`) && " +
             "(!`isConnected " + storeShader +".message miDefaultOptions.contourStore`))\n" + 
             "\tconnectAttr -f " + storeShader + ".message miDefaultOptions.contourStore;\n");

        out.write("\n");
      }
      
      /* memory and performance */ 
      {
        int method = -1;
        switch(getSingleEnumParamIndex(aAccelerationMethod)) {
        case 0:
          method = 0;
          break;
          
        case 1:
          method = 3;
          break;
          
        case 2:
          method = 1;
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
        boolean bspShadow = getSingleBooleanParamValue(aBspShadow); 

        out.write
          ("// MEMORY AND PERFORMANCE ATTRIBUTES\n" + 
           "setAttr \"mentalrayGlobals.accelerationMethod\" " + method + ";\n" +
           "setAttr \"mentalrayGlobals.bspSize\" " + bspSize + ";\n" +
           "setAttr \"mentalrayGlobals.bspDepth\" " + bspDepth + ";\n" +
           "setAttr \"mentalrayGlobals.bspShadow\" " + bspShadow + ";\n" +
           "setAttr \"mentalrayGlobals.gridResolution\" " + gridRes + ";\n" +
           "setAttr \"mentalrayGlobals.gridMaxSize\" " + gridSize + ";\n" +
           "setAttr \"mentalrayGlobals.gridDepth\" " + gridDepth + ";\n" +
           "setAttr \"mentalrayGlobals.taskSize\" " + taskSize + ";\n" +
           "setAttr \"mentalrayGlobals.jlpm\" " + memory + ";\n\n");
      }

      /* shadows */
      {
        int shadowMethod = getSingleEnumParamIndex(aShadowMethod);
        int shadowLinking = getSingleEnumParamIndex(aShadowLinking);
        boolean shadowsObeyShadowLinking = false;
        boolean shadowsObeyLightLinking = false;
        int shadowMaps = getSingleEnumParamIndex(aShadowMapsFormat);
        int rebuildShadowMaps = getSingleEnumParamIndex(aShadowMapsRebuildMode);
        boolean motionBlurShadowMaps  = getSingleBooleanParamValue(aMotionBlurShadowMaps);

        switch(shadowLinking) {
        case 0:
          shadowsObeyShadowLinking = true;
          break;
          
        case 1:
          shadowsObeyLightLinking = true;
          break;
          
        default: //do nothing...
          break;
        }

        out.write
          ("// SHADOWS \n" +
           "setAttr \"miDefaultOptions.shadowMethod\" " + shadowMethod + ";\n" +
           "setAttr \"miDefaultOptions.shadowMaps\" " + shadowMaps + ";\n" +
           "setAttr \"miDefaultOptions.rebuildShadowMaps\" " + rebuildShadowMaps + ";\n" +
           "setAttr \"miDefaultOptions.motionBlurShadowMaps\" " + motionBlurShadowMaps + ";\n" +
           "setAttr \"mentalrayGlobals.shadowsObeyShadowLinking\" " + shadowsObeyShadowLinking + ";\n" +
           "setAttr \"mentalrayGlobals.shadowsObeyLightLinking\" " + shadowsObeyLightLinking + ";\n\n");
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
        int exportHair = getSingleEnumParamIndex(aExportHair);
        boolean exportVertexColors = getSingleBooleanParamValue(aExportVertexColors);
        boolean exportAssignedOnly = getSingleBooleanParamValue(aPruneObjectsWOMaterial);
        boolean exportVisibleOnly = getSingleBooleanParamValue(aOptNonAnimDisplayVis);
        boolean optimizeAnimateDetection = getSingleBooleanParamValue(aOptAnimDetection);
        boolean exportSharedVertices = getSingleBooleanParamValue(aOptVertSharing);
        boolean optimizeRaytraceShadows = getSingleBooleanParamValue(aOptRaytraceShadows);
        boolean exportMotionSegments = getSingleBooleanParamValue(aExportMotionSegments);
        boolean exportTriangles = getSingleBooleanParamValue(aExportTriangulatedPoly);
        boolean exportShapeDeformation = getSingleBooleanParamValue(aExportShapeDeformation);
        boolean exportPolygonDerivatives = getSingleBooleanParamValue(aExportPolygonDerivatives);
        boolean mayaDerivatives = getSingleBooleanParamValue(aMayaDerivatives);
        boolean smoothPolygonDerivatives = getSingleBooleanParamValue(aSmoothPolygonDerivatives);
        boolean exportNurbsDerivatives = getSingleBooleanParamValue(aExportNurbsDerivatives);
        boolean exportObjectsOnDemand = getSingleBooleanParamValue(aExportObjectsOnDemand);
        int exportPlaceholderSize = getSingleIntegerParamValue(aPerformanceThreshold);
        boolean renderShadersWithFiltering = getSingleBooleanParamValue(aRenderShadersWithFilter);
        boolean exportStateShader = getSingleBooleanParamValue(aExportStateShader);
        boolean exportLightLinker = getSingleBooleanParamValue(aExportLightLinker);
        boolean exportMayaOptions = getSingleBooleanParamValue(aExprtMayaOptions);
        boolean exportCustomColors = getSingleBooleanParamValue(aCustomColors);
        boolean exportCustom = getSingleBooleanParamValue(aCustomTexts);
        boolean exportCustomData = getSingleBooleanParamValue(aCustomData);
        boolean exportCustomVectors = getSingleBooleanParamValue(aCustomVectors);

        out.write
          ("// TRANSLATION\n" + 
           "setAttr \"mentalrayGlobals.exportVerbosity\" " + verbose + ";\n" +
           "setAttr \"mentalrayGlobals.exportExactHierarchy\" " + hier + ";\n" +
           "setAttr \"mentalrayGlobals.exportFullDagpath\" " + dag + ";\n" +
           "setAttr \"mentalrayGlobals.exportTexturesFirst\" " + textures + ";\n" +
           "setAttr \"mentalrayGlobals.exportParticles\" " + particles + ";\n" +
           "setAttr \"mentalrayGlobals.exportParticleInstances\" " + pinst + ";\n" +
           "setAttr \"mentalrayGlobals.exportFluids\" " + fluids + ";\n" +
           "setAttr \"mentalrayGlobals.exportPostEffects\" " + effects + ";\n" +
           "setAttr \"mentalrayGlobals.exportHair\" " + exportHair + ";\n" +
           "setAttr \"mentalrayGlobals.exportVertexColors\" " + exportVertexColors + ";\n" +
           "setAttr \"mentalrayGlobals.exportAssignedOnly\" " + exportAssignedOnly + ";\n" +
           "setAttr \"mentalrayGlobals.exportVisibleOnly\" " + exportVisibleOnly + ";\n" +
           "setAttr \"mentalrayGlobals.optimizeAnimateDetection\" " + optimizeAnimateDetection + ";\n" +
           "setAttr \"mentalrayGlobals.exportSharedVertices\" " + exportSharedVertices + ";\n" +
           "setAttr \"mentalrayGlobals.optimizeRaytraceShadows\" " + optimizeRaytraceShadows + ";\n" +
           "setAttr \"mentalrayGlobals.exportMotionSegments\" " + exportMotionSegments + ";\n" +
           "setAttr \"mentalrayGlobals.exportTriangles\" " + exportTriangles + ";\n" +
           "setAttr \"mentalrayGlobals.exportShapeDeformation\" " + exportShapeDeformation + ";\n" +
           "setAttr \"mentalrayGlobals.exportPolygonDerivatives\" " + exportPolygonDerivatives + ";\n" +
           "setAttr \"mentalrayGlobals.mayaDerivatives\" " + mayaDerivatives + ";\n" +
           "setAttr \"mentalrayGlobals.smoothPolygonDerivatives\" " + smoothPolygonDerivatives + ";\n" +
           "setAttr \"mentalrayGlobals.exportNurbsDerivatives\" " + exportNurbsDerivatives + ";\n" +
           "setAttr \"mentalrayGlobals.exportObjectsOnDemand\" " + exportObjectsOnDemand + ";\n" +
           "setAttr \"mentalrayGlobals.exportPlaceholderSize\" " + exportPlaceholderSize + ";\n" +
           "setAttr \"mentalrayGlobals.renderShadersWithFiltering\" " + renderShadersWithFiltering + ";\n" +
           "setAttr \"mentalrayGlobals.exportStateShader\" " + exportStateShader + ";\n" +
           "setAttr \"mentalrayGlobals.exportLightLinker\" " + exportLightLinker + ";\n" +
           "setAttr \"mentalrayGlobals.exportMayaOptions\" " + exportMayaOptions + ";\n" +
           "setAttr \"mentalrayGlobals.exportCustomColors\" " + exportCustomColors + ";\n" +
           "setAttr \"mentalrayGlobals.exportCustom\" " + exportCustom + ";\n" +
           "setAttr \"mentalrayGlobals.exportCustomData\" " + exportCustomData + ";\n" +
           "setAttr \"mentalrayGlobals.exportCustomVectors\" " + exportCustomVectors + ";\n\n");
      }

      /* preview */
      {
        boolean previewAnimation = getSingleBooleanParamValue(aPreviewAnimation);
        boolean previewMotionBlur = getSingleBooleanParamValue(aPreviewMotionBlur);
        boolean previewRenderTiles = getSingleBooleanParamValue(aPreviewRenderTiles);
        boolean previewConvertTiles = getSingleBooleanParamValue(aPreviewConvertTiles);
        boolean previewTonemapTiles = getSingleBooleanParamValue(aPreviewTonemapTiles);
        double tonemapRangeHigh = getSingleDoubleParamValue(aTonemapScale);

        out.write
          ("// PREVIEW\n" + 
           "setAttr \"mentalrayGlobals.previewAnimation\" " + previewAnimation + ";\n" +
           "setAttr \"mentalrayGlobals.previewMotionBlur\" " + previewMotionBlur + ";\n" +
           "setAttr \"mentalrayGlobals.previewRenderTiles\" " + previewRenderTiles + ";\n" +
           "setAttr \"mentalrayGlobals.previewConvertTiles\" " + previewConvertTiles + ";\n" +
           "setAttr \"mentalrayGlobals.previewTonemapTiles\" " + previewTonemapTiles + ";\n" +
           "setAttr \"mentalrayGlobals.tonemapRangeHigh\" " + tonemapRangeHigh + ";\n\n");
      }

      /* custom entities */
      {
        boolean passAlphaThrough = getSingleBooleanParamValue(aPassCustomAlphaChannel);
        boolean passDepthThrough = getSingleBooleanParamValue(aPassCustomDepthChannel);
        boolean passLabelThrough = getSingleBooleanParamValue(aPassCustomLabelChannel);
        String versions = getSingleStringParamValue(aCustomVersions);
        String links = getSingleStringParamValue(aCustomLinks);
        String includes = getSingleStringParamValue(aCustomIncludes);

        out.write
          ("// CUSTOM ENTITIES\n" + 
           "setAttr \"mentalrayGlobals.passAlphaThrough\" " + passAlphaThrough + ";\n" +
           "setAttr \"mentalrayGlobals.passDepthThrough\" " + passDepthThrough + ";\n" +
           "setAttr \"mentalrayGlobals.passLabelThrough\" " + passLabelThrough + ";\n");

        String customEntities = "";
        if (versions != null)
          customEntities = "setAttr -type \"string\" \"mentalrayGlobals.versions\" \"" + versions + "\";\n";
        if (links != null)
          customEntities = customEntities.concat("setAttr -type \"string\" \"mentalrayGlobals.links\" \"" + links + "\";\n");
        if (includes != null)
          customEntities = customEntities.concat("setAttr -type \"string\" \"mentalrayGlobals.includes\" \"" + includes + "\";\n");
        customEntities = customEntities.concat("\n");

        out.write(customEntities);
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

  public static final String aResolutionSource          = "ResolutionSource";
  public static final String aImageWidth                = "ImageWidth";
  public static final String aImageHeight               = "ImageHeight";
  public static final String aPixelAspectRatio          = "PixelAspectRatio";
  public static final String aImageResolution           = "ImageResolution";
  public static final String aMinSampleLevel            = "MinSampleLevel";
  public static final String aMaxSampleLevel            = "MaxSampleLevel";
  public static final String aPixelFilterType           = "PixelFilterType";
  public static final String aPixelFilterWidthX         = "PixelFilterWidthX";
  public static final String aPixelFilterWidthY         = "PixelFilterWidthY";
  public static final String aThreshold                 = "Threshold";
  public static final String aCoverageThreshold         = "AlphaThreshold";
  public static final String aSampleLock                = "SampleLock";
  public static final String aJitter                    = "Jitter";
  public static final String aUseRaytracing             = "UseRaytracing";
  public static final String aReflections               = "Reflections";
  public static final String aRefractions               = "Refractions";
  public static final String aMaxTraceDepth             = "MaxTraceDepth";
  public static final String aShadows                   = "Shadows";
  public static final String aScanline                  = "PrimaryRenderer";
  public static final String aFaces                     = "ObjectFaces";
  public static final String aMotionBlur                = "MotionBlur";
  public static final String aMotionBlurBy              = "MotionBlurBy";
  public static final String aShutterClose              = "ShutterClose";
  public static final String aShutterOpen               = "ShutterOpen";
  public static final String aTimeContrast              = "TimeContrast";
  public static final String aTimeContrastAlpha         = "AlphaTimeContrast";
  public static final String aMotionSteps               = "MotionSteps";
  public static final String aUseCaustics               = "UseCaustics";
  public static final String aCausticsAccuracy          = "CausticsAccuracy";
  public static final String aCausticsRadius            = "CausticsRadius";
  public static final String aCausticFilterType         = "CausticFilterType";
  public static final String aCausticFilterKernel       = "CausticFilterKernel";
  public static final String aUseGlobalIllum            = "UseGlobalIllum";
  public static final String aGlobalIllumAccuracy       = "GlobalIllumAccuracy";
  public static final String aGlobalIllumRadius         = "GlobalIllumRadius";
  public static final String aPhotonVolumeAccuracy      = "PhotonVolumeAccuracy";
  public static final String aPhotonVolumeRadius        = "PhotonVolumeRadius";
  public static final String aMaxReflectionPhotons      = "PhotonReflections";
  public static final String aMaxRefractionPhotons      = "PhotonRefractions";
  public static final String aMaxPhotonDepth            = "MaxPhotonDepth";
  public static final String aPhotonMapFile             = "PhotonMapFile";
  public static final String aEnableGIMapVisualizer     = "EnableGIMapVisualizer";
  public static final String aPhotonMapRebuild          = "RebuildPhotonMap";
  public static final String aDirectIllumShadowEffects  = "DirectIllumShadowEffects";
  public static final String aUseFinalGather            = "UseFinalGather";
  public static final String aPrecompPhotonLookup       = "PrecompPhotonLookup";
  public static final String aFinalGatherRays           = "Accuracy";
  public static final String aMinRadius                 = "MinRadius";
  public static final String aMaxRadius                 = "MaxRadius";
  public static final String aFilter                    = "Filter";
  public static final String aFalloffStart              = "FalloffStart";
  public static final String aFalloffStop               = "FalloffStop";
  public static final String aTraceDepth                = "TraceDepth";
  public static final String aTraceReflection           = "TraceReflection";
  public static final String aTraceRefraction           = "TraceRefraction";
  public static final String aFinalGatherMapFile        = "FinalGatherMapFile";
  public static final String aEnableFGMapVisualizer     = "EnableFGMapVisualizer";
  public static final String aFinalGatherMapRebuild     = "RebuildFinalGatherMap";
  public static final String aPreviewFinalGather        = "PreviewFinalGatherTiles";
  public static final String aOptimizeForAnim           = "OptimizeForAnimation";
  public static final String aUseRadiusQualityControl   = "UseRadiusQualityControl";
  public static final String aDataType                  = "DataType";
  public static final String aGamma                     = "Gamma";
  public static final String aColorClip                 = "ColorClip";
  public static final String aInterpSamples             = "InterpSamples";
  public static final String aDesaturate                = "Desaturate";
  public static final String aPremultiply               = "Premultiply";
  public static final String aDither                    = "Dither";
  public static final String aAccelerationMethod        = "AccelerationMethod";
  public static final String aBspSize                   = "BspSize";
  public static final String aBspDepth                  = "BspDepth";
  public static final String aGridResolution            = "GridResolution";
  public static final String aGridMaxSize               = "GridMaxSize";
  public static final String aGridDepth                 = "GridDepth";
  public static final String aTaskSize                  = "TaskSize";
  public static final String aPhysicalMemory            = "PhysicalMemory";
  public static final String aExportVerbosity           = "ExportVerbosity";
  public static final String aExportExactHierarchy      = "ExportExactHierarchy";
  public static final String aExportFullDagpath         = "ExportFullDagpath";
  public static final String aExportTexturesFirst       = "ExportTexturesFirst";
  public static final String aExportParticles           = "ExportParticles";
  public static final String aExportParticleInstances   = "ExportParticleInstances";
  public static final String aExportFluids              = "ExportFluids";
  public static final String aExportPostEffects         = "ExportPostEffects";
  public static final String aQuality                   = "QualityPresets";
  public static final String aVisibilitySamples         = "VisibilitySamples";
  public static final String aShadingQuality            = "ShadingQuality";
  public static final String aReflectionBlurLimit       = "ReflectionBlurLimit";
  public static final String aRefractionBlurLimit       = "RefractionBlurLimit";
  public static final String aBspShadow                 = "SeparateShadowMap";
  public static final String aShadowMethod              = "ShadowMethod";
  public static final String aShadowLinking             = "ShadowLinking";
  public static final String aShadowMapsFormat          = "ShadowMapsFormat";
  public static final String aShadowMapsRebuildMode     = "ShadowMapsRebuildMode";
  public static final String aMotionBlurShadowMaps      = "MotionBlurShadowMaps";
  public static final String aMotionQualityFactor       = "MotionQualityFactor";
  public static final String aCustomMotionOffsets       = "CustomMotionOffsets";
  public static final String aMotionBackOffset          = "MotionBackOffset";
  public static final String aStaticObjectOffset        = "StaticObjectOffset";
  public static final String aTimeSamples               = "TimeSamples";
  public static final String aCausticScale              = "CausticScale";
  public static final String aGlobalIllumScale          = "GlobalIllumScale";
  public static final String aPhotonAutoVolume          = "PhotonAutoVolume";
  public static final String aPointDensity              = "PointDensity";
  public static final String aPointInterpolation        = "PointInterpolation";
  public static final String aFinalGatherScale          = "FinalGatherScale";
  public static final String aFinalGatherView           = "ViewRadiiInPixelSize";
  public static final String aFinalGatherTraceDiffuse   = "SecondaryDiffuseBounces";
  public static final String aFGBounceScale             = "SecondaryBounceScale";
  public static final String aDiagnoseSamples           = "DiagnoseSamples";
  public static final String aDiagnoseBsp               = "DiagnoseBsp";
  public static final String aDiagnoseGrid              = "DiagnoseGrid";
  public static final String aDiagnoseGridSize          = "DiagnoseGridSize";
  public static final String aDiagnosePhoton            = "DiagnosePhoton";
  public static final String aDiagnosePhotonDensity     = "DiagnosePhotonDensity";
  public static final String aDiagnoseFinalGather       = "DiagnoseFinalGather";
  public static final String aVolumeShaders             = "VolumeShaders";
  public static final String aGeometryShaders           = "GeometryShaders";
  public static final String aDisplacementShaders       = "DisplacementShaders";
  public static final String aOutputShaders             = "OutputShaders";
  public static final String aAutoVolume                = "AutoVolume";
  public static final String aDisplacePresample         = "DisplacePresample";
  public static final String aMergeSurfaces             = "MergeSurfaces";
  public static final String aRenderFurHair             = "RenderFurHair";
  public static final String aRenderPasses              = "RenderPasses";
  public static final String aVolumeSamples             = "VolumeSamples";
  public static final String aMaxDisplace               = "MaxDisplace";
  public static final String aShadowMapBias             = "ShadowMapBias";
  public static final String aSurfaceApprox             = "TessellationSurfaceApprox";
  public static final String aDisplaceApprox            = "TessellationDisplaceApprox";
  public static final String aCausticsGenerating        = "CausticsGenerating";
  public static final String aCausticsReceiving         = "CausticsReceiving";
  public static final String aGlobalIllumGenerating     = "GlobalIllumGenerating";
  public static final String aGlobalIllumReceiving      = "GlobalIllumReceiving";
  public static final String aEnableContourRendering    = "EnableContourRendering";
  public static final String aHideSource                = "HideSource";
  public static final String aFloodColor                = "FloodColor";
  public static final String aOverSample                = "OverSample";
  public static final String aContourFilterType         = "ContourFilterType";
  public static final String aContourFilterSupport      = "ContourFilterSupport";
  public static final String aAroundSilhouette          = "AroundSilhouette";
  public static final String aAroundAllPolyFaces        = "AroundAllPolyFaces";
  public static final String aAroundCoplanarFaces       = "AroundCoplanarFaces";
  public static final String aBetweenDiffInstances      = "BetweenDiffInstances";
  public static final String aBetweenDiffMaterials      = "BetweenDiffMaterials";
  public static final String aBetweenDiffLabels         = "BetweenDiffLabels";
  public static final String aAroundRenderTessellation  = "AroundRenderTessellation";
  public static final String aFrontVsBackFaceContours   = "FrontVsBackFaceContours";
  public static final String aEnableColorContrast       = "EnableColorContrast";
  public static final String aColorContrast             = "ColorContrast";
  public static final String aEnableDepthContrast       = "EnableDepthContrast";
  public static final String aDepthContrast             = "DepthContrast";
  public static final String aEnableDistanceContrast    = "EnableDistanceContrast";
  public static final String aDistanceContrast          = "DistanceContrast";
  public static final String aEnableNormalContrast      = "EnableNormalContrast";
  public static final String aNormalContrast            = "NormalContrast";
  public static final String aEnableUVContours          = "EnableUVContours";
  public static final String aUVContours                = "UVContours";
  public static final String aContrastShader            = "ContrastShader";
  public static final String aStoreShader               = "StoreShader";
  public static final String aExportHair                = "ExportHair";
  public static final String aExportVertexColors        = "ExportVertexColors";
  public static final String aPruneObjectsWOMaterial    = "PruneObjectsWithoutMaterial";
  public static final String aOptNonAnimDisplayVis      = "OptimizeNonanimatedDisplayVisibility";
  public static final String aOptAnimDetection          = "OptimizeAnimationDetection";
  public static final String aOptVertSharing            = "OptimizeVertexSharing";
  public static final String aOptRaytraceShadows        = "OptimizeRaytraceShadows";
  public static final String aExportMotionSegments      = "ExportMotionSegments";
  public static final String aExportTriangulatedPoly    = "ExportTriangulatedPolygons";
  public static final String aExportShapeDeformation    = "ExportShapeDeformation";
  public static final String aExportPolygonDerivatives  = "ExportPolygonDerivatives";
  public static final String aMayaDerivatives           = "MayaDerivatives";
  public static final String aSmoothPolygonDerivatives  = "SmoothPolygonDerivatives";
  public static final String aExportNurbsDerivatives    = "ExportNurbsDerivatives";
  public static final String aExportObjectsOnDemand     = "ExportObjectsOnDemand";
  public static final String aPerformanceThreshold      = "PerformanceThreshold";
  public static final String aRenderShadersWithFilter   = "RenderShadersWithFiltering";
  public static final String aExportStateShader         = "ExportStateShader";
  public static final String aExportLightLinker         = "ExportLightLinker";
  public static final String aExprtMayaOptions          = "ExportMayaOptions";
  public static final String aCustomColors              = "ExportCustomColors";
  public static final String aCustomTexts               = "ExportCustomTexts";
  public static final String aCustomData                = "ExportCustomData";
  public static final String aCustomVectors             = "ExportCustomVectors";
  public static final String aPreviewAnimation          = "PreviewAnimation";
  public static final String aPreviewMotionBlur         = "PreviewMotionBlur";
  public static final String aPreviewRenderTiles        = "PreviewRenderTiles";
  public static final String aPreviewConvertTiles       = "PreviewConvertTiles";
  public static final String aPreviewTonemapTiles       = "PreviewTonemapTiles";
  public static final String aTonemapScale              = "TonemapScale";
  public static final String aPassCustomAlphaChannel    = "PassCustomAlphaChannel";
  public static final String aPassCustomDepthChannel    = "PassCustomDepthChannel";
  public static final String aPassCustomLabelChannel    = "PassCustomLabelChannel";
  public static final String aCustomVersions            = "CustomVersions";
  public static final String aCustomLinks               = "CustomLinks";
  public static final String aCustomIncludes            = "CustomIncludes";
  public static final String aCreateDefaultNodes        = "CreateDefaultNodes";
  public static final String aAlphaMaskChannel          = "AlphaMaskChannel";
  public static final String aZDepthChannel             = "ZDepthChannel";

  private static final long serialVersionUID = -7887483141312167689L;
}

