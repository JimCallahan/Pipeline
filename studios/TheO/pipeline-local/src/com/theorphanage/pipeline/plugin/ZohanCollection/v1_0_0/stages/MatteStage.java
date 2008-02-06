// $Id: MatteStage.java,v 1.1 2008/02/06 05:12:04 jesse Exp $

package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.StageFunction;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.EmptyFileStage;


public 
class MatteStage
  extends EmptyFileStage
{
  public
  MatteStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    String silhouetteScene
  )
    throws PipelineException
  {
    super(stageInformation, context, client,
          nodeName, range, 4, "exr");
    
    addLink(new LinkMod(silhouetteScene, LinkPolicy.Dependency));
  }
  
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aRenderedImage;
  }
  
  private static final long serialVersionUID = 944517619162407662L;
}
