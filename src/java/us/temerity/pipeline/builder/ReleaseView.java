// $Id: ReleaseView.java,v 1.1 2009/04/06 00:53:11 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R E L E A S E   V I E W                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The options for what the builder should do with a working area when it finishes?
 */
public 
enum ReleaseView
{
  /**
   * Release the working area no matter what.
   */
  Always,
  
  /**
   * Do not ever release the working area. 
   */
  Never,
  
  /**
   * Release the working area only if there is an error.
   */
  OnError,
  
  /**
   * Release the working area only if execution completes successfully.
   */
  OnSuccess;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<ReleaseView>
  all() 
  {
    ReleaseView values[] = values();
    ArrayList<ReleaseView> all = new ArrayList<ReleaseView>(values.length);
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
    for(ReleaseView method : values()) 
      titles.add(method.toTitle());
    return titles;
  }


  /*----------------------------------------------------------------------------------------*/

  public static ReleaseView
  valueFromKey
  (
    int key
  )
  {
    return ReleaseView.values()[key];
  }
   
  public static ReleaseView
  valueFromString
  (
    String string  
  )
  {
    ReleaseView toReturn = null;
    for (ReleaseView each : values())
      if (each.toString().equals(string))
        toReturn = each;
    return toReturn;
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
