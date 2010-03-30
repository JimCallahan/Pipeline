// $Id: MiscArchiveQueryReq.java,v 1.1 2005/03/10 08:07:27 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A R C H I V E   Q U E R Y   R E Q                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get archive related information about the checked-in versions which match the 
 * given criteria. 
 */
public
class MiscArchiveQueryReq
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
   * 
   * @param maxArchives
   *   The maximum allowable number of archive volumes which contain the checked-in version
   *   in order for it to be inclued in the returned list or <CODE>null</CODE> for any number 
   *   of archives.
   */
  public
  MiscArchiveQueryReq
  (
   String pattern,
   Integer maxArchives
  )
  {
    pPattern     = pattern;
    pMaxArchives = maxArchives;
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

  /** 
   * Get the maximum allowable number of archive volumes which contain the checked-in 
   * version in order for it to be inclued in the returned list or <CODE>null</CODE> for 
   * any number of archives.
   */ 
  public Integer
  getMaxArchives() 
  {
    return pMaxArchives;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6948877135645453830L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes or <CODE>null</CODE> for all nodes.
   */ 
  private String  pPattern;

  /** 
   * The maximum allowable number of archive volumes which contain the checked-in 
   * version in order for it to be inclued in the returned list or <CODE>null</CODE> for 
   * any number of archives.
   */ 
  private Integer  pMaxArchives;

}
  
