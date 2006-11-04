// $Id: ArchType.java,v 1.1 2006/11/04 02:14:25 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H   T Y P E                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The machine architecture type.
 */
public
enum ArchType
{  
  /** 
   * Intel/AMD i386 compatible processor.
   */
  x86, 

  /**
   * PowerPC 7450 processor.
   */
  G4, 

  /** 
   * PowerPC 970 processor.
   */ 
  G5; 


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<ArchType>
  all() 
  {
    ArchType values[] = values();
    ArrayList<ArchType> all = new ArrayList<ArchType>(values.length);
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
    for(ArchType os : ArchType.all()) 
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
