// $Id: ShadeApproveTaskBuilder.java,v 1.2 2008/06/26 20:45:55 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.ApprovalCollection.v2_4_1.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A D E   A P P R O V E   T A S K   B U I L D E R                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Approval builder for the Shading task, which takes care of copying textures from the edit
 * node to the final texture node.
 */
public 
class ShadeApproveTaskBuilder
  extends ApproveTaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Required constructor to launch the builder.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param builderInfo
   *   Information that is shared among all builders in a given invocation.
   */ 
  public
  ShadeApproveTaskBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("ShadeApproveTask",
         "A builder which implements the shade task approval operation.", 
         mclient, qclient, builderInfo);
    
    addDefaultParams();
    
    addSetupPass(new ApprovalSetup());
    addConstructPass(new UpdateNetworks());
    
    setDefaultEditors(StudioDefinitions.getDefaultEditors());
    
    setLayout(getDefaultLayoutGroup());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class ApprovalSetup
    extends LookupAndValidate
  {
    public 
    ApprovalSetup()
    {
      super();
    }
    
    /**
     * Phase in which parameter values should be extracted from parameters and checked
     * for consistency and applicability.
     */
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      super.validatePhase();
      
      pAssetNames = 
        AssetNamer.getNamerFromNodeName(pSubmitNode, getMasterMgrClient(), getQueueMgrClient());
    }
    private static final long serialVersionUID = -4186029807770743040L;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected
  class UpdateNetworks
    extends ConstructPass
  {
    public 
    UpdateNetworks()
    {
      super("Update Networks", 
            "Check-out the approve and submit node networks, update the texture node, " +
            "regenerate any stale nodes in the approval network and check-in the changes.");
    }
    
    /**
     * Check-out the latest approve and submit node networks, regenerated anything stale
     * in the approve network and check-in the changes. 
     */ 
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pSubmitNode);
      pClient.checkOut(getAuthor(), getView(), pSubmitNode, null, 
                       CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);

      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pApproveNode);
      pClient.checkOut(getAuthor(), getView(), pApproveNode, null, 
                       CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      
      String textureNode = pAssetNames.getTextureEditNode();
      String finalTexNode = pAssetNames.getTextureProductNode();
      NodeID finalTexID = new NodeID(getAuthor(), getView(), finalTexNode);
      NodeMod texMod = pClient.getWorkingVersion(getAuthor(), getView(), textureNode);
      NodeMod finalTexMod = pClient.getWorkingVersion(finalTexID);
      
      for (String source : finalTexMod.getSourceNames()) {
        pClient.unlink(finalTexID, source);
      }
      for (LinkMod link : texMod.getSources()) {
        pClient.link(finalTexID, link);
      }

      addToQueueList(pApproveNode);
      addToCheckInList(pApproveNode);
    }
    private static final long serialVersionUID = 8540275476705668113L;
  }

  
  private AssetNamer pAssetNames;
  
  private static final long serialVersionUID = 5481125794482969903L;
}

