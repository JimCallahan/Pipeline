// $Id: NukeThumbnailStage.java,v 1.2 2008/02/06 21:33:22 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   T H U M B N A I L   S T A G E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a thumbnail image using the NukeThumbnail action.
 */ 
public 
class NukeThumbnailStage
  extends StandardStage
{
  /**
   * Construct a new stage.
   * 
   * @param name
   *   The name of the stage.
   * 
   * @param desc
   *   A description of what the stage should do.
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
   * @param thumbnailSize
   *   Specifies the frame number of image from the source sequence to process.
   * 
   * @param thumbnailSize
   *   The image resolution of the generatted thumbnail. 
   */
  public
  NukeThumbnailStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName, 
   String suffix,
   String source,
   int imageNumber, 
   int thumbnailSize, 
   boolean addAlpha, 
   boolean overBackground, 
   Color3d backgroundColor
  )
    throws PipelineException
  {
    super("NukeThumbnailStage",
          "Generates a thumbnail image using the NukeThumbnail action.", 
          stageInfo, context, client,
          nodeName, suffix,
          null,
          new PluginContext("NukeThumbnail"));

    addLink(new LinkMod(source, LinkPolicy.Dependency));
    addSingleParamValue("ImageNumber", imageNumber);
    addSingleParamValue("ThumbnailSize", thumbnailSize);
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
    return StageFunction.aRenderedImage;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 4435149474061655258L;

}
