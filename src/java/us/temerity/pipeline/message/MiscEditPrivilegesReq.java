// $Id: MiscEditPrivilegesReq.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   E D I T   P R I V I L E G E S   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the administrative privileges for the given users.
 */
public 
class MiscEditPrivilegesReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param table
   *   The privileges for each user indexed by user name.
   */
  public
  MiscEditPrivilegesReq
  (
   TreeMap<String,Privileges> table
  )
  { 
    super();

    if(table == null) 
      throw new IllegalArgumentException
	("The selection table cannot be (null)!");
    pTable = table;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the privileges for each user indexed by user name.
   */
  public TreeMap<String,Privileges> 
  getTable()
  {
    return pTable; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1501005110551992724L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The privileges for each user indexed by user name.
   */ 
  private TreeMap<String,Privileges>  pTable; 

}
  
