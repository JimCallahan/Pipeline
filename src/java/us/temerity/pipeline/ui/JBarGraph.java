// $Id: JBarGraph.java,v 1.1 2004/07/28 19:22:50 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   B A R   G R A P H                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Draws a simple bar graph of values.
 */ 
public 
class JBarGraph
  extends JComponent
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new tree component.
   */
  public 
  JBarGraph()
  {
    super();

    pHighlight = Color.yellow;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the values to be displayed. <P> 
   * 
   * The graph will draw a single pixel vertical line for each value starting at the bottom
   * edge of the component.  The height of the lines is rescaled so that a value of (1.0) 
   * will be the full height of the component. <P> 
   * 
   * If there are more values than the width of the component (in pixels), the extra values
   * will not be drawn. <P> 
   * 
   * The first value (leftmost) will be draw in the highlight color (see 
   * {@link #setHighlight setHighlight}).  The remaining lines will be drawn in the foreground
   * color (see {@link #setForeground setForeground}).
   * 
   * @param
   *   The normalized [0,1] values.
   */ 
  public void 
  setValues
  (
   float[] values
  ) 
  {
    pValues = values;
  } 


  /**
   * Get the highlight color used to draw the first value.
   */ 
  public Color
  getHighlight() 
  {
    return pHighlight;
  }

  /**
   * Set the highlight color used to draw the first value.
   */ 
  public void 
  setHighlight
  (
   Color color
  ) 
  {
    pHighlight = color;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs any custom painting.
   */
  protected void 
  paintComponent
  (
   Graphics graphics
  )
  {
    super.paintComponent(graphics);
    
    if(pValues == null)
      return;

    Graphics2D gfx = (Graphics2D) graphics.create(); 
    int width = getWidth();
    int height = getHeight()-1;
    
    /* draw the first value */ 
    {
      int value = 0;
      if(pValues.length > 0)
	value = Math.min(height, Math.max(0, (int) (pValues[0] * ((float) height))));
      
      gfx.setColor(pHighlight);
      gfx.drawLine(0, height, 0, height-value);
      gfx.drawLine(1, height, 1, height-value);
    }

    /* draw the rest of the values */ 
    {
      gfx.setColor(getForeground());

      int npt = Math.min(width, pValues.length) - 1;
      int wk;
      for(wk=2; wk<npt; wk++) {
	int value = Math.min(height, Math.max(0, (int) (pValues[wk-1] * ((float) height))));
	gfx.drawLine(wk, height, wk, height-value);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5794848458057372110L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The highlight color used to draw the first value.
   */ 
  private Color  pHighlight; 

  /**
   * The values to graph.
   */ 
  private float[]  pValues;
}
