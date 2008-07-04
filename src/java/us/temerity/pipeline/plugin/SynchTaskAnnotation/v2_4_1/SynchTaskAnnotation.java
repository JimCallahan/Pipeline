// $Id: SynchTaskAnnotation.java,v 1.1 2008/07/04 15:33:13 jesse Exp $

package us.temerity.pipeline.plugin.SynchTaskAnnotation.v2_4_1;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_1.*;

/*------------------------------------------------------------------------------------------*/
/*   S Y N C H   T A S K   A N N O T A T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A node that should be checked-in when the Unify nodes for a task have been updated.<P> 
 * 
 * All nodes with the CommonTask annotation with a Purpose of Unify should be upstream of 
 * this node so that they are assured of being included in the tree of nodes checked-in with 
 * this node. 
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
 *   Purpose <BR>
 *   <DIV style="margin-left: 40px;">
 *     The way this node is intended to be used.  Always set to "Synch".
 *   </DIV> <BR> 
 * 
 *   Controlled By <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the Pipeline work group or specific artist assigned to update the Unify
 *     nodes associated with this task.  Only a user assigned to this node (or part of the 
 *     work group assigned to this node) should be allowed to check-in this node.  If 
 *     unset, anyone can synch the task for this node.
 *   </DIV> <BR>
 * 
 *   Synch Builder <BR>
 *   <DIV style="margin-left: 40px;">
 *     If specified, the custom synch builder to run whenever synchronization is needed.
 *     If not given, the synch network will need to be manually updated and checked-in.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public 
class SynchTaskAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  SynchTaskAnnotation() 
  {
    super("SynchTask", new VersionID("2.4.1"), "Temerity", 
          "A node that should be checked-in when the Unify nodes for a task " +
          "have been updated."); 
    
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
         "The way this node is intended to be used.  Always set to \"Synch\".", 
         NodePurpose.Synch.toTitle()); 
      addConstantParam(param);   
    }
    
    {
      AnnotationParam param = 
        new WorkGroupAnnotationParam
        (aControlledBy, 
         "The name of the Pipeline work group or specific artist assigned to update the Unify" +
         "nodes associated with this task.  Only a user assigned to this node (or part of " +
         "the work group assigned to this node) should be allowed to check-in this node.  " +
         "If unset, anyone can synch the task for this node.", 
         true, true, null); 
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BuilderIDAnnotationParam
        (aSynchBuilder, 
         "If specified, the name of a custom synch builder within a specific builder " + 
         "collection to run when the task needs to be synched in order to update and " + 
         "check-in this node.  If not given, the synch network will need to be " + 
         "manually updated and checked-in.", 
          new BuilderID("Approval", new VersionID("2.4.1"), "Temerity", "SynchTask")); 
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
      layout.add(aControlledBy);
      layout.add(null); 
      layout.add(aSynchBuilder);

      setLayout(layout);
    }

    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -7193092802924952018L;
  
  public static final String aProjectName     = "ProjectName";
  public static final String aEntityType      = "EntityType";
  public static final String aTaskName        = "TaskName";
  public static final String aTaskType        = "TaskType";
  public static final String aCustomTaskType  = "CustomTaskType";
  public static final String aControlledBy    = "ControlledBy";
  public static final String aSynchBuilder    = "SynchBuilder";
  public static final String aPurpose         = "Purpose";
}
