// $Id: ArchType.java,v 1.4 2008/07/22 21:37:30 jesse Exp $

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
   * A 32-bit Intel/AMD i386 compatible processor.
   */
  x86, 

  /** 
   * A 64-bit Intel/AMD i386 compatible processor.
   */
  x86_64, 

  /**
   * Any of the CPU types supported by Universal Binary.
   */
  UnivBin,

  /**
   * A 32-bit Motorola PowerPC G4 compatible processor.
   */
  PPC_G4,
  
  /**
   * A 64-bit Motorola PowerPC G4 compatible processor.
   */
  PPC_G5;


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
    return sTitles[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "x86 (32-bit)", 
    "x86 (64-bit)", 
    "Universal Binary (32-bit)",
    "PowerPC G4 (32-bit)",
    "PowerPC G5 (32-bit)"
  };
}
