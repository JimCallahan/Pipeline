// $Id: EditedNodeEvent.java,v 1.2 2007/03/28 19:56:42 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E D I T E D   N O D E    E V E N T                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * An event for the period of time in which an Editor plugin was launched which was capable 
 * of modifying the files associated with a working version of a node. <P> 
 * 
 * Using an Editor plugin to "View" a node in a working area now owned by the current 
 * user in which the files associated with the node can't be modified are not recorded.
 */
public
class EditedNodeEvent
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
  EditedNodeEvent() 
  {}

  /** 
   * Create the event.
   * 
   * @param started
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when editing 
   *   was begun. 
   * 
   * @param finished
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when editing 
   *   was completed or <CODE>null</CODE> if still editing.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param ename 
   *   The short name of the Editor plugin.
   * 
   * @param evid
   *   The plugin revision number.
   * 
   * @param evendor
   *   The name of the plugin vendor.
   * 
   * @param hostname
   *   The full name of the host on which the Editor was run.
   * 
   * @param imposter
   *   The name of the user impersonating the owner of the node being edited or 
   *   <CODE>null</CODE> if being edited by the node owner themselves.
   */
  public
  EditedNodeEvent
  ( 
   long started, 
   Long finished, 
   NodeID nodeID, 
   String ename,  
   VersionID evid,
   String evendor, 
   String hostname, 
   String imposter
  ) 
  {
    super(started, NodeEventOp.Edited, nodeID);

    pFinishedStamp = finished; 

    if(ename == null) 
      throw new IllegalArgumentException("The Editor plugin name cannot be (null)");
    pEditorName = ename;
    
    if(evid == null) 
      throw new IllegalArgumentException("The Editor plugin version cannot be (null)");
    pEditorVersionID = evid;

    if(evendor == null) 
      throw new IllegalArgumentException("The Editor plugin vendor cannot be (null)");
    pEditorVendor = evendor;

    if(hostname == null) 
      throw new IllegalArgumentException("The hostname cannot be (null)");
    pHostname = hostname;

    pImposter = imposter;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the Editor is currently still running.
   */ 
  public boolean 
  isEditing() 
  {
    return (pFinishedStamp == null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when editing 
   * was completed.
   */ 
  public void 
  setFinishedStamp
  (
   long finished
  ) 
  {
    pFinishedStamp = finished;
  }

  /** 
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when editing 
   * was completed or <CODE>null</CODE> if editing isn't finished yet.
   */ 
  public Long
  getFinishedStamp() 
  {
    return pFinishedStamp; 
  }

  /** 
   * Get the interval of time during which the node was edited or 
   * <CODE>null</CODE> if editing isn't finished yet.
   */ 
  public TimeInterval
  getInterval() 
  {
    if(pFinishedStamp != null) 
      return new TimeInterval(pTimeStamp, pFinishedStamp);
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the Editor plugin. 
   */ 
  public String
  getEditorName() 
  {
    return pEditorName;
  }

  /**
   * Get the revision number of the Editor plugin. 
   */ 
  public VersionID
  getEditorVersionID()
  {
    return pEditorVersionID;
  }
  
  /**
   * Get the name of the Editor plugin vendor. 
   */ 
  public String
  getEditorVendor()
  {
    return pEditorVendor; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The full name of the host on which the Editor was run.
   */ 
  public String 
  getHostname() 
  {
    return pHostname;   
  }

  /**
   * Get the name of the user impersonating the owner of the node being edited or 
   * <CODE>null</CODE> if being edited by the node owner themselves.
   */ 
  public String
  getImposter()
  {
    return pImposter; 
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

    encoder.encode("FinishedStamp", pFinishedStamp);
    encoder.encode("EditorName", pEditorName); 
    encoder.encode("EditorVersionID", pEditorVersionID); 
    encoder.encode("EditorVendor", pEditorVendor); 
    encoder.encode("Hostname", pHostname); 

    if(pImposter != null) 
      encoder.encode("Imposter", pImposter); 
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    Long stamp = (Long) decoder.decode("FinishedStamp");
    if(stamp == null)
      throw new GlueException("The \"FinishedStamp\" was missing!");
    if(stamp <= 0L) 
      throw new GlueException("The \"FinishedStamp\" was illegal!");
    pFinishedStamp = stamp;
    
    String ename = (String) decoder.decode("EditorName");
    if(ename == null) 
      throw new GlueException("The \"EditorName\" was missing!");
    pEditorName = ename;

    VersionID evid = (VersionID) decoder.decode("EditorVersionID");
    if(evid == null) 
      throw new GlueException("The \"EditorVersionID\" was missing!");
    pEditorVersionID = evid;

    String vendor = (String) decoder.decode("EditorVendor");
    if(vendor == null) 
      throw new GlueException("The \"EditorVendor\" was missing!");
    pEditorVendor = vendor;

    String host = (String) decoder.decode("Hostname"); 
    if(host == null) 
      throw new GlueException("The \"Hostname\" was missing!");
    pHostname = host;

    pImposter = (String) decoder.decode("Imposter");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5830882229794366886L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when editing 
   * was completed. 
   */
  private Long  pFinishedStamp; 


  /**
   * The name of the Editor plugin. 
   */ 
  private String  pEditorName;

  /**
   * The revision number of the Editor plugin. 
   */ 
  private VersionID  pEditorVersionID;
  
  /**
   * The name of the Editor plugin vendor. 
   */ 
  private String  pEditorVendor; 


  /**
   * The full name of the host on which the Editor was run.
   */ 
  private String  pHostname; 
  
  /**
   * The name of the user impersonating the owner of the node being edited or 
   * <CODE>null</CODE> if being edited by the node owner themselves.
   */ 
  private String  pImposter; 

}

