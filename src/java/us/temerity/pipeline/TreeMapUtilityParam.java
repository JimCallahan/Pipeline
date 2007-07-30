package us.temerity.pipeline;

import java.util.ArrayList;
import java.util.Map;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   T R E E   M A P   U T I L I T Y   P A R A M                                            */
/*------------------------------------------------------------------------------------------*/

public class TreeMapUtilityParam
  extends TreeMapParam<UtilityParam>
  implements UtilityParam
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */  
  public TreeMapUtilityParam()
  {
    super();
  }

  /**
   * @param name
   * @param desc
   * @param values
   */
  public TreeMapUtilityParam
  (
    String name,
    String desc,
    Map<String, ArrayList<String>> values
  )
  {
    super(name, desc, values);
  }

  /**
   * @param name
   * @param desc
   * @param firstName
   * @param firstDesc
   * @param secondName
   * @param secondDesc
   * @param values
   */
  public TreeMapUtilityParam
  (
    String name,
    String desc,
    String firstName,
    String firstDesc,
    String secondName,
    String secondDesc,
    Map<String, ArrayList<String>> values
  )
  {
    super(name, desc, firstName, firstDesc, secondName, secondDesc, values);
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

  private static final long serialVersionUID = 2721877847555922855L;

}
