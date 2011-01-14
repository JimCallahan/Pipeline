// $Id: HfsBatchKeyChooser.java,v 1.1 2008/06/25 20:44:44 jim Exp $

package us.temerity.pipeline.plugin.MaxKeyChooser.v2_5_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.*;

import java.util.*;


/*------------------------------------------------------------------------------------------*/
/*   M A X   K E Y   C H O O S E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to turn a license key on for jobs who's Action plugin runs a 3d Studio Max binary.
 * 
 * This key chooser defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Key Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The kind of 3dsMax license key being matched:<BR>
 *     <DIV style="margin-left: 40px;">
 *       Render - The key will be turned on for actions using the "3dsmaxcmd.exe" binary.<P>
 *       Interactive - The key will be turned on for actions using the "3dsmax.exe" binary.<P>
 *   </DIV> <BR>
 * </DIV> <BR>
 */
public 
class MaxKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  MaxKeyChooser() 
  {
    super("Max", new VersionID("2.5.1"), "Temerity", 
          "Used to turn a license key on for jobs who's Action plugin runs a 3d Studio " +
          "Max binary.");

    {
      ArrayList<String> choices = new ArrayList<String>(); 
      choices.add(aRender); 
      choices.add(aInteractive);    

      KeyParam param = 
        new EnumKeyParam
        (aKeyType, 
         "The kind of 3ds Max license key being matched.", 
         aInteractive, choices); 
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

    return ((aname.equals("MaxRender") && keyType.equals(aRender)) ||
            (aname.equals("MaxScript") && keyType.equals(aInteractive))); 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 572091776882752838L;

  public static final String aKeyType       = "KeyType";
  public static final String aRender        = "Render"; 
  public static final String aInteractive   = "Interactive";
}
