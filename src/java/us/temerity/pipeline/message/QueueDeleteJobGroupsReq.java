// $Id: QueueDeleteJobGroupsReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   D E L E T E   J O B   G R O U P S   R E Q                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to delete the completed job groups. <P> 
 */
public
class QueueDeleteJobGroupsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param groupAuthors
   *   The name of the user which submitted the group indexed by job group ID.
   */
  public
  QueueDeleteJobGroupsReq
  (
   TreeMap<Long,String> groupAuthors
  )
  { 
    super();

    if(groupAuthors == null) 
      throw new IllegalArgumentException("The group authors cannot be (null)!");
    pGroupAuthors = groupAuthors;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the user which submitted the group indexed by job group ID.
   */
  public TreeMap<Long,String>
  getGroupAuthors()
  {
    return pGroupAuthors; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4291683818382930434L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user which submitted the group indexed by job group ID.
   */ 
  private TreeMap<Long,String>  pGroupAuthors; 

}

  
