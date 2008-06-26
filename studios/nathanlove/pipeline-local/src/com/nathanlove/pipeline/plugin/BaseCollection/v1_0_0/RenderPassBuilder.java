// $Id: RenderPassBuilder.java,v 1.2 2008/06/26 20:45:55 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   R E N D E R   P A S S   B U I L D E R                                                  */
/*------------------------------------------------------------------------------------------*/

public 
class RenderPassBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Default Constructor for standalone invocation.
   * 
   * @param mclient
   *   The instance of Master Manager the builder will use. 
   * @param qclient 
   *   The instance of the Queue Manager the builder will use.
   * @param builderInformation 
   *   The globally shared builder information.
   * 
   */
  public
  RenderPassBuilder
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
  RenderPassBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefinitions studioDefinitions
  )
    throws PipelineException
  {
    super("RenderPass",
          "The builder to make render passes for an existing shot for the " +
          "Nathan Love Base Collection.",
          mclient, qclient, builderInformation, EntityType.Shot);
    
    pStudioDefinitions = studioDefinitions;
    
    DoubleMap<String, String, ArrayList<String>> all = pStudioDefinitions.getAllProjectsAllNames();
    Set<String> projects = all.keySet();
    if (projects == null || projects.isEmpty())
      throw new PipelineException
        ("Please create a project before running the pass builder.");
    boolean shot = false;
    for (String project : projects) {
      Set<String> spots = all.keySet(project);
      if (spots != null && !spots.isEmpty()) {
        for (String spot : spots) {
          ArrayList<String> shots = all.get(project, spot);
          if (shots != null && !shots.isEmpty()) {
            shot = true;
            break;
          }
        }
      }
      if (shot)
        break;
    }
    
    if (!shot)
      throw new PipelineException
        ("At least on shot must exist inside an existing project before the " +
         "Render Pass Builder can be run.");
    
    {
      /* select the project, sequence and shot for the task */ 
      UtilityParam param = 
        new DoubleMapUtilityParam(
            ParamNames.aLocation, 
            "The Project, Sequence, and Shot to put the Shot in.",
            ParamNames.aProjectName,
            "Select the name of the project",
            ParamNames.aSpotName,
            "Select the name of the spot.",
            ParamNames.aShotName,
            "Select the name of the shot.",
            all);
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
         "ParamNames.aMentalRay",
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
        new BooleanUtilityParam
        (aBuildScene,
         "Should a separate render scene be built for this pass.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (ParamNames.aPassName,
         "The name of the render pass.", 
         "beau"); 
      addParam(param);
    }
    
    pProjectNamer = new ProjectNamer(mclient, qclient);
    addSubBuilder(pProjectNamer);
    addMappedParam(pProjectNamer.getName(), ParamNames.aProjectName, ParamNames.aProjectName);
    
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
        layout.addEntry(2, ParamNames.aPassName);
        layout.addEntry(2, ParamNames.aRenderer);
        layout.addEntry(2, aBuildScene);
        
        LayoutGroup mayaGroup = 
          new LayoutGroup("MayaGroup", "Parameters related to Maya scenes", true);

        mayaGroup.addEntry(ParamNames.aMayaContext);
        
        layout.addSubGroup(1, mayaGroup);
      }
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
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
            "The First Information pass for the Render Pass Builder");
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
      
      pProject    =  getStringParamValue(aProjectMapping);
      String spot =  getStringParamValue(aSpotMapping);
      String shot =  getStringParamValue(aShotMapping);
      
      pShotNamer = ShotNamer.getGeneratedNamer(pClient, pQueue, pProject, spot, shot);
      
      pPassName = getStringParamValue(new ParamMapping(ParamNames.aPassName), false);
    }
    private static final long serialVersionUID = -943562265542298574L;
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
    public void 
    buildPhase()
      throws PipelineException
    {
      pFinalizeStages = new LinkedList<FinalizableStage>();
      pStageInfo = getStageInformation();
      
      String textureProduct = pShotNamer.getLightingTextureProductNode();
      frozenStomp(textureProduct);
      String lightingProduct = pShotNamer.getLightingProductScene();
      frozenStomp(lightingProduct);
      
      //String renderScene 
      
    }
    
    private StageInformation pStageInfo;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/

  public static final String aBuildScene = "BuildScene";
  
  private static final ParamMapping aProjectMapping = 
    new ParamMapping(ParamNames.aLocation, ParamNames.aProjectName); 
  
  private static final ParamMapping aSpotMapping = 
    new ParamMapping(ParamNames.aLocation, ParamNames.aSpotName); 

  private static final ParamMapping aShotMapping = 
    new ParamMapping(ParamNames.aLocation, ParamNames.aShotName); 

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private ProjectNamer pProjectNamer;
  private ShotNamer pShotNamer;
  
  private MayaContext pMayaContext;
  private FrameRange  pFrameRange;
  
  private String pTaskName;
  
  private String pPassName;
  
  private TreeSet<String> pRequiredNodes;
  
  private StudioDefinitions pStudioDefinitions;
  
  private String pProject;
  
  private String pRenderer;
  
  private LinkedList<FinalizableStage> pFinalizeStages;
}
