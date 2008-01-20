// $Id: JTuple3dField.java,v 1.2 2008/01/20 01:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   3 D   F I E L D                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a Tuple3d value.
 */
public 
class JTuple3dField
  extends JDoubleTupleField
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JTuple3dField() 
  {
    super(3);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the Tuple3d value.
   * 
   * @return 
   *   The Tuple3d value or <CODE>null</CODE> if empty.
   */ 
  public Tuple3d
  getValue()
  {
    Tuple3d tuple = new Tuple3d();

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
   * Set the Tuple3d value.
   * 
   * @param value
   *   The Tuple3d value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Tuple3d value
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

  private static final long serialVersionUID = -1460220970609364246L;
  
}
