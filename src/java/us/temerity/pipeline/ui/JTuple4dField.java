// $Id: JTuple4dField.java,v 1.2 2008/01/20 01:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   4 D   F I E L D                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a Tuple4d value.
 */
public 
class JTuple4dField
  extends JDoubleTupleField
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JTuple4dField() 
  {
    super(4);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the Tuple4d value.
   * 
   * @return 
   *   The Tuple4d value or <CODE>null</CODE> if empty.
   */ 
  public Tuple4d
  getValue()
  {
    Tuple4d tuple = new Tuple4d();

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
   * Set the Tuple4d value.
   * 
   * @param value
   *   The Tuple4d value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Tuple4d value
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

  private static final long serialVersionUID = -2453191464454358030L;
  
}
