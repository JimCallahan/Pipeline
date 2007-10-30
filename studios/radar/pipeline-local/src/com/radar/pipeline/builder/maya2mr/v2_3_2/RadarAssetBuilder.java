package com.radar.pipeline.builder.maya2mr.v2_3_2;


import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.AssetBuilder;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.AssetModelExportStage;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.stages.EmptyMayaAsciiStage;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   A S S E T   B U I L D E R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The Radar Films Asset Builder
 * <p>
 * Note, due to how the blend shape nodes are being generated, using this builder with
 * ActionOnExistence set to Conform will result in undesirable behavior.  So don't do it.
 */
public 
class RadarAssetBuilder
  extends AssetBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  RadarAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    super
      (mclient,
       qclient,
       new RadarBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)), 
       new RadarAssetNames(mclient, qclient),
       new RadarProjectNames(mclient, qclient),
       info);
    
    pRadarNames = (RadarAssetNames) pAssetNames;
    
    setParamValue(aModelDelivery, "Export");
    disableParam(new ParamMapping(aModelDelivery));
    disableParam(new ParamMapping(aReRigSetup));
    disableParam(new ParamMapping(aBuildTextureNode));
    disableParam(new ParamMapping(aVerifyShaderMEL));
    disableParam(new ParamMapping(aVerifyModelMEL));
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Overriden to change the default editors.
   */
  protected void
  setDefaultEditors()
  {
    setDefaultEditor(StageFunction.aMayaScene, new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.aNone, new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.aTextFile, new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.aScriptFile, new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.aRenderedImage, new PluginContext("ImfDisp"));
    setDefaultEditor(StageFunction.aSourceImage, new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.aMotionBuilderScene, new PluginContext("Jedit", "Radar"));
  }
  
  protected void
  addConstructPasses()
    throws PipelineException
  {
    ConstructPass build = new RadarBuildPass();
    addConstructPass(build);
    ConstructPass end = new FinalizePass();
    addConstructPass(end);
    addPassDependency(build, end);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected MappedArrayList<String, PluginContext> 
  getNeededActions()
  {
    ArrayList<PluginContext> plugins = new ArrayList<PluginContext>();
    plugins.add(new PluginContext("MayaExport", "Temerity", new Range<VersionID>(new VersionID("2.3.10"), null)));
    MappedArrayList<String, PluginContext> toReturn = new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);
    return toReturn;
  }
  
  private static final long serialVersionUID = -7603820481994992856L;
  
  protected RadarAssetNames pRadarNames;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class RadarBuildPass
    extends AssetBuilder.BuildPass
  {
    public 
    RadarBuildPass()
    {
      super();
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
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the build phase in the Build Pass");
      doModel();
      if (pHasBlendShapes)
        doBlends();
      doRig();
      doMaterials();
    }
    
    protected void
    doBlends()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Finer, 
      "Custom radar blend shape settings");
      String editBlend = pRadarNames.getBlendShapeEditModelNodeName();
      String finalBlend = pRadarNames.getBlendShapeModelNodeName();
      {
        EmptyMayaAsciiStage stage = 
          new EmptyMayaAsciiStage
          (pStageInfo,
           pContext,
           pClient,
           pMayaContext,
           editBlend);
        stage.build();
        pEmptyMayaScenes.add(stage);
      }
      {
	AssetModelExportStage stage = 
	  new AssetModelExportStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   finalBlend,
	   editBlend,
	   "BLEND",
	   null);
        stage.build();
      }
    }
    private static final long serialVersionUID = 1110297173584863863L;
  }
}
