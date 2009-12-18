// $Id: PanelType.java,v 1.2 2009/12/18 19:55:56 jim Exp $

package us.temerity.pipeline.ui;

import java.util.ArrayList; 

/*------------------------------------------------------------------------------------------*/
/*   P A N E L   T Y P E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**x
 * The types of panels contained in the top-level windows of plui(1).<P> 
 */ 
public 
enum PanelType
{
  NodeBrowser, 
  NodeViewer, 

  NodeDetails, 
  NodeFiles, 
  NodeLinks, 
  NodeHistory, 
  NodeAnnotations, 

  QueueStats, 
  QueueServers, 
  QueueSlots, 
  
  JobBrowser,
  JobViewer, 
  JobDetails, 

  Empty;

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<PanelType>
  all() 
  {
    PanelType values[] = values();
    ArrayList<PanelType> all = new ArrayList<PanelType>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
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
    return sTitles[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "Node Browser", 
    "Node Viewer", 
    "Node Details", 
    "Node Files", 
    "Node Links", 
    "Node History", 
    "Node Annotations", 
    "Queue Stats", 
    "Queue Servers", 
    "Queue Slots", 
    "Job Browser",
    "Job Viewer", 
    "Job Details", 
    "Empty"
  };
}
