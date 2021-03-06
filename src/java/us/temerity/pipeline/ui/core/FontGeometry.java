// $Id: FontGeometry.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.math.*;

/*------------------------------------------------------------------------------------------*/
/*   F O N T   G E O M E T R Y                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A description of the geometry of the characters which make up a font.
 */
public
interface FontGeometry
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the character printable?
   */ 
  public boolean
  isPrintable
  (
   char code
  );
    

  /**
   * The origin of the given character.
   */ 
  public Point3d
  getOrigin
  (
   char code
  );

  /**
   * The extent of the given character.
   */ 
  public Vector3d 
  getExtent
  (
   char code
  );
}
