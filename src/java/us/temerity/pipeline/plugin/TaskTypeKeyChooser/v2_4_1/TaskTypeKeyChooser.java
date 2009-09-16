// $Id: TaskTypeKeyChooser.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TaskTypeKeyChooser.v2_4_1;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.param.key.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   T Y P E   K E Y   C H O O S E R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Key Chooser that matches against the TaskType field on the TaskAnnotation to determine
 * whether the key should be on, designed to work with Task Policy v2.4.1
 * 
 * This key chooser defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Task Type<BR>
 *   <DIV style="margin-left: 40px;">
 *     The type of production goal the node is used to achieve.
 *   </DIV> <BR>
 * </DIV> <BR>
 */
public 
class TaskTypeKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  TaskTypeKeyChooser() 
  {
    super("TaskType", new VersionID("2.4.1"), "Temerity", 
          "Matches against the TaskType of the Task Annotation.");
    
    {
      ArrayList<String> choices = TaskType.titles();
      
      KeyParam param = 
        new EnumKeyParam
        (aTaskType, 
         "The type of production goal the node is used to achieve.", 
         TaskType.Asset.toTitle(), choices); 
      addParam(param);
    }
    {
      KeyParam param = 
        new StringKeyParam
        (aCustomTaskType, 
         "A unique type of production goal this node is used to achieve which is not one " +
         "of the standard type available in TaskType.  If a custom type is specified, the " +
         "TaskType parameter should be set to [[CUSTOM]].",
         null); 
      addParam(param);
    }
    
    LayoutGroup group = new LayoutGroup(true);
    group.addEntry(aTaskType);
    group.addEntry(aCustomTaskType);
    setLayout(group);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I S   A C T I V E                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return a value indicating whether the given node meets the criteria for this key.
   * <P> 
   * @param job
   *   The QueueJob that the key is going to apply to.  This contains the BaseAction and
   *   the ActionAgenda that can be mined for information.
   *   
   * @param annots
   *   The list of annotations assigned to the node the job is being created for.
   * 
   * @return 
   *   Whether this key is active for the job being created by the given node.
   * 
   * @throws PipelineException 
   *   If unable to return a value due to illegal, missing or incompatible 
   *   information in the node information or a general failure of the isActive method code.
   */
  @Override
  public boolean 
  isActive
  (
    QueueJob job,
    TreeMap<String, BaseAnnotation> annots
  )
    throws PipelineException
  {
    String taskType = (String) getParamValue(aTaskType);
    if((taskType != null) && (taskType.length() == 0))
      taskType = null;
    
    if((taskType != null) && taskType.equals(aCUSTOM)) {
      taskType = (String) getParamValue(aCustomTaskType);
      if((taskType != null) && (taskType.length() == 0))
        taskType = null;
    }
    
    if (taskType == null)
      return false;
    
    for (String annotName : annots.keySet()) {
      if ( (annotName.equals("Task") || annotName.startsWith("AltTask") )) {
        BaseAnnotation task = annots.get(annotName);
        String given = (String) task.getParamValue(aTaskType);
        if (given == null)
          return false;
        
        if (taskType.equals(given))
          return true;
      }
    }
    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3217811503443112558L;

  
  public static final String aTaskType       = "TaskType";
  public static final String aCustomTaskType = "CustomTaskType";
  public static final String aCUSTOM         = "[[CUSTOM]]";   
  
}
