// $Id: LinkRelationship.java,v 1.1 2004/03/13 17:20:13 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   R E L A T I O N S H I P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The nature of the relationship between files associated with the source and target  
 * nodes of a node link. <P> 
 *
 * @see LinkMod
 * @see LinkVersion
 */
public
enum LinkRelationship
{  
  /** 
   * There are no relationships between files associated with source and target nodes.
   */
  None,

  /**
   * There is a one-to-one relationship between files associated with the target and 
   * source nodes. <P> 
   * 
   * In other words, changes to an individual file associated with the source node 
   * only has the potential to affect a single file associated with the target node.
   */
  OneToOne,

  /**
   * There is a all-to-all relationship between files associated with the target and 
   * source nodes. <P> 
   * 
   * In other words, changes to any of the files associated with the source node has
   * the potential to affect all of the files associated with the target node.
   */
  All;
}
