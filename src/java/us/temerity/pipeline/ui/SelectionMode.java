// $Id: SelectionMode.java,v 1.1 2004/05/05 20:57:24 jim Exp $

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
    ArrayList<SelectionMode> modes = new ArrayList<SelectionMode>();
    modes.add(Normal);
    modes.add(Selected); 
    modes.add(Primary);

    return modes;
  }
}
