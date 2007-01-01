// $Id: CheckedOutNodeEvent.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K E D - O U T   N O D E    E V E N T                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * An event for the check-out of node from the repository into a user's working area.
 */
public
class CheckedOutNodeEvent
  extends BaseRepoNodeEvent
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  CheckedOutNodeEvent()
  {}

  /** 
   * Create the event.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid
   *   The revision number of the version checked-out.
   * 
   * @param isFrozen  
   *   Whether the node was checked-out read-only.
   * 
   * @param isLocked 
   *   Whetehr the node was checked-out locked.
   */
  public
  CheckedOutNodeEvent
  ( 
   NodeID nodeID, 
   VersionID vid, 
   boolean isFrozen, 
   boolean isLocked
  ) 
  {
    super(NodeEventOp.CheckedOut, nodeID, vid);

    if(!isFrozen && isLocked) 
      throw new IllegalArgumentException
	("The node cannot be checked-out locked unless it is also frozen!"); 
    pIsFrozen = isFrozen;
    pIsLocked = isLocked;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get whether the node was checked-out read-only.
   */
  public boolean 
  isFrozen() 
  {
    return pIsFrozen; 
  }

  /** 
   * Get whether the node was checked-out locked.
   */
  public boolean 
  isLocked() 
  {
    return pIsLocked; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder); 

    encoder.encode("IsFrozen", pIsFrozen); 
    encoder.encode("IsLocked", pIsLocked); 
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder); 

    Boolean isFrozen = (Boolean) decoder.decode("IsFrozen");
    if(isFrozen == null) 
      throw new GlueException("The \"IsFrozen\" was missing!");
    pIsFrozen = isFrozen; 

    Boolean isLocked = (Boolean) decoder.decode("IsLocked");
    if(isLocked == null) 
      throw new GlueException("The \"IsLocked\" was missing!");
    pIsLocked = isLocked; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5352027428177007497L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Whether the node was checked-out read-only.
   */
  private boolean  pIsFrozen; 

  /** 
   * Whether the node was checked-out locked.
   */
  private boolean  pIsLocked; 

}

