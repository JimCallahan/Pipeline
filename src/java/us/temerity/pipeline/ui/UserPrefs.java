// $Id: UserPrefs.java,v 1.2 2004/05/13 21:29:16 jim Exp $

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
  /*   N O D E   V I E W E R                                                                */
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


  /**
   * Get the vertical offset distance for nodes with an odd depth level in the node viewer.
   */ 
  public double 
  getNodeOffset()
  {
    return pNodeOffset;
  }

  /**
   * Set the vertical offset distance for nodes with an odd depth level in the node viewer.
   */ 
  public void 
  setNodeOffset
  (
   double offset
  )
  {
    pNodeOffset = offset;
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
    pNodeOffset = 0.5;

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

    System.out.print("Prefs loaded: " + file + "\n");
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
    /* node viewer */ 
    encoder.encode("NodeSpaceX", pNodeSpaceX);
    encoder.encode("NodeSpaceY", pNodeSpaceY);
    encoder.encode("NodeOffset", pNodeOffset);

    // ...

  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    /* node viewer */ 
    {
      Double spaceX = (Double) decoder.decode("NodeSpaceX");
      if(spaceX != null)
	pNodeSpaceX = spaceX;
      
      Double spaceY = (Double) decoder.decode("NodeSpaceY");
      if(spaceY != null)
	pNodeSpaceY = spaceY;
      
      Double offset = (Double) decoder.decode("NodeOffset");
      if(offset != null)
	pNodeOffset = offset;
    }

    
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
   * The horizontal distance between nodes in the node viewer.
   */ 
  private double  pNodeSpaceX;
  
  /**
   * The vertical distance between nodes in the node viewer.
   */ 
  private double  pNodeSpaceY;
  
  /**
   * The vertical offset distance for nodes with an odd depth level in the node viewer.
   */ 
  private double  pNodeOffset;

  
}
