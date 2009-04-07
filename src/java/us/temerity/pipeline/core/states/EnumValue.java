// $Id: EnumValue.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   V A L U E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A cell policy which allows one or more String values which are members of an enumeration.
 */ 
public 
class EnumValue
  implements CellPolicy
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
  EnumValue
  (
   String enumClassName, 
   TreeSet<String> enumValues
  ) 
  {
    if(enumClassName == null) 
      throw new IllegalArgumentException("The enumeration class name cannot be (null)!"); 
    pEnumClassName = enumClassName;

    if(enumValues == null) 
      throw new IllegalArgumentException("The enumeration values cannot be (null)!"); 
    if(enumValues.isEmpty()) 
      throw new IllegalArgumentException("At least one enumeration value must be supplied!");
    pEnumValues = enumValues;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the literal name of the Java enum class.
   */ 
  public final String
  getEnumClassName() 
  {
    return pEnumClassName;
  }

  /**
   * Get the string representations of all legal enum values.
   */ 
  public final SortedSet<String> 
  getEnumValues() 
  {
    return Collections.unmodifiableSortedSet(pEnumValues);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert the String value of a cell from the spreadsheet into a unique Comparable key
   * which can be used to index the state the cell represents.
   */
  public Comparable
  parseKey
  (
   String text
  ) 
    throws ParseException
  {
    if(text == null) 
      throw new ParseException
        ("The cell text cannot be (null)!"); 

    ComparableTreeSet<String> keys = new ComparableTreeSet<String>();

    String parts[] = text.split(","); 
    int wk; 
    for(wk=0; wk<parts.length; wk++) {
      String value = parts[wk].trim();
      if(value.length() > 0) {
        if(!pEnumValues.contains(value)) 
          throw new ParseException
            ("The cell text (" + text + ") contains a value (" + value + ") which is " + 
             "not a valid member of the (" + pEnumClassName + ") enumeration!"); 
        keys.add(value); 
      }
    }

    if(keys.isEmpty()) 
      throw new ParseException
        ("The cell text (" + text + ") did not contain any valid members of the " + 
         "(" + pEnumClassName + ") enumeration!"); 

    return keys;
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
  public LinkedList<Comparable>
  validateAndOrder
  (
   Collection<Comparable> keys
  ) 
    throws GenerateException
  {
    LinkedList<Comparable> ordered = new LinkedList<Comparable>(); 

    TreeSet<String> all = new TreeSet<String>(pEnumValues);

    for(Comparable key : keys) {
      if(key == null) 
        throw new GenerateException
          ("Encountered a key with a (null) value!");

      if(key instanceof ComparableTreeSet) {
        ComparableTreeSet<String> values = (ComparableTreeSet<String>) key;
        for(String value : values) {
          if(!pEnumValues.contains(value)) 
            throw new GenerateException
              ("Encountered an key which includes a value (" + value + ") which is not " + 
               "a member of the (" + pEnumClassName + ") enumeration!"); 
          
          if(!all.contains(value)) 
            throw new GenerateException
              ("The enumeration value (" + value + ") is being used by more than one " + 
               "key for the current conditional!"); 

          all.remove(value); 
        }

        ordered.add(values);
      }
      else {
        throw new GenerateException
          ("Encountered a key (" + key + ") which was not a set of string enumeration " + 
           "values!"); 
      }
    }

    if(!all.isEmpty()) {
      StringBuilder buf = new StringBuilder();
      buf.append
        ("The following (" + pEnumClassName + ") enumeration values are not being handled" + 
         "by any of the keys for the current conditional:\n\n");
      for(String value : all) 
        buf.append("  " + value + "\n"); 
      throw new GenerateException(buf.toString());
    }

    return ordered; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The literal name of the Java enum class.
   */ 
  private final String pEnumClassName; 

  /**
   * The string representation of all legal enum values.
   */ 
  private final TreeSet<String> pEnumValues; 

}
  

