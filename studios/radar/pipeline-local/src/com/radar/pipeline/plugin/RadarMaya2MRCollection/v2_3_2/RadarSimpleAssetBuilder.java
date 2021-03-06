package com.radar.pipeline.plugin.RadarMaya2MRCollection.v2_3_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.*;

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
       new RadarBuilderAnswers(mclient, qclient, 
         UtilContext.getDefaultUtilContext(mclient), info.getLoggerName()), 
       new RadarAssetNames(mclient, qclient, info),
       new RadarProjectNames(mclient, qclient, info),
       info);
  }
  
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
  
  private static final long serialVersionUID = 5771734469057685916L;
}
