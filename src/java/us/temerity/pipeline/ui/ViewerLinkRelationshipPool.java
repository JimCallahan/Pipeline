// $Id: ViewerLinkRelationshipPool.java,v 1.2 2004/05/19 19:04:17 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.util.*;
import javax.media.j3d.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   L I N K   R E L A T I O N S H I P   P O O L                              */
/*------------------------------------------------------------------------------------------*/

/**
 * An efficient reuseable collection of {@link ViewerLinkRelationship ViewerLinkRelationship}
 * objects located under a common {@link BranchGroup BranchGroup}. <P> 
 */
public
class ViewerLinkRelationshipPool
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer node pool. 
   */ 
  public
  ViewerLinkRelationshipPool()
  {
    BranchGroup bg = new BranchGroup();
    bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    pRoot = bg;      

    pActive = 
      new EnumMap<LinkRelationship,Stack<ViewerLinkRelationship>>(LinkRelationship.class);

    pPrevious = 
      new EnumMap<LinkRelationship,Stack<ViewerLinkRelationship>>(LinkRelationship.class);

    pReserve = new Stack<ViewerLinkRelationship>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root group containing all node groups. 
   */ 
  public BranchGroup
  getBranchGroup()
  {
    return pRoot;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   S T A T E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Prepare to update the link relationship icons.
   */ 
  public synchronized void 
  updatePrep() 
  {
    /* hide the active nodes */ 
    for(Stack<ViewerLinkRelationship> active : pActive.values()) {
      if(active != null) {
	for(ViewerLinkRelationship vlink : active) 
	  vlink.setVisible(false);
      }
    }

    /* swap tables */ 
    pPrevious = pActive;
    pActive = 
      new EnumMap<LinkRelationship,Stack<ViewerLinkRelationship>>(LinkRelationship.class);
  }

  /**
   * Add a new {@link ViewerLinkRelationship ViewerLinkRelationship} instance 
   * or reuse an existing instance to represent the given parent link and target viewer node.
   * 
   * @param link 
   *   The parent link.
   * 
   * @param vnode 
   *   The target viewer node.
   */ 
  public synchronized ViewerLinkRelationship
  addIcon
  (
   LinkCommon link,
   ViewerNode vnode
  ) 
  {
    if(link == null) 
      throw new IllegalArgumentException("The node link cannot be (null)!");

    if(vnode == null) 
      throw new IllegalArgumentException("The viewer node cannot be (null)!");

    ViewerLinkRelationship vlink = null;
    {
      Stack<ViewerLinkRelationship> previous = pPrevious.get(link.getRelationship());
      if((previous != null) && !previous.empty())
	vlink = previous.pop();

      if((vlink == null) && !pReserve.empty())
	vlink = pReserve.pop();

      if(vlink == null) {
	vlink = new ViewerLinkRelationship();
 	pRoot.addChild(vlink.getBranchGroup());
      }

      Stack<ViewerLinkRelationship> active = pActive.get(link.getRelationship());
      if(active == null) {
	active = new Stack<ViewerLinkRelationship>();
	pActive.put(link.getRelationship(), active);
      }
      active.push(vlink);
    }

    vlink.setCurrentState(link, vnode);
    
    return vlink;
  }

  /**
   * Update the appearance of the active {@link ViewerLinkRelationship ViewerLinkRelationship}
   * instances to reflect changes in their associated parent links and target nodes.
   */ 
  public synchronized void 
  update()
  {
    /* update and show the active icons */ 
    for(Stack<ViewerLinkRelationship> active : pActive.values()) {
      if(active != null) {
	for(ViewerLinkRelationship vlink : active) 
	  vlink.update(); 
      }
    }
    for(Stack<ViewerLinkRelationship> active : pActive.values()) {
      if(active != null) {
	for(ViewerLinkRelationship vlink : active) 
	  vlink.setVisible(true);
      }
    }

    /* move any remaining previously active icons to the reserve */ 
    for(Stack<ViewerLinkRelationship> previous : pPrevious.values()) {
      if(previous != null) {
	while(!previous.empty())
	  pReserve.push(previous.pop());
      }
    }

    pPrevious = null;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root branch group. 
   */ 
  private BranchGroup  pRoot; 



  /**
   * The table of currently active viewer nodes indexed by node path.
   */ 
  private EnumMap<LinkRelationship,Stack<ViewerLinkRelationship>> pActive;

  /**
   * The table of previously active viewer nodes indexed by node path.
   */ 
  private EnumMap<LinkRelationship,Stack<ViewerLinkRelationship>>  pPrevious;

  /**
   * The reserve of inactive viewer nodes ready for reuse indexed by icon title. <P> 
   * 
   * They idea behind storing them by title is to minimize title regeneration when 
   * recycling nodes.
   */ 
  private Stack<ViewerLinkRelationship>  pReserve;

}
