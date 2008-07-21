// $Id: NodeDetailsLight.java,v 1.1 2008/07/21 17:31:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S   L I G H T                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A lightweight collection of node state information respect to a particular working 
 * area view. <P> 
 * 
 * When a lightweight node status operation is performed, the information contained in 
 * this class is provided by plmaster(1).
 */
public
class NodeDetailsLight
  extends BaseNodeDetails
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given state information. <P> 
   * 
   * @param work
   *   The working version of the node or <CODE>null</CODE> if the node has not been 
   *   checked-out. 
   * 
   * @param base
   *   The checked-in version of the node upon which the working version was based or 
   *   <CODE>null</CODE> if this is an initial working version or if the node has not 
   *   been checked-out. 
   * 
   * @param latest    
   *   The latest checked-in version of the node or <CODE>null</CODE> if this is an 
   *   initial working version which has never been checked-in. 
   *
   * @param versionIDs
   *   The revision numbers of all checked-in versions or <CODE>null</CODE> if no 
   *   checked-in versions exist.
   * 
   * @param versionState
   *   The version state of the node.
   * 
   * @param propertyState  
   *   The state of the node properties.
   * 
   * @param linkState 
   *   The state of the upstream node links.
   */
  public 
  NodeDetailsLight
  (
   NodeMod work, 
   NodeVersion base, 
   NodeVersion latest, 
   Collection<VersionID> versionIDs, 
   VersionState versionState, 
   PropertyState propertyState, 
   LinkState linkState
  ) 
  {
    super(base, latest, versionIDs);

    pWorkingVersion = work;   

    if(versionState == null) 
      throw new IllegalArgumentException("The version state cannot be (null)!");
    pVersionState = versionState;

    if(propertyState == null) 
      throw new IllegalArgumentException("The property state cannot be (null)!");
    pPropertyState = propertyState;

    if(linkState == null) 
      throw new IllegalArgumentException("The link state cannot be (null)!");
    pLinkState = linkState;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the working version of the node.
   * 
   * @return
   *   The working version or <CODE>null</CODE> if none exists.
   */ 
  public NodeMod
  getWorkingVersion()
  {
    return pWorkingVersion;
  }

  /**
   * Get the checked-in version of the node upon which the working version was based.
   * 
   * @return
   *   The base version or <CODE>null</CODE> if none exists.
   */ 
  public NodeVersion
  getBaseVersion()
  {
    return getVersion(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the version state of the node.
   */ 
  public VersionState
  getVersionState() 
  {
    return pVersionState;
  }

  /**
   * Get the state of the node properties.
   */ 
  public PropertyState
  getPropertyState() 
  {
    return pPropertyState;
  }

  /**
   * Get the state of the upstream node links.
   */ 
  public LinkState
  getLinkState() 
  {
    return pLinkState;
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
    if(pWorkingVersion != null)
      return pWorkingVersion.toString();
    return getLatestVersion().toString();
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
    if(pWorkingVersion != null) 
      encoder.encode("WorkingVersion", pWorkingVersion);

    if(getBaseVersion() != null) 
      encoder.encode("BaseVersion", getBaseVersion());

    super.toGlue(encoder);

    encoder.encode("VersionState", pVersionState);
    encoder.encode("PropertyState", pPropertyState);
    encoder.encode("LinkState", pLinkState);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    throw new GlueException("NodeDetailsLight does not support GLUE decoding!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8370560801429546733L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The working version of the node.
   */ 
  private NodeMod  pWorkingVersion;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The relationship between the revision numbers of working and checked-in versions of 
   * a node. 
   */ 
  private VersionState  pVersionState;

  /** 
   * The relationship between the values of the node properties associated with the working 
   * and checked-in versions of a node. 
   */   
  private PropertyState  pPropertyState;

  /** 
   * A comparison of the upstream node link information associated with a working version 
   * and the latest checked-in version of a node. <P> 
   */   
  private LinkState  pLinkState;
  
}

