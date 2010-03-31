// $Id: MiscGetArchiveRsp.java,v 1.5 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A R C H I V E   R S P                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetArchiveReq MiscGetArchiveReq} request.
 */
public
class MiscGetArchiveRsp
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
   * @param archive
   *   The archive.
   */ 
  public
  MiscGetArchiveRsp
  (
   TaskTimer timer, 
   ArchiveVolume archive
  )
  { 
    super(timer);

    if(archive == null) 
      throw new IllegalArgumentException("The archive cannot be (null)!");
    pArchive = archive;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getArchive(): " + pArchive.getName() + "\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the archive.
   */
  public ArchiveVolume
  getArchive() 
  {
    return pArchive;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2560564056383317219L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The archive information.
   */ 
  private ArchiveVolume  pArchive;

}
  
