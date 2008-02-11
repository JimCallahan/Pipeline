// $Id: NodeNoteAnnotation.java,v 1.3 2008/02/11 22:20:08 jim Exp $

package us.temerity.pipeline.plugin.NodeNoteAnnotation.v2_3_14;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   N O T E   A N N O T A T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * An annotation that allows users to leave notes on nodes.
 */
public 
class NodeNoteAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  NodeNoteAnnotation() 
  {
    super("NodeNote", new VersionID("2.3.14"), "Temerity", 
	  "Allows users to leave notes on nodes.");
    
    {
      AnnotationParam param = 
	new TextAreaAnnotationParam
	(aNodeNote, 
	 "The node note.", 
	 null,
	 7); 
      addParam(param);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public boolean 
  isUserAddable()
  {
    return true;
  }

  @Override
  public boolean
  isUserRemovable()
  {
    return true;
  }

  /**
   * Node notes are editable by all users. <P> 
   * 
   * @param pname  
   *   The name of the parameter. 
   * 
   * @param user
   *   The name of the user requesting access to modify the parameter.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the user. 
   */
  @Override
  public boolean
  isParamModifiable
  (
   String pname,
   String user, 
   PrivilegeDetails privileges
  )
  {
    return true;
  }
  
 
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8690038250725548028L;
  
  public static final String aNodeNote = "NodeNote";
}
