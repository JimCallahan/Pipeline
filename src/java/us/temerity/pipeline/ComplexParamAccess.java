package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M P L E X   P A R A M   A C C E S S                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Interface which defines the methods need to interact with a Complex Parameter (a Parameter
 * that contains other Parameters).
 */
public 
interface ComplexParamAccess<E>
{
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the parameter identified by the key.
   */
  public E
  getParam
  (
    String key
  );
  
  /**
   * Gets the nested parameter identified by the list of keys.
   */
  public E
  getParam
  (
    List<String> keys
  );
  
  /**
   * Gets the value of the parameter identified by the key.
   */
  @SuppressWarnings("unchecked")
  public Comparable
  getValue
  (
    String key
  );
  
  /**
   * Gets the value of the nested parameter identified by the list of keys.
   */
  @SuppressWarnings("unchecked")
  public Comparable
  getValue
  (
    List<String> keys
  );
  
  /**
   * Is there a parameter identified with this key? 
   */
  public boolean 
  hasParam
  (
    String key
  );

  /**
   * Is there a nested parameter identified by this list of keys? 
   */
  public boolean 
  hasParam
  (
    List<String> keys
  );
  
  /**
   * Is there a Simple Parameter identified with this key? 
   */
  public boolean 
  hasSimpleParam
  (
    String key
  );

  /**
   * Is there a nested Simple Parameter identified by this list of keys? 
   */
  public boolean 
  hasSimpleParam
  (
    List<String> keys
  );
  
  /**
   * Is there a Simple Parameter which implements {@link SimpleParamFromString} 
   * identified with this key? 
   */
  public boolean 
  canSetSimpleParamFromString
  (
    String key
  );

  /**
   * Is there a nested Simple Parameter which implements {@link SimpleParamFromString} 
   * identified by this list of keys? 
   */
  public boolean 
  canSetSimpleParamFromString
  (
    List<String> keys
  );

  /**
   * Sets the value of the parameter identified with the key.
   */
  @SuppressWarnings("unchecked")
  public void 
  setValue
  (
    String key, 
    Comparable value
  );

  /**
   * Sets the value of the nested parameter identified by the list of keys.
   */
  @SuppressWarnings("unchecked")
  public void 
  setValue
  (
    List<String> keys, 
    Comparable value
  );
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
//  public boolean
//  requiresUpdating();
//  
//  public void
//  valuesUpdated();
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L A Y O U T                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the layout for the parameters.
   */
  public ArrayList<String> 
  getLayout();
}
