// $Id: SimpleAssetBuilder.java,v 1.3 2009/03/10 16:54:04 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.stages.*;

import com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   S I M P L E   A S S E T   B U I L D E R                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Simple Asset Builder for the Nathan Love Base Collection.
 * <p>
 * This sort of asset is not broken up into departments.  Instead is has a single work file 
 * in which all departments work together.
 */
public 
class SimpleAssetBuilder
  extends BaseAssetBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Default constructor that allows for standalone invocation.
   */
  public
  SimpleAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
   this(mclient, qclient, builderInformation, 
        new StudioDefinitions(mclient, qclient, 
          UtilContext.getDefaultUtilContext(mclient), builderInformation.getLoggerName()));
  }

  public 
  SimpleAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefinitions studioDefinitions
  )
    throws PipelineException
  {
    super("SimpleAsset",
          "Simple Asset Builder for the Nathan Love Base Collection.",
          mclient, qclient, builderInformation, studioDefinitions);
    
    
    addSetupPass(new InformationPass());
    addConstructPass(new BuildPass());
    addConstructPass(new FinalizePass());
    
    setDefaultEditors(StudioDefinitions.getDefaultEditors());
    
    setLayout(getBuilderLayout());
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
      pRequiredNodes.add(pProjectNames.getModelPlaceholderMEL());
      pRequiredNodes.add(pProjectNames.getAssetVerificationMEL());
    }
    private static final long serialVersionUID = 555373229473853972L;
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
      String type = TaskType.Asset.toTitle();
      String editAsset = pAssetNames.getAssetEditScene();
      String verifyAsset = pAssetNames.getAssetEditScene();
      
      buildTexture();
      
      {
        AssetEditStage stage = 
          new AssetEditStage
          (pStageInfo, pContext, pClient, pMayaContext, 
           editAsset, pAssetNames.getTextureEditNode(), 
           pProjectNames.getModelPlaceholderMEL());
        addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
        if (stage.build())
          pFinalizeStages.add(stage);
      }
      {
        AssetVerifyStage stage =
          new AssetVerifyStage
          (pStageInfo, pContext, pClient, pMayaContext,
           verifyAsset, editAsset, pProjectNames.getAssetVerificationMEL());
        addTaskAnnotation(stage, NodePurpose.Focus, pProjectName, pTaskName, type);
        stage.build();
      }
      String assetApprove = pAssetNames.getAssetApproveNode();
      {
        String assetSubmit = pAssetNames.getAssetSubmitNode();
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(verifyAsset);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, assetSubmit, sources);
        addSubmitTaskAnnotation(stage, pProjectName, pTaskName, type);
        if (stage.build()) {
          addToQueueList(assetSubmit);
          addToCheckInList(assetSubmit);
        }
      }
      String assetFinal = pAssetNames.getAssetProductScene();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, assetFinal, "ma", verifyAsset, StageFunction.aMayaScene);
        addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
        stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(assetFinal);
        sources.add(pAssetNames.getTextureProductNode());
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, assetApprove, sources);
        addApproveTaskAnnotation(stage, pProjectName, pTaskName, type, 
          new BuilderID("BaseBuilders", new VersionID("1.0.0"), "NathanLove", 
                        "ShadeApproveTask"));
        if (stage.build()) {
          addToQueueList(assetApprove);
          addToCheckInList(assetApprove);
        }
      }
    }
    
    private void
    buildTexture()
      throws PipelineException
    {
      String type = TaskType.Asset.toTitle();
      
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
    
    private static final long serialVersionUID = -5849496071993238049L;
  }
  private static final long serialVersionUID = -5956920286874370156L;
}
