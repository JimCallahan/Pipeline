// $Id: JProportionGraph.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

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
   * 
   * 
   * @param values
   *   The set of proportion values (should sum to 1.0).
   * 
   * @param colors
   *   The colors to use when drawing bars for each proportion value. 
   * 
   * @param highlight
   *   Whether to highlight the first sample.
   */ 
  public void 
  setValues
  (
   float[] values, 
   Color[] colors 
  ) 
  {
    assert(values.length == colors.length);
    pValues = values;
    pColors = colors; 
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
	  if(pValues[wk] > 0.0f) {
	    int size = Math.max(1, (int) (pValues[wk] * ((float) width)));
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

      /* increase/descrease the size of the largest bar until the total size matches the 
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
  private float[]  pValues;

  /**
   * The bar colors. 
   */ 
  private Color[]  pColors; 

}
