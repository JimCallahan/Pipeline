// $Id: PackageVersion.java,v 1.2 2004/05/23 19:56:48 jim Exp $

package us.temerity.pipeline.toolset;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   V E R S I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A read-only version of a package. <P> 
 */
public
class PackageVersion
  extends PackageCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  PackageVersion() 
  {
    super();
  }

  /**
   * Construct a read-only package version from a modifiable package.
   * 
   * @param mod
   *   The modifiable package.
   * 
   * @param vid 
   *   The revision number of the new read-only package.
   * 
   * @param msg 
   *   The creation log message.
   */ 
  public
  PackageVersion
  (
   PackageMod mod, 
   VersionID vid,
   String msg
  ) 
  {
    super(mod);
    
    if(vid == null) 
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;

    if(msg == null) 
      throw new IllegalArgumentException("The creation message cannot be (null)!");
    pMessage = new LogMessage(msg);
  }
  
  /** 
   * Copy constructor. 
   * 
   * @param vsn 
   *   The <CODE>PackageVersion</CODE> to copy.
   */ 
  public 
  PackageVersion
  (
   PackageVersion vsn
  ) 
  {
    super(vsn);

    pVersionID = vsn.getVersionID();
    pMessage   = new LogMessage(vsn.pMessage);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the revision number of this version of the package.
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
  }


  /**
   * Get when the version was created.
   */ 
  public Date
  getTimeStamp() 
  {
    return pMessage.getTimeStamp();
  }

  /**
   * Get the name of the user who created the version.
   */ 
  public String
  getAuthor() 
  {
    return pMessage.getAuthor();
  }

  /**
   * Get the creation log message text. 
   */ 
  public String
  getMessage() 
  {
    return pMessage.getMessage();
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
    return new PackageVersion(this);
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
    encoder.encode("Message", pMessage);
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

    LogMessage msg = (LogMessage) decoder.decode("Message");
    if(msg == null) 
      throw new GlueException("The \"Message\" was missing!");
    pMessage = msg;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1339532394037898044L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of this version. 
   */ 
  private VersionID  pVersionID;       

  /**
   * The descriptive message given at the time this version of the package was created.
   * The timestamp and author of the message are also the timestamp and author of the 
   * package version. <P> 
   */
  private LogMessage  pMessage;        

}
