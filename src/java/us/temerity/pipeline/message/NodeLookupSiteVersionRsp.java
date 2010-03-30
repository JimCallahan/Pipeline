// $Id: NodeLookupSiteVersionRsp.java,v 1.1 2009/03/25 22:02:24 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*  N O D E   L O O K U P   S I T E  V E R S I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Lookup the NodeVersion contained within the extracted site version JAR archive.
 */
public
class NodeLookupSiteVersionRsp
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
   * @param vsn
   *   The node version. 
   */
  public
  NodeLookupSiteVersionRsp
  (
   TaskTimer timer, 
   NodeVersion vsn
  )
  { 
    super(timer);

    if(vsn == null) 
      throw new IllegalArgumentException("The node version cannot be (null)!"); 
    pNodeVersion = vsn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The node version.
   */
  public NodeVersion
  getNodeVersion() 
  {
    return pNodeVersion; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1247530889992696807L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The node version; 
   */
  private NodeVersion  pNodeVersion; 

}
  
