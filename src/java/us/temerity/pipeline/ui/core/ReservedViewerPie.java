// $Id: ReservedViewerPie.java,v 1.2 2007/01/05 23:46:10 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S E R V E D   V I E W E R   P I E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a reservation Histogram as a pie chart.                                       
 */
public 
class ReservedViewerPie
  extends ViewerPie
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer pie.
   * 
   * @param hist
   *   The histogram to visualize.
   * 
   * @param workGroups
   *   The names of the user work groups.
   */ 
  public 
  ReservedViewerPie
  (
   Histogram hist, 
   TreeSet<String> workGroups
  ) 
  {
    super(hist, false);

    if(workGroups == null) 
      throw new IllegalArgumentException("The work groups cannot be (null)!");
    pWorkGroups = workGroups;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Perform any modifications of the histogram catagory label required.
   */ 
  protected String
  formatLabel
  (
   String group
  ) 
  {
    if((group != null) && pWorkGroups.contains(group))
      return ("[" + group + "]");
    return group;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the user work groups.
   */ 
  private TreeSet<String>  pWorkGroups; 

}
