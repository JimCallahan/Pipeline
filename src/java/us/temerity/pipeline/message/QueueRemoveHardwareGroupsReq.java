// $Id: QueueRemoveHardwareGroupsReq.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.message;

import java.util.TreeSet;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   H A R D W A R E   G R O U P   R E Q                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the given existing hardware group. <P> 
 */
public 
class QueueRemoveHardwareGroupsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param names
   *   The names of the hardware groups. 
   */
  public
  QueueRemoveHardwareGroupsReq
  (
   TreeSet<String> names
  )
  { 
    super();

    if(names == null) 
      throw new IllegalArgumentException
	("The hardware group names cannot be (null)!");
    pNames = names;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of the hardware groups. 
   */
  public TreeSet<String>
  getNames() 
  {
    return pNames; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4091635337961922424L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the hardware groups. 
   */ 
  private TreeSet<String>  pNames; 

}
  
