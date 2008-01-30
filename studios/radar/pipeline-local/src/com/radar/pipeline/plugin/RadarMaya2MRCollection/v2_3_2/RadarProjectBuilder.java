package com.radar.pipeline.plugin.RadarMaya2MRCollection.v2_3_2;


import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.DefaultBuilderAnswers;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.ProjectBuilder;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   P R O J E C T   B U I L D E R                                              */
/*------------------------------------------------------------------------------------------*/

public 
class RadarProjectBuilder
  extends ProjectBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  RadarProjectBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    super(mclient,
         qclient,
         new RadarProjectNames(mclient, qclient),
         new DefaultBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)), 
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

  
  private static final long serialVersionUID = -7932410944847347891L;
}
