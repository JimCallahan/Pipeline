// $Id: NodeStyles.java,v 1.5 2008/05/04 00:40:22 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.Color;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T Y L E S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of static methods related to the graphic representation of node and queue
 * states. <P> 
 */
public
class NodeStyles
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the color associated with the given selection mode. 
   */ 
  public static Color3d
  getSelectionColor3d
  (
   SelectionMode mode
  ) 
  { 
    UserPrefs prefs = UserPrefs.getInstance();

    switch(mode) {
    case Normal:
      return prefs.getNormalRingColor();
      
    case Selected:
      return prefs.getSelectedRingColor();
      
    case Primary:
      return prefs.getPrimaryRingColor();

    default:
      assert(false) : ("Somehow the SelectionMode (" + mode + ") was not handled!");
      return null;
    }
  }

  /**
   * Gets the color associated with the given selection mode. 
   */ 
  public static Color
  getSelectionColor
  (
   SelectionMode mode
  ) 
  {
    Color3d color = getSelectionColor3d(mode);
    return new Color((float) color.r(), (float) color.g(), (float) color.b());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the color associated with the given overall queue state.
   */ 
  public static Color3d
  getQueueColor3d
  (
   OverallQueueState state
  ) 
  { 
    UserPrefs prefs = UserPrefs.getInstance();

    if(state == null) 
      return prefs.getUndefinedCoreColor();

    switch(state) {
    case Undefined:
      return prefs.getUndefinedCoreColor();
          
    case Finished:
      return prefs.getFinishedCoreColor();
      
    case Stale:
      return prefs.getStaleCoreColor();
      
    case Queued:
      return prefs.getQueuedCoreColor();
      
    case Paused:
      return prefs.getPausedCoreColor();
          
    case Running:
      return prefs.getRunningCoreColor();
          
    case Aborted:
      return prefs.getAbortedCoreColor();
          
    case Failed:
      return prefs.getFailedCoreColor();

    case Dubious:
      return prefs.getDubiousCoreColor();

    default:
      assert(false) : ("Somehow the OverallQueueState (" + state + ") was not handled!");
      return null;
    }
  }
 
  /**
   * Gets the color associated with the given overall queue state.
   */ 
  public static Color
  getQueueColor
  (
   OverallQueueState state
  ) 
  {
    Color3d color = getQueueColor3d(state);
    if(color == null) 
      return null;
    
    return new Color((float) color.r(), (float) color.g(), (float) color.b());
  }
 
  /**
   * Gets the name of the color associated with the given overall queue state.
   */ 
  @Deprecated
  public static String
  getQueueColorString
  (
   OverallQueueState state
  ) 
  {
    // THESE ARE NOT ACCURATE ANYMORE NOW THAT USER PREFERENCES CAN OVERRIDE THE COLORS!

    if(state == null) 
      return sQueueStateColorNames[0];
    return sQueueStateColorNames[state.ordinal()];
  }
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the color associated with the given job state.
   */ 
  public static Color3d
  getJobColor3d
  (
   JobState state
  ) 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    if(state == null) 
      return prefs.getUndefinedCoreColor();

    switch(state) {
    case Finished:
      return prefs.getFinishedCoreColor();
      
    case Queued:
      return prefs.getQueuedCoreColor();
      
    case Paused:
      return prefs.getPausedCoreColor();
      
    case Running:
      return prefs.getRunningCoreColor();
      
    case Aborted:
      return prefs.getAbortedCoreColor();
      
    case Failed:
      return prefs.getFailedCoreColor();   
      
    case Preempted: 
      return prefs.getPreemptedCoreColor();

    default:
      assert(false) : ("Somehow the JobState (" + state + ") was not handled!");
      return null;
    }
  }
 
  /**
   * Gets the color associated with the given overall job state.
   */ 
  public static Color
  getJobColor
  (
   JobState state
  ) 
  {
    Color3d color = getJobColor3d(state);
    if(color == null) 
      return null;
    
    return new Color((float) color.r(), (float) color.g(), (float) color.b());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the colors corresponding to specific OverallQueueState values.
   */ 
  private static final String[] sQueueStateColorNames = {
    "dark grey", /* Undefined */ 
    "blue",      /* Finished */ 
    "purple",    /* Stale */ 
    "cyan",      /* Queued */ 
    "yellow",    /* Paused */ 
    "green",     /* Running */ 
    "orange",    /* Aborted */ 
    "red",       /* Failed */ 
    "black"      /* Dubious */ 
  };

}
