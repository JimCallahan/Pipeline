// $Id: LayoutOrientation.java,v 1.1 2009/12/18 19:56:44 jim Exp $

package us.temerity.pipeline.ui;

import java.util.ArrayList; 

/*------------------------------------------------------------------------------------------*/
/*   L A Y O U T   O R I E N T A T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to specify alignment of node networks and job groups in the Node Viewer and Job 
 * Viewer panels.
 */ 
public 
enum LayoutOrientation
{
  /**
   * Layout the entities from left to right along a horizontal line.
   */ 
  Horizontal, 

  /**
   * ayout the entities from top to bottom along a vertical line.
   */ 
  Vertical; 



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<LayoutOrientation>
  all() 
  {
    LayoutOrientation values[] = values();
    ArrayList<LayoutOrientation> all = new ArrayList<LayoutOrientation>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return toString();
  }
}
