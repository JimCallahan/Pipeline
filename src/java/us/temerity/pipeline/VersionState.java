// $Id: VersionState.java,v 1.2 2004/04/14 20:59:33 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   V E R S I O N  S T A T E                                                               */
/*------------------------------------------------------------------------------------------*/

/**
 * The relationship between the revision numbers of working and checked-in versions of 
 * a node. <P> 
 * 
 * Each working version of a node maintains the revision number of the checked-in 
 * version it is based upon.  This working revision number can also be <CODE>null</CODE>
 * if the node only has an initial working version and has never been checked-in.  The
 * working revision number for a node is obtainable by calling the 
 * {@link NodeMod#getWorkingID NodeMod.getWorkingID} method. <P> 
 * 
 * This set of states represents the relationship between the working revision number 
 * and the revision number of the latest checked-in version.  This state is an important
 * foundation for determining most of the other node states.
 * 
 * @see PropertyState
 * @see LinkState 
 * @see FileState
 * @see OverallNodeState
 * @see NodeStatus
 */
public
enum VersionState
{  
  /**
   * A working version of the node exists, but is not based on a checked-in version 
   * since no checked-in versions exist yet.
   */
  Pending, 

  /**
   * No working version of the node exists, but one or more checked-in versions of the 
   * node do exist.
   */
  CheckedIn, 

  /**
   * The working version of the node exists and is based on the latest checked-in version.
   */
  Identical, 

  /**
   * The working version exists but is based on an older checked-in version than the
   * latest checked-in version.
   */
  NeedsCheckOut;
}
