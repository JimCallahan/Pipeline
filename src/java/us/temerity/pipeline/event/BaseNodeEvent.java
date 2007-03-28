// $Id: BaseNodeEvent.java,v 1.2 2007/03/28 19:56:42 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   N O D E   E V E N T                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class for classes used to record significant operations involving 
 * Pipeline nodes.
 */
public
class BaseNodeEvent
  implements Glueable, Serializable
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
  BaseNodeEvent()
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
   * @param name 
   *   The fully resolved node name.
   * 
   * @param author 
   *   The name of the user which generated the event.
   */
  protected
  BaseNodeEvent
  ( 
   long stamp, 
   NodeEventOp nodeOp, 
   String name, 
   String author
  ) 
  {
    pTimeStamp = stamp; 

    if(nodeOp == null) 
      throw new IllegalArgumentException("The node operation type cannot be (null)!");
    pNodeOp = nodeOp; 

    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;
  }

  /** 
   * Internal constructor used to create new events. 
   * 
   * @param nodeOp
   *   The type of node operation recorded by the event. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param author 
   *   The name of the user which generated the event.
   */
  protected
  BaseNodeEvent
  ( 
   NodeEventOp nodeOp, 
   String name, 
   String author
  ) 
  {
    this(System.currentTimeMillis(), nodeOp, name, author); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the event 
   * occured.
   */ 
  public long
  getTimeStamp() 
  {
    return pTimeStamp; 
  }

  /** 
   * Get the type of node operation recorded by the event. 
   */ 
  public NodeEventOp
  getNodeOp() 
  {
    return pNodeOp; 
  }

  /** 
   * Get the fully resolved name of the node. 
   */ 
  public String
  getNodeName() 
  {
    return pName;
  }

  /** 
   * Get the name of the user which generated the event.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
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
    encoder.encode("TimeStamp", pTimeStamp);
    encoder.encode("NodeOp", pNodeOp); 
    encoder.encode("Name", pName);
    encoder.encode("Author", pAuthor);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Long stamp = (Long) decoder.decode("TimeStamp");
    if(stamp == null)
      throw new GlueException("The \"TimeStamp\" was missing!");
    if(stamp <= 0L) 
      throw new GlueException("The \"TimeStamp\" was illegal!");
    pTimeStamp = stamp;
    
    NodeEventOp nodeOp = (NodeEventOp) decoder.decode("NodeOp");
    if(nodeOp == null) 
      throw new GlueException("The \"NodeOp\" was missing!");
    pNodeOp = nodeOp;

    String name = (String) decoder.decode("Name");
    if(name == null) 
      throw new GlueException("The \"Name\" was missing!");
    pName = name;

    String author = (String) decoder.decode("Author");
    if(author == null) 
      throw new GlueException("The \"Author\" was missing!");
    pAuthor = author;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8081860086372684573L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the event 
   * occured.
   */
  protected long  pTimeStamp; 

  /** 
   * The type of node operation recorded by the event. 
   */
  protected NodeEventOp  pNodeOp;

  /** 
   * The fully resolved node name.
   */
  protected String  pName;  

  /** 
   * The name of the user which generated the event.
   */
  protected String  pAuthor;

}

