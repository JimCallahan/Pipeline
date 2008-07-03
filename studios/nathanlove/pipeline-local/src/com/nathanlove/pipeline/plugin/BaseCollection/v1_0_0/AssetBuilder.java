// $Id: AssetBuilder.java,v 1.3 2008/07/03 19:52:48 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.stages.*;

import com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   B U I L D E R                                                              */
/*------------------------------------------------------------------------------------------*/


/**
 * Standard Asset Builder for the Nathan Love Base Collection.
 */
public 
class AssetBuilder
  extends BaseAssetBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Default constructor that allows for standalone invocation.
   */
  public
  AssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
   this(mclient, qclient, builderInformation, 
        new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)));
  }

  public 
  AssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefinitions studioDefinitions
  )
    throws PipelineException
  {
    super("Asset",
          "Standard Asset Builder for the Nathan Love Base Collection.",
          mclient, qclient, builderInformation, studioDefinitions);
    
    pStudioDefinitions = studioDefinitions;
    
    addSetupPass(new InformationPass());
    addConstructPass(new BuildPass());
    addConstructPass(new FinalizePass());
    addConstructPass(new SecondFinalizePass());
    
    setDefaultEditors(StudioDefinitions.getDefaultEditors());
    
    setLayout(getBuilderLayout());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  V E R I F I C A T I O N                                                               */
  /*----------------------------------------------------------------------------------------*/
  
  @SuppressWarnings("unchecked")
  @Override
  public MappedArrayList<String, PluginContext> 
  getNeededActions()
  {
    MappedArrayList<String, PluginContext> toReturn = 
      new MappedArrayList<String, PluginContext>();
    
    String toolset = getToolset();
    
    {
      Range<VersionID> range = new Range<VersionID>(new VersionID("2.3.4"), null);
      toReturn.put(toolset, new PluginContext("MayaBuild", "Temerity", range));
    }
    {
      Range<VersionID> range = new Range<VersionID>(new VersionID("2.2.1"), null);
      toReturn.put(toolset, new PluginContext("Touch", "Temerity", range));
    }
    {
      Range<VersionID> range = new Range<VersionID>(new VersionID("2.3.15"), null);
      toReturn.put(toolset, new PluginContext("Copy", "Temerity", range));
    }
    {
      Range<VersionID> range = new Range<VersionID>(new VersionID("2.4.1"), null);
      toReturn.put(toolset, new PluginContext("MayaFTNBuild", "Temerity", range));
    }
    {
      Range<VersionID> range = new Range<VersionID>(new VersionID("2.3.1"), null);
      toReturn.put(toolset, new PluginContext("MayaShaderExport", "Temerity", range));
    }
    
    return toReturn;
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
      pRequiredNodes.add(pProjectNames.getModelVerificationMEL());
      pRequiredNodes.add(pProjectNames.getRigVerificationMEL());
      pRequiredNodes.add(pProjectNames.getRigFinalizeMEL(pAssetType));
      pRequiredNodes.add(pProjectNames.getShadeFinalizeMEL(pAssetType));
      pRequiredNodes.add(pProjectNames.getShadeVerificationMEL());
      
    }
    private static final long serialVersionUID = 7409773506214092503L;
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
      pFinalizeStages2 = new LinkedList<FinalizableStage>();
      pStageInfo = getStageInformation();
      buildModel();
      buildRig();
      buildTexture();
      buildShade();
    }
    
    private void
    buildModel()
      throws PipelineException
    {
      String type = TaskType.Modeling.toTitle();
      String editModel = pAssetNames.getModelEditScene();
      String verifyModel = pAssetNames.getModelVerifyScene();
      
      {
        ModelEditStage stage = 
          new ModelEditStage
          (pStageInfo, pContext, pClient, pMayaContext, 
           editModel, pProjectNames.getModelPlaceholderMEL());
        addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
        if (stage.build())
          pFinalizeStages.add(stage);
      }
      {
        AssetVerifyStage stage =
          new AssetVerifyStage
          (pStageInfo, pContext, pClient, pMayaContext,
           verifyModel, editModel, pProjectNames.getModelVerificationMEL());
        addTaskAnnotation(stage, NodePurpose.Focus, pProjectName, pTaskName, type);
        stage.build();
      }
      String modelApprove = pAssetNames.getModelApproveNode();
      {
        String modelSubmit = pAssetNames.getModelSubmitNode();
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(verifyModel);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, modelSubmit, sources);
        addSubmitTaskAnnotation(stage, pProjectName, pTaskName, type);
        if (stage.build()) {
          addToQueueList(modelSubmit);
          addToCheckInList(modelSubmit);
        }
      }
      String modelFinal = pAssetNames.getModelProductScene();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, modelFinal, "ma", verifyModel, StageFunction.aMayaScene.toString());
        addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
        stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(modelFinal);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, modelApprove, sources);
        addApproveTaskAnnotation(stage, pProjectName, pTaskName, type, 
          new BuilderID("Approval", new VersionID("2.4.1"), "Temerity", "ApproveTask"));
        if (stage.build()) {
          addToQueueList(modelApprove);
          addToCheckInList(modelApprove);
        }
      }
    }
    
    private void
    buildRig()
      throws PipelineException
    {
      String type = TaskType.Rigging.toTitle();
      LockBundle bundle = new LockBundle();
      
      String modelProduct = pAssetNames.getModelProductScene();
      bundle.addNodeToLock(modelProduct);
      
      String rigEdit = pAssetNames.getRigEditScene();
      {
        RigEditStage stage = 
          new RigEditStage
          (pStageInfo, pContext, pClient, pMayaContext,
           rigEdit,
           modelProduct);
        addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
        if (stage.build()) 
          pFinalizeStages2.add(stage);
      }
      
      String rigVerify = pAssetNames.getRigVerifyScene();
      {
        AssetVerifyStage stage =
          new AssetVerifyStage
          (pStageInfo, pContext, pClient, pMayaContext,
            rigVerify, rigEdit, pProjectNames.getRigFinalizeMEL(pAssetType));
        addTaskAnnotation(stage, NodePurpose.Focus, pProjectName, pTaskName, type);
        stage.build();
      }
      String rigApprove = pAssetNames.getRigApproveNode();
      {
        String rigSubmit = pAssetNames.getRigSubmitNode();
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(rigVerify);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, rigSubmit, sources);
        addSubmitTaskAnnotation(stage, pProjectName, pTaskName, type);
        if (stage.build()) {
          addToQueueList(rigSubmit);
          addToCheckInList(rigSubmit);
          bundle.addNodeToCheckin(rigSubmit);
        }
      }
      String rigFinal = pAssetNames.getRigProductScene();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, rigFinal, "ma", rigVerify, StageFunction.aMayaScene.toString());
        addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
        stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(rigFinal);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, rigApprove, sources);
        addApproveTaskAnnotation(stage, pProjectName, pTaskName, type, 
          new BuilderID("Approval", new VersionID("2.4.1"), "Temerity", "ApproveTask"));
        if (stage.build()) {
          addToQueueList(rigApprove);
          addToCheckInList(rigApprove);
          bundle.addNodeToCheckin(rigApprove);
        }
      }
      if (!bundle.getNodesToCheckin().isEmpty())
        addLockBundle(bundle);
    }
    
    private void
    buildTexture()
      throws PipelineException
    {
      String type = TaskType.Shading.toTitle();
      
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
    
    private void
    buildShade()
      throws PipelineException
    {
      String type = TaskType.Shading.toTitle();
      
      String modelFinal = pAssetNames.getModelProductScene();
      
      LockBundle bundle = new LockBundle();
      
      String shdName = pAssetNames.getShadeEditScene();
      {
        ShadeEditStage stage =
          new ShadeEditStage
          (pStageInfo, pContext, pClient, pMayaContext,
           shdName, modelFinal,
           pAssetNames.getTextureEditNode());
        addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
        if (stage.build())
          addToDisableList(shdName);
      }
      
      String shadeExport = pAssetNames.getShaderExportScene();
      {
        ShaderExportStage stage = 
          new ShaderExportStage
          (pStageInfo, pContext, pClient, 
           shadeExport, shdName,
           pProjectNames.getShadeVerificationMEL());
        addTaskAnnotation(stage, NodePurpose.Prepare, pProjectName, pTaskName, type);
        stage.build();
      }
      
      String rigProduct = pAssetNames.getRigProductScene();
      bundle.addNodeToLock(rigProduct);
      
      String shdFinal = pAssetNames.getShadeFinalScene();
      {
        ShadeFinalStage stage = 
          new ShadeFinalStage
          (pStageInfo,
           pContext, 
           pClient,
           pMayaContext,
           shdFinal, 
           rigProduct, shdName, shadeExport,
           pProjectNames.getShadeFinalizeMEL(pAssetType));
        addTaskAnnotation(stage, NodePurpose.Focus, pProjectName, pTaskName, type);
        stage.build();
      }
      String shdApprove = pAssetNames.getShadeApproveNode();
      {
        String shdSubmit = pAssetNames.getShadeSubmitNode();
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(shdFinal);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, shdSubmit, sources);
        addSubmitTaskAnnotation(stage, pProjectName, pTaskName, type);
        if (stage.build()) {
          addToQueueList(shdSubmit);
          addToCheckInList(shdSubmit);
          bundle.addNodeToCheckin(shdSubmit);
        }
      }
      String shdProduct = pAssetNames.getShadeProductScene();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, shdProduct, "ma", shdFinal, StageFunction.aMayaScene.toString());
        addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
        stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(shdProduct);
        sources.add(pAssetNames.getTextureProductNode());
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, shdApprove, sources);
        addApproveTaskAnnotation(stage, pProjectName, pTaskName, type, 
          new BuilderID("BaseCollection", new VersionID("1.0.0"), "NathanLove", 
                        "ShadeApproveTask"));
        if (stage.build()) {
          addToQueueList(shdApprove);
          addToCheckInList(shdApprove);
          bundle.addNodeToCheckin(shdApprove);
          
        }
      }
      if (!bundle.getNodesToCheckin().isEmpty())
        addLockBundle(bundle);
    }
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      return pRequiredNodes;
    }

    private static final long serialVersionUID = 1476189272491840367L;

    private StageInformation pStageInfo;
  }
  
  protected
  class SecondFinalizePass
    extends ConstructPass
  {
    public 
    SecondFinalizePass()
    {
      super("SecondFinalizePass", 
            "The AssetBuilder pass that cleans everything else.");
    }
    
    @Override
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> regenerate = new LinkedList<String>();

      for(FinalizableStage stage : pFinalizeStages2) 
        regenerate.add(stage.getNodeName());

      return regenerate;
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      for(FinalizableStage stage : pFinalizeStages2) 
        stage.finalizeStage();
    }    
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 211203648424077939L;

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  protected LinkedList<FinalizableStage> pFinalizeStages2;
}
