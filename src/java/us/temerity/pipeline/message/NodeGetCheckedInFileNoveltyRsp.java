// $Id: NodeGetCheckedInFileNoveltyRsp.java,v 1.2 2004/07/16 22:04:39 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   C H E C K E D - I N   F I L E   N O V E L T Y   R S P                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a 
 * {@link NodeGetCheckedInFileNoveltyReq NodeGetCheckedInFileNoveltyReq} request.
 */
public
class NodeGetCheckedInFileNoveltyRsp
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
   * @param name
   *   The fully resolved node name.
   * 
   * @param novelty
   *   The file novelty table.
   */
  public
  NodeGetCheckedInFileNoveltyRsp
  (
   TaskTimer timer, 
   String name, 
   TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty
  )
  { 
    super(timer);

    if(novelty == null) 
      throw new IllegalArgumentException("The novelty cannot be (null)!");
    pNovelty = novelty;

    Logs.net.finest("MasterMgr.getCheckedInFileNovelty(): " + name + ":\n" + 
		    "  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets whether each file associated with each checked-in version of the given node 
   * contains new data not present in the previous checked-in versions. <P> 
   */
  public TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>
  getFileNovelty()
  {
    return pNovelty;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2953009359519888695L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether each file associated with each checked-in version of the given node 
   * contains new data not present in the previous checked-in versions. <P> 
   */
  private TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> pNovelty;

}
  
