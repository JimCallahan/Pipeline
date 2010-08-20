// $Id: EntityType.java,v 1.2 2009/05/13 22:48:09 jesse Exp $

package us.temerity.pipeline.builder.v2_4_28;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E N T I T Y   T Y P E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The ShotgunEntity types that are valid in Task Annotation.
 */
public 
enum EntityType
{
  Asset, 
  Shot, 
  Practical, 
  Tool, 
  Element, 
  File,
  CustomEntity01,
  CustomEntity02,
  CustomEntity03,
  CustomEntity04,
  CustomEntity05,
  CustomEntity06,
  CustomEntity07,
  CustomEntity08,
  CustomEntity09,
  CustomEntity10,
  Ignore;
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  public String 
  toTitle()
  {
    switch (this) {
    case Ignore: 
      return "[[Ignore]]";
    }
    return super.toString();
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
    for (EntityType type : values()) {
      toReturn.add(type.toTitle());
    }
    return toReturn;
  }
  
  /**
   * Converts a String into an EntityType, using the titles.
   */
  public static EntityType
  fromString
  (
    String value  
  )
  {
    for (EntityType type: values()) {
      if (type.toTitle().equals(value))  {
        return type;
      }
    }
    return null;
  }
  
}
