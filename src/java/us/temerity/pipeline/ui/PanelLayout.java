// $Id: PanelLayout.java,v 1.1 2004/05/11 19:17:03 jim Exp $

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
   * @param size
   *   The size of the parent <CODE>JFrame</CODE>.
   */
  public 
  PanelLayout
  (
   JManagerPanel root, 
   Dimension size
  )
  {
    pRoot = root;
    pSize = size;
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
   * Get the dimensions of the parent <CODE>JFrame</CODE>.
   */ 
  public Dimension
  getSize() 
  {
    return pSize;
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

    encoder.encode("FrameWidth",  pSize.width);
    encoder.encode("FrameHeight", pSize.height);
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


    Integer width = (Integer) decoder.decode("FrameWidth");
    if(width == null) 
      throw new GlueException("The \"FrameWidth\" was missing or (null)!");

    Integer height = (Integer) decoder.decode("FrameHeight");
    if(height == null) 
      throw new GlueException("The \"FrameHeight\" was missing or (null)!");

    pSize = new Dimension(width, height);
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root manager panel.
   */ 
  private JManagerPanel  pRoot; 

  /**
   * The dimensions of the parent <CODE>JFrame</CODE>.
   */ 
  private Dimension  pSize;

}
