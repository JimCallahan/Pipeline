// $Id: OverallNodeState.java,v 1.5 2004/04/17 19:49:01 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   O V E R A L L   N O D E   S T A T E                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A single state computed from the combination of {@link VersionState VersionState}, 
 * {@link PropertyState PropertyState}, {@link LinkState LinkState} and the individual
 * {@link FileState FileState} of each file associated with the node.
 *
 * @see VersionState
 * @see PropertyState
 * @see LinkState 
 * @see FileState
 */
public
enum OverallNodeState
{  
  /**
   * A working version of the node exists, but is not based on a checked-in version 
   * since no checked-in versions exist yet. <P> 
   * 
   * The <CODE>VersionState</CODE> and therefore the <CODE>PropertyState</CODE>, 
   * <CODE>LinkState</CODE> and individual <CODE>FileState</CODE> of each file associated 
   * with the node are <CODE>Pending</CODE>. <P> 
   */
  Pending, 

  /**
   * No working version of the node exists, but one or more checked-in versions of the 
   * node do exist. <P> 
   * 
   * The <CODE>VersionState</CODE> and therefore the <CODE>PropertyState</CODE>, 
   * <CODE>LinkState</CODE> and individual <CODE>FileState</CODE> of each file associated 
   * with the node are <CODE>CheckedIn</CODE>.
   */
  CheckedIn, 

  /**
   * The working version of the node exists, is based on the latest checked-in version 
   * and is identical to that version. <P>
   * 
   * The <CODE>VersionState</CODE>, <CODE>PropertyState</CODE>, <CODE>LinkState</CODE> 
   * and individual <CODE>FileState</CODE> of each file associated with the node are all 
   * <CODE>Identical</CODE>. <P> 
   * 
   * In addition, none of the linked upstream nodes have an <CODE>OverallNodeState</CODE>
   * equal to <CODE>Modified</CODE>, <CODE>ModifiedLinks</CODE> or <CODE>Conflicted</CODE>.
   * Furthermore, the working revision numbers of the linked upstream nodes are identical to
   * the revision numbers of the linked upstream nodes of the checked-in version upon 
   * which this working version is based.
   */
  Identical, 

  /**
   * The working version of the node exists, is based on the latest checked-in version 
   * and is identical to that version. <P>
   * 
   * The <CODE>VersionState</CODE>, <CODE>PropertyState</CODE>, <CODE>LinkState</CODE> 
   * and individual <CODE>FileState</CODE> of each file associated with the node are all 
   * <CODE>Identical</CODE>. However, one or more of the linked upstream nodes have 
   * an <CODE>OverallNodeState</CODE> equal to <CODE>Modified</CODE>, 
   * <CODE>ModifiedLinks</CODE> or <CODE>Conflicted</CODE>. <P> 
   * 
   * Alternatively, the working revision numbers of the upstream nodes are different than 
   * the revision numbers of the upstream nodes associated with the checked-in version upon 
   * which this working version is based.
   */
  ModifiedLinks, 

  /**
   * The working version is based on the lastest checked-in version, but is node identical
   * to that version. <P> 
   * 
   * The <CODE>VersionState</CODE> is <CODE>Identical</CODE>. One or more of the 
   * <CODE>PropertyState</CODE>, <CODE>LinkState</CODE> or individual <CODE>FileState</CODE>
   * of each file associated with the node are <CODE>Modified</CODE> or <CODE>Added</CODE>. 
   * Also, none of these states are <CODE>NeedsCheckOut</CODE> or <CODE>Conflicted</CODE>.
   */
  Modified,

  /**
   * The working version exists but is based on an older checked-in version than the
   * latest checked-in version. <P> 
   *
   * The <CODE>VersionState</CODE> is <CODE>NeedsCheckOut</CODE>. One or more of the 
   * <CODE>PropertyState</CODE>, <CODE>LinkState</CODE> or individual <CODE>FileState</CODE>
   * of each file associated with the node are <CODE>NeedsCheckOut</CODE>. Also, none of 
   * these states are <CODE>Added</CODE>, <CODE>Modified</CODE> or <CODE>Conflicted</CODE>.
   */
  NeedsCheckOut,

  /**
   * The working version and latest checked-in version of the node are different and
   * these differences are due to both changes in the working version and the 
   * creation of a new checked-in version since the time the working version was 
   * checked-out. <P> 
   * 
   * The <CODE>VersionState</CODE> is <CODE>NeedsCheckOut</CODE>.  One or more of the 
   * <CODE>PropertyState</CODE>, <CODE>LinkState</CODE> or individual <CODE>FileState</CODE>
   * of each file associated with the node are <CODE>Conflicted</CODE>. Alternatively, some 
   * of the states are <CODE>Modified</CODE> or <CODE>Added</CODE> at the same time that
   * some of them are <CODE>NeedsCheckOut</CODE>.
   */
  Conflicted;
}
