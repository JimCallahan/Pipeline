// $Id: BooleanTestGenerator.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   T E S T   G E N E R A T O R                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * An abtract Java code generator for a simple boolean test. <P> 
 * 
 * This must be extended to define the conditional expression being tested.
 */ 
public abstract 
class BooleanTestGenerator
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
  BooleanTestGenerator
  (
   Comparable key, 
   BooleanValue policy
  ) 
    throws ParseException
  {
    super(key, policy);

    if(!(key instanceof Boolean)) 
      throw new ParseException
        ("The cell value (" + key + ") must be an instance of Boolean!"); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the value of the spreadsheet cell as boolean.
   */
  public boolean
  asBoolean() 
  {
    return (Boolean) getCellKey(); 
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
    if(asBoolean()) 
      return (indent(level) + "if(" + conditional() + ") {\n");
    return (indent(level) + "else {\n");
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
    return (indent(level) + "}\n");
  }

}



