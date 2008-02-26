// $Id: CommonTaskAnnotation.java,v 1.4 2008/02/26 11:34:48 jim Exp $

package com.intelligentcreatures.pipeline.plugin.CommonTaskAnnotation.v1_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M M O N   T A S K   A N N O T A T I O N                                            */
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
 * illict versioning of nodes which are used downstream in the production process can be 
 * prevented.
 */
public
class CommonTaskAnnotation
  extends BaseAnnotation
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  CommonTaskAnnotation()
  {
    super("CommonTask", new VersionID("1.0.0"), "ICVFX", 
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
      ArrayList<String> choices = new ArrayList<String>(); 
      choices.add(aPrereq); 
      choices.add(aEdit); 
      choices.add(aPrepare); 
      choices.add(aFocus); 
      choices.add(aThumbnail); 
      choices.add(aProduct); 

      AnnotationParam param = 
	new EnumAnnotationParam
	(aPurpose, 
	 "The way this node is intended to be used.", 
	 "Edit", choices); 
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

  private static final long serialVersionUID = -3738625679634142201L;

  public static final String aProjectName    = "ProjectName";
  public static final String aTaskName       = "TaskName";
  public static final String aTaskType       = "TaskType";
  public static final String aCustomTaskType = "CustomTaskType";
  
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

  public static final String aPurpose   = "Purpose";
  public static final String aPrereq    = "Prereq";
  public static final String aEdit      = "Edit";
  public static final String aPrepare   = "Prepare";
  public static final String aFocus     = "Focus";
  public static final String aThumbnail = "Thumbnail";
  public static final String aProduct   = "Product";
}


