// $Id: PluginType.java,v 1.1 2006/10/23 11:30:20 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   T Y P E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The general classifications of plugins used by Pipeline.
 */
public
enum PluginType
{  
  /**
   * Plugins used to launch applications to view or edit the files associated with nodes.
   */
  Editor, 

  /**
   * Plugins used to compare versions of files associated with nodes.
   */
  Comparator, 

  /**
   * Plugins used to run applications to regenerate the files associated with nodes.
   */
  Action, 

  /**
   * Plugins used to perform arbitrary operations on nodes.
   */
  Tool, 

  /**
   * Plugins used to archive and restore checked-in plugin versions.
   */
  Archiver, 

  /**
   * Plugins used to extend the behavior of the Master Manager daemon.
   */
  MasterExt, 

  /**
   * Plugins used to extend the behavior of the Queue Manager daemon.
   */
  QueueExt; 



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<PluginType>
  all() 
  {
    PluginType values[] = values();
    ArrayList<PluginType> all = new ArrayList<PluginType>(values.length);
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
    for(PluginType policy : PluginType.all()) 
      titles.add(policy.toTitle());
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

}
