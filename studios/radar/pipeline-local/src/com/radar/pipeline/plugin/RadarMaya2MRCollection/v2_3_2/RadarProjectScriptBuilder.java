package com.radar.pipeline.plugin.RadarMaya2MRCollection.v2_3_2;


import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.*;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   P R O J E C T   S C R I P T   B U I L D E R                                */
/*------------------------------------------------------------------------------------------*/

public 
class RadarProjectScriptBuilder
  extends ProjectScriptBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public 
  RadarProjectScriptBuilder
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
       new RadarProjectNames(mclient, qclient, info),
       new RadarBuilderAnswers(mclient, qclient, 
         UtilContext.getDefaultUtilContext(mclient), info.getLoggerName()), 
       info);
  }
  
  /**
   * Overriden to change the default editors.
   */
  protected void
  setDefaultEditors()
  {
    setDefaultEditor(StageFunction.aMayaScene, new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.aNone, new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.aTextFile, new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.aScriptFile, new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.aRenderedImage, new PluginContext("ImfDisp"));
    setDefaultEditor(StageFunction.aSourceImage, new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.aMotionBuilderScene, new PluginContext("JEdit", "Radar"));
  }
  
  private static final long serialVersionUID = 6620502540702923973L;
}
