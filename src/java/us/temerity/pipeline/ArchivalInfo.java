// $Id: ArchivalInfo.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V A L   I N F O                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the archival status of a checked-in version of a node.
 */
public
class ArchivalInfo
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new instance.
   * 
   * @param checkedIn 
   *   The timestamp of when the checked-in version was created.
   * 
   * @param checkedOut
   *   The timestamp of the latest check-out of the version or 
   *  <CODE>null</CODE> if no checked-out versions exist.
   * 
   * @param archived
   *   The timestamp of when the checked-in version was last archived or 
   *  <CODE>null</CODE> if never archived.
   * 
   * @param numWorking
   *   The number of existing working versions based on the checked-in version.
   * 
   * @param numArchives
   *   The number of archives which contain the checked-in version.
   * 
   * @param canOffline
   *   Whether this checked-in version can be offlined.
   */ 
  public
  ArchivalInfo
  (
   Date checkedIn, 
   Date checkedOut, 
   Date archived, 
   int numWorking, 
   int numArchives, 
   boolean canOffline 
  ) 
  {
    if(checkedIn == null)
      throw new IllegalArgumentException("The checked-in timestamp cannot be (null)!");
    pCheckedInStamp = checkedIn;

    pCheckedOutStamp = checkedOut;
    pArchivedStamp   = archived;
    pNumWorking      = numWorking;
    pNumArchives     = numArchives;
    pCanOffline      = canOffline; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the timestamp of when the checked-in version was created.
   */ 
  public Date  
  getCheckedInStamp() 
  {
    return pCheckedInStamp;
  }
 
  /**
   * Gets the timestamp of the latest check-out of the version or 
   * <CODE>null</CODE> if no checked-out versions exist.
   */ 
  public Date  
  getCheckedOutStamp() 
  {
    return pCheckedOutStamp;
  }
 
  /**
   * Gets the timestamp of when the checked-in version was last archived or 
   * <CODE>null</CODE> if never archived.
   */ 
  public Date  
  getArchivedStamp() 
  {
    return pArchivedStamp;
  }
 
  /**
   * Get the number of existing working versions based on the checked-in version.
   */ 
  public int
  numWorking() 
  {
    return pNumWorking;
  }

  /**
   * Get the number of archives which contain the checked-in version.
   */ 
  public int
  numArchives() 
  {
    return pNumArchives;
  }

  /**
   * Whether this checked-in version can be offlined.
   */ 
  public boolean
  canOffline()
  {
    return pCanOffline; 
  }
 



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8938314884704803551L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when the checked-in version was created.
   */
  private Date  pCheckedInStamp;

  /**
   * The timestamp of the latest check-out of the version or 
   * <CODE>null</CODE> if never archived.
   */
  private Date  pCheckedOutStamp;

  /**
   * The timestamp of when the checked-in version was last archived or 
   * <CODE>null</CODE> if never archived.
   */
  private Date  pArchivedStamp;

  /**
   * The number of existing working versions based on the checked-in version.
   */
  private int  pNumWorking;

  /**
   * The number of archives which contain the checked-in version.
   */
  private int  pNumArchives;

  /**
   * Whether this checked-in version can be offlined.
   */ 
  private boolean  pCanOffline;


}
