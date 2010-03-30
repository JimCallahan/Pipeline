// $Id: MiscOfflineQueryReq.java,v 1.2 2005/03/14 16:08:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   O F F L I N E   Q U E R Y   R E Q                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request get offline related information about the checked-in versions which match the 
 * given criteria. <P> 
 */
public
class MiscOfflineQueryReq
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
   * @param excludeLatest
   *   The number of latest checked-in versions of the node to exclude from the returned list
   *   or <CODE>null</CODE> to include all versions.
   * 
   * @param minArchives
   *   The minimum number of archive volumes containing the checked-in version in order for 
   *   it to be inclued in the returned list or <CODE>null</CODE> for any number of archives.
   * 
   * @param unusedOnly
   *   Whether to only include checked-in versions which can be offlined.
   */
  public
  MiscOfflineQueryReq
  (
   String pattern,
   Integer excludeLatest, 
   Integer minArchives, 
   boolean unusedOnly
  )
  {
    pPattern       = pattern;
    pExcludeLatest = excludeLatest;
    pMinArchives   = minArchives;
    pUnusedOnly    = unusedOnly;
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
   * Get the number of newer checked-in versions of the node to exclude from the returned 
   * list or <CODE>null</CODE> to include all versions.
   */ 
  public Integer
  getExcludeLatest() 
  {
    return pExcludeLatest;
  }

  /** 
   * Get minimum number of archive volumes containing the checked-in version in order for 
   * it to be inclued in the returned list or <CODE>null</CODE> for any number of archives.
   */ 
  public Integer
  getMinArchives() 
  {
    return pMinArchives;
  }

  /** 
   * Whether to only include checked-in versions which can be offlined.
   */ 
  public boolean
  getUnusedOnly()
  {
    return pUnusedOnly;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1197626254420117310L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes to restore or <CODE>null</CODE> for all nodes.
   */ 
  private String  pPattern;

  /**
   * The number of newer checked-in versions of the node to exclude from the returned 
   * list or <CODE>null</CODE> to include all versions.
   */ 
  private Integer  pExcludeLatest;

  /** 
   * The minimum number of archive volumes containing the checked-in version in order for 
   * it to be inclued in the returned list or <CODE>null</CODE> for any number of archives.
   */ 
  private Integer  pMinArchives;

  /** 
   * Whether to only include checked-in versions which can be offlined.
   */ 
  private boolean  pUnusedOnly; 

}
  
