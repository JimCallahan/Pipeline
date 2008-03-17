// $Id: DjvUnixQtStage.java,v 1.2 2008/03/17 19:38:43 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D J V   U N I X   Q U I C K T I M E   S T A G E                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a QuickTime movie from a set of images using the DjvUnixQt action.
 */ 
public 
class DjvUnixQtStage
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
   * @param fps
   *   The frames per second of generated movie.
   */
  public
  DjvUnixQtStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName, 
   String imageSource,
   String fps
  )
    throws PipelineException
  {
    super("DjvUnixQtStage",
          "Generates a thumbnail image using the DjvUnixQt action.", 
          stageInfo, context, client,
          nodeName, "qt",
          null,
          new PluginContext("DjvUnixQt"));

    addLink(new LinkMod(imageSource, LinkPolicy.Dependency));
    addSingleParamValue("ImageSource", imageSource);
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
 
  private static final long serialVersionUID = -8605825130002847029L;

}
