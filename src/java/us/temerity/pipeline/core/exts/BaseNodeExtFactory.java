// $Id: BaseNodeExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   N O D E   E X T   F A C T O R Y                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for extension factories related to working area nodes.
 */
public 
class BaseNodeExtFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   */ 
  public 
  BaseNodeExtFactory
  (
   NodeID nodeID
  )      
  {
    pNodeID = nodeID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier. 
   */ 
  protected NodeID  pNodeID; 

}



