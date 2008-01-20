// $Id: JTuple2dField.java,v 1.2 2008/01/20 01:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   2 D   F I E L D                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a Tuple2d value.
 */
public 
class JTuple2dField
  extends JDoubleTupleField
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JTuple2dField() 
  {
    super(2);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the Tuple2d value.
   * 
   * @return 
   *   The Tuple2d value or <CODE>null</CODE> if empty.
   */ 
  public Tuple2d
  getValue()
  {
    Tuple2d tuple = new Tuple2d();

    int i;
    for(i=0; i<pSize; i++) {
      Double value = pFields[i].getValue(); 
      if(value == null) 
        value = 0.0;
      
      tuple.setComp(i, value);
    }
    
    return tuple;
  }

  /**
   * Set the Tuple2d value.
   * 
   * @param value
   *   The Tuple2d value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Tuple2d value
  ) 
  {
    if(value == null) 
      return;
    
    int i;
    for(i=0; i<pSize; i++) 
      pFields[i].setValue(value.getComp(i));
    
    fireActionPerformed();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6932219796218633627L;
  
}
