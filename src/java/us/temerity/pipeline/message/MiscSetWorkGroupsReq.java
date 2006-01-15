// $Id: MiscSetWorkGroupsReq.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   W O R K   G R O U P S   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the work groups used to determine the scope of administrative privileges. 
 * 
 * @see MasterMgr
 */
public
class MiscSetWorkGroupsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param groups
   *   The work groups.
   */
  public
  MiscSetWorkGroupsReq
  (
   WorkGroups groups
  )
  {
    super();

    if(groups == null) 
      throw new IllegalArgumentException
	("The work groups cannot be (null)!");
    pGroups = groups;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the work groups.
   */ 
  public WorkGroups
  getGroups() 
  {
    return pGroups;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

   private static final long serialVersionUID = 6592068558215240067L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The work groups.
   */
  private WorkGroups  pGroups;  

}
  
