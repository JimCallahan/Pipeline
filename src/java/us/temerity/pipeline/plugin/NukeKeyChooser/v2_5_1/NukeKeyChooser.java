// $Id: HfsBatchKeyChooser.java,v 1.1 2008/06/25 20:44:44 jim Exp $

package us.temerity.pipeline.plugin.NukeKeyChooser.v2_5_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.*;

import java.util.*;


/*------------------------------------------------------------------------------------------*/
/*   N U K E   K E Y   C H O O S E R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to turn a license key on for jobs who's Action plugin runs Nuke.
 */
public 
class NukeKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  NukeKeyChooser() 
  {
    super("Nuke", new VersionID("2.5.1"), "Temerity", 
          "Used to turn a license key on for jobs who's Action plugin runs Nuke."); 

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
    BaseAction action = job.getAction(); 
    String aname = action.getName(); 

    return (aname.equals("NukeBuild") ||
            aname.equals("NukeCatComp") ||
            aname.equals("NukeComp") ||
            aname.equals("NukeMakeHDR") ||
            aname.equals("NukeQt") ||
            aname.equals("NukeSubstComp") ||
            aname.equals("NukeThumbnail"));
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5293496856117450165L;

}
