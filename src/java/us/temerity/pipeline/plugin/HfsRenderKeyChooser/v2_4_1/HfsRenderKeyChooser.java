// $Id: HfsRenderKeyChooser.java,v 1.1 2008/03/24 07:14:32 jim Exp $

package us.temerity.pipeline.plugin.HfsRenderKeyChooser.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.*;

import java.util.*;


/*------------------------------------------------------------------------------------------*/
/*   H F S   R E N D E R   K E Y   C H O O S E R                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to turn the "HfsRender" license key on for jobs who's Action plugin requires a 
 * Houdini "Render" license. 
 */
public 
class HfsRenderKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  HfsRenderKeyChooser() 
  {
    super("HfsRender", new VersionID("2.4.1"), "Temerity", 
          "Used to turn the \"HfsRender\" license key on for jobs who's Action plugin " + 
          "requires a Houdini \"Render\" license."); 

    underDevelopment(); 
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
    String aname = job.getAction().getName(); 

    if(aname.equals("HfsMantra")) 
      return true;

    return false;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8138299723864680141L;

}
