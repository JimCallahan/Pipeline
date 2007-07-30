package us.temerity.pipeline;

import java.util.ArrayList;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E   M A P   U T I L I T Y   P A R A M                                        */
/*------------------------------------------------------------------------------------------*/

public class DoubleMapUtilityParam
  extends DoubleMapParam<UtilityParam>
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
  DoubleMapUtilityParam() 
  {
    super();
  }


  /**
   * Construct a parameter with the given name, description, and values and the
   * given parameter names.
   * 
   * @param name
   * @param desc
   * @param firstName
   * @param firstDesc
   * @param secondName
   * @param secondDesc
   * @param thirdName
   * @param thirdDesc
   * @param values
   */
  public DoubleMapUtilityParam
  (
    String name,
    String desc,
    String firstName,
    String firstDesc,
    String secondName,
    String secondDesc,
    String thirdName,
    String thirdDesc,
    DoubleMap<String, String, ArrayList<String>> values
  )
  {
    super(name, 
          desc, 
          firstName, 
          firstDesc, 
          secondName, 
          secondDesc, 
          thirdName, 
          thirdDesc, 
          values);
  }
  
  /**
   * Construct a parameter with the given name, description, and values.
   *
   * @param name
   *    The short name of the parameter.
   * 
   * @param desc
   *    A short description used in tooltips.
   * 
   * @param values
   *    The Double Maps containing the three tiers of parameter values.
   *    
   */
  public 
  DoubleMapUtilityParam
  (
    String name,
    String desc,
    DoubleMap<String, String, ArrayList<String>> values
  )
  {
    super(name, desc, values);
  }


  @Override
  protected UtilityParam 
  createEnumParam
  (
    String name,
    String desc,
    String value,
    ArrayList<String> values
  )
  {
    return new OptionalEnumUtilityParam(name, desc, value, values);
  }
  
  private static final long serialVersionUID = 2194083652618826027L;


}
