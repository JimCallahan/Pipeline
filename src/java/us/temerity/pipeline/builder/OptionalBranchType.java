// $Id: OptionalBranchType.java,v 1.1 2009/08/12 20:33:05 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

/**
 *  Enum for the Optional Branches in Template Builders.
 */
public 
enum OptionalBranchType
{
  BuildOnly, 
  AsProduct;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(OptionalBranchType method : OptionalBranchType.values()) 
      titles.add(method.toTitle());
    return titles;
  }
  
  /*----------------------------------------------------------------------------------------*/

  public static OptionalBranchType
  valueFromKey
  (
   int key
  )
  {
    return OptionalBranchType.values()[key];
  }
   
  public static OptionalBranchType
  valueFromString
  (
    String string  
  )
  {
    OptionalBranchType toReturn = null;
    for (OptionalBranchType each : OptionalBranchType.values())
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
