// $Id: SelectionMode.java,v 1.2 2004/05/29 06:38:06 jim Exp $

package us.temerity.pipeline.ui;

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
