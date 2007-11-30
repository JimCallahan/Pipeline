package us.temerity.pipeline;

import java.util.ArrayList;

import us.temerity.pipeline.PropertyState;

/**
 * What sort of control is this property operating under?
 * <p>
 * <ul>
 * <li> Manual
 * <li> SemiAutomatic
 * <li> Automatic
 * </ul>
 */
public enum
EditableState
{
  /**
   * The property is solely under the control of users.
   */
  Manual,
  
  /**
   * The property is currently under the control of users, but it may
   * be taken over by its schedule in the future.
   */
  SemiAutomatic,
  
  /**
   * The property is under the control of a schedule and cannot be modified by 
   * users.
   */
  Automatic;
  
  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(PropertyState status : PropertyState.values()) 
      titles.add(status.toString());
    return titles;
  }
}