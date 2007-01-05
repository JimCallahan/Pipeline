// $Id: ViewerIcon.java,v 1.3 2007/01/05 23:46:10 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   I C O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class of OpenGL rendered icons displayed in viewer panels. 
 */
public abstract
class ViewerIcon
  extends ViewerGraphic
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
    super();
    pMode = SelectionMode.Normal;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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
  public boolean
  isInsideOf
  (
   BBox2d bbox
  )
  {
    Point2d minC = bbox.getMin();
    Point2d maxC = bbox.getMax();

    if(pPos.x() < minC.x()) {
      if(pPos.y() < minC.y()) 
	return isInside(minC);
      else if(pPos.y() > maxC.y()) 
	return isInside(new Point2d(minC.x(), maxC.y()));
      else 
	return ((minC.x() - pPos.x()) < 0.45);
    }
    else if(pPos.x() > maxC.x()) {
      if(pPos.y() < minC.y()) 
	return isInside(new Point2d(maxC.x(), minC.y()));
      else if(pPos.y() > maxC.y()) 
	return isInside(maxC);
      else 
	return ((pPos.x() - maxC.x()) < 0.45);
    }
    else {
      if(pPos.y() < minC.y()) 
	return ((minC.y() - pPos.y()) < 0.45);
      else if(pPos.y() > maxC.y()) 
	return ((pPos.y() - maxC.y()) < 0.45);
      else 
	return true;
    }
  }


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

}
