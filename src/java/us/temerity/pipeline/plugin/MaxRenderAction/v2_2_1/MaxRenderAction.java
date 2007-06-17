// $Id: MaxRenderAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MaxRenderAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 
import us.temerity.pipeline.math.Range; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A X  R E N D E R   A C T I O N                                                       */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a series of images from a source 3dsmax scene node. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Max Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source 3dsmax scene node.
 *   </DIV> <BR>
 * 
 *   Scene State <BR>
 *   <DIV style="margin-left: 40px;">
 *     Restore the given scene state before rendering (if set).
 *   </DIV> <BR>
 * 
 *   Camera Override <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the render camera (if set). <BR> 
 *   </DIV> <BR>
 * 
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
 *     The ratio of pixel height to pixel width. <BR>
 *   </DIV> <BR> <BR>
 * 
 * 
 *   Render Preset <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source render preset node which specifies the renderer to use and its settings.
 *     If unset, the current settings from the 3dsmax scene are used.
 *   </DIV> <BR>
 * 
 *   Path Config <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source path configuration file node. 
 *   </DIV> <BR>
 * 
 * 
 *   Render Verbosity<BR>
 *   <DIV style="margin-left: 40px;">
 *     The verbosity of render progress, warning and error messages.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * 
 * 
 *   Pre Render Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MAXScript to sourced before rendering begins. 
 *   </DIV> <BR>
 * 
 *   Post Render Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MAXScript to sourced after rendering ends.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MaxRenderAction
  extends MaxActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MaxRenderAction() 
  {
    super("MaxRender", new VersionID("2.2.1"), "Temerity",
	  "Renders a 3dsmax scene.");
    
    {
      ActionParam param =
	new LinkActionParam
        (aMaxScene, 
         "The source 3dsmax scene node.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
	new StringActionParam
        (aSceneState, 
         "Restore the given scene state before rendering (if set).", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aCameraOverride,
	 "Overrides the render camera (if set).", 
	 null);
      addSingleParam(param);
    }

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
	choices.add("35mm Full Aperture (cine) - 512x389");
	choices.add("35mm Full Aperture (cine) - 1536x1167");
	choices.add("35mm Full Aperture (cine) - 2048x1556");
	choices.add("35mm Full Aperture (cine) - 4096x3112");

	choices.add("35mm Academy (cine) - 457x333");
	choices.add("35mm Academy (cine) - 1371x999");
	choices.add("35mm Academy (cine) - 1828x1332");
	choices.add("35mm Academy (cine) - 3656x2664");

	choices.add("35mm 1.66:1 (cine) - 250x150");
	choices.add("35mm 1.66:1 (cine) - 1024x614");
	choices.add("35mm 1.66:1 (cine) - 1536x921");
	choices.add("35mm 1.66:1 (cine) - 4096x2458");

	choices.add("35mm 1.75:1 (cine) - 350x200");
	choices.add("35mm 1.75:1 (cine) - 1120x640");
	choices.add("35mm 1.75:1 (cine) - 1575x900");
	choices.add("35mm 1.75:1 (cine) - 4096x2340");

	choices.add("35mm 1.85:1 (cine) - 370x200");
	choices.add("35mm 1.85:1 (cine) - 1024x554");
	choices.add("35mm 1.85:1 (cine) - 1536x830");
	choices.add("35mm 1.85:1 (cine) - 4096x2214");

	choices.add("35mm Anamorphic - 256x109");   
	choices.add("35mm Anamorphic - 1024x436"); 
	choices.add("35mm Anamorphic - 1828x778"); 
	choices.add("35mm Anamorphic - 4096x1743"); 

	choices.add("35mm Anamorphic (squeezed) - 256x218"); 
	choices.add("35mm Anamorphic (squeezed) - 1024x871"); 
	choices.add("35mm Anamorphic (squeezed) - 1828x1556"); 
	choices.add("35mm Anamorphic (squeezed) - 4096x3486"); 

	choices.add("70mm Panavision (cine) - 440x200"); 
	choices.add("70mm Panavision (cine) - 1024x465"); 
	choices.add("70mm Panavision (cine) - 1536x698"); 
	choices.add("70mm Panavision (cine) - 4096x1862"); 

	choices.add("70mm IMAX (cine) - 256x188");
	choices.add("70mm IMAX (cine) - 1024x751");
	choices.add("70mm IMAX (cine) - 1536x1126");
	choices.add("70mm IMAX (cine) - 4096x3003");

	choices.add("VistaVision - 360x200");
	choices.add("VistaVision - 1024x683");
	choices.add("VistaVision - 1536x1024");
	choices.add("VistaVision - 4096x2731");

	choices.add("35mm (slide) - 360x240");
	choices.add("35mm (slide) - 1024x683");
	choices.add("35mm (slide) - 1536x1024");
	choices.add("35mm (slide) - 4096x2731");

	choices.add("Square (slide) - 256x256");
	choices.add("Square (slide) - 1024x1024");
	choices.add("Square (slide) - 1536x1536");
	choices.add("Square (slide) - 4096x4096");

	choices.add("4x5 (slide) - 512x410"); 
	choices.add("4x5 (slide) - 1024x819");
	choices.add("4x5 (slide) - 1536x1229");
	choices.add("4x5 (slide) - 4096x3277");

	choices.add("NTSC D-1 (video) - 200x135");  
	choices.add("NTSC D-1 (video) - 360x243");
	choices.add("NTSC D-1 (video) - 512x346");
	choices.add("NTSC D-1 (video) - 720x486");

	choices.add("NTSC DV (video) - 300x200");  
	choices.add("NTSC DV (video) - 360x240");
	choices.add("NTSC DV (video) - 512x341");
	choices.add("NTSC DV (video) - 720x480");

	choices.add("PAL (video) - 180x135");
	choices.add("PAL (video) - 240x180");
	choices.add("PAL (video) - 480x360");
	choices.add("PAL (video) - 769x576");

	choices.add("PAL D-1 (video) - 180x144");  
	choices.add("PAL D-1 (video) - 240x192");
	choices.add("PAL D-1 (video) - 480x384");
	choices.add("PAL D-1 (video) - 720x576");

	choices.add("HDTV (video) - 320x180");
	choices.add("HDTV (video) - 490x270");
	choices.add("HDTV (video) - 1280x720");
	choices.add("HDTV (video) - 1920x1080");

	addPreset(aImageResolution, choices);

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      389);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Full Aperture (cine) - 512x389", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1536);
	  values.put(aImageHeight,      1167);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Full Aperture (cine) - 1536x1167", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       2048);
	  values.put(aImageHeight,      1556);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Full Aperture (cine) - 2048x1556", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      3112);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Full Aperture (cine) - 4096x3112", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       457);
	  values.put(aImageHeight,      333);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Academy (cine) - 457x333", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1371);
	  values.put(aImageHeight,      999);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Academy (cine) - 1371x999", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1828);
	  values.put(aImageHeight,      1332);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Academy (cine) - 1828x1332", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       3656);
	  values.put(aImageHeight,      2664);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Academy (cine) - 3656x2664", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       250);
	  values.put(aImageHeight,      150);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.66:1 (cine) - 250x150", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      614);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.66:1 (cine) - 1024x614", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1536);
	  values.put(aImageHeight,      921);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.66:1 (cine) - 1536x921", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      2458);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.66:1 (cine) - 4096x2458", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       350);
	  values.put(aImageHeight,      200);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.75:1 (cine) - 350x200", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1120);
	  values.put(aImageHeight,      640);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.75:1 (cine) - 1120x640", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1575);
	  values.put(aImageHeight,      900);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.75:1 (cine) - 1575x900", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      2340);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.75:1 (cine) - 4096x2340", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       370);
	  values.put(aImageHeight,      200);
	  values.put(aPixelAspectRatio, 1.0);

	  addPresetValues(aImageResolution, "35mm 1.85:1 (cine) - 370x200", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      554);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.85:1 (cine) - 1024x554", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1536);
	  values.put(aImageHeight,      830);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.85:1 (cine) - 1536x830", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      2214);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm 1.85:1 (cine) - 4096x2214", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       256);
	  values.put(aImageHeight,      109);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Anamorphic - 256x109", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      436);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Anamorphic - 1024x436", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1828);
	  values.put(aImageHeight,      778);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Anamorphic - 1828x778", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      1743);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm Anamorphic - 4096x1743", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       256);
	  values.put(aImageHeight,      218);
	  values.put(aPixelAspectRatio, 2.0);
	
	  addPresetValues(aImageResolution, "35mm Anamorphic (squeezed) - 256x218", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      871);
	  values.put(aPixelAspectRatio, 2.0);
	
	  addPresetValues(aImageResolution, "35mm Anamorphic (squeezed) - 1024x871", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1828);
	  values.put(aImageHeight,      1556);
	  values.put(aPixelAspectRatio, 2.0);
	
	  addPresetValues(aImageResolution, "35mm Anamorphic (squeezed) - 1828x1556", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      3486);
	  values.put(aPixelAspectRatio, 2.0);
	
	  addPresetValues(aImageResolution, "35mm Anamorphic (squeezed) - 4096x3486", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       440);
	  values.put(aImageHeight,      200);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "70mm Panavision (cine) - 440x200", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      465);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "70mm Panavision (cine) - 1024x465", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1536);
	  values.put(aImageHeight,      698);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "70mm Panavision (cine) - 1536x698", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      1862);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "70mm Panavision (cine) - 4096x1862", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       256);
	  values.put(aImageHeight,      188);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "70mm IMAX (cine) - 256x188", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      751);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "70mm IMAX (cine) - 1024x751", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1536);
	  values.put(aImageHeight,      1126);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "70mm IMAX (cine) - 1536x1126", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      3003);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "70mm IMAX (cine) - 4096x3003", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       360);
	  values.put(aImageHeight,      200);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "VistaVision - 360x200", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      683);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "VistaVision - 1024x683", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1536);
	  values.put(aImageHeight,      1024);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "VistaVision - 1536x1024", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      2731);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "VistaVision - 4096x2731", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       360);
	  values.put(aImageHeight,      240);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm (slide) - 360x240", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      683);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm (slide) - 1024x683", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1536);
	  values.put(aImageHeight,      1024);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm (slide) - 1536x1024", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      2731);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "35mm (slide) - 4096x2731", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      410);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "4x5 (slide) - 512x410", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      819);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "4x5 (slide) - 1024x819", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1536);
	  values.put(aImageHeight,      1229);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "4x5 (slide) - 1536x1229", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      3277);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "4x5 (slide) - 4096x3277", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       200);
	  values.put(aImageHeight,      135);
	  values.put(aPixelAspectRatio, 0.9);
	
	  addPresetValues(aImageResolution, "NTSC D-1 (video) - 200x135", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       360);
	  values.put(aImageHeight,      243);
	  values.put(aPixelAspectRatio, 0.9);
	
	  addPresetValues(aImageResolution, "NTSC D-1 (video) - 360x243", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      346);
	  values.put(aPixelAspectRatio, 0.9);
	
	  addPresetValues(aImageResolution, "NTSC D-1 (video) - 512x346", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       720);
	  values.put(aImageHeight,      486);
	  values.put(aPixelAspectRatio, 0.9);
	
	  addPresetValues(aImageResolution, "NTSC D-1 (video) - 720x486", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       300);
	  values.put(aImageHeight,      200);
	  values.put(aPixelAspectRatio, 0.9);
	
	  addPresetValues(aImageResolution, "NTSC DV (video) - 300x200", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       360);
	  values.put(aImageHeight,      240);
	  values.put(aPixelAspectRatio, 0.9);
	
	  addPresetValues(aImageResolution, "NTSC DV (video) - 360x240", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      341);
	  values.put(aPixelAspectRatio, 0.9);
	
	  addPresetValues(aImageResolution, "NTSC DV (video) - 512x341", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       720);
	  values.put(aImageHeight,      480);
	  values.put(aPixelAspectRatio, 0.9);
	
	  addPresetValues(aImageResolution, "NTSC DV (video) - 720x480", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       180);
	  values.put(aImageHeight,      135);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "PAL (video) - 180x135", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       240);
	  values.put(aImageHeight,      180);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "PAL (video) - 240x180", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       480);
	  values.put(aImageHeight,      360);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "PAL (video) - 480x360", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       769);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "PAL (video) - 769x576", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       180);
	  values.put(aImageHeight,      144);
	  values.put(aPixelAspectRatio, 1.0667);
	
	  addPresetValues(aImageResolution, "PAL D-1 (video) - 180x144", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       240);
	  values.put(aImageHeight,      192);
	  values.put(aPixelAspectRatio, 1.0667);
	
	  addPresetValues(aImageResolution, "PAL D-1 (video) - 240x192", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       480);
	  values.put(aImageHeight,      384);
	  values.put(aPixelAspectRatio, 1.0667);
	
	  addPresetValues(aImageResolution, "PAL D-1 (video) - 480x384", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       720);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 1.0667);
	
	  addPresetValues(aImageResolution, "PAL D-1 (video) - 720x576", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       320);
	  values.put(aImageHeight,      180);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "HDTV (video) - 320x180", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       490);
	  values.put(aImageHeight,      270);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "HDTV (video) - 490x270", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1280);
	  values.put(aImageHeight,      720);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "HDTV (video) - 1280x720", values);
	}
        
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1920);
	  values.put(aImageHeight,      1080);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues(aImageResolution, "HDTV (video) - 1920x1080", values);
	}
      }
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aRenderPreset,
	 "The source render preset node which specifies the renderer to use and its " + 
         "settings.  If unset, the current settings from the 3dsmax scene are used.", 
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aPathConfig,
	 "The source path configuration file node.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("Minimal");
      choices.add("Low");
      choices.add("Medium");
      choices.add("High");
      choices.add("Highest");

      ActionParam param = 
	new EnumActionParam
	(aRenderVerbosity,
	 "The verbosity of rendering statistics.",
	 "Medium", choices);
      addSingleParam(param);
    }

    addExtraOptionsParam();

    {
      ActionParam param = 
	new LinkActionParam
	(aPreRenderScript,
	 "The MAXScript to sourced before rendering begins.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aPostRenderScript,
	 "The MAXScript to sourced after rendering ends.", 
	 null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMaxScene);
      layout.addEntry(aSceneState);
      layout.addEntry(aCameraOverride);
      layout.addSeparator();
      layout.addEntry(aImageResolution);
      layout.addEntry(aImageWidth);
      layout.addEntry(aImageHeight);
      layout.addEntry(aPixelAspectRatio);
      layout.addSeparator();
      layout.addEntry(aRenderPreset);
      layout.addEntry(aPathConfig);
      layout.addSeparator();
      layout.addEntry(aRenderVerbosity);
      addExtraOptionsParamToLayout(layout); 

      {
	LayoutGroup sub = 
          new LayoutGroup
	  ("MAXScripts", 
	   "MAXScripts run at various stages of the rendering process.", 
	   true);
	sub.addEntry(aPreRenderScript); 
	sub.addEntry(aPostRenderScript);

	layout.addSubGroup(sub);
      }

      setSingleLayout(layout);
    }

    addSupport(OsType.Windows);   
    removeSupport(OsType.Unix);
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
    /* the target image sequence */ 
    FileSeq target = null;
    {
      target = agenda.getPrimaryTarget();
      String suffix = target.getFilePattern().getSuffix();
      if(suffix == null) 
	throw new PipelineException
	  ("The target file sequence (" + target + ") must have a filename suffix!");

      if(!target.hasFrameNumbers())
        throw new PipelineException
          ("The " + getName() + " Action requires that the output images have frame " + 
           "numbers.");
    }

    /* the source 3dsmax scene */ 
    Path sourceScene = getMaxSceneSourcePath(aMaxScene, agenda);
    if(sourceScene == null) 
      throw new PipelineException 
        ("A source MaxScene must be specified!"); 

    /* command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    {
      args.add("-v:" + getSingleEnumParamIndex(aRenderVerbosity));
      args.add("-rfw:0");

      String state = getSingleStringParamValue(aSceneState);
      if(state != null) 
        args.add("-sceneState:\"" + state + "\"");
      
      Path preset = getPrimarySourcePath(aRenderPreset, agenda, 
                                         "rps", "3dsmax render preset file");
      if(preset != null) 
        args.add("-preset:\"" + preset.toOsString() + "\"");
      
      Path pathConfig = 
        getPrimarySourcePath(aPathConfig, agenda, "mxp", "3dsmax path configuration file");
      if(pathConfig != null) 
        args.add("-pathFile:\"" + pathConfig.toOsString() + "\"");

      Path preRenderScript = getMaxScriptSourcePath(aPreRenderScript, agenda);
      if(preRenderScript != null) 
        args.add("-preRenderScript:\"" + preRenderScript.toOsString() + "\"");
      
      Path postRenderScript = getMaxScriptSourcePath(aPostRenderScript, agenda);
      if(postRenderScript != null) 
        args.add("-postRenderScript:\"" + postRenderScript.toOsString() + "\"");

      FilePattern fpat = target.getFilePattern();
      Path output = new Path(agenda.getTargetPath(), 
                             fpat.getPrefix() + ".." + fpat.getSuffix());
      args.add("-outputName:\"" + output.toOsString() + "\"");

      FrameRange range = target.getFrameRange();
      args.add("-start:" + range.getStart()); 
      args.add("-end:" + range.getEnd());
      args.add("-nthFrame:" + range.getBy());
            
      String camera = getSingleStringParamValue(aCameraOverride);
      if(camera != null) 
        args.add("-camera:" + camera);
      
      int width = getSingleIntegerParamValue(aImageWidth, new Range(1, null)); 
      args.add("-width:" + width);

      int height = getSingleIntegerParamValue(aImageHeight, new Range(1, null)); 
      args.add("-height:" + height);

      double ratio = getSingleDoubleParamValue(aPixelAspectRatio, 
                                               new Range(0.0, null, false));
      args.add("-pixelAspect:" + ratio);

      args.addAll(getExtraOptionsArgs());

      args.add("\"" + sourceScene.toOsString() + "\"");
    }

    /* create the process to run the action */ 
    return createSubProcess(agenda, "3dsmaxcmd.exe", args, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4022411368099310007L;

  public static final String aMaxScene         = "MaxScene";
  public static final String aSceneState       = "SceneState";
  public static final String aCameraOverride   = "CameraOverride";
  public static final String aImageWidth       = "ImageWidth";
  public static final String aImageHeight      = "ImageHeight";
  public static final String aPixelAspectRatio = "PixelAspectRatio";
  public static final String aImageResolution  = "ImageResolution";
  public static final String aRenderPreset     = "RenderPreset";
  public static final String aPathConfig       = "PathConfig";
  public static final String aRenderVerbosity  = "RenderVerbosity";
  public static final String aPreRenderScript  = "PreRenderScript";
  public static final String aPostRenderScript = "PostRenderScript";

}

