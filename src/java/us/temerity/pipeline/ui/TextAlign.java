// $Id: TextAlign.java,v 1.1 2004/08/30 14:30:57 jim Exp $

package us.temerity.pipeline.ui;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   A L I G N                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The alignment of text relative to a position. 
 */
public
enum TextAlign
{  
  /**
   * Left justified.
   */
  Left, 

  /**
   * Centered.
   */
  Center, 

  /**
   * Right justified.
   */
  Right; 


  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<TextAlign>
  all() 
  {
    TextAlign values[] = values();
    ArrayList<TextAlign> all = new ArrayList<TextAlign>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }
}
