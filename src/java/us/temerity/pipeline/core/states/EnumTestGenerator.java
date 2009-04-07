// $Id: EnumTestGenerator.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   T E S T   G E N E R A T O R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A Java code generator for a test based on a subset of enumeration values.
 */ 
public 
class EnumTestGenerator
  extends BaseGenerator
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new code generator. 
   * 
   * @param key
   *   The value of the spreadsheet cell this represents.
   * 
   * @param policy
   *   The policy which determines legal values for this type of cell. 
   * 
   * @param variableName
   *   The literal name of the Java variable being tested.
   */ 
  public
  EnumTestGenerator
  (
   Comparable key, 
   EnumValue policy, 
   String variableName
  ) 
    throws ParseException
  {
    super(key, policy);

    if(!(key instanceof ComparableTreeSet)) 
      throw new ParseException
        ("The cell value must be an instance of ComparableTreeSet<String>!"); 
   
    if(variableName == null) 
      throw new ParseException("The test variable name cannot be (null)!"); 
    pVariableName = variableName;
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the value of the spreadsheet cell as a set of strings.
   */
  public ComparableTreeSet<String>
  asStrings() 
  {
    return (ComparableTreeSet<String>) getCellKey(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O D E   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the code which begins the current conditional scope. 
   * 
   * @param first
   *   Whether this is the first child generator processed by its parent generator.
   * 
   * @param level 
   *   The indentation level for this block of generated Java source code.
   */ 
  public String
  openScope
  (
   boolean first, 
   int level 
  ) 
  {
    StringBuilder buf = new StringBuilder();

    if(first) 
      buf.append(indent(level) + "switch(" + conditional() + ") {\n");
    
    for(String value : asStrings()) 
      buf.append(indent(level) + "case " + value + ":\n");

    return buf.toString();
  }
    
  /**
   * Generate the code which ends the current conditional scope. 
   * 
   * @param last
   *   Whether this is the last child generator processed by its parent generator.
   * 
   * @param level 
   *   The indentation level for this block of generated Java source code.
   */ 
  public String
  closeScope
  (
   boolean last, 
   int level 
  ) 
  {
    if(last) 
      return(indent(level) + "}\n");
    return "\n";
  }
    
  /**
   * Generate code for the conditional expression being evaluated in the current scope.
   */ 
  public String
  conditional()
  {
    return pVariableName; 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    StringBuilder buf = new StringBuilder(); 
    boolean first = true;
    for(String value : asStrings()) {
      buf.append(first ? "[" : ", "); 
      buf.append(value);  
      first = false;
    }
    buf.append("]"); 

    return buf.toString(); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the variable in the generated Java code contiaining the enum value
   * being tested.
   */ 
  private String pVariableName; 

}



