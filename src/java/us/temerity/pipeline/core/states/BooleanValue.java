// $Id: BooleanValue.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   V A L U E                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A cell policy which requires a single boolean value. 
 */ 
public 
class BooleanValue
  implements CellPolicy
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new cell policy.
   */ 
  public
  BooleanValue()
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
    if(text == null) 
      throw new ParseException
        ("The cell text cannot be (null)!"); 

    if((text.compareToIgnoreCase("yes") == 0) ||
       (text.compareToIgnoreCase("true") == 0) ||
       (text.compareToIgnoreCase("same") == 0)) {
      return true; 
    }
    else if((text.compareToIgnoreCase("no") == 0) ||
            (text.compareToIgnoreCase("false") == 0) ||
            (text.compareToIgnoreCase("different") == 0)) {
      return false;
    }
    else {
      throw new ParseException
        ("The cell text (" + text + ") did not represent a legal boolean value!"); 
    }
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
    boolean foundTrue  = false;
    boolean foundFalse = false;
    for(Comparable key : keys) {
      if(key == null) 
        throw new GenerateException
          ("Encountered a key with a (null) value!");
      
      if(key instanceof Boolean) { 
        Boolean tf = (Boolean) key;
        if(tf) {
          if(foundTrue) 
            throw new GenerateException
              ("Encountered more than one (true) case!");
          foundTrue = true;
        }
        else {
          if(foundFalse) 
            throw new GenerateException
              ("Encountered more than one (false) case!");
          foundFalse = true;
        }
      }
      else {
        throw new GenerateException
          ("Encountered a key (" + key + ") which was not a boolean!"); 
      }
    }

    if(!foundTrue) 
      throw new GenerateException
        ("There was no (true) case defined for the current conditional!"); 

    if(!foundFalse) 
      throw new GenerateException
        ("There was no (false) case defined for the current conditional!"); 


    LinkedList<Comparable> ordered = new LinkedList<Comparable>();
    ordered.add(true);
    ordered.add(false);

    return ordered; 
  }

}



