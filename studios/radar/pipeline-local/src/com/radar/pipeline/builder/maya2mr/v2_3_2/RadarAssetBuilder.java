package com.radar.pipeline.builder.maya2mr.v2_3_2;


import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.AssetBuilder;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.DefaultBuilderAnswers;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   A S S E T   B U I L D E R                                                  */
/*------------------------------------------------------------------------------------------*/


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
       new DefaultBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)), 
       new RadarAssetNames(mclient, qclient),
       new RadarProjectNames(mclient, qclient),
       info);
  }
  
  /**
   * Overriden to change the default editors.
   */
  protected void
  setDefaultEditors()
  {
    setDefaultEditor(StageFunction.MayaScene.toString(), new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.None.toString(), new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.TextFile.toString(), new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.ScriptFile.toString(), new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.RenderedImage.toString(), new PluginContext("ImfDisp"));
    setDefaultEditor(StageFunction.SourceImage.toString(), new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.MotionBuilderScene.toString(), new PluginContext("JEdit", "Radar"));
  }
  private static final long serialVersionUID = -7603820481994992856L;
}
