// $Id: AfterFXImgRenderStage.java,v 1.1 2008/02/05 09:35:23 jesse Exp $

package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;

/**
 *  
 *
 */
public 
class AfterFXImgRenderStage
  extends StandardStage
{

  public
  AfterFXImgRenderStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String suffix,
    String source,
    String compName
  )
    throws PipelineException
  {
    super("AfterFXImgRender", "Renders an AfterFX scene to an image sequence",
          stageInformation, context, client,
          nodeName, suffix, null, new PluginContext("AfterFXRenderImg"));
    
    addLink(new LinkMod(source, LinkPolicy.Reference));
  }
  
  public static final String aCompName = "CompName";
  
  public static final String aAfterFXScene = "AfterFXScene";
}
