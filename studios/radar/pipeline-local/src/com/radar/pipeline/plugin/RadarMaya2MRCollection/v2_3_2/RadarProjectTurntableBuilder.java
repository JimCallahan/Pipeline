package com.radar.pipeline.plugin.RadarMaya2MRCollection.v2_3_2;


import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.ProjectTurntableBuilder;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   P R O J E C T   T U R N T A B L E   B U I L D E R                          */
/*------------------------------------------------------------------------------------------*/

public 
class RadarProjectTurntableBuilder
  extends ProjectTurntableBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public 
  RadarProjectTurntableBuilder
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
       new RadarProjectNames(mclient, qclient),
       new RadarBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)), 
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

  
  private static final long serialVersionUID = -1456116421042902749L;
}
