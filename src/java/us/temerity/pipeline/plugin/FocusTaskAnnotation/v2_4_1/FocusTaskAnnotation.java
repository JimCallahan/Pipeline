// $Id: FocusTaskAnnotation.java,v 1.3 2009/05/13 19:01:58 jesse Exp $

package us.temerity.pipeline.plugin.FocusTaskAnnotation.v2_4_1;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_1.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   A N N O T A T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Signifies the node associated with the data to be reviewed by the supervisor 
 * of the task in order to determine if the task has been completed satisfactorily. <P> 
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
 * </DIV> <P>
 */
public
class FocusTaskAnnotation
  extends BaseAnnotation
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  FocusTaskAnnotation()
  {
    super("FocusTask", new VersionID("2.4.1"), "Temerity", 
          "Signifies the node associated with the data to be reviewed by the supervisor " + 
          "of a production goal (task)."); 

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
         "The way this node is intended to be used.  Always set to \"Focus\".", 
         NodePurpose.Focus.toTitle()); 
      addConstantParam(param);   
    }

    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aMaster, 
         "Denotes that this node is the most important of all Focus nodes for the task.",
         true); 
      addConstantParam(param);
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
      layout.add(aMaster);

      setLayout(layout);      
    }

    underDevelopment(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 820517377970207299L;

  public static final String aProjectName    = "ProjectName";
  public static final String aEntityType     = "EntityType";
  public static final String aTaskName       = "TaskName";
  public static final String aTaskType       = "TaskType";
  public static final String aCustomTaskType = "CustomTaskType";
  public static final String aPurpose        = "Purpose";
  public static final String aMaster         = "Master";
}


