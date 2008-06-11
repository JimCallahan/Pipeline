// $Id: RigEditStage.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/**
 * A leaf stage used in the AssetBuilder that builds the rig node.
 * <p>
 * This node is the rig stage of the asset process. It has a single source, the model node. It
 * uses the MayaBuild Action to import the model. Once it is disabled, the model file will
 * have to be imported by hand into this scene, every time it is changed.
 */
public 
class RigEditStage
  extends MayaBuildStage
  implements FinalizableStage
  {

  /**
   * This constructor will initialize the stage to generate the rig node.
   * 
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *        The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param modelName
   *        The name of the model node to link to the rig node.
   */
  public 
  RigEditStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    String modelName
  )
    throws PipelineException
  {
    super("RigEdit", 
          "Stage to build the rig scene", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName,
          true);
    setupLink(modelName, "rig", MayaBuildStage.getImport(), false);
  }
  
  public void 
  finalizeStage() 
    throws PipelineException
  {
    //removeAction();
    //vouch();
  }
  
  private static final long serialVersionUID = -6777269054052951292L;

}
