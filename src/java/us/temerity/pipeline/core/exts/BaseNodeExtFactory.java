// $Id: BaseNodeExtFactory.java,v 1.4 2007/06/22 01:26:09 jim Exp $

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
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all nodes associated with the operation.
   */ 
  public LinkedList<String> 
  getNodeNames()
  {
    LinkedList<String> names = new LinkedList<String>();
    if(pNodeID != null) 
      names.add(pNodeID.getName()); 
    
    return names;
  }

  /**
   * Get the name of the user performing the operation. 
   */ 
  public String 
  getWorkUser()
  {
    if(pNodeID != null) 
      return pNodeID.getAuthor(); 
    return null;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier. 
   */ 
  protected NodeID  pNodeID; 

}



