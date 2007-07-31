// $Id: JTuple2iField.java,v 1.1 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   2 I   F I E L D                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a Tuple2i value.
 */
public 
class JTuple2iField
  extends JIntegerTupleField
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JTuple2iField() 
  {
    super(2);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the Tuple2i value.
   * 
   * @return 
   *   The Tuple2i value or <CODE>null</CODE> if empty.
   */ 
  public Tuple2i
  getValue()
  {
    Tuple2i tuple = new Tuple2i();

    int i;
    for(i=0; i<pSize; i++) {
      Integer value = pFields[i].getValue(); 
      if(value == null) 
        value = 0;
      
      tuple.setComp(i, value);
    }
    
    return tuple;
  }

  /**
   * Set the Tuple2i value.
   * 
   * @param value
   *   The Tuple2i value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Tuple2i tuple
  ) 
  {
    if(tuple == null) 
      return;
    
    int i;
    for(i=0; i<pSize; i++) 
      pFields[i].setValue(tuple.getComp(i));
    
    fireActionPerformed();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7758882333847964794L;
  
}
