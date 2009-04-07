// $Id: CellPolicy.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C E L L   P O L I C Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The policy which determines legal values for a spreadsheet cell. 
 */ 
public 
interface CellPolicy
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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
    throws ParseException;

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
    throws GenerateException;
}



