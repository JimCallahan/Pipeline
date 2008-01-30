// $Id: AnnotationPermissions.java,v 1.1 2008/01/30 16:52:44 jesse Exp $

package us.temerity.pipeline;

import java.io.Serializable;

/*------------------------------------------------------------------------------------------*/
/*   A N N O T A T I O N   P E R M I S S I O N S                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Class containing two boolean values, one related to annotation creation and one relating
 * to annotation removal.
 * <p>
 * These are created by the Plugin Manger at start-up time (and plugin installation time)
 * and passed around with the plugin cache to client nodes.
 */
public 
class AnnotationPermissions
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  AnnotationPermissions
  (
    boolean userCreatable,
    boolean userRemovable
  )
  {
    pUserCreatable = userCreatable;
    pUserRemovable = userRemovable;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the annotation this is associated with creatable by ordinary users.
   */
  public boolean
  isUserCreatable()
  {
    return pUserCreatable;
  }
  
  /**
   * Is the annotation this is associated with removable by ordinary users.
   */
  public boolean
  isUserRemovable()
  {
    return pUserRemovable;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5599911589407035786L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  
  private boolean pUserCreatable;
  private boolean pUserRemovable;
}
