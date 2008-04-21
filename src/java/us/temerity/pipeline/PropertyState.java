// $Id: PropertyState.java,v 1.7 2008/04/21 06:15:10 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O P E R T Y   S T A T E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The relationship between the values of the node properties associated with the working 
 * and checked-in versions of a node. <P> 
 * 
 * The node properties includes: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   The file patterns and frame ranges of primary and secondary file sequences. <BR>
 *   The toolset environment under which editors and actions are run. <BR>
 *   The name of the editor plugin used to edit the data files associated with the node. <BR>
 *   The regeneration action and its single and per-dependency parameters. <BR>
 *   The job requirements. <BR>
 *   The IgnoreOverflow and IsSerial flags. <BR>
 *   The job batch size. <BR> 
 * </DIV> <BR> 
 * 
 * This state considers all of these properties together.  If there are differences between
 * any of the individual working and checked-in properties, this state will consider the 
 * node properties to be different as a whole. <P> 
 * 
 * The <CODE>PropertyState</CODE> is computed within the context of a previously determined
 * {@link VersionState VersionState} for the node.  The following state descriptions will 
 * frequently refer to this <CODE>VersionState</CODE> context. 
 * 
 * @see VersionState
 * @see LinkState 
 * @see FileState
 * @see OverallNodeState
 */
public
enum PropertyState
{  
  /**
   * The only possible state if the <CODE>VersionState</CODE> is 
   * {@link VersionState#Pending Pending}, since there are no checked-in versions to 
   * compare the working version node properties against.
   */
  Pending, 

  /**
   * The only possible state if the <CODE>VersionState</CODE> is 
   * {@link VersionState#CheckedIn CheckedIn}, since there is no working version to 
   * compare the checked-in version node properties against.
   */
  CheckedIn, 

  /**
   * The working version node properties are identical to the node properties of the latest 
   * checked-in version.  The <CODE>VersionState</CODE> may be either 
   * {@link VersionState#Identical Identical} or 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.  In the case of 
   * <CODE>NeedsCheckOut</CODE>, the latest checked-in version is newer than the one
   * the working version was based upon, however its node properties are still identical to 
   * those of the working version.
   */
  Identical, 

  /**
   * The working version and latest checked-in version of the node have different node
   * properties, yet these differences are entirely due to changes to the working version 
   * since the time it was checked-out.  However, the changes are limited to properties 
   * which do not directly affect how the contents of files associated with the node are 
   * generated.  For example, changing the Editor plugin property.  The 
   * <CODE>VersionState</CODE> must be {@link VersionState#Identical Identical}.
   */
  TrivialMod, 

  /**
   * The working version and latest checked-in version of the node have different node
   * properties, yet these differences are entirely due to changes to the working version 
   * since the time it was checked-out.  The changes include properties which do affect the 
   * how the contents of files associated with the node are generated.  For example, changing
   * a parameter of an enabled Action plugin. The <CODE>VersionState</CODE> must be 
   * {@link VersionState#Identical Identical}.
   */
  Modified, 

  /**
   * The working version and latest checked-in version of the node have different node
   * properties, yet these differences are entirely due to the creation of a new checked-in
   * version of the node since the time the working version was checked-out.  The node 
   * properties of the working version remain identical to the checked-in version upon 
   * which it was based.  The <CODE>VersionState</CODE> must be 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.
   */
  NeedsCheckOut, 

  /**
   * The working version and latest checked-in version of the node have different node
   * properties. These differences are due to both changes in the working version and the 
   * creation of a new checked-in version since the time the working version was 
   * checked-out. The <CODE>VersionState</CODE> must be 
   * {@link VersionState#NeedsCheckOut NeedsCheckOut}.
   */
  Conflicted;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<PropertyState>
  all() 
  {
    PropertyState values[] = values();
    ArrayList<PropertyState> all = new ArrayList<PropertyState>(values.length);
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
    "Trivial Mod", 
    "Modified", 
    "Needs Check-Out", 
    "Conflicted"
  };
}
