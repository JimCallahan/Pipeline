// $Id: RootTestGenerator.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R O O T   T E S T   G E N E R A T O R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A stub generator which acts as the root of the generator tree.
 */ 
public  
class RootTestGenerator
  extends BaseGenerator
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new code generator. 
   */ 
  public
  RootTestGenerator()
    throws ParseException 
  {
    super("ROOT", new RootPolicy()); 
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
    return (indent(level) + "{\n");
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

  /**
   * Generate code for the conditional expression being evaluated in the current scope.
   */ 
  public String
  conditional()
  {
    throw new IllegalStateException("This should never be called!"); 
  }


}



