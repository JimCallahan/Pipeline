// $Id: RestoreState.java,v 1.1 2005/03/21 07:04:35 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S T O R E   S T A T E                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The state of a {@link RestoreRequest RestoreRequest}.
 */
public
enum RestoreState
{  
  /**
   * The request has not yet been acted upon.
   */
  Pending,
  
  /**
   * The checked-in version has been successfully restored.
   */ 
  Restored, 

  /**
   * The request has been denied by system administrators.
   */
  Denied; 



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<RestoreState>
  all() 
  {
    RestoreState values[] = values();
    ArrayList<RestoreState> all = new ArrayList<RestoreState>(values.length);
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
    for(RestoreState method : RestoreState.all()) 
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
    return toString();
  }

}
