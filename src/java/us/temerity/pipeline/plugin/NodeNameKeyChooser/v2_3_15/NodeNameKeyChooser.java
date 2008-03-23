// $Id: NodeNameKeyChooser.java,v 1.2 2008/03/23 19:06:25 jim Exp $

package us.temerity.pipeline.plugin.NodeNameKeyChooser.v2_3_15;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.KeyParam;
import us.temerity.pipeline.param.key.StringKeyParam;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   N A M E   K E Y   C H O O S E R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Key Chooser that uses a regular expression match against the node name to determine
 * whether the key should be on.<P> 
 * 
 * This key chooser defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Node Pattern <BR>
 *   <DIV style="margin-left: 40px;">
 *     The regular expression to match against the node name.
 *   </DIV> <BR>
 * </DIV> <BR>
 */
public 
class NodeNameKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  NodeNameKeyChooser() 
  {
    super("NodeName", new VersionID("2.3.15"), "Temerity", 
          "Uses a regular expression match against the node name.");
    
    {
      KeyParam param = 
        new StringKeyParam
        (aNodePattern, 
         "The regular expression to match against the node name.", 
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
    String pattern = (String) getParamValue(aNodePattern);
    if (pattern == null)
      return false;
    String nodeName = job.getNodeID().getName();
    return nodeName.matches(pattern);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7259431222116200839L;
  
  public static final String aNodePattern = "NodePattern";
}
