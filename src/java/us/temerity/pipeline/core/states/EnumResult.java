// $Id: EnumResult.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   R E S U L T                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A cell policy which requires single String value which is a member of an enumeration.
 */ 
public 
class EnumResult
  extends EnumValue
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new Java source code generator.
   *
   * @param enumClassName
   *   The literal name of the Java enum class.
   * 
   * @param enumValues
   *   The string representations of all legal enum values.
   */ 
  public
  EnumResult
  (
   String enumClassName, 
   TreeSet<String> enumValues
  ) 
  {
    super(enumClassName, enumValues); 
  }



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

    if(!getEnumValues().contains(text)) 
      throw new ParseException
        ("The cell text (" + text + ") is not a valid member of the " + 
         "(" + getEnumClassName() + ") enumeration!"); 

    return text;
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
    LinkedList<Comparable> ordered = new LinkedList<Comparable>(); 

    for(Comparable key : keys) {
      if(!(key instanceof String)) 
        throw new GenerateException
          ("Encountered a key (" + key + ") which was not a string enumeration value!");
      ordered.add((String) key); 
    }

    if(ordered.size() != 1) {
      StringBuilder buf = new StringBuilder();
      buf.append
        ("There can only be one valid result value, however the following " + 
         "(" + getEnumClassName() + ") enumeration values were all found as resuts for " + 
         "the current conditional:\n\n");
      for(Comparable value : ordered) 
        buf.append("  " + value + "\n"); 
      throw new GenerateException(buf.toString());
    }
    
    return ordered;
  }
  
}
  

