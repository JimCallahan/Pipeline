// $Id: NodeStyles.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

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
   * Gets the color associated with the given overall queue state.
   */ 
  public static Color
  getQueueColor
  (
   OverallQueueState state
  ) 
  {
    if(state == null) 
      return sQueueStateColors[0];
    return sQueueStateColors[state.ordinal()];
  }
 
  /**
   * Gets the name of the color associated with the given overall queue state.
   */ 
  public static String
  getQueueColorString
  (
   OverallQueueState state
  ) 
  {
    if(state == null) 
      return sQueueStateColorNames[0];
    return sQueueStateColorNames[state.ordinal()];
  }
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the color associated with the given job state.
   */ 
  public static Color
  getJobColor
  (
   JobState state
  ) 
  {
    if(state == null) 
      return sJobStateColors[0];
    return sJobStateColors[state.ordinal()];
  }
 
  /**
   * Gets all of the color associated with job states.
   */ 
  public static Color[]
  getJobColors()
  {
    return sJobStateColors;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The colors corresponding to specific OverallQueueState values.
   */ 
  private static final Color[] sQueueStateColors = {
    new Color(0.20f, 0.20f, 0.20f),  /* Undefined */ 
    new Color(0.00f, 0.00f, 0.65f),  /* Finished */ 
    new Color(0.45f, 0.00f, 0.45f),  /* Stale */ 
    new Color(0.00f, 0.49f, 0.49f),  /* Queued */ 
    new Color(0.90f, 0.90f, 0.00f),  /* Paused */ 
    new Color(0.00f, 0.49f, 0.00f),  /* Running */ 
    new Color(0.75f, 0.49f, 0.00f),  /* Aborted */ 
    new Color(0.65f, 0.00f, 0.00f)   /* Failed */ 
  };

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
    "red"        /* Failed */ 
  };

  
  /**
   * The colors corresponding to specific JobState values.
   */ 
  private static final Color[] sJobStateColors = {
    new Color(0.00f, 0.49f, 0.49f),  /* Queued */ 
    new Color(0.90f, 0.90f, 0.00f),  /* Paused */ 
    new Color(0.75f, 0.49f, 0.00f),  /* Aborted */ 
    new Color(0.00f, 0.49f, 0.00f),  /* Running */ 
    new Color(0.00f, 0.00f, 0.65f),  /* Finished */ 
    new Color(0.65f, 0.00f, 0.00f)   /* Failed */ 
  };

 
}
