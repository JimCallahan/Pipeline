// $Id: FileState.java,v 1.1 2004/03/01 21:45:04 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S T A T E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The relationship between the individual files associated with the working and checked-in 
 * versions of a node. Each file in the primary and secondary file sequences associated
 * with the working version will have a <CODE>FileState</CODE>.
 * 
 * The <CODE>FileState</CODE> is computed within the context of a previously determined
 * {@link VersionState VersionState} for the node.  The following state descriptions will 
 * frequently refer to this <CODE>VersionState</CODE> context. 
 * 
 * @see VersionState
 * @see PropertyState
 * @see DependState 
 * @see OverallNodeState
 * @see NodeStatus
 */
public
enum FileState
{  
  /**
   * The only possible state if the <CODE>VersionState</CODE> is 
   * {@link VersionState#Pending Pending}, since there is no checked-in file to 
   * compare the working file against.
   */
  Pending, 

  /**
   * The only possible state if the <CODE>VersionState</CODE> is 
   * {@link VersionState#CheckedIn CheckedIn}, since there is no working file to 
   * compare the checked-in file against.
   */
  CheckedIn, 

  /**
   * The working file is identical to the latest checked-in file.  The 
   * <CODE>VersionState</CODE> may be either {@link VersionState#Identical Identical} or 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.  In the case of 
   * <CODE>NeedsCheckOut</CODE>, the latest checked-in version is newer than the one
   * the working version was based upon, however its associated file remains identical to
   * the file associated the working version.  In the case where the <CODE>VersionState</CODE>
   * is <CODE>NeedsCheckOut</CODE>, it is possible that this file may be part of a file 
   * sequence that was added or had its frame range expanded since the time that the working 
   * version was checked-out, yet it is still identical to the file associated with the 
   * latest checked-in version.
   */
  Identical, 

  /**
   * The working file and latest checked-in file associated with the node both exist and
   * are different from each other, yet these differences are entirely due to changes to the 
   * working file since the time it was checked-out.  The <CODE>VersionState</CODE> must 
   * be {@link VersionState#Identical Identical}.
   */
  Modified, 

  /** 
   * The working file exists and is part of a file sequence that was added or had its 
   * frame range expanded since the time that the working version was checked-out.  There
   * is no corresponding file associated with the checked-in version upon which the 
   * working version is based.  The <CODE>VersionState</CODE> must be 
   * {@link VersionState#Identical Identical}.
   */
  Added, 

  /**
   * The working file and latest checked-in file associated with the node both exist and
   * are different from each other, yet these differences are entirely due to the creation
   * of a new checked-in version since the time that the working version was checked-out.  
   * The working file remains identical to the checked-in in file upon which the working 
   * version is based. The <CODE>VersionState</CODE> must be 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.
   */
  NeedsCheckOut, 

  /**
   * The working file and latest checked-in file both exist and are different.  These 
   * differences are due to both changes to the working file and the creation of a new 
   * checked-in file since the time the working version was checked-out. The 
   * <CODE>VersionState</CODE> must be {@link VersionState#NeedsCheckOut NeedsCheckOut}.
   */
  Conflicted, 

  /**
   * No working file exists for this member of a file sequence associated with the 
   * working version of the node.  The <CODE>VersionState</CODE> cannot be
   * {@link VersionState#CheckedIn CheckedIn} since no working file should exists if 
   * the working version has not been checked-out.
   */
  Missing;
}
