// $Id: ViewerGraphic.java,v 1.1 2006/12/07 05:18:25 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   G R A P H I C                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class of OpenGL rendered geometry displayed in viewer panels. 
 */
public abstract
class ViewerGraphic
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer graphic. 
   */ 
  protected
  ViewerGraphic() 
  {
    pPos = new Point2d();
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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
   * The 2D position of the node (center of icon).
   */ 
  protected Point2d  pPos;         

}
