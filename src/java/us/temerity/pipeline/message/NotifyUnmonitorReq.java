// $Id: NotifyUnmonitorReq.java,v 1.1 2004/04/11 19:31:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   M O N I T O R   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to begin monitoring a directory for changes.
 * 
 * @see DNotify
 */
public
class NotifyUnmonitorReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param dir
   *   The directory to unmonitor.
   */
  public
  NotifyUnmonitorReq
  (
   File dir
  )
  { 
    if(dir == null) 
      throw new IllegalArgumentException
	("The directory cannot be (null)");
    pDir = dir;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the directory to unmonitor.
   */
  public File
  getDirectory() 
  {
    return pDir;
  }
  
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -928588138957379666L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The directory to unmonitor.
   */ 
  private File  pDir;


}
  
