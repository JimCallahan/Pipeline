// $Id: TaskAnnotation.java,v 1.1 2008/05/12 16:41:50 jesse Exp $

package us.temerity.pipeline.plugin.TaskAnnotation.v2_4_1;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_1.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   A N N O T A T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Signifies the nodes that make up a common production goal (task). <P> 
 * 
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Project Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the project this task part of achieving.
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
 *   Purpose <BR>
 *   <DIV style="margin-left: 40px;">
 *     The way this node is intended to be used: <BR>
 *     <DIV style="margin-left: 40px;">
 *       Prereq - Identifies the node that should be checked-in out together to provide 
 *                a compatible set of Product nodes required as inputs into a Submit node 
 *                network.
 * 
 *       Edit - Identifies the nodes that an artist should interactive modify in order to 
 *              accomplish a task.<P>
 * 
 *       Focus - A typically procedurally generated node that should be inspected by a 
 *               tasks supervisor part of the review process.<P>
 * 
 *       Thumbnail - Identifies nodes associated with a single JPEG image suitable to 
 *                   represent a Focus node for external production tracking applications
 *                   or web based systems.<P>
 * 
 *       Product -  A node containing a procedurally generated post-approval product of 
 *                  the task used as input for tasks downstream in the production process.<P> 
 * 
 *       Prepare - A node used in either the submit or approval networks as part of the 
 *                 process which will generates one or more of the other procedural types 
 *                 of nodes in the task but which has no direct utility to either artists 
 *                 or supervisors.
 * 
 *       Deliver - A node representing a QuickTime movie delivered to clients or a DDR 
 *                 for internal dailies. 
 *     </DIV> <BR> 
 *   </DIV> <BR> 
 * </DIV> <P>
 * 
 * Some additional notes on the Purpose parameter: <P> 
 * 
 * Submit nodes have several additional parameters and are therefore represented by their own 
 * annotation plugin class called SubmitTask.  Similarly, Approve nodes are represented by
 * the ApproveTask annotation. The SubmitTask and ApproveTask annotations have an effective 
 * Purpose of Submit and Approve respectively, although they do not have parameters called 
 * Purpose.<P>
 * 
 * Prereq nodes a simply a grouping of the Product nodes from all upstream tasks required
 * as inputs into the current task.  To avoid the possibility of incompatible changes coming
 * from different upstream tasks from being used together in a submit network, the Prereq
 * node should be used to indirectly check-out new version of Product nodes rather than 
 * by checking-out the Product nodes individually.  This insured that compatible versions
 * of Product nodes will always be used together.  If not new version of a Prereq node 
 * exists for a task, then newer Product nodes should be ignored.<P> 
 * 
 * Prepare nodes cannot be checked-in unless the root node of the check-in is a Submit or 
 * Approve node in order to prevent the creation of extraneous versions of nodes that are 
 * unecessary for the approval process.<P> 
 * 
 * Focus nodes are located downstream from an Edit node and upstream from the 
 * Submit node for a task and are associated with the data that will be reviewed by 
 * the supervisor of the task in order to determine if the task has been completed
 * satisfactorily. <P> 
 * 
 * Each Thumbnail node should correspond to a specific Focus node.  The Focus node for a 
 * Thumbnail is determined by following the upstream connections util the first Focus node 
 * for the same task is encountered.  Typically this is the only node directed linked 
 * to the Thumbnail node.<P> 
 * 
 * The Product node purpose should be assigned to nodes which should not be checked-in 
 * unless part of a node network rooted at an Approve node.  Such check-ins represent an 
 * officially approved update.  By restricting the check-in behavior of Product nodes, 
 * Illicit versioning of nodes which are used downstream in the production process can be 
 * prevented.
 */
public
class TaskAnnotation
  extends BaseAnnotation
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TaskAnnotation()
  {
    super("Task", new VersionID("2.4.1"), "Temerity", 
          "Signifies the nodes that make up a common production goal (task)."); 

    {
      AnnotationParam param = 
        new StringAnnotationParam
        (aProjectName, 
         "The name of the project this task part of achieving.", 
         null); 
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
        new EnumAnnotationParam
        (aPurpose, 
         "The way this node is intended to be used.", 
         NodePurpose.Edit.toTitle(), NodePurpose.commonTitles()); 
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aProjectName);
      layout.add(aTaskName);
      layout.add(aTaskType);
      layout.add(aCustomTaskType);
      layout.add(aPurpose); 

      setLayout(layout);      
    }

    underDevelopment(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2160674856425757963L;

  public static final String aProjectName    = "ProjectName";
  public static final String aTaskName       = "TaskName";
  public static final String aTaskType       = "TaskType";
  public static final String aCustomTaskType = "CustomTaskType";
  public static final String aPurpose        = "Purpose";
}


