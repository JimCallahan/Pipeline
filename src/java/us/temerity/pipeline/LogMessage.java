// $Id: LogMessage.java,v 1.8 2004/10/30 13:42:19 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G   M E S S A G E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**                                                                                   
 * A node check-in message.
 */
public 
class LogMessage 
  extends SimpleLogMessage
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  LogMessage() 
  {
    super();
  }
  
  /**
   * Construct a new check-in message owned by the given author (recording the time). 
   * 
   * @param author
   *   The name of the user creating the message.
   * 
   * @param msg 
   *   The message text.
   * 
   * @param rootName
   *   The fully resolved name of the root node of the check-in operation.
   * 
   * @param rootVersionID
   *   The revision number of the new version of the root node created by the check-in 
   *   operation.
   */ 
  public 
  LogMessage
  (
   String author, 
   String msg, 
   String rootName, 
   VersionID rootVersionID 
  ) 
  {
    super(author, msg);

    pRootName      = rootName;
    pRootVersionID = rootVersionID;
  }

  /**
   * Construct a new message (recording the current author and time). 
   * 
   * @param msg 
   *   The message text.
   * 
   * @param rootName
   *   The fully resolved name of the root node of the check-in operation.
   * 
   * @param rootVersionID
   *   The revision number of the new version of the root node created by the check-in 
   *   operation.
   */ 
  public 
  LogMessage
  (
   String msg, 
   String rootName, 
   VersionID rootVersionID  
  ) 
  {
    super(msg);

    pRootName      = rootName;
    pRootVersionID = rootVersionID;
  }

  /**
   * Copy constructor. 
   */ 
  public 
  LogMessage
  (
   LogMessage log
  ) 
  {
    super(log);
    
    pRootName      = log.getRootName();
    pRootVersionID = log.getRootVersionID();
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved name of the root node of the check-in operation.
   */ 
  public String
  getRootName() 
  {
    return pRootName;
  }

  /**
   * Get the revision number of the new version of the root node created by the check-in 
   * operation.
   */ 
  public VersionID
  getRootVersionID() 
  {
    return pRootVersionID; 
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

      if(getTimeStamp().equals(log.getTimeStamp()) && 
	 getAuthor().equals(log.getAuthor()) &&
	 getMessage().equals(log.getMessage()) &&
	 (((pRootName == null) && (log.pRootName == null)) ||
	  ((pRootName != null) && pRootName.equals(log.pRootName))) && 
	 (((pRootVersionID == null) && (log.pRootVersionID == null)) ||
	  ((pRootVersionID != null) && pRootVersionID.equals(log.pRootVersionID))))
	return true;
    }
    return false;
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

    if(pRootName != null) 
      encoder.encode("RootName", pRootName);

    if(pRootVersionID != null) 
      encoder.encode("RootVersionID", pRootVersionID);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    String name = (String) decoder.decode("RootName");
    if(name != null) 
      pRootName = name;

    VersionID vid = (VersionID) decoder.decode("RootVersionID");
    if(vid != null) 
      pRootVersionID = vid;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9213949194764455768L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the root node of the check-in operation.
   */ 
  private String  pRootName; 
  
  /**
   * The revision number of the new version of the root node created by the check-in 
   * operation.
   */ 
  private VersionID  pRootVersionID; 

}



