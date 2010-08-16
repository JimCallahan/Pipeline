package us.temerity.pipeline.message.simple;

import java.util.*;

import us.temerity.pipeline.message.*;

/*------------------------------------------------------------------------------------------*/
/*   S I M P L E   S E T   R S P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A server request that takes a set of strings as its argument.
 */
public class SimpleSetReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new request.
   * <p>
   * 
   * @param set
   *   The set of strings that makes up the request.  Cannot be <code>null</code>.  
   */
  public 
  SimpleSetReq
  (
    TreeSet<String> set
  )
  {
    if (set == null)
      throw new IllegalArgumentException("The set cannot be (null)!");
    
    pSet = set;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the set of strings.
   */
  public TreeSet<String>
  getSet()
  {
    return pSet;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4868477259055698323L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The set of strings that makes up the request.
   */
  private TreeSet<String> pSet;
}
