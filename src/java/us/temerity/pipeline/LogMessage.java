// $Id: LogMessage.java,v 1.11 2009/10/30 04:56:31 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

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

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
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
   *   
   * @param impostor
   *   The name of the user who requested the check-in.
   */ 
  public 
  LogMessage
  (
   String author, 
   String msg, 
   String rootName, 
   VersionID rootVersionID,
   String impostor
  ) 
  {
    super(author, msg);

    pRootName      = rootName;
    pRootVersionID = rootVersionID;
    pImpostor      = impostor;
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
  
  /**
   * Get the name of the user who requested the check-in.
   * <p>
   * This field will only have a value in cases where the requesting user is different than
   * the owner of the working area the check-in was submitted from.  In all other cases this
   * value will be <code>null</code>.
   * <p>
   * Nodes created in versions of Pipeline before 2.4.13 will never have a value other
   * than <code>null</code>.
   * 
   * @return
   *   The name of the user who requested the check-in or <code>null</code>
   */
  public String
  getImpostor()
  {
    return pImpostor;
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
  @Override
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof LogMessage)) {
      LogMessage log = (LogMessage) obj;

      if((getTimeStamp() == log.getTimeStamp()) && 
	 getAuthor().equals(log.getAuthor()) &&
	 getMessage().equals(log.getMessage()) &&
	 (((pRootName == null) && (log.pRootName == null)) ||
	  ((pRootName != null) && pRootName.equals(log.pRootName))) && 
	 (((pRootVersionID == null) && (log.pRootVersionID == null)) ||
	  ((pRootVersionID != null) && pRootVersionID.equals(log.pRootVersionID))) &&
	 (((pImpostor == null) && (log.pImpostor == null)) ||
	  ((pImpostor != null) && pImpostor.equals(log.pImpostor))))
	return true;
    }
    return false;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
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
    
    if (pImpostor != null)
      encoder.encode("Impostor", pImpostor);
  }

  @Override
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
    
    String impostor = (String) decoder.decode("Impostor");
    if(impostor != null) 
      pImpostor = impostor;
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
  
  /**
   * The name of the user that requested this check-in if it was not the user whose working
   * area the check-in originated from.  If the working area and the requesting user were the
   * same, this field will be null.  It will always be null for nodes created in versions of
   * Pipeline before 2.4.13. impostor
   */
  private String pImpostor;
}



