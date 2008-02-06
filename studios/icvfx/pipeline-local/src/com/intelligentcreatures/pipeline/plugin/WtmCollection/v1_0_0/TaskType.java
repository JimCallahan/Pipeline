// $Id: TaskType.java,v 1.1 2008/02/06 07:21:06 jim Exp $

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
  Track,

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
  Comp; 



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return the conventional name for a directory containing nodes with this purpose.
   */ 
  public String 
  toDirName() 
  {
    return super.toString().toLowerCase(); 
  }

  /**
   * Return the conventional name (as a Path) for a directory containing nodes with 
   * this purpose.
   */ 
  public Path 
  toDirPath() 
  {
    return new Path(toDirName()); 
  }
}
