// $Id: EnumResultGenerator.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   R E S U L T   G E N E R A T O R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A Java code generator returning an enum value as the result of a test.
 */ 
public 
class EnumResultGenerator
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
   */ 
  public
  EnumResultGenerator
  (
   Comparable key, 
   EnumResult policy
  ) 
    throws  ParseException
  {
    super(key, policy); 
    
    if(!(key instanceof String)) 
      throw new ParseException
        ("The cell value must be an instance of String!"); 
  }

  
 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the value of the spreadsheet cell as a string.
   */
  public String
  asString() 
  {
    return (String) getCellKey();
  }

  /**
   * Get the name of Java enumeration class. 
   */
  public String
  getEnumClassName() 
  {
    return ((EnumResult) getCellPolicy()).getEnumClassName();
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
    return (indent(level) + "return " + getEnumClassName() + "." + asString() + ";\n"); 
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
    return "";
  }
    
  /**
   * Generate code for the conditional expression being evaluated in the current scope.
   */ 
  public String
  conditional()
  {
    throw new IllegalStateException("This should never happen!"); 
  }
    
}



