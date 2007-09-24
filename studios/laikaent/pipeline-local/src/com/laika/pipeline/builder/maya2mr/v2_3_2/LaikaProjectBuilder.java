package com.laika.pipeline.builder.maya2mr.v2_3_2;


import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.*;

/*------------------------------------------------------------------------------------------*/
/*   L A I K A   P R O J E C T   B U I L D E R                                              */
/*------------------------------------------------------------------------------------------*/

public 
class LaikaProjectBuilder
  extends ProjectBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  LaikaProjectBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    super(mclient,
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
    setDefaultEditor(StageFunction.MayaScene.toString(), new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.None.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.TextFile.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.ScriptFile.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.RenderedImage.toString(), new PluginContext("Shake"));
    setDefaultEditor(StageFunction.SourceImage.toString(), new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.MotionBuilderScene.toString(), new PluginContext("Emacs"));
  }
  private static final long serialVersionUID = 3594431597508581966L;
}
