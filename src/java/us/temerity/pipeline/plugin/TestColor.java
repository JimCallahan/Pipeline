// $Id: TestColor.java,v 1.1 2004/06/14 22:30:25 jim Exp $

package us.temerity.pipeline.plugin;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   C O L O R                                                                    */
/*------------------------------------------------------------------------------------------*/
  
/**
 * A dummy enumeration used to test Enum based action parameters.
 */ 
public 
enum TestColor 
{ 
  /**
   * The colors.
   */ 
  Red, Green, Blue, Orange, Yellow, Purple;
  
  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<TestColor>
  all() 
  {
    TestColor values[] = values();
    ArrayList<TestColor> all = new ArrayList<TestColor>(values.length);
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
    for(TestColor color : TestColor.all()) 
      titles.add(color.toTitle());
    return titles;
  }

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return toString();
  }
};    

