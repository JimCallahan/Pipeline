// $Id: CameraAssetBuilder.java,v 1.3 2009/03/10 16:54:04 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.stages.*;

import com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   C A M E R A   A S S E T   B U I L D E R                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Camera Asset Builder for the Nathan Love Base Collection.
 */
public 
class CameraAssetBuilder
  extends BaseAssetBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Default constructor that allows for stand alone invocation.
   */
  public
  CameraAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
   this(mclient, qclient, builderInformation, 
        new StudioDefinitions(mclient, qclient, 
          UtilContext.getDefaultUtilContext(mclient), builderInformation.getLoggerName()),
        null);
  }
  
  public 
  CameraAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefinitions studioDefinitions,
    String cameraName
  )
    throws PipelineException
  {
    super("CameraAsset",
          "Camera Asset Builder for the Nathan Love Base Collection.",
          mclient, qclient, builderInformation, studioDefinitions);
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (ParamNames.aAssetName, 
         "The Name of the asset", 
         cameraName);
      addParam(param);
    }
    {
      UtilityParam param = 
        new StringUtilityParam
        (ParamNames.aAssetType, 
         "The Type of the asset", 
         AssetType.cam.toTitle());
      addParam(param);
    }
    disableParam(new ParamMapping(ParamNames.aAssetType));
    if (cameraName != null)
      disableParam(new ParamMapping(ParamNames.aAssetName));
    
    addMappedParam(pAssetNames.getName(), 
                   ParamNames.aAssetName, 
                   ParamNames.aAssetName);
    
    addMappedParam(pAssetNames.getName(), 
                   ParamNames.aAssetType, 
                   ParamNames.aAssetType);
    
    addSetupPass(new InformationPass());
    addConstructPass(new BuildPass());
    addConstructPass(new FinalizePass());
    
    setDefaultEditors(StudioDefinitions.getDefaultEditors());
    
    PassLayoutGroup layout = getBuilderLayout();
    AdvancedLayoutGroup group = layout.getPassLayout(1);
    group.addEntry(1, ParamNames.aAssetName);
    group.addEntry(1, ParamNames.aAssetType);
    
    
    setLayout(layout);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class InformationPass
    extends SetupPass
  {
    public 
    InformationPass()
    {
      super("Information Pass", 
            "Information pass for the AssetBuilder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pStudioDefinitions.setContext(pContext);
      getStageInformation().setDoAnnotations(true);
      
      pMayaContext = (MayaContext) getParamValue(ParamNames.aMayaContext);
      
      pAssetType = pAssetNames.getAssetType();
      
      pProjectName = pProjectNames.getProjectName();
      pTaskName = pAssetNames.getTaskName();
      
      pRequiredNodes = new TreeSet<String>();
      pRequiredNodes.add(pProjectNames.getCameraPlaceholderMEL());
    }
    private static final long serialVersionUID = -4924968686270321522L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  private
  class BuildPass
    extends ConstructPass
  {
    public 
    BuildPass()
    {
      super("Build Pass", 
            "The Pass which constructs the node networks.");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      pFinalizeStages = new LinkedList<FinalizableStage>();
      pStageInfo = getStageInformation();
     
      String cameraNode = pAssetNames.getAssetEditScene();
      String cameraMEL = pProjectNames.getCameraPlaceholderMEL();
      String type = "Camera";
      
      buildTexture();
      
      {
        AssetEditStage stage = 
          new AssetEditStage
          (pStageInfo, pContext, pClient, pMayaContext, 
           cameraNode, pAssetNames.getTextureEditNode(), 
           cameraMEL);
        addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);        
        if (stage.build()) {
          pFinalizeStages.add(stage);
        }
      }
      {
        String camSubmit = pAssetNames.getAssetSubmitNode();
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(cameraNode);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, camSubmit, sources);
        addSubmitTaskAnnotation(stage, pProjectName, pTaskName, type);
        if (stage.build()) {
          addToQueueList(camSubmit);
          addToCheckInList(camSubmit);
        }
      }
      String camFinal = pAssetNames.getAssetProductShortScene();
      String camApprove = pAssetNames.getAssetApproveNode();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, camFinal, "ma", cameraNode, 
                           StageFunction.aMayaScene.toString());
        addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
        stage.build();
      }
      String finalTex = pAssetNames.getTextureProductNode();
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(camFinal);
        sources.add(finalTex);
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, camApprove, sources);
        addApproveTaskAnnotation(stage, pProjectName, pTaskName, type, 
          new BuilderID("BaseBuilders", new VersionID("1.0.0"), "NathanLove", 
                        "ShadeApproveTask"));
        if (stage.build()) {
          addToQueueList(camApprove);
          addToCheckInList(camApprove);
        }
      }
    }
    
    private void
    buildTexture()
      throws PipelineException
    {
      String type = "Camera";
      
      String texNode = pAssetNames.getTextureEditNode();
      {
        MayaFTNBuildStage stage = 
          new MayaFTNBuildStage(pStageInfo, pContext, pClient, pMayaContext, texNode, true);
        addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
        stage.build();
      }
      
      String finalTex = pAssetNames.getTextureProductNode();
      {
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, finalTex, new TreeSet<String>());
        addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
        stage.build();
      }
    }
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      return pRequiredNodes;
    }

    private StageInformation pStageInfo;
    private static final long serialVersionUID = 2111701091751066701L;
  }
  
  private static final long serialVersionUID = -4924968686270321522L;
}
