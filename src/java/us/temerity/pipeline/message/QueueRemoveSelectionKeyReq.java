// $Id: QueueRemoveSelectionKeyReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   S E L E C T I O N   K E Y   R E Q                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove a selection key to the currently defined selection keys. <P> 
 */
public
class QueueRemoveSelectionKeyReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param kname
   *   The name of the selection key to remove.
   */
  public
  QueueRemoveSelectionKeyReq
  (
   String kname
  )
  { 
    super();

    if(kname == null) 
      throw new IllegalArgumentException
	("The selection key name cannot be (null)!");
    pKeyName = kname;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the selection key to remove. 
   */
  public String
  getKeyName() 
  {
    return pKeyName;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1770230365366611389L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the selection key to remove.
   */ 
  private String pKeyName;

}
  
