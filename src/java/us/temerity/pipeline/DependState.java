// $Id: DependState.java,v 1.1 2004/03/01 21:45:04 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   D E P E N D   S T A T E                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A comparison of the dependency information (upstream node connections) of a working 
 * version and the latest checked-in version of a node. <P> 
 * 
 * A node version maintains the names of the upstream nodes which it depends upon. For each
 * dependency, the version also stores whether each file associated with the target node 
 * depends on exactly one file associated with the source node or all of the files.  If the 
 * file dependency relationship is 1-to-1, a file index offset is also stored. <P> 
 * 
 * For the purposes of this state, two versions of a node will be considered idential only
 * if they have exactly the same set of dependency names and all of the per-dependency 
 * information is also identical. <P> 
 * 
 * The <CODE>DependState</CODE> is computed within the context of a previously determined
 * {@link VersionState VersionState} for the node.  The following state descriptions will 
 * frequently refer to this <CODE>VersionState</CODE> context. 
 * 
 * @see VersionState
 * @see PropertyState 
 * @see FileState
 * @see OverallNodeState
 * @see NodeStatus
 */
public
enum DependState
{  
  /**
   * The only possible state if the <CODE>VersionState</CODE> is 
   * {@link VersionState#Pending Pending}, since there are no checked-in versions to 
   * compare the working version dependencies against.
   */
  Pending, 

  /**
   * The only possible state if the <CODE>VersionState</CODE> is 
   * {@link VersionState#CheckedIn CheckedIn}, since there is no working version to 
   * compare the checked-in version dependencies against.
   */
  CheckedIn, 

  /**
   * The working version dependencies are identical to the dependencies of the latest 
   * checked-in version.  The <CODE>VersionState</CODE> may be either 
   * {@link VersionState#Identical Identical} or 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.  In the case of 
   * <CODE>NeedsCheckOut</CODE>, the latest checked-in version is newer than the one
   * the working version was based upon, however its dependencies are still identical
   * to those of the working version. In the case where the <CODE>VersionState</CODE> is
   * <CODE>NeedsCheckOut</CODE>, it is possible that the working version's dependencies
   * may have been changed since the time it was checked-out, but these dependency
   * changes have resulted in the working version having identical dependencies as the 
   * latest checked-in version. <P> 
   * 
   * In addition to the conditions above, all upstream nodes connected either directly 
   * or indirectly to this node also have a <CODE>DependState</CODE> of <CODE>Identical<CODE>.
   */
  Identical, 

  /**
   * The working version and latest checked-in version of the node have different 
   * dependencies, yet these differences are entirely due to changes to the working version 
   * since the time it was checked-out.  The <CODE>VersionState</CODE> must be 
   * {@link VersionState#Identical Identical}. <P> 
   * 
   * Alternatively, the working version and latest checked-in version have identical 
   * dependencies but one or more of the upstream nodes connected either directly or 
   * indirectly to this node have a <CODE>DependState</CODE> other than <CODE>Identical<CODE>.
   */
  Modified, 

  /**
   * The working version and latest checked-in version of the node have different 
   * dependencies, yet these differences are entirely due to the creation of a new checked-in
   * version of the node since the time the working version was checked-out.  The dependencies
   * of the working version remain identical to those of the checked-in version upon 
   * which it was based.  The <CODE>VersionState</CODE> must be 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.
   */
  NeedsCheckOut, 

  /**
   * The working version and latest checked-in version of the node have different 
   * dependencies. These differences are due to both changes in the working version and the 
   * creation of a new checked-in version since the time the working verison was 
   * checked-out. The <CODE>VersionState</CODE> must be 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.
   */
  Conflicted;
}
