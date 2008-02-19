// $Id: TaskType.java,v 1.7 2008/02/19 09:26:36 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   T Y P E                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The types of tasks for each shot.
 */ 
public  
enum TaskType
{
  /**
   * 
   */ 
  HDRI, 

  /**
   * Contains the scanned plate images, camera data and any other reference images shot on 
   * set.  Any required painting fixes are applied and then the images are undistored and 
   * linearized.  A GridWarp Nuke node is produced which can be used to redistort rendered
   * images later along with a MEL script to set the undistored image resolution for renders.
   * Finally, the undistored plates are resized down to 1k, a QuickTime movie is built and 
   * a thumbnail image is extracted. 
   */ 
  Plates, 

  /**
   *
   */ 
  Tracking,
  
  /**
   *
   */ 
  Modeling, 

  /**
   *
   */ 
  Rigging, 

  /**
   *
   */ 
  LookDev, 

  /**
   *
   */  
  Match, 

  /**
   *
   */ 
  Anim, 

  /**
   *
   */ 
  Blot,

  /**
   *
   */  
  Noise, 

  /**
   *
   */ 
  Sim, 

  /**
   *
   */ 
  Lighting,

  /**
   *
   */ 
  Comp, 

  /**
   *
   */ 
  Placeholder, 

  /**
   *
   */ 
  Misc; 




  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return the conventional name for a directory containing nodes for this type of task. 
   */ 
  public String 
  toDirName() 
  {
    switch(this) {
    case Modeling:
      return "model";
      
    case Rigging:
      return "rig"; 

    case LookDev:
      return "shade";
	
    default:
      return super.toString().toLowerCase(); 
    }
  }

  /**
   * Return the conventional name (as a Path) for a directory containing nodes for
   * this type of task. 
   */ 
  public Path 
  toDirPath() 
  {
    return new Path(toDirName()); 
  }
}
