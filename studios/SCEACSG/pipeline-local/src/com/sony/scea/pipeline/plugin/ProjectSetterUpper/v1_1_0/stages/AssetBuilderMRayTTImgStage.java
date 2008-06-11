/*
 * Created on Jul 7, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2
 * 
 */
package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import java.util.TreeSet;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
//import us.temerity.pipeline.stages.TurntableStage;

public 
class AssetBuilderMRayTTImgStage 
  extends MayaMRayTurntableStage
{

public 
  AssetBuilderMRayTTImgStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String mayaScene,
    String globals//,
    //Renderer renderer
  )
    throws PipelineException
  {
    super("AssetBuilderTTImg", 
          "The stage in the AdvAssetBuilder that makes a turntable images node", 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          "iff", 
          180, 
          4, 
          mayaScene, 
          globals//,
          /*renderer*/);
    //addSingleParamValue("CameraOverride", "tt:renderCam");
    
    //TreeSet<String> selectionKeys = new TreeSet<String>();
    //selectionKeys.add("OnlyLinux"); 
    //selectionKeys.add("LinuxOnly");
    //setSelectionKeys(selectionKeys);
  }
private static final long serialVersionUID = -8772334695943722039L;
}
