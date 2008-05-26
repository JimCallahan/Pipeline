// $Id: AssetVerifyStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   V E R I F Y   S T A G E                                                    */
/*------------------------------------------------------------------------------------------*/

public 
class AssetVerifyStage 
  extends MayaBuildStage
{

  public
  AssetVerifyStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName, 
    String scene,
    String verifyMelScript
  ) 
    throws PipelineException
  {
    super("AssetVerify", 
          "Stage to build the version of an asset stage that is submitted.", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true);
    setupLink(scene, "temp", MayaBuildStage.getImport(), false);
    setModelMel(verifyMelScript);
  }

  private static final long serialVersionUID = -5795408239165726810L;
}
