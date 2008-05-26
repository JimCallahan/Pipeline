// $Id: LightingProductStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   L I G H T I N G   P R O D U C T   S T A G E                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a scene for the Rendering task, which has the prelight and all the animation 
 * scenes imported, but retains references to the shaded models.
 */
public 
class LightingProductStage
  extends MayaMELStage
  implements FinalizableStage
{
  public
  LightingProductStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String source,
    String productMEL,
    Collection<String> assets,
    String textureNode
  )
    throws PipelineException
  {
   super("LightingProduct", 
         "Creates a scene for the Rendering task, which has the prelight and all the " +
         "animation scenes imported, but retains references to the shaded models.",
         stageInformation, context, client,
         nodeName, source, true, null);
   
   addScript(productMEL);
   
   pMelScript = productMEL;
   pSourceNode = source;
   pTextureNode = textureNode;
   pAssets = assets;
  }

  @Override
  public void 
  finalizeStage()
    throws PipelineException
  {
    NodeID id = new NodeID(getAuthor(), getView(), pRegisteredNodeName);
    removeAction();
    pClient.unlink(id, pSourceNode);
    pClient.unlink(id, pMelScript);
    
    {
      LinkMod mod = new LinkMod(pTextureNode, LinkPolicy.Association);
      pClient.link(id, mod);
    }
    
    for (String asset : pAssets) {
      LinkMod mod = new LinkMod(asset, LinkPolicy.Reference);
      pClient.link(id, mod);
    }
    vouch();
    
  }
  
  private String pSourceNode;
  private String pTextureNode;
  private String pMelScript;
  private Collection<String> pAssets;
  
  private static final long serialVersionUID = -6313061530085538626L;
}
