// $Id: TaskTypeKeyChooser.java,v 1.3 2008/03/23 19:06:26 jim Exp $

package us.temerity.pipeline.plugin.TaskTypeKeyChooser.v2_3_15;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.EnumKeyParam;
import us.temerity.pipeline.param.key.KeyParam;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   T Y P E   K E Y   C H O O S E R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Key Chooser that matches against the TaskType field on the TaskAnnotation to determine
 * whether the key should be on.
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
    super("TaskType", new VersionID("2.3.15"), "Temerity", 
          "Matches against the TaskType of the Task Annotation.");
    
    {
      ArrayList<String> choices = new ArrayList<String>(); 
      choices.add(aSimpleAsset);  
      choices.add(aModeling);        
      choices.add(aRigging);         
      choices.add(aLookDev);        
      choices.add(aLayout);          
      choices.add(aAnimation);       
      choices.add(aEffects);         
      choices.add(aLighting);        
      choices.add(aCompositing);  
      
      KeyParam param = 
        new EnumKeyParam
        (aTaskType, 
         "The type of production goal the node is used to achieve.", 
         aSimpleAsset, choices); 
      addParam(param);
    }
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
    BaseAnnotation task = null;
    for (BaseAnnotation annot : annots.values()) {
      if ( (annot.getName().equals("Task") || annot.getName().equals("SubmitNode") )  && 
            annot.getVersionID().equals(new VersionID("2.3.2")))
        task = annot;
      if (task != null)
        break;
    }
    if (task == null)
      return false;
    
    String chosen = (String) getParamValue(aTaskType);
    if (chosen == null)
      return false;
    
    String given = (String) task.getParamValue(aTaskType);
    if (given == null)
      return false;
    
    if (chosen.equals(given))
      return true;
    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1812603423428011172L;
  
  public static final String aTaskType = "TaskType";
  
  public static final String aSimpleAsset = "Simple Asset";  
  public static final String aModeling    = "Modeling";        
  public static final String aRigging     = "Rigging";         
  public static final String aLookDev     = "Look Dev";        
  public static final String aLayout      = "Layout";          
  public static final String aAnimation   = "Animation";       
  public static final String aEffects     = "Effects";         
  public static final String aLighting    = "Lighting";        
  public static final String aCompositing = "Compositing"; 
}
