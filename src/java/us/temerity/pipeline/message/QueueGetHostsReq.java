// $Id: QueueGetHostsReq.java,v 1.1 2006/12/05 18:23:30 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T S   R E Q                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Gets the current state of the hosts participating in the Pipeline queue filtered
 * by the given histogram specs. <P> 
 */
public 
class QueueGetHostsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param specs
   *   The histograms catagory specifications or <CODE>null</CODE> for all hosts.
   */
  public
  QueueGetHostsReq
  (
   QueueHostHistogramSpecs specs
  )
  { 
    pSpecs = specs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the histograms catagory specifications or <CODE>null</CODE> for all hosts.
   */ 
  public QueueHostHistogramSpecs
  getSpecs() 
  {
    return pSpecs; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8247283468770977135L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The histograms catagory specifications.
   */ 
  private QueueHostHistogramSpecs  pSpecs; 

}
  
