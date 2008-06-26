// $Id: AppDirs.java,v 1.13 2008/06/26 05:34:29 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   A P P   D I R S                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The types of application specific directories used in the production.
 */ 
public  
enum AppDirs
{
  /*-- MAYA --------------------------------------------------------------------------------*/

  /**
   * Maya scenes. 
   */ 
  Maya, 

  /**
   * Maya geometry caches. 
   */ 
  Cache, 

  /**
   * General purpose MEL scripts.
   */ 
  MEL, 

  /**
   * MEL scripts for attaching shaders to geometry. 
   */ 
  ShadeMEL, 

  /**
   * MEL scripts for doing rigging and binding operations. 
   */ 
  RigMEL, 

  /**
   * MEL scripts for ??? (something to do with the Match task).
   */ 
  MatchMEL, 

  /**
   * Pre-render MEL scripts to setup a scene for rendering a particular pass. 
   */ 
  RenderSetupMEL, 

  /**
   * Miscellaneous MEL scripts.
   */ 
  MiscMEL, 


  /*-- HOUDINI -----------------------------------------------------------------------------*/
  
  /**
   * Houdini's GEO/BGEO format model files. 
   */ 
  Geo, 

  /**
   * Houdini's HIP scene files. 
   */ 
  Hip, 

  /**
   * Houdini's command file.
   */
  Cmd,

  /**
   * Houdini's OTL file.
   */
  Otl,

  /**
   * Exported Houdini channels.
   */
  Chan,

  /**
   * Houdini render input files. 
   */
  Ifd,

  /**
   * Houdini RAT format texture/image files.
   */
  Rat,



  /*-- IMAGES ------------------------------------------------------------------------------*/

  /**
   * Generic images. 
   */ 
  Images, 

  /**
   * Images of roughly 2048x1556 resolution.
   */ 
  Image2k, 

  /**
   * Images of roughly 1024x778 resolution.
   */ 
  Image1k, 

  /**
   * Cineon format images.
   */ 
  Cineon, 

  /**
   * DPX format images.
   */ 
  DPX, 

  /**
   * Raw digital camera images.
   */ 
  Raw, 

  /**
   * Roto generated matte images. 
   */ 
  Mattes, 

  /**
   * High dynamic range (HDR) images. 
   */ 
  HDR, 

  /**
   * Images used to compute the lens distortion.
   */ 
  Grids, 

  /**
   * Images containing UV values used in the lens distortion process.
   */ 
  UV, 

  /**
   * Generic texture images.
   */ 
  Texture, 

  /**
   * Generic rendered images. 
   */ 
  Render, 

  /**
   * Generic composited images. 
   */ 
  Comp, 

  /**
   * QuickTime movies.
   */
  QuickTime,

  /**
   * EXR image format files. 
   */
  Exr,


  /*-- MISC MODELS -------------------------------------------------------------------------*/

  /**
   * OBJ format model files. 
   */ 
  Obj, 

  /**
   * IGES format model files. 
   */ 
  Iges, 


  /*-- OTHERS ------------------------------------------------------------------------------*/

  /**
   * Nuke scripts.
   */ 
  Nuke, 

  /**
   * PFTrack scenes.
   */ 
  PFTrack, 

  /**
   * Source code for various compiled formats such as shaders, VEX operators, etc... 
   */ 
  Source, 

  /**
   * Placeholder files.
   */ 
  Placeholder, 

  /**
   * Miscellaneous data files not easily catagorized.
   */
  Misc;


  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return the conventional name for a directory containing files of this type. 
   */ 
  public String 
  toDirName() 
  {
    switch(this) {
    case ShadeMEL:
      return "shade/mel"; 

    case RigMEL:
      return "rig/mel"; 
      
    case MatchMEL:
      return "match/mel"; 
      
    case RenderSetupMEL:
      return "render_setup/mel"; 

    case MiscMEL:
      return "misc/mel"; 

    case Image2k:
      return "2k";

    case Image1k:
      return "1k";
      
    case Cineon:
      return "cin";

    case QuickTime:
      return "qt";
      
    case Source:
      return "src";
      
    default:
      return super.toString().toLowerCase(); 
    }
  }
  
  /**
   * Return the conventional name (as a Path) for a directory containing files of this type. 
   */ 
  public Path 
  toDirPath() 
  {
    return new Path(toDirName()); 
  }
}
