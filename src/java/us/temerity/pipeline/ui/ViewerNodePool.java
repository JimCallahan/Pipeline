// $Id: ViewerNodePool.java,v 1.1 2004/05/07 18:11:25 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.util.*;
import javax.media.j3d.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   N O D E   P O O L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An efficient reuseable collection of {@link ViewerNode ViewerNode} objects located 
 * under a common {@link BranchGroup BranchGroup}. <P> 
 * 
 * {@link JNodeViewerPanel JNodeViewerPanel} instances use this class to maintain a set 
 * of <CODE>ViewerNode</CODE> instances which correspond to {@link NodeStatus NodeStatus} 
 * objects retrieved from <B>plmaster</B>(1).  When the <CODE>NodeStatus</CODE> of a node 
 * is updated, any existing associated <CODE>ViewerNode</CODE> simply have its appearance
 * updated to reflect the changes. <P>
 * 
 * When existing <CODE>ViewerNode</CODE> instances are no longer needed they are simply
 * hidden rather than being deallocated and removed from the Java3D scene.  When new 
 * <CODE>ViewerNode</CODE> instances are required to represent updated or new 
 * <CODE>NodeStatus</CODE> instances, these hidden <CODE>ViewerNode</CODE> instances are 
 * resused instead of creating new instances.  Only when there are no remaining hidden 
 * <CODE>ViewerNode</CODE> are new instances allocated. <P> 
 */
public
class ViewerNodePool
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer node pool. 
   */ 
  public
  ViewerNodePool()
  {
    BranchGroup bg = new BranchGroup();
    bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    pRoot = bg;      

    pActive   = new HashMap<NodePath,ViewerNode>();
    pPrevious = new HashMap<NodePath,ViewerNode>();
    pReserve  = new Stack<ViewerNode>();
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
   * Prepare to update the node status.
   */ 
  public synchronized void 
  updatePrep() 
  {
    /* swap node tables */ 
    pPrevious = pActive;
    pActive   = new HashMap<NodePath,ViewerNode>(pPrevious.size());
  }

  /**
   * Get an existing or create a new {@link ViewerNode ViewerNode} instance to represent 
   * the given {@link NodeStatus NodeStatus} reachable by the given {@link NodePath NodePath}.
   * 
   * @param status
   *   The current node status.
   * 
   * @param path
   *   The path from the focus node to the current node.
   */ 
  public synchronized ViewerNode
  getViewerNode
  (
   NodeStatus status, 
   NodePath path  
  ) 
  {
    if(status == null) 
      throw new IllegalArgumentException("The node status cannot be (null)!");

    if(path == null) 
      throw new IllegalArgumentException("The node path cannot be (null)!");

    assert(status.getName().equals(path.getCurrentName()));
    assert(!pActive.containsKey(path));

    System.out.print("Getting: " + status + " [" + path + "]\n");

    /* lookup or create... */ 
    ViewerNode vnode = pPrevious.remove(path);
    if(vnode == null) {
      if(!pReserve.empty()) {
	vnode = pReserve.pop();
	
	System.out.print("  Recycled: " + vnode.getNodeStatus() + 
			 " [" + vnode.getNodePath() + "]\n");
      }
      else {
	vnode = new ViewerNode();
	pRoot.addChild(vnode.getBranchGroup());

	System.out.print("  Created!\n");
      }
    }
    else {
      System.out.print("  Reused: " + vnode.getNodeStatus() + 
		       " [" + vnode.getNodePath() + "]\n");
    }
    pActive.put(path, vnode);

    /* update state */ 
    vnode.setCurrentState(status, path);
    
    return vnode;
  }

  /**
   * Update the appearance of the active {@link ViewerNode ViewerNode} instances to 
   * reflect changes in their associated {@link NodeStatus NodeStatus} and the viewer 
   * layout scheme.
   */ 
  public synchronized void 
  update()
  {
    /* update and show the active nodes */ 
    for(ViewerNode vnode : pActive.values()) {
      vnode.update(); 
      vnode.setVisible(true);
    }

    /* hide any previously active nodes which are no longer in use 
        and move them to the reserve */ 
    for(ViewerNode vnode : pPrevious.values()) {
      vnode.setVisible(false);
      vnode.reset();
      pReserve.push(vnode);
    }

    pPrevious = null;

    System.out.print("NodePool: \n" + 
		     "   Active = " + pActive.size() + "\n" + 
		     "  Reserve = " + pReserve.size() + "\n");
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
  private HashMap<NodePath,ViewerNode>  pActive;

  /**
   * The table of previously active viewer nodes indexed by node path.
   */ 
  private HashMap<NodePath,ViewerNode>  pPrevious;

  /**
   * The reserve of inactive viewer nodes ready for reuse.
   */ 
  private Stack<ViewerNode> pReserve;

}
