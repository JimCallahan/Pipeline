// $Id: SelectionMode.java,v 1.1 2005/01/03 06:56:25 jim Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   M O D E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The selection state of nodes the {@link JNodeViewerPanel JNodeViewerPanel}.
 */
public
enum SelectionMode
{  
  /**
   * The node is unselected.
   */
  Normal,

  /**
   * The node is selection.
   */
  Selected, 

  /**
   * The node is the primary selection.
   */
  Primary;


  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<SelectionMode>
  all() 
  {
    SelectionMode values[] = values();
    ArrayList<SelectionMode> all = new ArrayList<SelectionMode>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }
}
