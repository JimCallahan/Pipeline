// $Id: NukeSubstCompStage.java,v 1.3 2008/03/17 19:33:35 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   S U B S T   C O M P   S T A G E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the NukeSubstComp action.
 */ 
public 
class NukeSubstCompStage 
  extends StandardStage
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new stage.
   * 
   * @param stageInfo
   *   Class containing basic information shared among all stages.
   * 
   * @param context
   *   The {@link UtilContext} that this stage acts in.
   * 
   * @param client
   *   The instance of Master Manager that the stage performs all its actions in.
   * 
   * @param nodeName
   *   The name of the node that is to be created.
   * 
   * @param range
   *   The frame range to render.
   * 
   * @param padding
   *   The padding for the file numbers. If this is set to <code>null</code>, a
   *   padding of 4 will be used.
   * 
   * @param suffix 
   *   The filename suffix of the create node.
   * 
   * @param mode
   *   The mode of operation: "Append & Process" or "Process".
   * 
   * @param masterNuke
   *   The name of source master Nuke script.
   * 
   * @param substitutions
   *   The values for the per-source "ReplaceName" parameter indexed by source image
   *   node name containing the Nuke script fragments to replace with.
   */ 
  @SuppressWarnings("unchecked")
  public
  NukeSubstCompStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   FrameRange range, 
   int padding, 
   String suffix, 
   String mode, 
   String masterNuke,
   TreeMap<String,String> substitutions
  )
    throws PipelineException
  {
    this(stageInfo, context, client, nodeName, range, padding, suffix, 
         mode, masterNuke, substitutions, null); 
  }

  /**
   * Construct a new stage.
   * 
   * @param stageInfo
   *   Class containing basic information shared among all stages.
   * 
   * @param context
   *   The {@link UtilContext} that this stage acts in.
   * 
   * @param client
   *   The instance of Master Manager that the stage performs all its actions in.
   * 
   * @param nodeName
   *   The name of the node that is to be created.
   * 
   * @param range
   *   The frame range to render.
   * 
   * @param padding
   *   The padding for the file numbers. If this is set to <code>null</code>, a
   *   padding of 4 will be used.
   * 
   * @param suffix 
   *   The filename suffix of the create node.
   * 
   * @param mode
   *   The mode of operation: "Append & Process" or "Process".
   * 
   * @param masterNuke
   *   The name of source master Nuke script.
   * 
   * @param substitutions
   *   The values for the per-source "ReplaceName" parameter indexed by source image
   *   node name containing the Nuke script fragments to replace with.
   * 
   * @param editor
   *   The editor plugin specification.
   */ 
  @SuppressWarnings("unchecked")
  public
  NukeSubstCompStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   FrameRange range, 
   int padding, 
   String suffix, 
   String mode, 
   String masterNuke,
   TreeMap<String,String> substitutions, 
   PluginContext editor
  )
    throws PipelineException
  {
    super("NukeSubstComp", 
	 "Creates a node which uses the NukeSubstComp action.", 
          stageInfo, context, client, 
          nodeName, range, padding, suffix, 
          editor, 
	  new PluginContext("NukeSubstComp", "Temerity", 
			    new Range<VersionID>(new VersionID("2.4.2"), null)));  

    addSingleParamValue("Mode", mode); 

    addLink(new LinkMod(masterNuke, LinkPolicy.Dependency));
    addSingleParamValue("MasterScript", masterNuke); 

    for(String sname : substitutions.keySet()) {
      String replaceName = substitutions.get(sname);
      addLink(new LinkMod(sname, LinkPolicy.Dependency));
      addSourceParamValue(sname, "ReplaceName", replaceName); 
    }

    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(5);
  }

  


  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aRenderedImage;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 8871017210829213950L;

}
