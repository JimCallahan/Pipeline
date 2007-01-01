// $Id: BaseRepoNodeEvent.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   R E P O   N O D E    E V E N T                                               */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class for classes used to record significant operations involving 
 * both working and checked-in versions of Pipeline nodes.
 */
public
class BaseRepoNodeEvent
  extends BaseWorkingNodeEvent
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
  BaseRepoNodeEvent()
  {}

  /** 
   * Internal constructor used to create new events. 
   * 
   * @param nodeOp
   *   The type of node operation recorded by the event. 
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid
   *   The revision number of the checked-in version associated with the event.
   */
  protected
  BaseRepoNodeEvent
  ( 
   NodeEventOp nodeOp, 
   NodeID nodeID, 
   VersionID vid
  ) 
  {
    super(nodeOp, nodeID); 

    if(vid == null) 
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the revision number of the checked-in version associated with the event.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID;
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

    encoder.encode("VersionID", pVersionID);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder); 

    VersionID vid = (VersionID) decoder.decode("VersionID");
    if(vid == null) 
      throw new GlueException("The \"VersionID\" was missing!");
    pVersionID = vid; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4317031901356811327L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The revision number of the checked-in version associated with the event.
   */
  protected VersionID  pVersionID; 

}

