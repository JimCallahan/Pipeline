// $Id: QueueGetHostHistogramsReq.java,v 1.1 2006/12/05 18:23:30 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   H I S T O G R A M S   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Gets frequency distribution data for significant catagories of information shared 
 * by all job server hosts. 
 */
public 
class QueueGetHostHistogramsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param specs
   *   The histograms catagory specifications.
   */
  public
  QueueGetHostHistogramsReq
  (
   QueueHostHistogramSpecs specs
  )
  { 
    if(specs == null) 
      throw new IllegalArgumentException
	("The histogram specs cannot be (null)!");
    pSpecs = specs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the histograms catagory specifications.
   */ 
  public QueueHostHistogramSpecs
  getSpecs() 
  {
    return pSpecs; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2010113776185478862L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The histograms catagory specifications.
   */ 
  private QueueHostHistogramSpecs  pSpecs; 

}
  
