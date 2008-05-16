// $Id: SubmitTaskAnnotation.java,v 1.4 2008/05/16 04:53:54 jim Exp $

package us.temerity.pipeline.plugin.SubmitTaskAnnotation.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_1.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S U B M I T   T A S K   A N N O T A T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Signifies the node that should be checked-in to signal that the task is ready for 
 * review.<P> 
 * 
 * All nodes with the CommonTask annotation with a Purpose of Thumbnail, Prepare, Focus or 
 * Edit should be upstream of this node so that they are assured of being included in 
 * the tree of nodes checked-in with this node. 
 * 
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Project Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the project this task part of achieving.
 *   </DIV> <BR> 
 * 
 *   Entity Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The Shotgun entity type owning this task or [[IGNORE]] if not using Shotgun.
 *   </DIV> <BR> 
 *   
 *   Task Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the overall production goal this node is used to achieve.  Typically, this
 *     is the name of a shot or the asset name.
 *   </DIV> <BR> 
 * 
 *   Task Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The standard type of production goal this node is used to achieve.
 *   </DIV> <BR> 
 *   
 *   Custom Task Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     A unique type of production goal this node is used to achieve which is not one 
 *     of the standard type available in TaskType.  If a custom type is specified, the
 *     TaskType parameter should be set to [[CUSTOM]].
 *   </DIV> <BR> 
 *   
 *   Assigned To <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the Pipeline wwork group or specific artist assigned to complete the task 
 *     involving this node.  Only a user assigned to a node (or part of the work group 
 *     assigned to the node) is allowed to check-in this node. 
 *   </DIV>   
 * </DIV> <P> 
 */
public 
class SubmitTaskAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  SubmitTaskAnnotation() 
  {
    super("SubmitTask", new VersionID("2.4.1"), "Temerity", 
          "Signifies the node that should be checked-in to signal that a task is " + 
          "ready for creative/technical review.");
    
    {
      AnnotationParam param = 
        new StringAnnotationParam
        (aProjectName, 
         "The name of the project this task part of achieving.", 
         null); 
      addParam(param);
    }
 
    {
      String choices[] = {"Shot", "Asset", "[[IGNORE]]"};
      AnnotationParam param = 
        new EnumAnnotationParam
        (aEntityType, 
         "The Shotgun entity type owning this task or [[IGNORE]] if not using Shotgun.", 
         "[[IGNORE]]", new ArrayList<String>(Arrays.asList(choices))); 
      addParam(param);
    }

    {
      AnnotationParam param = 
        new StringAnnotationParam
        (aTaskName, 
         "The name of the overall production goal this node is used to achieve.  " + 
         "Typically, this is the name of a shot or the asset name.",
         null); 
      addParam(param);
    }

    {
      AnnotationParam param = 
        new EnumAnnotationParam
        (aTaskType, 
         "The standard type of production goal this node is used to achieve.", 
         TaskType.Asset.toTitle(), TaskType.titles()); 
      addParam(param);
    }

    {
      AnnotationParam param = 
        new StringAnnotationParam
        (aCustomTaskType, 
         "A unique type of production goal this node is used to achieve which is not one " +
         "of the standard type available in TaskType.  If a custom type is specified, the " +
         "TaskType parameter should be set to [[CUSTOM]].",
         null); 
      addParam(param);
    }

    {
      AnnotationParam param = 
        new StringAnnotationParam
        (aPurpose, 
         "The way this node is intended to be used.  Always set to \"Submit\".", 
         NodePurpose.Submit.toTitle()); 
      addConstantParam(param);   
    }

    {
      AnnotationParam param = 
        new WorkGroupAnnotationParam
        (aAssignedTo, 
         "The name of the WorkGroup or specific artist assigned to complete the task " + 
         "involving this node.", 
         true, true, null); 
      addParam(param);
    }
    
    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aProjectName);
      layout.add(aEntityType);
      layout.add(aTaskName);
      layout.add(aTaskType);
      layout.add(aCustomTaskType);
      layout.add(aPurpose);
      layout.add(null);
      layout.add(aAssignedTo);

      setLayout(layout);
    }

    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3034801411821239877L;

  public static final String aProjectName    = "ProjectName";
  public static final String aEntityType     = "EntityType";
  public static final String aTaskName       = "TaskName";
  public static final String aTaskType       = "TaskType";
  public static final String aCustomTaskType = "CustomTaskType";
  public static final String aAssignedTo     = "AssignedTo";
  public static final String aPurpose        = "Purpose";
}
