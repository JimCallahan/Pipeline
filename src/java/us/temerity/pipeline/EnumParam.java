// $Id: EnumParam.java,v 1.2 2004/11/19 11:49:54 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   P A R A M                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with an Enum value. <P> 
 */
public 
class EnumParam
  extends BaseParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  public
  EnumParam() 
  {
    super();

    pValues = new ArrayList<String>();
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
  EnumParam
  (
   String name,  
   String desc, 
   String value, 
   ArrayList<String> values
  ) 
  {
    super(name, desc, value);

    if(value == null)
      throw new IllegalArgumentException("The value cannot be (null)!");

    pValues = values;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the enumeration value based on the ordinal index.
   */ 
  public String
  getValueOfIndex
  (
   int idx
  ) 
  {
    return pValues.get(idx);
  }

  /**
   * Get the index of the current value 
   */ 
  public int 
  getIndex() 
  {
    return pValues.indexOf(pValue);
  }


  /**
   * The complete set of enumerated values.
   */ 
  public Collection<String>
  getValues() 
  {
    return Collections.unmodifiableCollection(pValues);
  }  

  /**
   * Sets the value of the parameter. 
   */
  public void 
  setValue
  (
   Comparable value  
  ) 
  {
    if(value == null)
      throw new IllegalArgumentException
	("The parameter (" + pName + ") cannot accept (null) values!");
      
    if(!(value instanceof String)) 
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (String) values!");

    if(!pValues.contains(value)) 
      throw new IllegalArgumentException
	("The value (" + value + ") was not a member of the enumeration for the " + 
	 "(" + pName + ") parameter!");

    pValue = value;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 436958043289043276L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The complete set of enumerated values.
   */
  private ArrayList<String>  pValues;

}



