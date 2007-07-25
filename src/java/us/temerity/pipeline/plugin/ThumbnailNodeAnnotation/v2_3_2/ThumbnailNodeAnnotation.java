package us.temerity.pipeline.plugin.ThumbnailNodeAnnotation.v2_3_2;

import java.util.ArrayList;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T H U M B N A I L   N O D E   A N N O T A T I O N                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Identifies nodes associated with a single JPEG image suitable to represent a focus node
 * on a web page.<P> 
 * 
 * Thumbnail nodes should correspond to a specific focus node which should have a name 
 * derived by stripping off the "thumbs" last directory component of the thumbnail node name. 
 * For instance, for a thumbnail node named "/some-path/thumbs/myimages" there should be a
 * focus node named "/some-path/myimages".  For this example, the thumbnail image would be 
 * named "myimage.jpg".<P> 
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
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * @deprecated
 *   This class has been made obsolete by the TaskAnnotation (v2.3.2).
 */
@Deprecated
public 
class ThumbnailNodeAnnotation 
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ThumbnailNodeAnnotation()
  {
    super("ThumbnailNode", new VersionID("2.3.2"), "Temerity", 
	  "Identifies nodes associated with a single JPEG image suitable to represent a " + 
          "focus node on a web page."); 
    
    {
      AnnotationParam param = 
	new StringAnnotationParam
	(aTaskName, 
	 "The name of the common production goal this node is used to achieve.", 
	 null); 
      addParam(param);
    }

    {
      AnnotationParam param = 
	new StringAnnotationParam
	(aTaskType, 
	 "The type of production goal this node is used to achieve.", 
	 null); 
      addParam(param);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aTaskName = "TaskName";
  public static final String aTaskType = "TaskType";
  
  //private static final long serialVersionUID = 
  
}
