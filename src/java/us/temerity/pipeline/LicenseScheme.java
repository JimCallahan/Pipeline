// $Id: LicenseScheme.java,v 1.1 2005/05/31 09:37:45 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I C E N S E   S C H E M E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The licensing scheme used to determine the number of available floating licenses associated
 * with a {@link LicenseKey LicenseKey}.
 */
public
enum LicenseScheme
{  
  /** 
   * Each job uses a single license regardless of which host it is running on or the 
   * number of other jobs on the host using a license.
   */
  PerSlot,   

  /** 
   * Any number of jobs running on the same host use only a single license. <P> 
   * 
   * A count is maintained of the number of jobs on each host using the license key. Licenses 
   * are reserved when the count on a machine increases from 0 to 1 and released when the 
   * count decreases from 1 to 0. Additional jobs on a machine already using a license do 
   * not require reserving additional licenses.
   */
  PerHost, 
  
  /** 
   * Only a limited number of jobs may be run on a single host, but only a single license 
   * is used for each host regardless of the number of slots in use. <P> 
   * 
   * Behaves identically to <CODE>PerHost</CODE> except that there is a limit on the total 
   * number of slots which may used on a single machine.
   */
  PerHostSlot; 



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<LicenseScheme>
  all() 
  {
    LicenseScheme values[] = values();
    ArrayList<LicenseScheme> all = new ArrayList<LicenseScheme>(values.length);
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
    for(LicenseScheme scheme : LicenseScheme.all()) 
      titles.add(scheme.toTitle());
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
    "Per Slot", 
    "Per Host", 
    "Per Host Slot"
  };

};
