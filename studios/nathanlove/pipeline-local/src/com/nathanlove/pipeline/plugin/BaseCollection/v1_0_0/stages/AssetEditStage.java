// $Id: AssetEditStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   E D I T   S T A G E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Builds the edit node for the SimpleAsset.
 */
public 
class AssetEditStage
  extends MayaBuildStage
  implements FinalizableStage
{
  public
  AssetEditStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName, 
    String texName,
    String placeHolderMel
  )
    throws PipelineException
  {
    super("AssetEdit", 
          "Stage to build the version of an asset stage the artist works on.", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true);
    
    pPlaceHolderMel = placeHolderMel;
    setInitialMel(pPlaceHolderMel);
    
    if (texName != null)
      setupLink(texName, "tex", MayaBuildStage.getReference(), true);
  }
  
  public void 
  finalizeStage() 
    throws PipelineException
  {
    removeAction();
    if(pPlaceHolderMel != null)
      if (pRegisteredNodeMod.getSourceNames().contains(pPlaceHolderMel))
        pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, pPlaceHolderMel);
    vouch();
  }
  
  private static final long serialVersionUID = -1379427184063221978L;
  private String pPlaceHolderMel;
}
