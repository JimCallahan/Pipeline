// $Id: MiscRestoreQueryReq.java,v 1.1 2005/03/23 20:45:01 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   R E S T O R E   Q U E R Y   R E Q                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request get the names and revision numbers of the offline checked-in versions 
 * who's names match the given criteria.
 */ 
public
class MiscRestoreQueryReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> for all nodes.
   */
  public
  MiscRestoreQueryReq
  (
   String pattern
  )
  {
    pPattern = pattern;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes or <CODE>null</CODE> for all nodes.
   */ 
  public String
  getPattern() 
  {
    return pPattern;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4343100083154656875L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes to restore or <CODE>null</CODE> for all nodes.
   */ 
  private String  pPattern;

}
  
