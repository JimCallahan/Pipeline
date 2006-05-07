// $Id: OsType.java,v 1.2 2006/05/07 21:30:07 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   O S   T Y P E                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The operating system type.
 */
public
enum OsType
{  
  /** 
   * Linux and other UNIX flavors.
   */
  Unix, 

  /**
   * Microsoft Windows.
   */
  Windows, 

  /** 
   * Apple's Macintosh OS.
   */ 
  MacOS; 


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<OsType>
  all() 
  {
    OsType values[] = values();
    ArrayList<OsType> all = new ArrayList<OsType>(values.length);
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
    for(OsType os : OsType.all()) 
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
    return sTitles[ordinal()];
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "Linux", 
    "Windows XP", 
    "Mac OS X"
  };

}
