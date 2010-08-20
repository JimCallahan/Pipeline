package us.temerity.pipeline.plugin.TaskAnnotation.v2_4_28;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_28.*;

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
 *   Entity Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The Shotgun entity type owning this task or [[IGNORE]] if not using Shotgun.
 *   </DIV> <BR> 
 *   
 *   Task Ident1 <BR>
 *   <DIV style="margin-left: 40px;">
 *     The first part of the name of the overall production goal this node is used to achieve.  
 *     Typically this represents the larger grouping that the task is part of, potentially 
 *     a sequence name or the type of an asset.
 *   </DIV> <BR>
 *   
 *   Task Ident2 <BR>
 *   <DIV style="margin-left: 40px;">
 *     The second part of the name of the overall production goal this node is used to 
 *     achieve. Typically this represents the smaller, more exact name of the task, 
 *     potentially a shot name or the name of an asset.
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
 *     The way this node is intended to be used.  See the javadocs in {@link NodePurpose} for
 *     more extensive discussions of the different purposes and how they are meant to be 
 *     used. <BR>
 *   </DIV> <BR> 
 *   
 *   Master <BR>
 *   <DIV style="margin-left: 40px;">
 *     Only used with focus nodes, this settings allows a specific focus node to be specified 
 *     as the most important focus node.
 *   </DIV> <BR> 
 * </DIV> <P>
 * 
 * Some additional notes on the Purpose parameter: <P> 
 *
 * Unlike in previous versions of the Task system, there are not different annotation for 
 * Submit, Approve (which has been replaced by Publish in any event), Synch or Focus.  All of
 * the features which these annotations might have performed have either been eliminated, 
 * moved into other annotations, or are now being handled in a different matter.  The goal is
 * to streamline the task system, but to also make it more powerful.
 * 
 * Prepare nodes cannot be checked-in unless the root node of the check-in is a Submit, 
 * Verify, or Publish node in order to prevent the creation of extraneous versions of nodes 
 * that are not necessary for the approval process.  This prohibition is managed by the 
 * TaskGuard Server-Side Extension.<P> 
 * 
 * Focus nodes are need to be located downstream from an Edit node (or be the edit node, 
 * perhaps)and upstream from the Verify node for a task and are associated with the data 
 * that will be reviewed by the supervisor of the task in order to determine if the task has 
 * been completed satisfactorily. <P> 
 * 
 * Each Thumbnail node should correspond to a specific Focus node.  The Focus node for a 
 * Thumbnail is determined by following the upstream connections until the first Focus node 
 * for the same task is encountered.  Typically this is the only node directed linked 
 * to the Thumbnail node.  In some cases, the thumbnail may not be generated directly from the
 * Focus node (one example would be a focus node QT which is generated from an image 
 * sequence.  The thumbnail could be generated from the image sequence, for simplicity's 
 * sake, but that would not place it downstream of the Focus node.  In these cases, simply 
 * make the Focus node an Association of the the Thumbnail node.  This will cause it to be 
 * correctly found when the task system scans the network, but will not affect the evaluation 
 * of the node network.<P> 
 * 
 * The Product node purpose should be assigned to nodes which should not be checked-in 
 * unless part of a node network rooted at an Publish node.  Such check-ins represent an 
 * officially approved update.  By restricting the check-in behavior of Product nodes, 
 * illicit versioning of nodes which are used downstream in the production process can be 
 * prevented.  There are some cases where studios wish to streamline the production process 
 * and have Product nodes which are also Edit nodes.  While we strenuously advise against such
 * a step (any expediency gained is offset by the problems that can be caused due to 
 * unleashing un-verified products on people downstream.  It would be more conducive to make
 * the Publish and Verify node the same node and combine verification and publishing in a 
 * single step.  This still has the benefits of near-instantaneous Product generation, but 
 * allows for the imposition of quality control steps that can hopefully spare downstream 
 * artists from being exposed to products of dubious quality.
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
    super("Task", new VersionID("2.4.28"), "Temerity", 
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
        new EnumAnnotationParam
        (aEntityType, 
         "The Shotgun entity type owning this task or [[IGNORE]] if not using Shotgun.", 
         EntityType.Ignore.toTitle(), EntityType.titles()); 
      addParam(param);
    }

    {
      AnnotationParam param = 
        new StringAnnotationParam
        (aTaskIdent1, 
         "The first identifier used to name to the task.",
         null); 
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new StringAnnotationParam
        (aTaskIdent2, 
         "The second identifier used to name to the task.",
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
         NodePurpose.Edit.toTitle(), NodePurpose.titles()); 
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new BooleanAnnotationParam
        (aMaster,
         "Used to indicate the most important Focus node.  This is ignored for all other Node " +
         "Purposes",
         false
        );
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aProjectName);
      layout.add(aTaskIdent1);
      layout.add(aTaskIdent2);
      layout.add(null);
      layout.add(aTaskType);
      layout.add(aCustomTaskType);
      layout.add(null);
      layout.add(aPurpose);
      layout.add(aMaster);
      layout.add(null);
      layout.add(aEntityType);

      setLayout(layout);      
    }
    
    underDevelopment();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 679012310359222818L;

  public static final String aProjectName    = "ProjectName";
  public static final String aEntityType     = "EntityType";
  public static final String aTaskIdent1     = "TaskIdent1";
  public static final String aTaskIdent2     = "TaskIdent2";
  public static final String aTaskType       = "TaskType";
  public static final String aCustomTaskType = "CustomTaskType";
  public static final String aPurpose        = "Purpose";
  public static final String aMaster         = "Master";
}


