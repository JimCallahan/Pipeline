// $Id: CopyImagesStage.java,v 1.3 2008/06/15 17:31:10 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O P Y   I M A G E S   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Just copy some images.
 */ 
public 
class CopyImagesStage 
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
   * @param suffix
   *   The suffix for the created node.
   * 
   * @param source
   *   The name of the source images node.
   */
  public 
  CopyImagesStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String suffix,
    String source
  )
    throws PipelineException
  {
    super("CopyImages", 
          "Just copy some images.", 
          stageInfo, context, client, 
          nodeName, suffix, 
          null, new PluginContext("Copy"));

    addLink(new LinkMod(source, LinkPolicy.Dependency, LinkRelationship.OneToOne, 0));
    
    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(10);   
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
   *   The frame range for the node.
   * 
   * @param padding
   *   The padding for the file numbers. If this is set to <code>null</code>, a
   *   padding of 4 will be used.
   * 
   * @param suffix
   *   The suffix for the created node.
   * 
   * @param source
   *   The name of the source images node.
   */
  public 
  CopyImagesStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    String source
  )
    throws PipelineException
  {
    super("CopyImages", 
          "Just copy some images.", 
          stageInfo, context, client, 
          nodeName, range, padding, suffix, 
          null, new PluginContext("Copy"));

    addLink(new LinkMod(source, LinkPolicy.Dependency, LinkRelationship.OneToOne, 0));

    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(10);   
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
 
  private static final long serialVersionUID = 8575373286641931582L;

}
