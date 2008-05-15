// $Id: TaskBuilder.java,v 1.2 2008/05/15 22:38:17 jesse Exp $

package us.temerity.pipeline.builder.v2_4_1;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 *  Abstract Builder Class providing shortcuts for adding Task Annotations to node networks
 *  intended for use with version 2.4.1 of the Task Annotations and Extensions.
 */
public abstract 
class TaskBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  /**
   * Constructor.
   * 
   * @param name
   *   The name of the Builder.
   * @param desc
   *   A brief description of what the Builder is supposed to do.
   * @param mclient
   *   The instance of the Master Manager that the Builder is going to use.
   * @param qclient
   *   The instance of the Queue Manager that the Builder is going to use
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   * @param entityType
   *   The Shotgun entity type that the Tasks this Builder is making will be.  This can be
   *   set to <code>null</code> if no entity type is desired.
   */
  protected 
  TaskBuilder
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    String entityType
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, builderInformation);
    
    pAnnotTaskTypeChoices = TaskType.titles(); 
    
    pEntityType = entityType;
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
  /*  T A S K   A N N O T A T I O N S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Adds a SubmitTask, ApproveTask or Task annotation to the set of annotation 
   * plugins which will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param purpose
   *   The purpose of the node within the task.
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   */ 
  protected void
  addTaskAnnotation
  (
   BaseStage stage,
   NodePurpose purpose, 
   String projectName, 
   String taskName, 
   String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(purpose, projectName, taskName, taskType); 
    addTaskAnnotationToStage(stage, annot); 
  }

  /** 
   * Adds a SubmitTask, ApproveTask or Task annotation to the set of annotation 
   * plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   */ 
  protected void
  addTaskAnnotation
  (
   String nodeName, 
   NodePurpose purpose, 
   String projectName, 
   String taskName, 
   String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(purpose, projectName, taskName, taskType); 
    addTaskAnnotationToNode(nodeName, annot);
  }

  
  
  /** 
   * Adds a SubmitTask, ApproveTask or Task annotation to the set of annotation 
   * plugins on the given node if it doesn't already exist. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   */ 
  protected void
  addMissingTaskAnnotation
  (
   String nodeName, 
   NodePurpose purpose, 
   String projectName, 
   String taskName, 
   String taskType
  )
    throws PipelineException
  {
    boolean found = false;
    TreeMap<String,BaseAnnotation> annotations = pClient.getAnnotations(nodeName);
    for(String aname : annotations.keySet()) {
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        BaseAnnotation annot = annotations.get(aname);
        if(lookupPurpose(nodeName, aname, annot).equals(purpose.toString())) { 
          String proj = lookupProjectName(nodeName, aname, annot);
          String task = lookupTaskName(nodeName, aname, annot);
          if(proj.equals(projectName) && task.equals(taskName)) 
            found = true;
        }
      }
    }

    if(!found) 
      addTaskAnnotation(nodeName, purpose, projectName, taskName, taskType); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Adds an ApproveTask with a specific approval builder to the set of annotation 
   * plugins which will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   * 
   * @param builderID
   *   The unique ID of the approval builder.
   */ 
  protected void
  addApproveTaskAnnotation
  (
   BaseStage stage,
   String projectName, 
   String taskName, 
   String taskType, 
   BuilderID builderID
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      getNewTaskAnnotation(NodePurpose.Approve, projectName, taskName, taskType); 
    annot.setParamValue("ApprovalBuilder", builderID);
    addTaskAnnotationToStage(stage, annot); 
  }

  /** 
   * Adds an ApproveTask with a specific approval builder to the set of annotation 
   * plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   * 
   * @param builderID
   *   The unique ID of the approval builder.
   */ 
  protected void
  addApproveTaskAnnotation
  (
   String nodeName, 
   String projectName, 
   String taskName, 
   String taskType, 
   BuilderID builderID
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      getNewTaskAnnotation(NodePurpose.Approve, projectName, taskName, taskType); 
    annot.setParamValue("ApprovalBuilder", builderID);
    addTaskAnnotationToNode(nodeName, annot);
  }

  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Adds a SubmitTask with a specific approval node to the set of annotation 
   * plugins which will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   * 
   * @param approveNode
   *   The name of the approve node which corresponds to this approve node.
   */ 
  protected void
  addSubmitTaskAnnotation
  (
   BaseStage stage,
   String projectName, 
   String taskName, 
   String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      getNewTaskAnnotation(NodePurpose.Submit, projectName, taskName, taskType); 
    addTaskAnnotationToStage(stage, annot); 
  }

  /** 
   * Adds a SubmitTask with a specific approval node  to the set of annotation 
   * plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   * 
   * @param approveNode
   *   The name of the approve node which corresponds to this approve node.
   */ 
  protected void
  addSubmitTaskAnnotation
  (
   String nodeName, 
   String projectName, 
   String taskName, 
   String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      getNewTaskAnnotation(NodePurpose.Submit, projectName, taskName, taskType); 
    addTaskAnnotationToNode(nodeName, annot);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Return a new SubmitTask, ApproveTask or Task annotation instance appropriate
   * to be added to the set of annotation plugins for a node. <P> 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   */ 
  protected BaseAnnotation
  getNewTaskAnnotation
  (
   NodePurpose purpose, 
   String projectName, 
   String taskName, 
   String taskType
  )
    throws PipelineException
  {
    BaseAnnotation annot = null; 
    switch(purpose) {
    case Submit:
    case Approve:
      annot = pPlug.newAnnotation(purpose + "Task", new VersionID("2.4.1"), "Temerity");
      break;

    default:
      annot = pPlug.newAnnotation("Task", new VersionID("2.4.1"), "Temerity");
    }
 
    annot.setParamValue(aAnnotProjectName, projectName);
    annot.setParamValue(aAnnotTaskName, taskName);

    if(pAnnotTaskTypeChoices.contains(taskType)) {
      annot.setParamValue(aAnnotTaskType, taskType);
    }
    else {
      annot.setParamValue(aAnnotTaskType, TaskType.CUSTOM.toTitle()); 
      annot.setParamValue(aAnnotCustomTaskType, taskType);
    }

    switch(purpose) {
    case Submit:
    case Approve:
      break;

    default:
      annot.setParamValue(aAnnotPurpose, purpose.toString());
    }
    
    if (pEntityType != null)
      annot.setParamValue(aAnnotEntityType, pEntityType);

    return annot; 
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given task annotation to a stage.
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param annot
   *   The annotation to add.
   */ 
  protected void 
  addTaskAnnotationToStage
  (
   BaseStage stage,
   BaseAnnotation annot
  ) 
  {
    /* find a unique name for this annotation, 
         the convention is to call the primary purpose task annotation "Task" and 
         any secondary task annotations "AltTask1", "AltTask2" ... "AltTaskN"  */ 
    String annotName = "Task";
    {
      Map<String, BaseAnnotation> exist = stage.getAnnotations();
      int wk;
      for(wk=1; true; wk++) {
        if(!exist.containsKey(annotName)) {
          stage.addAnnotation(annotName, annot); 
          break;
        }

        annotName = ("AltTask" + wk);
      }
    }
  }

  /**
   * Add the given task annotation to a stage.
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param annot
   *   The annotation to add.
   */ 
  protected void 
  addTaskAnnotationToNode
  (
   String nodeName, 
   BaseAnnotation annot
  ) 
    throws PipelineException
  {
    /* find a unique name for this annotation, 
         the convention is to call the primary purpose task annotation "Task" and 
         any secondary task annotations "AltTask1", "AltTask2" ... "AltTaskN"  */ 
    String annotName = "Task";
    {
      TreeMap<String, BaseAnnotation> exist = pClient.getAnnotations(nodeName);
      int wk;
      for(wk=1; true; wk++) {
        if(!exist.containsKey(annotName)) {
          pClient.addAnnotation(nodeName, annotName, annot);
          break;
        }

        annotName = ("AltTask" + wk);
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N   P A R A M E T E R   L O O K U P                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Lookup the value of the ProjectName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupProjectName
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String projectName = (String) annot.getParamValue(aAnnotProjectName);
    if(projectName == null) 
      throw new PipelineException
        ("No " + aAnnotProjectName + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 
    
    return projectName;
  }

  /**
   * Lookup the value of the TaskName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskName
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskName = (String) annot.getParamValue(aAnnotTaskName);
    if(taskName == null) 
      throw new PipelineException
        ("No " + aAnnotTaskName + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 

    return taskName;
  }

  /**
   * Lookup the value of the (Custom)TaskType annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskType
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskType = (String) annot.getParamValue(aAnnotTaskType);
    if(taskType == null) 
      throw new PipelineException
        ("No " + aAnnotTaskType + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 

    if(taskType.equals(TaskType.CUSTOM.toTitle())) {
      taskType = (String) annot.getParamValue(aAnnotCustomTaskType);
      if(taskType == null) 
        throw new PipelineException
          ("No " + aAnnotCustomTaskType + " parameter was specified for the " + 
           "(" + aname + ") annotation on the node (" + name + ") even though the " + 
           aAnnotTaskType + " " + "parameter was set to (" + TaskType.CUSTOM.toTitle() + ")!"); 
    }

    return taskType;
  }

  /**
   * Lookup the value of the TaskName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupPurpose
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String purpose = (String) annot.getParamValue(aAnnotPurpose);
    if(purpose == null) 
      throw new PipelineException
        ("No " + aAnnotPurpose + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 

    return purpose;
  }
  

  /**
   * The names of parameters supported by SubmitTask, ApproveTask and Task annotations.
   */ 
  public static final String aAnnotProjectName    = "ProjectName";
  public static final String aAnnotTaskName       = "TaskName";
  public static final String aAnnotTaskType       = "TaskType";
  public static final String aAnnotEntityType     = "EntityType";
  public static final String aAnnotCustomTaskType = "CustomTaskType";
  public static final String aAnnotPurpose        = "Purpose"; 
  public static final String aAnnotAssignedTo     = "AssignedTo";
  public static final String aAnnotSupervisedBy   = "SupervisedBy";

    
  public static final String aDoAnnotations = "DoAnnotations";
  
  private static final long serialVersionUID = 486953128847968229L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The names of all built-in TaskTypes supported by the SubmitTask, ApproveTask and 
   * Task annotations.
   */ 
  private ArrayList<String> pAnnotTaskTypeChoices;
  
  private String pEntityType;
  
}
