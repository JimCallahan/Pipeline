// $Id: BaseSecondaryExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   S E C O N D A R Y   E X T   F A C T O R Y                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for extension factories related to secondary sequences.
 */
public 
class BaseSecondaryExtFactory
  extends BaseNodeExtFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param fseq
   *   The secondary file sequence.
   */ 
  public 
  BaseSecondaryExtFactory
  (
   NodeID nodeID,
   FileSeq fseq
  )      
  {
    super(nodeID);

    pFileSeq = fseq; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The secondary file sequence.
   */ 
  protected FileSeq  pFileSeq; 

}



