// $Id: UserPrefs.java,v 1.3 2004/05/14 02:40:59 jim Exp $

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


  /**
   * Get the collapse node action hot key.
   */ 
  public HotKey
  getCollapseNode() 
  {
    return pCollapseNode;
  }
  
  /**
   * Set the collapse node action hot key.
   */ 
  public void 
  setCollapseNode
  (
   HotKey key
  ) 
  {
    pCollapseNode = key;
  }


  /**
   * Get the expand node action hot key.
   */ 
  public HotKey
  getExpandNode() 
  {
    return pExpandNode;
  }
  
  /**
   * Set the expand node action hot key.
   */ 
  public void 
  setExpandNode
  (
   HotKey key
  ) 
  {
    pExpandNode = key;
  }


  /**
   * Get the expand all nodes action hot key.
   */ 
  public HotKey
  getExpandAllNodes() 
  {
    return pExpandAllNodes;
  }
  
  /**
   * Set the expand all nodes action hot key.
   */ 
  public void 
  setExpandAllNodes
  (
   HotKey key
  ) 
  {
    pExpandAllNodes = key;
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

    pCollapseNode = null;
    pExpandNode = null;
    pExpandAllNodes = null;

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
    /* node viewer */ 
    {
      encoder.encode("NodeSpaceX", pNodeSpaceX);
      encoder.encode("NodeSpaceY", pNodeSpaceY);
      encoder.encode("NodeOffset", pNodeOffset);
      
      encoder.encode("CollapseNode",  pCollapseNode);
      encoder.encode("ExpandNode",    pExpandNode);
      encoder.encode("ExpandAllNodes", pExpandAllNodes);
    }


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


      HotKey collapse = (HotKey) decoder.decode("CollapseNode");
      if(collapse != null)
	pCollapseNode = collapse;

      HotKey expand = (HotKey) decoder.decode("ExpandNode");
      if(expand != null)
	pExpandNode = expand;

      HotKey expandAll = (HotKey) decoder.decode("ExpandAllNodes");
      if(expandAll != null)
	pExpandAllNodes = expandAll;
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


  /*-- NODE VIEWER -------------------------------------------------------------------------*/

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


  /**
   * The collapse node action hot key.
   */ 
  private HotKey  pCollapseNode;

  /**
   * The expand node action hot key.
   */ 
  private HotKey  pExpandNode;

  /**
   * The expand all nodes action hot key.
   */ 
  private HotKey  pExpandAllNodes;

  
}
