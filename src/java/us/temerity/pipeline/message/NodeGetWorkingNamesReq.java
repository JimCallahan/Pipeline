// $Id: NodeGetWorkingNamesReq.java,v 1.1 2005/02/23 06:50:07 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   W O R K I N G   N A M E S                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names of the nodes in a working area for which have a name 
 * matching the given search pattern.
 */
public
class NodeGetWorkingNamesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> to match all nodes.
   */
  public
  NodeGetWorkingNamesReq
  (
   String author, 
   String view, 
   String pattern
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    pPattern = pattern;
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
   * Get the regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes or <CODE>null</CODE> to match all nodes.
   */ 
  public String
  getPattern() 
  {
    return pPattern;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3536757053743556255L;

  

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
   * The regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes or <CODE>null</CODE> to match all nodes.
   */ 
  private String  pPattern;

}
  
