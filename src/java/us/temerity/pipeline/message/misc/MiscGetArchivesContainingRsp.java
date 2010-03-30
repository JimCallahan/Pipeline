// $Id: MiscGetArchivesContainingRsp.java,v 1.3 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A R C H I V E S   C O N T A I N I N G   R S P                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a 
 * {@link MiscGetArchivesContainingReq MiscGetArchivesContainingReq} request.
 */
public
class MiscGetArchivesContainingRsp
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
   * @param archives
   *   The names of the archives containing the requested checked-in versions indexed by 
   *   fully resolved node name and revision number.
   */ 
  public
  MiscGetArchivesContainingRsp
  (
   TaskTimer timer, 
   DoubleMappedSet<String,VersionID,String> archives
  )
  { 
    super(timer);

    if(archives == null) 
      throw new IllegalArgumentException("The archive names cannot be (null)!");
    pArchives = archives;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getArchivesContaining()\n  " + getTimer());
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of the archives containing the requested checked-in versions indexed by 
   * fully resolved node name and revision number.
   */
  public DoubleMappedSet<String,VersionID,String>
  getArchiveNames()
  {
    return pArchives;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3775313534470409809L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the archives containing the requested checked-in versions indexed by 
   * fully resolved node name and revision number.
   */ 
  private DoubleMappedSet<String,VersionID,String> pArchives;

}
  
