// $Id: RestoreReason.java,v 1.1 2005/03/14 16:08:21 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S T O R E   R E A S O N                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The reason a {@link RestoreRequest RestoreRequest} has been submitted.
 */
public
enum RestoreReason
{  
  /**
   * An offline version was encountered during a Check-Out operation.
   */
  CheckOut,
  
  /**
   * An offline version was encountered during an Evolve operation.
   */ 
  Evolve, 

  /**
   * The restore was requested by system administrators unrelated to node operations.
   */
  Admin; 



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<RestoreReason>
  all() 
  {
    RestoreReason values[] = values();
    ArrayList<RestoreReason> all = new ArrayList<RestoreReason>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(RestoreReason method : RestoreReason.all()) 
      titles.add(method.toTitle());
    return titles;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return sTitles[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "Check-Out", 
    "Evolve",
    "Admin"
  };
}
