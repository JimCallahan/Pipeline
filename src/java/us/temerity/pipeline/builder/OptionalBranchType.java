// $Id: OptionalBranchType.java,v 1.2 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   O P T I O N A L   B R A N C H   T Y P E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 *  Enum for the Optional Branches in Template Builders.
 */
public 
enum OptionalBranchType
{
  BuildOnly, 
  AsProduct,
  CheckOut;

  
  
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
