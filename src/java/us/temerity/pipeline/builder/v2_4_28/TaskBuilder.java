package us.temerity.pipeline.builder.v2_4_28;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.TaskCollection.v2_4_28.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Abstract Builder Class providing shortcuts for adding Task Annotations to node networks
 * intended for use with version 2.4.28 of the Task Annotations and Extensions.
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
    EntityType entityType
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, builderInformation);
    
    pAllAnnotCache = new TripleMap<String, String, String, TreeMap<String,BaseAnnotation>>();
    pNodeAnnotCache = new DoubleMap<String, String, BaseAnnotation>();
    
    pAnnotTaskTypeChoices = TaskType.titlesNonCustom(); 
    
    pEntityType = entityType;
    
    pTaskInfoSet = false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  T A S K    I N F OR M A T I O N                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the task information that the Task Builder will use when assigning annotations to 
   * nodes or stages. <p>
   * 
   * This needs to be called before any calls are made to add annotations to nodes.
   * 
   * @param projectName
   *   The value to give the ProjectName parameter of the annotation.
   * 
   * @param taskIdent1
   *   The value to give the TaskIdent1 parameter of the annotation.
   *   
   * @param taskIdent2
   *   The value to give the TaskIdent2 parameter of the annotation.
   * 
   * @param taskType
   *   The value to give the TaskType/CustomTaskType parameter(s) of the annotation.
   *   
   * @throws IllegalArgumentException
   *   If any of the values passed in are <code>null</code>.
   */
  protected void
  setTaskInformation
  (
    String projectName, 
    String taskIdent1, 
    String taskIdent2,
    String taskType
  )
  {
    if (projectName == null)
      throw new IllegalArgumentException("the projectName argument cannot be (null)");
    pProjectName = projectName;
    
    if (taskIdent1 == null)
      throw new IllegalArgumentException("the taskIdent1 argument cannot be (null)");
    pTaskIdent1 = taskIdent1;
    
    if (taskIdent2 == null)
      throw new IllegalArgumentException("the taskIdent2 argument cannot be (null)");
    pTaskIdent2 = taskIdent2;
    
    if (taskType == null)
      throw new IllegalArgumentException("the taskType argument cannot be (null)");

    if(pAnnotTaskTypeChoices.contains(taskType)) {
      pTaskType = taskType;
      pCustomTaskType = null;
    }
    else {
      pTaskType = TaskType.CUSTOM.toTitle();
      pCustomTaskType = taskType;
    }
    
    pTaskInfoSet = true;
  }
  
  /**
   * Get the name of the task's project. <p>
   */
  protected String
  getProjectName()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before using getters.");
    
    return pProjectName;
  }
  
  /**
   * Get the first of the task's identifiers. <p>
   */
  protected String
  getTaskIdent1()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before using getters.");
    
    return pTaskIdent1;
  }
  
  /**
   * Get the second of the task's identifiers. <p>
   */
  protected String
  getTaskIdent2()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before using getters.");
    
    return pTaskIdent2;
  }
  
  /**
   * Get the type of the task. <p>
   * 
   * @return
   */
  protected String
  getTaskType()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before using getters.");
    
    return pTaskType;
  }
  
  /**
   * Convert the array representation of a task into a String so it can be printed in log
   * messages.
   * 
   * @param taskinfo
   *   The unique name information used to identify the task being converted. This is the same 
   *   information returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method. 
   *   The format is [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  public static String
  taskArrayToString
  (
    String taskInfo[]  
  )
  {
    return taskInfo[0] + "|" + taskInfo[1] + "|" + taskInfo[2] + "|" + taskInfo[3];
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  D E F A U L T   N A M E S                                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the default path where nodes are not directly related to the artist's work will 
   * live. <p>
   * 
   * This path is used to generate the other default paths in this builder.
   * 
   * @param taskinfo
   *   The unique name information used to identify the task.  This is the same information
   *   returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method.  The format is
   *   [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  public static final Path
  getDefaultTemerityPath
  (
    String[] taskinfo
  )
  {
    String projectName = taskinfo[0];
    String taskIdent1 = taskinfo[1];
    String taskIdent2 = taskinfo[2];
    String taskType = taskinfo[3];
    
    return
      new Path(new Path(new Path(new Path(new Path(
        new Path("/projects"), projectName), "temerity"), taskType), taskIdent1), 
                  taskIdent2);
  }
  
  /**
   * Get the default path where nodes are not directly related to the artist's work will 
   * live. <p>
   * 
   * This path is used to generate the other default paths in this builder.
   */
  protected final Path
  getDefaultTemerityPath()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before getting default node names.");

    String taskInfo[] = new String[4];
    taskInfo[0] = pProjectName;
    taskInfo[1] = pTaskIdent1;
    taskInfo[2] = pTaskIdent2;
    if (pTaskType.equals(TaskType.CUSTOM.toString()))
      taskInfo[3] = pCustomTaskType;
    else
      taskInfo[3] = pTaskType;

    return getDefaultTemerityPath(taskInfo);
  }
  
  /**
   * Get the default node name for the verify node in the task system.
   * <p>
   * 
   * All the Temerity Task tools that look for a verify node will look for it here. This only
   * really involves the default VerifyTaskBuilder in the {@link TaskCollection}, which looks
   * up this node to check it out. Other approaches to verifying Task networks (including
   * using templates) do not require this value. If it is desirable to change the location of
   * this node and use the simple task builders, then a studio needs to make a custom version
   * of the TaskCollection whose builders look in the right place.
   * 
   * @param taskinfo
   *   The unique name information used to identify the task.  This is the same information
   *   returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method.  The format is
   *   [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  public static final String
  getDefaultVerifyNodeName
  (
    String[] taskinfo
  )
  {
    String taskIdent1 = taskinfo[1];
    String taskIdent2 = taskinfo[2];
    String taskType = taskinfo[3];
    
    Path verifyPath = 
      new Path(new Path(getDefaultTemerityPath(taskinfo), "verify"), 
                        taskIdent1 + "_" + taskIdent2 + "_" + taskType + "_verify");   

    return verifyPath.toString();
  }
  
  /**
   * Get the default node name for the verify node in the task system.
   * <p>
   * 
   * All the Temerity Task tools that look for a verify node will look for it here. This only
   * really involves the default VerifyTaskBuilder in the {@link TaskCollection}, which looks
   * up this node to check it out. Other approaches to verifying Task networks (including
   * using templates) do not require this value. If it is desirable to change the location of
   * this node and use the simple task builders, then a studio needs to make a custom version
   * of the TaskCollection whose builders look in the right place.
   */
  protected String
  getDefaultVerifyNodeName()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before getting default node names.");
    
    String taskInfo[] = new String[4];
    taskInfo[0] = pProjectName;
    taskInfo[1] = pTaskIdent1;
    taskInfo[2] = pTaskIdent2;
    if (pTaskType.equals(TaskType.CUSTOM.toString()))
      taskInfo[3] = pCustomTaskType;
    else
      taskInfo[3] = pTaskType;
    
    return getDefaultVerifyNodeName(taskInfo);
  }
  
  /**
   * Get the default node name for the publish node in the task system.
   * <p>
   * 
   * All the Temerity Task tools that look for a publish node will look for it here. This only
   * really involves the default PublishTaskBuilder in the {@link TaskCollection}, which looks
   * up this node to check it out. Other approaches to verifying Task networks (including
   * using templates) do not require this value. If it is desirable to change the location of
   * this node and use the simple task builders, then a studio needs to make a custom version
   * of the TaskCollection whose builders look in the right place.
   * 
   * @param taskinfo
   *   The unique name information used to identify the task.  This is the same information
   *   returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method.  The format is
   *   [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  public static final String
  getDefaultPublishNodeName
  (
    String[] taskinfo
  )
  {
    String taskIdent1 = taskinfo[1];
    String taskIdent2 = taskinfo[2];
    String taskType = taskinfo[3];
   
    Path publishPath = 
      new Path(new Path(getDefaultTemerityPath(taskinfo), "publish"), 
                        taskIdent1 + "_" + taskIdent2 + "_" + taskType + "_publish");   

    return publishPath.toString();
  }

  /**
   * Get the approve node name for the verify node in the task system.
   * <p>
   * 
   * All the Temerity Task tools that look for a publish node will look for it here. This
   * only really involves the default PublishTaskBuilder in the {@link TaskCollection}, which
   * looks up this node to check it out. Other approaches to publishing Task networks
   * (including using templates) do not require this value. If it is desirable to change the
   * location of this node and use the simple task builders, then a studio needs to make a
   * custom version of the TaskCollection whose builders look in the right place.
   */
  protected String
  getDefaultPublishNodeName()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before getting default node names.");
    
    String taskInfo[] = new String[4];
    taskInfo[0] = pProjectName;
    taskInfo[1] = pTaskIdent1;
    taskInfo[2] = pTaskIdent2;
    if (pTaskType.equals(TaskType.CUSTOM.toString()))
      taskInfo[3] = pCustomTaskType;
    else
      taskInfo[3] = pTaskType;

    return getDefaultPublishNodeName(taskInfo);
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default node name for the node used to run the verify builder in the task system.
   * <p>
   * 
   * All the Temerity Task plugins that wish to run the verify builder will look for this 
   * node. Changing this node will involve modifying the tools and server-side extensions 
   * which interact with the task system.
   * 
   * @param taskinfo
   *   The unique name information used to identify the task.  This is the same information
   *   returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method.  The format is
   *   [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  public static final String
  getDefaultVerifyBuilderNodeName
  (
    String[] taskinfo  
  )
  {
    String taskIdent1 = taskinfo[1];
    String taskIdent2 = taskinfo[2];
    String taskType = taskinfo[3];
   
    Path verifyPath = 
      new Path(new Path(getDefaultTemerityPath(taskinfo), "builder"), 
                        taskIdent1 + "_" + taskIdent2 + "_" + taskType + "_runVerify");   

    return verifyPath.toString();
  }

  /**
   * Get the default node name for the node used to run the verify builder in the task system.
   * <p>
   * 
   * All the Temerity Task plugins that wish to run the verify builder will look for this 
   * node. Changing this node will involve modifying the tools and server-side extensions 
   * which interact with the task system.
   * 
   * @param taskinfo
   *   The unique name information used to identify the task.  This is the same information
   *   returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method.  The format is
   *   [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  protected String
  getDefaultVerifyBuilderNodeName()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before getting default node names.");
    
    String taskInfo[] = new String[4];
    taskInfo[0] = pProjectName;
    taskInfo[1] = pTaskIdent1;
    taskInfo[2] = pTaskIdent2;
    if (pTaskType.equals(TaskType.CUSTOM.toString()))
      taskInfo[3] = pCustomTaskType;
    else
      taskInfo[3] = pTaskType;
    
    return getDefaultVerifyBuilderNodeName(taskInfo);
  }

  /**
   * Get the default node name for the node used to run the publish builder in the task 
   * system.
   * <p>
   * 
   * All the Temerity Task plugins that wish to run the publish builder will look for this 
   * node. Changing this node will involve modifying the tools and server-side extensions 
   * which interact with the task system.
   * 
   * @param taskinfo
   *   The unique name information used to identify the task.  This is the same information
   *   returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method.  The format is
   *   [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  public static final String
  getDefaultPublishBuilderNodeName
  (
    String[] taskinfo  
  )
  {
    String projectName = taskinfo[0];
    String taskIdent1 = taskinfo[1];
    String taskIdent2 = taskinfo[2];
    String taskType = taskinfo[3];
    
    Path publishPath = 
      new Path(new Path(getDefaultTemerityPath(taskinfo), "builder"), 
                        taskIdent1 + "_" + taskIdent2 + "_" + taskType + "_runPublish");   

    return publishPath.toString();
  }
  
  /**
   * Get the default node name for the node used to run the publish builder in the task 
   * system.
   * <p>
   * 
   * All the Temerity Task plugins that wish to run the publish builder will look for this 
   * node. Changing this node will involve modifying the tools and server-side extensions 
   * which interact with the task system.
   * 
   * @param taskinfo
   *   The unique name information used to identify the task.  This is the same information
   *   returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method.  The format is
   *   [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  protected String
  getDefaultPublishBuilderNodeName()
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before getting default node names.");
    
    String taskInfo[] = new String[4];
    taskInfo[0] = pProjectName;
    taskInfo[1] = pTaskIdent1;
    taskInfo[2] = pTaskIdent2;
    if (pTaskType.equals(TaskType.CUSTOM.toString()))
      taskInfo[3] = pCustomTaskType;
    else
      taskInfo[3] = pTaskType;
    
    return getDefaultPublishBuilderNodeName(taskInfo);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  T A S K   A N N O T A T I O N S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Adds a Task annotation to the set of annotation plugins which will be added to the node 
   * built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param purpose
   *   The purpose of the node within the task.
   */ 
  protected void
  addTaskAnnotation
  (
    BaseStage stage,
    NodePurpose purpose
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(purpose); 
    addTaskAnnotationToStage(stage, annot); 
  }

  /** 
   * Adds a Task annotation to the set of annotation plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   */ 
  protected void
  addTaskAnnotation
  (
    String nodeName, 
    NodePurpose purpose
  )
    throws PipelineException
  {
    BaseAnnotation annot = 
      getNewTaskAnnotation(purpose); 
    addTaskAnnotationToNode(nodeName, annot);
  }

  /** 
   * Adds a Task annotation to the set of annotation plugins on the given node if it doesn't 
   * already exist. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   */ 
  protected void
  addMissingTaskAnnotation
  (
    String nodeName, 
    NodePurpose purpose
  )
    throws PipelineException
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before adding task annotations.");
    
    boolean found = false;
    TreeMap<String,BaseAnnotation> annotations = pClient.getAnnotations(nodeName);
    for(String aname : annotations.keySet()) {
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        BaseAnnotation annot = annotations.get(aname);
        if(lookupTaskPurpose(nodeName, aname, annot).equals(purpose.toString())) { 
          String proj = lookupProjectName(nodeName, aname, annot);
          String task1 = lookupTaskIdent1(nodeName, aname, annot);
          String task2 = lookupTaskIdent2(nodeName, aname, annot);
          String type = lookupTaskType(nodeName, aname, annot);
          if(proj.equals(pProjectName) && task1.equals(pTaskIdent1) && 
             task2.equals(pTaskIdent2) && type.equals(pTaskType))
            found = true;
        }
      }
    }

    if(!found) 
      addTaskAnnotation(nodeName, purpose); 
  }

  /*----------------------------------------------------------------------------------------*/
  
  
  /** 
   * Add a Task Annotation with the Publish Purpose to the set of annotation plugins which 
   * will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   */ 
  protected void
  addPublishTaskAnnotation
  (
    BaseStage stage
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(NodePurpose.Publish); 
    addTaskAnnotationToStage(stage, annot); 
  }

  /** 
   * Add a Task Annotation with the Publish Purpose to the set of annotation 
   * plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   */ 
  protected void
  addPublishTaskAnnotation
  (
    String nodeName
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(NodePurpose.Publish); 
    addTaskAnnotationToNode(nodeName, annot);
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Add a Task Annotation with the Verify Purpose to the set of annotation plugins which 
   * will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   */ 
  protected void
  addVerifyTaskAnnotation
  (
    BaseStage stage
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(NodePurpose.Verify); 
    addTaskAnnotationToStage(stage, annot); 
  }

  /** 
   * Add a Task Annotation with the Verify Purpose to the set of annotation 
   * plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   */ 
  protected void
  addVerifyTaskAnnotation
  (
    String nodeName
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(NodePurpose.Publish); 
    addTaskAnnotationToNode(nodeName, annot);
  }

  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Adds a SubmitTask to the set of annotation plugins which will be added to the node 
   * built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   */ 
  protected void
  addSubmitTaskAnnotation
  (
    BaseStage stage
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(NodePurpose.Submit); 
    addTaskAnnotationToStage(stage, annot); 
  }

  /** 
   * Adds a SubmitTask to the set of annotation plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   */ 
  protected void
  addSubmitTaskAnnotation
  (
    String nodeName
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(NodePurpose.Submit); 
    addTaskAnnotationToNode(nodeName, annot);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Adds a FocusTask to the set of annotation plugins which will be added to the node 
   * built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   */ 
  protected void
  addMasterFocusTaskAnnotation
  (
    BaseStage stage
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(NodePurpose.Focus, true); 
    addTaskAnnotationToStage(stage, annot); 
  }

  /** 
   * Adds a FocusTask to the set of annotation plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   */ 
  protected void
  addMasterFocusTaskAnnotation
  (
    String nodeName
  )
    throws PipelineException
  {
    BaseAnnotation annot = getNewTaskAnnotation(NodePurpose.Focus, true); 
    addTaskAnnotationToNode(nodeName, annot);
  }

  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Return a new Task annotation instance appropriate to be added to the set of annotation 
   * plugins for a node. <P> 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   *   
   * @param master
   *   If the purpose is Focus, this will make it a Master Focus Node using the 
   *   FocusTask annotation.  Otherwise it has no effect.
   *   
   * @throws IllegalStateException
   *   If {@link #setTaskInformation(String, String, String, String) setTaskInformation()} 
   *   was not called before calling this method.
   */ 
  protected BaseAnnotation
  getNewTaskAnnotation
  (
   NodePurpose purpose, 
   boolean master
  )
    throws PipelineException
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before adding task annotations.");
    
    BaseAnnotation annot = pPlug.newAnnotation("Task", new VersionID("2.4.28"), "Temerity"); 
 
    annot.setParamValue(aAnnotProjectName, pProjectName);
    annot.setParamValue(aAnnotTaskIdent1, pTaskIdent1);
    annot.setParamValue(aAnnotTaskIdent2, pTaskIdent2);
    annot.setParamValue(aAnnotTaskType, pTaskType);
    annot.setParamValue(aAnnotCustomTaskType, pCustomTaskType);
    
    annot.setParamValue(aAnnotPurpose, purpose.toString());
    
    if (purpose == NodePurpose.Focus && master)
      annot.setParamValue(aAnnotMaster, true);
    
    if (pEntityType != null)
      annot.setParamValue(aAnnotEntityType, pEntityType.toTitle());

    return annot; 
  }

  /**
   * Return a new Task annotation instance appropriate to be added to the set of annotation
   * plugins for a node. <P>
   * 
   * @param purpose
   *  The purpose of the node within the task.
   */ 
  protected BaseAnnotation
  getNewTaskAnnotation
  (
   NodePurpose purpose
  )
    throws PipelineException
  {
    return getNewTaskAnnotation(purpose, false);
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
  /*   A N N O T A T I O N   L O O K U P                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the per-node and per-version Annotations on the given node.  
   * <p>
   * If there is no working version of the node, this method will fail.  If per-node 
   * annotations need to be retrieved then the method should be used. 
   * <p>
   * This method uses a cache to accelerate access to the annotations.  Annotations will only
   * be looked up once for each node.  This is not a cross-builder cache, so multiple
   * builders based on the TaskBuilder (perhaps being used a sub-builders) may lookup the
   * same information.
   * 
   * @param name
   *   The name of the node.
   * 
   * @return
   *   A TreeMap of Annotations indexed by annotation name, which may be empty if no 
   *   annotations exist on the node.
   */
  protected TreeMap<String, BaseAnnotation>
  getAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = 
      pAllAnnotCache.get(getAuthor(), getView(), name);
    if (annots == null) {
      annots = getMasterMgrClient().getAnnotations(getAuthor(), getView(), name);
      pAllAnnotCache.put(getAuthor(), getView(), name, annots);
    }
   return annots;
  }

  /**
   * Get the per-node Annotations on the given node.  
   * <p>
   * This method uses a cache to accelerate access to the annotations.  Annotations will only
   * be looked up once for each node.  This is not a cross-builder cache, so multiple
   * builders based on the TaskBuilder (perhaps being used a sub-builders) may lookup the
   * same information.
   * 
   * @param name
   *   The name of the node.
   * 
   * @return
   *   A TreeMap of Annotations indexed by annotation name which may be empty if no per-node
   *   annotations exist.
   */
  protected TreeMap<String, BaseAnnotation>
  getNodeAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = 
      pNodeAnnotCache.get(name);
    if (annots == null) {
      annots = getMasterMgrClient().getAnnotations(name);
      pNodeAnnotCache.put(name, annots);
    }
   return annots;
  }
  
  /**
   * Get the Task Annotations on the given node. <p>
   * 
   * This method will only return the Task annotations that are version 2.4.28 from the 
   * Temerity vendor.
   *
   * @param name
   *   The name of the node.
   * @return
   *   A TreeMap of Task Annotations indexed by annotation name or 
   *   <code>null</code> if none exists.
   */
  protected TreeMap<String, BaseAnnotation>
  getTaskAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = getNodeAnnotations(name);
    PluginID pid = new PluginID("Task", new VersionID("2.4.28"), "Temerity");
    
    TreeMap<String, BaseAnnotation> toReturn = null;
    for(Entry<String, BaseAnnotation> entry : annots.entrySet()) {
      String aname = entry.getKey();
      BaseAnnotation tannot = annots.get(aname);
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        if (tannot.getPluginID().equals(pid)) {
          if (toReturn == null)
            toReturn = new TreeMap<String, BaseAnnotation>();
          toReturn.put(aname, tannot);
        }
      }
    }
   return toReturn;
  }

  /**
   * Searches the set of annotations associated with the given node for Task related 
   * annotations. 
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param byPurpose
   *   A table of those that match indexed by Purpose parameter.
   * 
   * @return 
   *   The [ProjectName, TaskIdent1, TaskIdent1, TaskType] array.
   */ 
  protected String[] 
  lookupTaskAnnotations
  (
    String name, 
    TreeMap<NodePurpose, BaseAnnotation> byPurpose
  ) 
    throws PipelineException
  {
    PluginID pid = new PluginID("Task", new VersionID("2.4.28"), "Temerity");
    
    TreeMap<String, BaseAnnotation> annots = pClient.getAnnotations(name);
    String projectName = null; 
    String taskIdent1  = null; 
    String taskIdent2  = null;
    String taskType    = null; 
    for(Entry<String, BaseAnnotation> entry : annots.entrySet()) {
      String aname = entry.getKey();
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        BaseAnnotation an = entry.getValue();
        
        if (!an.getPluginID().equals(pid))
          continue;
            
        
        NodePurpose purpose = lookupTaskPurpose(name, aname, an); 
        if(purpose != null) {
          if(byPurpose.containsKey(purpose)) 
          throw new PipelineException
            ("More than one Task related annotation with a " + aAnnotPurpose + " of " + 
             purpose + " was found on node (" + name + ")!"); 
  
          {
            String pname = lookupProjectName(name, aname, an); 
            if(pname == null) 
              throw new PipelineException
                ("The " + aAnnotProjectName + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((projectName != null) && !projectName.equals(pname)) 
              throw new PipelineException 
                ("The " + aAnnotProjectName + " was set in multiple Task annotations on " +
                 "node (" + name + "), but the did not match!  Both (" + projectName + ") " +
                 "and (" + pname + ") where given as the " + aAnnotProjectName + ".");
  
            projectName = pname;
          }
  
          {
            String tname = lookupTaskIdent1(name, aname, an);  
            if(tname == null) 
              throw new PipelineException
                ("The " + aAnnotTaskIdent1 + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskIdent1 != null) && !taskIdent1.equals(tname)) 
              throw new PipelineException 
                ("The " + aAnnotTaskIdent1 + " was set in multiple Task annotations on " +
                 "node (" + name + "), but they did not match!  Both (" + taskIdent1 + ") " +
                 "and (" + tname + ") where given as the " + aAnnotTaskIdent1 + ".");
  
            taskIdent1 = tname; 
          }
          
          {
            String tname = lookupTaskIdent2(name, aname, an);  
            if(tname == null) 
              throw new PipelineException
                ("The " + aAnnotTaskIdent2 + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskIdent2 != null) && !taskIdent2.equals(tname)) 
              throw new PipelineException 
                ("The " + aAnnotTaskIdent2 + " was set in multiple Task annotations on " +
                 "node (" + name + "), but they did not match!  Both (" + taskIdent2 + ") " +
                 "and (" + tname + ") where given as the " + aAnnotTaskIdent2 + ".");
  
            taskIdent2 = tname; 
          }
  
          {
            String ttype = lookupTaskType(name, aname, an);  
            if(ttype == null) 
              throw new PipelineException
                ("The " + aAnnotTaskType + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskType != null) && !taskType.equals(ttype)) 
              throw new PipelineException 
                ("The " + aAnnotTaskType + " was set in multiple Task annotations on node " + 
                 "(" + name + "), but the did not match!  Both (" + taskType + ") and " + 
                 "(" + ttype + ") where given as the " + aAnnotTaskType + ".");
  
            taskType = ttype;
          }
  
          byPurpose.put(purpose, an); 
        }
      }
    }

    if(!byPurpose.isEmpty()) {
      String names[] = { projectName, taskIdent1, taskIdent2, taskType };
      return names;
    }

    return null;
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
   * Lookup the value of the TaskIdent1 annotation parameter.
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
  lookupTaskIdent1
  (
    String name, 
    String aname, 
    BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskName = (String) annot.getParamValue(aAnnotTaskIdent1);
    if(taskName == null) 
      throw new PipelineException
        ("No " + aAnnotTaskIdent1 + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 

    return taskName;
  }
  
  /**
   * Lookup the value of the TaskIdent2 annotation parameter.
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
  lookupTaskIdent2
  (
    String name, 
    String aname, 
    BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskName = (String) annot.getParamValue(aAnnotTaskIdent2);
    if(taskName == null) 
      throw new PipelineException
        ("No " + aAnnotTaskIdent2 + " parameter was specified for the (" + aname + ") " + 
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
  protected NodePurpose
  lookupTaskPurpose
  (
    String name, 
    String aname, 
    BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    EnumAnnotationParam param = (EnumAnnotationParam) annot.getParam(aAnnotPurpose);
    
    if(param == null) 
      throw new PipelineException
        ("No " + aAnnotPurpose + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!");
    
    return NodePurpose.values()[param.getIndex()];
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  C O M P A R A T O R                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the passed in task information come from the same task as the one this task builder
   * is working on? 
   * <p>
   * 
   * @param taskinfo
   *   The unique name information used to identify the task being compared. This is the same 
   *   information returned by the {@link #lookupTaskAnnotations(String, TreeMap)} method. 
   *   The format is [ProjectName, TaskIdent1, TaskIdent1, TaskType].
   */
  protected boolean
  isSameTask
  (
    String taskinfo[]  
  )
  {
    if (!pTaskInfoSet)
      throw new IllegalStateException
        ("The setTaskInformation() method must be called before adding task annotations.");
    
    return 
      ( (pProjectName.equals(taskinfo[0])) &&
        (pTaskIdent1.equals(taskinfo[1])) &&
        (pTaskIdent2.equals(taskinfo[2])) &&
        (pTaskType.equals(taskinfo[3])) );
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6856508651994386636L;

  /**
   * The names of parameters supported by SubmitTask, ApproveTask, SynchTask, 
   * and Task annotations.
   */ 
  public static final String aAnnotProjectName    = "ProjectName";
  public static final String aAnnotTaskIdent1     = "TaskIdent1";
  public static final String aAnnotTaskIdent2     = "TaskIdent2";
  public static final String aAnnotTaskType       = "TaskType";
  public static final String aAnnotEntityType     = "EntityType";
  public static final String aAnnotCustomTaskType = "CustomTaskType";
  public static final String aAnnotPurpose        = "Purpose"; 
  public static final String aAnnotMaster         = "Master";

    
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The names of all built-in TaskTypes supported by the Task annotation.
   */ 
  private ArrayList<String> pAnnotTaskTypeChoices;
  
  private EntityType pEntityType;
  
  private TripleMap<String, String, String, TreeMap<String, BaseAnnotation>> pAllAnnotCache;
  
  private DoubleMap<String, String, BaseAnnotation> pNodeAnnotCache;
  
  /**
   * The boolean that indicates whether the task information has been set.
   */
  boolean pTaskInfoSet;
  
  /**
   * The fields that store the task information.
   */
  private String pProjectName;
  private String pTaskIdent1;
  private String pTaskIdent2;
  private String pTaskType;
  private String pCustomTaskType;
  
}
