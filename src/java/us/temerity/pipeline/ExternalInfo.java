// $Id: ExternalInfo.java,v 1.1 2005/03/10 08:07:27 jim Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E X T E R N A L   I N F O                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Protected base class containing the common elements of the {@link ArchiveInfo ArchiveInfo}
 * and {@link OfflineInfo OfflineInfo} classes.
 */
public
class ExternalInfo
  extends Named
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
   * @param archived
   *   The timestamp of when the checked-in version was last archived or 
   *  <CODE>null</CODE> if never archived.
   * 
   * @param numArchives
   *   The number of archive volumes which contain the checked-in version.
   */ 
  protected 
  ExternalInfo
  (
   String name, 
   VersionID vid, 
   Date archived, 
   int numArchives
  ) 
  {
    super(name);

    if(vid == null) 
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;

    pArchivedStamp = archived;
    pNumArchives   = numArchives;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the revision number of the checked-in version of the node.
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
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
   * Get the number of archive volumes which contain the checked-in version.
   */ 
  public int
  numArchives() 
  {
    return pNumArchives;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4237837613678983887L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the checked-in version. 
   */ 
  private VersionID  pVersionID;       

  /**
   * The timestamp of when the checked-in version was last archived or 
   * <CODE>null</CODE> if never archived.
   */
  private Date  pArchivedStamp;

  /**
   * The number of archive volumes which contain the checked-in version.
   */
  private int  pNumArchives;

}
