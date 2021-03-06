// $Id: JPercentTableCellRenderer.java,v 1.1 2009/12/11 23:29:39 jesse Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.text.*;

import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E    T A B L E   C E L L   R E N D E R E R                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing data that is a double but is 
 * displayed as a percent.
 */ 
public 
class JPercentTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param align
   *   The horizontal alignment.
   *   
   * @param decimalPlaces
   *   The number of decimal places to display.
   */
  public 
  JPercentTableCellRenderer
  (
    int align,
    int decimalPlaces
  )
  {
    super(align);
    
    init(decimalPlaces);
  }

  /**
   * Construct a new renderer.
   * 
   * @param colorPrefix
   *   The Synth color prefix to give the component name.
   * 
   * @param align
   *   The horizontal alignment.
   *   
   * @param decimalPlaces
   *   The number of decimal places to display.
   */
  public 
  JPercentTableCellRenderer
  (
    String colorPrefix,
    int align,
    int decimalPlaces
  )
  {
    super(colorPrefix, align);
    
    init(decimalPlaces);
  }

  /**
   * Construct a new renderer.
   * 
   * @param colorPrefix
   *   The Synth color prefix to give the component name.
   * 
   * @param align
   *   The horizontal alignment.
   * 
   * @param dimUneditable
   *   Whether to render uneditable cells with a dimmed foreground color.
   *   
   * @param decimalPlaces
   *   The number of decimal places to display.
   */
  public 
  JPercentTableCellRenderer
  (
    String colorPrefix,
    int align,
    boolean dimUneditable,
    int decimalPlaces
  )
  {
    super(colorPrefix, align, dimUneditable);
    
    init(decimalPlaces);
  }

  
  private void
  init
  (
    int decimalPlaces  
  )
  {
    NumberFormat fmt = NumberFormat.getNumberInstance();
    fmt.setMinimumIntegerDigits(1);
    fmt.setMinimumFractionDigits(1);
    fmt.setMaximumFractionDigits(decimalPlaces);
    pNumberFormat = fmt;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
          
  /**
   * Returns the component used for drawing the cell.
   */ 
  @Override
  public Component      
  getTableCellRendererComponent
  (
   JTable table, 
   Object value, 
   boolean isSelected, 
   boolean hasFocus, 
   int row, 
   int col
  )
  {
    Double num = null;
    if(value != null) 
      num = (Double) value;

    if(num != null) {
      
      num *=100d;
      
      setText(pNumberFormat.format(num) + "%");
    }
    else 
      setText("-");

    setBasicForeground(table, isSelected, row, col);

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2252416513171335307L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private NumberFormat pNumberFormat;
  
}
