package com.laika.pipeline.builder.maya2mr.v2_3_2;


import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.*;
import us.temerity.pipeline.math.Range;

/*------------------------------------------------------------------------------------------*/
/*   L A I K A   A S S E T   B U I L D E R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The Laika Films Asset Builder
 * <p>
 * Note, due to how the blend shape nodes are being generated, using this builder with
 * ActionOnExistance set to Conform will result in undesirable behavior.  So don't do it.
 */
public 
class LaikaAssetBuilder
  extends AssetBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  LaikaAssetBuilder
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
       new DefaultAssetNames(mclient, qclient),
       new DefaultProjectNames(mclient, qclient),
       info);
    
    pLaikaNames = (DefaultAssetNames) pAssetNames;
    
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
    setDefaultEditor(StageFunction.MayaScene.toString(), new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.None.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.TextFile.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.ScriptFile.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.RenderedImage.toString(), new PluginContext("Shake"));
    setDefaultEditor(StageFunction.SourceImage.toString(), new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.MotionBuilderScene.toString(), new PluginContext("Emacs"));
  }
  
  protected void
  addConstructPasses()
    throws PipelineException
  {
    ConstructPass build = new BuildPass();
    addConstuctPass(build);
    ConstructPass end = new FinalizePass();
    addConstuctPass(end);
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
  
  protected DefaultAssetNames pLaikaNames;
  
  private static final long serialVersionUID = -4219162335012375300L;
}
