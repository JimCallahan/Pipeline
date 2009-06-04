// $Id: Flags.java,v 1.1 2009/06/04 09:45:12 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F L A G S                                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A unique combination of boolean flags. 
 */ 
public class 
Flags
  implements Comparable<Flags> 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new set of flags.
   */
  public
  Flags
  (
   boolean[] choices
  ) 
  {
    pChoices = choices;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof Flags)) {
      Flags vid = (Flags) obj;
      return Arrays.equals(pChoices, vid.pChoices);
    }
    return false;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    StringBuilder buf = new StringBuilder();
    buf.append("["); 
    for(boolean choice : pChoices) 
      buf.append(choice ? "1" : "0");
    buf.append("]"); 
    return buf.toString();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this <CODE>Flags</CODE> with the given <CODE>Flags</CODE> for order.
   * 
   * @param flags
   *   The <CODE>Flags</CODE> to be compared.
   */
  public int
  compareTo
  (
   Flags flags
  )
  {
    if(pChoices.length != flags.pChoices.length)
      throw new IllegalArgumentException
        ("Cannot compare Flags which have different numbers of members!"); 

    int wk;
    for(wk=0; wk<pChoices.length; wk++)
      if(pChoices[wk] != flags.pChoices[wk])
 	return pChoices[wk] ? 1 : -1;
    
    return 0;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The set of flags. 
   */ 
  private boolean[] pChoices; 

}

