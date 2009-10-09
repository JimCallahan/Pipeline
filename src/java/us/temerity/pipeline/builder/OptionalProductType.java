// $Id: OptionalProductType.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   O P T I O N A L   P R O D U C T   T Y P E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 *  Enum for the Product Type on optional branches in Template Builders.
 */
public 
enum OptionalProductType
{
  UseProduct,
  LockCurrent;
  
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
    for(OptionalProductType method : values()) 
      titles.add(method.toTitle());
    return titles;
  }
  
  /*----------------------------------------------------------------------------------------*/

  public static OptionalProductType
  valueFromKey
  (
   int key
  )
  {
    return OptionalProductType.values()[key];
  }
   
  public static OptionalProductType
  valueFromString
  (
    String string  
  )
  {
    OptionalProductType toReturn = null;
    for (OptionalProductType each : values())
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
