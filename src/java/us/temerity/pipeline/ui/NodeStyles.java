// $Id: NodeStyles.java,v 1.3 2004/07/22 00:08:19 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.Color;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T Y L E S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of static methods related to the graphic representation of node state.
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
    new Color(0.00f, 0.49f, 0.00f),  /* Running */ 
    new Color(0.75f, 0.49f, 0.00f),  /* Aborted */ 
    new Color(0.00f, 0.00f, 0.65f)   /* Failed */ 
  };

  /**
   * The names of the colors corresponding to specific OverallQueueState values.
   */ 
  private static final String[] sQueueStateColorNames = {
    "dark grey", /* Undefined */ 
    "blue",      /* Finished */ 
    "purple",    /* Stale */ 
    "cyan",      /* Queued */ 
    "green",     /* Running */ 
    "orange",    /* Aborted */ 
    "red"        /* Failed */ 
  };

  
 
}
