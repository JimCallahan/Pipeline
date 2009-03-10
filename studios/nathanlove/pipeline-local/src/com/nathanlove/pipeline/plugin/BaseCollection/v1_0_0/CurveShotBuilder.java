// $Id: CurveShotBuilder.java,v 1.4 2009/03/10 16:54:04 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.stages.*;

import com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   C U R V E   S H O T   B U I L D E R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Shot builder for creating shots that export animation as Maya Curves.
 */
public 
class CurveShotBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Default Constructor for standalone invocation.
   * @param mclient
   *   The instance of Master Manager the builder will use. 
   * @param qclient 
   *   The instance of the Queue Manager the builder will use.
   * @param builderInformation 
   *   The globally shared builder information.
   * 
   */
  public
  CurveShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    this(mclient, 
         qclient, 
         builderInformation,
         new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)));
  }
  
  public 
  CurveShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefinitions studioDefinitions
  )
    throws PipelineException
  {
    super("CurveShot",
          "The shot builder using maya curves as an animation transfer format for the " +
          "Nathan Love Base Collection.",
          mclient, qclient, builderInformation, EntityType.Shot);
    
    pStudioDefinitions = studioDefinitions;
    
    ArrayList<String> projects = pStudioDefinitions.getProjectList();
    if (projects.isEmpty())
      throw new PipelineException
        ("Please create a project before running the shot builder.");
    
    addSetupPass(new SetupEssentialsPass());
    addSetupPass(new AssetInfoPass());
    addConstructPass(new MakeNodesPass());
    addConstructPass(new FinalizePass());
    
    /* Params  */
    {
      {
        DoubleMap<String, String, ArrayList<String>> location = 
          pStudioDefinitions.getAllProjectsAllNamesForParam();
  
        /* select the project, sequence and shot for the task */ 
        UtilityParam param = 
          new DoubleMapUtilityParam(
              ParamNames.aLocation, 
              "The Project, Sequence, and Shot to put the Shot in.",
              ParamNames.aProjectName,
              "Select the name of the project",
              ParamNames.aSpotName,
              "Select the name of the spot or [[NEW]] to create a new spot",
              ParamNames.aShotName,
              "Select the name of the shot or [[NEW]] to create a new shot",
              location);
        addParam(param);
      }

      {
        UtilityParam param = 
          new MayaContextUtilityParam
          (ParamNames.aMayaContext,
           "The Linear, Angular, and Time Units to assign to all constructed Maya scenes.",
           new MayaContext()); 
        addParam(param);
      }
      
      {
        String choices[] = {ParamNames.aSoftware, ParamNames.aMentalRay};
        UtilityParam param = 
          new EnumUtilityParam
          (ParamNames.aRenderer,
           "Which Maya rendering engine should be used for this shot.",
           ParamNames.aMentalRay,
           new ArrayList<String>(Arrays.asList(choices))); 
        addParam(param);
      }
      
      addCheckinWhenDoneParam();
      
      {
        UtilityParam param = 
          new IntegerUtilityParam
          (ParamNames.aStartFrame,
           "The first frame of the shot.", 
           1); 
        addParam(param);
      }
      
      {
        UtilityParam param = 
          new IntegerUtilityParam
          (ParamNames.aEndFrame,
           "The last frame of the shot.", 
           24); 
        addParam(param);
      }
 
      {
        UtilityParam param = 
          new PlaceholderUtilityParam
          (aChars, 
           "Which characters are included in the shot."); 
        addParam(param);
      }
      
      {
        UtilityParam param = 
          new PlaceholderUtilityParam
          (aEnvs, 
           "Which sets are included in the shot."); 
        addParam(param);
      }
      
      {
        UtilityParam param = 
          new PlaceholderUtilityParam
          (aProps, 
           "Which props are included in the shot."); 
        addParam(param);
      }
      
      {
        UtilityParam param = 
          new PlaceholderUtilityParam
          (aCamera, 
           "Which camera should be in the shot."); 
        addParam(param);
      }
      
      pProjectNamer = new ProjectNamer(mclient, qclient);
      addSubBuilder(pProjectNamer);
      addMappedParam(pProjectNamer.getName(), new ParamMapping(ParamNames.aProjectName), 
                     aProjectMapping);
      
    }
    
    setDefaultEditors(StudioDefinitions.getDefaultEditors());
    
    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Builder Information", 
           "The pass where all the basic stageInformation about the shot is collected " +
           "from the user.", 
           "Builder Settings", 
           true);
      {
        layout.addColumn("Shot Information", true);
        layout.addEntry(1, aUtilContext);
        layout.addEntry(1, null);
        layout.addEntry(1, aCheckinWhenDone);
        layout.addEntry(1, aActionOnExistence);
        layout.addEntry(1, aReleaseOnError);
        layout.addEntry(2, ParamNames.aLocation);
        layout.addEntry(2, ParamNames.aStartFrame);
        layout.addEntry(2, ParamNames.aEndFrame);
        layout.addSeparator(2);
        layout.addEntry(2, ParamNames.aRenderer);
        
        LayoutGroup mayaGroup = 
          new LayoutGroup("MayaGroup", "Parameters related to Maya scenes", true);

        mayaGroup.addEntry(ParamNames.aMayaContext);
        
        layout.addSubGroup(1, mayaGroup);

      }
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      
      {
        AdvancedLayoutGroup layout2 = 
          new AdvancedLayoutGroup
          ("Asset Information", 
           "What assets are in the shot.", 
           "Characters", 
           true);
        
        layout2.addColumn("Props", true);
        layout2.addColumn("Env", true);
        layout2.addColumn("Camera", true);
        
         LayoutGroup charGroup =
           new LayoutGroup("Characters", "List of characters in the shot", true);
         charGroup.addEntry(aChars);
         layout2.addSubGroup(1, charGroup);
         
         LayoutGroup propGroup =
           new LayoutGroup("Props", "List of props in the shot", true);
         propGroup.addEntry(aProps);
         layout2.addSubGroup(2, propGroup);
         
         LayoutGroup setGroup =
           new LayoutGroup("Env", "List of environments in the shot", true);
         setGroup.addEntry(aEnvs);
         layout2.addSubGroup(3, setGroup);
         
         layout2.addEntry(4, aCamera);
         
         finalLayout.addPass(layout2.getName(), layout2);
         setLayout(finalLayout);
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class SetupEssentialsPass
    extends SetupPass
  {
    public 
    SetupEssentialsPass()
    {
      super("Setup Essentials", 
            "The First Information pass for the CurveShotBuilder");
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
      
      int start = getIntegerParamValue(
        new ParamMapping(ParamNames.aStartFrame), new Range<Integer>(0, null));
      int end = getIntegerParamValue(
        new ParamMapping(ParamNames.aEndFrame), new Range<Integer>(start + 1, null));
      
      pFrameRange = new FrameRange(start, end, 1);
      
      pRenderer = 
        getStringParamValue(new ParamMapping(ParamNames.aRenderer), false);
      
      pRequiredNodes = new TreeSet<String>();
      if (pRenderer.equals(ParamNames.aMentalRay))
        pRequiredNodes.add(pProjectNamer.getLightingMRayGlobalsMEL());
      else
        pRequiredNodes.add(pProjectNamer.getLightingMayaGlobalsMEL());
    }
    
    @Override
    public void 
    initPhase() 
      throws PipelineException
    {
      pProject    = (String) getParamValue(aProjectMapping);
      String spot = (String) getParamValue(aSpotMapping);
      String shot = (String) getParamValue(aShotMapping);
      
      pShotNamer = new ShotNamer(pClient, pQueue);
      
      addSubBuilder(pShotNamer);
      
      addMappedParam(pShotNamer.getName(), 
                     new ParamMapping(ParamNames.aProjectName), 
                     aProjectMapping);
      
      if (!spot.equals(StudioDefinitions.aNEW))  {
        addMappedParam(pShotNamer.getName(), 
                       new ParamMapping(ParamNames.aSpotName),
                       aSpotMapping);
      }

      if (!shot.equals(StudioDefinitions.aNEW))  {
        addMappedParam(pShotNamer.getName(), 
                       new ParamMapping(ParamNames.aShotName),
                       aShotMapping);
      }

      MappedArrayList<String, String> assets = 
        pStudioDefinitions.getAssetList(pProject, AssetType.titles());
      {
        ArrayList<String> chars = assets.get(AssetType.character.toTitle());
        if (chars == null)
          chars = new ArrayList<String>();
        UtilityParam param =
          new ListUtilityParam
          (aChars, 
           "Which characters are included in the shot.",
           new TreeSet<String>(),
           new TreeSet<String>(chars), 
           null,
           null);
        replaceParam(param);
      }
      
      {
        ArrayList<String> props = assets.get(AssetType.prop.toTitle());
        if (props == null)
          props = new ArrayList<String>();
        UtilityParam param =
          new ListUtilityParam
          (aProps, 
           "Which props are included in the shot.",
           new TreeSet<String>(),
           new TreeSet<String>(props), 
           null,
           null);
        replaceParam(param);
      }
      
      {
        ArrayList<String> envs = assets.get(AssetType.env.toTitle());
        if (envs == null)
          envs = new ArrayList<String>();
        UtilityParam param =
          new ListUtilityParam
          (aEnvs, 
           "Which environments are included in the shot.",
           new TreeSet<String>(),
           new TreeSet<String>(envs), 
           null,
           null);
        replaceParam(param);
      }
      
      ArrayList<String> cams = assets.get(AssetType.cam.toTitle());
      if (cams == null || cams.isEmpty()) {
        CameraAssetBuilder camBuilder = 
          new CameraAssetBuilder
            (pClient, pQueue, getBuilderInformation(), pStudioDefinitions, "renderCam");
        addSubBuilder(camBuilder);
        String name = camBuilder.getName();
        addMappedParam(name, ParamNames.aMayaContext, ParamNames.aMayaContext);
        camBuilder.setParamValue(aCheckinWhenDone, true);
        camBuilder.disableParam(new ParamMapping(aCheckinWhenDone));
        addMappedParam(name, 
                       new ParamMapping(ParamNames.aProjectName), 
                       aProjectMapping);
        pCameraNames = 
          AssetNamer.getGeneratedNamer(pClient, pQueue, pProject, "renderCam", AssetType.cam);
        disableParam(new ParamMapping(aCamera));
      }
      else {
        UtilityParam param =
          new EnumUtilityParam
          (aCamera, 
           "Which camera to use in the shot.",
           cams.get(0),
           cams);
        replaceParam(param);
      }
    }
    private static final long serialVersionUID = 3418072626330048643L;
  }
  
  private 
  class AssetInfoPass
    extends SetupPass
  {
    public 
    AssetInfoPass()
    {
      super("Asset Info", 
            "The Asset Information pass for the CurveShotBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      pAssets = new TreeMap<String, AssetBundle>();
      TreeSet<String> names = new TreeSet<String>();
      
      if (pCameraNames == null) {
        String cameraName = (String) getParamValue(aCamera);
        pCameraNames = 
          AssetNamer.getGeneratedNamer(pClient, pQueue, pProject, cameraName, AssetType.cam);
      }
      pAssets.put(pCameraNames.getNamespace(), new AssetBundle(pCameraNames));
      
      
      {
        TreeSet<String> chars = (TreeSet<String>) getParamValue(aChars);
        for (String each : chars) {
          AssetNamer namer = 
            AssetNamer.getGeneratedNamer(pClient, pQueue, pProject, each, AssetType.character);
          if (pAssets.put(namer.getNamespace(), new AssetBundle(namer)) != null)
            throw new PipelineException
              ("Two assets with the namespace (" + namer.getNamespace()+ ") were specified " +
               "for the Shot Builder.  There cannot be two assets with the same namespace " +
               "in a shot.");
          String name = namer.getAssetName();
          if (names.contains(name)) 
            throw new PipelineException
              ("Two assets with the name (" + name + ") were specified for the Shot Builder.  " +
               "You cannot have a shot with two identically named assets.");
          names.add(name);
        }
      }
      
      {
        TreeSet<String> props = (TreeSet<String>) getParamValue(aProps);
        for (String each : props) {
          AssetNamer namer = 
            AssetNamer.getGeneratedNamer(pClient, pQueue, pProject, each, AssetType.prop);
          if (pAssets.put(namer.getNamespace(), new AssetBundle(namer)) != null)
            throw new PipelineException
              ("Two assets with the namespace (" + namer.getNamespace()+ ") were specified " +
               "for the Shot Builder.  There cannot be two assets with the same namespace " +
               "in a shot.");
          String name = namer.getAssetName();
          if (names.contains(name)) 
            throw new PipelineException
              ("Two assets with the name (" + name + ") were specified for the Shot Builder.  " +
               "You cannot have a shot with two identically named assets.");
          names.add(name);
        }
      }
      
      {
        TreeSet<String> envs = (TreeSet<String>) getParamValue(aEnvs);
        for (String each : envs) {
          AssetNamer namer = 
            AssetNamer.getGeneratedNamer(pClient, pQueue, pProject, each, AssetType.env);
          if (pAssets.put(namer.getNamespace(), new AssetBundle(namer)) != null)
            throw new PipelineException
              ("Two assets with the namespace (" + namer.getNamespace()+ ") were specified " +
               "for the Shot Builder.  There cannot be two assets with the same namespace " +
               "in a shot.");
          String name = namer.getAssetName();
          if (names.contains(name)) 
            throw new PipelineException
              ("Two assets with the name (" + name + ") were specified for the Shot Builder.  " +
               "You cannot have a shot with two identically named assets.");
          names.add(name);
        }
      }
      pTaskName = pShotNamer.getTaskName();
    }
    private static final long serialVersionUID = -2978221999265366470L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class MakeNodesPass
    extends ConstructPass
  {
    public 
    MakeNodesPass()
    {
      super("Make Nodes", 
            "Constructs the node networks.");
    }
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      return pRequiredNodes;
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      pFinalizeStages = new LinkedList<FinalizableStage>();
      pTextureNodes = new TreeSet<String>();
      pAnimNodes = new TreeMap<String, String>();
      pLgtNodes = new TreeMap<String, String>();
      
      pStageInfo = getStageInformation();
      lockAssets();
      buildAnimation();
      buildLighting();
    }
    
    private void
    lockAssets()
      throws PipelineException
    {
      for (String nameSpace : pAssets.keySet()) {
        AssetBundle bundle = pAssets.get(nameSpace);
        AssetClass ac = bundle.getAssetClass();
        AssetNamer namer = bundle.getAssetNamer();
        switch (ac) {
        case Camera:
          lockLatest(namer.getAssetProductShortScene());
          frozenStomp(namer.getTextureProductNode());
          pAnimNodes.put(nameSpace, pCameraNames.getAssetProductShortScene());
          pLgtNodes.put(nameSpace, pCameraNames.getAssetProductShortScene());
          break;
        case SimpleAsset:
          lockLatest(namer.getAssetProductScene());
          pAnimNodes.put(nameSpace, namer.getAssetProductScene());
          pLgtNodes.put(nameSpace, namer.getAssetProductScene());
          break;
        case Asset:
          lockLatest(namer.getRigProductScene());
          lockLatest(namer.getShadeProductScene());
          pAnimNodes.put(nameSpace, namer.getRigProductScene());
          pLgtNodes.put(nameSpace, namer.getShadeProductScene());
          break;
        }
        frozenStomp(namer.getTextureProductNode());
        pTextureNodes.add(namer.getTextureProductNode());
      }
    }
    
    private void
    buildAnimation()
      throws PipelineException
    {
      String type = TaskType.Animation.toTitle();
      
      ActionOnExistence cache = pStageInfo.getActionOnExistence();
      
      String animEdit = pShotNamer.getAnimEditScene();
      {
        pStageInfo.setActionOnExistence(ActionOnExistence.CheckOut);
        AnimEditStage stage = 
          new AnimEditStage
          (pStageInfo, pContext, pClient, pMayaContext,
           animEdit, pAnimNodes, pFrameRange);
        addTaskAnnotation(stage, NodePurpose.Edit, pProject, pTaskName, type);
        if (stage.build())
          addToDisableList(animEdit);
        pStageInfo.setActionOnExistence(cache);
      }

      TreeMap<String, String> animPrepareFiles = new TreeMap<String, String>();
      pAnimProductFiles = new TreeMap<String, String>();

      for (String nameSpace : pAssets.keySet()) {
        AssetNamer namer = pAssets.get(nameSpace).getAssetNamer();
        String assetName = namer.getAssetName();
        AssetType assetType = namer.getAssetType();
        String animPrepare = pShotNamer.getAnimCurveExportNode(assetName, assetType);
        String animProduct = pShotNamer.getAnimCurveProductNode(assetName, assetType);
        
        {
          AnimCurveExportStage stage = 
            new AnimCurveExportStage
            (pStageInfo, pContext, pClient,
             animPrepare, animEdit, nameSpace + ":SELECT");
          addTaskAnnotation(stage, NodePurpose.Prepare, pProject, pTaskName, type);
          stage.build();
        }
        {
          ProductStage stage = 
            new ProductStage
            (pStageInfo, pContext, pClient, 
             animProduct, "ma", animPrepare, 
             StageFunction.aMayaScene.toString());
          addTaskAnnotation(stage, NodePurpose.Product, pProject, pTaskName, type);
          stage.build();
        }
        animPrepareFiles.put(nameSpace, animPrepare);
        pAnimProductFiles.put(nameSpace, animProduct);
      }
      String animVerify = pShotNamer.getAnimVerifyScene();
      {
        pStageInfo.setActionOnExistence(ActionOnExistence.Conform);
        AnimVerifyStage stage = 
          new AnimVerifyStage
          (pStageInfo, pContext, pClient, pMayaContext,
           animVerify, pAnimNodes, animPrepareFiles,
           null, pFrameRange);
        addTaskAnnotation(stage, NodePurpose.Focus, pProject, pTaskName, type);
        stage.build();
        pStageInfo.setActionOnExistence(cache);
      }
      String animSubmit = pShotNamer.getAnimSubmitNode();
      {
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, animSubmit, animVerify);
        addSubmitTaskAnnotation(stage, pProject, pTaskName, type);
        if (stage.build()) {
          addToQueueList(animSubmit);
          addToCheckInList(animSubmit);
        }
      }
      String animApprove = pShotNamer.getAnimApproveNode();
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.addAll(pAnimProductFiles.values());
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, animApprove, sources);
        addApproveTaskAnnotation(stage, pProject, pTaskName, type, 
          new BuilderID("BaseBuilders", new VersionID("1.0.0"), "NathanLove", 
                        "AnimApproveTask"));

        if (stage.build()) {
          addToQueueList(animApprove);
          addToCheckInList(animApprove);
        }
      }
    }
    
    private void
    buildLighting()
      throws PipelineException
    {
      ActionOnExistence cache = pStageInfo.getActionOnExistence();
      
      String type = TaskType.Lighting.toTitle();
      LockBundle bundle = new LockBundle();
      for (String node : pAnimProductFiles.values()) 
        bundle.addNodeToLock(node);
      
      String textureNode = pShotNamer.getLightingTextureNode();
      {
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, textureNode, pTextureNodes);
        addTaskAnnotation(stage, NodePurpose.Edit, pProject, pTaskName, type);
        stage.build();
      }
      String preLightScene = pShotNamer.getPreLightScene();
      {
        pStageInfo.setActionOnExistence(ActionOnExistence.Conform);
        PreLightStage stage = 
          new PreLightStage
          (pStageInfo, pContext, pClient, pMayaContext,
            preLightScene, pLgtNodes, pAnimProductFiles, textureNode,
           null, pFrameRange);
        addTaskAnnotation(stage, NodePurpose.Edit, pProject, pTaskName, type);
        stage.build();
        pStageInfo.setActionOnExistence(cache);
      }
      String lightingScene = pShotNamer.getLightingEditScene();
      {
        pStageInfo.setActionOnExistence(ActionOnExistence.CheckOut);
        LightingEditStage stage = 
          new LightingEditStage
          (pStageInfo, pContext, pClient, pMayaContext,
           lightingScene, preLightScene, pFrameRange);
        addTaskAnnotation(stage, NodePurpose.Edit, pProject, pTaskName, type);
        if (stage.build())
          addToDisableList(lightingScene);
        pStageInfo.setActionOnExistence(cache);
      }
      String lightingRenderNode = pShotNamer.getLightingRenderNode();
      {
        String globals;
        if (pRenderer.equals(ParamNames.aMentalRay))
          globals = pProjectNamer.getLightingMRayGlobalsMEL();
        else
          globals = pProjectNamer.getLightingMayaGlobalsMEL();
        LightingRenderStage stage =
            new LightingRenderStage
            (pStageInfo, pContext, pClient, 
             lightingRenderNode, lightingScene, globals,
             pRenderer, pFrameRange);
        addTaskAnnotation(stage, NodePurpose.Focus, pProject, pTaskName, type);
        stage.build();
      }
      String lightingSubmit = pShotNamer.getLightingSubmitNode();
      {
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, lightingSubmit, lightingRenderNode);
        addSubmitTaskAnnotation(stage, pProject, pTaskName, type);
        if (stage.build()) {
          addToQueueList(lightingSubmit);
          addToCheckInList(lightingSubmit);
          bundle.addNodeToCheckin(lightingSubmit);
        }
      }
      String lightingProduct = pShotNamer.getLightingProductScene();
      String textureProduct  = pShotNamer.getLightingTextureProductNode();
      String lightingApprove = pShotNamer.getLightingApproveNode();
      {
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, textureProduct, pTextureNodes);
        addTaskAnnotation(stage, NodePurpose.Product, pProject, pTaskName, type);
        stage.build();
      }
      {
        pStageInfo.setActionOnExistence(ActionOnExistence.Conform);
        String script = pProjectNamer.getLightingProductMEL();
        LightingProductStage stage = 
          new LightingProductStage
          (pStageInfo, pContext, pClient,
           lightingProduct, lightingScene, script, pLgtNodes.values(), textureProduct );
        addTaskAnnotation(stage, NodePurpose.Product, pProject, pTaskName, type);
        if (stage.build()) 
          pFinalizeStages.add(stage);
        pStageInfo.setActionOnExistence(cache);
      }
      {
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, lightingApprove, lightingProduct);
        addApproveTaskAnnotation(stage, pProject, pTaskName, type, 
          new BuilderID("BaseBuilders", new VersionID("1.0.0"), "NathanLove", 
                        "LgtApproveTask"));

        if (stage.build()) {
          addToQueueList(lightingApprove);
          addToCheckInList(lightingApprove);
        }
      }
    }
    
    private static final long serialVersionUID = 2079737341001263099L;
    
    private StageInformation pStageInfo;
    private TreeSet<String> pTextureNodes;
    private TreeMap<String, String> pAnimNodes;
    private TreeMap<String, String> pLgtNodes;
    private TreeMap<String, String> pAnimProductFiles;
  }
  
  private
  class FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("FinalizePass", 
            "The pass that cleans everything up.");
    }
    
    @Override
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> regenerate = new LinkedList<String>();

      regenerate.addAll(getDisableList());
      for(FinalizableStage stage : pFinalizeStages) 
        regenerate.add(stage.getNodeName());

      return regenerate;
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      for(FinalizableStage stage : pFinalizeStages) 
        stage.finalizeStage();
      disableActions();
    }    
    private static final long serialVersionUID = 3517173255367921806L;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aChars  = "Chars";
  public final static String aProps  = "Props";
  public final static String aEnvs   = "Envs";
  public final static String aCamera = "Camera";
  
  
  private static final ParamMapping aProjectMapping = 
    new ParamMapping(ParamNames.aLocation, ParamNames.aProjectName); 
  
  private static final ParamMapping aSpotMapping = 
    new ParamMapping(ParamNames.aLocation, ParamNames.aSpotName); 

  private static final ParamMapping aShotMapping = 
    new ParamMapping(ParamNames.aLocation, ParamNames.aShotName); 
  
  private static final long serialVersionUID = 1179249256087253761L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private ProjectNamer pProjectNamer;
  private ShotNamer    pShotNamer;
  
  private MayaContext pMayaContext;
  private FrameRange  pFrameRange;
  
  private String pTaskName;
  
  private TreeSet<String> pRequiredNodes;
  
  private TreeMap<String, AssetBundle> pAssets;
  
  private StudioDefinitions pStudioDefinitions;
  
  private String pProject;
  
  private AssetNamer pCameraNames = null;
  
  private String pRenderer;
  
  private LinkedList<FinalizableStage> pFinalizeStages;
}
