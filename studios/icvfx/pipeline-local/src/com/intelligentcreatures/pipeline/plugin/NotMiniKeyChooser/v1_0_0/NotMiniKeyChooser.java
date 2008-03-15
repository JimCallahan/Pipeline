// $Id: NotMiniKeyChooser.java,v 1.3 2008/03/15 18:17:45 jim Exp $

package com.intelligentcreatures.pipeline.plugin.NotMiniKeyChooser.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.*;

import java.util.*;


/*------------------------------------------------------------------------------------------*/
/*   N O T   M I N I   K E Y   C H O O S E R                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to turn the "NotMini" key on for jobs who's Action plugin should not be executed 
 * on the Mac Mini. 
 */
public 
class NotMiniKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  NotMiniKeyChooser() 
  {
    super("NotMini", new VersionID("1.0.0"), "ICVFX", 
          "Used to turn the \"NotMini\" key on for jobs who's Action plugin should not be " + 
          "executed on the Mac Mini."); 

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

    /* just QuickTime related actions will be allowed to run on the Mac Mini for now... */ 
    if(aname.equals("NukeQt")) 
      return false;

    return true; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1736117527147337485L;

}
