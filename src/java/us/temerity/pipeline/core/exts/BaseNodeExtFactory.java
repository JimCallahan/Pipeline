// $Id: BaseNodeExtFactory.java,v 1.5 2007/07/08 01:18:16 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

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



