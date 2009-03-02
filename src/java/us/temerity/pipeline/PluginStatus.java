// $Id: PluginStatus.java,v 1.1 2009/03/02 00:16:27 jlee Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   S T A T U S                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The status of a plugin.
 */
public
enum PluginStatus
{
  /**
   * Plugins that are found in the required plugin GLUE file.
   */
  Required, 

  /**
   * Plugins that are required and have been loaded/installed.
   */
  Installed, 

  /**
   * Required plugins that have been loaded/installed but marked under 
   * development.
   */
  UnderDevelopment, 

  /**
   * Required plugins that have been loaded/installed but not marked 
   * under development.
   */
  Permanent, 

  /**
   * Required plugins that have not been loaded/installed.
   */
  Missing, 

  /**
   * Plugins not found in the required plugins GLUE files during startup.
   */
  Unknown;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<PluginStatus>
  all() 
  {
    PluginStatus values[] = values();
    ArrayList<PluginStatus> all = new ArrayList<PluginStatus>(values.length);
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
    for(PluginStatus status : PluginStatus.all()) 
      titles.add(status.toTitle());
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
    return toString();
  }

  /**
   * Convert to a short name representation.
   */
  public String
  toShortName()
  {
    return sShortNames[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sShortNames[] = {
    "req", 
    "inst", 
    "dev", 
    "perm", 
    "miss", 
    "unknown"
  };

}

