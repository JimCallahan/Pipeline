// $Id: CheckOutMethod.java,v 1.1 2004/11/17 13:34:08 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K - O U T   M E T H O D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The method for creating working area files/links from the checked-in files.
 */
public
enum CheckOutMethod
{  
  /**
   * Copy the checked-in files associated with all nodes to the working area.
   */
  Modifiable, 
  
  /**
   * Copy the checked-in files associated with the root node of the check-out to the 
   * working area, but create symlinks from the working area to the checked-in files for 
   * all nodes upstream of the root node.
   */ 
  FrozenUpstream,

  /**
   * Create symlinks from the working area to the checked-in files for all nodes.
   */
  AllFrozen;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<CheckOutMethod>
  all() 
  {
    CheckOutMethod values[] = values();
    ArrayList<CheckOutMethod> all = new ArrayList<CheckOutMethod>(values.length);
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
    for(CheckOutMethod method : CheckOutMethod.all()) 
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
    "Modifiable",
    "Frozen Upstream", 
    "All Frozen" 
  };
}
