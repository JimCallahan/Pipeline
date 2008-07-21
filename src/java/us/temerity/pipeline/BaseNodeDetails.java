// $Id: BaseNodeDetails.java,v 1.1 2008/07/21 17:31:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   N O D E   D E T A I L S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A common base class for all node details implementations classes. 
 */
class BaseNodeDetails
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given state information. <P> 
   * 
   * @param vsn
   *   The checked-in version or <CODE>null</CODE> if there are no checked-in versions.
   * 
   * @param latest    
   *   The latest checked-in version of the node or <CODE>null</CODE> if there are no 
   *   checked-in versions.
   *
   * @param versionIDs
   *   The revision numbers of all checked-in versions.  Can be <CODE>null</CODE> or empty if
   *   there are no checked-in versions.
   */
  public 
  BaseNodeDetails
  (
   NodeVersion vsn, 
   NodeVersion latest, 
   Collection<VersionID> versionIDs
  ) 
  {
    pVersion       = vsn;
    pLatestVersion = latest;    

    if(versionIDs != null) 
      pVersionIDs = new ArrayList<VersionID>(versionIDs);
    else 
      pVersionIDs = new ArrayList<VersionID>();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the checked-in version.
   * 
   * @return
   *   The checked-in version. 
   */ 
  protected NodeVersion
  getVersion()
  {
    return pVersion;
  }

  /**
   * Get the latest checked-in version of the node.
   * 
   * @return
   *   The latest version.
   */ 
  public NodeVersion
  getLatestVersion()
  {
    return pLatestVersion;
  }

  /**
   * Get the revision numbers of all checked-in versions.
   */ 
  public ArrayList<VersionID> 
  getVersionIDs() 
  {
    return new ArrayList<VersionID>(pVersionIDs);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    if(pLatestVersion != null) 
      encoder.encode("LatestVersion", pLatestVersion);

    if(!pVersionIDs.isEmpty()) 
      encoder.encode("VersionIDs", pVersionIDs);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    throw new GlueException("BaseNodeDetails does not support GLUE decoding!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8370560801429546733L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A specific checked-in version.
   */
  private NodeVersion  pVersion;
  
  /**
   * The latest checked-in version of the node.
   */
  private NodeVersion  pLatestVersion;

  /**
   * The revision numbers of all checked-in versions.
   */ 
  private ArrayList<VersionID>  pVersionIDs;


}

