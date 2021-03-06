// Automatically generated by "pipeline/data/maya/mel/renderFont.mel"
//   DO NOT EDIT MANUALLY!

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.math.*;

public
class ArialRegularFontGeometry
   implements FontGeometry
{
  /*-----------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                               */
  /*-----------------------------------------------------------------------*/

  public
  ArialRegularFontGeometry()
  {}



  /*-----------------------------------------------------------------------*/
  /*   A C C E S S                                                         */
  /*-----------------------------------------------------------------------*/

  /**
   * Is the character printable?
   */ 
  public boolean
  isPrintable
  (
   char code
  )
  {
    return (sOrigin[code] != null);
  }


  /**
   * The origin of the given character.
   */ 
  public Point3d
  getOrigin
  (
   char code
  )
  {
    if(sOrigin[code] != null)
      return new Point3d(sOrigin[code]);
    return null;
  }

  /**
   * The extent of the given character.
   */ 
  public Vector3d 
  getExtent
  (
   char code
  )
  {
    if(sExtent[code] != null)
      return new Vector3d(sExtent[code]);
    return null;
  }



  /*-----------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                   */
  /*-----------------------------------------------------------------------*/

  private static final Point3d sOrigin[] = {
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    new Point3d(-0.00234375, 0, 0.0),
    null,
    null,
    null,
    null,
    new Point3d(0.1, 0, 0.0),
    new Point3d(0.05, 0.4, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0.05, -0.1, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0, 0.4, 0.0),
    new Point3d(0.05, -0.2, 0.0),
    new Point3d(0, -0.2, 0.0),
    new Point3d(0.05, 0.4, 0.0),
    new Point3d(0.05, 0.05, 0.0),
    new Point3d(0.1, -0.1, 0.0),
    new Point3d(0, 0.2, 0.0),
    new Point3d(0.1, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0.04708872568, 0, 0.0),
    new Point3d(0.1, 0, 0.0),
    new Point3d(0.04998667761, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.1, 0, 0.0),
    new Point3d(0.1, -0.1, 0.0),
    new Point3d(0.05, 0.1, 0.0),
    new Point3d(0.05, 0.2, 0.0),
    new Point3d(0.05, 0.1, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, -0.2, 0.0),
    new Point3d(-0.00703125, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, -0.0203125, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(-0.01484375, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0.05, -0.2, 0.0),
    new Point3d(-0.00234375, 0, 0.0),
    new Point3d(0, -0.2, 0.0),
    new Point3d(0, 0.3, 0.0),
    new Point3d(0, -0.2, 0.0),
    new Point3d(0.05, 0.55, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0.05, -0.2, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(-0.04140625, -0.2, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, -0.2, 0.0),
    new Point3d(0.05, -0.2, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0.05, 0, 0.0),
    new Point3d(-0.0171875, 0, 0.0),
    new Point3d(-0.025, 0, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(-0.01875, -0.2, 0.0),
    new Point3d(0, 0, 0.0),
    new Point3d(0, -0.2, 0.0),
    new Point3d(0.1, -0.2, 0.0),
    new Point3d(0, -0.2, 0.0),
    new Point3d(0, 0.2, 0.0),
    null,
  };

  private static final Vector3d sExtent[] = {
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    new Vector3d(0.30234375, 0.65, 0.0),
    null,
    null,
    null,
    null,
    new Vector3d(0.1, 0.65, 0.0),
    new Vector3d(0.25, 0.25, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.45, 0.8, 0.0),
    new Vector3d(0.7, 0.65, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.1, 0.25, 0.0),
    new Vector3d(0.25, 0.85, 0.0),
    new Vector3d(0.25, 0.85, 0.0),
    new Vector3d(0.25, 0.25, 0.0),
    new Vector3d(0.5, 0.5, 0.0),
    new Vector3d(0.1, 0.2, 0.0),
    new Vector3d(0.25, 0.1, 0.0),
    new Vector3d(0.1, 0.1, 0.0),
    new Vector3d(0.30234375, 0.65, 0.0),
    new Vector3d(0.4029112743, 0.65, 0.0),
    new Vector3d(0.25, 0.65, 0.0),
    new Vector3d(0.4000133224, 0.65, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.45, 0.65, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.1, 0.5, 0.0),
    new Vector3d(0.1, 0.6, 0.0),
    new Vector3d(0.45, 0.45, 0.0),
    new Vector3d(0.45, 0.3, 0.0),
    new Vector3d(0.45, 0.45, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.85, 0.85, 0.0),
    new Vector3d(0.5640625, 0.65, 0.0),
    new Vector3d(0.5, 0.65, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.5, 0.65, 0.0),
    new Vector3d(0.45, 0.65, 0.0),
    new Vector3d(0.6, 0.65, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.1, 0.65, 0.0),
    new Vector3d(0.35, 0.65, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.65, 0.65, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.6, 0.65, 0.0),
    new Vector3d(0.5, 0.65, 0.0),
    new Vector3d(0.6, 0.6703125, 0.0),
    new Vector3d(0.59453125, 0.65, 0.0),
    new Vector3d(0.5, 0.65, 0.0),
    new Vector3d(0.5, 0.65, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.58203125, 0.65, 0.0),
    new Vector3d(0.85, 0.65, 0.0),
    new Vector3d(0.55, 0.65, 0.0),
    new Vector3d(0.6, 0.65, 0.0),
    new Vector3d(0.5, 0.65, 0.0),
    new Vector3d(0.2, 0.85, 0.0),
    new Vector3d(0.30234375, 0.65, 0.0),
    new Vector3d(0.2, 0.85, 0.0),
    new Vector3d(0.35, 0.35, 0.0),
    new Vector3d(0.5, 0.1, 0.0),
    new Vector3d(0.15, 0.1, 0.0),
    new Vector3d(0.4, 0.5, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.3555862549, 0.5, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.4, 0.5, 0.0),
    new Vector3d(0.29921875, 0.65, 0.0),
    new Vector3d(0.4, 0.7, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.1, 0.65, 0.0),
    new Vector3d(0.19140625, 0.85, 0.0),
    new Vector3d(0.4, 0.65, 0.0),
    new Vector3d(0.1, 0.65, 0.0),
    new Vector3d(0.6, 0.5, 0.0),
    new Vector3d(0.4, 0.5, 0.0),
    new Vector3d(0.4, 0.5, 0.0),
    new Vector3d(0.4, 0.7, 0.0),
    new Vector3d(0.4, 0.7, 0.0),
    new Vector3d(0.25, 0.5, 0.0),
    new Vector3d(0.35, 0.5, 0.0),
    new Vector3d(0.2, 0.66328125, 0.0),
    new Vector3d(0.4, 0.5, 0.0),
    new Vector3d(0.490625, 0.5, 0.0),
    new Vector3d(0.7, 0.5, 0.0),
    new Vector3d(0.40703125, 0.5, 0.0),
    new Vector3d(0.46953125, 0.7, 0.0),
    new Vector3d(0.4, 0.5, 0.0),
    new Vector3d(0.3, 0.85, 0.0),
    new Vector3d(0.1, 0.85, 0.0),
    new Vector3d(0.3, 0.85, 0.0),
    new Vector3d(0.5, 0.2, 0.0),
    null,
  };
}
