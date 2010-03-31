// $Id: MiscArchiveRsp.java,v 1.3 2007/07/01 23:54:23 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A R C H I V E   R S P                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscArchiveReq MiscArchiveReq} 
 * request.
 */
public
class MiscArchiveRsp
  extends DryRunRsp
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
   * @param name
   *   The unique name given to the newly created archive. 
   * 
   * @param msg
   *   An optional text message detailing how the operation would have been performed 
   *   or <CODE>null</CODE> if the operation was performed.
   */ 
  public
  MiscArchiveRsp
  (
   TaskTimer timer, 
   String name, 
   String msg
  )
  { 
    super(timer, "MasterMgr.archive(): " + name + "\n  " + timer, msg);

    if(name == null) 
      throw new IllegalArgumentException("The archive name cannot be (null)!");
    pName = name; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique name given to the newly created archive. 
   */
  public String
  getName()
  {
    return pName; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8688166869973310014L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique name given to the newly created archive. 
   */ 
  private String  pName; 

}
  
