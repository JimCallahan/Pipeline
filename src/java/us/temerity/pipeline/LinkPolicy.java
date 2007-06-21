// $Id: LinkPolicy.java,v 1.9 2007/06/21 16:40:50 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   P O L I C Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The policy concerning how the {@link OverallNodeState OverallNodeState} and 
 * {@link OverallQueueState OverallQueueState} of the source node are considered when 
 * computing the state of the target node.
 *
 * @see LinkMod
 * @see LinkVersion
 */
public
enum LinkPolicy
{  
  /**
   * This kind of link represents a indirect relationship between nodes which does not
   * affect node status. <P> 
   * 
   * An Association is similar to a Reference, except that changes to the source node do 
   * not affect the the target node's <CODE>OverallQueueState</CODE> in any way.  They can 
   * be used in place of Reference links in cases where propogation of staleness from the 
   * source to targe node is not desirable. 
   */ 
  Association, 
  
  /**
   * This kind of link represents an indirect relationship between the target node on the 
   * source node. <P> 
   *
   * The link has no affect on the target node's <CODE>OverallQueueState</CODE>, but can 
   * affect the target node's <CODE>OverallNodeState</CODE>. Changes to the source node will 
   * not cause the target node to become <CODE>Stale</CODE>, but may cause it to become
   * <CODE>ModifiedLinks</CODE>. <P> 
   *
   * References are often to describe externally referenced assets by nodes.  For example, 
   * a rendering scene file might reference textures that it assigns to objects.  A change 
   * to the texture doesn't actually change the scene file but the images produced by 
   * rendering the scene file would be different.  A change to the texture would not cause
   * the scene file to become <CODE>Stale</CODE>, but nodes downstream of the scene file
   * which depend on the scene would become <CODE>Stale</CODE>.  In essence, a 
   * <CODE>Reference</CODE> link communicates staleness downstream without causing the 
   * target node to become <CODE>Stale</CODE> itself.  Changes to the source node will 
   * cause the target node to become <CODE>ModifiedLinks<CODE> since the target node will
   * need to record the correct version of the target node used when it is checked-in. <P>
   */
  Reference, 

  /**
   * This kind of link represents the standard concept of a dependency. <P> 
   * 
   * Changes to the source node will affect both the <CODE>OverallNodeState</CODE> and
   * <CODE>OverallQueueState</CODE> of the target node.
   */
  Dependency;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible values.
   */ 
  public static ArrayList<LinkPolicy>
  all() 
  {
    LinkPolicy values[] = values();
    ArrayList<LinkPolicy> all = new ArrayList<LinkPolicy>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(LinkPolicy policy : LinkPolicy.all()) 
      titles.add(policy.toTitle());
    return titles;
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
    return toString();
  }

}
