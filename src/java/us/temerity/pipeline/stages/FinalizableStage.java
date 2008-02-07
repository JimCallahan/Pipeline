// $Id: FinalizableStage.java,v 1.1 2008/02/07 10:20:04 jesse Exp $

package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   F I N A L I Z A B L E   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Stages which extend this class have a step which needs to be run after they
 * have been built and queued. 
 */
public 
interface FinalizableStage
{
  
  /**
   * Getter for the name of the created node.
   */
  public String 
    getNodeName();
  
  /**
   * Finishes off the work of the stage after it has been queued.
   */
  public void
  finalizeStage()
    throws PipelineException;
}
