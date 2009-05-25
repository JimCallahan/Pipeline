// $Id: ActionParamKeyChooser.java,v 1.1 2009/05/25 01:19:42 jesse Exp $

package us.temerity.pipeline.plugin.ActionParamKeyChooser.v2_4_6;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.KeyParam;
import us.temerity.pipeline.param.key.StringKeyParam;

/*------------------------------------------------------------------------------------------*/
/*   A C T I O N   P A R A M   K E Y   C H O O S E R                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Key Chooser that uses a regular expression match against a particular action parameter.
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
class ActionParamKeyChooser
  extends BaseKeyChooser
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ActionParamKeyChooser() 
  {
    super("ActionParam", new VersionID("2.4.6"), "Temerity", 
          "Uses a regular expression match against the node name.");
    
    {
      KeyParam param = 
        new StringKeyParam
        (aParamName, 
         "The name of the param to check.", 
         null); 
      addParam(param);
    }
    
    {
      KeyParam param = 
        new StringKeyParam
        (aParamPattern, 
         "The regular expression to match against the node name.", 
         null); 
      addParam(param);
    }
    
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aParamName);
    layout.addEntry(aParamPattern);
    
    setLayout(layout);
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
    String paramName = (String) getParamValue(aParamName);
    BaseAction act = job.getAction();
    ActionParam param = act.getSingleParam(paramName);
    if (param == null)
      return false;
    if ((param instanceof StringActionParam) || (param instanceof EnumActionParam)) {
      String pattern = (String) getParamValue(aParamPattern);
      if (pattern == null)
        return false;
      String value = (String) param.getValue();
      if (value != null)
        return value.matches(pattern);
      else
        return false;
    }
    else
      return false;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8730016311183902065L;
  
  public static final String aParamName = "ParamName";
  public static final String aParamPattern = "ParamPattern";
}
