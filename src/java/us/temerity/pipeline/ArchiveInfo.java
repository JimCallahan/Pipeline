// $Id: ArchiveInfo.java,v 1.1 2005/03/10 08:07:27 jim Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   I N F O                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the archival status of a checked-in version of a node.
 */
public
class ArchiveInfo
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
   * @param checkedIn 
   *   The timestamp of when the checked-in version was created.
   * 
   * @param archived
   *   The timestamp of when the checked-in version was last archived or 
   *  <CODE>null</CODE> if never archived.
   * 
   * @param numArchives
   *   The number of archive volumes which contain the checked-in version.
   */ 
  public
  ArchiveInfo
  (
   String name, 
   VersionID vid, 
   Date checkedIn, 
   Date archived, 
   int numArchives
  ) 
  {
    super(name, vid, archived, numArchives);

    if(checkedIn == null)
      throw new IllegalArgumentException("The checked-in timestamp cannot be (null)!");
    pCheckedInStamp = checkedIn;
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7909002625269581098L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when the checked-in version was created.
   */
  private Date  pCheckedInStamp;

}
