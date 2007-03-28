// $Id: BaseWorkingNodeEvent.java,v 1.2 2007/03/28 19:56:42 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   W O R K I N G   N O D E    E V E N T                                         */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class for classes used to record significant operations involving 
 * working versions of Pipeline nodes.
 */
public
class BaseWorkingNodeEvent
  extends BaseNodeEvent
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
  BaseWorkingNodeEvent()
  {}

  /** 
   * Internal constructor used to create new events. 
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the event 
   *   occurred. 
   * 
   * @param nodeOp
   *   The type of node operation recorded by the event. 
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   */
  protected
  BaseWorkingNodeEvent
  ( 
   long stamp, 
   NodeEventOp nodeOp, 
   NodeID nodeID 
  ) 
  {
    super(stamp, nodeOp, nodeID.getName(), nodeID.getAuthor());

    pView = nodeID.getView();
  }

  /** 
   * Internal constructor used to create new events. 
   * 
   * @param nodeOp
   *   The type of node operation recorded by the event. 
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   */
  protected
  BaseWorkingNodeEvent
  ( 
   NodeEventOp nodeOp, 
   NodeID nodeID 
  ) 
  {
    this(System.currentTimeMillis(), nodeOp, nodeID); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the name of the user's working area view associated with the event. 
   */
  public String
  getView()
  {
    return pView;
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

    encoder.encode("View", pView);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder); 

    String view = (String) decoder.decode("View");
    if(view == null) 
      throw new GlueException("The \"View\" was missing!");
    pView = view; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 377091971792581028L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of the user's working area view associated with the event.
   */
  protected String  pView;

}

