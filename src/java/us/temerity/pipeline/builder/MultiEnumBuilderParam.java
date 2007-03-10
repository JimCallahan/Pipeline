// $Id: MultiEnumBuilderParam.java,v 1.1 2007/03/10 22:44:33 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M U L T I   E N U M   B U I L D E R   P A R A M                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An Builder parameter with an Enum value allowing multiple selections. <P> 
 */
public 
class MultiEnumBuilderParam
  extends MultiEnumParam
  implements PrimitiveBuilderParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */    
  public
  MultiEnumBuilderParam() 
  {
    super();
  }

  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the parameter.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   * 
   * @param values
   *   The complete set of enumerated values.
   */ 
  public
  MultiEnumBuilderParam
  (
   String name,  
   String desc, 
   ComparableTreeSet<String> value,
   TreeSet<String> values
  ) 
  {
    super(name, desc, value, values);
  }

  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the parameter.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   * 
   * @param values
   *   The complete set of enumerated values.
   * 
   * @param layout
   * 	The layout for the values
   * 
   * @param tooltips
   * 	The tooltips for each values
   */ 
  public
  MultiEnumBuilderParam
  (
   String name,  
   String desc, 
   ComparableTreeSet<String> value,
   TreeSet<String> values,
   ArrayList<String> layout,
   TreeMap<String, String> tooltips
  ) 
  {
    super(name, desc, value, values, layout, tooltips);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the value of this parameter from a string.
   */
  public void 
  valueFromString
  (
    String value
  )
  {
    if (value == null)
      return;
    ComparableTreeSet<String> treeValue = new ComparableTreeSet<String>();
    String buffer[] = value.split(",");
    for(String each : buffer)
      treeValue.add(each);
    setValue(treeValue);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  private static final long serialVersionUID = 246244447101116042L;

}



