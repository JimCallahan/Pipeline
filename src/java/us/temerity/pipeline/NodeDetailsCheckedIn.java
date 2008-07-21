// $Id: NodeDetailsCheckedIn.java,v 1.1 2008/07/21 17:31:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S   C H E C K E D - I N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of information about a specific checked-in version of a node. <P> 
 * 
 * When a node status operation is performed for a particular checked-in version, the 
 * information contained in this class is provided by plmaster(1).
 */
public
class NodeDetailsCheckedIn
  extends BaseNodeDetails
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given state information. <P> 
   * 
   * @param checkedIn
   *   The specific checked-in version of the node found by the node status operation.
   * 
   * @param latest    
   *   The latest checked-in version of the node.
   *
   * @param versionIDs
   *   The revision numbers of all checked-in versions.
   */
  public 
  NodeDetailsCheckedIn
  (
   NodeVersion checkedIn, 
   NodeVersion latest, 
   Collection<VersionID> versionIDs
  ) 
  {
    super(checkedIn, latest, versionIDs);

    if(getCheckedInVersion() == null) 
      throw new IllegalArgumentException("The checked-in version cannot be (null)!");

    if(getLatestVersion() == null) 
      throw new IllegalArgumentException("The latest version cannot be (null)!");
  }




  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the specific checked-in version of the node found by the node status operation.
   * 
   * @return
   *   The checked-in version.
   */ 
  public NodeVersion
  getCheckedInVersion()
  {
    return getVersion();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string representation of the primary file sequence.
   */ 
  public String
  toString()
  {
    return getCheckedInVersion().toString();
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
    if(getCheckedInVersion() != null) 
      encoder.encode("CheckedInVersion", getCheckedInVersion());

    super.toGlue(encoder);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    throw new GlueException("NodeDetailsCheckedIn does not support GLUE decoding!");
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5997022115283187738L;


}

