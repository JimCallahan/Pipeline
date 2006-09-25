// $Id: OverallNodeState.java,v 1.14 2006/09/25 11:42:00 jim Exp $

package us.temerity.pipeline;

import java.util.*;

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
   * with the node are also <CODE>Pending</CODE>. <P> 
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
   * <CODE>Identical</CODE>.  However, the IsLocked property of one or more of the upstream
   * working versions is different than the IsLocked property of the base checked-in link to 
   * the node. This means that one of the nodes upstream has been Locked or Unlocked since
   * the current node was checked-out. <P> 
   * 
   * Alternatively, one or more of the upstream nodes has an <CODE>OverallNodeState</CODE> 
   * state of <CODE>Modified Locks</CODE>. <P> 
   * 
   * This state has lower precendence than <CODE>Modified</CODE> or <CODE>ModifiedLinks</CODE>.
   */
  ModifiedLocks, 

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
   * The working version is based on the latest checked-in version, but is not identical
   * to that version. <P> 
   * 
   * The <CODE>VersionState</CODE> is <CODE>Identical</CODE>. One or more of the 
   * <CODE>PropertyState</CODE>, <CODE>LinkState</CODE> or individual <CODE>FileState</CODE>
   * of each file associated with the node are <CODE>Modified</CODE> or <CODE>Added</CODE>. 
   * Also, none of these states are <CODE>NeedsCheckOut</CODE>, <CODE>Obsolete</CODE> or 
   * <CODE>Conflicted</CODE>.
   */
  Modified,

  /**
   * The working version exists but is based on an older checked-in version than the
   * latest checked-in version. <P> 
   *
   * The <CODE>VersionState</CODE> is <CODE>NeedsCheckOut</CODE>. One or more of the 
   * <CODE>PropertyState</CODE>,  <CODE>LinkState</CODE> or individual <CODE>FileState</CODE>
   * of each file associated with the node are <CODE>NeedsCheckOut</CODE> or 
   * <CODE>Obsolete</CODE>. Also, none of these states are <CODE>Added</CODE>, 
   * <CODE>Modified</CODE> or <CODE>Conflicted</CODE>.
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
   * some of them are <CODE>NeedsCheckOut</CODE> or <CODE>Obsolete</CODE>.
   */
  Conflicted, 

  /**
   * A working version exists, but one or more of the associated files are missing from 
   * the working area. <P> 
   * 
   * This implies that one or more of the individual <CODE>FileState</CODE> of each file 
   * associated with the node are <CODE>Missing</CODE>. <P> 
   * 
   * This state has precedence over all other states except <CODE>MissingNewer</CODE> and 
   * <CODE>CheckedIn</CODE>.  The <CODE>CheckedIn</CODE> state can never occur at the same 
   * time as <CODE>Missing</CODE> since there are no working files to check for existence.
   */
  Missing, 

  /**
   * Identical to the Missing state except that the working version is based on an older 
   * checked-in version than the latest checked-in version. <P> 
   * 
   * This state precedence over all other states except <CODE>CheckedIn</CODE>.
   */
  MissingNewer;

  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<OverallNodeState>
  all() 
  {
    OverallNodeState values[] = values();
    ArrayList<OverallNodeState> all = new ArrayList<OverallNodeState>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return sTitles[ordinal()];
  }

  /**
   * Convert to a one-character symbolic representation.
   */ 
  public String
  toSymbol() 
  {
    return sSymbols[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "Pending", 
    "Checked-In", 
    "Identical", 
    "Modified Locks",
    "Modified Links",
    "Modified", 
    "Needs Check-Out", 
    "Conflicted", 
    "Missing", 
    "Missing Newer"
  };

  private static String sSymbols[] = {
    "P", 
    "I", 
    "=", 
    "K",
    "L",
    "M", 
    "O",
    "C",
    "?",
    "X"
  };
}
