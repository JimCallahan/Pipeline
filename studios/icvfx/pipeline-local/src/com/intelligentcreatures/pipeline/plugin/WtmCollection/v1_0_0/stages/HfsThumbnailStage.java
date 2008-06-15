// $Id: HfsThumbnailStage.java,v 1.2 2008/06/16 17:01:35 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   T H U M B N A I L   S T A G E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a thumbnail image using the HfsThumbnail action.
 */ 
public 
class HfsThumbnailStage
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
   * @param pixelGain
   *   Multiplier of input pixel value intensity to produce thumbnail pixel values.
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
  HfsThumbnailStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName, 
   String suffix,
   String source,
   int imageNumber, 
   int thumbnailSize,
   double pixelGain, 
   boolean addAlpha, 
   boolean overBackground, 
   Color3d backgroundColor
  )
    throws PipelineException
  {
    super("HfsThumbnailStage",
          "Generates a thumbnail image using the HfsThumbnail action.", 
          stageInfo, context, client,
          nodeName, suffix,
          null,
          new PluginContext("HfsThumbnail"));

    addLink(new LinkMod(source, LinkPolicy.Dependency));
    addSingleParamValue("ImageNumber", imageNumber);
    addSingleParamValue("ThumbnailSize", thumbnailSize);
    addSingleParamValue("PixelGain", pixelGain); 
    addSingleParamValue("AddAlpha", addAlpha);
    addSingleParamValue("OverBackground", overBackground);
    addSingleParamValue("BackgroundColor", backgroundColor);
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
    return ICStageFunction.aRenderedImage;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -4663719599812425763L;

}
