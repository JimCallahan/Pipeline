package com.radar.pipeline.builder.maya2mr.v2_3_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.SimpleAssetBuilder;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   S I M P L E   A S S E T   B U I L D E R                                    */
/*------------------------------------------------------------------------------------------*/

public 
class RadarSimpleAssetBuilder
  extends SimpleAssetBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  RadarSimpleAssetBuilder
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
  }
  
  /**
   * Overriden to change the default editors.
   */
  protected void
  setDefaultEditors()
  {
    setDefaultEditor(StageFunction.MayaScene.toString(), new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.None.toString(), new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.TextFile.toString(), new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.ScriptFile.toString(), new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.RenderedImage.toString(), new PluginContext("ImfDisp"));
    setDefaultEditor(StageFunction.SourceImage.toString(), new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.MotionBuilderScene.toString(), new PluginContext("Jedit", "Radar"));
  }
  
  private static final long serialVersionUID = 5771734469057685916L;
}
