// $Id: DownstreamMode.java,v 1.1 2008/09/29 19:02:17 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D O W N S T R E A M   M O D E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The criteria used to determine how downstream node status is reported.
 */
public
enum DownstreamMode
{  
  /**
   * Ignore all nodes downstream of the root nodes of the status operation.
   */
  None, 
  
  /**
   * Only report nodes checked-out into the current working area which contain one or more
   * of the root nodes of the status operation in the nodes reachable upstream via other
   * working nodes checked-out into the same working area. 
   */
  WorkingOnly, 

  /**
   * Only report nodes with checked-in versions which contain any checked-in version of 
   * one or more of the root nodes of the status operation in the nodes reachable upstream 
   * via other checked-in versions. <P> 
   * 
   * Each checked-in node reported must be either the most downstream and latest checked-in 
   * version or any checked-in version of a node along a path from one of these most 
   * downstream latest checked-in nodes and one or more of the root nodes of the status
   * operation. 
   */
  CheckedInOnly, 

  /**
   * Any node which meets either of the WorkingOnly or CheckedInOnly criteria.
   */
  All;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<DownstreamMode>
  all() 
  {
    DownstreamMode values[] = values();
    ArrayList<DownstreamMode> all = new ArrayList<DownstreamMode>(values.length);
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
    for(DownstreamMode method : DownstreamMode.all()) 
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

  /**
   * Convert from a more human friendly string representation.
   */ 
  public static DownstreamMode
  fromTitle
  (
   String title
  ) 
  {
    DownstreamMode result = DownstreamMode.None;

    for(DownstreamMode dmode : DownstreamMode.all()) {
      if(dmode.toTitle().equals(title)) {
        result = dmode;
        break;
      }
    }

    return result;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "None", 
    "Working Only",
    "Checked-In Only", 
    "All" 
  };
}
