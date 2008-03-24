// $Id: HfsBatchKeyChooser.java,v 1.1 2008/03/24 07:14:32 jim Exp $

package us.temerity.pipeline.plugin.HfsBatchKeyChooser.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.*;

import java.util.*;


/*------------------------------------------------------------------------------------------*/
/*   H F S   B A T C H   K E Y   C H O O S E R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to turn the "HfsBatch" or "HfsMaster" license key on for jobs who's Action plugin 
 * requires a Houdini "Batch" or "Master" license. 
 * 
 * This key chooser defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Key Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The kind of Houdini license key being matched:<BR>
 *     <DIV style="margin-left: 40px;">
 *       Houdini Master - The key will be turned on for actions which set the 
 *       UseGraphicalLicense parameter to (YES).<P>
 *       Houdini Batch - The key will be turned on for actions which set the 
 *       UseGraphicalLicense parameter to (NO).<P>
 *   </DIV> <BR>
 * </DIV> <BR>
 */
public 
class HfsBatchKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  HfsBatchKeyChooser() 
  {
    super("HfsBatch", new VersionID("2.4.1"), "Temerity", 
          "Used to turn the \"HfsBatch\" or \"HfsMaster\" license key on for jobs who's " + 
          "Action plugin requires a Houdini \"Batch\" or \"Master\" license."); 

    {
      ArrayList<String> choices = new ArrayList<String>(); 
      choices.add(aHoudiniMaster); 
      choices.add(aHoudiniBatch);    

      KeyParam param = 
        new EnumKeyParam
        (aKeyType, 
         "The kind of Houdini license key being matched.", 
         aHoudiniBatch, choices); 
      addParam(param);
    }

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
    String keyType = (String) getParamValue(aKeyType);
    if(keyType == null) 
      throw new PipelineException
        ("Somehow the " + aKeyType + " parameter was (null)!");

    BaseAction action = job.getAction(); 
    String aname = action.getName(); 

    if(aname.equals("HfsClip") || 
       aname.equals("HfsBuild") || 
       aname.equals("HfsScript") || 
       aname.equals("HfsGEO") || 
       aname.equals("HfsComposite") || 
       aname.equals("HfsGenerateAction") || 
       aname.equals("HfsSdExport") || 
       aname.equals("HfsRender")) {

      boolean useGL = false;
      {
        Boolean value = (Boolean) action.getSingleParamValue(aUseGraphicalLicense);  
        useGL = ((value != null) && value);
      }

      if(((keyType.equals(aHoudiniMaster)) && useGL) ||
         ((keyType.equals(aHoudiniBatch)) && !useGL))
        return true; 
    }

    return false;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2712263122914872814L;

  public static final String aKeyType             = "KeyType";
  public static final String aHoudiniMaster       = "Houdini Master";
  public static final String aHoudiniBatch        = "Houdini Batch";
  public static final String aUseGraphicalLicense = "UseGraphicalLicense";
}
