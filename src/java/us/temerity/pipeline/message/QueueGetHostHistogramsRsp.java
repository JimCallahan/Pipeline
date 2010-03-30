// $Id: QueueGetHostHistogramsRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   H I S T O G R A M S   R S P                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Gets frequency distribution data for significant catagories of information shared 
 * by all job server hosts. 
 */
public
class QueueGetHostHistogramsRsp
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
   * @param hist
   *   The requested job server histograms.
   */ 
  public
  QueueGetHostHistogramsRsp
  (
   TaskTimer timer, 
   QueueHostHistograms histograms
  )
  { 
    super(timer);

    if(histograms == null) 
      throw new IllegalArgumentException("The histograms cannot be (null)!");
    pHistograms = histograms;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHostHistograms():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the requested histograms.
   */
  public QueueHostHistograms
  getHistograms() 
  {
    return pHistograms; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1684240265953715015L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The requested histograms. 
   */ 
  private QueueHostHistograms  pHistograms; 

}
  
