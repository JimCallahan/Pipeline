// $Id: ICTaskBuilder.java,v 1.1 2008/02/06 07:21:06 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*; 

import java.util.*; 

/**
 * Abstract builder base class that provides the task annotation functionality. 
 */
public abstract 
class ICTaskBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A protected constructor used by subclasses. 
   * 
   * @param name
   *   Name of the builder.
   * 
   * @param desc 
   *   A short description of the builder. 
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
  protected 
  ICTaskBuilder
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, builderInfo);

    pAnnotTaskTypeChoices = new ArrayList<String>(); 
    pAnnotTaskTypeChoices.add(aAnnotSimpleAsset);    
    pAnnotTaskTypeChoices.add(aAnnotModeling);        
    pAnnotTaskTypeChoices.add(aAnnotRigging);         
    pAnnotTaskTypeChoices.add(aAnnotLookDev);        
    pAnnotTaskTypeChoices.add(aAnnotLayout);          
    pAnnotTaskTypeChoices.add(aAnnotAnimation);       
    pAnnotTaskTypeChoices.add(aAnnotEffects);         
    pAnnotTaskTypeChoices.add(aAnnotLighting);  
    pAnnotTaskTypeChoices.add(aAnnotPlates);        
    pAnnotTaskTypeChoices.add(aAnnotTracking);     
    pAnnotTaskTypeChoices.add(aAnnotRoto);         
    pAnnotTaskTypeChoices.add(aAnnotMattePainting);            
    pAnnotTaskTypeChoices.add(aAnnotCompositing);  
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Helper method for adding the DoAnnotatons parameter to the current builder.
   */ 
  protected final void
  addDoAnnotationParam()
  {
    UtilityParam param = 
      new BooleanUtilityParam
      (aDoAnnotations, 
       "Should Task related annotations be added to all created nodes.", 
       true); 
    addParam(param);
  }



  /*----------------------------------------------------------------------------------------*/
  /*  T A S K   A N N O T A T I O N S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Adds the SubmitTask annotation to the set of annotation plugins which will be added 
   * to the node created by the given stage.<P> 
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
    BaseAnnotation annot = null; 
    switch(purpose) {
    case Submit:
    case Approve:
      annot = pPlug.newAnnotation(purpose + "Task", new VersionID("1.0.0"), "ICVFX");
      break;

    default:
      annot = pPlug.newAnnotation("CommonTask", new VersionID("1.0.0"), "ICVFX");
    }
 
    annot.setParamValue(aAnnotProjectName, projectName);
    annot.setParamValue(aAnnotTaskName, taskName);

    if(pAnnotTaskTypeChoices.contains(taskType)) {
      annot.setParamValue(aAnnotTaskType, taskType);
    }
    else {
      annot.setParamValue(aAnnotTaskType, aAnnotCUSTOM); 
      annot.setParamValue(aAnnotCustomTaskType, taskType);
    }

    switch(purpose) {
    case Submit:
    case Approve:
      break;

    default:
      annot.setParamValue(aAnnotPurpose, purpose.toString());
    }
    
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

  

  /*----------------------------------------------------------------------------------------*/
  /*  P U B L I C   S T A T I C S                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aDoAnnotations = "DoAnnotations";

  

  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The names of parameters supported by SubmitTask, ApproveTask and CommonTask annotations.
   */ 
  private static final String aAnnotProjectName    = "ProjectName";
  private static final String aAnnotTaskName       = "TaskName";
  private static final String aAnnotTaskType       = "TaskType";
  private static final String aAnnotCustomTaskType = "CustomTaskType";
  private static final String aAnnotPurpose        = "Purpose"; 

  /**
   * The names of the built-in TaskTypes supported by the SubmitTask, ApproveTask and 
   * CommonTask annotations.
   */ 
  private static final String aAnnotSimpleAsset   = "Simple Asset";  
  private static final String aAnnotModeling      = "Modeling";        
  private static final String aAnnotRigging       = "Rigging";         
  private static final String aAnnotLookDev       = "Look Dev";        
  private static final String aAnnotLayout        = "Layout";          
  private static final String aAnnotAnimation     = "Animation";       
  private static final String aAnnotEffects       = "Effects";         
  private static final String aAnnotLighting      = "Lighting"; 
  private static final String aAnnotPlates        = "Plates"; 
  private static final String aAnnotTracking      = "Tracking"; 
  private static final String aAnnotRoto          = "Roto"; 
  private static final String aAnnotMattePainting = "MattePainting"; 
  private static final String aAnnotCompositing   = "Compositing";  
  private static final String aAnnotCUSTOM        = "[[CUSTOM]]";  


  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The names of all built-in TaskTypes supported by the SubmitTask, ApproveTask and 
   * CommonTask annotations.
   */ 
  private ArrayList<String> pAnnotTaskTypeChoices; 

}
