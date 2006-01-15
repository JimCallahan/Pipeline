// $Id: QueueGetUnfinishedJobsForNodesReq.java,v 1.1 2006/01/15 17:42:27 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   U N F I N I S H E D   J O B S   F O R   N O D E S   R E Q          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the job IDs of unfinished jobs associated with the given nodes.
 */
public
class QueueGetUnfinishedJobsForNodesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param fseqs
   *   The primary file sequences indexed by fully resolved names of the nodes.
   */
  public
  QueueGetUnfinishedJobsForNodesReq
  (
   String author, 
   String view, 
   TreeMap<String,FileSeq> fseqs
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    if(fseqs == null) 
      throw new IllegalArgumentException("The file sequences cannot be (null)!");
    pFileSeqs = fseqs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of user which owens the working area.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /** 
   * Get the name of the working area view.
   */
  public String
  getView()
  {
    return pView;
  }

  /**
   * Get the primary file sequences indexed by fully resolved names of the nodes.
   */
  public TreeMap<String,FileSeq>
  getFileSeqs()
  {
    return pFileSeqs;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1132626567556170767L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of user which owens the working version.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view.
   */
  private String  pView;

  /** 
   * The primary file sequences indexed by fully resolved names of the nodes.
   */
  private TreeMap<String,FileSeq>  pFileSeqs; 

}
  
