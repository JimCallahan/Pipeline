// $Id: UserNameKeyChooser.java,v 1.1 2007/12/16 11:11:34 jesse Exp $

package us.temerity.pipeline.plugin.UserNameKeyChooser.v2_3_15;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.KeyParam;
import us.temerity.pipeline.param.key.UserNameKeyParam;

/*------------------------------------------------------------------------------------------*/
/*   U S E R   N A M E   K E Y   C H O O S E R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Key Chooser that turns on the key for a single user.
 */
public 
class UserNameKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  UserNameKeyChooser() 
  {
    super("UserName", new VersionID("2.3.15"), "Temerity", 
          "Matches against a single user's name.");
    
    {
      KeyParam param = 
        new UserNameKeyParam
        (aUserName, 
         "The user name to match against the user who submitted the job.", 
         null); 
      addParam(param);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I S   A C T I V E                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return a value indicating whether the given node meets the criteria for this key.
   * <P> 
   * @param job
   *   The QueueJob that the key is going to apply to.  This contains the BaseAction and
   *   the ActionAgenda that can be mined for information.
   *   
   * @param annots
   *   The list of annotations assigned to the node the job is being created for.
   * 
   * @return 
   *   Whether this key is active for the job being created by the given node.
   * 
   * @throws PipelineException 
   *   If unable to return a value due to illegal, missing or incompatible 
   *   information in the node information or a general failure of the isActive method code.
   */
  @Override
  public boolean 
  isActive
  (
    QueueJob job,
    TreeMap<String, BaseAnnotation> annots
  )
    throws PipelineException
  {
    String user = (String) getParamValue(aUserName);
    if (user == null)
      return false;
    String nodeUser = job.getNodeID().getAuthor();
    return user.equals(nodeUser);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2444419689917975552L;
  
  public static final String aUserName = "UserName";
}
