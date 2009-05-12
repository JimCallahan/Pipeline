// $Id: AnimApproveTaskBuilder.java,v 1.4 2009/05/12 03:22:29 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.plugin.ApprovalCollection.v2_4_1.*;
import us.temerity.pipeline.stages.*;

/**
 * Approval builder for the Animation task, which takes care of creating new product animation
 * nodes and removing ones which are no longer being used.
 */
public 
class AnimApproveTaskBuilder
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
  AnimApproveTaskBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("AnimApproveTask",
         "A builder which implements the animation task approval operation.", 
         mclient, qclient, builderInfo);
    
    addDefaultParams();
    
    addReleaseViewParam();
    
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
      
      pShotNamer = 
        ShotNamer.getNamerFromNodeName(pSubmitNode, getMasterMgrClient(), getQueueMgrClient());
    }
    private static final long serialVersionUID = -8739435007934849684L;
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
      frozenStomp(pSubmitNode);
      
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pApproveNode);
      pClient.checkOut(getAuthor(), getView(), pApproveNode, null, 
                       CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      
      /* Find the animation node. */
      String animEdit = pShotNamer.getAnimEditScene();
      NodeStatus aeStatus = 
        pClient.status(getAuthor(), getView(), animEdit, true, DownstreamMode.None);
      TreeSet<String> prepareNodes = new TreeSet<String>();
      
      /* Find all the animation being exported from that node.*/
      for (String target : aeStatus.getTargetNames() ) {
        TreeMap<String, BaseAnnotation> byPurpose = new TreeMap<String, BaseAnnotation>();
        String values[] = lookupTaskAnnotations(target, byPurpose);
        if (values == null)
          throw new PipelineException
            ("There were no valid Task Extensions on the node (" + target +").");
        validateTaskAnnotation("unknown", values[0], values[1], values[2]);
        if (byPurpose.containsKey(NodePurpose.Prepare))
          prepareNodes.add(target);
      }
      
      /* Make sure all the product nodes exist for all the prepare nodes.*/
      TreeSet<String> productNodes = new TreeSet<String>();
      StageInformation stageInfo = getStageInformation();
      stageInfo.setActionOnExistence(ActionOnExistence.Conform);
      for (String prepareNode : prepareNodes) {
        String productNode = 
          prepareNode.replace(SubDir.prepare.toString(), SubDir.product.toString());
        
        ProductStage stage = 
          new ProductStage
          (stageInfo, pContext, pClient, 
           productNode, "ma", prepareNode, 
           StageFunction.aMayaScene.toString());
        addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, pTaskType);
        stage.build();
        
        productNodes.add(productNode);
      }
      
      {
        TargetStage stage = 
          new TargetStage(stageInfo, pContext, pClient, pApproveNode, productNodes);
        addApproveTaskAnnotation(stage, pProjectName, pTaskName, pTaskType, 
          new BuilderID("BaseCollection", new VersionID("1.0.0"), "NathanLove", 
                        "AnimApproveTask"));
        stage.build();
      }
      
      addToQueueList(pApproveNode);
      addToCheckInList(pApproveNode);
    }
    private static final long serialVersionUID = -5095583179922571408L;
  }
  
  private static final long serialVersionUID = 7327970065329577137L;
  
  private ShotNamer pShotNamer;
}
