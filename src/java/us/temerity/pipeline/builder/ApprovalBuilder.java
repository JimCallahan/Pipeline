package us.temerity.pipeline.builder;

import us.temerity.pipeline.*;
import us.temerity.pipeline.stages.BaseStage;

/**
 *  Abstract Builder Class that adds all the Temerity Submit/Approve functionality
 *  into the builder.   
 */
public abstract 
class ApprovalBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * @param name
   * @param vid
   * @param vendor
   * @param desc
   * @param mclient
   * @param qclient
   * @param builderInformation
   * @throws PipelineException
   */
  protected 
  ApprovalBuilder
  (
    String name,
    VersionID vid,
    String vendor,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super(name, vid, vendor, desc, mclient, qclient, builderInformation);
  }

  /*----------------------------------------------------------------------------------------*/
  /*  A N N O T A T I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/
  
  protected void
  isSubmitNode
  (
    BaseStage stage,
    String taskType,
    String approveNode
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("SubmitNodeAnnotation", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    annot.setParamValue("ApproveNode", approveNode);
    stage.addAnnotation("Submit", annot); 
  }
  
  protected void
  isIntermediateNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("IntermediateNodeAnnotation", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    stage.addAnnotation("Intermediate", annot); 
  }
  
  protected void
  isProductNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("ProductNodeAnnotation", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    stage.addAnnotation("Product", annot); 
  }
  
  protected void
  isEditNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("EditNodeAnnotation", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    stage.addAnnotation("Edit", annot); 
  }
  
  protected void
  isFocusNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("FocusNodeAnnotation", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    stage.addAnnotation("Intermediate", annot); 
  }
  
  protected void
  isApprovalNode
  (
    BaseStage stage,
    String taskType,
    String classPath
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("ApprovalNodeAnnotation", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    stage.addAnnotation("Approval", annot);
    //TODO add in the right thing here.
  }
  
  protected String pTaskName;
}
