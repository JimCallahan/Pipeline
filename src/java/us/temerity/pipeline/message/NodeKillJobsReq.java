// $Id: NodeKillJobsReq.java,v 1.1 2004/08/04 01:43:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   K I L L   J O B S   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to kill the jobs with the given IDs. <P> 
 * 
 * @see MasterMgr
 */
public
class NodeKillJobsReq
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
   * @param jobIDs
   *   The unique job identifiers.
   */
  public
  NodeKillJobsReq
  (
   String author, 
   long[] jobIDs
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException
	("The owner of the jobs cannot be (null)!");
    pAuthor = author;

    if(jobIDs == null)
      throw new IllegalArgumentException
	("The job IDs cannot be (null)!");
    pJobIDs = jobIDs;
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
   * Gets the unique job identifiers.
   */
  public long[]
  getJobIDs() 
  {
    return pJobIDs; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3516689695721074417L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user which owns the jobs.
   */ 
  private String  pAuthor; 

  /**
   * The unique job identifiers.
   */ 
  private long[]  pJobIDs;

}
  
