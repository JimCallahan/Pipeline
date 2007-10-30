package com.laika.pipeline.builder.maya2mr.v2_3_2;


import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.*;

/*------------------------------------------------------------------------------------------*/
/*   L A I K A   P R O J E C T   S C R I P T   B U I L D E R                                */
/*------------------------------------------------------------------------------------------*/

public 
class LaikaProjectScriptBuilder
  extends ProjectScriptBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public 
  LaikaProjectScriptBuilder
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
       new DefaultProjectNames(mclient, qclient),
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
    setDefaultEditor(StageFunction.aNone, new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.aTextFile, new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.aScriptFile, new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.aRenderedImage, new PluginContext("Shake"));
    setDefaultEditor(StageFunction.aSourceImage, new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.aMotionBuilderScene, new PluginContext("Emacs"));
  }
  private static final long serialVersionUID = -6533560917878403068L;
}
