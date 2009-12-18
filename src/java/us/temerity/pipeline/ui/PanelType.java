// $Id: PanelType.java,v 1.1 2009/12/18 08:44:26 jim Exp $

package us.temerity.pipeline.ui;

/*------------------------------------------------------------------------------------------*/
/*   P A N E L   T Y P E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The types of panels contained in the top-level windows of plui(1).
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

  None;
}
