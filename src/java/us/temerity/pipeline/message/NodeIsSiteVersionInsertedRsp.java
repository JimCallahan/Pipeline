// $Id: NodeIsSiteVersionInsertedRsp.java,v 1.1 2009/03/25 22:02:24 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   M I S S I N G   S I T E  V E R S I O N   R E F S   R S P             */
/*------------------------------------------------------------------------------------------*/

/**
 * Whether the extracted node contained in the given JAR archive has already been inserted
 * into the node database.
 */
public
class NodeIsSiteVersionInsertedRsp
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
   * @param isInserted
   *   Whether the extracted node has already been inserted.
   */
  public
  NodeIsSiteVersionInsertedRsp
  (
   TaskTimer timer, 
   boolean isInserted
  )
  { 
    super(timer);

    pIsInserted = isInserted; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the extracted node has already been inserted.
   */
  public boolean
  isInserted() 
  {
    return pIsInserted; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6790819977837520311L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the extracted node has already been inserted.
   */
  private boolean  pIsInserted; 

}
  
