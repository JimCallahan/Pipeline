// $Id: NodeKillJobGroupReq.java,v 1.1 2004/08/04 01:43:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   K I L L   J O B   G R O U P   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to kill all jobs which belong to the job group with the given ID. <P> 
 * 
 * @see MasterMgr
 */
public
class NodeKillJobGroupReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param author 
   *   The name of the user which owns the jobs.
   * 
   * @param groupID
   *   The unique job group identifier.
   */
  public
  NodeKillJobGroupReq
  (
   String author, 
   long groupID
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException
	("The owner of the jobs cannot be (null)!");
    pAuthor = author;

    pGroupID = groupID; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the user which owns the jobs.
   */
  public String
  getAuthor() 
  {
    return pAuthor; 
  }

  /**
   * Gets the unique job group identifier.
   */
  public long
  getGroupID() 
  {
    return pGroupID; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7750367585512039309L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user which owns the jobs.
   */ 
  private String  pAuthor; 

  /**
   * The unique job group identifier.
   */ 
  private long  pGroupID; 

}
  
