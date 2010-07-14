// $Id: JProportionGraph.java,v 1.3 2009/05/14 23:30:43 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O P O R T I O N    G R A P H                                                       */
/*------------------------------------------------------------------------------------------*/

/**
 * Draws a set of colored bars which represent a proportional breakdown.
 */ 
public 
class JProportionGraph
  extends JComponent
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new component.
   */
  public 
  JProportionGraph()
  {
    super();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the values to be displayed. <P> 
   * 
   * @param values
   *   The set of proportion values (should sum to 1.0).
   * 
   * @param colors
   *   The colors to use when drawing bars for each proportion value. 
   */ 
  public void 
  setValues
  (
   double[] values, 
   Color[] colors 
  ) 
  {
    pValues = values;
    pColors = colors;
    pLabel = null;
  } 

  /**
   * Set the values to be displayed. <P> 
   * 
   * @param values
   *   The set of proportion values (should sum to 1.0).
   * 
   * @param colors
   *   The colors to use when drawing bars for each proportion value.
   *   
   * @param label
   *   A string to draw on top of the graph.
   */ 
  public void 
  setValues
  (
   double[] values, 
   Color[] colors,
   String label
  ) 
  {
    pValues = values;
    pColors = colors; 
    pLabel = label;
  } 


  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs any custom painting.
   */
  @Override
  protected void 
  paintComponent
  (
   Graphics graphics
  )
  {
    super.paintComponent(graphics);
    
    if((pValues == null) || (pColors == null)) 
      return;

    Graphics2D gfx = (Graphics2D) graphics.create(); 
    int width = getWidth();
    int height = getHeight();
    
    if((width <= 0) || (height <= 0)) 
      return;

    /* compute integer pixel sizes for the bars */ 
    int ws[] = new int[pValues.length];
    {
      /* compute provisional sizes, rounding up small bars to at least one pixel */ 
      int total = 0;
      {
	int wk;
	for(wk=0; wk<pValues.length; wk++) {
	  if(pValues[wk] > 0.0) {
	    int size = Math.max(1, (int) (pValues[wk] * width));
	    ws[wk] = size; 
	    total += size;
	  }
	  else {
	    ws[wk] = 0;
	  }
	}
      }
      
      if(total == 0) 
	return;

      /* increase/decrease the size of the largest bar until the total size matches the 
	   width of the component */ 
      while(total != width) {
	int wk;
	int largest = 0;
	int idx = -1;
	for(wk=0; wk<ws.length; wk++) {
	  if(ws[wk] > largest) {
	    largest = ws[wk];
	    idx = wk;
	  }
	}	
	assert(idx != -1); 

	int delta = (total > width) ? -1 : 1;
	ws[idx] += delta;
	total   += delta;
      }
    }
    
    /* draw the bars */ 
    {
      int left = 0;
      int wk;
      for(wk=0; wk<ws.length; wk++) {
	gfx.setColor(pColors[wk]);
	gfx.fill(new Rectangle(left, 1, ws[wk], height));
	left += ws[wk];
      }
    }

    if (pLabel != null) {
      gfx.setColor(Color.white);
      Font font = gfx.getFont();
      FontRenderContext frc = gfx.getFontRenderContext();
      Rectangle2D bounds = font.getStringBounds(pLabel, frc);
//      int rX = (width - (int) bounds.getWidth())/2;
      int rY = (height - (int) bounds.getHeight()/2);
      gfx.drawString(pLabel, 8, rY);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4894204461941211833L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The values to graph.
   */ 
  private double[]  pValues;

  /**
   * The bar colors. 
   */ 
  private Color[]  pColors; 
  
  /**
   * The string to draw on top of the graph.
   */
  private String   pLabel;

}
