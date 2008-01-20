// $Id: JTuple3iField.java,v 1.2 2008/01/20 01:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   3 I   F I E L D                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a Tuple3i value.
 */
public 
class JTuple3iField
  extends JIntegerTupleField
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JTuple3iField() 
  {
    super(3);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the Tuple3i value.
   * 
   * @return 
   *   The Tuple3i value or <CODE>null</CODE> if empty.
   */ 
  public Tuple3i
  getValue()
  {
    Tuple3i tuple = new Tuple3i();

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
   * Set the Tuple3i value.
   * 
   * @param value
   *   The Tuple3i value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Tuple3i value
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

  private static final long serialVersionUID = 2494105237163466491L;
  
}
