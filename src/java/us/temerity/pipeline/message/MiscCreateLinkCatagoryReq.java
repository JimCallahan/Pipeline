// $Id: MiscCreateLinkCatagoryReq.java,v 1.1 2004/06/28 23:39:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   C R E A T E   L I N K   C A T A G O R Y   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to create a new link catagory.
 * 
 * @see MasterMgr
 */
public
class MiscCreateLinkCatagoryReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name 
   *   The name of the link catagory.
   * 
   * @param policy 
   *   The node state propogation policy.
   * 
   * @param desc
   *   A short description of the link catagory.
   */
  public
  MiscCreateLinkCatagoryReq
  (
   String name,  
   LinkPolicy policy, 
   String desc
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The link catagory name cannot be (null)!");
    pName = name;

    if(policy == null) 
      throw new IllegalArgumentException("The policy cannot be (null)!");
    pPolicy = policy;

    if(desc == null) 
      throw new IllegalArgumentException
	("The link catagory description cannot be (null)!");
    pDescription = desc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the link catagory.
   */ 
  public String
  getName() 
  {
    assert(pName != null);
    return pName;
  }
 
  /**
   * Get the link's {@link OverallNodeState OverallNodeState} and
   * {@link OverallQueueState OverallQueueState} propagation policy.
   */ 
  public LinkPolicy
  getPolicy() 
  {
    return pPolicy;
  }

  /**
   * Get the link catagory description text.
   */ 
  public String 
  getDescription() 
  {
    return pDescription;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9048291255990646917L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the link catagory.
   */
  private String  pName;  

  /** 
   * The link's {@link OverallNodeState OverallNodeState} and 
   * {@link OverallQueueState OverallQueueState} propagation policy.
   */
  private LinkPolicy  pPolicy; 

  /**
   * The link catagory description text.
   */ 
  private String  pDescription;

}
  
