// $Id: NodeGetDownstreamCheckedInLinksRsp.java,v 1.1 2006/10/18 08:43:17 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   D O W N S T R E A M   C H E C K E D - I N   L I N K S   R S P        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetDownstreamCheckedInLinksReq} request.
 */
public
class NodeGetDownstreamCheckedInLinksRsp
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
   *   The checked-in links indexed by the name and revision number of the downstream node. 
   */
  public
  NodeGetDownstreamCheckedInLinksRsp
  (
   TaskTimer timer, 
   DoubleMap<String,VersionID,LinkVersion> links
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
   * Gets the checked-in links indexed by the name and revision number of the 
   * downstream node. 
   */
  public DoubleMap<String,VersionID,LinkVersion>
  getLinks() 
  {
    return pLinks;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6385559427371702771L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checked-in links indexed by the name and revision number of the downstream node. 
   */ 
  private  DoubleMap<String,VersionID,LinkVersion> pLinks; 

}
  
