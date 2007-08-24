package com.radar.pipeline.builder.maya2mr.v2_3_2;


import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.ProjectTurntableBuilder;

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
    setDefaultEditor(StageFunction.MayaScene.toString(), new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.None.toString(), new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.TextFile.toString(), new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.ScriptFile.toString(), new PluginContext("JEdit", "Radar"));
    setDefaultEditor(StageFunction.RenderedImage.toString(), new PluginContext("ImfDisp"));
    setDefaultEditor(StageFunction.SourceImage.toString(), new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.MotionBuilderScene.toString(), new PluginContext("JEdit", "Radar"));
  }
  
  private static final long serialVersionUID = -1456116421042902749L;
}
