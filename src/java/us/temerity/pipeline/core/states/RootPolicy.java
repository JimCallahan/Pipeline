// $Id: RootPolicy.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R O O T   V A L U E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A stub cell policy used for the root generator.
 */ 
public 
class RootPolicy
  implements CellPolicy
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new cell policy.
   */ 
  public
  RootPolicy()
  {}

  

  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert the String value of a cell from the spreadsheet into a unique Comparable key
   * which can be used to index the state the cell represents.
   */
  public final Comparable
  parseKey
  (
   String text
  ) 
    throws ParseException
  {
    throw new ParseException("This should never be called!"); 
  }

  /**
   * Validate given set of keys for completeness and return them in the order they 
   * should be processed during code generation.
   * 
   * To be compelte, all possible states should be represented once and only once. 
   * 
   * @param keys
   *   The set of all keys for a given conditional clause.
   * 
   * @throws GenerateException
   *   If the keys are incomplete or contain duplicated states. 
   */ 
  public final LinkedList<Comparable>
  validateAndOrder
  (
   Collection<Comparable> keys
  ) 
    throws GenerateException
  {
    throw new GenerateException("This should never be called!"); 
  }

}



