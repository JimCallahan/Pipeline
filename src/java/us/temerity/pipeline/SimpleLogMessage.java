// $Id: SimpleLogMessage.java,v 1.3 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S I M L P L E   L O G    M E S S A G E                                                 */
/*------------------------------------------------------------------------------------------*/

/**                                                                                   
 * A simple text message which automaticly records who wrote the text and when.            
 */
public 
class SimpleLogMessage 
  implements Cloneable, Glueable, Serializable
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
  SimpleLogMessage() 
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
  SimpleLogMessage
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
  SimpleLogMessage
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
  SimpleLogMessage
  (
   SimpleLogMessage log  
  ) 
  {
    pTimeStamp = log.getTimeStamp();
    pAuthor    = log.getAuthor();
    pMessage   = log.getMessage();
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
    if(pTimeStamp == null)
      throw new IllegalStateException(); 
    return (Date) pTimeStamp.clone();
  }

  /**
   * Get the name of the user who wrote the message. 
   */ 
  public String
  getAuthor() 
  {
    if(pAuthor == null)
      throw new IllegalStateException(); 
    return pAuthor;
  }

  /**
   * Get the message text. 
   */ 
  public String
  getMessage() 
  {
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
    if((obj != null) && (obj instanceof SimpleLogMessage)) {
      SimpleLogMessage log = (SimpleLogMessage) obj;

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
      throw new IllegalStateException();      
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

  private static final long serialVersionUID = 3405770516028643619L;



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



