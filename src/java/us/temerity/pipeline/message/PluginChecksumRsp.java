// $Id: PluginChecksumRsp.java,v 1.2 2009/04/07 01:48:12 jlee Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*    P L U G I N S   R E S O U R C E   C H E C K S U M S   R S P                           */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link PluginResourceChecksumReq PluginResourceChecksumReq} 
 * request.
 */
public 
class PluginChecksumRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a PluginChecksumRsp response.
   *
   * @param timer
   *   The timing statistics for a task.
   *
   * @param checksums
   *   The checksums bytes of resources indexed by a resource path.
   *
   */
  public
  PluginChecksumRsp
  (
   TaskTimer timer, 
   SortedMap<String,byte[]> checksums
  )
  { 
    super(timer);

    pChecksums = checksums;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  public SortedMap<String,byte[]>
  getChecksums() 
  {
    return pChecksums;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6082991414896326002L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private SortedMap<String,byte[]> pChecksums;

}

