// $Id: LinkState.java,v 1.8 2008/05/16 01:11:40 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   S T A T E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A comparison of the upstream node link information associated with a working version 
 * and the latest checked-in version of a node. <P> 
 * 
 * A node version maintains a set of information about the upstream nodes with which 
 * is linked in form of {@link LinkMod LinkMod} and {@link LinkVersion LinkVersion} instances.
 * For the purposes of this state, two versions of a node will be considered idential only
 * if they are linked to exactly the same set of upstream nodes and the information associated
 * with these links are identical. <P> 
 * 
 * The <CODE>LinkState</CODE> is computed within the context of a previously determined
 * {@link VersionState VersionState} for the node.  The following state descriptions will 
 * frequently refer to this <CODE>VersionState</CODE> context. 
 * 
 * @see VersionState
 * @see PropertyState 
 * @see FileState
 * @see OverallNodeState
 */
public
enum LinkState
{  
  /**
   * The only possible state if the <CODE>VersionState</CODE> is 
   * {@link VersionState#Pending Pending}, since there are no checked-in versions to 
   * compare the working version upstream node links against.
   */
  Pending, 

  /**
   * The only possible state if the <CODE>VersionState</CODE> is 
   * {@link VersionState#CheckedIn CheckedIn}, since there is no working version to 
   * compare the checked-in version upstream node links against.
   */
  CheckedIn, 

  /**
   * The working version upstream node links are identical to those of the latest 
   * checked-in version.  The <CODE>VersionState</CODE> may be either 
   * {@link VersionState#Identical Identical} or 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.  In the case of 
   * <CODE>NeedsCheckOut</CODE>, the latest checked-in version is newer than the one
   * the working version was based upon, however its upstream node links are still identical
   * to those of the working version. In the case where the <CODE>VersionState</CODE> is
   * <CODE>NeedsCheckOut</CODE>, it is possible that the working version's upstream node links
   * may have been changed since the time it was checked-out, but these link related
   * changes have resulted in the working version having identical upstream node links as the 
   * latest checked-in version. 
   */
  Identical, 

  /**
   * The working version and latest checked-in version of the node have different 
   * upstream node links, yet these differences are entirely due to changes to the working 
   * version since the time it was checked-out.  The <CODE>VersionState</CODE> must be 
   * {@link VersionState#Identical Identical}. 
   */
  Modified, 

  /**
   * The working version and latest checked-in version of the node have different 
   * upstream node links, yet these differences are entirely due to the creation of a new 
   * checked-in version of the node since the time the working version was checked-out.  
   * The upstream node links of the working version remain identical to those of the 
   * checked-in version upon which it was based.  The <CODE>VersionState</CODE> must be 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.
   */
  NeedsCheckOut, 

  /**
   * The working version and latest checked-in version of the node have different 
   * upstream node links. These link related differences are due to both changes in the 
   * working version and the creation of a new checked-in version since the time the 
   * working verison was checked-out. The <CODE>VersionState</CODE> must be 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.
   */
  Conflicted;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<LinkState>
  all() 
  {
    LinkState values[] = values();
    ArrayList<LinkState> all = new ArrayList<LinkState>(values.length);
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "Pending", 
    "Checked-In", 
    "Identical",   
    "Modified", 
    "Needs Check-Out", 
    "Conflicted"
  };
}
