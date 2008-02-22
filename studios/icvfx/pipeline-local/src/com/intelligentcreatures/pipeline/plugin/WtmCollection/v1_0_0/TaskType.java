// $Id: TaskType.java,v 1.8 2008/02/22 09:22:29 jim Exp $

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
   * A builder for constructing the nodes associated with the HDRI task.
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
   * Primarily used to validate the a Maya scene containing the tracked camera and head 
   * model. The validation process includes performing MEL based tests on the scene as well 
   * as rendering/comping the shot using a rig and shaders designed to show flaws in the 
   * tracking data.
   */ 
  Tracking,
  
  /**
   *
   */  
  Match, 

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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Miscelaneous task-like components.
   */ 
  Modeling, 
  Rigging, 
  LookDev, 
  Anim, 
  Placeholder, 
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
