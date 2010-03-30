// $Id: MiscSetSuffixEditorsReq.java,v 1.1 2004/06/08 02:24:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   S U F F I X   E D I T O R S   R E Q                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the filename suffix to default editor mappings for the given user. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscSetSuffixEditorsReq
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
   * 
   * @param editors
   *   The suffix editors.
   */
  public
  MiscSetSuffixEditorsReq
  (
   String author, 
   TreeSet<SuffixEditor> editors   
  )
  {
    if(author == null) 
      throw new IllegalArgumentException
	("The author cannot be (null)!");
    pAuthor = author;

    if(editors == null) 
      throw new IllegalArgumentException("The suffix editors cannot be (null)!");
    pEditors = editors;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the owner of suffix/editor mappings.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }
 
  /**
   * Gets the suffix editors.
   */
  public TreeSet<SuffixEditor>
  getEditors() 
  {
    return pEditors;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2549803674915885869L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The owner of suffix/editor mappings.
   */
  private String  pAuthor;  

  /**
   * The suffix editors.
   */ 
  private TreeSet<SuffixEditor>  pEditors;

}
  
