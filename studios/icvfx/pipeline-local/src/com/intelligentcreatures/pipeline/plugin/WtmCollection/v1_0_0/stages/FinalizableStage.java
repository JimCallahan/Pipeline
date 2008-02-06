// $Id: FinalizableStage.java,v 1.2 2008/02/06 18:17:43 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I N A L I Z A B L E   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A stage that supports finalizing actions.   
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
 
