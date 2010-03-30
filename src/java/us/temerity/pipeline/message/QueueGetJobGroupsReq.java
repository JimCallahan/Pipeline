// $Id: QueueGetJobGroupsReq.java,v 1.1 2009/05/14 23:30:43 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   G R O U P   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the job groups which match the following working area pattern.
 */
public
class QueueGetJobGroupsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param author
   *   The name of the user owning the job groups or 
   *   <CODE>null</CODE> to match all users.
   * 
   * @param view 
   *   The name of the working area view owning the job groups or 
   *   <CODE>null</CODE> to match all working areas.
   */
  public
  QueueGetJobGroupsReq
  (
   String author,
   String view
  )
  { 
    pAuthor = author;
    pView   = view;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets name of the user owning the job groups or 
   * <CODE>null</CODE> to match all users.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /**
   * Gets name of the working area view owning the job groups or 
   * <CODE>null</CODE> to match all working areas.
   */ 
  public String
  getView() 
  {
    return pView;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1028840693669218795L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user owning the job groups or 
   * <CODE>null</CODE> to match all users.
   */
  private String  pAuthor;  

  /**
   * The name of the working area view owning the job groups or 
   * <CODE>null</CODE> to match all working areas.
   */
  private String  pView; 


}

  
