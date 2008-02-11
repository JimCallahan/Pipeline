// $Id: ApproveTaskAnnotation.java,v 1.3 2008/02/11 22:56:00 jim Exp $

package com.intelligentcreatures.pipeline.plugin.ApproveTaskAnnotation.v1_0_0;

import java.util.ArrayList;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A P P R O V E   T A S K   A N N O T A T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Signifies the node that should be checked-in to signal that the task has been approved.<P> 
 * 
 * All nodes with the CommonTask annotation with a Purpose of Product, Prepare or 
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
 *     The way this node is intended to be used.  Always set to "Approve".
 *   </DIV> <BR> 
 * 
 *   Supervised By <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the Pipeline work group or specific artist assigned to review the 
 *     state of completion of the task involving this node.  Only a user assigned to this
 *     node (or part of the work group assigned to this node) should be allowed to 
 *     check-in this node.  If unset, anyone can supervise the task for this node.
 *   </DIV> <BR>
 * 
 *   Approval Builder <BR>
 *   <DIV style="margin-left: 40px;">
 *     If specified, the custom approval builder to run after the task has been approved
 *     in order to update and check-in this node.  If not given, the approval network will
 *     need to be manually updated and checked-in.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public 
class ApproveTaskAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ApproveTaskAnnotation() 
  {
    super("ApproveTask", new VersionID("1.0.0"), "ICVFX", 
	  "Signifies the node that should be checked-in to signal that a task has " + 
          "been approved."); 
    
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
      ArrayList<String> choices = new ArrayList<String>(); 
      choices.add(aSimpleAsset);    
      choices.add(aModeling);        
      choices.add(aRigging);         
      choices.add(aLookDev);        
      choices.add(aLayout);          
      choices.add(aAnimation);       
      choices.add(aEffects);         
      choices.add(aLighting);  
      choices.add(aPlates);        
      choices.add(aTracking);     
      choices.add(aRoto);         
      choices.add(aMattePainting);            
      choices.add(aCompositing);            
      choices.add(aCUSTOM);  

      AnnotationParam param = 
	new EnumAnnotationParam
	(aTaskType, 
	 "The standard type of production goal this node is used to achieve.", 
         aSimpleAsset, choices); 
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
	 "The way this node is intended to be used.  Always set to \"Approve\".", 
	 aApprove); 
      addConstantParam(param);   
    }

    {
      AnnotationParam param = 
	new WorkGroupAnnotationParam
	(aSupervisedBy, 
	 "The name of the Pipeline work group or specific artist assigned to review the " + 
         "state of completion of the task involving this node.  Only a user assigned to " + 
         "this node (or part of the work group assigned to this node) should be allowed " + 
         "to check-in this node.  If unset, anyone can supervise the task for this node.", 
	 true, true, null); 
      addParam(param);
    }
    
    {
      AnnotationParam param = 
 	new BuilderIDAnnotationParam
 	(aApprovalBuilder, 
         "If specified, the name of a custom approval builder within a specific builder " + 
         "collection to run after the task has been approved in order to update and " + 
         "check-in this node.  If not given, the approval network will need to be " + 
         "manually updated and checked-in.", 
          new BuilderID("WtmBuilders", new VersionID("1.0.0"), "ICVFX", "ApproveTask")); 
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aProjectName);
      layout.add(aTaskName);
      layout.add(aTaskType);
      layout.add(aCustomTaskType);
      layout.add(aPurpose);
      layout.add(null); 
      layout.add(aSupervisedBy);
      layout.add(null); 
      layout.add(aApprovalBuilder);

      setLayout(layout);
    }

    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6244454414171268316L;

  public static final String aProjectName     = "ProjectName";
  public static final String aTaskName        = "TaskName";
  public static final String aTaskType        = "TaskType";
  public static final String aCustomTaskType  = "CustomTaskType";
  public static final String aSupervisedBy    = "SupervisedBy";
  public static final String aApprovalBuilder = "ApprovalBuilder";
  
  public static final String aSimpleAsset   = "Simple Asset";  
  public static final String aModeling      = "Modeling";        
  public static final String aRigging       = "Rigging";         
  public static final String aLookDev       = "Look Dev";        
  public static final String aLayout        = "Layout";          
  public static final String aAnimation     = "Animation";       
  public static final String aEffects       = "Effects";         
  public static final String aLighting      = "Lighting"; 
  public static final String aPlates        = "Plates"; 
  public static final String aTracking      = "Tracking"; 
  public static final String aRoto          = "Roto"; 
  public static final String aMattePainting = "MattePainting"; 
  public static final String aCompositing   = "Compositing";  
  public static final String aCUSTOM        = "[[CUSTOM]]";  
  
  public static final String aPurpose = "Purpose";
  public static final String aApprove = "Approve";
}
