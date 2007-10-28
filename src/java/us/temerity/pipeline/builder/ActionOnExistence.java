// $Id: ActionOnExistence.java,v 1.2 2007/10/28 21:50:25 jim Exp $

package us.temerity.pipeline.builder;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A C T I O N  O N   E X I S T E N C E                                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * The action a Builder should to take when it encounters an already existing instance of 
 * the node it wishes to build.
 */
public
enum ActionOnExistence
{  
  /**
   * Check-out the latest version of the node into the current working area, but do not 
   * overwrite any of its settings.
   */
  CheckOut,

  /**
   * Just use the existing working version of the node as-is.
   */
  Continue,

  /**
   * Abort execution of the builder with an error.
   */
  Abort,

  /**
   * Change the properties and links of the current working version to match what the 
   * builder expects for the node.
   */
  Conform;
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<ActionOnExistence>
  all() 
  {
    ActionOnExistence values[] = values();
    ArrayList<ActionOnExistence> all = new ArrayList<ActionOnExistence>(values.length);
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
    for(ActionOnExistence method : ActionOnExistence.all()) 
      titles.add(method.toTitle());
    return titles;
  }


  /*----------------------------------------------------------------------------------------*/

  public static ActionOnExistence
  valueFromKey
  (
   int key
  )
  {
    return ActionOnExistence.values()[key];
  }
   
  public static ActionOnExistence
  valueFromString
  (
   String string  
  )
  {
    ActionOnExistence toReturn = null;
    for (ActionOnExistence each : ActionOnExistence.values())
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
