// $Id: MiscGetSuffixEditorsReq.java,v 1.1 2004/06/08 02:24:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   S U F F I X   E D I T O R S   R E Q                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the filename suffix to default editor mappings for the given user. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscGetSuffixEditorsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param author
   *   The owner of suffix/editor mappings.
   */
  public
  MiscGetSuffixEditorsReq
  (
   String author
  )
  {
    if(author == null) 
      throw new IllegalArgumentException
	("The author cannot be (null)!");
    pAuthor = author;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the owner of suffix/editor mappings.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7704227096394438057L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The owner of suffix/editor mappings.
   */
  private String  pAuthor;  

}
  
