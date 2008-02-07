// $Id: StringListParam.java,v 1.1 2008/02/07 10:19:18 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   S T R I N G   L I S T   P A R A M                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter that contains one or more String values.
 * <P>
 * This is designed to be used with {@link PlaceholderUtilityParam PlaceholderParams} for
 * circumstances where it is known that there will be a certain number of string parameters
 * but the number is unknown until after the Utility has started to run.
 * <p>
 * Each String parameter nested in this Complex Param can be accessed through the param name
 * Value####, where #### is the four-padded zero-based number of the String.  So the first
 * String has the param name Value0000.
  */
public abstract
class StringListParam<E>
  extends ComplexParam<E>
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
  StringListParam() 
  {
    super();
  }
  
  /**
   * Construct a parameter with the given name, description, default value, and layout.
   *
   * @param name
   *   The short name of the parameter.
   * 
   * @param desc
   *   A short description used in tooltips.
   * 
   * @param numStrings
   *   The number of Strings that should be part of this parameter.
   *    
   * @param defaults
   *   Default values for the String params or <code>null</code> if no defaults are
   *   desired.  If the length of defaults is longer than numStrings, the extra values
   *   will be ignored.  If it is shorter then defaults will not be applied to the extra
   *   parameter.
   */
  public
  StringListParam
  (
   String name, 
   String desc,
   int numStrings,
   ArrayList<String> defaults
  ) 
  {
    super(name, desc);
    
    if (numStrings < 1 || numStrings >= 10000)
      throw new IllegalArgumentException
        ("The String List parameter only supports numStrings values between 1 and 9999.  " +
         "The inputted value (" + numStrings + " ) is outside that range.");
    int defaultLength = 0;
    if (defaults != null)
      defaultLength = defaults.size();
    for (int i = 0 ; i < numStrings ; i++ ) {
      String value = null;
      if (defaultLength > i)
        value = defaults.get(i);
      String paramName = paramName(i);
      E param = createStringParam(paramName, desc, value);
      addParam(param);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R I C S   S U P P O R T                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  protected abstract E
  createStringParam
  (
    String name, 
    String desc, 
    String value 
  );

  

  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected boolean
  needsUpdating()
  {
    return false;
  }
 
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  public ArrayList<String>
  getStringValues()
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    for (String each : getParams().keySet()) {
      String value = (String) getValue(each);
      toReturn.add(value);
    }
    return toReturn;
  }
  
  private String 
  paramName
  (
    int i
  )
  {
    String pad = String.valueOf(i);
    while(pad.length() < 4)
      pad = "0" + pad;
    return "Value" + pad;
  }

}
