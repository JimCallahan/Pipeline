// $Id: ViewerNodePool.java,v 1.5 2004/08/30 06:52:15 jim Exp $

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
 * is updated, any existing associated <CODE>ViewerNode</CODE> simply has its appearance
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
    pReserve  = new HashMap<String,Stack<ViewerNode>>();
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


  /**
   * Get the node paths of the active viewer nodes.
   */ 
  public synchronized Set<NodePath>
  getActiveNodePaths() 
  {
    return Collections.unmodifiableSet(pActive.keySet());
  }

  /**
   * Get the active viewer node reachable by the given {@link NodePath NodePath}.
   * 
   * @param path
   *   The path from the focus node to the current node.
   */ 
  public synchronized ViewerNode
  getActiveViewerNode
  (
   NodePath path  
  ) 
  {
    return pActive.get(path);
  }

  /** 
   * Get all of the active viewer nodes.
   */ 
  public synchronized Collection<ViewerNode>
  getActiveViewerNodes()
  {
    return Collections.unmodifiableCollection(pActive.values());
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
    /* hide the active nodes */ 
    for(ViewerNode vnode : pActive.values()) 
      vnode.setVisible(false);

    /* swap node tables */ 
    pPrevious = pActive;
    pActive   = new HashMap<NodePath,ViewerNode>(pPrevious.size());
  }

  /**
   * Lookup an existing or create a new {@link ViewerNode ViewerNode} instance to represent 
   * the given {@link NodeStatus NodeStatus} reachable by the given {@link NodePath NodePath}.
   * 
   * @param status
   *   The current node status.
   * 
   * @param path
   *   The path from the focus node to the current node.
   */ 
  public synchronized ViewerNode
  lookupOrCreateViewerNode
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

//     System.out.print("Getting: " + status + " [" + path + "]\n");

    /* look up the exact path from the previous viewer nodes */ 
    ViewerNode vnode = pPrevious.remove(path);
    if(vnode == null) {
      String text = status.toString();

      /* first see if there exists a match with the same label in the reserve */ 
      {
	Stack<ViewerNode> vstack = pReserve.get(text);
	if(vstack != null) {
	  assert(!vstack.empty());

	  vnode = vstack.pop();
	  if(vstack.empty()) 
	    pReserve.remove(text);
	  
// 	  System.out.print("  Recycled (match): " + vnode.getNodeStatus() + 
// 			   " [" + vnode.getNodePath() + "]\n");
	}
      }

      /* just grab the first available viewer node in the reserve */ 
      if((vnode == null) && (!pReserve.isEmpty())) {
	Iterator<String> iter = pReserve.keySet().iterator();
	if(iter.hasNext()) {
	  String key = iter.next();
	  Stack<ViewerNode> vstack = pReserve.get(key);
	  assert(vstack != null);
	  assert(!vstack.empty());

	  vnode = vstack.pop();
	  if(vstack.empty()) 
	    pReserve.remove(key);
	  
// 	  System.out.print("  Recycled (grab): " + vnode.getNodeStatus() + 
// 			   " [" + vnode.getNodePath() + "]\n");
	}
      }

      /* the reserve is empty, create a new viewer node */ 
      if(vnode == null) {
	vnode = new ViewerNode();
 	pRoot.addChild(vnode.getBranchGroup());
	
//  	System.out.print("  Created!\n");
      }
    }
//     else {
//       System.out.print("  Reused: " + vnode.getNodeStatus() + 
// 		       " [" + vnode.getNodePath() + "]\n");
//     }
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
    for(ViewerNode vnode : pActive.values()) 
      vnode.update(); 
    for(ViewerNode vnode : pActive.values()) 
      vnode.setVisible(true);

    /* hide any previously active nodes which are no longer in use 
        and move them to the reserve */ 
    for(ViewerNode vnode : pPrevious.values()) {
      vnode.reset();

      String text = vnode.getLabelText();
      assert(text != null);
      
      Stack<ViewerNode> vstack = pReserve.get(text);
      if(vstack == null) {
	vstack = new Stack<ViewerNode>();
	pReserve.put(text, vstack);
      }

      vstack.push(vnode);
    }

    pPrevious = null;

    // DEBUG 
//     {
//       System.out.print("NodePool: \n" + 
// 		       "   Active = " + pActive.size() + "\n" + 
// 		       "  Reserve = " + pReserve.size() + "\n");

//       for(String text : pReserve.keySet()) 
// 	System.out.print("    [" + text + "]: " + (pReserve.get(text).size()) + "\n");
//     }
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
   * The reserve of inactive viewer nodes ready for reuse indexed by icon title. <P> 
   * 
   * They idea behind storing them by title is to minimize title regeneration when 
   * recycling nodes.
   */ 
  private HashMap<String,Stack<ViewerNode>> pReserve;

}
