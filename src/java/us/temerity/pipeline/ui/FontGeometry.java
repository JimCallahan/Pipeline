// $Id: FontGeometry.java,v 1.2 2004/12/15 15:38:06 jim Exp $

package us.temerity.pipeline.ui;

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
