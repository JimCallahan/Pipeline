// $Id: OverflowPolicy.java,v 1.1 2004/03/13 17:20:13 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   O V E R F L O W   P O L I C Y                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The frame range overflow policy. <P> 
 * 
 * During generation of jobs to execute the regneration action for nodes with a 
 * {@link LinkRelationship LinkRelationship} of {@link LinkRelationship@OneToOne OneToOne}, 
 * it is possible that the frame index offset of the node link may overflow the frame range
 * of the source node.  In these cases, this class provides the policy Pipeline will adopt
 * in handling this frame range overflow.
 *
 * @see NodeMod
 * @see NodeVersion
 */
public
enum OverflowPolicy
{  
  /** 
   * Any frame range overflow will be silently ignored. 
   */
  Ignore, 

  /**
   * Frame range overflows will cause Pipeline to abort generation of overflowed jobs and 
   * report these overflows to the user.
   */
  Abort;

}
