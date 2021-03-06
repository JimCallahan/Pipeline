package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   U T I L I T Y   P A R A M                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A Utility parameter that represents a list of boolean choices.
 */
public 
class ListUtilityParam
  extends ListParam<UtilityParam>
  implements UtilityParam
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class when
   * encountered during the reading of GLUE format files and should not be called from user
   * code.
   */
  public 
  ListUtilityParam() 
  {
    super();
  }

  /**
   * Construct a parameter with the given name, description, default value, and layout.

   * @param name
   *    The short name of the parameter.
   * 
   * @param desc
   *    A short description used in tooltips.
   * 
   * @param value
   *    The default value for this parameter. This is the chosen 
   *    subset of all possible values from <code>values</code>.
   * 
   * @param values
   *    The list of all possible values.
   *    
   * @param layout
   * 	A layout for all the parameters.  If this is <code>null</code>, then all
   *    the values will be arranged in alphabetical order.  Otherwise, this structure
   *    needs to contain a string value for each value the parameter could have in the 
   *    order that they are supposed to be displayed.  It can also contain <code>null</code> 
   *    values. Each <code>null</code> value will be displayed as a vertical space between
   *    values.
   *    
   * @param tooltips
   *  	A map of descriptions of what each value is, mapped by value name. 
   */
  public
  ListUtilityParam
  (
   String name, 
   String desc, 
   Set<String> value,
   Set<String> values,
   ArrayList<String> layout,
   TreeMap<String, String> tooltips
  )
  {
    super(name, desc, value, values, layout, tooltips);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R I C S   S U P P O R T                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Implemented to support Builder Parameters. 
   */
  @Override
  protected UtilityParam 
  createBooleanParam
  (
    String name,
    String desc,
    Boolean value
  )
  {
    return new BooleanUtilityParam(name, desc, value);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   H E L P E R S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a list param containing the currently user choosable Selection Keys.
   * 
   * @deprecated
   *   The practice of setting selection keys in a Builder has largely been made 
   *   obsolete by the creation of KeyChooser plugins which are able to determine 
   *   the settings for keys at runtime rather than when the node is created by the 
   *   builder.  
   */ 
  @Deprecated
  public static ListUtilityParam
  createSelectionKeyParam
  (
    String name,
    String desc,
    Set<String> value,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    TreeSet<String> initial = new TreeSet<String>();
    if(value != null) 
      initial.addAll(value); 
    
    TreeMap<String,String> table = qclient.getSelectionKeyDescriptions(true); 

    return new ListUtilityParam(name, desc, initial, table.keySet(), null, table); 
  }
  
  /**
   * Create a list param containing the currently user choosable Hardware Keys.
   * 
   * @deprecated
   *   The practice of setting hardware keys in a Builder has largely been made 
   *   obsolete by the creation of KeyChooser plugins which are able to determine 
   *   the settings for keys at runtime rather than when the node is created by the 
   *   builder.  
   */ 
  @Deprecated
  public static ListUtilityParam
  createHardwareKeyParam
  (
    String name,
    String desc,
    Set<String> value,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    TreeSet<String> initial = new TreeSet<String>();
    if(value != null) 
      initial.addAll(value); 
    
    TreeMap<String,String> table = qclient.getHardwareKeyDescriptions(true); 

    return new ListUtilityParam(name, desc, initial, table.keySet(), null, table); 
  }
  
  /**
   * Create a list param containing the currently user choosable License Keys.
   * 
   * @deprecated
   *   The practice of setting license keys in a Builder has largely been made 
   *   obsolete by the creation of KeyChooser plugins which are able to determine 
   *   the settings for keys at runtime rather than when the node is created by the 
   *   builder.  
   */ 
  @Deprecated
  public static ListUtilityParam
  createLicenseKeyParam
  (
    String name,
    String desc,
    Set<String> value,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    TreeSet<String> initial = new TreeSet<String>();
    if(value != null) 
      initial.addAll(value); 
    
    TreeMap<String,String> table = qclient.getLicenseKeyDescriptions(true); 

    return new ListUtilityParam(name, desc, initial, table.keySet(), null, table); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -467786339516427750L;
}
