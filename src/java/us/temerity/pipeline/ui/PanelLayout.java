// $Id: PanelLayout.java,v 1.2 2004/08/23 06:43:37 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.awt.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   P A N E L   L A Y O U T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to save and restore panel layouts.
 */ 
public  
class PanelLayout
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  PanelLayout()
  {}

  /**
   * Construct a new layout.
   * 
   * @param root
   *   The root manager panel.
   * 
   * @param bounds
   *   The bounds of the parent window.
   */
  public 
  PanelLayout
  (
   JManagerPanel root, 
   Rectangle bounds
  )
  {
    pRoot   = root;
    pBounds = bounds; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the root manager panel.
   */ 
  public JManagerPanel
  getRoot() 
  {
    return pRoot;
  }
  
  /**
   * Get the bounds of the parent window.
   */ 
  public Rectangle
  getBounds() 
  {
    return pBounds;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    encoder.encode("RootPanel", pRoot);

    encoder.encode("FramePosX",   pBounds.x);
    encoder.encode("FramePosY",   pBounds.y);
    encoder.encode("FrameWidth",  pBounds.width);
    encoder.encode("FrameHeight", pBounds.height);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    JManagerPanel root = (JManagerPanel) decoder.decode("RootPanel");
    if(root == null) 
      throw new GlueException("The \"RootPanel\" was missing or (null)!");
    pRoot = root;


    Integer posX = (Integer) decoder.decode("FramePosX");
    if(posX == null) 
      throw new GlueException("The \"FramePosX\" was missing or (null)!");

    Integer posY = (Integer) decoder.decode("FramePosY");
    if(posY == null) 
      throw new GlueException("The \"FramePosY\" was missing or (null)!");

    Integer width = (Integer) decoder.decode("FrameWidth");
    if(width == null) 
      throw new GlueException("The \"FrameWidth\" was missing or (null)!");

    Integer height = (Integer) decoder.decode("FrameHeight");
    if(height == null) 
      throw new GlueException("The \"FrameHeight\" was missing or (null)!");

    pBounds = new Rectangle(posX, posY, width, height);
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root manager panel.
   */ 
  private JManagerPanel  pRoot; 

  /**
   * The bounds of the parent window. 
   */ 
  private Rectangle  pBounds; 

}
