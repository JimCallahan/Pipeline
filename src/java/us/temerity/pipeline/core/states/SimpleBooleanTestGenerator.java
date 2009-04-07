// $Id: SimpleBooleanTestGenerator.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S I M P L E   B O O L E A N   T E S T   G E N E R A T O R                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A Java code generator for comparing base and latest version node properties. 
 */ 
public  
class SimpleBooleanTestGenerator
  extends BooleanTestGenerator
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
   * @param expression
   *   The literal Java code to use as the conditional expression.
   */ 
  public
  SimpleBooleanTestGenerator
  (
   Comparable key, 
   BooleanValue policy,
   String expression
  ) 
    throws ParseException 
  {
    super(key, policy);
    
    if(expression == null) 
      throw new IllegalArgumentException("The expression cannot be (null)!"); 
    pExpression = expression;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O D E   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate code for the conditional expression being evaluated in the current scope.
   */
  public String
  conditional()
  {
    return pExpression;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The literal Java code to use as the conditional expression.
   */ 
  private String pExpression; 

    
}



