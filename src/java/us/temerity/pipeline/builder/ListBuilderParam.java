package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BooleanBuilderParam;
import us.temerity.pipeline.builder.BuilderParam;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   B U I L D E R   P A R A M                                                    */
/*------------------------------------------------------------------------------------------*/

public class ListBuilderParam
  extends ListParam<BuilderParam>
  implements BuilderParam
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
  ListBuilderParam() 
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
  ListBuilderParam
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
  protected BuilderParam 
  createBooleanParam
  (
    String name,
    String desc,
    Boolean value
  )
  {
    return new BooleanBuilderParam(name, desc, value);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   H E L P E R S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  public static ListBuilderParam
  createSelectionKeyParam
  (
    String name,
    String desc,
    Set<String> value
  )
    throws PipelineException
  {
    ArrayList<SelectionKey> keys = BaseUtil.getSelectionKeys();
    
    TreeSet<String> values = new TreeSet<String>();
    TreeMap<String, String> tooltips = new TreeMap<String, String>();
    
    if (value == null)
      value = new TreeSet<String>();
    
    for (SelectionKey key : keys) {
       values.add(key.getName());
       tooltips.put(key.getName(), key.getDescription());
    }
    return new ListBuilderParam(name, desc, value, values, null, tooltips);
  }
  
  public static ListBuilderParam
  createLicenseKeyParam
  (
    String name,
    String desc,
    Set<String> value
  )
    throws PipelineException
  {
    ArrayList<LicenseKey> keys = BaseUtil.getLicenseKeys();
    
    TreeSet<String> values = new TreeSet<String>();
    TreeMap<String, String> tooltips = new TreeMap<String, String>();
    
    if (value == null)
      value = new TreeSet<String>();
    
    for (LicenseKey key : keys) {
       values.add(key.getName());
       tooltips.put(key.getName(), key.getDescription());
    }
    return new ListBuilderParam(name, desc, value, values, null, tooltips);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -467786339516427750L;
}
