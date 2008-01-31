// $Id: TaskBuilder.java,v 1.3 2008/01/31 17:29:11 jim Exp $

package us.temerity.pipeline.builder;

import us.temerity.pipeline.*;
import us.temerity.pipeline.stages.BaseStage;

/**
 *  Abstract Builder Class that adds all the Temerity Submit/Approve functionality
 *  into the builder.   
 */
public abstract 
class TaskBuilder
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
  TaskBuilder
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, builderInformation);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Helper method for adding the "doAnnotatons" parameter to the current builder.
   */ 
  protected final void
  addDoAnnotationParam()
  {
    UtilityParam param = 
      new BooleanUtilityParam
      (aDoAnnotations, 
       "Should Task Annotations be added to all the nodes.", 
       true); 
    addParam(param);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  A N N O T A T I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Adds the SubmitNode annotation to the set of annotation plugins which will be added 
   * to the node created by the given stage.<P> 
   * 
   * Submit nodes are used to group together all nodes required for a submitting a task
   * for supervisor approval.
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param taskType
   *   The value to give the TaskType parameter of the annotation.
   */ 
  protected void
  addSubmitAnnotation
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
  
  /** 
   * Adds the Task annotation with the Prepare Purpose to the set of annotation plugins 
   * which will be added to the node created by the given stage.<P> 
   * 
   * Prepare nodes are intermediary nodes used during the process of regenerating Focus
   * nodes for a task from changes made in the task's Edit nodes.
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param taskType
   *   The value to give the TaskType parameter of the annotation.
   */ 
  protected void
  addPrepareAnnotation
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
  
  /** 
   * Adds the Task annotation with the Product Purpose to the set of annotation plugins 
   * which will be added to the node created by the given stage.<P> 
   * 
   * Product nodes contain the data which will be used in downstream tasks build during
   * the post-approval process from approved changes made in the task's Edit nodes.
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param taskType
   *   The value to give the TaskType parameter of the annotation.
   */ 
  protected void
  addProductAnnotation
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
  
  /** 
   * Adds the Task annotation with the Edit Purpose to the set of annotation plugins 
   * which will be added to the node created by the given stage.<P> 
   * 
   * Edit nodes are those nodes interactively modified by the artist in order to complete
   * the task.
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param taskType
   *   The value to give the TaskType parameter of the annotation.
   */ 
  protected void
  addEditAnnotation
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
  
  /** 
   * Adds the Task annotation with the Focus Purpose to the set of annotation plugins 
   * which will be added to the node created by the given stage.<P> 
   * 
   * Focus nodes are those nodes reviewed by the supervisor(s) of the task in order to 
   * determine whether a task has been successfully completed.
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param taskType
   *   The value to give the TaskType parameter of the annotation.
   */ 
  protected void
  addFocusAnnotation
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
  
  /**   
   * Adds the Task annotation with the Approve Purpose to the set of annotation plugins 
   * which will be added to the node created by the given stage.<P> 
   * 
   * Approve nodes are used to group together all nodes which make of the post-approval
   * process for a task.
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param taskType
   *   The value to give the TaskType parameter of the annotation.
   */ 
  protected void
  addApproveAnnotation
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
  
  /** 
   * Adds the Task annotation with the Thumbnail Purpose to the set of annotation plugins 
   * which will be added to the node created by the given stage.<P> 
   * 
   * Thumbnail nodes create small reference images (typically generated from Focus nodes) 
   * which can be used by production tracking systems to represent the task.
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param taskType
   *   The value to give the TaskType parameter of the annotation.
   */ 
  protected void
  addThumbnailAnnotation
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
 
  
  /*-- DEPRECATED METHODS ------------------------------------------------------------------*/
     
  /** 
   * @deprecated 
   *   This method has been renamed to {@link #addSubmitAnnotation} and may be removed
   *   in future versions of Pipeline.
   */ 
  @Deprecated
  protected void
  isSubmitNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    addSubmitAnnotation(stage, taskType);
  }
   
  /** 
   * @deprecated 
   *   This method has been renamed to {@link #addPrepareAnnotation} and may be removed
   *   in future versions of Pipeline.
   */ 
  @Deprecated
  protected void
  isPrepareNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    addPrepareAnnotation(stage, taskType);
  }
  
  /** 
   * @deprecated 
   *   This method has been renamed to {@link #addProductAnnotation} and may be removed
   *   in future versions of Pipeline.
   */ 
  @Deprecated
  protected void
  isProductNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    addProductAnnotation(stage, taskType);
  }
  
  /** 
   * @deprecated 
   *   This method has been renamed to {@link #addEditAnnotation} and may be removed
   *   in future versions of Pipeline.
   */ 
  @Deprecated
  protected void
  isEditNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    addEditAnnotation(stage, taskType);
  }
  
  /** 
   * @deprecated 
   *   This method has been renamed to {@link #addFocusAnnotation} and may be removed
   *   in future versions of Pipeline.
   */ 
  @Deprecated
  protected void
  isFocusNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    addFocusAnnotation(stage, taskType);
  }
  
  /**   
   * @deprecated 
   *   This method has been renamed to {@link #addApproveAnnotation} and may be removed
   *   in future versions of Pipeline.
   */ 
  @Deprecated
  protected void
  isApproveNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    addApproveAnnotation(stage, taskType);
  }
  
  /** 
   * @deprecated 
   *   This method has been renamed to {@link #addThumbnailAnnotation} and may be removed
   *   in future versions of Pipeline.
   */ 
  @Deprecated
  protected void
  isThumbnailNode
  (
    BaseStage stage,
    String taskType
  )
    throws PipelineException
  {
    addThumbnailAnnotation(stage, taskType);
  }
 
  

  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aDoAnnotations = "DoAnnotations";


  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Name of the task being built.
   */ 
  protected String pTaskName;

}
