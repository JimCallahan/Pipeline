// $Id: LogMessage.java,v 1.6 2004/07/14 20:59:20 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G   M E S S A G E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**                                                                                   
 * A text message with automaticly records who wrote the text and when.                  
 */
public 
class LogMessage 
  implements Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  LogMessage() 
  {}

  /**
   * Construct a new message owned by the given author (recording the time). 
   * 
   * @param author
   *   The name of the user creating the message.
   * 
   * @param msg 
   *   The message text.
   */ 
  public 
  LogMessage
  (
   String author, 
   String msg  
  ) 
  {
    pTimeStamp = Dates.now();

    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    pMessage = msg;
  }

  /**
   * Construct a new message (recording the current author and time). 
   * 
   * @param msg 
   *   The message text.
   */ 
  public 
  LogMessage
  (
   String msg  
  ) 
  {
    pTimeStamp = Dates.now();
    pAuthor    = PackageInfo.sUser;
    pMessage   = msg;
  }

  /**
   * Copy constructor. 
   */ 
  public 
  LogMessage
  (
   LogMessage msg  
  ) 
  {
    pTimeStamp = msg.getTimeStamp();
    pAuthor    = msg.getAuthor();
    pMessage   = msg.getMessage();
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get when the message was written. 
   */ 
  public Date
  getTimeStamp() 
  {
    assert(pTimeStamp != null);
    return (Date) pTimeStamp.clone();
  }

  /**
   * Get the name of the user who wrote the message. 
   */ 
  public String
  getAuthor() 
  {
    assert(pAuthor != null);
    return pAuthor;
  }

  /**
   * Get the message text. 
   */ 
  public String
  getMessage() 
  {
    assert(pMessage != null);
    return pMessage;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof LogMessage)) {
      LogMessage log = (LogMessage) obj;

      if(pTimeStamp.equals(log.pTimeStamp) && 
	 pAuthor.equals(log.pAuthor) &&
	 pMessage.equals(log.pMessage));
	return true;
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException ex) {
      assert(false);
      return null;
    }
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
    encoder.encode("TimeStamp", pTimeStamp.getTime());
    encoder.encode("Author",    pAuthor);
    encoder.encode("Message",   pMessage);
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
    pTimeStamp = new Date(stamp);

    String author = (String) decoder.decode("Author");
    if(author == null) 
      throw new GlueException("The \"Author\" was missing!");
    pAuthor = author;

    String message = (String) decoder.decode("Message");
    if(message == null) 
      throw new GlueException("The \"Message\" was missing!");
    pMessage = message;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9213949194764455768L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * When the message was written. 
   */ 
  private Date  pTimeStamp; 
  
  /**
   * The name of the user who wrote the message. 
   */ 
  private String  pAuthor;  
  
  /**
   * The message text. 
   */ 
  private String  pMessage;    
  
}



