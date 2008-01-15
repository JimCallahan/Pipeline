// $Id: BuildType.java,v 1.1 2008/01/15 14:50:16 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   T Y P E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The machine builditecture type.
 */
public
enum BuildType
{  
  /** 
   * 
   */
  Opt, 

  /** 
   *
   */
  Debug, 
  
  /**
   * 
   */
  Profile;


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<BuildType>
  all() 
  {
    BuildType values[] = values();
    ArrayList<BuildType> all = new ArrayList<BuildType>(values.length);
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
    for(BuildType os : BuildType.all()) 
      titles.add(os.toTitle());
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
