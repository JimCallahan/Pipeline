// $Id: SilhouetteBuildStage.java,v 1.1 2008/02/05 09:35:24 jesse Exp $

package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0.stages;

import java.util.List;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;


public 
class SilhouetteBuildStage
  extends StandardStage
{

  public 
  SilhouetteBuildStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String format,
    List<String> plates
  )
    throws PipelineException
  {
    super("SilhouetteBuild", "Makes a silhouette scene using silhouette build",
          stageInformation, context, client,
          nodeName, "sfx",
          null,
          new PluginContext("SilhouetteBuild"));
    for (String plate : plates) {
      addLink(new LinkMod(plate, LinkPolicy.Reference));
    }
    if (format != null)
      addSingleParamValue(aSession, format);
  }
  
  @Override
  public String 
  getStageFunction()
  {
    return "SilhouetteScene";
  }

  public static final String aSession = "Session";
  
  private static final long serialVersionUID = -2322652195858939826L;
}
