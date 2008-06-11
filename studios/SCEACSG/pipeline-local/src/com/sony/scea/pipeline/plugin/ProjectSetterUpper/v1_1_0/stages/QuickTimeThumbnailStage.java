package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
//import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;


public 
class QuickTimeThumbnailStage
  extends StandardStage
{

public
  QuickTimeThumbnailStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName, 
    String suffix,
    String source,
    int xRes,
    int yRes,
    int frame
  )
    throws PipelineException
  {
    super("QuickTimeThumbnailStage",
          "Stage to build a thumbnail image from a sequence",
          stageInformation,
          context,
          client,
          nodeName,
          suffix,
          null,
          new PluginContext("ShakeQuickTimeThumbnail", "SCEA"));
    addLink(new LinkMod(source, LinkPolicy.Dependency));
    addSingleParamValue("MovieSource", source);
    addSingleParamValue("XRes", xRes);
    addSingleParamValue("YRes", yRes);
    addSingleParamValue("Frame", frame);
    
    TreeSet<String> selectionKeys = new TreeSet<String>();
    selectionKeys.add("MacOnly");
    setSelectionKeys(selectionKeys);
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
/*
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aRenderedImage;
  }
*/
  private static final long serialVersionUID = 7417987382567608187L;
}
