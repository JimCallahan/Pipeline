// $Id: PanelGroup.java,v 1.2 2004/08/25 05:22:45 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   P A N E L   G R O U P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Manages a set of {@link JTopLevelPanel JTopLevelPanel} subclass instances identified
 * by a group ID.
 */ 
public 
class PanelGroup<T> 
{
  /**
   * Construct a new panel group.
   */ 
  public
  PanelGroup() 
  {
    pPanels = new ArrayList<T>(10);

    int wk;
    for(wk=0; wk<10; wk++) 
      pPanels.add(null);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the given panel group currently unused.
   */ 
  public synchronized boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    if((groupID < 1) || (groupID > 9))
      throw new IndexOutOfBoundsException
	("The group ID (" + groupID + ") must be in the range: [1,9]!");

    return (pPanels.get(groupID) == null);      
  }

  /**
   * Get the panel belonging to the given group.
   * 
   * @return 
   *   The panel or <CODE>null</CODE> if no panel exists for the group.
   */ 
  public synchronized T
  getPanel
  (
   int groupID
  ) 
  {
    if((groupID < 1) || (groupID > 9))
      throw new IndexOutOfBoundsException
	("The group ID (" + groupID + ") must be in the range: [1,9]!");

    return pPanels.get(groupID);
  }

  /**
   * Get all panels assigned to a group.
   */ 
  public synchronized LinkedList<T>
  getPanels() 
  {
    LinkedList<T> panels = new LinkedList<T>();
    int wk;
    for(wk=1; wk<10; wk++) {
      T panel = getPanel(wk);
      if(panel != null) 
	panels.add(panel);
    }

    return panels;
  }

  /**
   * Assign the given panel to the given group.
   */ 
  public synchronized void 
  assignGroup
  (
   T panel, 
   int groupID
  ) 
  {
    if((groupID < 1) || (groupID > 9))
      throw new IndexOutOfBoundsException
	("The group ID (" + groupID + ") must be in the range: [1,9]!");

    if(pPanels.get(groupID) != null)
      throw new IllegalStateException
	("The group ID (" + groupID + ") is currently in use!");
	
    pPanels.set(groupID, panel);
  }

  /**
   * Make the given panel group available.
   */ 
  public synchronized void 
  releaseGroup 
  (
   int groupID
  ) 
  {
    if((groupID < 1) || (groupID > 9))
      throw new IndexOutOfBoundsException
	("The group ID (" + groupID + ") must be in the range: [1,9]!");

    if(pPanels.get(groupID) == null)
      throw new IllegalStateException
	("The group ID (" + groupID + ") is currently unused!");

    pPanels.set(groupID, null);
  }

  /**
   * Remove all panels.
   */ 
  public synchronized void 
  clear() 
  {
    int wk;
    for(wk=0; wk<10; wk++) 
      pPanels.set(wk, null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of active panels indexed by assigned group: [1-9]. <P> 
   * 
   * If no panel is assigned to the group, the element will be 
   * <CODE>null</CODE>.  The (0) element is always <CODE>null</CODE>, because the (0) 
   * group ID means unassinged.
   */ 
  private ArrayList<T>  pPanels;
}
