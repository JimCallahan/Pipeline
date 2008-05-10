// $Id: TaskType.java,v 1.1 2008/05/10 03:23:22 jesse Exp $

package us.temerity.pipeline.builder.v2_4_1;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   T Y P E                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A list of the standard task types used by version 2.4.1 of the Task Annotations, Task
 * Extensions, and Task Builders.
 * <p>
 * These are always used as Strings in the actual extensions and annotations, so are just
 * provided here as annotations to ensure correctness between all the different pieces that
 * use them.  Methods are provided for String equivalents for each TaskType as well as 
 * retrieving a Collection of all the String equivalents which is perfect for use in dropdown
 * menus and EnumParam types.
 */
public 
enum TaskType
{
  Asset, 
  Modeling, 
  Rigging, 
  LookDev, 
  Shading, 
  Texturing, 
  Layout,
  Animation,        
  Effects,
  Lighting,
  Rendering,
  Plates,
  Tracking,
  Mattes,
  MattePainting, 
  Compositing,  
  CUSTOM;
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public String 
  toString()
  {
    switch (this) {
    case LookDev:
      return "Look Dev";
    case CUSTOM:
      return "[[CUSTOM]]";
    default:
      return super.toString();    
    }
  }
  
  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle()
  {
    return toString();
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
    for (TaskType type : values()) {
      toReturn.add(type.toTitle());
    }
    return toReturn;
  }
  
  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<TaskType>
  all() 
  {
    return new ArrayList<TaskType>(Arrays.asList(values()));
  }
  
}

