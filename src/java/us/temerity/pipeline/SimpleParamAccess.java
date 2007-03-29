package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   S I M P L E   P A R A M   A C C E S S                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Interface which defines the methods need to set and get a Parameter's value using a 
 * single {@link Comparable} value.
 */
public 
interface SimpleParamAccess
{
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the value of the parameter. 
   */ 
  @SuppressWarnings("unchecked")
  public Comparable
  getValue() ;
  
  /**
   * Sets the value of the parameter. 
   */
  @SuppressWarnings("unchecked")
  public void 
  setValue
  (
   Comparable value  
  );
  
  /**
   * Sets the value of the parameter from a String.
   * <p>
   * This method is used for setting parameter values from command line arguments.
   */
  public void
  fromString
  (
    String value
  );
}
