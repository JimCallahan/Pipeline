// $Id: JobGetResourcesRsp.java,v 1.1 2004/07/28 19:10:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   G E T   R E S O U R C E S   R S P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a request to get a point sample of the currently available 
 * system resources.
 */ 
public
class JobGetResourcesRsp
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
   * @param sample
   *   The point sample of the currently available system resources.
   */ 
  public
  JobGetResourcesRsp
  (
   TaskTimer timer, 
   ResourceSample sample 
  )
  { 
    super(timer);

    if(sample == null) 
      throw new IllegalArgumentException("The resource sample cannot be (null)!");
    pSample = sample; 

    Logs.net.finest("JobMgr.getResources():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the point sample of the currently available system resources.
   */
  public ResourceSample
  getSample()
  {
    return pSample;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -504374384135167412L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The point sample of the currently available system resources.
   */ 
  private ResourceSample  pSample; 

}
  
