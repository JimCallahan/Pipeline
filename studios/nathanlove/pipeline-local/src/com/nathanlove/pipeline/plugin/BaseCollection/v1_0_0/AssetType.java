// $Id: AssetType.java,v 1.1 2008/05/26 03:19:49 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   T Y P E                                                                    */
/*------------------------------------------------------------------------------------------*/


/**
 * A list of the different types of assets that projects using the Base Collection can
 * contain.
 */
public 
enum AssetType
{
  character, prop, env, cam;
  

  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle()
  {
    switch(this) {
    case character:
      return "char";
    default:
      return super.toString();
    }
  }
  
  @Override
  public String 
  toString()
  {
    return toTitle();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles()
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    for (AssetType type : values()) {
      toReturn.add(type.toTitle());
    }
    return toReturn;
  }
  
  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  commonTitles()
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    for (AssetType type : values()) {
      if (type != cam)
        toReturn.add(type.toTitle());
    }
    return toReturn;
  }
  
  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<AssetType>
  all() 
  {
    return new ArrayList<AssetType>(Arrays.asList(values()));
  }
  
  public static AssetType
  fromString
  (
    String value  
  )
  {
    for (AssetType type: values()) {
      if (type.toTitle().equals(value))  {
        return type;
      }
    }
    return null;
  }
}
