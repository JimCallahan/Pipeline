// $Id: ViewerIcon.java,v 1.1 2005/01/03 06:56:25 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   I C O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class of OpenGL rendered icons displayed in viewer panels. 
 */
public abstract
class ViewerIcon
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer node. 
   */ 
  protected
  ViewerIcon() 
  {
    pMode = SelectionMode.Normal;
    pPos = new Point2d();
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the upstream/downstream nodes are collapsed.
   */ 
  public boolean 
  isCollapsed() 
  {
    return pIsCollapsed;
  }

  /**
   * Set whether the upstream/downstream nodes are collapsed.
   */ 
  public void 
  setCollapsed
  (
   boolean tf
  ) 
  {
    pIsCollapsed = tf;
  }

  /**
   * Toggle whether the upstream/downstream nodes are collapsed.
   */ 
  public void 
  toggleCollapsed() 
  {
    pIsCollapsed = !pIsCollapsed;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current selection mode.
   */ 
  public SelectionMode
  getSelectionMode() 
  {
    return pMode;
  }

  /**
   * Set the current selection mode.
   */ 
  public void 
  setSelectionMode
  (
   SelectionMode mode
  ) 
  {
    pMode = mode;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given position is inside the node icon.
   */ 
  public abstract boolean
  isInside
  (
   Point2d pos
  );

  /**
   * Whether any portion of the node icon is inside the given bounding box.
   */ 
  public abstract boolean
  isInsideOf
  (
   BBox2d bbox
  );


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the 2D position of the node (center of icon).
   */ 
  public Point2d
  getPosition()
  {
    return new Point2d(pPos);
  }

  /**
   * Set the 2D position of the node (center of icon). 
   */
  public void 
  setPosition
  (
   Point2d pos    
  ) 
  {
    pPos.set(pos);
  }

  /**
   * Move the node position by the given amount.
   */ 
  public void 
  movePosition
  (
   Vector2d delta
  ) 
  {
    pPos.add(delta);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Rebuild any OpenGL display list needed to render the node.
   *
   * @param gl
   *   The OpenGL interface.
   */ 
  public abstract void 
  rebuild
  (
   GL gl
  );

  /**
   * Render the OpenGL geometry for the node.
   *
   * @param gl
   *   The OpenGL interface.
   */ 
  public abstract void 
  render
  (
   GL gl
  );
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The UI selection mode.
   */
  protected SelectionMode  pMode;  

  /**
   * Whether to display the collapsed icon. 
   */
  protected boolean  pIsCollapsed;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The 2D position of the node (center of icon).
   */ 
  protected Point2d  pPos;         

}
