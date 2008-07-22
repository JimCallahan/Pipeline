// $Id: OsType.java,v 1.3 2008/07/22 21:37:30 jesse Exp $

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
  /*   A R C H   T Y P E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets a list of the architecture types that Pipeline considers valid for each 
   * operating system.
   * 
   * @param os
   *   The OS type.
   */
  public static List<ArchType>
  getValidArchTypes
  (
    OsType os  
  )
  {
    switch (os) {
    case Windows:
      return sWindowsArch;
    case Unix:
      return sUnixArch;
    case MacOS:
      return sMacArch;
    default:
      assert(false);
      return null;
    }
  }

  /**
   * Is the given ArchType valid for this OsType.
   * 
   * @param arch
   *   The architecture being validated
   */
  public boolean
  isValidArchType
  (
    ArchType arch  
  )
  {
    switch (this) {
    case Windows:
      return sWindowsArch.contains(arch);
    case Unix:
      return sUnixArch.contains(arch);
    case MacOS:
      return sMacArch.contains(arch);
    default:
      assert(false);
      return false;
    }
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

  private static final String sTitles[] = {
    "Linux", 
    "Windows XP", 
    "Mac OS X"
  };
  
  private static List<ArchType> sWindowsArch ;
  private static List<ArchType> sUnixArch ;
  private static List<ArchType> sMacArch ;
  
  static {
    {
      ArchType types [] = {
        ArchType.x86,
        ArchType.x86_64
      };
      sWindowsArch = Collections.unmodifiableList(Arrays.asList(types));
    }
    {
      ArchType types [] = {
        ArchType.x86,
        ArchType.x86_64,
        ArchType.PPC_G5
      };
      sUnixArch = Collections.unmodifiableList(Arrays.asList(types));
    }
    {
      ArchType types [] = {
        ArchType.x86_64,
        ArchType.PPC_G4,
        ArchType.PPC_G5
      };
      sMacArch = Collections.unmodifiableList(Arrays.asList(types));
    }
  }
}
