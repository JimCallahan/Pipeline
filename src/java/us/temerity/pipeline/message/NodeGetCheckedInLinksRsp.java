// $Id: NodeGetCheckedInLinksRsp.java,v 1.1 2005/02/09 18:21:12 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   C H E C K E D - I N   L I N K S   R S P                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a 
 * {@link NodeGetCheckedInLinksReq NodeGetCheckedInLinksReq} request.
 */
public
class NodeGetCheckedInLinksRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param links
   *   The checked-in links indexed by revision number and upstream node name.
   */
  public
  NodeGetCheckedInLinksRsp
  (
   TaskTimer timer, 
   TreeMap<VersionID,TreeMap<String,LinkVersion>> links
  )
  { 
    super(timer);

    if(links == null) 
      throw new IllegalArgumentException("The checked-in links cannot be (null)!");
    pLinks = links;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the checked-in links indexed by revision number and upstream node name.
   */
  public TreeMap<VersionID,TreeMap<String,LinkVersion>>
  getLinks() 
  {
    return pLinks;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7325728961990878692L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checked-in links indexed by revision number and upstream node name.
   */ 
  private TreeMap<VersionID,TreeMap<String,LinkVersion>>  pLinks; 

}
  
