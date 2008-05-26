// $Id: LgtApproveTaskBuilder.java,v 1.1 2008/05/26 03:19:49 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.plugin.ApprovalCollection.v2_4_1.*;
import us.temerity.pipeline.stages.*;

import com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages.*;

/**
 * Approval builder for the Lighting task, which takes care of rebuilding the lighting product
 * node.
 */
public 
class LgtApproveTaskBuilder
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
  LgtApproveTaskBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("lgtApproveTask",
         "A builder which implements the lighting task approval operation.", 
         mclient, qclient, builderInfo);
    
    addDefaultParams();
    
    addSetupPass(new ApprovalSetup());
    addConstructPass(new UpdateNetworks());
    addConstructPass(new FinalizePass());
    
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
      
      pShotNamer = 
        ShotNamer.getNamerFromNodeName(pSubmitNode, getMasterMgrClient(), getQueueMgrClient());
      pProjectNamer = 
        ProjectNamer.getGeneratedNamer(pClient, pQueue, pProjectName);
      
      pRequiredNodes = new TreeSet<String>();
      pRequiredNodes.add(pProjectNamer.getLightingProductMEL());
    }
    private static final long serialVersionUID = -439893178053256066L;
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
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      return pRequiredNodes;
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
      frozenStomp(pSubmitNode);

      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pApproveNode);
      pClient.checkOut(getAuthor(), getView(), pApproveNode, null, 
                       CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      
      pFinalizeStages = new LinkedList<FinalizableStage>();
      
      String type = TaskType.Lighting.toString();
      
      
      String textureNode = pShotNamer.getLightingTextureNode();
      String textureProduct = pShotNamer.getLightingTextureProductNode();
      NodeID finalTexID = new NodeID(getAuthor(), getView(), textureProduct);
      NodeMod texMod = pClient.getWorkingVersion(getAuthor(), getView(), textureNode);
      NodeMod finalTexMod = pClient.getWorkingVersion(finalTexID);
      
      for (String source : finalTexMod.getSourceNames()) {
        pClient.unlink(finalTexID, source);
      }
      for (LinkMod link : texMod.getSources()) {
        pClient.link(finalTexID, link);
      }
      
      
      String prelgtNode = pShotNamer.getPreLightScene();
      NodeMod preLgtMod = pClient.getWorkingVersion(getAuthor(), getView(), prelgtNode);
      BaseAction preLgtAct = preLgtMod.getAction();
      
      TreeSet<String> lgtModels = new TreeSet<String>();
      for (String source : preLgtMod.getSourceNames()) {
       String sceneType = (String) preLgtAct.getSourceParamValue(source, "SceneType");
       if (sceneType != null && sceneType.equals("Model")) {
         lgtModels.add(source);
       }
      }
      
      String lightingScene = pShotNamer.getLightingEditScene();
      String lightingProduct = pShotNamer.getLightingProductScene();
      
      StageInformation stageInfo = getStageInformation();
      stageInfo.setActionOnExistence(ActionOnExistence.Conform);
      {
        String script = pProjectNamer.getLightingProductMEL();
        LightingProductStage stage = 
          new LightingProductStage
          (stageInfo, pContext, pClient,
           lightingProduct, lightingScene, script, lgtModels, textureProduct );
        addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
        if (stage.build()) 
          pFinalizeStages.add(stage);
      }

      addToQueueList(pApproveNode);
      addToCheckInList(pApproveNode);
    }
    private static final long serialVersionUID = -4320465056249070407L;
  }
  
  private
  class FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("FinalizePass", 
            "The pass that cleans everything up.");
    }
    
    @Override
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> regenerate = new LinkedList<String>();

      for(FinalizableStage stage : pFinalizeStages) 
        regenerate.add(stage.getNodeName());

      return regenerate;
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      for(FinalizableStage stage : pFinalizeStages) 
        stage.finalizeStage();
    }    
    private static final long serialVersionUID = 8159198786855459751L;
  }
  
  private static final long serialVersionUID = -3903489606862232041L;
  
  private ShotNamer pShotNamer;
  private ProjectNamer pProjectNamer;
  private TreeSet<String> pRequiredNodes;
  protected LinkedList<FinalizableStage> pFinalizeStages;
}
