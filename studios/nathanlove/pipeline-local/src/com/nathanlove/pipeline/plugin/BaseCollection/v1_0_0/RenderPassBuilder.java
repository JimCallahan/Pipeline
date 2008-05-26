// $Id: RenderPassBuilder.java,v 1.1 2008/05/26 03:19:50 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;

/*------------------------------------------------------------------------------------------*/
/*   R E N D E R   P A S S   B U I L D E R                                                  */
/*------------------------------------------------------------------------------------------*/

public class RenderPassBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Default Constructor for standalone invocation.
   */
  public
  RenderPassBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    this(mclient, 
         qclient, 
         builderInformation,
         new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)));
  }
  
  public 
  RenderPassBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefinitions studioDefinitions
  )
    throws PipelineException
  {
    super("RenderPass",
          "The builder to make render passes for an existing shot for the " +
          "Nathan Love Base Collection.",
          mclient, qclient, builderInformation, EntityType.Shot);
    
    DoubleMap<String, String, ArrayList<String>> all = studioDefinitions.getAllProjectsAllNames();
    Set<String> projects = all.keySet();
    if (projects == null || projects.isEmpty())
      throw new PipelineException
        ("Please create a project before running the pass builder.");
    boolean shot = false;
    for (String project : projects) {
      Set<String> spots = all.keySet(project);
      if (spots != null && !spots.isEmpty()) {
        for (String spot : spots) {
          ArrayList<String> shots = all.get(project, spot);
          if (shots != null && !shots.isEmpty()) {
            shot = true;
            break;
          }
        }
      }
      if (shot)
        break;
    }
    
    if (!shot)
      throw new PipelineException
        ("At least on shot must exist inside an existing project before the " +
         "Render Pass Builder can be run.");
    
  }

}
