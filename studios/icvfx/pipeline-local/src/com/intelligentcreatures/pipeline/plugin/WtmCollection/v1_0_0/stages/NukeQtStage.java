// $Id: NukeQtStage.java,v 1.1 2008/02/07 14:14:33 jim Exp $

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
   * @param suffix
   *   The suffix for the created node.
   * 
   * @param source
   *   The name of source images.
   * 
   * @param imageNumber 
   *   Specifies the frame number of image from the source sequence to process.
   * 
   * @param thumbnailSize
   *   The image resolution of the generated thumbnail. 
   * 
   * @param addAlpha
   *   Whether to add an solid alpha channel to the input image before resizing and/or
   *   compositing over the optional background layer.
   * 
   * @param overBackground
   *   Whether to composite the thumbnail images over a constant colored background layer.
   * 
   * @param backgroundColor
   *   The thumbnail is composited over a background layer of this constant color.
   */
  public
  NukeQtStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName, 
   String source,
   double fps
  )
    throws PipelineException
  {
    super("NukeQtStage",
          "Generates a thumbnail image using the NukeQt action.", 
          stageInfo, context, client,
          nodeName, "qt",
          null,
          new PluginContext("NukeQt"));

    addLink(new LinkMod(source, LinkPolicy.Dependency));
    addSingleParamValue("FPS", fps);
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
    return ICStageFunction.aQuickTime; 
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 4435149474061655258L;

}
