// $Id: QueueRemoveByNameReq.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message;

import java.util.TreeSet;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   B Y   N A M E   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the given existing queue controls. <P> 
 */
public 
class QueueRemoveByNameReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param names
   *   The names of the queue controls.
   *   
   * @param controlName
   *   The name of queue control, used for error messages.
   */
  public
  QueueRemoveByNameReq
  (
    TreeSet<String> names,
    String controlName
  )
  { 
    super();

    if(names == null) 
      throw new IllegalArgumentException
	("The " + controlName + " names cannot be (null)!");
    pNames = names;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of the queue controls. 
   */
  public TreeSet<String>
  getNames() 
  {
    return pNames; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6588712620718799232L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the hardware groups. 
   */ 
  private TreeSet<String>  pNames; 
}