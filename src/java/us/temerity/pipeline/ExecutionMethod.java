// $Id: ExecutionMethod.java,v 1.1 2004/03/13 17:20:13 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   E X E C U T I O N   M E T H O D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The methodology for regenerating the files associated with nodes with regeneration
 * actions. <P> 
 * 
 * Note that there is no difference between the following execution policies if the
 * primary file sequence for the node only contains a single file.
 *
 * @see NodeMod
 * @see NodeVersion
 */
public
enum ExecutionMethod
{  
  /** 
   * All files associated with the node must be generated in a single execution of the 
   * regeneration action. <P> 
   * 
   * This means that if any of the files associated with a node need regeneration, then 
   * all files will be regenerated.  This is typically used for nodes which represent
   * some form of physical simulation such as dynamics, particle systems, cloth and 
   * hair simulations. 
   */
  Serial, 

  /**
   * Each file associated with the node may be generated by an independent execution of 
   * the regeneration action. <P> 
   * 
   * Multiple files may still be regenerated by a single execution of the regeneration 
   * action depending on the setting of the batch size property (see 
   * {@link NodeCommon#getBatchSize getBatchSize}) of the node.  However it is possible that
   * each frame may be generated by a seperate action execution.  Furthermore, there is 
   * no dependencies between the frames and they may be executed on the same or different 
   * hosts at simultaneously.  This policy is typically used for rendering and compositing 
   * actions which have no inter-frame dependencies.
   */
  Parallel;

}
