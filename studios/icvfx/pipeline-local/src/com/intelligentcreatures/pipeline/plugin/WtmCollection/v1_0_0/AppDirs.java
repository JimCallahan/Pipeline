// $Id: AppDirs.java,v 1.1 2008/02/06 07:21:06 jim Exp $

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


  /*-- IMAGES ------------------------------------------------------------------------------*/

  /**
   * Images of roughly 2048x1556 resolution.
   */ 
  Image2k, 

  /**
   * Images of roughly 1024x778 resolution.
   */ 
  Image1k, 


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
      return "mel/shade"; 

    case RigMEL:
      return "mel/rig"; 
      
    case MatchMEL:
      return "mel/match"; 
      
    case RenderSetupMEL:
      return "mel/render_setup"; 

    case MiscMEL:
      return "mel/misc"; 

    case Image2k:
      return "2k";

    case Image1k:
      return "1k";
      
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
