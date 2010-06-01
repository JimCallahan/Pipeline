package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   P R E   B L O T   A N I M   S T A G E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the Maya scene used to animate the blot textures.
 */
public
class BuildPreBlotAnimStage
  extends MayaBuildStage
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
   *
   * @param attachMEL
   *   The name of the node containing the soundtrack attach MEL script.
   *
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  BuildPreBlotAnimStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String seqAnim,
    String attachMEL,
    String frameOffsetNode,
    FrameRange range
  )
    throws PipelineException
  {
    super("BuildPreBlotAnim",
      	  "Pre-blot Maya scene.",
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);

    if(range != null)
      setMayaFrameRange(range);

    setUnits();

    addLink(new LinkMod(seqAnim, LinkPolicy.Dependency));
    addSourceParamValue(seqAnim, "PrefixName", "lib");
    addSourceParamValue(seqAnim, "BuildType", "Import");
    addSourceParamValue(seqAnim, "NameSpace", true);

    addLink(new LinkMod(attachMEL, LinkPolicy.Dependency));
    addSingleParamValue("ModelMEL", attachMEL);

    addLink(new LinkMod(frameOffsetNode, LinkPolicy.Dependency));
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7748064797093838599L;


}
