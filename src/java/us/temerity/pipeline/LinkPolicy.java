// $Id: LinkPolicy.java,v 1.4 2004/07/14 20:55:43 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   P O L I C Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The policy concerning whether the {@link OverallNodeState OverallNodeState} and 
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
   * Neither the <CODE>OverallNodeState</CODE> or the <CODE>OverallQueueState</CODE> of 
   * the source node are considered when computing the state of the target 
   * node.
   */
  None,

  /**
   * Only the <CODE>OverallNodeState</CODE> is considered when computing the 
   * state of the target node.
   */
  NodeStateOnly, 

  /**
   * Both the <CODE>OverallNodeState</CODE> and the per-file <CODE>QueueState</CODE> of the 
   * source node are considered when computing the state of the target node.
   */
  Both;



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
    return sTitles[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "None", 
    "Node Only", 
    "Node & Queue"
  };
}
