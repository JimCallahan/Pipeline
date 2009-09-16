// $Id: TaskAnnotation.java,v 1.6 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TaskAnnotation.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   A N N O T A T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Signifies the nodes that make up a common production goal. <P> 
 * 
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Task Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the common production goal this node is used to achieve.
 *   </DIV> <BR> 
 * 
 *   Task Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The type of production goal this node is used to achieve.
 *   </DIV> <P> 
 * 
 *   Usage <BR>
 *   <DIV style="margin-left: 40px;">
 *     The way this node is intended to be used: <BR>
 *     <DIV style="margin-left: 40px;">
 *       Edit - Identifies the nodes that an artist should be editing in order to 
 *              accomplish a task.<P>
 * 
 *       Prepare - A node that exists between the Edit and Submit nodes used to prepare
 *                 the data for submission but has no direct utility for the artist.<P>
 * 
 *       Focus - A node that should be inspected as part of the review process for a task.<P>
 * 
 *       Thumbnail - Identifies nodes associated with a single JPEG image suitable to 
 *                   represent a Focus node on a web page.<P>
 * 
 *       Product -  A node versioned along with the Approve node which represents the 
 *                  post-approval products of the task.<P>
 * 
 *       Approve - A node that needs to be rebuilt each time a task has been reviewed 
 *                 and approved. <P>
 *     </DIV> <BR> 
 *   </DIV> <BR> 
 * </DIV> <P>
 * 
 * Some additional notes on the Usage parameter: <P> 
 * 
 * Prepare nodes cannot be checked-in except along with a Submit node in order to prevent
 * the creation of extraneous versions of nodes that are unecessary for the approval 
 * process.<P> 
 * 
 * Focus nodes are located downstream from the EditNode and upstream from the 
 * Submit node for a task and are associated with the data that will be reviewed by 
 * the supervisor of the task in order to determine if the task has been completed
 * satisfactorily. <P> 
 * 
 * Thumbnail nodes should correspond to a specific Focus node.  The Focus node for a 
 * Thumbnail is determined by following the upstream connections util the first Focus node 
 * for the same task is encountered.  Typically this is the only node directed linked 
 * to the Thumbnail node.<P> 
 * 
 * Submit nodes have several additional parameters and are therefore represented by their own 
 * Annotation plugin class called SubmitNode. <P> 
 * 
 * Product nodes exist to prevent a node from being checked in when the Approval node
 * is not being checked-in.  This prevents illict versioning of things that could 
 * contaminate the production process.<P> 
 * 
 * Check-in of an Approve node signals the completion of an approval step. In other words, 
 * that all nodes upstream of this node have been reviewed and officially approved. <P> 
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
    super("Task", new VersionID("2.3.2"), "Temerity", 
          "Signifies the nodes that make up a common production goal."); 

    {
      AnnotationParam param = 
	new StringAnnotationParam
	(aTaskName, 
	 "The name of the common production goal this node is used to achieve.", 
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
      choices.add(aCompositing);
      choices.add(aRoto);
      choices.add(aPlates);
      

      AnnotationParam param = 
	new EnumAnnotationParam
	(aTaskType, 
	 "The type of production goal this node is used to achieve.", 
	 aSimpleAsset, choices); 
      addParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>(); 
      choices.add(aEdit); 
      choices.add(aPrepare); 
      choices.add(aFocus); 
      choices.add(aThumbnail); 
      choices.add(aProduct); 
      choices.add(aApprove); 

      AnnotationParam param = 
	new EnumAnnotationParam
	(aPurpose, 
	 "The way this node is intended to be used.", 
	 "Edit", choices); 
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aTaskName); 
      layout.add(aTaskType); 
      layout.add(null);
      layout.add(aPurpose); 

      setLayout(layout);      
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5029661173906269492L;

  public static final String aTaskName = "TaskName";
  public static final String aTaskType = "TaskType";
  public static final String aPurpose  = "Purpose";

  public static final String aSimpleAsset = "Simple Asset";  
  public static final String aModeling    = "Modeling";        
  public static final String aRigging     = "Rigging";         
  public static final String aLookDev     = "Look Dev";        
  public static final String aLayout      = "Layout";          
  public static final String aAnimation   = "Animation";       
  public static final String aEffects     = "Effects";         
  public static final String aLighting    = "Lighting";        
  public static final String aCompositing = "Compositing";
  public static final String aRoto        = "Roto";
  public static final String aPlates      = "Plates";
  

  public static final String aEdit      = "Edit";
  public static final String aPrepare   = "Prepare";
  public static final String aFocus     = "Focus";
  public static final String aThumbnail = "Thumbnail";
  public static final String aProduct   = "Product";
  public static final String aApprove   = "Approve";

}


