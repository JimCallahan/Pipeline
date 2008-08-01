// $Id: NukeQtStage.java,v 1.5 2008/08/01 20:19:15 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   Q U I C K T I M E   S T A G E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a QuickTime movie from a set of images using the NukeQt action.
 */
public
class NukeQtStage
  extends StandardStage
{
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
   * @param imageSource
   *   The name of source images.
   *
   * @param audioSource
   *   The name of source audio soundtrack.
   *
   * @param fps
   *   The frames per second of generated movie.
   */
  @SuppressWarnings("unchecked")
  public
  NukeQtStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   String imageSource,
   String audioSource,
   double fps
  )
    throws PipelineException
  {
    super("NukeQtStage",
          "Generates a thumbnail image using the NukeQt action.",
          stageInfo, context, client,
          nodeName, "qt",
          null,
          new PluginContext("NukeQt", "ICVFX",
			    new Range<VersionID>(new VersionID("1.0.0"), null)));

    addLink(new LinkMod(imageSource, LinkPolicy.Dependency));
    addSingleParamValue("ImageSource", imageSource);
    //addSingleParamValue("Codec", "Motion JPEG A");
    //addSingleParamValue("Quality", "Normal");
    //addSingleParamValue("KeyframeRate", 1);
    addSingleParamValue("FPS", fps);

    if(audioSource != null) {
      addLink(new LinkMod(audioSource, LinkPolicy.Dependency));
      addSingleParamValue("AudioSource", audioSource);
    }
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
    return ICStageFunction.aQuickTimeSound;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4435149474061655258L;

}
