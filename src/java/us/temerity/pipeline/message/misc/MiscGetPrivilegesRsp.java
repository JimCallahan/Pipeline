// $Id: MiscGetPrivilegesRsp.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P R I V I L E G E S   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the administrative privileges for all users.
 */
public
class MiscGetPrivilegesRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param table
   *   The table of privileges for each user indexed by user name.
   */ 
  public
  MiscGetPrivilegesRsp
  (
   TaskTimer timer, 
   TreeMap<String,Privileges> table
  )
  { 
    super(timer);

    if(table == null) 
      throw new IllegalArgumentException("The privileges table cannot be (null)!");
    pTable = table;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the table of privileges for each user indexed by user name.
   */
  public TreeMap<String,Privileges>
  getTable() 
  {
    return pTable;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 646019933988020189L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of privileges for each user indexed by user name.
   */ 
  private TreeMap<String,Privileges>  pTable;

}
  
