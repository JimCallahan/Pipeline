// $Id: UserPrefs.java,v 1.1 2004/05/13 02:37:41 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   U S E R   P R E F S                                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * 
 */ 
public
class UserPrefs
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   */ 
  public 
  UserPrefs()
  {
    reset();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the UserPrefs instance.
   */ 
  public static UserPrefs
  getInstance() 
  {
    return sUserPrefs;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the horizontal distance between nodes in the node viewer.
   */ 
  public double 
  getNodeSpaceX()
  {
    return pNodeSpaceX;
  }

  /**
   * Set the horizontal distance between nodes in the node viewer.
   */ 
  public void
  setNodeSpaceX
  (
   double space
  )
  {
    pNodeSpaceX = space;
  }


  /**
   * Get the vertical distance between nodes in the node viewer.
   */ 
  public double 
  getNodeSpaceY()
  {
    return pNodeSpaceY;
  }

  /**
   * Set the vertical distance between nodes in the node viewer.
   */ 
  public void 
  setNodeSpaceY
  (
   double space
  )
  {
    pNodeSpaceY = space;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Reset all preferences to the default values.
   */ 
  public void 
  reset() 
  {
    pNodeSpaceX = 2.0;
    pNodeSpaceY = 2.0;

    // ...

  }

  /**
   * Save the preferences to disk.
   */ 
  public static void 
  save() 
    throws GlueException, GlueLockException
  {
    File file = new File(PackageInfo.sHomeDir, 
			 PackageInfo.sUser + "/.pipeline/preferences");
    LockedGlueFile.save(file, "UserPreferences", sUserPrefs);
  }
  
  /**
   * Load the preferences from disk.
   */ 
  public static void 
  load() 
    throws GlueException, GlueLockException
  {
    File file = new File(PackageInfo.sHomeDir, 
			 PackageInfo.sUser + "/.pipeline/preferences");
    sUserPrefs = (UserPrefs) LockedGlueFile.load(file);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    encoder.encode("NodeSpaceX", pNodeSpaceX);
    encoder.encode("NodeSpaceY", pNodeSpaceY);

    // ...

  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Double spaceX = (Double) decoder.decode("NodeSpaceX");
    if(spaceX == null)
      throw new GlueException("The \"NodeSpaceX\" cannot be (null)!");

    Double spaceY = (Double) decoder.decode("NodeSpaceY");
    if(spaceY == null)
      throw new GlueException("The \"NodeSpaceY\" cannot be (null)!");

    
    // ...
    
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance of this class.
   */ 
  private static UserPrefs sUserPrefs = new UserPrefs();



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Horizontal distance between nodes in the node viewer.
   */ 
  private double  pNodeSpaceX;
  
  /**
   * Vertical distance between nodes in the node viewer.
   */ 
  private double  pNodeSpaceY;
  
}
