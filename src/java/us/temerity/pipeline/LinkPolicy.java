// $Id: LinkPolicy.java,v 1.1 2004/03/13 17:20:13 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   P O L I C Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The policy concerning whether the {@link OverallNodeState OverallNodeState} and 
 * {@link OverallQueueState OverallQueueState} of the source node are considered when 
 * computing the {@link NodeStatus NodeStatus} of the target node.
 *
 * @see LinkMod
 * @see LinkVersion
 */
public
enum LinkPolicy
{  
  /** 
   * Neither the <CODE>OverallNodeState</CODE> or the <CODE>OverallQueueState</CODE> of 
   * the source node are considered when computing the <CODE>NodeStatus</CODE> of the target 
   * node.
   */
  None,

  /**
   * Only the <CODE>OverallNodeState</CODE> is considered when computing the 
   * <CODE>NodeStatus</CODE> of the target node.
   */
  NodeStateOnly, 

  /**
   * Both the <CODE>OverallNodeState</CODE> and <CODE>OverallQueueState</CODE> of the 
   * source node are considered when computing the <CODE>NodeStatus</CODE> of the target 
   * node.
   */
  Both;
}
