// $Id: SlateSubstStage.java,v 1.1 2008/04/02 20:56:16 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S L A T E   S U B S T   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the SlateSubst action.
 */ 
public 
class SlateSubstStage 
  extends StandardStage
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new stage which generates a Nuke script using the "Substitute" mode.
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
   * @param templateScript
   *   The name of the template Nuke script node.
   * 
   * @param deliveryType
   *   The type of deliverable.
   * 
   * @param deliverable
   *   The name of the deliverable.
   * 
   * @param sourceVersion
   *   The source images node checked-in version being delivered.
   * 
   * @param notes
   *   Notes about the deliverable to be included in the slate.
   * 
   * @param slateHold
   *   The number of frames to hold the slate before starting the animation. 
   */ 
  @SuppressWarnings("unchecked")
  public
  SlateSubstStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String templateScript,
   String deliveryType, 
   String deliverable, 
   String clientVersion, 
   NodeVersion sourceVersion, 
   String notes, 
   int slateHold
  )
    throws PipelineException
  {
    super("SlateSubst", 
	  "Creates a node which uses the SlateSubst action.", 
          stageInfo, context, client, 
          nodeName, "nk", 
          new PluginContext("Nuke"), 
	  new PluginContext("SlateSubst", "ICVFX")); 

    addLink(new LinkMod(templateScript, LinkPolicy.Dependency));
    addSingleParamValue("TemplateScript", templateScript); 

    addSingleParamValue("DeliveryType", deliveryType); 
    addSingleParamValue("Deliverable", deliverable); 
    addSingleParamValue("ClientVersion", clientVersion); 
    addSingleParamValue("SourceImages", sourceVersion.getPrimarySequence().toString()); 
    addSingleParamValue("SourceNode", sourceVersion.getName()); 
    addSingleParamValue("SourceVersion", sourceVersion.getVersionID().toString()); 
    addSingleParamValue("CreatedOn", TimeStamps.format(sourceVersion.getTimeStamp())); 
    addSingleParamValue("CreatedBy", sourceVersion.getAuthor()); 
    addSingleParamValue("Notes", notes); 
    addSingleParamValue("SlateHold", slateHold); 
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
    return ICStageFunction.aNukeScript;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 7552272168207902771L;

}
