// $Id: ViewerLinkRelationship.java,v 1.1 2005/01/03 06:56:25 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   L I N K   R E L A T I O N S H I P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about a {@link LinkRelationship LinkRelationship} icon displayed by a 
 * {@link JNodeViewerPanel JNodeViewerPanel}.
 */
public 
class ViewerLinkRelationship
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer link relationship. 
   */ 
  public 
  ViewerLinkRelationship
  (
   LinkCommon link,
   ViewerNode vnode, 
   Point2d pos
  ) 
  {
    if(link == null) 
      throw new IllegalArgumentException("The node link cannot be (null)!");
    pLink = link;

    if(vnode == null) 
      throw new IllegalArgumentException("The node vnode cannot be (null)!");
    pViewerNode = vnode;

    Vector2d size = new Vector2d(0.25, 0.148);
    pBBox = new BBox2d(Point2d.sub(pos, size), Point2d.add(pos, size)); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the parent link.
   */ 
  public LinkCommon
  getLink() 
  {
    return pLink;
  }

  /**
   * Get the target viewer node.
   */ 
  public ViewerNode
  getViewerNode() 
  {
    return pViewerNode;
  }

  /**
   * Get the status of the node which is the target of the link.
   */ 
  public NodeStatus
  getNodeStatus() 
  {
    return pViewerNode.getNodeStatus();
  }
  
  /**
   * Get position of the center of the icon.
   */ 
  public Point2d
  getPosition()
  {
    return pBBox.getCenter();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given position is inside the node icon.
   */ 
  public boolean
  isInside
  (
   Point2d pos
  ) 
  {              
    return pBBox.isInside(pos);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent link.
   */ 
  private LinkCommon  pLink;

  /**
   * The target viewer node.
   */ 
  private ViewerNode  pViewerNode;

  /**
   * Whether this geometry is currently visible.
   */
  private boolean  pIsVisible;     

  /**
   * The bounding box of the node icon. 
   */ 
  private BBox2d  pBBox;

}
