// $Id: OfflineInfo.java,v 1.2 2005/03/14 16:08:21 jim Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   O F F L I N E   I N F O                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the offline status of a checked-in version of a node.
 */
public
class OfflineInfo
  extends ExternalInfo
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new instance.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param vid
   *   The checked-in revision number.
   * 
   * @param checkedOut
   *   The timestamp of the latest check-out of the version or 
   *  <CODE>null</CODE> if no working versions exist.
   * 
   * @param author
   *   The name of the owner of the working latest checked-out working version 
   *   or <CODE>null</CODE> if no working versions exist.
   * 
   * @param view
   *   The name of the working area view where the latest check-out of the version 
   *   occurred or <CODE>null</CODE> if no checked-out versions exist.
   * 
   * @param numWorking
   *   The number of existing working versions based on the checked-in version.
   * 
   * @param archived
   *   The timestamp of when the checked-in version was last archived or 
   *  <CODE>null</CODE> if never archived.
   * 
   * @param numArchives
   *   The number of archive volumes which contain the checked-in version.
   * 
   * @param canOffline
   *   Whether this checked-in version can be offlined.
   */ 
  public
  OfflineInfo
  (
   String name, 
   VersionID vid, 
   Date checkedOut, 
   String author, 
   String view, 
   int numWorking, 
   Date archived,  
   int numArchives, 
   boolean canOffline 
  ) 
  {
    super(name, vid, archived, numArchives);

    pCheckedOutStamp = checkedOut;
    pAuthor          = author; 
    pView            = view; 
    pNumWorking      = numWorking;
    pCanOffline      = canOffline; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Gets the timestamp of the latest check-out of the version or 
   * <CODE>null</CODE> if no working versions exist.
   */ 
  public Date  
  getCheckedOutStamp() 
  {
    return pCheckedOutStamp;
  }
 
  /** 
   * Get the name of the owner of the working latest checked-out working version 
   * or <CODE>null</CODE> if no checked-out versions exist.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /** 
   * Get the name of the working area view where the latest check-out of the version 
   * occurred or <CODE>null</CODE> if no checked-out versions exist.
   */
  public String
  getView()
  {
    return pView;
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

  private static final long serialVersionUID = -8012946139167153388L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of the latest check-out of the version or 
   * <CODE>null</CODE> if never archived.
   */
  private Date  pCheckedOutStamp;

  /** 
   * The name of the owner of the working latest checked-out working version 
   * or <CODE>null</CODE> if no checked-out versions exist.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view where the latest check-out of the version 
   * occurred or <CODE>null</CODE> if no checked-out versions exist.
   */
  private String  pView;

  /**
   * The number of existing working versions based on the checked-in version.
   */
  private int  pNumWorking;

  /**
   * Whether this checked-in version can be offlined.
   */ 
  private boolean  pCanOffline;

}
