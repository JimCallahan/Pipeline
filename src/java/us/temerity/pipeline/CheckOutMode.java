// $Id: CheckOutMode.java,v 1.3 2005/12/30 23:28:44 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K - O U T   M O D E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The criteria used to determine whether nodes upstream of the root node of the check-out
 * should also be checked-out.
 */
public
enum CheckOutMode
{  
  /**
   * Check-out upstream nodes regardless of the version of any existing working version 
   * of the node overwriting all working versions.
   */
  OverwriteAll, 
  
  /**
   * Skip the check-out if there exists a working version which is based on the version 
   * being checked-out or on a version which is newer than the version being checked-out. <P> 
   * 
   * The current {@link OverallNodeState OverallNodeState} and 
   * {@link OverallQueueState OverallQueueState} are not considered. 
   */
  KeepModified;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<CheckOutMode>
  all() 
  {
    CheckOutMode values[] = values();
    ArrayList<CheckOutMode> all = new ArrayList<CheckOutMode>(values.length);
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
    for(CheckOutMode method : CheckOutMode.all()) 
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
    "Overwrite All", 
    "Keep Newer",
    "Keep Modified"
  };
}
