// $Id: EnumActionParam.java,v 1.1 2004/06/14 22:43:27 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   A C T I O N   P A R A M                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * An Action parameter with an Enum value. <P> 
 */
public 
class EnumActionParam
  extends BaseActionParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the editor.  
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
   * @param titles
   *   The human friendly titles of each enumerated value.
   */ 
  public
  EnumActionParam
  (
   String name,  
   String desc, 
   Enum value, 
   ArrayList values, 
   ArrayList<String> titles
  ) 
  {
    super(name, desc, value);

    if(value == null)
      throw new IllegalArgumentException("The value cannot be (null)!");

    pValues = values;
    pTitles = titles;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the enumeration value based on the ordinal index.
   */ 
  public Comparable
  getValueOfIndex
  (
   int idx
  ) 
  {
    return pValues.get(idx);
  }


  /**
   * The complete set of enumerated values.
   */ 
  public ArrayList<Comparable>
  getValues() 
  {
    return new ArrayList<Comparable>(pValues);
  }  

  /**
   * The human friendly titles of each enumerated value.
   */ 
  public ArrayList<String>
  getTitles() 
  {
    return new ArrayList<String>(pTitles);
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
	("The action parameter (" + pName + ") cannot accept (null) values!");
      
    if(value.getClass() != pValue.getClass()) 
      throw new IllegalArgumentException
	("The action parameter (" + pName + ") only accepts " + 
	 "(" + pValue.getClass() + ") values!");

    pValue = value;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4250278439723258283L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The complete set of enumerated values.
   */
  private ArrayList<Comparable>  pValues;

  /**
   * The human friendly titles of each enumerated value.
   */
  private ArrayList<String>  pTitles;

}



