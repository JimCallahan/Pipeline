// $Id: MiscGetEditorForSuffixReq.java,v 1.1 2004/06/08 02:24:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   E D I T O R   F O R   S U F F I X                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the name of the default editor for the given file suffix.
 * 
 * @see MasterMgr
 */
public
class MiscGetEditorForSuffixReq
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
   * @param suffix
   *   The filename suffix.
   */
  public
  MiscGetEditorForSuffixReq
  (
   String author, 
   String suffix
  )
  {
    if(author == null) 
      throw new IllegalArgumentException
	("The author cannot be (null)!");
    pAuthor = author;

    if(suffix == null) 
      throw new IllegalArgumentException
	("The toolset suffix cannot be (null)!");
    pSuffix = suffix;
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
   * Gets the filename suffix.
   */ 
  public String
  getSuffix() 
  {
    return pSuffix;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6114352092505113198L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The owner of suffix/editor mappings.
   */
  private String  pAuthor;  

  /**
   * The filename suffix.
   */
  private String  pSuffix;  

}
  
