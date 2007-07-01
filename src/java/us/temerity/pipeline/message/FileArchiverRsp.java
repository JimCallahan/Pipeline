// $Id: FileArchiverRsp.java,v 1.2 2007/07/01 23:54:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   A R C H I V E R   R S P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileArchiveReq FileArchiveReq} or 
 * {@link FileRestoreReq FileRestoreReq} request.
 */
public
class FileArchiverRsp
  extends DryRunRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response. <P> 
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param output
   *   The STDOUT output of the archiver process or <CODE>null</CODE> if none exists.
   * 
   * @param msg
   *   An optional text message detailing how the operation would have been performed 
   *   or <CODE>null</CODE> if the operation was performed.
   */
  public
  FileArchiverRsp
  (
   TaskTimer timer, 
   String output, 
   String msg
  )
  { 
    super(timer, msg);

    pOutput = output;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the STDOUT output of the archiver process or <CODE>null</CODE> if none exists.
   */
  public String
  getOutput() 
  {
    return pOutput; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7262758143788564436L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The STDOUT output of the archiver process or <CODE>null</CODE> if none exists.
   */ 
  private String   pOutput; 

}
  
