// $Id: ApprovalBuilder.java,v 1.3 2007/07/24 07:25:15 jim Exp $

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
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  protected final void
  addDoAnnotationParam()
  {
    UtilityParam param = 
      new BooleanUtilityParam
      (aDoAnnotations, 
       "Should Task Annotations be added to all the nodes.", 
       false); 
    addParam(param);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  A N N O T A T I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/
  
  protected void
  isSubmitNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("SubmitNode", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    stage.addAnnotation("Submit", annot); 
  }
  
  protected void
  isPrepareNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("Task", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    annot.setParamValue("Purpose", "Prepare");
    stage.addAnnotation("Task", annot); 
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
      pPlug.newAnnotation("Task", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    annot.setParamValue("Purpose", "Product");
    stage.addAnnotation("Task", annot); 
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
      pPlug.newAnnotation("Task", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    annot.setParamValue("Purpose", "Edit");
    stage.addAnnotation("Task", annot); 
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
      pPlug.newAnnotation("Task", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    annot.setParamValue("Purpose", "Focus");
    stage.addAnnotation("Task", annot); 
  }
  
  protected void
  isApproveNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("Task", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    annot.setParamValue("Purpose", "Approve");
    stage.addAnnotation("Task", annot);
  }
  
  protected void
  isThumbnailNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      pPlug.newAnnotation("Task", new VersionID("2.3.2"), "Temerity");
    annot.setParamValue("TaskName", pTaskName);
    annot.setParamValue("TaskType", taskType);
    annot.setParamValue("Purpose", "Thumbnail");
    stage.addAnnotation("Task", annot);
  }
  
  protected String pTaskName;
  
  public static final String aDoAnnotations = "DoAnnotations";
}
